package mctest.structures;

import mctest.McModTest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class KlugShrine implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {

        // 1. Rarity check (1 in 50 chance per chunk)
        if (random.nextInt(50) != 0) {
            return;
        }

        // 2. Calculate a random X and Z inside this specific chunk
        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);

        // 3. Find the ground level at that random X and Z
        BlockPos pos = new BlockPos(x, 0, z);
        int groundY = world.getTopSolidOrLiquidBlock(pos).getY();
        BlockPos anchor = new BlockPos(x, groundY, z);

        // 4. Define the block we want to use
        IBlockState shrineBlock = McModTest.Custom_b.getDefaultState();

        // 5. Build the Floor (A 3x3 square)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                // Place the floor block
                world.setBlockState(anchor.add(dx, 0, dz), shrineBlock);

                // 6. Build the Walls (Only place blocks on the edges)
                if (Math.abs(dx) == 1 || Math.abs(dz) == 1) {
                    world.setBlockState(anchor.add(dx, 1, dz), shrineBlock);
                    world.setBlockState(anchor.add(dx, 2, dz), shrineBlock);
                }
            }
        }

        // 7. Build the Roof
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.setBlockState(anchor.add(dx, 3, dz), shrineBlock);
            }
        }
    }
}
