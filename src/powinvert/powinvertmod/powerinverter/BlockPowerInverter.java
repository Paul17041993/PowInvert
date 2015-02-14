package powinvert.powinvertmod.powerinverter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import powinvert.powinvertmod.Objects;
import powinvert.powinvertmod.energy.UniPower;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockPowerInverter extends BlockContainer
{
	private int tier = 0;
	
	public BlockPowerInverter(Material material, int tier)
	{
		super(material);
		
		this.tier = tier;
		
		setHardness(2.0F);
        setBlockName(String.format("powinvert.inverter_tier%d",this.tier));
		
		setCreativeTab(Objects.mainTab);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int p_149915_2_)
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
	public void onNeighborChange(IBlockAccess iBlockAccess,int x,int y,int z,int tileX,int tileY,int tileZ)
	{
		TileEntity tile = iBlockAccess.getTileEntity(x, y, z);
        if(! (tile instanceof TileEntityPowerInverter) ) return;
		((TileEntityPowerInverter) tile).updateNeighbourTile(tileX,tileY,tileZ);
		//((TileEntityPowerInverter) tile).setDirtyHandles();
		
		super.onNeighborChange(iBlockAccess, x, y, z, tileX, tileY, tileZ);
	}
	
	@Override
	public void onNeighborBlockChange(World world,int x,int y,int z,Block block)
	{
		TileEntity tile = world.getTileEntity(x, y, z);
        if(! (tile instanceof TileEntityPowerInverter) ) return;
		((TileEntityPowerInverter) tile).setDirtyHandles();
		
		super.onNeighborBlockChange(world,x,y,z,block);
	}
	

    @SideOnly(Side.CLIENT)
    protected IIcon sendFIcon;
    @SideOnly(Side.CLIENT)
    protected IIcon reciFIcon;
	
    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta)
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
    public IIcon getIcon(IBlockAccess iBlockAccess, int x, int y, int z, int side)
    {
    	TileEntity tile = iBlockAccess.getTileEntity(x, y, z);
        if(! (tile instanceof TileEntityPowerInverter) ) return this.blockIcon;
		if( ((TileEntityPowerInverter) tile).CanSendOnSide(ForgeDirection.VALID_DIRECTIONS[side]) ) return sendFIcon;
		if( ((TileEntityPowerInverter) tile).CanReceiveOnSide(ForgeDirection.VALID_DIRECTIONS[side]) ) return reciFIcon;
		return this.blockIcon;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister par1IconRegister)
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
		
		TileEntity tile = world.getTileEntity(x, y, z);

        if(! (tile instanceof TileEntityPowerInverter) ) return;
		
		((TileEntityPowerInverter) tile).SetTier(this.tier);
		((TileEntityPowerInverter) tile).SetSidesFromOrientation(side);
		
		
	}
}
