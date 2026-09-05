package su.uTa4u.logistaeviae.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import su.uTa4u.logistaeviae.LogistaeViae;
import su.uTa4u.logistaeviae.inventory.container.AbstractContainerPipe;
import su.uTa4u.logistaeviae.inventory.container.ContainerProviderPipe;
import su.uTa4u.logistaeviae.inventory.container.ContainerSupplierPipe;
import su.uTa4u.logistaeviae.inventory.gui.AbstractGuiPipe;
import su.uTa4u.logistaeviae.inventory.gui.GuiProviderPipe;
import su.uTa4u.logistaeviae.inventory.gui.GuiSupplierPipe;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.function.BiFunction;

public final class GuiHandler implements IGuiHandler {

    private static final ArrayList<BiFunction<InventoryPlayer, TileEntityPipe, AbstractContainerPipe>> SERVER_GUI_BY_ID = new ArrayList<>();
    private static final ArrayList<BiFunction<InventoryPlayer, TileEntityPipe, AbstractGuiPipe>> CLIENT_GUI_BY_ID = new ArrayList<>();

    public static final int INVALID_GUI_ID = -1;
    public static final int PIPE_PROVIDER_ID = registerGui(ContainerProviderPipe::new, GuiProviderPipe::new);
    public static final int PIPE_SUPPLIER_ID = registerGui(ContainerSupplierPipe::new, GuiSupplierPipe::new);

    private static int registerGui(
            BiFunction<InventoryPlayer, TileEntityPipe, AbstractContainerPipe> serverSupplier,
            BiFunction<InventoryPlayer, TileEntityPipe, AbstractGuiPipe> clientSupplier
    ) {
        int serverID = SERVER_GUI_BY_ID.size();
        if (SERVER_GUI_BY_ID.add(serverSupplier)) {
            LogistaeViae.LOGGER.warn("Server gui with id = {} was overwritten", serverID);
        }
        int clientID = CLIENT_GUI_BY_ID.size();
        if (CLIENT_GUI_BY_ID.add(clientSupplier)) {
            LogistaeViae.LOGGER.warn("Client gui with id = {} was overwritten", clientID);
        }
        if (serverID == clientID) {
            return serverID;
        } else {
            throw new RuntimeException("ID mismatch");
        }
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == INVALID_GUI_ID) return null;
        TileEntityPipe pipe = TileEntityPipe.getOrNull(world.getTileEntity(new BlockPos(x, y, z)));
        if (pipe == null) return null;
        BiFunction<InventoryPlayer, TileEntityPipe, AbstractContainerPipe> func = SERVER_GUI_BY_ID.get(ID);
        if (func == null) return null;
        return func.apply(player.inventory, pipe);
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == INVALID_GUI_ID) return null;
        TileEntityPipe pipe = TileEntityPipe.getOrNull(world.getTileEntity(new BlockPos(x, y, z)));
        if (pipe == null) return null;
        BiFunction<InventoryPlayer, TileEntityPipe, AbstractGuiPipe> func = CLIENT_GUI_BY_ID.get(ID);
        if (func == null) return null;
        return func.apply(player.inventory, pipe);
    }

    @Nullable
    public static BiFunction<InventoryPlayer, TileEntityPipe, AbstractContainerPipe> getServerBiFunction(int ID) {
        return SERVER_GUI_BY_ID.get(ID);
    }
}
