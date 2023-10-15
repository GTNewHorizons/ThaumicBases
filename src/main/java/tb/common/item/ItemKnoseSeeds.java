package tb.common.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemKnoseSeeds extends ItemSeeds {

    private Block cropBlock;
    private Block soilBlockID;

    public ItemKnoseSeeds(Block crop, Block soil) {
        super(crop, soil);
        this.cropBlock = crop;
        this.soilBlockID = soil;
    }

    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side,
        float fx, float fy, float fz) {
        if (side != 1) {
            return false;
        } else
            if (player.canPlayerEdit(x, y, z, side, itemStack) && player.canPlayerEdit(x, y + 1, z, side, itemStack)) {
                if (world.getBlock(x, y, z) == soilBlockID) {
                    if (world.isAirBlock(x, y + 1, z)) {
                        /* world.getBlock(x, y + 1, z) == Blocks.air */
                        world.setBlock(x, y + 1, z, this.cropBlock);
                        --itemStack.stackSize;
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        return false;
    }
}
