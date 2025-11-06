package tb.common.tile;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import DummyCore.Utils.MathUtils;
import DummyCore.Utils.MiscUtils;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.lib.events.EssentiaHandler;
import tuhljin.automagy.tiles.TileEntityJarXP;

public class TileOverchanter extends TileEntity implements IInventory, IWandable {

    public ItemStack inventory;

    public int enchantingTime;
    public int xpToAbsorb;
    public boolean isEnchantingStarted;
    public int syncTimer;

    // public Lightning renderedLightning;

    public static boolean automagy = false;

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateEntity() {
        if (!worldObj.isRemote) {
            if (syncTimer <= 0) {
                syncTimer = 100;
                NBTTagCompound tg = new NBTTagCompound();
                tg.setInteger("0", enchantingTime);
                tg.setInteger("1", xpToAbsorb);
                tg.setBoolean("2", isEnchantingStarted);
                tg.setInteger("x", xCoord);
                tg.setInteger("y", yCoord);
                tg.setInteger("z", zCoord);
                MiscUtils.syncTileEntity(tg, 0);
            } else--syncTimer;
        }

        if (this.inventory == null) {
            isEnchantingStarted = false;
            xpToAbsorb = 1318;
            // ~30 levels, and exactly 1/8 an automagy xp jar
            enchantingTime = 0;
            // renderedLightning = null;
        } else {
            if (this.isEnchantingStarted) {
                if (this.worldObj.getTotalWorldTime() % 20 == 0) {
                    // renderedLightning = new Lightning(this.worldObj.rand, new Coord3D(0,0,0), new
                    // Coord3D(MathUtils.randomDouble(this.worldObj.rand)/50,MathUtils.randomDouble(this.worldObj.rand)/50,MathUtils.randomDouble(this.worldObj.rand)/50),
                    // 0.3F, 1,0,1);
                    this.worldObj
                        .playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "thaumcraft:infuserstart", 1F, 1.0F);
                    if (EssentiaHandler.drainEssentia(this, Aspect.MAGIC, ForgeDirection.UNKNOWN, 8, false)) {
                        ++enchantingTime;
                        if (enchantingTime >= 16 && this.xpToAbsorb != 0) {
                            if (automagy) {
                                this.xpToAbsorb -= this.drainXPJarsInRange(
                                //Is 8 too much of a range? The drainEssentia call has a range of 8
                                //I don't know if it just does an 8x8x8 cube or a full 17x17x17 with that, but this will do 17^3-1 = 4912
                                //Edit: looked through TC code, it does look like it does the 17x17x17 (maybe this causes lag for overchanter?)
                                if (xpToAbsorb == 0) break;
                            }
                            List<EntityPlayer> players = this.worldObj.getEntitiesWithinAABB(
                                EntityPlayer.class,
                                AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1)
                                    .expand(6, 3, 6));
                            if (!players.isEmpty()) {
                                for (int i = 0; i < players.size(); ++i) {
                                    EntityPlayer p = players.get(i);
                                    if (p.experienceLevel >= 30) {
                                        p.attackEntityFrom(DamageSource.magic, 8);
                                        this.worldObj
                                            .playSoundEffect(p.posX, p.posY, p.posZ, "thaumcraft:zap", 1F, 1.0F);
                                        p.addExperience(-this.xpToAbsorb);
                                        break;
                                    }
                                }
                            }
                        }

                        if (xpToAbsorb == 0 && enchantingTime >= 32) {
                            int enchId = this.findEnchantment(inventory);
                            NBTTagList nbttaglist = this.inventory.getEnchantmentTagList();
                            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                                NBTTagCompound tag = nbttaglist.getCompoundTagAt(i);
                                if (tag != null && Integer.valueOf(tag.getShort("id")) == enchId) {
                                    tag.setShort("lvl", Math.max((short) 1, (short) (Integer.valueOf(tag.getShort("lvl")) + 1)));
                                    NBTTagCompound stackTag = MiscUtils.getStackTag(inventory);
                                    if (!stackTag.hasKey("overchants")) {
                                        stackTag.setIntArray("overchants", new int[] { enchId });
                                    } else {
                                        int[] arrayInt = stackTag.getIntArray("overchants");
                                        int[] newArrayInt = new int[arrayInt.length + 1];
                                        for (int j = 0; j < arrayInt.length; ++j) {
                                            newArrayInt[j] = arrayInt[j];
                                        }
                                        newArrayInt[newArrayInt.length - 1] = enchId;

                                        stackTag.setIntArray("overchants", newArrayInt);
                                    }
                                    break;
                                }
                            }
                            isEnchantingStarted = false;
                            xpToAbsorb = 1318;
                            enchantingTime = 0;
                            // renderedLightning = null;
                            this.worldObj
                                .playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "thaumcraft:wand", 1F, 1F);
                        }

                    } else {
                        --enchantingTime;
                    }
                }
            }
        }
    }

    public boolean canStartEnchanting() {
        if (!this.isEnchantingStarted) if (this.inventory != null) {
            if (this.inventory.getEnchantmentTagList() != null && this.inventory.getEnchantmentTagList()
                .tagCount() > 0) {
                if (findEnchantment(inventory) != -1) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public int findEnchantment(ItemStack enchanted) {
        NBTTagCompound stackTag = MiscUtils.getStackTag(inventory);
        LinkedHashMap<Integer, Integer> ench = (LinkedHashMap<Integer, Integer>) EnchantmentHelper
            .getEnchantments(enchanted);
        Set<Integer> keys = ench.keySet();
        Iterator<Integer> $i = keys.iterator();

        while ($i.hasNext()) {
            int i = $i.next();
            if (!stackTag.hasKey("overchants")) {
                return i;
            } else {
                int[] overchants = stackTag.getIntArray("overchants");
                if (MathUtils.arrayContains(overchants, i)) continue;

                return i;
            }
        }

        return -1;
    }

    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        enchantingTime = pkt.func_148857_g()
            .getInteger("0");
        xpToAbsorb = pkt.func_148857_g()
            .getInteger("1");
        isEnchantingStarted = pkt.func_148857_g()
            .getBoolean("2");
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (!worldObj.isRemote && !isEnchantingStarted) {

            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory;
    }

    @Override
    public ItemStack decrStackSize(int slot, int num) {
        if (this.inventory != null) {
            ItemStack itemstack;

            if (this.inventory.stackSize <= num) {
                itemstack = this.inventory;
                this.inventory = null;
                this.markDirty();
                return itemstack;
            } else {
                itemstack = this.inventory.splitStack(num);

                if (this.inventory.stackSize == 0) {
                    this.inventory = null;
                }

                this.markDirty();
                return itemstack;
            }
        } else {
            return null;
        }
    }
    /*
     * @Override public ItemStack decrStackSize(int i, int j) { if (inventory != null) { ItemStack stackAt; if
     * (inventory.stackSize <= j) { stackAt = inventory; inventory = null; return stackAt; } else { stackAt =
     * inventory.splitStack(j); if (inventory.stackSize == 0) inventory = null; return stackAt; } } return null; }
     */

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return inventory;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stk) {
        inventory = stk;
    }

    @Override
    public String getInventoryName() {
        return "tb.overchanter";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return player.dimension == this.worldObj.provider.dimensionId
            && this.worldObj.blockExists(xCoord, yCoord, zCoord);
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stk) {
        // return stk.hasTagCompound() && stk.getEnchantmentTagList() != null;
        return false;
    }

    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        enchantingTime = tag.getInteger("enchTime");
        xpToAbsorb = tag.getInteger("xpToAbsorb");
        isEnchantingStarted = tag.getBoolean("enchStarted");

        if (tag.hasKey("itm")) inventory = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("itm"));
    }

    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        tag.setInteger("enchTime", enchantingTime);
        tag.setInteger("xpToAbsorb", xpToAbsorb);
        tag.setBoolean("enchStarted", isEnchantingStarted);

        if (inventory != null) {
            NBTTagCompound t = new NBTTagCompound();
            inventory.writeToNBT(t);
            tag.setTag("itm", t);
        }
    }

    @Override
    public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side,
        int md) {
        if (canStartEnchanting()) {
            isEnchantingStarted = true;
            player.swingItem();
            syncTimer = 0;
            this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "thaumcraft:craftstart", 0.5F, 1.0F);
            return 1;
        }
        return -1;
    }

    @Override
    public ItemStack onWandRightClick(World world, ItemStack wandstack, EntityPlayer player) {
        return wandstack;
    }

    @Override
    public void onUsingWandTick(ItemStack wandstack, EntityPlayer player, int count) {}

    @Override
    public void onWandStoppedUsing(ItemStack wandstack, World world, EntityPlayer player, int count) {}

    private int drainXPJarsInRange(int xp, int range) {
            if (xp < 0) return xp;
            Iterator<int[]> cubeIter = cubeIterator(range);
            while (cubeIter.hasNext()) {
                int[] coords = cubeIter.next()
                if (this.worldObj.getTileEntity(coords[0] + this.xCoord, coords[1] + this.yCoord, coords[2] + this.zCoord) instanceof TileEntityJarXP jar) {
                    int jarxp = jar.getXP();
                    if (jarxp < xp) {
                        jar.setXP(0);
                        xp -= jarxp;
                        continue;
                    }
                    jar.setXP(jarxp - xp);
                    return 0;
                }
            }
            return xp;
            //This algorithm drains each jar it comes across sequentially without regard to how full they are. If you would rather it prioritize emptying barely filled jars within a certain radius, and then emptying non-full jars, then instead have a counter sinceLastPrioJar that starts at 0 and increments each iteration, and cache the most recent "priority" jar (lowest non-full jar with jarxp > xp argument); reset that counter per each jar with jarxp under xp argument, and drain each jar with jarxp under xp argument, stopping once the counter reaches an arbitrary count of blocks searched without draining a jar, and then drain the cached lowest-filled jar. The TC EssentiaHandler would be so much better if it worked that way as well.
    }

    private static Iterator<int[]> cubeIterator(int range) {
        return new Iterator<int[]>() {
            private int range = 0;
            private int n = 0;
            private int l = 0;
            private int m = 0;
            //wow, it's just like electron orbitals. and the spin is the sign. how beautiful
            private Iterator<int[]> init(int range) {
                this.range = range;
                return this;
            }
            public boolean hasNext() { return -n<range || -l<range || -m<range; }
            public int[] next() {
                //this shit looks like the decompile of an obfuscated assembly but i assure you it is hand written
                godwhy: {
                    m = -m;
                    if (m<0) break godwhy;
                    l = -l;
                    if (l<0) break godwhy;
                    n = -n;
                    if (n<0) break godwhy;
                    if (l>=n || m>n) {
                        if (m>=l) {
                            if (n<=l) {
                                if (m>n) {
                                    n ^= m;
                                    m ^= n;
                                    n ^= m;
                                    if (l>m) {
                                        ++m;
                                        break godwhy;
                                    }
                                    m = 0;
                                    ++l;
                                    break godwhy;
                                }
                                l = 0;
                                m = 0;
                                ++n;
                                break godwhy;
                            }
                            n ^= l;
                            l ^= n;
                            n ^= l;
                            break godwhy;
                        }
                        if (n<m) {
                            n ^= m;
                            m ^= n;
                            n ^= m;
                            break godwhy;
                        }
                        m ^= l;
                        l ^= m;
                        m ^= l;
                        break godwhy;
                    }
                    if (l>m) {
                        m ^= l;
                        l ^= m;
                        m ^= l;
                        break godwhy;
                    }
                    n ^= l;
                    l ^= n;
                    n ^= l;
                } //i have just found out that Java has a `when` statement, but primitive pattern matching is preview and the syntax sucks (case boolean b when a>6)
                int[] out = {n,l,m};
                return out; //i genuinely would rather have written this bytecode by bytecode but here we are
            }
        }.init(range); //what is this, JavaScript???
    }
}
