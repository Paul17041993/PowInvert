package powinvert.powinvertmod;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class PowInvertTab extends CreativeTabs
{

	public PowInvertTab() {
		super("powinvert");
	}

    @Override
    public Item getTabIconItem() {
        return new ItemStack(Objects.BlockPowerInverter[0]).getItem();
    }
}
