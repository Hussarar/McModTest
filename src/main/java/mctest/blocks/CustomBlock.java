package mctest.blocks;

import mctest.McModTest;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class CustomBlock extends Block {
    public CustomBlock(Material materialIn) {
        super(materialIn);
        this.blockHardness = 2;
        this.setRegistryName("CustomB");
        this.setTranslationKey("custom_b");
        //this.translucent = true;
        //this.lightValue = 15;
        //this.setDefaultSlipperiness(10f);
        this.setCreativeTab(McModTest.KLUG_TAB);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune){
        drops.add(new ItemStack(McModTest.klugsword));
    }
}
