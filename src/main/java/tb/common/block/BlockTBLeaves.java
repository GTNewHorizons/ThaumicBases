package tb.common.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNetherrack;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.BlockStone;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.oredict.OreDictionary;

import DummyCore.Utils.MathUtils;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tb.core.TBCore;
import tb.init.TBBlocks;

public class BlockTBLeaves extends BlockLeaves {

    public static final String[] names = new String[] { "goldenOakLeaves", "peacefullTreeLeaves", "netherTreeLeaves",
        "enderTreeLeaves" };

    public static final String[] textures = new String[] { "goldenOak/leaves", "peacefullTree/leaves",
        "netherTree/leaves", "enderTree/leaves" };

    public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
        if (world.getBlockMetadata(x, y, z) % 4 == 3) if (entity instanceof EntityDragon) return false;

        return super.canEntityDestroy(world, x, y, z, entity);
    }

    public boolean isFlammable(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        if (world.getBlockMetadata(x, y, z) % 4 == 2) return true;

        return super.isFlammable(world, x, y, z, face);
    }

    public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        if (world.getBlockMetadata(x, y, z) % 4 == 2) return 0;

        return super.getFlammability(world, x, y, z, face);
    }

    public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        if (world.getBlockMetadata(x, y, z) % 4 == 2) return 0;

        return super.getFlammability(world, x, y, z, face);
    }

    public boolean isFireSource(World world, int x, int y, int z, ForgeDirection side) {
        if (world.getBlockMetadata(x, y, z) % 4 == 2) return true;

        return super.isFireSource(world, x, y, z, side);
    }

    public static IIcon[] icons = new IIcon[names.length];

    @SideOnly(Side.CLIENT)
    public int getBlockColor() {
        return 0xffffff;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        if (world.getBlockMetadata(x, y, z) % 4 == 3) {
            return 11;
        }
        return super.getLightValue(world, x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess w, int x, int y, int z, int meta) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World w, int x, int y, int z, Random rnd) {
        super.randomDisplayTick(w, x, y, z, rnd);

        switch (w.getBlockMetadata(x, y, z) % 4) {
            case 0 -> w
                .spawnParticle("reddust", x + rnd.nextDouble(), y + rnd.nextDouble(), z + rnd.nextDouble(), 1, 1, 0);
            case 1 -> w
                .spawnParticle("heart", x + rnd.nextDouble(), y + rnd.nextDouble(), z + rnd.nextDouble(), 0, 10, 0);
            case 2 -> {
                if (rnd.nextFloat() <= 0.01F && w.isAirBlock(x, y - 1, z))
                    w.spawnParticle("dripLava", x + rnd.nextDouble(), y, z + rnd.nextDouble(), 0, 0, 0);
            }
            case 3 -> w.spawnParticle(
                "portal",
                x + rnd.nextDouble(),
                y + rnd.nextDouble(),
                z + rnd.nextDouble(),
                MathUtils.randomDouble(rnd),
                MathUtils.randomDouble(rnd),
                MathUtils.randomDouble(rnd));
        }
    }

    public Item getItemDropped(int meta, Random rnd, int fortune) {
        return Item.getItemFromBlock(TBBlocks.sapling);
    }

    @SideOnly(Side.CLIENT)
    public int getRenderColor(int p_149741_1_) {
        return 0xffffff;
    }

    @Override
    public void updateTick(World w, int x, int y, int z, Random rnd) {
        super.updateTick(w, x, y, z, rnd);
        int meta = w.getBlockMetadata(x, y, z) % 4;

        switch (meta) {
            case 1 -> peacefulEffect(w, x, y, z, rnd);
            case 2 -> netherEffect(w, x, y, z, rnd);
            case 3 -> endEffect(w, x, y, z, rnd);
        }
    }

    private void peacefulEffect(World w, int x, int y, int z, Random rnd) {
        if (rnd.nextDouble() > 0.03D) return;

        int dy = y;
        while (--dy >= y - 6) {
            Block b = w.getBlock(x, dy, z);
            if (b.isAir(w, x, dy, z)) continue;

            spawnBiomeEntity(w, x, dy, z, w.getBiomeGenForCoords(x, z), EnumCreatureType.creature, rnd);
            break;
        }
    }

    private void netherEffect(World w, int x, int y, int z, Random rnd) {
        int dy = y;
        while (--dy >= y - 8) {
            Block b = w.getBlock(x, dy, z);
            if (b.isAir(w, x, dy, z)) continue;

            if (isNetherBlock(b) && rnd.nextDouble() <= 0.05D) {
                spawnBiomeEntity(
                    w,
                    x,
                    dy,
                    z,
                    BiomeGenBase.hell,
                    rnd.nextBoolean() ? EnumCreatureType.creature : EnumCreatureType.monster,
                    rnd);
                break;
            } else if (isOverworldBlock(b, w, x, dy, z)) {
                Block setTo = getNetherBlock(rnd.nextFloat());
                w.setBlock(x, dy, z, setTo, 0, 3);
                break;
            }
        }
    }

    private void endEffect(World w, int x, int y, int z, Random rnd) {
        int dy = y;
        while (--dy >= y - 11) {
            Block b = w.getBlock(x, dy, z);
            if (b.isAir(w, x, dy, z)) continue;

            if (isEndBlock(b) && rnd.nextDouble() <= 0.02D) {
                spawnBiomeEntity(
                    w,
                    x,
                    dy,
                    z,
                    BiomeGenBase.sky,
                    rnd.nextBoolean() ? EnumCreatureType.creature : EnumCreatureType.monster,
                    rnd);
                break;
            } else if (isOverworldBlock(b, w, x, dy, z)) {
                Block setTo = getEndBlock(rnd.nextFloat());
                w.setBlock(x, dy, z, setTo, 0, 3);
                break;
            }
        }
    }

    private void spawnBiomeEntity(World w, int x, int y, int z, BiomeGenBase biome, EnumCreatureType type, Random rnd) {
        if (biome == null) return;

        List<SpawnListEntry> list = biome.getSpawnableList(type);
        if (list == null || list.isEmpty()) return;

        SpawnListEntry entry = list.get(rnd.nextInt(list.size()));
        if (entry == null || entry.entityClass == null || !EntityLiving.class.isAssignableFrom(entry.entityClass))
            return;

        try {
            EntityLiving entity = entry.entityClass.getConstructor(World.class)
                .newInstance(w);
            entity.setPositionAndRotation(x + 0.5, y + 1, z + 0.5, 0, 0);
            entity.onSpawnWithEgg(null);

            Result canSpawn = ForgeEventFactory
                .canEntitySpawn(entity, w, (float) entity.posX, (float) entity.posY, (float) entity.posZ);
            if ((canSpawn == Result.ALLOW || (canSpawn == Result.DEFAULT && canSpawn(entity)))) {
                w.spawnEntityInWorld(entity);
            }
        } catch (Exception e) {
            FMLLog.warning("[TB] Failed to create biome entity of class %s: ", entry.entityClass.getName());
            e.printStackTrace();
        }
    }

    /**
     * Ignores light checks so mobs can spawn outside during the day.
     */
    public static boolean canSpawn(EntityLiving entity) {
        return entity.worldObj.checkNoEntityCollision(entity.boundingBox)
            && entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox)
                .isEmpty()
            && !entity.worldObj.isAnyLiquid(entity.boundingBox);
    }

    private boolean isOverworldBlock(Block b, World w, int x, int y, int z) {
        if (b instanceof BlockDirt || b instanceof BlockGrass
            || b instanceof BlockGravel
            || b instanceof BlockSand
            || b instanceof BlockStone) {
            return true;
        }

        ItemStack stk = new ItemStack(b, 1, w.getBlockMetadata(x, y, z));
        for (int id : OreDictionary.getOreIDs(stk)) {
            String ore = OreDictionary.getOreName(id);
            if (ore != null && !ore.isEmpty()
                && (ore.contains("dirt") || ore.contains("grass")
                    || ore.contains("sand")
                    || ore.contains("gravel")
                    || ore.contains("stone"))) {
                return true;
            }
        }
        return false;
    }

    private boolean isNetherBlock(Block b) {
        return b instanceof BlockNetherrack || b instanceof BlockSoulSand || b == Blocks.quartz_ore;
    }

    private boolean isEndBlock(Block b) {
        return b == Blocks.end_stone || b instanceof BlockObsidian;
    }

    private static Block getNetherBlock(float r) {
        return r <= 0.6 ? Blocks.netherrack : r <= 0.9 ? Blocks.soul_sand : Blocks.quartz_ore;
    }

    private static Block getEndBlock(float r) {
        return r <= 0.9 ? Blocks.end_stone : Blocks.obsidian;
    }

    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess p_149720_1_, int p_149720_2_, int p_149720_3_, int p_149720_4_) {
        return 0xffffff;
    }

    // getSaplingDropRate
    protected int func_150123_b(int meta) {
        return meta == 0 ? 50 : 30;
    }

    // dropRareItem
    protected void func_150124_c(World w, int x, int y, int z, int meta, int chance) {
        if (meta == 0 && w.rand.nextInt(chance) == 0) {
            this.dropBlockAsItem(w, x, y, z, new ItemStack(Items.golden_apple, 1, 0));
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return icons[meta % 4];
    }

    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item i, CreativeTabs tab, List<ItemStack> list) {
        for (int f = 0; f < names.length; ++f) list.add(new ItemStack(i, 1, f));
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        for (int i = 0; i < icons.length; ++i) icons[i] = reg.registerIcon(TBCore.modid + ":" + textures[i]);

        blockIcon = reg.registerIcon(getTextureName());
    }

    @Override
    public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune) {
        if (world.getBlockMetadata(x, y, z) % 4 == 0) return new ArrayList<>();
        else return super.onSheared(item, world, x, y, z, fortune);
    }

    // getLeafNames
    @Override
    public String[] func_150125_e() {
        return names;
    }
}
