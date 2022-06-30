package tb.common.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tb.common.tile.TileNodeLinker;

public class BlockNodeLinker extends BlockContainer {

    public BlockNodeLinker() {
        super(Material.rock);
    }

    @Override
    public TileEntity createNewTileEntity(World w, int meta) {
        return new TileNodeLinker();
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    public int getRenderType() {
        return 0x421922;
    }
}
