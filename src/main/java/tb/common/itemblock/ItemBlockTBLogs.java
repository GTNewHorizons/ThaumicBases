package tb.common.itemblock;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import tb.common.block.BlockTBLog;

public class ItemBlockTBLogs extends ItemBlock {

    public ItemBlockTBLogs(Block b) {
        super(b);
        this.setHasSubtypes(true);
    }

    public String getUnlocalizedName(ItemStack stk) {
        return "tile." + BlockTBLog.names[Math.min(BlockTBLog.names.length - 1, stk.getItemDamage() % 4)];
    }

    public int getMetadata(int meta) {
        return meta;
    }
}
