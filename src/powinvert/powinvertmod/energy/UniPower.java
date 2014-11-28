package powinvert.powinvertmod.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.info.Info;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.FMLCommonHandler;

// a universal power interface for various uses, made by Paul17041993 :D
public class UniPower extends TileEntity implements IEnergyHandler, IEnergySink, IEnergySource
{
	public static enum SideType{
		NONE(0),
		RECEIVE(1),
		SEND(2);
		public final byte value;
		private SideType(int val)
		{ value = (byte) val; }
		
		public static final SideType Sides[] = {NONE,RECEIVE,SEND};
		
		public static SideType[] FromBytes(byte bytes[], int size)
		{
			SideType ret[] = new SideType[size];
			for(int i = 0; i < size; ++i)
				ret[i] = Sides[bytes[i]];
			return ret;
		}
		
		public static byte[] ToBytes(SideType sides[], int size)
		{
			byte ret[] = new byte[size];
			for(int i = 0; i < size; ++i)
				ret[i] = sides[i].value;
			return ret;
		}
	}
	
	//ratios (fyi zero values are technically dangerous)
	private static float uniRatioRF = 4;
	private static float uniRatioEU = 1;

	public static final int TIERS = 5;
	private static int peakTroughputRF[] = {500,1000,2500,10000,40000};
	private static int peakTroughputEU[] = {32,128,512,2048,8192};
	
	private static int peakCapacity[] = {500,1000,2500,10000,40000};
	
	public static final float GetPeakThroughputRF(int t) { if(t >= 0 && t < TIERS) return (float)peakTroughputRF[t]; else return 0.f; }
	public static final float GetPeakThroughputEU(int t) { if(t >= 0 && t < TIERS) return (float)peakTroughputEU[t]; else return 0.f; }

	public static final float GetPeakCapacity(int t) { if(t >= 0 && t < TIERS) return (float)peakCapacity[t]; else return 0.f; }
	
	
	private float currentUniPower = 0;
	//private float maxUniPower = 0;
	//current can be > than the max, however when so will not accept more
	protected int tier = 0;
	public void SetTier(int t) { this.tier = t; }
	
	public static final void LoadConfig(Configuration config) throws Exception
	{
		uniRatioRF = (float)(config.get("Power Inversion", "ratioRF", uniRatioRF).getDouble((double)uniRatioRF));
		if( uniRatioRF <= 0.0 ) throw new Exception("ratioRF must be greater than zero!");
		
		uniRatioEU = (float)(config.get("Power Inversion", "ratioEU", uniRatioEU).getDouble((double)uniRatioEU));
		if( uniRatioEU <= 0.0 ) throw new Exception("ratioEU must be greater than zero!");
		
		//peakTroughputRF = (float)(config.get("Power Inversion", "peakRF", peakTroughputRF).getDouble((double)peakTroughputRF));
		//if( peakTroughputRF <= 0.0 ) throw new Exception("peakRF must be greater than zero!");
		
		//peakTroughputEU = (float)(config.get("Power Inversion", "peakEU", peakTroughputEU).getDouble((double)peakTroughputEU));
		//if( peakTroughputEU <= 0.0 ) throw new Exception("peakEU must be greater than zero!");
		
		peakTroughputRF = config.get("Power Inversion", "peakRF", peakTroughputRF).getIntList();
		if( peakTroughputRF.length != TIERS ) throw new Exception(String.format("peakRF must have %d tiers!",TIERS));
		for(int i = 0; i < peakTroughputRF.length; ++i)
			if( peakTroughputRF[i] <= 0 ) throw new Exception("peakRF must be greater than zero!");

		peakTroughputEU = config.get("Power Inversion", "peakEU", peakTroughputEU).getIntList();
		if( peakTroughputEU.length != TIERS ) throw new Exception(String.format("peakEU must have %d tiers!",TIERS));
		for(int i = 0; i < peakTroughputEU.length; ++i)
			if( peakTroughputEU[i] <= 0 ) throw new Exception("peakEU must be greater than zero!");
		

		peakCapacity = config.get("Power Inversion", "peakCapacity", peakCapacity).getIntList();
		if( peakCapacity.length != TIERS ) throw new Exception(String.format("peakCapacity must have %d tiers!",TIERS));
		for(int i = 0; i < peakCapacity.length; ++i)
			if( peakCapacity[i] <= 0 ) throw new Exception("peakCapacity must be greater than zero!");
	}
	
