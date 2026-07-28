package swapper.swappermod.menu.block;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import swapper.swappermod.block.entity.custom.SwapperBlockEntity;
import swapper.swappermod.menu.ModMenuTypes;

public class SwapperBlockMenu extends AbstractContainerMenu {

    private static final int SLOTS_COUNT = SwapperBlockEntity.CONTAINER_SIZE; // 10 (5 top + 5 bottom)

    private static final int CONTAINER_START = 0;
    private static final int CONTAINER_END = SLOTS_COUNT;
    private static final int INVENTORY_START = CONTAINER_END;
    private static final int INVENTORY_END = INVENTORY_START + Inventory.INVENTORY_SIZE;

    // Custom Swapper Slots
    private static final int ROW_START_X = 44;
    private static final int DISPENSE_ROW_Y = 20;
    private static final int EXTRACT_ROW_Y = 44;

    // Vanilla Player Inventory Alignments
    private static final int INVENTORY_START_X = 8;
    private static final int INVENTORY_START_Y = 84;

    private static final int SLOT_SIZE = 18;

    private final Container container;

    // Client-side constructor
    public SwapperBlockMenu(final int containerId, final Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(SLOTS_COUNT));
    }

    // Server-side constructor
    public SwapperBlockMenu(final int containerId, final Inventory inventory, final Container container) {
        super(ModMenuTypes.SWAPPER_BLOCK, containerId);
        checkContainerSize(container, SLOTS_COUNT);
        this.container = container;

        container.startOpen(inventory.player);

        addRow(SwapperBlockEntity.TOP_START, SwapperBlockEntity.TOP_CONTAINER_SIZE, DISPENSE_ROW_Y);
        addRow(SwapperBlockEntity.BOTTOM_START, SwapperBlockEntity.BOTTOM_CONTAINER_SIZE, EXTRACT_ROW_Y);

        this.addStandardInventorySlots(inventory, INVENTORY_START_X, INVENTORY_START_Y);
    }

    private void addRow(int firstContainerIndex, int count, int y) {
        for (int x = 0; x < count; x++) {
            this.addSlot(new Slot(
                    this.container,
                    firstContainerIndex + x,
                    ROW_START_X + x * SLOT_SIZE,
                    y
            ));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack clicked = stack.copy();

        if (slotIndex < CONTAINER_END) {
            // Clicked inside the swapper's own slots -> try moving to player inventory
            if (!this.moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Shift-click into dispense (top) slots only
            int topStart = SwapperBlockEntity.TOP_START;
            int topEnd = SwapperBlockEntity.TOP_START + SwapperBlockEntity.TOP_CONTAINER_SIZE;
            if (!this.moveItemStackTo(stack, topStart, topEnd, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return clicked;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }
}