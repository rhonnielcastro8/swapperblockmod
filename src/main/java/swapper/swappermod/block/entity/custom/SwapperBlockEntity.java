package swapper.swappermod.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import swapper.swappermod.block.entity.ImplementedContainer;
import swapper.swappermod.block.entity.ModBlockEntities;
import swapper.swappermod.menu.block.SwapperBlockMenu;

public class SwapperBlockEntity extends BlockEntity implements ImplementedContainer, WorldlyContainer, MenuProvider {
    public static final int TOP_CONTAINER_SIZE = 5;
    public static final int BOTTOM_CONTAINER_SIZE = 5;
    public static final int CONTAINER_SIZE = TOP_CONTAINER_SIZE + BOTTOM_CONTAINER_SIZE;

    public static final int TOP_START = 0;
    public static final int BOTTOM_START = TOP_CONTAINER_SIZE;

    private static final int[] ALL_SLOTS = buildRange(TOP_START, CONTAINER_SIZE);

    public static final int COOLDOWN_TICKS = 4;

    public final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private int cooldown = 0;

    public SwapperBlockEntity(BlockPos position, BlockState blockState) {
        super(ModBlockEntities.SWAPPER_BLOCK_ENTITY, position, blockState);
    }

    private static int[] buildRange(int start, int length) {
        int[] arr = new int[length];
        for (int i = 0; i < length; i++) arr[i] = start + i;
        return arr;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        return saveWithoutMetadata(registryLookup);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.items);
        cooldown = input.getIntOr("Cooldown", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("Cooldown", cooldown);
        super.saveAdditional(output);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.swappermod.swapper");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SwapperBlockMenu(containerId, inventory, this);
    }

    public boolean isTopSlot(int slot) {
        return slot >= TOP_START && slot < TOP_START + TOP_CONTAINER_SIZE;
    }

    public boolean isBottomSlot(int slot) {
        return slot >= BOTTOM_START && slot < BOTTOM_START + BOTTOM_CONTAINER_SIZE;
    }

    public boolean isBottomSlotAvailable() {
        for (int i = BOTTOM_START; i < BOTTOM_START + BOTTOM_CONTAINER_SIZE; i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    public boolean isTopEmpty() {
        for (int i = TOP_START; i < TOP_START + TOP_CONTAINER_SIZE; i++) {
            if (!items.get(i).isEmpty()) return false;
        }
        return true;
    }

    public boolean isBottomEmpty() {
        for (int i = BOTTOM_START; i < BOTTOM_START + BOTTOM_CONTAINER_SIZE; i++) {
            if (!items.get(i).isEmpty()) return false;
        }
        return true;
    }

    public int getNextDispenseSlot() {
        for (int i = TOP_START; i < TOP_START + TOP_CONTAINER_SIZE; i++) {
            if (!items.get(i).isEmpty()) return i;
        }
        return -1;
    }

    public int getInsertBottomSlot(ItemStack incoming) {
        for (int i = BOTTOM_START; i < BOTTOM_START + BOTTOM_CONTAINER_SIZE; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()
                    && ItemStack.isSameItemSameComponents(stack, incoming)
                    && stack.getCount() < stack.getMaxStackSize()) {
                return i;
            }
        }
        for (int i = BOTTOM_START; i < BOTTOM_START + BOTTOM_CONTAINER_SIZE; i++) {
            if (items.get(i).isEmpty()) return i;
        }
        return -1;
    }

    public boolean isOnCooldown() {
        return cooldown > 0;
    }

    public void resetCooldown() {
        cooldown = COOLDOWN_TICKS;
    }

    public void tickCooldown() {
        if (cooldown > 0) cooldown--;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return ALL_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return isTopSlot(slot);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return isBottomSlot(slot);
    }
}