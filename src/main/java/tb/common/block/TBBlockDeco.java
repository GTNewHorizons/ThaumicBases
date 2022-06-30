package tb.common.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.world.IBlockAccess;
import tb.init.TBBlocks;

public class TBBlockDeco extends Block {

    boolean isGlass;

    public TBBlockDeco(Material m, boolean b) {
        super(m);
        isGlass = b;
    }

    public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
        if (entity instanceof EntityDragon) return this != TBBlocks.enderPlanks;

        return super.canEntityDestroy(world, x, y, z, entity);
    }

    public boolean isOpaqueCube() {
        return !isGlass;
    }

    public int getLightOpacity() {
        return isGlass ? 7 : 15;
    }

    @SideOnly(Side.CLIENT)
    public int getRenderBlockPass() {
        return isGlass ? 1 : 0;
    }
}
