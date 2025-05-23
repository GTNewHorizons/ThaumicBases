package tb.common.block;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tb.init.TBItems;

public class BlockBriar extends BlockBush implements IGrowable {

    private IIcon[] doublePlantBottomIcons;
    private IIcon[] doublePlantTopIcons;
    public int growthStages;
    public int growthDelay;

    public BlockBriar(int stages, int g) {
        super(Material.plants);
        this.setHardness(0.0F);
        this.setStepSound(soundTypeGrass);
        growthStages = stages;
        growthDelay = g;
    }

    public int getRenderType() {
        return 6;
    }

    public void updateTick(World w, int x, int y, int z, Random rnd) {
        super.updateTick(w, x, y, z, rnd);

        if (w.getBlock(x, y, z) == this) {
            int meta = w.getBlockMetadata(x, y, z);
            if (isTopBlock(meta)) {
                if (meta - 8 < growthStages - 1 && w.rand.nextInt(growthDelay) == 0) {
                    w.setBlockMetadataWithNotify(x, y, z, meta + 1, 3);
                    w.setBlockMetadataWithNotify(x, y - 1, z, meta - 7, 3);
                }
            } else {
                if (meta < growthStages - 1 && w.rand.nextInt(growthDelay) == 0) {
                    w.setBlockMetadataWithNotify(x, y, z, meta + 1, 3);
                    w.setBlockMetadataWithNotify(x, y + 1, z, meta + 9, 3);
                }
            }
        }
    }

    public boolean onBlockActivated(World aWorld, int aX, int aY, int aZ, EntityPlayer aPlayer, int aSide, float pX,
        float pY, float pZ) {
        int aMeta = aWorld.getBlockMetadata(aX, aY, aZ);
        boolean isTop = isTopBlock(aMeta);
        int lowerY = isTop ? aY - 1 : aY;
        int upperY = isTop ? aY : aY + 1;

        int lowerMeta = aWorld.getBlockMetadata(aX, lowerY, aZ);
        int upperMeta = aWorld.getBlockMetadata(aX, upperY, aZ);

        if (lowerMeta >= growthStages - 1 && upperMeta >= growthStages - 1 + 8) {
            int fortune = EnchantmentHelper.getFortuneModifier(aPlayer);

            ArrayList<ItemStack> drops = getDrops(aWorld, aX, lowerY, aZ, upperMeta, fortune);
            for (ItemStack stack : drops) {
                dropBlockAsItem(aWorld, aX, lowerY, aZ, stack);
            }

            aWorld.setBlockMetadataWithNotify(aX, lowerY, aZ, 0, 2);
            aWorld.setBlockMetadataWithNotify(aX, upperY, aZ, 8, 2);

            return true;
        }

        return false;
    }

    public void setBlockBoundsBasedOnState(IBlockAccess w, int x, int y, int z) {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    public boolean canPlaceBlockAt(World w, int x, int y, int z) {
        return super.canPlaceBlockAt(w, x, y, z) && w.isAirBlock(x, y + 1, z);
    }

    protected void checkAndDropBlock(World w, int x, int y, int z) {
        int meta = w.getBlockMetadata(x, y, z);
        if (isTopBlock(meta)) {
            if (w.getBlock(x, y - 1, z) != this) {
                w.setBlockToAir(x, y, z);
                this.dropBlockAsItem(w, x, y, z, meta, 0);
            }
        } else {
            if (w.getBlock(x, y + 1, z) != this) {
                this.dropBlockAsItem(w, x, y, z, meta, 0);
                w.setBlockToAir(x, y, z);
                return;
            }
            if (!canBlockStay(w, x, y, z)) {
                this.dropBlockAsItem(w, x, y + 1, z, meta, 0);
                w.setBlockToAir(x, y + 1, z);
                this.dropBlockAsItem(w, x, y, z, meta, 0);
                w.setBlockToAir(x, y, z);
            }
        }
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

        if (!isTopBlock(metadata)) {
            ret.add(new ItemStack(this, 1, 0));
            return ret;
        } else {
            metadata -= 8;
            if (metadata >= this.growthStages - 1) {
                ret.add(new ItemStack(TBItems.resource, 1 + world.rand.nextInt(4), 6));
                return ret;
            } else {
                return ret;
            }
        }
    }

    public boolean canBlockStay(World w, int x, int y, int z) {
        return !w.isAirBlock(x, y - 1, z) && (w.getBlock(x, y - 1, z)
            .isReplaceableOreGen(w, x, y - 1, z, Blocks.grass)
            || w.getBlock(x, y - 1, z)
                .isReplaceableOreGen(w, x, y, z, Blocks.dirt)
            || w.getBlock(x, y - 1, z)
                .canSustainPlant(w, x, y - 1, z, ForgeDirection.UP, this));
    }

    public boolean isTopBlock(int meta) {
        return meta > 7;
    }

    public void onBlockPlacedBy(World w, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        w.setBlock(x, y + 1, z, this, 8, 3);
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        doublePlantBottomIcons = new IIcon[growthStages];
        doublePlantTopIcons = new IIcon[growthStages];

        for (int i = 0; i < growthStages; ++i) {
            doublePlantBottomIcons[i] = reg.registerIcon(textureName + "stage_" + i + "_bot");
            doublePlantTopIcons[i] = reg.registerIcon(textureName + "stage_" + i + "_top");
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return isTopBlock(meta) ? doublePlantTopIcons[meta - 8] : doublePlantBottomIcons[meta];
    }

    // canApplyBonemeal
    @Override
    public boolean func_149851_a(World w, int x, int y, int z, boolean remote) {
        return w.getBlockMetadata(x, y, z) % 8 < growthStages - 1;
    }

    // canGrowPlant
    @Override
    public boolean func_149852_a(World w, Random r, int x, int y, int z) {
        return w.getBlockLightValue(x, y + 1, z) >= 9;
    }

    // growPlant
    @Override
    public void func_149853_b(World w, Random r, int x, int y, int z) {
        if (w.getBlock(x, y, z) == this) {
            int meta = w.getBlockMetadata(x, y, z);
            if (isTopBlock(meta)) {
                if (meta - 8 < growthStages - 1 && w.rand.nextInt(growthDelay) == 0) {
                    w.setBlockMetadataWithNotify(x, y, z, meta + 1, 3);
                    w.setBlockMetadataWithNotify(x, y - 1, z, meta - 7, 3);
                }
            } else {
                if (meta < growthStages - 1 && w.rand.nextInt(growthDelay) == 0) {
                    w.setBlockMetadataWithNotify(x, y, z, meta + 1, 3);
                    w.setBlockMetadataWithNotify(x, y + 1, z, meta + 9, 3);
                }
            }
        }
    }
}
