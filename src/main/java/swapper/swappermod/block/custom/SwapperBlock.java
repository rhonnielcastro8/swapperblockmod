package swapper.swappermod.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import swapper.swappermod.block.entity.custom.SwapperBlockEntity;
import swapper.swappermod.swapbehavior.ArmorStandTarget;
import swapper.swappermod.swapbehavior.ItemFrameTarget;
import swapper.swappermod.swapbehavior.SwapTarget;

import java.util.List;

/**
 * The Swapper block. On a redstone pulse (same edge-triggered pattern as vanilla's DispenserBlock)
 * it performs two independent, one-directional operations against whatever is in front of it:
 * extract one item into the bottom (extract) slots, then dispense one item from the top (dispense)
 * slots — see {@link SwapTarget} and its implementations for how each kind of target is handled.
 * <p>
 * Deliberate design choice: unlike a vanilla hopper, Swapper does NOT respect a vanilla container's
 * own {@link net.minecraft.world.WorldlyContainer} extraction restrictions (e.g. a Jukebox normally
 * blocks hopper extraction while a disc is playing). Swapper bypasses that on purpose — the whole
 * point of the block is unrestricted swapping. The one exception is another Swapper: extracting from
 * another Swapper still only reaches its bottom (extract) slots, since that's this mod's own
 * convention, not a vanilla rule.
 */
public class SwapperBlock extends BaseEntityBlock {

    public static final MapCodec<SwapperBlock> CODEC = simpleCodec(SwapperBlock::new);
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;

    /** Vanilla's level event id for the dispenser "click" sound (LevelEvent.SOUND_DISPENSE_FAIL). */
    private static final int SOUND_DISPENSE_CLICK = 1001;

    public SwapperBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos position, BlockState blockState) {
        return new SwapperBlockEntity(position, blockState);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TRIGGERED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SwapperBlockEntity swapperBlockEntity) {
            player.openMenu(swapperBlockEntity);
        }
        return InteractionResult.SUCCESS;
    }

    // ---------- Redstone trigger (same edge-triggered pattern as DispenserBlock) ----------

    @Override
    protected void neighborChanged(
            final BlockState state, final Level level, final BlockPos pos, final Block block,
            final @Nullable Orientation orientation, final boolean movedByPiston
    ) {
        boolean shouldTrigger = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
        boolean isTriggered = state.getValue(TRIGGERED);
        if (shouldTrigger && !isTriggered) {
            level.scheduleTick(pos, this, 4);
            level.setBlock(pos, state.setValue(TRIGGERED, true), 2);
        } else if (!shouldTrigger && isTriggered) {
            level.setBlock(pos, state.setValue(TRIGGERED, false), 2);
        }
    }

    @Override
    protected void tick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        this.swapFrom(level, state, pos);
    }

    @Override
    protected void affectNeighborsAfterRemoval(final BlockState state, final ServerLevel level, final BlockPos pos, final boolean movedByPiston) {
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }

    // ---------- The actual swap ----------

    private void swapFrom(final ServerLevel level, final BlockState state, final BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof SwapperBlockEntity blockEntity)) {
            return;
        }

        Direction facing = state.getValue(FACING);
        BlockPos targetPos = pos.relative(facing);
        Direction sideTouched = facing.getOpposite();

        SwapTarget target = resolveTarget(level, blockEntity, targetPos, sideTouched);

        // Phase 1: extract one item, hopper-style, into our bottom slots.
        extractInto(blockEntity, target);

        // Phase 2: dispense one item, dropper-style, from our top slots — but only if there's
        // still room to receive a swapped-in item later, OR the target has nothing left to give
        // in the first place. Otherwise this would give without ever being able to take back.
        boolean targetEmpty = target == null || target.isEmpty();
        if (blockEntity.isBottomSlotAvailable() || targetEmpty) {
            dispenseFrom(level, pos, blockEntity, target, facing);
        }

        // Always give audible feedback that the swapper fired, whether or not anything actually
        // moved — matches vanilla dispenser's "click" on an empty/failed activation.
        level.levelEvent(SOUND_DISPENSE_CLICK, pos, 0);
    }

    /**
     * Figures out what kind of thing is in front of the Swapper and wraps it in a matching
     * {@link SwapTarget} adapter, or returns null if there's nothing interactable there (in which
     * case dispensing falls back to spawning an item into the world, and extraction is a no-op).
     */
    private @Nullable SwapTarget resolveTarget(final ServerLevel level, final SwapperBlockEntity source, final BlockPos targetPos, final Direction sideTouched) {
        Container container = HopperBlockEntity.getContainerAt(level, targetPos);
        if (container != null) {
            return new ContainerTarget(container, source, sideTouched);
        }

        List<ArmorStand> stands = level.getEntitiesOfClass(ArmorStand.class, new AABB(targetPos));
        if (!stands.isEmpty()) {
            return new ArmorStandTarget(stands.get(0));
        }

        // Only match a frame actually mounted facing back toward the Swapper, not an unrelated
        // frame on a different face of the same block space.
        List<ItemFrame> frames = level.getEntitiesOfClass(ItemFrame.class, new AABB(targetPos));
        for (ItemFrame frame : frames) {
            if (frame.getDirection() == sideTouched) {
                return new ItemFrameTarget(frame);
            }
        }

        return null;
    }

    private void extractInto(final SwapperBlockEntity blockEntity, final @Nullable SwapTarget target) {
        if (target == null || !blockEntity.isBottomSlotAvailable()) {
            return;
        }

        ItemStack extracted = target.extractOne();
        if (extracted.isEmpty()) {
            return;
        }

        int destSlot = blockEntity.getInsertBottomSlot(extracted);
        if (destSlot == -1) {
            // Shouldn't normally happen given the isBottomSlotAvailable() check above, but guard
            // anyway. Note the item has already left the target at this point.
            return;
        }

        ItemStack destStack = blockEntity.getItem(destSlot);
        if (destStack.isEmpty()) {
            blockEntity.setItem(destSlot, extracted);
        } else {
            destStack.grow(extracted.getCount());
            blockEntity.setChanged();
        }
    }

    /**
     * Pushes a single item from the next sequential top (dispense) slot into {@code target}.
     * If there's no target, or it can't take it, the item is spawned into the world in front of
     * the block instead — same fallback vanilla droppers use.
     */
    private void dispenseFrom(final ServerLevel level, final BlockPos pos, final SwapperBlockEntity blockEntity, final @Nullable SwapTarget target, final Direction facing) {
        int dispenseSlot = blockEntity.getNextDispenseSlot();
        if (dispenseSlot == -1) {
            return;
        }

        ItemStack sourceStack = blockEntity.getItem(dispenseSlot);
        ItemStack toEject = sourceStack.copyWithCount(1);

        if (target != null) {
            toEject = target.insertOne(toEject);
        }

        if (!toEject.isEmpty()) {
            Vec3 centerPos = Vec3.atCenterOf(pos);
            Vec3 itemSpawnOffset = centerPos.relative(facing, 0.7);
            DefaultDispenseItemBehavior.spawnItem(level, toEject, 6, facing, itemSpawnOffset);
        }

        sourceStack.shrink(1);
        blockEntity.setChanged();
    }
}