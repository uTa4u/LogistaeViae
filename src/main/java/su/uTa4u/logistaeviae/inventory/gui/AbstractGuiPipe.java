package su.uTa4u.logistaeviae.inventory.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import su.uTa4u.logistaeviae.LogistaeViae;
import su.uTa4u.logistaeviae.block.BlockPipe;
import su.uTa4u.logistaeviae.inventory.GuiHandler;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import java.util.Objects;

public abstract class AbstractGuiPipe extends GuiContainer {

    private final ResourceLocation texture;
    private final InventoryPlayer playerInv;
    private final TileEntityPipe pipe;

    public AbstractGuiPipe(InventoryPlayer playerInv, TileEntityPipe pipe) {
        this(playerInv, pipe, pipe.getBlockType());
    }

    private AbstractGuiPipe(InventoryPlayer playerInv, TileEntityPipe pipe, BlockPipe block) {
        super(Objects.requireNonNull(GuiHandler.getServerBiFunction(block.getGuiID())).apply(playerInv, pipe));
        this.texture = LogistaeViae.resource("textures/gui/" + block.getName() + ".png");
        this.playerInv = playerInv;
        this.pipe = pipe;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.pipe.getBlockType().getLocalizedName(), 8, 6, 4210752);
        this.fontRenderer.drawString(this.playerInv.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
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
