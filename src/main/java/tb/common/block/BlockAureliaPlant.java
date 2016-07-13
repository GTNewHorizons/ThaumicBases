package tb.common.block;

import java.util.Random;

import tb.init.TBBlocks;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockAureliaPlant extends Block{

	public BlockAureliaPlant() 
	{
		super(Material.plants);
		this.setTickRandomly(true);
		this.setBlockBounds(0.25F, 0, 0.25F, 0.75F, 0.85F, 0.75F);
		this.setLightLevel(0.5F);
	}
	
	public static IIcon[] icons = new IIcon[2];
	
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg)
    {
    	icons[0] = reg.registerIcon(textureName+"closed");
    	icons[1] = reg.registerIcon(textureName+"open");
    }
    
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
    	return icons[meta];
    }
    
    protected boolean canPlaceBlockOn(Block b)
    {
    	return b != null && (b == Blocks.grass || b == Blocks.dirt || b instanceof BlockGrass || b instanceof BlockDirt);
    }
	
	public void updateTick(World w, int x, int y, int z, Random rnd) 
	{
		checkForMoonlight(w,x,y,z);
		if(w.getBlockMetadata(x, y, z) == 1)
		{
			int rndX = x + rnd.nextInt(5) - rnd.nextInt(5);
			int rndZ = z + rnd.nextInt(5) - rnd.nextInt(5);
			//int rndY = findSutableGround(w,rndX,y+2,rndZ)+1;
			//if(rndY > 2)
				//w.setBlock(rndX, rndY, rndZ, TBBlocks.aureliaPetal,0,3);
			
        	ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[2+w.rand.nextInt(4)];
        	//int newX = x+dir.offsetX;
        	//int newZ = z+dir.offsetZ;
        	int newY = findSutableY(w,rndX,y,rndZ);
        	if(canPlaceBlockOn(w.getBlock(rndX, newY-1, rndZ)) && w.isAirBlock(rndX, newY, rndZ)) //fix for the Sweeds destroying blocks. Totally not copy-pasted code...
        		w.setBlock(rndX, newY, rndZ, TBBlocks.aureliaPetal, 0, 3);
		}
	}
	
	public int findSutableGround(World w, int x, int y, int z)
	{
		while(w.isAirBlock(x, y, z) && y > 0 && !w.isSideSolid(x, y, z, ForgeDirection.UP))
			--y;
		
		return y;
	}
	
    public int findSutableY(World w, int x, int y, int z)
    {
    	int bY = y;
    	y += 1;
    	while(!canPlaceBlockOn(w.getBlock(x, y, z)) && y > bY - 2)
    		--y;
    	
    	return y+1;
    }
	
    public int tickRate(World w)
    {
    	return 10;
    }
	
	public void onNeighborBlockChange(World w, int x, int y, int z, Block updated) 
	{
		if(!canGrowOn(w,x,y-1,z))
			w.setBlockToAir(x, y, z);
		else
			checkForMoonlight(w,x,y,z);
		
	}
	
    public int damageDropped(int meta)
    {
    	return 0;
    }
    
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_)
    {
        return null;
    }

    public boolean isOpaqueCube()
    {
        return false;
    }

    public boolean renderAsNormalBlock()
    {
        return false;
    }

    public int getRenderType()
    {
        return 1;
    }
	
	public void checkForMoonlight(World w, int x, int y, int z)
	{
		boolean underSky = w.canBlockSeeTheSky(x, y+1, z);
		boolean night = !w.isDaytime();
		boolean isFullMoon = w.provider.getMoonPhase(w.getWorldTime()) == 0;
		boolean isOpen = w.getBlockMetadata(x, y, z) == 1;
		
		if(underSky && night && isFullMoon)
		{
			if(!isOpen)
			{
				w.setBlockMetadataWithNotify(x, y, z, 1, 3);
				w.markBlockRangeForRenderUpdate(x-1, y-1, z-1, x+1, y+1, z+1);
			}
		}else
		{
			if(isOpen)
			{
				w.setBlockMetadataWithNotify(x, y, z, 0, 3);
				w.markBlockRangeForRenderUpdate(x-1, y-1, z-1, x+1, y+1, z+1);
			}
		}
	}

	public static boolean canGrowOn(World w, int x, int y, int z)
	{
		return !w.isAirBlock(x, y, z) && (w.getBlock(x, y, z).isReplaceableOreGen(w, x, y, z, Blocks.grass) || w.getBlock(x, y, z).isReplaceableOreGen(w, x, y, z, Blocks.dirt));
	}
}
