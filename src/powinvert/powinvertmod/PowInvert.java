package powinvert.powinvertmod;


import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid="PowInvert", name="PowInvert", version="0.0.2", acceptableRemoteVersions="0.0.2")
public class PowInvert {
	
	@Instance(value = "PowInvert")
	public static PowInvert instance;
	
	@SidedProxy(clientSide="powinvert.powinvertmod.client.ClientProxy", serverSide="powinvert.powinvertmod.CommonProxy")
	public static CommonProxy proxy;
	
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception
	{
		Objects.Init(event);
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event) throws Exception
	{
		proxy.registerRenderers();
		Objects.Load(event);
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) throws Exception
	{
	}
}
