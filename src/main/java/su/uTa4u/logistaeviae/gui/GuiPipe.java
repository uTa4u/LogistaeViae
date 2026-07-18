package su.uTa4u.logistaeviae.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import su.uTa4u.logistaeviae.LogistaeViae;
import su.uTa4u.logistaeviae.gui.container.AbstractContainerPipe;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

public class GuiPipe extends GuiContainer {

    private final InventoryPlayer playerInv;
    private final TileEntityPipe pipe;
    private final ResourceLocation texture;

    public GuiPipe(AbstractContainerPipe inventorySlots, InventoryPlayer playerInv, TileEntityPipe pipe) {
        super(inventorySlots);
        this.playerInv = playerInv;
        this.pipe = pipe;
        this.texture = LogistaeViae.resource("textures/gui/" + this.pipe.getBlockType().getName() + ".png");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.pipe.getBlockType().getLocalizedName(), 8, 6, 4210752);
        this.fontRenderer.drawString(this.playerInv.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.mc.getTextureManager().bindTexture(this.texture);
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
