package swapper.swappermod.swapbehavior;

import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;

/** Wraps an ItemFrame or GlowItemFrame entity (GlowItemFrame extends ItemFrame, same API). */
public final class ItemFrameTarget implements SwapTarget {
    private final ItemFrame frame;

    ItemFrameTarget(ItemFrame frame) {
        this.frame = frame;
    }

    @Override
    public boolean isEmpty() {
        return frame.getItem().isEmpty();
    }

    @Override
    public ItemStack extractOne() {
        ItemStack current = frame.getItem();
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        frame.setItem(ItemStack.EMPTY);
        return current;
    }

    @Override
    public ItemStack insertOne(ItemStack stack) {
        if (!frame.getItem().isEmpty()) {
            return stack; // already holding something — caller spawns this one into the world instead
        }
        frame.setItem(stack);
        return ItemStack.EMPTY;
    }
}