package net.todd.beyondinfinity.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.todd.beyondinfinity.BeyondInfinity;
import net.todd.beyondinfinity.blocks.ModBlocks;
import net.todd.beyondinfinity.blocks.RocketBlock;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BeyondInfinity.MODID);

    public static final RegistryObject<BlockEntityType<CombustionGeneratorBlockEntity>> COMBUSTION_GENERATOR =
            BLOCK_ENTITIES.register("combustion_generator",
                    () -> BlockEntityType.Builder.of(CombustionGeneratorBlockEntity::new,
                            ModBlocks.COMBUSTION_GENERATOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<CopperInsulatedWireBlockEntity>> COPPER_INSULATED_WIRE =
            BLOCK_ENTITIES.register("insulated_copper_wire",
                    () -> BlockEntityType.Builder.of(CopperInsulatedWireBlockEntity::new,
                            ModBlocks.COPPER_INSULATED_WIRE.get()).build(null));

    public static final RegistryObject<BlockEntityType<BatteryBlockEntity>> BATTERY_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("battery_block",
                    () -> BlockEntityType.Builder.of(BatteryBlockEntity::new,
                            ModBlocks.BATTERY_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<ElectrolysisGeneratorBlockEntity>> ELECTROLYSIS_GENERATOR_ENTITY =
            BLOCK_ENTITIES.register("electrolysis_generator",
                    () -> BlockEntityType.Builder.of(ElectrolysisGeneratorBlockEntity::new,
                            ModBlocks.ELECTROLYSIS_GENERATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<RocketBlockEntity>>  ROCKET_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("rocket_block",
                    () -> BlockEntityType.Builder.of(RocketBlockEntity::new,
                            ModBlocks.ROCKET_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
