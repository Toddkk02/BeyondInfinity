package net.todd.beyondinfinity.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.todd.beyondinfinity.BeyondInfinity;
import net.todd.beyondinfinity.item.armor.SpaceSuitBoots;
import net.todd.beyondinfinity.item.armor.SpaceSuitChestplate;
import net.todd.beyondinfinity.item.armor.SpaceSuitHelmet;
import net.todd.beyondinfinity.item.armor.SpaceSuitLeggings;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BeyondInfinity.MODID);

    public static final RegistryObject<Item> TIN_INGOT = ITEMS.register("tin_ingot",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ALUMINIUM_INGOT = ITEMS.register("aluminum_ingot",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TITANIUM_INGOT = ITEMS.register("titanium_ingot",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RAW_LITHIUM = ITEMS.register("lithium",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HYDROGEN_TANK = ITEMS.register("hydrogen_tank",
            () -> new HydrogenTank(new Item.Properties()));
    public static final RegistryObject<Item> SPACE_SUIT_HELMET = ITEMS.register("space_suit_helmet",
            () -> new SpaceSuitHelmet(ModArmorMaterials.SPACE_SUIT, ArmorItem.Type.HELMET,
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SPACE_SUIT_CHESTPLATE = ITEMS.register("space_suit_chestplate",
            () -> new SpaceSuitChestplate(ModArmorMaterials.SPACE_SUIT, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SPACE_SUIT_LEGGINGS = ITEMS.register("space_suit_leggings",
            () -> new SpaceSuitLeggings(ModArmorMaterials.SPACE_SUIT, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SPACE_SUIT_BOOTS = ITEMS.register("space_suit_boots",
            () -> new SpaceSuitBoots(ModArmorMaterials.SPACE_SUIT, ArmorItem.Type.BOOTS,
                    new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ALLOY_HAMMER = ITEMS.register("alloy_hammer",
            () -> new AlloyHammer());

    // New Alloy Items
    public static final RegistryObject<Item> STEEL_ALLOY = ITEMS.register("steel_alloy",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HEAVY_DUTY_PLATE = ITEMS.register("heavy_duty_plate",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BRONZE_ALLOY = ITEMS.register("bronze_alloy",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ALUMINUM_ALLOY = ITEMS.register("aluminum_alloy",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ROCKET_ENGINE = ITEMS.register("rocket_engine",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ROCKET_NOSE_CONE = ITEMS.register("rocket_nose_cone",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ROCKET_FUEL_TANK = ITEMS.register("rocket_fuel_tank",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ROCKET_BODY = ITEMS.register("rocket_body",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ROCKET_FINS = ITEMS.register("rocket_fins",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ROCKET_COCKPIT = ITEMS.register("rocket_cockpit",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ROCKET = ITEMS.register("rocket",
            () -> new RocketItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);

    }
}
