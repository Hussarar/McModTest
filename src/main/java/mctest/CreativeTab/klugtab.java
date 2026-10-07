package mctest.CreativeTab;

import mctest.McModTest;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class klugtab extends CreativeTabs {

    public klugtab(String label) {
        super(label);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(McModTest.klugsword);
    }
}
