package tb.common.tile;

import static tb.common.tile.Effect.*;

import java.util.Hashtable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.EntityAspectOrb;
import DummyCore.Utils.MiscUtils;

enum Effect {
    BRIGHTNESS,
    DESTRUCTION,
    EFFICIENCY,
    HUNGER,
    INSTABILITY,
    PURITY,
    SINISTER,
    SPEED,
    STABILITY,
    TAINT,
    EMPTY
}

public class TileNodeManipulator extends TileEntity implements IWandable {

    private int workTime = 0;
    private int maxTimeRequired = 0;
    public Hashtable<String, Integer> nodeAspectsOldState = new Hashtable<String, Integer>();
    private NodeType nodeType;
    private INode node;

    private Effect castEffectToEnum(int effectNum) {
        switch (effectNum) {
            case 0:
                return BRIGHTNESS;
            case 1:
                return DESTRUCTION;
            case 2:
                return EFFICIENCY;
            case 3:
                return HUNGER;
            case 4:
                return INSTABILITY;
            case 5:
                return PURITY;
            case 6:
                return SINISTER;
            case 7:
                return SPEED;
            case 8:
                return STABILITY;
            case 9:
                return TAINT;
            default:
                return EMPTY;
        }
    }

    private int getColor(int effect) {
        int color;
        switch (effect) {
            case 1:
                color = 0x4e4756;
                break;
            case 2:
                color = 0xd2d200;
                break;
            case 3:
                color = 0xaf7c23;
                break;
            case 4:
                color = 0x0b4d42;
                break;
            case 5:
                color = 0xccc8f7;
                break;
            case 6:
                color = 0x643c5b;
                break;
            case 7:
                color = 0xeaeaea;
                break;
            case 8:
                color = 0xd0e0f8;
                break;
            case 9:
                color = 0x713496;
                break;
            default:
                color = 0xffffff;
        }
        return color;
    }

    private void stopManipulator() {
        workTime = 0;
        maxTimeRequired = 0;
        node = null;
        nodeAspectsOldState.clear();
    }

    private void applyEffectDESTRUCTION() {
        NodeModifier nodeModifier = node.getNodeModifier();

        if (maxTimeRequired == 0) {
            switch (nodeModifier) {
                case BRIGHT:
                    maxTimeRequired = 60 * 20;
                    break;
                case PALE:
                    maxTimeRequired = 3 * 60 * 20;
                    break;
                case FADING:
                    maxTimeRequired = 6 * 60 * 20;
                    break;
                default:
                    maxTimeRequired = 2 * 60 * 20;
                    break;
            }
        }

        if (workTime >= maxTimeRequired) {
            stopManipulator();

            if (nodeModifier == NodeModifier.FADING) {
                this.worldObj.setBlockToAir(xCoord, yCoord, zCoord);
                return;
            }

            NodeModifier newNodeModifier;
            switch (nodeModifier) {
                case BRIGHT:
                    newNodeModifier = null;
                    break;
                case PALE:
                    newNodeModifier = NodeModifier.FADING;
                    break;
                default:
                    newNodeModifier = NodeModifier.PALE;
            }

            node.setNodeModifier(newNodeModifier);

        } else {
            increaseWorkTime();
            if (!this.worldObj.isRemote && workTime % 200 == 0) {
                Aspect a = node.getAspects().getAspects()[this.worldObj.rand
                        .nextInt(node.getAspects().getAspects().length)];
                EntityAspectOrb aspect = new EntityAspectOrb(
                        worldObj,
                        xCoord + 0.5D,
                        yCoord - 0.5D,
                        zCoord + 0.5D,
                        a,
                        1);
                this.worldObj.spawnEntityInWorld(aspect);
            }
        }
    }

    private void applyEffectEFFICIENCY() {
        if (workTime == 0) workTime = -1;
        if (!worldObj.isRemote) {
            AspectList aspectList = node.getAspects();
            Aspect[] aspects = aspectList.getAspects();

            for (Aspect a : aspects) {
                if (nodeAspectsOldState.containsKey(a.getTag())) {
                    int current = aspectList.getAmount(a);
                    int prev = nodeAspectsOldState.get(a.getTag());

                    if (current < prev && this.worldObj.rand.nextInt(2) == 1) {
                        aspectList.add(a, prev - current);
                    }
                }
            }

            // save node state to array
            for (int i = 0; i < node.getAspects().size(); ++i) {
                nodeAspectsOldState.put(
                        node.getAspects().getAspects()[i].getTag(),
                        node.getAspects().getAmount(node.getAspects().getAspects()[i]));
            }
        }
    }

    private void applyEffectSPEED() {
        if (workTime == 0) workTime = -1;

        if (!this.worldObj.isRemote && this.worldObj.rand.nextInt(5) == 1) {
            boolean isNodeChanged = false;
            AspectList aspectList = node.getAspects();
            Aspect[] aspects = aspectList.getAspects();

            for (Aspect a : aspects) {
                int max = node.getNodeVisBase(a);
                int currentAmount = aspectList.getAmount(a);

                if (currentAmount < max) {
                    node.getAspects().add(a, 1);
                    isNodeChanged = true;
                }
            }

            if (isNodeChanged) {
                MiscUtils.sendPacketToAllAround(
                        worldObj,
                        this.worldObj.getTileEntity(xCoord, yCoord - 1, zCoord).getDescriptionPacket(),
                        xCoord,
                        yCoord,
                        zCoord,
                        this.worldObj.provider.dimensionId,
                        6);
            }
        }
    }

