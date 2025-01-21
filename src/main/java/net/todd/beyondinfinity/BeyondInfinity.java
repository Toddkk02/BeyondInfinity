package net.todd.beyondinfinity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.todd.beyondinfinity.blocks.ModBlocks;
import net.todd.beyondinfinity.client.ClientHandler;
import net.todd.beyondinfinity.client.renderer.MoonSkyRenderer;
import net.todd.beyondinfinity.creativetab.ModCreativeTab;
import net.todd.beyondinfinity.entity.ModBlockEntities;
import net.todd.beyondinfinity.entity.ModEntities;
import net.todd.beyondinfinity.event.KeyBindings;
import net.todd.beyondinfinity.item.ModItems;
import net.todd.beyondinfinity.networking.ModMessages;
import net.todd.beyondinfinity.render.RocketRenderer;
import net.todd.beyondinfinity.screen.*;
import net.todd.beyondinfinity.world.dimension.ModDimensions;
import net.todd.beyondinfinity.world.dimension.MoonChunkGenerator;
import net.todd.beyondinfinity.worldgen.*;
import org.slf4j.Logger;

@Mod(BeyondInfinity.MODID)
public class BeyondInfinity {

    public static final String MODID = "beyondinfinity";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BeyondInfinity(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Registrazioni base
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeTab.register(modEventBus);

        // Registrazioni di menu e BlockEntities
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        // Registrazione delle entità
        ModEntities.register(modEventBus);

        // Registrazione delle dimensioni

        // Registrazione del networking
        ModMessages.register();

        // Registrazioni worldgen
        ModConfiguredFeatures.FEATURES.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerStuff);

        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void registerStuff(RegisterEvent event) {
        event.register(Registries.CHUNK_GENERATOR, helper -> {
            helper.register(new ResourceLocation(MODID, "moon"), MoonChunkGenerator.CODEC);
        });
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (Config.logDirtBlock)
                LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
            LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);
            Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Lasciato vuoto intenzionalmente - gestito in ModCreativeTab
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(ModMenuTypes.COMBUSTION_GENERATOR_MENU.get(), CombustionGeneratorScreen::new);
                MenuScreens.register(ModMenuTypes.BATTERY_MENU.get(), BatteryScreen::new);
                MenuScreens.register(ModMenuTypes.ELECTROLYSIS_GENERATOR_MENU.get(), ElectrolysisGeneratorScreen::new);

            });
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.ROCKET.get(), RocketRenderer::new);
        }

        @SubscribeEvent
        public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
            event.register(new ResourceLocation(MODID, "moon"), new MoonSkyRenderer());
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(KeyBindings.LAUNCH_ROCKET_KEY);
        }
    }
}