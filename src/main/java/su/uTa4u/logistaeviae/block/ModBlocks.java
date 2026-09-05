package su.uTa4u.logistaeviae.block;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import su.uTa4u.logistaeviae.LogistaeViae;
import su.uTa4u.logistaeviae.inventory.GuiHandler;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@EventBusSubscriber
public final class ModBlocks {
    private ModBlocks() {
    }

    private static final List<Block> BLOCKS = new ArrayList<>();

    public static final List<BlockPipe> PIPES = new ArrayList<>();

    public static final Block PIPE_COBBLESTONE = registerPipe("cobblestone");
    public static final Block PIPE_BASIC = registerPipe("basic");
    // TODO: make pipe with gui a separate class
    public static final Block PIPE_PROVIDER = registerPipeWithGui("provider", GuiHandler.PIPE_PROVIDER_ID);
    public static final Block PIPE_SUPPLIER = registerPipeWithGui("supplier", GuiHandler.PIPE_SUPPLIER_ID);

    private static Block register(Supplier<Block> supplier) {
        Block block = supplier.get();
        BLOCKS.add(block);
        return block;
    }

    private static Block registerPipe(String name) {
        return registerPipeWithGui(name, GuiHandler.INVALID_GUI_ID);
    }

    private static Block registerPipeWithGui(String name, int guiID) {
        BlockPipe block = new BlockPipe(name, guiID);
        BLOCKS.add(block);
        PIPES.add(block);
        return block;
    }

    @SubscribeEvent
    public static void onRegisterBlock(RegistryEvent.Register<Block> event) {
        GameRegistry.registerTileEntity(TileEntityPipe.class, LogistaeViae.resource("tileentity_pipe"));
        for (Block block : BLOCKS) {
            event.getRegistry().register(block);
        }
    }

    @SubscribeEvent
    public static void onRegisterBlockItem(RegistryEvent.Register<Item> event) {
        for (Block block : BLOCKS) {
            event.getRegistry().register(new ItemBlock(block).setRegistryName(Objects.requireNonNull(block.getRegistryName())));
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerRender() {
        // TODO: try moving inside an event
        final ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        for (Block block : BLOCKS) {
            itemModelMesher.register(Item.getItemFromBlock(block), 0, new ModelResourceLocation(Objects.requireNonNull(block.getRegistryName()), "inventory"));
        }
    }
}