	public UniPower()//int tier) throws Exception
	{
		super();
		//maxUniPower = intendedCapacity;
		//if(tier < 0 || tier >= TIERS) throw new Exception("tier in UniPower init out of range!");
		
		//this.tier = tier;

		initialised = false;
		
		for(int i = 0; i < 5; ++i)
			neighbourHandlers[i] = null;
		
		dirtyHandles = true;
	}
	
	//unipow calls, these are the ones needed to be overwritten depending on purpose
	
	//sides
	protected boolean CanReceiveOnSide(ForgeDirection side)
	{
		return false;
	}
	
	protected boolean CanSendOnSide(ForgeDirection side)
	{
		return false;
	}
	
	//in-out rates per-energy, in their actual values (not uni)
	protected int InputRateRF()
	{
		return 0;
	}
	
	protected int OutputRateRF()
	{
		return 0;
	}
	
	protected double InputRateEU()
	{
		return 0;
	}
	
	protected double OutputRateEU()
	{
		return 0;
	}
	
	/**
	 * energy transfer event, for temperature etc,
	 * be sure to call super unless you don't want the store to change (creative)
	 * @param value positive or negative respectively
	 */
	protected void UniPowerInversion(float value)
	{
		currentUniPower += value;
	}
	
	//main transmit and receive, false by default, overwrite for needed purpose
	protected boolean CanAcceptEnergy()
	{
		return false;
	}
	
	protected boolean CanTransmitEnergy()
	{
		return false;
	}
	
	//end unipow, nothing after this should [need to] be overwritten
	// however ensure that if you override the updateEntity, invalidate or onChunkUnload 
	// tileentity functions, that you call super too, otherwise enet interaction will break
	
	//cofh power (RF)

