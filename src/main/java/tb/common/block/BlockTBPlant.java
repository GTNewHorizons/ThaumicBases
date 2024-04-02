package tb.common.block;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTBPlant extends BlockBush implements IGrowable {

    public int growthStages;
    public int growthDelay;
    public boolean requiresFarmland;
    public IIcon[] growthIcons;
    public ItemStack dropItem;
    public ItemStack dropSeed;

    public BlockTBPlant(int stages, int delay, boolean isCrop) {
        super();
        growthStages = stages;
        growthDelay = delay;
        requiresFarmland = isCrop;
        this.setTickRandomly(true);
        float f = 0.5F;
        if (isCrop) this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.25F, 0.5F + f);
        else this.setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 0.75F, 0.75F);

        this.setHardness(0.0F);
        this.setStepSound(soundTypeGrass);
        this.disableStats();
    }

    @SideOnly(Side.CLIENT)
    public Item getItem(World w, int x, int y, int z) {
        return dropSeed != null ? dropSeed.getItem() : Item.getItemFromBlock(this);
    }

    protected boolean canPlaceBlockOn(Block b) {
        return requiresFarmland ? b == Blocks.farmland : true;
    }

    public void updateTick(World w, int x, int y, int z, Random rnd) {
        super.updateTick(w, x, y, z, rnd);

        if (w.getBlockLightValue(x, y + 1, z) >= 9) {
            int l = w.getBlockMetadata(x, y, z);

            if (l < growthStages) {
                float f = this.calculateGrowth(w, x, y, z);

                if (rnd.nextInt((int) ((float) growthDelay / f) + 1) == 0) {
                    ++l;
                    w.setBlockMetadataWithNotify(x, y, z, l, 2);
                }
            }
        }
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
        return this.requiresFarmland ? EnumPlantType.Crop : EnumPlantType.Plains;
    }

    private float calculateGrowth(World w, int x, int y, int z) {
        float f = 1.0F;
        Block block = w.getBlock(x, y, z - 1);
        Block block1 = w.getBlock(x, y, z + 1);
        Block block2 = w.getBlock(x - 1, y, z);
        Block block3 = w.getBlock(x + 1, y, z);
        Block block4 = w.getBlock(x - 1, y, z - 1);
        Block block5 = w.getBlock(x + 1, y, z - 1);
        Block block6 = w.getBlock(x + 1, y, z + 1);
        Block block7 = w.getBlock(x - 1, y, z + 1);
        boolean flag = block2 == this || block3 == this;
        boolean flag1 = block == this || block1 == this;
        boolean flag2 = block4 == this || block5 == this || block6 == this || block7 == this;

        for (int l = x - 1; l <= x + 1; ++l) {
            for (int i1 = z - 1; i1 <= z + 1; ++i1) {
                float f1 = 0.0F;

                if (w.getBlock(l, y - 1, i1)
                    .canSustainPlant(w, l, y - 1, i1, ForgeDirection.UP, this)) {
                    f1 = 1.0F;

                    if (w.getBlock(l, y - 1, i1)
                        .isFertile(w, l, y - 1, i1)) {
                        f1 = 3.0F;
                    }
                }

                if (l != x || i1 != z) {
                    f1 /= 4.0F;
                }

                f += f1;
            }
        }

        if (flag2 || flag && flag1) {
            f /= 2.0F;
        }

        return f;
    }

    // canApplyBonemeal
    @Override
    public boolean func_149851_a(World w, int x, int y, int z, boolean remote) {
        return w.getBlockMetadata(x, y, z) < growthStages - 1;
    }

    // canGrowPlant
    @Override
    public boolean func_149852_a(World w, Random r, int x, int y, int z) {
        return w.getBlockLightValue(x, y + 1, z) >= 9;
    }

    // growPlant
    @Override
    public void func_149853_b(World w, Random r, int x, int y, int z) {
        w.setBlockMetadataWithNotify(
            x,
            y,
            z,
            Math.min(growthStages - 1, w.getBlockMetadata(x, y, z) + r.nextInt(3) + 1),
            3);
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        growthIcons = new IIcon[growthStages];
        for (int i = 0; i < growthStages; ++i) growthIcons[i] = reg.registerIcon(textureName + "stage_" + i);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return growthIcons[meta >= growthIcons.length ? growthIcons.length - 1 : meta];
    }

    public int getRenderType() {
        return requiresFarmland ? 6 : 1;
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

        if (this.dropItem != null && metadata >= growthStages - 1) {
            // You can approximate the fortune bonus by diving https://oeis.org/A000169 with https://oeis.org/A000435
            // I can't figure out a good efficient way to compute those without melting my brain so if you feel
            // like you can approximate it well enough, feel free to do so. For now, I'm just capping it at 3 drops
            // with a linear function since fortune levels above 3 aren't very common, and I don't want some poor lad
            // crashing their pc with max int fortune int.
            //
            // Average bonuses for each fortune level:
            // F1 = 0.5 -> 0.5 (stayed the same)
            // F2 = 0.88888 -> 0.9 (slight buff)
            // F3 = 1.21875 -> 1.3 (slight buff)
            // The EIG doesn't use fortune levels, so it shouldn't be affected anyway.
            int roundCount = 1;
            if (fortune > 0) {
                roundCount += (int) Math.min(3, Math.round(world.rand.nextDouble() * (1 + 0.8d * (fortune - 1))));
            }
            int dropCount = 0;
            if (growthStages <= metadata) {
                // we can just skip the random calls if we are at max growth.
                dropCount = roundCount;
            } else {
                for (int i = 0; i < roundCount; ++i) {
                    if (world.rand.nextInt(growthStages) <= metadata) {
                        dropCount++;
                    }
                }
            }
            if (dropCount > 0) {
                ItemStack drop = dropItem.copy();
                drop.stackSize = dropCount;
                ret.add(drop);
            }
        }
        if (dropSeed != null) ret.add(dropSeed.copy());

        return ret;
    }
}
