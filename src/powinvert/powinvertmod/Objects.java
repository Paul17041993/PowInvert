package powinvert.powinvertmod;

import ic2.api.item.Items;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import powinvert.powinvertmod.energy.UniPower;
import powinvert.powinvertmod.powerinverter.BlockPowerInverter;
import powinvert.powinvertmod.powerinverter.TileEntityPowerInverter;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class Objects {
	
	//tabs
	public static final CreativeTabs mainTab = new PowInvertTab();
	
	//blocks
	public static Block BlockPowerInverter[] = new Block[UniPower.TIERS];
	
	//tiles
	
	
	public static void Init(FMLPreInitializationEvent event) throws Exception
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		
		config.load();
		
		//power config
		UniPower.LoadConfig(config);
		
		//inverter block
		//int powerBlockID = config.getBlock("PowerInverter", 2860).getInt();
		Material powerBlockMat = new Material(null);
		//BlockPowerInverter = new BlockPowerInverter(powerBlockID,powerBlockMat);
		
		int powerBlockID[] = new int[UniPower.TIERS];
		for(int i = 0; i < UniPower.TIERS; ++i)
			powerBlockID[i] = config.getBlock(String.format("PowerInverterTier%d",i), 2857+i).getInt();

		config.save();
		
		for(int i = 0; i < UniPower.TIERS; ++i)
			BlockPowerInverter[i] = new BlockPowerInverter(powerBlockID[i],powerBlockMat,i);
		
	}
	
	public static void Load(FMLInitializationEvent event) throws Exception
	{
		//GameRegistry.registerBlock(BlockPowerInverter, BlockPowerInverter.getUnlocalizedName());
		//GameRegistry.registerTileEntity(TileEntityPowerInverter.class, BlockPowerInverter[0].getUnlocalizedName() + "_tile");
		

		for(int i = 0; i < UniPower.TIERS; ++i)
		{
			GameRegistry.registerBlock(BlockPowerInverter[i], BlockPowerInverter[i].getUnlocalizedName());
			//GameRegistry.registerTileEntity(TileEntityPowerInverter.class, BlockPowerInverter[i].getUnlocalizedName() + "_tile");
		}
		
		GameRegistry.registerTileEntity(TileEntityPowerInverter.class, "powinvert.inverter_tile");
		
		//recipes

		GameRegistry.addRecipe(new ShapedOreRecipe(BlockPowerInverter[0], true, 
				new Object[]{ "WRW", "CIC", "WRW",
				Character.valueOf('W'), "plankWood",
				Character.valueOf('C'), "ingotCopper",
				Character.valueOf('R'), "dustRedstone",
				Character.valueOf('I'), "ingotIron"}));

		GameRegistry.addRecipe(new ShapedOreRecipe(BlockPowerInverter[1], true, 
				new Object[]{ "IRI", "BIB", "IRI",
				Character.valueOf('B'), "ingotBronze",
				Character.valueOf('R'), "dustRedstone",
				Character.valueOf('I'), "ingotIron"}));

		GameRegistry.addRecipe(new ShapedOreRecipe(BlockPowerInverter[2], true, 
				new Object[]{ "IRI", "EIE", "IRI",
				Character.valueOf('E'), "ingotElectrum",
				Character.valueOf('R'), "dustRedstone",
				Character.valueOf('I'), "ingotIron"}));
		
		
		GameRegistry.addRecipe(new ShapedOreRecipe(BlockPowerInverter[3], true, 
				new Object[]{ "ILI", "GRG", "ILI",
				Character.valueOf('I'), "ingotIron",
				Character.valueOf('G'), "ingotGold",
				Character.valueOf('L'), "ingotLead",
				Character.valueOf('R'), "blockRedstone"}));

		GameRegistry.addRecipe(new ItemStack(BlockPowerInverter[4]), 
				"RBR", "BLB", "RBR",
				'B', new ItemStack(BlockPowerInverter[3]),
				'R', new ItemStack(Block.blockRedstone),
				'L', new ItemStack(Block.blockLapis));
	}

}
