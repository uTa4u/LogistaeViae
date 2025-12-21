package su.uTa4u.logistaeviae.block;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import su.uTa4u.logistaeviae.Tags;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockPipe extends Block implements ITileEntityProvider {
    private static final Byte2ObjectMap<AxisAlignedBB> AABB_BY_CONNECTION = generateAABBs();

    private final ResourceLocation texture;

    public BlockPipe(String name) {
        super(Material.CIRCUITS);
        this.setRegistryName("pipe/" + name);
        this.setTranslationKey("pipe_" + name);
        this.texture = new ResourceLocation(Tags.MOD_ID, "block/pipe/" + name);
    }

    public ResourceLocation getTexture() {
        return this.texture;
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
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
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
        return new TileEntityPipe(this);
    }

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

        for (byte i = 0; i < 64; i++) {
            if (((i >> EnumFacing.DOWN.getIndex()) & 1) == 1) {
                map.put(i, center.union(down));
            } else if (((i >> EnumFacing.UP.getIndex()) & 1) == 1) {
                map.put(i, center.union(up));
            } else if (((i >> EnumFacing.NORTH.getIndex()) & 1) == 1) {
                map.put(i, center.union(north));
            } else if (((i >> EnumFacing.SOUTH.getIndex()) & 1) == 1) {
                map.put(i, center.union(south));
            } else if (((i >> EnumFacing.WEST.getIndex()) & 1) == 1) {
                map.put(i, center.union(west));
            } else if (((i >> EnumFacing.EAST.getIndex()) & 1) == 1) {
                map.put(i, center.union(east));
            } else {
                assert i == 0;
                map.put(i, center);
            }
        }

        return map;
    }
}
