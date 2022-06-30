package tb.common.tile;

import java.lang.reflect.Field;
import net.minecraft.nbt.NBTTagCompound;
import tb.utils.TBConfig;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.TileAlchemyFurnace;

public class TileAdvAlchemicalFurnace extends TileAlchemyFurnace {
    private static Field fieldCount;
    public boolean isFuelAlumentum;

    static {
        Field field;
        try {
            field = TileAlchemyFurnace.class.getDeclaredField("count");
            field.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
            field = null;
        }
        fieldCount = field;
    }

    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        isFuelAlumentum = tag.getBoolean("isAlumentium");
    }

    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("isAlumentium", isFuelAlumentum);
    }

    public void updateEntity() {
        if (this.furnaceBurnTime == 0) {
            isFuelAlumentum = this.getStackInSlot(1) != null
                    && this.getStackInSlot(1).getItem() == ConfigItems.itemResource
                    && this.getStackInSlot(1).getItemDamage() == 0;
        }
        try {
            if (fieldCount != null && (this.isFuelAlumentum || !TBConfig.makeRequireAlumentum)) {
                int furnaceUpdateStep =
                        20; // hardcoded in thaum to be 20 (when boosted) or 40, but let's assume 20 for simplicity
                int currentCount = fieldCount.getInt(this);
                // max meaningful bonus happens when (currentStep + 1 + bonus) % furnaceUpdateStep == 0, propagating us
                // directly to next update count
                // simple checks:
                // if count=0, then we can add 19, thaum adds 1 and updates
                // if count=19, then we add 0, the thaum will arrive to update at max speed without our help
                // making sure to take remainders only from positives since java believes in negative remainders for
                // negative dividends
                int maxMeaningfulBonus =
                        (furnaceUpdateStep - (currentCount + 1) % furnaceUpdateStep) % furnaceUpdateStep;
                int bonusCount = TBConfig.speedMultiplierForFurnace - 1;
                int nextCount = currentCount + Math.min(bonusCount, maxMeaningfulBonus);

                fieldCount.setInt(this, nextCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.updateEntity();
    }
}
