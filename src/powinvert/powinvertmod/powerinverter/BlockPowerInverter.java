package powinvert.powinvertmod.powerinverter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import powinvert.powinvertmod.Objects;
import powinvert.powinvertmod.energy.UniPower;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ForgeDirection;

public class BlockPowerInverter extends BlockContainer
{
	private int tier = 0;
	
	public BlockPowerInverter(int id, Material material, int tier)
	{
		super(id, material);
		
		this.tier = tier;
		
		setHardness(2.0F);
		setUnlocalizedName(String.format("powinvert.inverter_tier%d",this.tier));
		
		setCreativeTab(Objects.mainTab);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		try
		{
			return new TileEntityPowerInverter();//this.tier);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void onNeighborTileChange(World world,int x,int y,int z,int tileX,int tileY,int tileZ)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(! (tile instanceof TileEntityPowerInverter) ) return;
		((TileEntityPowerInverter) tile).updateNeighbourTile(tileX,tileY,tileZ);
		//((TileEntityPowerInverter) tile).setDirtyHandles();
		
		super.onNeighborTileChange(world,x,y,z,tileX,tileY,tileZ);
	}
	
	@Override
	public void onNeighborBlockChange(World world,int x,int y,int z,int blockID)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(! (tile instanceof TileEntityPowerInverter) ) return;
		((TileEntityPowerInverter) tile).setDirtyHandles();
		
		super.onNeighborBlockChange(world,x,y,z,blockID);
	}
	

    @SideOnly(Side.CLIENT)
    protected Icon sendFIcon;
    @SideOnly(Side.CLIENT)
    protected Icon reciFIcon;
	
    @SideOnly(Side.CLIENT)
    @Override
    public Icon getIcon(int side, int meta)
    {
    	switch(side)
    	{
    	case 3:
    		return sendFIcon;
    	case 2:
    		return reciFIcon;
    	default:
    		return this.blockIcon;
    	}
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Icon getBlockTexture(IBlockAccess iBlockAccess, int x, int y, int z, int side)
    {
    	TileEntity tile = iBlockAccess.getBlockTileEntity(x, y, z);
		if(! (tile instanceof TileEntityPowerInverter) ) return this.blockIcon;
		if( ((TileEntityPowerInverter) tile).CanSendOnSide(ForgeDirection.VALID_DIRECTIONS[side]) ) return sendFIcon;
		if( ((TileEntityPowerInverter) tile).CanReceiveOnSide(ForgeDirection.VALID_DIRECTIONS[side]) ) return reciFIcon;
		return this.blockIcon;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister par1IconRegister)
    {
		String name = this.getUnlocalizedName();
		int ind = name.lastIndexOf('.') +1;
		name = "powinvert:" + name.substring(ind);
        this.blockIcon = par1IconRegister.registerIcon(name);
        this.sendFIcon = par1IconRegister.registerIcon(name + "_s");
        this.reciFIcon = par1IconRegister.registerIcon(name + "_r");
    }
    

    @Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack)
	{
    	int side = 0;
    	
		int dirV = MathHelper.floor_double((double)((entity.rotationPitch * 4F) / 360F) + 0.5D) & 3;
		
		switch(dirV)
		{
		case 1:
			side = 0;
			break;
		case 3:
			side = 1;
			break;

		default:
			
			int dirH = MathHelper.floor_double((double)((entity.rotationYaw * 4F) / 360F) + 0.5D) & 3;
			
			switch(dirH)
			{
			case 0:
				side = 3;
				break;
			case 1:
				side = 4;
				break;
			case 2:
				side = 2;
				break;
			case 3:
				side = 5;
				break;
				default:
					break;
			}
			
			break;
		}
		
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		
		if(! (tile instanceof TileEntityPowerInverter) ) return;
		
		((TileEntityPowerInverter) tile).SetTier(this.tier);
		((TileEntityPowerInverter) tile).SetSidesFromOrientation(side);
		
		
	}
	
}
