package net.todd.beyondinfinity.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.todd.beyondinfinity.BeyondInfinity;

import java.util.List;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> TIN_ORE_PLACED_KEY = createKey("tin_ore_placed");
    public static final ResourceKey<PlacedFeature> ALUMINIUM_ORE_PLACED_KEY = createKey("aluminium_ore_placed");
    public static final ResourceKey<PlacedFeature> LITHIUM_ORE_PLACED_KEY = createKey("lithium_ore_placed");
    public static final ResourceKey<PlacedFeature> TITANIUM_ORE_PLACED_KEY = createKey("titanium_ore_placed");
    public static final ResourceKey<PlacedFeature> RUBBER_TREE_PLACED_KEY = createKey("rubber_tree_placed");
    public static final ResourceKey<PlacedFeature> CRYSTAL_GEODE_PLACED_KEY = createKey("crystal_geode_placed");

    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        // Minerali con frequenza simile a ferro/rame (12-20 vein per chunk)
        register(context, TIN_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.OVERWORLD_TIN_ORE_KEY),
                ModOrePlacement.commonOrePlacement(16,
                        HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(64))));

        register(context, ALUMINIUM_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.OVERWORLD_ALUMINIUM_ORE_KEY),
                ModOrePlacement.commonOrePlacement(16,
                        HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(64))));

        register(context, LITHIUM_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.OVERWORLD_LITHIUM_ORE_KEY),
                ModOrePlacement.commonOrePlacement(20, // Aumenta il numero di vene per chunk
                        HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(128)))); // Aumenta il range di altezza

        register(context, TITANIUM_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.OVERWORLD_TITANIUM_ORE_KEY),
                ModOrePlacement.commonOrePlacement(12,
                        HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(32))));

        // Alberi della gomma con placement migliorato
        register(context, RUBBER_TREE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.RUBBER_TREE_KEY),
                List.of(
                        PlacementUtils.countExtra(1, 0.1f, 1),
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                        BiomeFilter.biome()
                ));

        // Placement dei geodi di cristallo (simile all'ametista)
        register(context, CRYSTAL_GEODE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.CRYSTAL_GEODE_KEY),
                List.of(
                        RarityFilter.onAverageOnceEvery(24), // Simile alla rarità dei geodi di ametista
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(
                                VerticalAnchor.aboveBottom(-64),
                                VerticalAnchor.absolute(0)
                        ),
                        BiomeFilter.biome()
                ));
    }

    private static ResourceKey<PlacedFeature> createKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(BeyondInfinity.MODID, name));
    }

    private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key,
                                 Holder<ConfiguredFeature<?, ?>> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}