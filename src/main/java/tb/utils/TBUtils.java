package tb.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.util.FakePlayer;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketAspectPool;
import thaumcraft.common.lib.research.ResearchManager;

public class TBUtils {

    public static void addAspectToKnowledgePool(EntityPlayer player, Aspect aspect, short amount) {
        Thaumcraft.proxy.playerKnowledge.addAspectPool(player.getCommandSenderName(), aspect, amount);
        ResearchManager.scheduleSave(player);
        if (player instanceof EntityPlayerMP) if (!(player instanceof FakePlayer)) {
            PacketHandler.INSTANCE.sendTo(
                new PacketAspectPool(
                    aspect.getTag(),
                    Short.valueOf(amount),
                    Short.valueOf(
                        Thaumcraft.proxy.playerKnowledge.getAspectPoolFor(player.getCommandSenderName(), aspect))),
                (EntityPlayerMP) player);
        }
    }

    /**
     * @param type 0 = Temporary, 1 = Regular, 2 = Permanent
     */
    public static void addWarpToPlayer(EntityPlayer addTo, int amount, int type) {
        switch (type) {
            case 2: {
                Thaumcraft.addWarpToPlayer(addTo, amount, false);
                return;
            }
            case 1: {
                Thaumcraft.addStickyWarpToPlayer(addTo, amount);
                return;
            }
            case 0: {
                Thaumcraft.addWarpToPlayer(addTo, amount, true);
                return;
            }
            default: {
                Thaumcraft.addWarpToPlayer(addTo, amount, false);
                return;
            }
        }
    }
}
