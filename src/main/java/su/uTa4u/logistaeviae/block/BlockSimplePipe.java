package su.uTa4u.logistaeviae.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import su.uTa4u.logistaeviae.tileentity.TileEntitySimplePipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockSimplePipe extends Block implements ITileEntityProvider {
    private static final String NAME = "simple_pipe";

    public BlockSimplePipe() {
        super(Material.CIRCUITS);
        this.setRegistryName(NAME);
        this.setTranslationKey(NAME);
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntitySimplePipe) {
                for (EnumFacing facing : EnumFacing.VALUES) {
                    ((TileEntitySimplePipe) te).tryConnect(world, pos, facing);
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
                    if (te instanceof TileEntitySimplePipe) {
                        ((TileEntitySimplePipe) te).disconnect(facing.getOpposite());
                    }
                }
            }
            return true;
        }
        return false;
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
        return new TileEntitySimplePipe();
    }
}
