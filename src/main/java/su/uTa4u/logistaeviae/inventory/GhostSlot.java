package su.uTa4u.logistaeviae.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class GhostSlot extends SlotItemHandler {

    public GhostSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }


    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        this.putStack(stack.copy());
        return false;
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        ItemStack heldStack = player.inventory.getItemStack();
        if (heldStack.isEmpty()) {
            this.putStack(ItemStack.EMPTY);
        } else {
            this.putStack(heldStack.copy());
        }
        return false;
    }
}
