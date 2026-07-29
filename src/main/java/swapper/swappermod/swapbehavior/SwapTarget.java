package swapper.swappermod.swapbehavior;

import net.minecraft.world.item.ItemStack;

public interface SwapTarget {
    boolean isEmpty();

    /** Removes and returns one item's worth if extractable, else ItemStack.EMPTY. Mutates the target. */
    ItemStack extractOne();

    /** Attempts to insert the given (count 1) stack. Returns leftover — empty if fully absorbed. */
    ItemStack insertOne(ItemStack stack);
}
