package su.uTa4u.logistaeviae.block;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import su.uTa4u.logistaeviae.Tags;
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
    public static final Block PIPE_SUPPLIER = registerPipe("supplier");
    public static final Block PIPE_PROVIDER = registerPipe("provider");

    private static Block register(Supplier<Block> supplier) {
        Block block = supplier.get();
        BLOCKS.add(block);
        return block;
    }

    private static Block registerPipe(String name) {
        BlockPipe block = new BlockPipe(name);
        BLOCKS.add(block);
        PIPES.add(block);
        return block;
    }

    @SubscribeEvent
    public static void onRegisterBlock(RegistryEvent.Register<Block> event) {
        GameRegistry.registerTileEntity(TileEntityPipe.class, new ResourceLocation(Tags.MOD_ID, "tileentity_pipe"));
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
