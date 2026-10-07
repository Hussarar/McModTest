package mctest.WorldGen;

import mctest.McModTest;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class KlugWorldGen implements IWorldGenerator {

    private final WorldGenMinable customBlockGen = new WorldGenMinable(
            McModTest.Custom_b.getDefaultState(),
            4,
            state -> state.getBlock() == Blocks.GRASS
    );

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() == 0){
            for(int i = 0; i < 15; i++){
                int PosX = chunkX * 16 + random.nextInt(16);
                int PosZ = chunkZ * 16 + random.nextInt(16);
                int PosY = 60 + random.nextInt(30);

                BlockPos position = new BlockPos(PosX,PosY,PosZ);
                customBlockGen.generate(world,random,position);
            }
        }
    }
}
