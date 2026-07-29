package swapper.swappermod.swapbehavior;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import swapper.swappermod.block.custom.SwapperBlock;
import swapper.swappermod.block.entity.custom.SwapperBlockEntity;

/** Wraps an ArmorStand entity, treating its 6 equipment slots as a fixed-priority slot list. */
public final class ArmorStandTarget implements SwapTarget {
    private static final EquipmentSlot[] SLOT_PRIORITY = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
    };

    private final ArmorStand standEntity;

    ArmorStandTarget(ArmorStand standEntity) {
        this.standEntity = standEntity;
    }

    @Override
    public boolean isEmpty() {
        for (EquipmentSlot slot : SLOT_PRIORITY) {
            if (!standEntity.getItemBySlot(slot).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack extractOne() {
        for (EquipmentSlot slot : SLOT_PRIORITY) {
            ItemStack current = standEntity.getItemBySlot(slot);
            if (!current.isEmpty()) {
                standEntity.setItemSlot(slot, ItemStack.EMPTY);
                return current;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertOne(ItemStack stack) {
        for (EquipmentSlot slot : SLOT_PRIORITY) {
            if (standEntity.getItemBySlot(slot).isEmpty()) {
                standEntity.setItemSlot(slot, stack);
                return ItemStack.EMPTY;
            }
        }
        return stack; // no empty slot — caller spawns it into the world instead
    }
}