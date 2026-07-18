package su.uTa4u.logistaeviae.gui;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import su.uTa4u.logistaeviae.LogistaeViae;
import su.uTa4u.logistaeviae.gui.container.AbstractContainerPipe;
import su.uTa4u.logistaeviae.gui.container.ContainerProviderPipe;
import su.uTa4u.logistaeviae.gui.container.ContainerSupplierPipe;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public final class GuiHandler implements IGuiHandler {

    private final Int2ObjectMap<BiFunction<InventoryPlayer, TileEntityPipe, AbstractContainerPipe>> serverGuiById = new Int2ObjectArrayMap<>();
    private final Int2ObjectMap<BiFunction<InventoryPlayer, TileEntityPipe, GuiContainer>> clientGuiById = new Int2ObjectArrayMap<>();

    public static final int INVALID_GUI_ID = -1;
    public static final int PIPE_PROVIDER_ID = 0;
    public static final int PIPE_SUPPLIER_ID = 1;

    public GuiHandler() {
        this.registerGui(PIPE_PROVIDER_ID, ContainerProviderPipe::new);
        this.registerGui(PIPE_SUPPLIER_ID, ContainerSupplierPipe::new);
    }

    private void registerGui(
            int ID,
            BiFunction<InventoryPlayer, TileEntityPipe, AbstractContainerPipe> serverSupplier
    ) {
        if (this.serverGuiById.put(ID, serverSupplier) != null) {
            LogistaeViae.LOGGER.warn("Server gui with id = {} was overwritten", ID);
        }
        BiFunction<InventoryPlayer, TileEntityPipe, GuiContainer> clientSupplier =
                (playerInv, pipe) -> new GuiPipe(serverSupplier.apply(playerInv, pipe), playerInv, pipe);
        if (this.clientGuiById.put(ID, clientSupplier) != null) {
            LogistaeViae.LOGGER.warn("Client gui with id = {} was overwritten", ID);
        }
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == INVALID_GUI_ID) return null;
        TileEntityPipe pipe = TileEntityPipe.getOrNull(world.getTileEntity(new BlockPos(x, y, z)));
        if (pipe == null) return null;
        BiFunction<InventoryPlayer, TileEntityPipe, AbstractContainerPipe> func = this.serverGuiById.get(ID);
        if (func == null) return null;
        return func.apply(player.inventory, pipe);
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == INVALID_GUI_ID) return null;
        TileEntityPipe pipe = TileEntityPipe.getOrNull(world.getTileEntity(new BlockPos(x, y, z)));
        if (pipe == null) return null;
        BiFunction<InventoryPlayer, TileEntityPipe, GuiContainer> func = this.clientGuiById.get(ID);
        if (func == null) return null;
        return func.apply(player.inventory, pipe);
    }
}
