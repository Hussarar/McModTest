package mctest.items;

import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;

public class KlugSword extends ItemSword {
    public KlugSword() {
        super(ToolMaterial.DIAMOND);
        this.setRegistryName("klugsword");
        this.setTranslationKey("klugsword");
        this.maxStackSize = 16;
        this.bFull3D = true;

    }
}
