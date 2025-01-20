package net.todd.beyondinfinity.world.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.todd.beyondinfinity.BeyondInfinity;

public class ModBiomes {
    public static final ResourceKey<Biome> MOON_BIOME = ResourceKey.create(Registries.BIOME,
            new ResourceLocation(BeyondInfinity.MODID, "moon_biome"));

    public static void boostrap(BootstapContext<Biome> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> worldCarvers = context.lookup(Registries.CONFIGURED_CARVER);

        context.register(MOON_BIOME, createMoonBiome(placedFeatures, worldCarvers));
    }

    private static Biome createMoonBiome(HolderGetter<PlacedFeature> placedFeatures,
                                         HolderGetter<ConfiguredWorldCarver<?>> worldCarvers) {
        // Impostazioni degli spawner di mob (nessuno sulla luna)
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        // Impostazioni di generazione (minimal per la luna)
        BiomeGenerationSettings.Builder biomeBuilder =
                new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers);

        // Creazione del bioma con effetti atmosferici specifici per la luna
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.0f)
                .downfall(0.0f)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x3f76e4)
                        .waterFogColor(0x050533)
                        .fogColor(0xc0c0c0)
                        .skyColor(0x000000)
                        .build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .temperatureAdjustment(Biome.TemperatureModifier.NONE)
                .build();
    }
}