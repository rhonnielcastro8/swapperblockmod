package swapper.swappermod.swapitembehavior;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;

public final class ArmorStandTarget implements SwapTarget {

    private static final EquipmentSlot[] SLOT_PRIORITY = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND
    };

    private final ArmorStand standEntity;

    public ArmorStandTarget(ArmorStand standEntity) {
        this.standEntity = standEntity;
    }

    @Override
    public boolean isEmpty() {
        for (EquipmentSlot slot : SLOT_PRIORITY) {
            if (!standEntity.getItemBySlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack extractOne() {
        for (EquipmentSlot slot : SLOT_PRIORITY) {
            ItemStack current = standEntity.getItemBySlot(slot);
            if (!current.isEmpty()) {
                ItemStack extracted = current.split(1);
                if (current.isEmpty()) {
                    standEntity.setItemSlot(slot, ItemStack.EMPTY);
                }
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertOne(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // Vanilla Dispenser Check: asks the Armor Stand if it can equip this item
        if (standEntity.canEquipWithDispenser(stack)) {
            EquipmentSlot slot = standEntity.getEquipmentSlotForItem(stack);

            // Only equip if that slot is empty
            if (standEntity.getItemBySlot(slot).isEmpty()) {
                ItemStack equip = stack.split(1);
                standEntity.setItemSlot(slot, equip);
                return stack; // Returns remaining stack
            }
        }

        // Return unchanged stack if equip failed or slot was occupied
        return stack;
    }
}