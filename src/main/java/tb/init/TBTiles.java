package tb.init;

import cpw.mods.fml.common.registry.GameRegistry;
import tb.common.tile.TileAdvAlchemicalFurnace;
import tb.common.tile.TileBrazier;
import tb.common.tile.TileCampfire;
import tb.common.tile.TileEntityDeconstructor;
import tb.common.tile.TileNodeLinker;
import tb.common.tile.TileNodeManipulator;
import tb.common.tile.TileOverchanter;
import tb.common.tile.TileRelocator;

public class TBTiles {

    public static void setup() {
        GameRegistry.registerTileEntity(TileAdvAlchemicalFurnace.class, "tb.advancedAlchemicalFurnace");
        GameRegistry.registerTileEntity(TileEntityDeconstructor.class, "tb.entityDeconstructor");
        GameRegistry.registerTileEntity(TileOverchanter.class, "tb.overchanter");
        GameRegistry.registerTileEntity(TileNodeManipulator.class, "tb.manipulator");
        GameRegistry.registerTileEntity(TileRelocator.class, "tb.relocator");
        GameRegistry.registerTileEntity(TileNodeLinker.class, "tb.nodeLinker");
        GameRegistry.registerTileEntity(TileCampfire.class, "tb.campfire");
        GameRegistry.registerTileEntity(TileBrazier.class, "tb.brazier");
    }
}
