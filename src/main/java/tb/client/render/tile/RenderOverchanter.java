package tb.client.render.tile;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;
import tb.common.tile.TileOverchanter;

public class RenderOverchanter extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(
            TileEntity tile, double screenX, double screenY, double screenZ, float partialTicks) {
        TileOverchanter overchanter = (TileOverchanter) tile;

        /*if(overchanter.inventory != null)
        {
        	GL11.glPushMatrix();

        	MiscUtils.renderItemStack_Full(overchanter.inventory, 0, 0, 0, screenX, screenY, screenZ, tile.getWorldObj().getWorldTime()%360, 0, 1, 1, 1, 0.5F, 0.9F, 0.5F);

        	GL11.glPopMatrix();
        }*/

        GL11.glPushMatrix();
        GL11.glTranslated(screenX, screenY + 0.75, screenZ);

        ItemStack item = overchanter.inventory;
        if (item != null) {
            GL11.glPushMatrix();
            GL11.glRotatef(90F, 1F, 0F, 0F);
            final float scale = 0.7F;
            GL11.glScalef(scale, scale, scale);
            GL11.glTranslatef(0.6F, -0.2F, 0F);
            GL11.glRotatef(30F, 0F, 0F, 1F);

            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationItemsTexture);

            int renderPass = 0;
            do {
                IIcon icon = item.getItem().getIcon(item, renderPass);
                if (icon != null) {
                    Color color = new Color(item.getItem().getColorFromItemStack(item, renderPass));
                    GL11.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
                    float f = icon.getMinU();
                    float f1 = icon.getMaxU();
                    float f2 = icon.getMinV();
                    float f3 = icon.getMaxV();
                    ItemRenderer.renderItemIn2D(
                            Tessellator.instance, f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 1F / 16F);
                    GL11.glColor3f(1F, 1F, 1F);
                }
                renderPass++;
            } while (renderPass < item.getItem().getRenderPasses(item.getItemDamage()));
            GL11.glPopMatrix();
        }

        GL11.glPopMatrix();

        /*if(overchanter.renderedLightning != null)
        {
        	GL11.glPushMatrix();

        	overchanter.renderedLightning.render(screenX+0.5D, screenY+1D, screenZ+0.5D, partialTicks);

        	GL11.glPopMatrix();
        }*/
    }
}
