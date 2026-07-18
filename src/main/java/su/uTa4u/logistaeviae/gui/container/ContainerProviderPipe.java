package su.uTa4u.logistaeviae.gui.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.SlotItemHandler;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

public class ContainerProviderPipe extends AbstractContainerPipe {

    public ContainerProviderPipe(InventoryPlayer playerInv, TileEntityPipe pipe) {
        super(playerInv, pipe);

        // Pipe slots
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 3; ++col) {
                this.addSlotToContainer(new SlotItemHandler(this.pipe.getItems(), row * 3 + col, col * 18 + 62, row * 18 + 17));
            }
        }
    }

}
