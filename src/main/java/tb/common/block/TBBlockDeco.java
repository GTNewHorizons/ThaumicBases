package tb.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tb.init.TBBlocks;
import thaumcraft.api.crafting.IInfusionStabiliser;

public class TBBlockDeco extends Block implements IInfusionStabiliser {

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

    @Override
    public boolean canStabaliseInfusion(World var1, int var2, int var3, int var4) {
        return true;
    }
}
