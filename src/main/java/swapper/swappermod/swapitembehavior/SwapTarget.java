package swapper.swappermod.swapitembehavior;

import net.minecraft.world.item.ItemStack;

public interface SwapTarget {
    boolean isEmpty();

    ItemStack extractOne();
    ItemStack insertOne(ItemStack stack);
}
