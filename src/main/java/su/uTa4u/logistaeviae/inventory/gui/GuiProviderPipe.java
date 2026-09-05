package su.uTa4u.logistaeviae.inventory.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import su.uTa4u.logistaeviae.LogistaeViae;
import su.uTa4u.logistaeviae.inventory.container.ContainerProviderPipe;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

public class GuiProviderPipe extends GuiContainer {

    private static final ResourceLocation GUI_TEXTURE = LogistaeViae.resource("textures/gui/provider.png");

    private final InventoryPlayer playerInv;
    private final TileEntityPipe pipe;

    public GuiProviderPipe(InventoryPlayer playerInv, TileEntityPipe pipe) {
        super(new ContainerProviderPipe(playerInv, pipe));
        this.playerInv = playerInv;
        this.pipe = pipe;
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