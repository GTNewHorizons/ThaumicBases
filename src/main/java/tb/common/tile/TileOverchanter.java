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
    public boolean xpAbsorbed;
    public boolean isEnchantingStarted;
    public int syncTimer;

    // public Lightning renderedLightning;

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
                tg.setBoolean("1", xpAbsorbed);
                tg.setBoolean("2", isEnchantingStarted);
                tg.setInteger("x", xCoord);
                tg.setInteger("y", yCoord);
                tg.setInteger("z", zCoord);
                MiscUtils.syncTileEntity(tg, 0);
            } else--syncTimer;
        }

        if (this.inventory == null) {
            isEnchantingStarted = false;
            xpAbsorbed = false;
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
                        if (enchantingTime >= 16 && !this.xpAbsorbed) {
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
                                        p.experienceLevel -= 30;
                                        xpAbsorbed = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (xpAbsorbed && enchantingTime >= 32) {
                            int enchId = this.findEnchantment(inventory);
                            NBTTagList nbttaglist = this.inventory.getEnchantmentTagList();
                            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                                NBTTagCompound tag = nbttaglist.getCompoundTagAt(i);
                                if (tag != null && Integer.valueOf(tag.getShort("id")) == enchId) {
                                    tag.setShort("lvl", (short) (Integer.valueOf(tag.getShort("lvl")) + 1));
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
                            xpAbsorbed = false;
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
        xpAbsorbed = pkt.func_148857_g()
            .getBoolean("1");
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
        xpAbsorbed = tag.getBoolean("xpAbsorbed");
        isEnchantingStarted = tag.getBoolean("enchStarted");

        if (tag.hasKey("itm")) inventory = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("itm"));
    }

    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        tag.setInteger("enchTime", enchantingTime);
        tag.setBoolean("xpAbsorbed", xpAbsorbed);
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
        for(;;) {
            if (xp < 0) return xp;
            int[] coords = new int[3];//delete this
            
            for (int r=1;r<=range;++r) {
                for (int h=0;h<=r;++h) {
                    for (int b=0;b<=h;++b) {
                        //delete this
                    }
                }
            }
        }
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
                /* This code here was rendered obsolete because Java forces cases to be constant. Now i'll just have to suffer the pain of nested if statements. Also watch spotlessApply fuck it up lol
                switch (58913) { //just let me do `switch (true)` dude
                    case m>0 ? 58913 : 0:
                        m = -m;
                        break;
                    case l>0 ? 58913 : 0:
                        m = -m;
                        l = -l;
                        break;
                    case n>0 ? 58913 : 0:
                        m = -m;
                        l = -l;
                        n = -n;
                        break;
                    default:
                        n = -n;
                        l = -l;
                        m = -m;
                        switch (58913) { //If you could sneak stuff between switch branch instructions in Java, maybe things wouldn't have to be this way
                            case l<n && m<n ? 58913 : 0:
                                if (m>l) {
                                    m = l^m;
                                    l = m^l;
                                    m = l^m;
                                    break;
                                }
                                n = n^l;
                                l = l^n;
                                n = n^l;
                                break;
                            case n<l && m<l ? 58913 : 0:
                                if (n<m) {
                                    n = n^m;
                                    m = n^m;
                                    n = n^m;
                                    break;
                                }
                                m = l^m;
                                l = m^l;
                                m = l^m;
                                break;
                            case n<m && l<m ? 58913 : 0:
                                if (n<l) {
                                    n = n^l;
                                    l = l^n;
                                    n = n^l;
                                    break;
                                }
                                n = n^m;
                                m = n^m;
                                n = n^m;
                                if (l != m) {
                                    ++m;
                                    break;
                                }
                                m = 0;
                                ++l;
                                break;
                            default:
                                ++n;
                                l=0;
                                m=0;
                        }
                } //just let me use gotos dude
                */
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