	@Override
	public final int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if( CanAcceptEnergy() && CanReceiveOnSide(from) )
		{
			//float headroom = maxUniPower - currentUniPower;
			float headroom = this.GetPeakCapacity(tier) - currentUniPower;

			int energyReceived = Math.min( (int)(headroom*uniRatioRF), Math.min(InputRateRF(), maxReceive));

			if(!simulate)
			{
				UniPowerInversion( (float)(energyReceived)/uniRatioRF );
			}
			return energyReceived;
		}
		else
			return 0;
	}

	@Override
	public final int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		if( CanTransmitEnergy() && CanSendOnSide(from) )
		{
			int energyExtracted = Math.min( (int)(currentUniPower*uniRatioRF), Math.min(OutputRateRF(), maxExtract));
			
			if(!simulate)
			{
				UniPowerInversion( -(float)(energyExtracted)/uniRatioRF );
			}
			return energyExtracted;
		}
		else
			return 0;
	}

	@Override
	public final boolean canInterface(ForgeDirection from)
	{
		return CanReceiveOnSide(from) || CanSendOnSide(from);
	}

	@Override
	public final int getEnergyStored(ForgeDirection from)
	{
		return (int)( currentUniPower * uniRatioRF );
	}

	@Override
	public final int getMaxEnergyStored(ForgeDirection from)
	{
		//return (int)( maxUniPower * uniRatioRF );
		return (int)( this.GetPeakCapacity(tier) * uniRatioRF );
	}

	
	//IC2 power (EU)
	// val = 2*4^tier
	// tier = log(val/2)/log(4)
	
	// sink
	@Override
	public final double demandedEnergyUnits()
	{
		//EnergyNet.instance.getPowerFromTier(tier);
		//float headroom = ( maxUniPower - currentUniPower ) * uniRatioEU;
		float headroom = ( this.GetPeakCapacity(tier) - currentUniPower ) * uniRatioEU;
		
		//work out the packet size that the headroom fits to
		//int headTier = (int)(Math.log(headroom/2.f)/Math.log(4.f));
		//float headSize = (float)(2d*Math.pow(4, headTier));
		
		//return Math.min((double)headSize, InputRateEU());
		
		if( headroom >= 0 )
			return InputRateEU();
		else
			return 0;
	}

	@Override
	public final double injectEnergyUnits(ForgeDirection directionFrom, double amount)
	{
		UniPowerInversion( (float)( amount)/uniRatioEU );
		return 0;
	}

	@Override
	public final int getMaxSafeInput()
	{
		return Integer.MAX_VALUE; //ie can't be blown up nomatter what
	}

	@Override
	public final boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return CanAcceptEnergy() && CanReceiveOnSide(direction);
	}
	
	// source
	@Override
	public final boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction)
	{
		return CanTransmitEnergy() && CanSendOnSide(direction);
	}

	@Override
	public final double getOfferedEnergy()
	{
		float curr = currentUniPower*uniRatioEU;
		
		if( (double)(curr) >= OutputRateEU() )
			return OutputRateEU();
		else
			return 0;
	}

	@Override
	public final void drawEnergy(double amount)
	{
		UniPowerInversion( -(float)( amount)/uniRatioEU );
	}
	
	//enet interaction
	private boolean initialised = false;
	//RF push
	private TileEntity neighbourHandlers[] = {null,null,null,null,null,null};
	private boolean dirtyHandles = true;
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			//init
			if(!initialised)
			{
				if (Info.isIc2Available())
					MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
				
				forceScanNeighbours();
				
				initialised = true;
			}
		
			if(dirtyHandles)
			{
				forceScanNeighbours();
				dirtyHandles = false;
			}
			
			//push RF
			if(CanTransmitEnergy())
			for ( ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS )
			{
				if(!CanSendOnSide(dir))
					continue;
				//System.out.println("pass cansend");
				
				int side = dir.ordinal();
				
				//not-so-effecient workaround so things work correctly
				// im not sure why rebuilding the cache on world load doesnt work...
				neighbourHandlers[side] = worldObj.getBlockTileEntity( xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ );
				
				
				if ( neighbourHandlers[side] == null )
					continue;
				//System.out.println("pass null");
				
				try
				{
					if( !(neighbourHandlers[side] instanceof IEnergyHandler) )
						continue;
					//System.out.println("pass instance");
					
					IEnergyHandler handle = (IEnergyHandler) neighbourHandlers[side];
					
					ForgeDirection from = dir.getOpposite();
					
					if( !handle.canInterface(from) )
						continue;
					//System.out.println("pass caninterface");
					
					int energy = Math.min( (int)(currentUniPower*uniRatioRF), OutputRateRF() );
				
					if( handle.receiveEnergy(from, energy, true) > 0 )
						UniPowerInversion( -(float)( handle.receiveEnergy(from, energy, false) )/uniRatioRF );
				}
				catch(Exception exce)
				{
					System.out.println("Someones a bad coder :O");
					System.out.println(neighbourHandlers[side] + 
							" " + neighbourHandlers[side].xCoord + 
							" " + neighbourHandlers[side].yCoord + 
							" " + neighbourHandlers[side].zCoord);
					exce.printStackTrace();
					
					setDirtyHandles();
				}
				//System.out.println(currentUniPower);
				
			}
		}
		
		//System.out.print(this.tier);
	}

	
	@Override
	public void invalidate()
	{
		super.invalidate();
		onChunkUnload();
	}
	
	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		
		if (initialised && Info.isIc2Available())
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		
		initialised = false;
	}

	
	@Override
	public void writeToNBT(NBTTagCompound par1)
	{
		par1.setInteger("tier", this.tier);
		par1.setFloat("currUniPow", currentUniPower);
		super.writeToNBT(par1);
	}

	@Override
	public void readFromNBT(NBTTagCompound par1)
	{
		this.tier = par1.getInteger("tier");
		currentUniPower = par1.getFloat("currUniPow");
		super.readFromNBT(par1);
	}

	
	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt)
    {
		readFromNBT(pkt.data);
    }

	@Override
	public Packet getDescriptionPacket()
	{
	    NBTTagCompound tagCompound = new NBTTagCompound();
	    writeToNBT(tagCompound);
	    return new Packet132TileEntityData(xCoord, yCoord, zCoord, 0, tagCompound);
	}
	
	
	private void updateNeighbour(TileEntity tile, int side)
	{
		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
			return;
		
		//System.out.println(side);
		
		if( side >= 0 && side <= 5 )
		{
			neighbourHandlers[side] = null;
			neighbourHandlers[side] = tile;
		}

		//System.out.println(neighbourHandlers[side]);
	}
	
	public void updateNeighbourTile(int x, int y, int z)
	{
		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
			return;
		
		TileEntity tile = worldObj.getBlockTileEntity(x,y,z);
		
		int side = 6;
		if (x < xCoord)
			side = 4;
		else if (x > xCoord)
			side = 5;
		else if (y < yCoord)
			side = 0;
		else if (y > yCoord)
			side = 1;
		else if (z < zCoord)
			side = 2;
		else if (z > zCoord)
			side = 3;
		
		updateNeighbour(tile, side);
	}
	
	private void forceScanNeighbours()
	{
		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
			return;
		
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			//TileEntity tile = worldObj.getBlockTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
			//updateNeighbour(tile,dir.ordinal());
			updateNeighbourTile( xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ );
		}
	}
	
	public void setDirtyHandles() { dirtyHandles = true; }
	
	//end
}
