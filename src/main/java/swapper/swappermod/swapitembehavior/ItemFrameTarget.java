package swapper.swappermod.swapitembehavior;

import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;

public final class ItemFrameTarget implements SwapTarget {
    private final ItemFrame frame;

    public ItemFrameTarget(ItemFrame frame) {
        this.frame = frame;
    }

    @Override
    public boolean isEmpty() {
        return frame.getItem().isEmpty();
    }

    @Override
    public ItemStack extractOne() {
        ItemStack current = frame.getItem().copy();

        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }

        frame.setItem(ItemStack.EMPTY, true);
        return current;
    }

    @Override
    public ItemStack insertOne(ItemStack stack) {
        if (!frame.getItem().isEmpty()) {
            return stack;
        }

        ItemStack single = stack.copy();
        single.setCount(1);

        System.out.println("From insertOne method" + single);
        frame.setItem(single, true);
        return ItemStack.EMPTY;
    }
}