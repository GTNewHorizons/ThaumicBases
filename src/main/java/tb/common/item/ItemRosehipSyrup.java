package tb.common.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigItems;

public class ItemRosehipSyrup extends Item {

    public final static int REMOVE_EFFECT = 1;
    public final static int REDUCE_EFFECT = 2;

    public ItemStack onItemRightClick(ItemStack stack, World w, EntityPlayer player) {
        player.setItemInUse(stack, getMaxItemUseDuration(stack));
        return stack;
    }

    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.drink;
    }

    public int getMaxItemUseDuration(ItemStack stack) {
        return 32;
    }

    @SuppressWarnings("unchecked")
    public ItemStack onEaten(ItemStack stack, World w, EntityPlayer player) {
        if (!player.capabilities.isCreativeMode) {
            --stack.stackSize;
            if (stack.stackSize > 0) {
                if (!player.inventory.addItemStackToInventory(new ItemStack(ConfigItems.itemEssence, 1, 0)))
                    player.dropPlayerItemWithRandomChoice(new ItemStack(ConfigItems.itemEssence, 1, 0), false);
            }
        }

        if (!w.isRemote) {
            Collection<PotionEffect> c = player.getActivePotionEffects();
            Iterator<PotionEffect> $i = c.iterator();
            ArrayList<Integer> removeEffects = new ArrayList<Integer>();
            ArrayList<PotionEffect> reduceEffects = new ArrayList<PotionEffect>();
            while ($i.hasNext()) {
                PotionEffect effect = $i.next();
                int i = processPotion(effect);
                if (i == REMOVE_EFFECT) removeEffects.add(effect.getPotionID());
                else if (i == REDUCE_EFFECT) reduceEffects.add(effect);
            }

            for (int i = 0; i < removeEffects.size(); ++i) {
                int j = removeEffects.get(i);
                if (j != -1 && j < Potion.potionTypes.length && Potion.potionTypes[j] != null) {
                    if (player.getActivePotionEffect(Potion.potionTypes[j]) != null) {
                        player.removePotionEffect(j);
                    }
                }
            }
            for (int i = 0; i < reduceEffects.size(); ++i) {
                PotionEffect j = reduceEffects.get(i);
                if (j != null) {
                    int k = j.getPotionID();
                    if (k != -1 && k < Potion.potionTypes.length
                        && player.getActivePotionEffect(Potion.potionTypes[k]) != null) {
                        reflectPotionEffect(player, j);
                    }
                }
            }
        }

        return stack.stackSize <= 0 ? new ItemStack(ConfigItems.itemEssence, 1, 0) : stack;
    }

    public static int processPotion(PotionEffect effect) {
        if (effect != null && effect.getPotionID() < Potion.potionTypes.length
            && Potion.potionTypes[effect.getPotionID()] != null) {
            if (canRemoveEffect(Potion.potionTypes[effect.getPotionID()])) return REMOVE_EFFECT;

            if (canDecreaseLevel(effect.getPotionID())) {
                if (effect.getAmplifier() == 0) return REMOVE_EFFECT;
                else return REDUCE_EFFECT;
            }
        }

        return 0;
    }

    public static boolean canRemoveEffect(Potion p) {
        return p != null && (p == Potion.blindness || p == Potion.confusion
            || p == Potion.digSlowdown
            || p == Potion.hunger
            || p == Potion.moveSlowdown
            || p == Potion.poison
            || p == Potion.weakness
            || p == Potion.wither);
    }

    public static boolean canDecreaseLevel(int id) {
        return id == Config.potionBlurredID || id == Config.potionInfVisExhaustID
            || id == Config.potionTaintPoisonID
            || id == Config.potionThaumarhiaID
            || id == Config.potionUnHungerID
            || id == Config.potionVisExhaustID;
    }

    public static void reflectPotionEffect(EntityPlayer p, PotionEffect effect) {
        int amp = effect.getAmplifier() - 1;
        int id = effect.getPotionID();
        int dur = effect.getDuration();
        boolean transparent = effect.getIsAmbient();
        PotionEffect neweffect = new PotionEffect(id, dur, amp, transparent);
        neweffect.setCurativeItems(effect.getCurativeItems());
        p.removePotionEffect(effect.getPotionID());
        p.addPotionEffect(neweffect);
    }
}
