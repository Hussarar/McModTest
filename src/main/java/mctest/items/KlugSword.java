package mctest.items;

import mctest.McModTest;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;

public class KlugSword extends ItemSword {
    public KlugSword() {
        super(McModTest.KLUG_MATERIAL);
        this.setRegistryName("klugsword");
        this.setTranslationKey("klugsword");
        this.maxStackSize = 16;
        this.bFull3D = true;
        this.setCreativeTab(McModTest.KLUG_TAB);

    }
}
