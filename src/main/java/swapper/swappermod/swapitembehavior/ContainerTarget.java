package swapper.swappermod.swapitembehavior;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import swapper.swappermod.block.entity.custom.SwapperBlockEntity;

public final class ContainerTarget implements SwapTarget {
    private final Container container;
    private final SwapperBlockEntity source;
    private final Direction sideTouched;

    public ContainerTarget(Container container, SwapperBlockEntity source, Direction sideTouched) {
        this.container = container;
        this.source = source;
        this.sideTouched = sideTouched;
    }

    @Override
    public boolean isEmpty() {
        if (container instanceof JukeboxBlockEntity jukebox) {
            return jukebox.getTheItem().isEmpty();
        }

        for (int i = 0; i < container.getContainerSize(); i++) {
            if (!container.getItem(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack extractOne() {
        // Special case for Jukeboxes
        if (container instanceof JukeboxBlockEntity jukebox) {
            ItemStack record = jukebox.getTheItem();
            if (record.isEmpty()) {
                return ItemStack.EMPTY;
            }

            // In modern Minecraft (1.20+), JukeboxBlockEntity has popOutRecord() or clearContent()
            // Setting the item to empty and notifying changes ensures the music stops & blockstate updates
            ItemStack taken = record.copy();
            taken.setCount(1);

            record.shrink(1);
            if (record.isEmpty()) {
                // Clears the item, stops music playback, and updates the blockstate
                jukebox.setTheItem(ItemStack.EMPTY);
            } else {
                jukebox.setTheItem(record);
            }
            jukebox.setChanged();
            return taken;
        }

        // Standard Container Extraction Logic
        boolean targetIsSwapper = container instanceof SwapperBlockEntity;

        // Respect WorldlyContainer / Side-based extraction rules if implemented (e.g. Composter, Furnace)
        if (container instanceof WorldlyContainer worldly) {
            int[] slots = worldly.getSlotsForFace(sideTouched);
            for (int slot : slots) {
                ItemStack candidate = container.getItem(slot);
                if (candidate.isEmpty() || !worldly.canTakeItemThroughFace(slot, candidate, sideTouched)) {
                    continue;
                }

                ItemStack taken = candidate.copyWithCount(1);
                candidate.shrink(1);
                container.setChanged();
                return taken;
            }
            return ItemStack.EMPTY;
        }

        // Default extraction for general Containers
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack candidate = container.getItem(i);
            if (candidate.isEmpty()) {
                continue;
            }

            if (targetIsSwapper && !((SwapperBlockEntity) container).isBottomSlot(i)) {
                continue;
            }

            ItemStack taken = candidate.copyWithCount(1);
            candidate.shrink(1);
            container.setChanged();
            return taken;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertOne(ItemStack stack) {
        return HopperBlockEntity.addItem(source, container, stack, sideTouched);
    }
}