package su.uTa4u.logistaeviae.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import javax.annotation.Nonnull;

public class ContainerSupplierPipe extends Container {

    private final TileEntityPipe pipe;

    public ContainerSupplierPipe(InventoryPlayer playerInv, TileEntityPipe pipe) {
        this.pipe = pipe;
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        return true;
    }
}