    private void applyEffectSTABILITY() {
        NodeModifier nodeModifier = node.getNodeModifier();
        if (nodeModifier == null) {
            nodeModifierManipulation(null, NodeModifier.BRIGHT, 5 * 60 * 20);
            return;
        }

        switch (nodeModifier) {
            case FADING:
                nodeModifierManipulation(NodeModifier.FADING, NodeModifier.PALE, 5 * 60 * 20);
                break;
            case PALE:
                nodeModifierManipulation(NodeModifier.PALE, null, 10 * 60 * 20);
                break;
            default: {
                switch (nodeType) {
                    case DARK:
                        nodeTypeManipulation(NodeType.DARK, NodeType.NORMAL, 2 * 60 * 20);
                        break;
                    case HUNGRY:
                        nodeTypeManipulation(NodeType.HUNGRY, NodeType.NORMAL, 30 * 20);
                        break;
                    case UNSTABLE:
                        nodeTypeManipulation(NodeType.UNSTABLE, NodeType.NORMAL, 7 * 30 * 20);
                        break;
                    case TAINTED:
                        nodeTypeManipulation(NodeType.TAINTED, NodeType.NORMAL, 30 * 30 * 20);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void increaseWorkTime() {
        workTime += 20;
    }

    private void nodeModifierManipulation(NodeModifier fromNodeModifier, NodeModifier toModifierNode, int time) {
        if (node.getNodeModifier() == fromNodeModifier) {
            if (maxTimeRequired == 0) maxTimeRequired = time;

            if (workTime >= maxTimeRequired) {
                node.setNodeModifier(toModifierNode);
                stopManipulator();
            } else increaseWorkTime();
        }
    }

    private void nodeTypeManipulation(NodeType fromNodeType, NodeType toTypeNode, int time) {
        if (nodeType == fromNodeType) {
            if (maxTimeRequired == 0) maxTimeRequired = time;

            if (workTime >= maxTimeRequired) {
                node.setNodeType(toTypeNode);
                stopManipulator();
            } else increaseWorkTime();
        }
    }

    @Override
    public void updateEntity() {
        int effectId = this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord) - 1;
        long tics = worldObj.getWorldTime() + 7;

        // Graphic
        if (this.worldObj.isRemote && workTime != 0) {
            Thaumcraft.proxy.beam(
                    this.worldObj,
                    xCoord + 0.5D,
                    yCoord + 0.5D,
                    zCoord + 0.5D,
                    xCoord + 0.5D,
                    yCoord - 0.5D,
                    zCoord + 0.5D,
                    2,
                    getColor(effectId),
                    false,
                    0.5F,
                    2);
        }

        // Logic
        if (tics % 2 == 0) {
            node = getNode();

            if (node == null || this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord) == 0) {
                if (workTime != 0) stopManipulator();
                return;
            }

            Effect effect = castEffectToEnum(effectId);
            nodeType = node.getNodeType();

            if (effect == EFFICIENCY) {
                applyEffectEFFICIENCY();
            }

            if (tics % 20 == 0) {

                switch (effect) {
                    case EMPTY:
                        return;
                    case BRIGHTNESS:
                        nodeModifierManipulation(null, NodeModifier.BRIGHT, 24000);
                        break;
                    case DESTRUCTION:
                        applyEffectDESTRUCTION();
                        break;
                    case EFFICIENCY:
                        break;
                    case HUNGER:
                        nodeTypeManipulation(NodeType.NORMAL, NodeType.HUNGRY, 6000);
                        break;
                    case INSTABILITY:
                        nodeTypeManipulation(NodeType.NORMAL, NodeType.UNSTABLE, 8400);
                        break;
                    case PURITY:
                        nodeTypeManipulation(NodeType.NORMAL, NodeType.PURE, 3600);
                        nodeTypeManipulation(NodeType.TAINTED, NodeType.NORMAL, 39600);
                        break;
                    case SINISTER:
                        nodeTypeManipulation(NodeType.NORMAL, NodeType.DARK, 7200);
                        nodeTypeManipulation(NodeType.PURE, NodeType.NORMAL, 18000);
                        break;
                    case SPEED:
                        applyEffectSPEED();
                        break;
                    case STABILITY:
                        applyEffectSTABILITY();
                        break;
                    case TAINT:
                        nodeTypeManipulation(NodeType.NORMAL, NodeType.TAINTED, 7200);
                        break;
                }
            }
        }
    }

    public INode getNode() {
        if (this.worldObj.getTileEntity(xCoord, yCoord - 1, zCoord) instanceof INode)
            return (INode) worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);

        return null;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        workTime = tag.getInteger("workTime");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("workTime", workTime);
    }

    @Override
    public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side,
            int md) {
        return 0;
    }

    @Override
    public ItemStack onWandRightClick(World world, ItemStack wandstack, EntityPlayer player) {
        return wandstack;
    }

    @Override
    public void onUsingWandTick(ItemStack wandstack, EntityPlayer player, int count) {}

    @Override
    public void onWandStoppedUsing(ItemStack wandstack, World world, EntityPlayer player, int count) {}
}
