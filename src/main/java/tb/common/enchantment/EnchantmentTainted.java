package tb.common.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;

public class EnchantmentTainted extends Enchantment {

    public EnchantmentTainted(int id, int weight, EnumEnchantmentType type) {
        super(id, weight, type);
    }

    public int getMaxLevel() {
        return 3;
    }

    public boolean canApplyTogether(Enchantment ench) {
        return !(ench instanceof EnchantmentElderKnowledge)
                && !(ench instanceof EnchantmentMagicTouch)
                && !(ench instanceof EnchantmentTainted)
                && !(ench instanceof EnchantmentVaporising);
    }

    // do I even want to know why this is here?

    public boolean canApply(ItemStack itemStack) {
        return false;
    }
}
