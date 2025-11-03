package tb.common.itemblock;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import tb.common.block.BlockTBLeaves;

public class ItemBlockTBLeaves extends ItemBlock {

    public ItemBlockTBLeaves(Block b) {
        super(b);
        this.setHasSubtypes(true);
    }

    public String getUnlocalizedName(ItemStack stk) {
        return "tile." + BlockTBLeaves.names[Math.min(BlockTBLeaves.names.length - 1, stk.getItemDamage() % 4)];
    }

    public int getMetadata(int meta) {
        return meta | 4;
    }
}
