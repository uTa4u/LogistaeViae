package su.uTa4u.logistaeviae.block;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import su.uTa4u.logistaeviae.LogistaeViae;
import su.uTa4u.logistaeviae.Tags;
import su.uTa4u.logistaeviae.client.model.PipeModelManager;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// TODO: can't be placed if player is in the same block despite AABB allowing it
public class BlockPipe extends Block implements ITileEntityProvider {
    // ArrayMap implementation should be fine for only 64 entries
    private static final Byte2ObjectMap<AxisAlignedBB> AABB_BY_CONNECTION = generateAABBs();

    public static final ConnectionsProperty CONNECTION_PROP = new ConnectionsProperty("connections");

    private final ResourceLocation texture;

    public BlockPipe(String name) {
        super(Material.CIRCUITS);
        this.setRegistryName(Tags.MOD_ID, "pipe/" + name);
        this.setTranslationKey(Tags.MOD_ID + ".pipe_" + name);
        this.texture = new ResourceLocation(Tags.MOD_ID, "block/pipe/" + name);
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(CONNECTION_PROP).build();
    }

    @Override
    @Nonnull
    public IBlockState getExtendedState(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        IExtendedBlockState ext = (IExtendedBlockState) state;
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPipe) {
            ext = ext.withProperty(CONNECTION_PROP, ((TileEntityPipe) te).packConnections());
        }
        return ext;
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityPipe) {
                for (EnumFacing facing : EnumFacing.VALUES) {
                    ((TileEntityPipe) te).tryConnect(facing);
                    TileEntity nbour = world.getTileEntity(pos.offset(facing));
                    if (nbour instanceof TileEntityPipe) {
                        // Redundant check for connectability, but whatever
                        ((TileEntityPipe) nbour).tryConnect(facing.getOpposite());
                    }
                }
            }
        }
    }

    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
        if (super.removedByPlayer(state, world, pos, player, willHarvest)) {
            if (!world.isRemote) {
                for (EnumFacing facing : EnumFacing.VALUES) {
                    TileEntity te = world.getTileEntity(pos.offset(facing));
                    if (te instanceof TileEntityPipe) {
                        ((TileEntityPipe) te).disconnect(facing.getOpposite());
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    @Nonnull
    public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        TileEntity te = source.getTileEntity(pos);
        if (te instanceof TileEntityPipe) {
            return AABB_BY_CONNECTION.get(((TileEntityPipe) te).packConnections());
        }
        return super.getBoundingBox(state, source, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    @Nonnull
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return LogistaeViae.IS_INSTANCED_RENDERING ? EnumBlockRenderType.INVISIBLE : EnumBlockRenderType.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(@Nonnull IBlockState state) {
        return false;
    }

    @Override
    public boolean isPassable(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World world, int meta) {
        return new TileEntityPipe();
    }

    // TODO: can we have actually accurate AABBs?
    private static Byte2ObjectMap<AxisAlignedBB> generateAABBs() {
        Byte2ObjectMap<AxisAlignedBB> map = new Byte2ObjectArrayMap<>();

        double onee = 1.00;
        double from = 0.25;
        double zero = 0.00;
        double tooo = 0.75;
        AxisAlignedBB center = new AxisAlignedBB(from, from, from, tooo, tooo, tooo);
        AxisAlignedBB down = new AxisAlignedBB(from, zero, from, tooo, from, tooo);
        AxisAlignedBB up = new AxisAlignedBB(from, tooo, from, tooo, onee, tooo);
        AxisAlignedBB north = new AxisAlignedBB(from, from, zero, tooo, tooo, from);
        AxisAlignedBB south = new AxisAlignedBB(from, from, tooo, tooo, tooo, onee);
        AxisAlignedBB west = new AxisAlignedBB(zero, from, from, from, tooo, tooo);
        AxisAlignedBB east = new AxisAlignedBB(tooo, from, from, onee, tooo, tooo);

        map.put((byte) 0, center);
        for (byte i = 1; i < PipeModelManager.BASE_INSTANCE_COUNT; i++) {
            AxisAlignedBB aabb = new AxisAlignedBB(center.minX, center.minY, center.minZ, center.maxX, center.maxY, center.maxZ);
            if (((i >> EnumFacing.DOWN.getIndex()) & 1) == 1) {
                aabb = aabb.union(down);
            }
            if (((i >> EnumFacing.UP.getIndex()) & 1) == 1) {
                aabb = aabb.union(up);
            }
            if (((i >> EnumFacing.NORTH.getIndex()) & 1) == 1) {
                aabb = aabb.union(north);
            }
            if (((i >> EnumFacing.SOUTH.getIndex()) & 1) == 1) {
                aabb = aabb.union(south);
            }
            if (((i >> EnumFacing.WEST.getIndex()) & 1) == 1) {
                aabb = aabb.union(west);
            }
            if (((i >> EnumFacing.EAST.getIndex()) & 1) == 1) {
                aabb = aabb.union(east);
            }
            map.put(i, aabb);
        }

        return map;
    }
}
