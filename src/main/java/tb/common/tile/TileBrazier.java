package tb.common.tile;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.monster.IMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import tb.common.entity.EntityAIAvoidCampfire;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.lib.events.EssentiaHandler;

public class TileBrazier extends TileEntity {
    public int burnTime;
    public int uptime;
    public boolean burning;

    public TileBrazier() {
        this.burning = false;
    }

    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        burnTime = tag.getInteger("burnTime");
    }

    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("burnTime", burnTime);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void updateEntity() {
        ++uptime;

        if (burnTime >= 0) {
            --burnTime;
        } else {
            if (EssentiaHandler.drainEssentia(this, Aspect.FIRE, ForgeDirection.UNKNOWN, 6, false)) {
                burnTime = 1600;
                this.burning = true;
                this.markDirty();
                this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            }
        }

        if (burnTime == -1) {
            this.burning = false;
            this.markDirty();
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            return;
        }

        // System.out.println("burn" + burnTime);

        if (!this.worldObj.isRemote) {
            List<EntityCreature> creatures = this.worldObj.getEntitiesWithinAABB(
                    EntityCreature.class,
                    AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1)
                            .expand(12, 6, 12));

            for (EntityCreature c : creatures) {
                if (!(c instanceof IMob)) continue;

                ArrayList entries = (ArrayList) c.tasks.taskEntries;

                EntityAIAvoidCampfire campfireAviodable = null;

                boolean hasTask = false;
                for (int i = 0; i < entries.size(); ++i) {
                    EntityAITasks.EntityAITaskEntry task = (EntityAITaskEntry) entries.get(i);
                    if ((task.action instanceof EntityAIAvoidCampfire)) {
                        campfireAviodable = (EntityAIAvoidCampfire) task.action;
                        hasTask = true;
                        break;
                    }
                }
                if (campfireAviodable == null) campfireAviodable = new EntityAIAvoidCampfire(c);

                campfireAviodable.isScared = true;
                campfireAviodable.scareTimer = 100;
                campfireAviodable.campfireX = xCoord;
                campfireAviodable.campfireY = yCoord;
                campfireAviodable.campfireZ = zCoord;

                if (!hasTask) c.tasks.addTask(1, campfireAviodable);
            }
        }
    }
}
