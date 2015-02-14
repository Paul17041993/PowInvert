package powinvert.powinvertmod.powerinverter;

import powinvert.powinvertmod.energy.UniPower;
import powinvert.powinvertmod.energy.UniPower.SideType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
//import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityPowerInverter extends UniPower
{
	public TileEntityPowerInverter()//int tier) throws Exception
	{
		//super(tier);
		super();
	}
	
	// 0 = none, 1 == recive, 2 == send
	protected SideType sides[] = {SideType.NONE,SideType.NONE,SideType.NONE,SideType.NONE,SideType.NONE,SideType.NONE};
	
	//sides
	@Override
	protected boolean CanReceiveOnSide(ForgeDirection side)
	{
		//if(side != ForgeDirection.UP)
		if(sides[side.ordinal()] == SideType.RECEIVE)
			return true;
		else
			return false;
	}

	@Override
	protected boolean CanSendOnSide(ForgeDirection side)
	{
		//if(side == ForgeDirection.UP)
		if(sides[side.ordinal()] == SideType.SEND)
			return true;
		else
			return false;
	}
	
	
	//in-out rates per-energy, in their actual values (not uni)
	@Override
	protected int InputRateRF()
	{
		//return 10000;
		return (int)this.GetPeakThroughputRF(this.tier);
	}

	@Override
	protected int OutputRateRF()
	{
		//return 10000;
		return (int)this.GetPeakThroughputRF(this.tier);
	}

	@Override
	protected double InputRateEU()
	{
		//return 2048;
		return (int)this.GetPeakThroughputEU(this.tier);
	}

	@Override
	protected double OutputRateEU()
	{
		//return 2048;
		return (int)this.GetPeakThroughputEU(this.tier);
	}
	

	@Override
	protected void UniPowerInversion(float value)
	{
		super.UniPowerInversion(value);
	}

	
	@Override
	protected boolean CanAcceptEnergy()
	{
		return true;
	}

	@Override
	protected boolean CanTransmitEnergy()
	{
		return true;
	}

	
	@Override
	public void writeToNBT(NBTTagCompound par1)
	{
		super.writeToNBT(par1);
		par1.setByteArray("sides", SideType.ToBytes(sides, 6) );

		//this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		//System.out.println("write");
	}

	@Override
	public void readFromNBT(NBTTagCompound par1)
	{
		super.readFromNBT(par1);
		sides = null;
		sides = SideType.FromBytes( par1.getByteArray("sides"), 6 ).clone();
		//System.out.println("read");
	}
	
	
	public void SetSidesFromOrientation(int orientation)
	{
		sides[orientation] = SideType.RECEIVE;

		//we want the tier 0 one to accept from all sides
		if(this.tier == 0)
			for(int i = 0; i < 6; ++i)
					sides[i] = SideType.RECEIVE;
		
		sides[ForgeDirection.OPPOSITES[orientation]] = SideType.SEND;		
	}
	
}
