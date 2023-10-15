package tb.client.render.block;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import tb.common.tile.TileBrazier;
import thaumcraft.client.renderers.block.BlockRenderer;
import thaumcraft.common.config.ConfigBlocks;

public class BrazierRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        IIcon icon = ConfigBlocks.blockCosmeticSolid.getIcon(0, 6);
        // base1
        block.setBlockBounds(W1, 0, W1, W15, W2, W15);
        renderer.setRenderBoundsFromBlock(block);
        drawFaces(renderer, block, icon, icon, icon, icon, icon, icon, true);
        // base2
        block.setBlockBounds(W2, W2, W2, W14, W4, W14);
        renderer.setRenderBoundsFromBlock(block);
        drawFaces(renderer, block, icon, icon, icon, icon, icon, icon, true);
        // pillar
        block.setBlockBounds(W4, W4, W4, W12, W12, W12);
        renderer.setRenderBoundsFromBlock(block);
        drawFaces(renderer, block, icon, icon, icon, icon, icon, icon, true);
        // bowl base
        block.setBlockBounds(W2, W12, W2, W14, W13, W14);
        renderer.setRenderBoundsFromBlock(block);
        drawFaces(renderer, block, icon, icon, icon, icon, icon, icon, true);
        // bowl 1
        block.setBlockBounds(W2, W13, W13, W14, 1, W14);
        renderer.setRenderBoundsFromBlock(block);
        drawFaces(renderer, block, icon, icon, icon, icon, icon, icon, true);
        // bowl 2
        block.setBlockBounds(W2, W13, W2, W14, 1, W3);
        renderer.setRenderBoundsFromBlock(block);
        drawFaces(renderer, block, icon, icon, icon, icon, icon, icon, true);
        // bowl 3
        block.setBlockBounds(W2, W13, W3, W3, 1, W13);
        renderer.setRenderBoundsFromBlock(block);
        drawFaces(renderer, block, icon, icon, icon, icon, icon, icon, true);
        // bowl 4
        block.setBlockBounds(W13, W13, W3, W14, 1, W13);
        renderer.setRenderBoundsFromBlock(block);
        drawFaces(renderer, block, icon, icon, icon, icon, icon, icon, true);

        icon = Blocks.coal_block.getIcon(0, 0);
        // coal
        block.setBlockBounds(W3, W13, W3, W13, W14, W13);
        renderer.setRenderBoundsFromBlock(block);
        drawFaces(renderer, block, icon, icon, icon, icon, icon, icon, true);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        renderer.renderAllFaces = true;

        renderer.setOverrideBlockTexture(ConfigBlocks.blockCosmeticSolid.getIcon(0, 6));
        // base1
        renderer.setRenderBounds(W1, 0, W1, W15, W2, W15);
        renderer.renderStandardBlock(block, x, y, z);
        // base2
        renderer.setRenderBounds(W2, W2, W2, W14, W4, W14);
        renderer.renderStandardBlock(block, x, y, z);
        // pillar
        renderer.setRenderBounds(W4, W4, W4, W12, W12, W12);
        renderer.renderStandardBlock(block, x, y, z);
        // bowl base
        renderer.setRenderBounds(W2, W12, W2, W14, W13, W14);
        renderer.renderStandardBlock(block, x, y, z);
        // bowl 1 (s)
        renderer.setRenderBounds(W2, W13, W13, W14, 1, W14);
        renderer.renderStandardBlock(block, x, y, z);
        // bowl 2 (n)
        renderer.setRenderBounds(W2, W13, W2, W14, 1, W3);
        renderer.renderStandardBlock(block, x, y, z);
        // bowl 3 (w)
        renderer.setRenderBounds(W2, W13, W3, W3, 1, W13);
        renderer.renderStandardBlock(block, x, y, z);
        // bowl 4 (e)
        renderer.setRenderBounds(W13, W13, W3, W14, 1, W13);
        renderer.renderStandardBlock(block, x, y, z);
        // coal fill
        renderer.setOverrideBlockTexture(Blocks.coal_block.getIcon(0, 0));
        renderer.setRenderBounds(W3, W13, W3, W13, W14, W13);
        renderer.renderStandardBlock(block, x, y, z);

        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile != null && tile instanceof TileBrazier && ((TileBrazier) tile).burning) {
            renderer.setOverrideBlockTexture(Blocks.fire.getIcon(2, 0));
            Tessellator.instance.setBrightness(247);
            Tessellator.instance.setColorOpaque_I(0xffffff);
            renderer.drawCrossedSquares(Blocks.fire.getIcon(2, 0), x, y + W14, z, 0.5F);
        }

        renderer.renderAllFaces = false;
        renderer.clearOverrideBlockTexture();
        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return 0x1242fd;
    }
}
