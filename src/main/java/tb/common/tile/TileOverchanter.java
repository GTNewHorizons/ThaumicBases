package tb.common.tile;

import static tb.core.TBCore.isAutomagyLoaded;
import static tb.core.TBCore.isEioLoaded;

import java.util.List;
import java.util.Map;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.gtnhlib.geometry.CubeIterator;

import DummyCore.Utils.MathUtils;
import DummyCore.Utils.MiscUtils;
import crazypants.enderio.machine.obelisk.xp.TileExperienceObelisk;
import crazypants.enderio.xp.ExperienceContainer;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.lib.events.EssentiaHandler;
import tuhljin.automagy.tiles.TileEntityJarXP;

public class TileOverchanter extends TileEntity implements ISidedInventory, IWandable {

    public ItemStack inventory;

    public int enchantingTicks;
    public int xpToAbsorb;
    public boolean isEnchantingStarted;

    public static final int xp30lv = 825;
    public static final String overchantsNbtTag = "overchants";
    public static final int nbtIntArrayType = 11;

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) {
            return;
        }

        if (this.inventory == null) {
            isEnchantingStarted = false;
            xpToAbsorb = xp30lv; // 30 levels
            enchantingTicks = 0;
            return;
        }
        if (this.isEnchantingStarted) {
            if (enchantingTicks % 20 == 0) {
                this.worldObj
                    .playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "thaumcraft:infuserstart", 1F, 1.0F);
                if (EssentiaHandler.drainEssentia(this, Aspect.MAGIC, ForgeDirection.UNKNOWN, 8, false)) {
                    // +1 is needed to exactly mimic original timings and not use extra 1 essentia (should be 32 in
                    // perfect conditions)
                    int enchantingSeconds = enchantingTicks / 20 + 1;
                    // the values being checked against are the second milestones in the enchanting process
                    if (enchantingSeconds >= 16) {
                        if (xpToAbsorb != 0) absorbXP();
                        if (enchantingSeconds >= 32 && xpToAbsorb == 0) {
                            int enchId = this.findEnchantment(inventory);
                            increaseEnchantmentLevel(enchId);
                            addOverchantId(enchId);
                            isEnchantingStarted = false;
                            xpToAbsorb = xp30lv;
                            enchantingTicks = 0;
                            this.worldObj
                                .playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "thaumcraft:wand", 1F, 1F);
                            return;
                        }
                    }

                } else {
                    enchantingTicks -= 20;
                }
            }
            enchantingTicks += 1;
        }
    }

    private void increaseEnchantmentLevel(int enchantmentId) {
        Map<Integer, Integer> enchantments = EnchantmentHelper.getEnchantments(this.inventory);
        int newLevel = MathHelper.clamp_int(enchantments.get(enchantmentId) + 1, 1, Short.MAX_VALUE);
        enchantments.put(enchantmentId, newLevel);
        EnchantmentHelper.setEnchantments(enchantments, this.inventory);
    }

    private void addOverchantId(int enchantmentId) {
        NBTTagCompound stackTag = MiscUtils.getStackTag(inventory);
        if (!stackTag.hasKey(overchantsNbtTag)) {
            stackTag.setIntArray(overchantsNbtTag, new int[] { enchantmentId });
        } else {
            int[] arrayInt = stackTag.getIntArray(overchantsNbtTag);
            int[] newArrayInt = new int[arrayInt.length + 1];
            System.arraycopy(arrayInt, 0, newArrayInt, 0, arrayInt.length);
            newArrayInt[newArrayInt.length - 1] = enchantmentId;
            stackTag.setIntArray(overchantsNbtTag, newArrayInt);
        }
    }

    public boolean tryStartEnchanting() {
        if (canStartEnchanting()) {
            this.isEnchantingStarted = true;
            this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "thaumcraft:craftstart", 0.5F, 1.0F);
            return true;
        }
        return false;
    }

    public boolean canStartEnchanting() {
        if (this.isEnchantingStarted || this.inventory == null) {
            return false;
        }
        NBTTagList enchantmentTagList = this.inventory.getEnchantmentTagList();
        if (enchantmentTagList == null || enchantmentTagList.tagCount() < 1) {
            return false;
        }
        return findEnchantment(inventory) != -1;
    }

    public int findEnchantment(ItemStack enchanted) {
        Map<Integer, Integer> enchantments = EnchantmentHelper.getEnchantments(enchanted);
        if (enchantments.isEmpty()) {
            return -1;
        }

        NBTTagCompound stackTag = MiscUtils.getStackTag(enchanted);
        if (!stackTag.hasKey(overchantsNbtTag)) {
            return enchantments.keySet()
                .iterator()
                .next();
        }
        if (!stackTag.hasKey(overchantsNbtTag, nbtIntArrayType)) {
            return -1;
        }
        int[] overchants = stackTag.getIntArray(overchantsNbtTag);

        for (int id : enchantments.keySet()) {
            if (!MathUtils.arrayContains(overchants, id)) {
                return id;
            }
        }

        return -1;
    }

    public void syncToClients() {
        if (worldObj.isRemote) {
            return;
        }
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        MiscUtils.syncTileEntity(tag, 0);
    }

    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (!worldObj.isRemote) {
            syncToClients();
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
            } else {
                itemstack = this.inventory.splitStack(num);
                if (this.inventory.stackSize == 0) {
                    this.inventory = null;
                }
            }
            this.markDirty();
            return itemstack;
        } else {
            return null;
        }
    }

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
        return stk.hasTagCompound() && stk.getEnchantmentTagList() != null
            && stk.getEnchantmentTagList()
                .tagCount() > 0
            && findEnchantment(stk) != -1;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack item, int side) {
        return !isItemValidForSlot(0, item);
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack item, int side) {
        return isItemValidForSlot(0, item);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return new int[] { 0 };
    }

    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        enchantingTicks = tag.getInteger("enchTime");
        xpToAbsorb = tag.hasKey("xpToAbsorb") ? tag.getInteger("xpToAbsorb") : xp30lv;
        isEnchantingStarted = tag.getBoolean("enchStarted");

        if (tag.hasKey("itm")) {
            inventory = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("itm"));
        } else {
            inventory = null;
        }
    }

    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        tag.setInteger("enchTime", enchantingTicks);
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
        if (!world.isRemote && tryStartEnchanting()) {
            player.swingItem();
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

    public void absorbXP() {
        // Note that the drain functions shouldn't be in a non-remote test because of player damage fallback
        if (isAutomagyLoaded) {
            // This scans a 17x17x17 cube centered around the TE, prioritizing the closest sources
            this.xpToAbsorb = this.drainXPJarsInRange(this.xpToAbsorb, 8);
            if (xpToAbsorb == 0) return;
        }
        if (isEioLoaded) {
            this.xpToAbsorb = this.drainEIOObelisksInRange(this.xpToAbsorb, 8);
            if (xpToAbsorb == 0) return;
        }
        List<EntityPlayer> players = this.worldObj.getEntitiesWithinAABB(
            EntityPlayer.class,
            AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1)
                .expand(6, 3, 6));
        if (!players.isEmpty()) {
            // See https://minecraft.wiki/w/Experience#Values_from_Java_Edition_1.3.1_to_1.8_(14w02a) for XP math
            // Uses a double in the second branch so that float division is used
            int lvlsleft = (int) Math
                .round(xpToAbsorb > 255 ? (59 + Math.sqrt(24 * xpToAbsorb - 5159)) / 6 : xpToAbsorb / 17d);
            for (EntityPlayer p : players) {
                if (p.experienceLevel >= lvlsleft) {
                    p.attackEntityFrom(DamageSource.magic, 8);
                    this.worldObj.playSoundEffect(p.posX, p.posY, p.posZ, "thaumcraft:zap", 1F, 1.0F);
                    p.experienceLevel -= lvlsleft;
                    this.xpToAbsorb = 0;
                    // If anyone else wants to implement the exact formula for experience draining, you can
                    return;
                }
            }
        }
    }

    public int drainXPJarsInRange(int xp, int range) {
        if (xp <= 0) return xp;
        CubeIterator cubeIter = new CubeIterator(range);
        int jarxp;
        while (cubeIter.hasNext()) {
            cubeIter.next();
            if (this.worldObj.getTileEntity(
                cubeIter.n + this.xCoord,
                cubeIter.l + this.yCoord,
                cubeIter.m + this.zCoord) instanceof TileEntityJarXP jar) {
                jarxp = jar.getXP();
                if (jarxp < xp) {
                    if (!worldObj.isRemote) jar.setXP(0);
                    xp -= jarxp;
                    continue;
                }
                if (!worldObj.isRemote) jar.setXP(jarxp - xp);
                return 0;
            }
        }
        return xp;
        // This algorithm drains each jar it comes across sequentially without regard to how full they are. If you would
        // rather it prioritize emptying barely filled jars within a certain radius, and then emptying non-full jars,
        // then instead have a counter sinceLastPrioJar that starts at 0 and increments each iteration, and cache the
        // most recent "priority" jar (lowest non-full jar with jarxp > xp argument); reset that counter per each jar
        // with jarxp under xp argument, and drain each jar with jarxp under xp argument, stopping once the counter
        // reaches an arbitrary count of blocks searched without draining a jar, and then drain the cached lowest-filled
        // jar. The TC EssentiaHandler would be so much better if it worked that way as well.
    }

    // EIO version (separated so that it prioritizes the magic jars, feel free to switch this to being in sync instead)
    public int drainEIOObelisksInRange(int xp, int range) {
        if (xp <= 0) return xp;
        CubeIterator cubeIter = new CubeIterator(range);
        int jarxp;
        while (cubeIter.hasNext()) {
            cubeIter.next();
            if (this.worldObj.getTileEntity(
                cubeIter.n + this.xCoord,
                cubeIter.l + this.yCoord,
                cubeIter.m + this.zCoord) instanceof TileExperienceObelisk obelisk) {
                ExperienceContainer cont = obelisk.getContainer();
                jarxp = cont.getExperienceTotal();
                // goddamn private fields with no good setters
                if (!worldObj.isRemote) {
                    cont.drain(null, Integer.MAX_VALUE, true);
                    cont.addExperience(Math.max(0, jarxp - xp));
                }
                // if this causes desyncs, remove the !worldObj.isRemote test or add an additional multiplayer test
                xp -= jarxp;
                if (xp <= 0) return 0;
            }
        }
        return xp;
    }

}
