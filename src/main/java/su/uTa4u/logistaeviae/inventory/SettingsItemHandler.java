package su.uTa4u.logistaeviae.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class SettingsItemHandler extends ItemStackHandler {

    public SettingsItemHandler(int size) {
        super(size);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        validateSlotIndex(slot);
        if (!simulate) {
            this.stacks.set(slot, stack.copy());
            onContentsChanged(slot);
        }
        return stack;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        validateSlotIndex(slot);
        if (!simulate) {
            this.stacks.set(slot, ItemStack.EMPTY);
            onContentsChanged(slot);
        }
        return ItemStack.EMPTY;
    }

}
