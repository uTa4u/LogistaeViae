package su.uTa4u.logistaeviae.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import javax.annotation.Nonnull;

public final class TileEntityPipeRenderer extends TileEntitySpecialRenderer<TileEntityPipe> {

    @Override
    public void render(@Nonnull TileEntityPipe pipe, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();

        ItemStack itemStack = new ItemStack(Item.getItemFromBlock(Blocks.SAND));

        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        GlStateManager.scale(0.5, 0.5, 0.5);

        // TODO: only render if in distance (like 64 blocks or smth)
        renderItem.renderItem(itemStack, ItemCameraTransforms.TransformType.FIXED);

        GlStateManager.popMatrix();
    }

}
