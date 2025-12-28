package su.uTa4u.logistaeviae.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants;
import su.uTa4u.logistaeviae.block.BlockPipe;
import su.uTa4u.logistaeviae.client.model.PipeModelManager;
import su.uTa4u.logistaeviae.client.render.PipeInstancedRenderer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class TileEntityPipe extends TileEntity {
    public static final String TAG_CONNECTIONS = "Connections";

    private final Set<EnumFacing> connections = EnumSet.noneOf(EnumFacing.class);

    private static final Random RNG = new Random(1);
    private static final Item[] TEST_ITEMS = new Item[]{Items.APPLE, Items.BREAD, Item.getItemFromBlock(Blocks.SAND), Item.getItemFromBlock(Blocks.STONE)};
    public final Item item;

    private byte cachedTextureID;

    public TileEntityPipe() {
        super();
        this.cachedTextureID = -1;
        this.item = TEST_ITEMS[RNG.nextInt(TEST_ITEMS.length)];
    }

    public byte getCachedTextureID() {
        if (this.cachedTextureID == -1) {
            TextureAtlasSprite tex = Minecraft.getMinecraft()
                    .getTextureMapBlocks()
                    .getAtlasSprite(PipeModelManager.getTextureLoc(this));
            this.cachedTextureID = PipeInstancedRenderer.instance.getTextureID(tex);
        }
        return this.cachedTextureID;
    }

    public void invalidateCachedTextureID() {
        this.cachedTextureID = -1;
    }

    public void tryConnect(@Nonnull EnumFacing facing) {
        if (canConnectTo(facing)) {
            this.connections.add(facing);
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, Constants.BlockFlags.DEFAULT);
        }
    }

    public void disconnect(@Nonnull EnumFacing facing) {
        if (this.connections.remove(facing)) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, Constants.BlockFlags.DEFAULT);
        }
    }

    private boolean canConnectTo(@Nonnull EnumFacing facing) {
        Block otherBlock = this.world.getBlockState(this.pos.offset(facing)).getBlock();
        return otherBlock instanceof BlockPipe;
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setByte(TAG_CONNECTIONS, this.packConnections());

        return nbt;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.connections.clear();
        this.connections.addAll(unpackConnections(nbt.getByte(TAG_CONNECTIONS)));
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 1, this.getUpdateTag());
    }

    @Override
    @Nonnull
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(@Nonnull NetworkManager net, @Nonnull SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    public byte packConnections() {
        byte data = 0;
        for (EnumFacing facing : this.connections) {
            data |= (byte) (1 << facing.getIndex());
        }
        return data;
    }

    public static Set<EnumFacing> unpackConnections(byte packedConnections) {
        Set<EnumFacing> connections = EnumSet.noneOf(EnumFacing.class);
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (((packedConnections >> facing.getIndex()) & 1) == 1) {
                connections.add(facing);
            }
        }
        return connections;
    }
}
