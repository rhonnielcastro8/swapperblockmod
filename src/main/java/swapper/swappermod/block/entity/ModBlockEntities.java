package swapper.swappermod.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import swapper.swappermod.SwapperMod;
import swapper.swappermod.block.ModBlocks;
import swapper.swappermod.block.entity.custom.SwapperBlockEntity;

public class ModBlockEntities {

    public static BlockEntityType<SwapperBlockEntity> SWAPPER_BLOCK_ENTITY = register("swapper", SwapperBlockEntity::new, ModBlocks.SWAPPER_BLOCK);

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
            Block blocks
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(SwapperMod.MOD_ID, name);
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
    }

    public static void initialize() {
        SwapperMod.LOGGER.info("Register Block Entities for " + SwapperMod.MOD_ID);
    }
}
