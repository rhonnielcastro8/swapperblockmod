package swapper.swappermod.block;

import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import swapper.swappermod.SwapperMod;

public class ModBlockItemIds {
    public static final BlockItemId SWAPPER_BLOCK = create("swapper");

    private static BlockItemId create(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(SwapperMod.MOD_ID, name);
        return BlockItemId.create(id, id);
    }
}
