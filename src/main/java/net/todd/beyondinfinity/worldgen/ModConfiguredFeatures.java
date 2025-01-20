package net.todd.beyondinfinity.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.todd.beyondinfinity.BeyondInfinity;
import net.todd.beyondinfinity.blocks.ModBlocks;

import java.util.List;

public class ModConfiguredFeatures {
    // Feature Registry
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, BeyondInfinity.MODID);

    // Crystal Feature Registration
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CRYSTAL_FEATURE =
            FEATURES.register("crystal_feature", () -> new CrystalFeature());

    // Resource Keys
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_TIN_ORE_KEY = registerKey("tin_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_ALUMINIUM_ORE_KEY = registerKey("aluminium_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_LITHIUM_ORE_KEY = registerKey("lithium_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_TITANIUM_ORE_KEY = registerKey("titanium_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> RUBBER_TREE_KEY = registerKey("rubber_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRYSTAL_GEODE_KEY = registerKey("crystal_geode");

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(BeyondInfinity.MODID, name));
    }

    // Crystal Feature Implementation
    private static class CrystalFeature extends Feature<NoneFeatureConfiguration> {
        public CrystalFeature() {
            super(NoneFeatureConfiguration.CODEC);
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

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> pContext) {
        RuleTest stoneReplaceable = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepSlateReplaceable = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        List<OreConfiguration.TargetBlockState> tinOreTargets = List.of(
                OreConfiguration.target(stoneReplaceable, ModBlocks.TIN_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepSlateReplaceable, ModBlocks.TIN_ORE.get().defaultBlockState())
        );

        List<OreConfiguration.TargetBlockState> aluminiumOreTargets = List.of(
                OreConfiguration.target(stoneReplaceable, ModBlocks.ALUMINIUM_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepSlateReplaceable, ModBlocks.ALUMINIUM_ORE.get().defaultBlockState())
        );

        List<OreConfiguration.TargetBlockState> lithiumOreTargets = List.of(
                OreConfiguration.target(stoneReplaceable, ModBlocks.LITHIUM_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepSlateReplaceable, ModBlocks.LITHIUM_ORE.get().defaultBlockState())
        );

        List<OreConfiguration.TargetBlockState> titaniumOreTargets = List.of(
                OreConfiguration.target(stoneReplaceable, ModBlocks.TITANIUM_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepSlateReplaceable, ModBlocks.TITANIUM_ORE.get().defaultBlockState())
        );

        // Configurazione minerali simile al ferro/rame (vein size 9)
        register(pContext, OVERWORLD_TIN_ORE_KEY, Feature.ORE, new OreConfiguration(tinOreTargets, 9));
        register(pContext, OVERWORLD_ALUMINIUM_ORE_KEY, Feature.ORE, new OreConfiguration(aluminiumOreTargets, 9));
        register(pContext, OVERWORLD_LITHIUM_ORE_KEY, Feature.ORE, new OreConfiguration(lithiumOreTargets, 9));
        register(pContext, OVERWORLD_TITANIUM_ORE_KEY, Feature.ORE, new OreConfiguration(titaniumOreTargets, 9));

        // Configurazione albero della gomma con controllo terreno
        register(pContext, RUBBER_TREE_KEY, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.RUBBER_LOG.get()),
                new StraightTrunkPlacer(5, 2, 2),
                BlockStateProvider.simple(ModBlocks.RUBBER_LEAVES.get()),
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
                new TwoLayersFeatureSize(1, 0, 1))
                .dirt(BlockStateProvider.simple(net.minecraft.world.level.block.Blocks.DIRT))
                .build());

        // Configurazione geodi di cristallo
        register(pContext, CRYSTAL_GEODE_KEY, CRYSTAL_FEATURE.get(),
                new NoneFeatureConfiguration());
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
            BootstapContext<ConfiguredFeature<?, ?>> context,
            ResourceKey<ConfiguredFeature<?, ?>> key,
            F feature,
            FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}