package net.todd.beyondinfinity.world.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraftforge.registries.RegisterEvent;
import net.todd.beyondinfinity.blocks.ModBlocks;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static net.todd.beyondinfinity.BeyondInfinity.MODID;

public class MoonChunkGenerator extends ChunkGenerator {
    private static final int MOON_HEIGHT = 256;
    private static final int SURFACE_DEPTH = 5;

    public static final Codec<MoonChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(BiomeSource.CODEC.fieldOf("biome_source")
                            .forGetter(ChunkGenerator::getBiomeSource))
                    .apply(instance, MoonChunkGenerator::new));

    private final long seed;

    public MoonChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
        this.seed = 12345L;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structures, RandomState randomState, ChunkAccess chunk) {
        // Clear any existing blocks first
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getMinBuildHeight(); y < chunk.getMaxBuildHeight(); y++) {
                    mutable.set(x, y, z);
                    chunk.setBlockState(mutable, Blocks.AIR.defaultBlockState(), false);
                }
            }
        }

        // Generate moon terrain
        long chunkSeed = getChunkSeed(chunk.getPos().x, chunk.getPos().z);
        RandomSource random = RandomSource.create(chunkSeed);

        int chunkX = chunk.getPos().x * 16;
        int chunkZ = chunk.getPos().z * 16;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int actualX = chunkX + x;
                int actualZ = chunkZ + z;

                int height = calculateHeight(actualX, actualZ, random);

                if (shouldGenerateCrater(random)) {
                    int craterRadius = random.nextInt(5) + 3;
                    height -= generateCrater(actualX, actualZ, craterRadius, random);
                }

                for (int y = 0; y <= height; y++) {
                    mutable.set(x, y, z);
                    BlockState state = getBlockForHeight(y, height);
                    chunk.setBlockState(mutable, state, false);
                }
            }
        }
    }

    private long getChunkSeed(int chunkX, int chunkZ) {
        return seed + (chunkX * 341873128712L + chunkZ * 132897987541L);
    }

    private BlockState getBlockForHeight(int currentY, int maxHeight) {
        if (currentY >= maxHeight - SURFACE_DEPTH) {
            return ModBlocks.MOON_SURFACE.get().defaultBlockState();
        }
        return ModBlocks.MOON_ROCK.get().defaultBlockState();
    }

    private boolean shouldGenerateCrater(RandomSource random) {
        return random.nextFloat() < 0.02f;
    }

    private int calculateHeight(int x, int z, RandomSource random) {
        double baseHeight = 64;
        double frequency1 = 0.015;
        double frequency2 = 0.04;

        double height = baseHeight;
        height += Math.sin(x * frequency1) * Math.cos(z * frequency1) * 12;
        height += Math.sin(x * frequency2) * Math.cos(z * frequency2) * 6;

        double ridgeNoise = Math.abs(Math.sin(x * 0.02) + Math.sin(z * 0.02));
        height += ridgeNoise * 6;

        height += random.nextDouble() * 2 - 1;

        return (int) Math.max(5, Math.min(MOON_HEIGHT - 10, height));
    }

    private int generateCrater(int centerX, int centerZ, int radius, RandomSource random) {
        int depthAtCenter = radius;
        double distanceFromCenter = Math.sqrt(centerX * centerX + centerZ * centerZ);

        if (distanceFromCenter <= radius) {
            double depthFactor = 1 - (distanceFromCenter / radius);
            return (int)(depthAtCenter * depthFactor * (0.8 + random.nextDouble() * 0.4));
        }
        return 0;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender,
                                                        RandomState random, StructureManager structures,
                                                        ChunkAccess chunk) {
        return CompletableFuture.supplyAsync(() -> {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = chunk.getMinBuildHeight(); y < chunk.getMaxBuildHeight(); y++) {
                        pos.set(x, y, z);
                        chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                    }
                }
            }
            return chunk;
        }, executor);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types types, LevelHeightAccessor level, RandomState random) {
        long pointSeed = seed + (x * 341873128712L + z * 132897987541L);
        return calculateHeight(x, z, RandomSource.create(pointSeed));
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
        long pointSeed = seed + (x * 341873128712L + z * 132897987541L);
        BlockState[] states = new BlockState[height.getHeight()];
        int terrainHeight = calculateHeight(x, z, RandomSource.create(pointSeed));

        for (int y = 0; y < height.getHeight(); y++) {
            if (y <= terrainHeight) {
                states[y] = getBlockForHeight(y, terrainHeight);
            } else {
                states[y] = Blocks.AIR.defaultBlockState();
            }
        }

        return new NoiseColumn(height.getMinBuildHeight(), states);
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random,
                             BiomeManager biomeManager, StructureManager structures,
                             ChunkAccess chunk, GenerationStep.Carving step) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
    }

    @Override
    public int getGenDepth() {
        return MOON_HEIGHT;
    }

    @Override
    public int getSeaLevel() {
        return -1;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("Moon Generator");
    }

}