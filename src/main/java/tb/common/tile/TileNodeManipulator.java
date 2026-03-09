package tb.common.tile;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import DummyCore.Utils.MiscUtils;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.EntityAspectOrb;

public class TileNodeManipulator extends TileEntity implements IWandable {

    private static final int ticksInWorkCycle = 20;
    private int workTime = 0;
    private int maxTimeRequired = 0;
    private final HashMap<Aspect, Integer> previousNodeAspects = new HashMap<>();
    private NodeType nodeType;
    private INode node;

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
        previousNodeAspects.clear();
    }

    private void applyDestructionEffect() {
        NodeModifier nodeModifier = node.getNodeModifier();

        if (maxTimeRequired == 0) {
            if (nodeModifier == null) {
                maxTimeRequired = 2 * 60 * 20;
            } else switch (nodeModifier) {
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
                    break;
            }
        }

        if (workTime >= maxTimeRequired) {
            if (nodeModifier == NodeModifier.FADING) {
                this.worldObj.setBlockToAir(xCoord, yCoord, zCoord);
                return;
            }

            NodeModifier newNodeModifier;
            if (nodeModifier == null) {
                newNodeModifier = NodeModifier.PALE;
            } else switch (nodeModifier) {
                case BRIGHT:
                    newNodeModifier = null;
                    break;
                case PALE:
                    newNodeModifier = NodeModifier.FADING;
                    break;
                default:
                    newNodeModifier = NodeModifier.FADING;
                    break;
            }

            node.setNodeModifier(newNodeModifier);
            node.markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord - 1, zCoord);
            stopManipulator();
            return;
        }
        increaseWorkTime();
        if (!this.worldObj.isRemote && workTime % 200 == 0) {
            Aspect[] aspects = node.getAspects()
                .getAspects();
            Aspect aspect = aspects[this.worldObj.rand.nextInt(aspects.length)];
            EntityAspectOrb aspectOrb = new EntityAspectOrb(
                worldObj,
                xCoord + 0.5D,
                yCoord - 0.5D,
                zCoord + 0.5D,
                aspect,
                1);
            this.worldObj.spawnEntityInWorld(aspectOrb);
        }
    }

    private void applyEfficiencyEffect() {
        if (workTime == 0) workTime = -1;
        if (!worldObj.isRemote) {
            AspectList aspectList = node.getAspects();
            Aspect[] aspects = aspectList.getAspects();

            for (Aspect aspect : aspects) {
                if (previousNodeAspects.containsKey(aspect)) {
                    int currentAmount = aspectList.getAmount(aspect);
                    int previousAmount = previousNodeAspects.get(aspect);

                    if (currentAmount < previousAmount && this.worldObj.rand.nextInt(2) == 1) {
                        node.addToContainer(aspect, previousAmount - currentAmount);
                    }
                }
                previousNodeAspects.put(
                    aspect,
                    node.getAspects()
                        .getAmount(aspect));
            }
        }
    }

    private void applySpeedEffect() {
        if (workTime == 0) workTime = -1;

        if (!this.worldObj.isRemote && this.worldObj.rand.nextInt(5) == 1) {
            boolean isNodeChanged = false;
            AspectList aspectList = node.getAspects();
            Aspect[] aspects = aspectList.getAspects();

            for (Aspect aspect : aspects) {
                int maximum = node.getNodeVisBase(aspect);
                int currentAmount = aspectList.getAmount(aspect);

                if (currentAmount < maximum) {
                    node.addToContainer(aspect, 1);
                    isNodeChanged = true;
                }
            }

            if (isNodeChanged) {
                MiscUtils.sendPacketToAllAround(
                    worldObj,
                    this.worldObj.getTileEntity(xCoord, yCoord - 1, zCoord)
                        .getDescriptionPacket(),
                    xCoord,
                    yCoord,
                    zCoord,
                    this.worldObj.provider.dimensionId,
                    6);
            }
        }
    }

    private void applyStabilityEffect() {
        NodeModifier nodeModifier = node.getNodeModifier();

        if (nodeModifier == null || nodeModifier == nodeModifier.BRIGHT) {
            switch (nodeType) {
                case DARK:
                    changeNodeType(NodeType.DARK, NodeType.NORMAL, 2 * 60 * 20);
                    break;
                case HUNGRY:
                    changeNodeType(NodeType.HUNGRY, NodeType.NORMAL, 30 * 20);
                    break;
                case UNSTABLE:
                    changeNodeType(NodeType.UNSTABLE, NodeType.NORMAL, 7 * 30 * 20);
                    break;
                case TAINTED:
                    changeNodeType(NodeType.TAINTED, NodeType.NORMAL, 30 * 60 * 20);
                    break;
                default:
                    break;
            }
        } else switch (nodeModifier) {
            case FADING:
                changeNodeModifier(NodeModifier.FADING, NodeModifier.PALE, 5 * 60 * 20);
                break;
            case PALE:
                changeNodeModifier(NodeModifier.PALE, null, 10 * 60 * 20);
                break;
            default:
                break;
        }
    }

    private void increaseWorkTime() {
        workTime += ticksInWorkCycle;
    }

    private void changeNodeModifier(NodeModifier fromNodeModifier, NodeModifier toModifierNode, int time) {
        if (node.getNodeModifier() == fromNodeModifier) {
            if (maxTimeRequired == 0) maxTimeRequired = time;

            if (workTime >= maxTimeRequired) {
                node.setNodeModifier(toModifierNode);
                node.markDirty();
                worldObj.markBlockForUpdate(xCoord, yCoord - 1, zCoord);
                stopManipulator();
            } else increaseWorkTime();
        }
    }

    private void changeNodeType(NodeType fromNodeType, NodeType toTypeNode, int time) {
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
        int effect = this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord) - 1;
        long ticks = worldObj.getWorldTime() + 7;

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
                getColor(effect),
                false,
                0.5F,
                2);
        }

        // Logic
        if (ticks % 2 == 0) {
            node = getNode();

            if (node == null || this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord) == 0) {
                if (workTime != 0) stopManipulator();
                return;
            }
            nodeType = node.getNodeType();

            if (effect == 2) {
                applyEfficiencyEffect();
            }

            if (ticks % ticksInWorkCycle == 0) {

                switch (effect) {
                    case 0: // Brightness
                        changeNodeModifier(null, NodeModifier.BRIGHT, 20 * 60 * 20);
                        break;
                    case 1: // Destruction
                        applyDestructionEffect();
                        break;
                    case 2: // Efficiency
                        break;
                    case 3: // Hunger
                        changeNodeType(NodeType.NORMAL, NodeType.HUNGRY, 5 * 60 * 20);
                        break;
                    case 4: // Instability
                        changeNodeType(NodeType.NORMAL, NodeType.UNSTABLE, 7 * 60 * 20);
                        break;
                    case 5: // Purity
                        changeNodeType(NodeType.NORMAL, NodeType.PURE, 3 * 60 * 20);
                        changeNodeType(NodeType.TAINTED, NodeType.NORMAL, 33 * 60 * 20);
                        break;
                    case 6: // Sinister
                        changeNodeType(NodeType.NORMAL, NodeType.DARK, 6 * 60 * 20);
                        changeNodeType(NodeType.PURE, NodeType.NORMAL, 15 * 60 * 20);
                        break;
                    case 7: // Speed
                        applySpeedEffect();
                        break;
                    case 8: // Stability
                        applyStabilityEffect();
                        break;
                    case 9: // Taint
                        changeNodeType(NodeType.NORMAL, NodeType.TAINTED, 6 * 60 * 20);
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
