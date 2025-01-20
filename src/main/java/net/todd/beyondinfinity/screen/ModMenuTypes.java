package net.todd.beyondinfinity.screen;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.todd.beyondinfinity.BeyondInfinity;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, BeyondInfinity.MODID);

    public static final RegistryObject<MenuType<CombustionGeneratorMenu>> COMBUSTION_GENERATOR_MENU =
            MENUS.register("combustion_generator_menu",
                    () -> IForgeMenuType.create(((windowId, inv, data) ->
                            new CombustionGeneratorMenu(windowId, inv, inv.player.level().getBlockEntity(data.readBlockPos()),
                                    new SimpleContainerData(4)))));

    public static final RegistryObject<MenuType<BatteryMenu>> BATTERY_MENU =
            MENUS.register("battery_menu",
                    () -> IForgeMenuType.create(((windowId, inv, data) ->
                            new BatteryMenu(windowId, inv, data))));
    public static final RegistryObject<MenuType<ElectrolysisGeneratorMenu>> ELECTROLYSIS_GENERATOR_MENU =
            MENUS.register("electrolysis_generator_menu",
                    () -> IForgeMenuType.create(((windowId, inv, data) ->
                            new ElectrolysisGeneratorMenu(windowId, inv, inv.player.level().getBlockEntity(data.readBlockPos()),
                                    new SimpleContainerData(4)))));


    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}