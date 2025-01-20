package net.todd.beyondinfinity.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.todd.beyondinfinity.blocks.ModBlocks;

public class CrystalFeature extends Feature<NoneFeatureConfiguration> {
    public CrystalFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();

        // Generiamo una sfera di cristalli
        int radius = 2 + random.nextInt(2); // Raggio tra 2 e 3 blocchi

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Verifica se il punto è all'interno della sfera
                    if (x * x + y * y + z * z <= radius * radius) {
                        BlockPos blockpos = pos.offset(x, y, z);

                        // Casualmente decidiamo se mettere un cluster o un blocco pieno
                        if (random.nextFloat() < 0.3) { // 30% di probabilità per i cluster
                            if (level.isEmptyBlock(blockpos)) {
                                level.setBlock(blockpos, ModBlocks.ENERGY_CRYSTAL_BLOCK.get().defaultBlockState(), 2);
                            }
                        } else {
                            if (level.isEmptyBlock(blockpos)) {
                                level.setBlock(blockpos, ModBlocks.ENERGY_CRYSTAL_BLOCK.get().defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }
        }

        return true;
    }
}