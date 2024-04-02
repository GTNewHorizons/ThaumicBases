package tb.common.block;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.entities.EntityAspectOrb;

public class BlockAshroom extends BlockTBPlant {

    public BlockAshroom(int stages, int delay, boolean isCrop) {
        super(stages, delay, isCrop);
    }

    @Override
    public void func_149853_b(World w, Random r, int x, int y, int z) {
        int meta = w.getBlockMetadata(x, y, z);
        w.setBlockMetadataWithNotify(x, y, z, Math.min(growthStages, meta + 1), 3);
    }

    protected boolean func_150109_e(World p_150109_1_, int p_150109_2_, int p_150109_3_, int p_150109_4_) {
        if (!this.canPlaceBlockAt(p_150109_1_, p_150109_2_, p_150109_3_, p_150109_4_)) {
            if (p_150109_1_.getBlock(p_150109_2_, p_150109_3_, p_150109_4_) == this) {
                this.dropBlockAsItem(
                    p_150109_1_,
                    p_150109_2_,
                    p_150109_3_,
                    p_150109_4_,
                    p_150109_1_.getBlockMetadata(p_150109_2_, p_150109_3_, p_150109_4_),
                    0);
                p_150109_1_.setBlockToAir(p_150109_2_, p_150109_3_, p_150109_4_);
            }

            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onBlockHarvested(World world, int x, int y, int z, int metadata, EntityPlayer player) {
        if (metadata >= this.growthStages - 1) {
            // should be close enough to the rate of the old logic.
            int dropCount = 8 + (int) Math.round(world.rand.nextDouble() * 11.76742);
            ArrayList<Aspect> primals = Aspect.getPrimalAspects();
            for (int i = 0; i < dropCount; ++i) // Nerf for the shrooms
            {
                // We should probably cluster orbs of the same aspect instead of spawning them individually.
                Aspect aspect = primals.get(world.rand.nextInt(primals.size()));
                EntityAspectOrb orb = new EntityAspectOrb(world, x, y, z, aspect, 1);
                if (!world.isRemote) world.spawnEntityInWorld(orb);
            }
        }
        super.onBlockHarvested(world, x, y, z, metadata, player);
    }

}
