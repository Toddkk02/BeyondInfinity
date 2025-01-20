package net.todd.beyondinfinity.world.dimension;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.todd.beyondinfinity.BeyondInfinity;
import net.todd.beyondinfinity.world.biome.ModBiomes;

import java.util.OptionalLong;

public class ModDimensions {
    public static final ResourceKey<Level> MOON_KEY_LEVEL = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(BeyondInfinity.MODID, "moon"));

    public static final ResourceKey<LevelStem> MOON_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            new ResourceLocation(BeyondInfinity.MODID, "moon"));

    public static final ResourceKey<DimensionType> MOON_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            new ResourceLocation(BeyondInfinity.MODID, "moon_type"));

    public static void bootstrapType(BootstapContext<DimensionType> context) {
        context.register(MOON_TYPE, new DimensionType(
                OptionalLong.of(12000),    // fixedTime
                false,                      // hasSkylight
                true,                       // hasCeiling
                false,                      // ultraWarm
                true,                       // natural
                1.0,                        // coordinateScale
                true,                       // bedWorks
                false,                      // respawnAnchorWorks
                0,                          // minY
                256,                        // height
                256,                        // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD,
                new ResourceLocation(BeyondInfinity.MODID, "moon"),  // effects
                0.1f,                       // ambientLight
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)));
    }

    public static void bootstrapStem(BootstapContext<LevelStem> context) {
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimTypes = context.lookup(Registries.DIMENSION_TYPE);

        BiomeSource biomeSource = new FixedBiomeSource(biomeRegistry.getOrThrow(ModBiomes.MOON_BIOME));

        LevelStem stem = new LevelStem(
                dimTypes.getOrThrow(MOON_TYPE),
                new MoonChunkGenerator(biomeSource));

        context.register(MOON_KEY, stem);
    }
}