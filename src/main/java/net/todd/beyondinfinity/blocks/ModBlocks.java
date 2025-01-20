package net.todd.beyondinfinity.blocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.todd.beyondinfinity.BeyondInfinity;
import net.todd.beyondinfinity.item.ModItems;
import net.todd.beyondinfinity.tree.RubberTreeGrower;

import java.util.function.Supplier;

public class ModBlocks {
    // Deferred Register per i blocchi
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BeyondInfinity.MODID);

    // Registrazione dei blocchi
    public static final RegistryObject<Block> TIN_ORE = registerBlock("tin_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .strength(3.0f, 6.0f)
                    .requiresCorrectToolForDrops()  // La chiave per richiedere uno strumento corretto
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> ALUMINIUM_ORE = registerBlock("aluminium_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .strength(3.0f, 6.0f)
                    .requiresCorrectToolForDrops()  // La chiave per richiedere uno strumento corretto
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> LITHIUM_ORE = registerBlock("lithium_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .strength(3.0f, 6.0f)
                    .requiresCorrectToolForDrops()  // La chiave per richiedere uno strumento corretto
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> TITANIUM_ORE = registerBlock("titanium_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .strength(4.0f, 6.0f)
                    .requiresCorrectToolForDrops()  // La chiave per richiedere uno strumento corretto
                    .sound(SoundType.STONE)));
    public static final RegistryObject<Block> ENERGY_CRYSTAL_BLOCK = registerBlock("energy_crystal_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.AMETHYST_BLOCK)
                    .strength(1.5f, 6.0f)
                    .requiresCorrectToolForDrops()  // La chiave per richiedere uno strumento corretto
                    .sound(SoundType.AMETHYST)
                    .pushReaction(PushReaction.DESTROY)
                    .lightLevel(state -> 5)));
    public static final RegistryObject<Block> RUBBER_LOG = registerBlock("rubber_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG)
                    .strength(2.0f)    // Stessa resistenza del legno vanilla
            ));
    public static final RegistryObject<Block> MOON_SURFACE = registerBlock("moon_surface",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG)
                    .strength(2.0f)
            ));
    public static final RegistryObject<Block> MOON_ROCK = registerBlock("moon_rock",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG)
                    .strength(2.0f)
            ));

    public static final RegistryObject<Block> RUBBER_LEAVES = registerBlock("rubber_leaves",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));

    public static final RegistryObject<Block> RUBBER_SAPLING = registerBlock("rubber_tree_sapling",
            () -> new SaplingBlock(new RubberTreeGrower(),
                    BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)));
    public static final RegistryObject<Block> COMBUSTION_GENERATOR = registerBlock("combustion_generator",
            () -> new CombustionGenerator());
    public static final RegistryObject<Block> COPPER_INSULATED_WIRE = registerBlock("insulated_copper_wire",
            () -> new CopperInsulatedWire());
    public static final RegistryObject<Block> BATTERY_BLOCK = registerBlock("battery_block",
            () -> new BatteryBlock());
    public static final RegistryObject<Block> ELECTROLYSIS_GENERATOR = registerBlock("electrolysis_machinery",
            () -> new ElectrolysisGenerator());
    public static final RegistryObject<Block> ROCKET_BLOCK = registerBlock("rocket_block",
            () -> new RocketBlock());
    public static final RegistryObject<Block> LAUNCH_PAD = registerBlock("launch_pad",
            () -> new LaunchPadBlock());
    // Metodo per registrare un oggetto (per il registro degli oggetti))

    // Metodo per registrare un oggetto (per il registro dei blocchi))
    // Metodo per registrare un blocco
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);  // Registrazione dell'oggetto per il blocco
        return toReturn;
    }

    // Metodo per registrare l'oggetto del blocco (per l'inventario)
    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    // Metodo per registrare tutti i blocchi presso l'evento
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
