package su.uTa4u.logistaeviae.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import javax.annotation.Nonnull;

public abstract class AbstractContainerPipe extends Container {
    private static final int PLAYER_INV_SLOT_COUNT = 4 * 9;

    protected final TileEntityPipe pipe;

    protected AbstractContainerPipe(InventoryPlayer playerInv, TileEntityPipe pipe) {
        this.pipe = pipe;

        // Player main slots
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlotToContainer(new Slot(playerInv, row * 9 + col + 9, col * 18 + 8, row * 18 + 84));
            }
        }

        // Player hotbar slots
        for (int col = 0; col < 9; ++col) {
            this.addSlotToContainer(new Slot(playerInv, col, col * 18 + 8, 142));
        }
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            int size = this.pipe.getItems().getSlots();

            if (index < PLAYER_INV_SLOT_COUNT) {
                // index is in player inventory
                if (!this.mergeItemStack(itemstack1, PLAYER_INV_SLOT_COUNT, PLAYER_INV_SLOT_COUNT + size, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, PLAYER_INV_SLOT_COUNT, false)) {
                // index is in pipe inventory
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer player) {
        return true;
    }
}
