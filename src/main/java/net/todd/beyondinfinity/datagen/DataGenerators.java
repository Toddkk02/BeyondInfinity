package net.todd.beyondinfinity.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.todd.beyondinfinity.BeyondInfinity;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = BeyondInfinity.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Registra il provider solo se includeServer è true e non è già stato registrato
        if (event.includeServer()) {
            generator.addProvider(true, new ModWorldGenProvider(packOutput, lookupProvider));
        }
    }
}