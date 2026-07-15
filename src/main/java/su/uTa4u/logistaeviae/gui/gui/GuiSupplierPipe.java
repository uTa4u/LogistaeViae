package su.uTa4u.logistaeviae.gui.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import su.uTa4u.logistaeviae.LogistaeViae;
import su.uTa4u.logistaeviae.gui.container.ContainerProviderPipe;
import su.uTa4u.logistaeviae.gui.container.ContainerSupplierPipe;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

public class GuiSupplierPipe extends GuiContainer {

    private static final ResourceLocation GUI_TEXTURE = LogistaeViae.resource("textures/gui/supplier.png");

    public GuiSupplierPipe(InventoryPlayer playerInv, TileEntityPipe pipe) {
        super(new ContainerSupplierPipe(playerInv, pipe));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        this.drawTexturedModalRect(
                (this.width - this.xSize) / 2,
                (this.height - this.ySize) / 2,
                0,
                0,
                this.xSize,
                this.ySize
        );
    }
}
