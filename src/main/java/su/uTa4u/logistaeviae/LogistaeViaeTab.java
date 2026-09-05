package su.uTa4u.logistaeviae;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import su.uTa4u.logistaeviae.block.ModBlocks;

import javax.annotation.Nonnull;

public class LogistaeViaeTab extends CreativeTabs {

    public LogistaeViaeTab() {
        super(Tags.MOD_ID);
    }

    @Override
    @Nonnull
    public ItemStack createIcon() {
        return new ItemStack(Item.getItemFromBlock(ModBlocks.PIPE_BASIC));
    }
}
