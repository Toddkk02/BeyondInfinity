package net.todd.beyondinfinity.creativetab;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.todd.beyondinfinity.BeyondInfinity;
import net.todd.beyondinfinity.blocks.ModBlocks;
import net.todd.beyondinfinity.item.ModItems;

import static net.todd.beyondinfinity.BeyondInfinity.MODID;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> BEYOND_INFINITY = CREATIVE_TAB.register("beyond_infinity", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("item_group." + MODID + ".beyond_infinity"))
                    .icon(() -> new ItemStack(ModItems.ROCKET.get()))
                    .displayItems((parameters, itemList) -> {
                        // Materiali base
                        itemList.accept(ModItems.TIN_INGOT.get());
                        itemList.accept(ModItems.ALUMINIUM_INGOT.get());
                        itemList.accept(ModItems.RAW_LITHIUM.get());
                        itemList.accept(ModItems.TITANIUM_INGOT.get());

                        // Leghe e Materiali Lavorati
                        itemList.accept(ModItems.STEEL_ALLOY.get());
                        itemList.accept(ModItems.BRONZE_ALLOY.get());
                        itemList.accept(ModItems.ALUMINUM_ALLOY.get());
                        itemList.accept(ModItems.HEAVY_DUTY_PLATE.get());

                        // Minerali
                        itemList.accept(ModBlocks.ALUMINIUM_ORE.get());
                        itemList.accept(ModBlocks.TIN_ORE.get());
                        itemList.accept(ModBlocks.LITHIUM_ORE.get());
                        itemList.accept(ModBlocks.TITANIUM_ORE.get());

                        // Blocchi naturali
                        itemList.accept(ModBlocks.RUBBER_LOG.get());
                        itemList.accept(ModBlocks.RUBBER_LEAVES.get());
                        itemList.accept(ModBlocks.RUBBER_SAPLING.get());

                        // Strumenti
                        itemList.accept(ModItems.ALLOY_HAMMER.get());

                        // Tuta Spaziale
                        itemList.accept(ModItems.SPACE_SUIT_HELMET.get());
                        itemList.accept(ModItems.SPACE_SUIT_CHESTPLATE.get());
                        itemList.accept(ModItems.SPACE_SUIT_LEGGINGS.get());
                        itemList.accept(ModItems.SPACE_SUIT_BOOTS.get());

                        // Macchine e Energia
                        itemList.accept(ModBlocks.COMBUSTION_GENERATOR.get());
                        itemList.accept(ModBlocks.COPPER_INSULATED_WIRE.get());
                        itemList.accept(ModBlocks.BATTERY_BLOCK.get());
                        itemList.accept(ModBlocks.ELECTROLYSIS_GENERATOR.get());
                        itemList.accept(ModItems.HYDROGEN_TANK.get());

                        // Componenti Razzo
                        itemList.accept(ModItems.ROCKET_ENGINE.get());
                        itemList.accept(ModItems.ROCKET_NOSE_CONE.get());
                        itemList.accept(ModItems.ROCKET_FUEL_TANK.get());
                        itemList.accept(ModItems.ROCKET_BODY.get());
                        itemList.accept(ModItems.ROCKET_FINS.get());
                        itemList.accept(ModItems.ROCKET_COCKPIT.get());
                        itemList.accept(ModBlocks.ROCKET_BLOCK.get());
                        itemList.accept(ModBlocks.LAUNCH_PAD.get());

                        //BLOCCHI LUNARI
                        itemList.accept(ModBlocks.MOON_ROCK.get());
                        itemList.accept(ModBlocks.MOON_SURFACE.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_TAB.register(eventBus);
    }
}