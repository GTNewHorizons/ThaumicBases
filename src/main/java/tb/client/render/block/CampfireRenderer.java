package tb.client.render.block;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import thaumcraft.client.renderers.block.BlockRenderer;
import thaumcraft.common.config.ConfigBlocks;

public class CampfireRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {}

    @Override
    public boolean renderWorldBlock(
            IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        renderer.renderAllFaces = true;
        /*
         * looks like shit
        renderer.setOverrideBlockTexture(Blocks.cobblestone.getIcon(0, 0));

        Random rand = new Random(x+y+z);

        for(int i = 0; i < 6+rand.nextInt(5); ++i)
        {
        	double dx = rand.nextDouble();
        	double dy = 0;
        	double dz = rand.nextDouble();

        	renderer.setRenderBounds(dx, dy, dz, dx+0.1D, dy+0.1D, dz+0.1D);
        	renderer.renderStandardBlock(Blocks.stone, x, y, z);
        }*/

        // gravel base
        renderer.setOverrideBlockTexture(Blocks.gravel.getIcon(0, 0));
        renderer.setRenderBounds(W1, 0, W1, W15, W1, W15);
        renderer.renderStandardBlock(block, x, y, z);

        // coal pit
        renderer.setOverrideBlockTexture(Blocks.coal_block.getIcon(0, 0));
        renderer.setRenderBounds(0.25D, 0, 0.25D, 0.75D, W2 - 0.001, 0.75D);
        renderer.renderStandardBlock(block, x, y, z);

        // logs
        renderer.setOverrideBlockTexture(ConfigBlocks.blockMagicalLog.getIcon(2, 0));
        double yIndex = 0;
        if (world.getBlockMetadata(x, y, z) > 0)
            for (int i = 0; i < 2; ++i) {
                renderer.setRenderBounds(W3, 0 + yIndex + (W2 * i), W2, W5, W2 + yIndex + (W2 * i), W14); // layer 1,1
                renderer.renderStandardBlock(block, x, y, z);
                renderer.setRenderBounds(W11, 0 + yIndex + (W2 * i), W2, W13, W2 + yIndex + (W2 * i), W14); // layer 1,2
                renderer.renderStandardBlock(block, x, y, z);

                renderer.setRenderBounds(W2, W2 + yIndex + (W2 * i), W3, W14, W4 + yIndex + (W2 * i), W5); // layer 2,1
                renderer.renderStandardBlock(block, x, y, z);
                renderer.setRenderBounds(
                        W2, W2 + yIndex + (W2 * i), W11, W14, W4 + yIndex + (W2 * i), W13); // layer 2,2
                renderer.renderStandardBlock(block, x, y, z);
                yIndex += W2;
            }

        if (world.getBlockMetadata(x, y, z) > 1) {
            renderer.setOverrideBlockTexture(Blocks.fire.getIcon(2, 0));
            Tessellator.instance.setBrightness(247);
            Tessellator.instance.setColorOpaque_I(0xffffff);
            renderer.drawCrossedSquares(Blocks.fire.getIcon(2, 0), x, y, z, 0.75F);
        }

        renderer.renderAllFaces = false;
        renderer.clearOverrideBlockTexture();
        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return false;
    }

    @Override
    public int getRenderId() {
        return 0x1242fe;
    }
}
