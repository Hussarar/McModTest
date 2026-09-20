package mctest.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class CustomBlock extends Block {
    public CustomBlock(Material materialIn) {
        super(materialIn);
        this.blockHardness = 2;
        this.setRegistryName("CustomB");
        this.setTranslationKey("custom_b");
        this.translucent = true;
        this.lightValue = 15;
        this.setDefaultSlipperiness(1f);
    }
}
