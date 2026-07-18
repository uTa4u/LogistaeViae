package su.uTa4u.logistaeviae.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import javax.annotation.Nonnull;

public abstract class AbstractContainerPipe extends Container {

    protected final TileEntityPipe pipe;

    protected AbstractContainerPipe(InventoryPlayer playerInv, TileEntityPipe pipe) {
        this.pipe = pipe;

        // Player hotbar slots
        for (int col = 0; col < 9; ++col) {
            this.addSlotToContainer(new Slot(playerInv, col, col * 18 + 8, 142));
        }

        // Player main slots
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlotToContainer(new Slot(playerInv, row * 9 + col + 9, col * 18 + 8, row * 18 + 84));
            }
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

            if (index < size) {
                if (!this.mergeItemStack(itemstack1, size, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, size, false)) {
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
