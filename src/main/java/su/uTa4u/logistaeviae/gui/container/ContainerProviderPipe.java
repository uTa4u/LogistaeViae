package su.uTa4u.logistaeviae.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.items.SlotItemHandler;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import javax.annotation.Nonnull;

public class ContainerProviderPipe extends Container {

    private final TileEntityPipe pipe;

    public ContainerProviderPipe(InventoryPlayer playerInv, TileEntityPipe pipe) {
        this.pipe = pipe;

        // Pipe slots
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 3; ++col) {
                this.addSlotToContainer(new SlotItemHandler(this.pipe.getItems(), row * 3 + col, col * 18 + 62, row * 18 + 17));
            }
        }

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
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        return true;
    }
}
