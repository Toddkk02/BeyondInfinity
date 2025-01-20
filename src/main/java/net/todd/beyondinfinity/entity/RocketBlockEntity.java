package net.todd.beyondinfinity.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.todd.beyondinfinity.blocks.ModBlocks;
import net.todd.beyondinfinity.item.HydrogenTank;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.world.entity.Entity;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class RocketBlockEntity extends BlockEntity {
    private static final int REQUIRED_FUEL = 1000; // Una tanica
    private int currentFuel = 0;
    private Player mountedPlayer = null;
    private boolean isLaunching = false;
    private double currentVelocity = 0.0D;
    private double acceleration = 0.01D;
    private AtomicBoolean shouldContinue = new AtomicBoolean(true);
    private final Random random = new Random();
    private int countdown = 10;
    private RocketEntity rocketEntity;

    public RocketBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROCKET_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide) {
            // Usa il costruttore corretto
            rocketEntity = new RocketEntity(ModEntities.ROCKET.get(), level);
            rocketEntity.setPos(worldPosition.getX() + 0.5, worldPosition.getY(), worldPosition.getZ() + 0.5);
            level.addFreshEntity(rocketEntity);
        }
    }

    public RocketEntity getRocketEntity() {
        return rocketEntity;
    }

    public boolean addFuel(ItemStack tank) {
        if (tank.getItem() instanceof HydrogenTank) {
            HydrogenTank hydrogenTank = (HydrogenTank) tank.getItem();
            int storedHydrogen = hydrogenTank.getStoredHydrogen(tank);

            if (storedHydrogen > 0 && currentFuel < REQUIRED_FUEL) {
                currentFuel += storedHydrogen;
                hydrogenTank.removeHydrogen(tank, storedHydrogen);

                if (currentFuel >= REQUIRED_FUEL) {
                    currentFuel = REQUIRED_FUEL;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasEnoughFuel() {
        return currentFuel >= REQUIRED_FUEL;
    }

    public void handleSpacePress(Player player) {
        if (player.isPassenger() && player.getVehicle() == rocketEntity && !isLaunching && hasEnoughFuel()) {
            mountedPlayer = player;
            startLaunchSequence();
        }
    }

    private void startLaunchSequence() {
        if (mountedPlayer instanceof ServerPlayer serverPlayer) {
            isLaunching = true;
            new Thread(() -> {
                try {
                    // Conto alla rovescia
                    for (int i = 10; i > 0; i--) {
                        if (!shouldContinue.get()) break;

                        String countdownText = "§c" + i;
                        serverPlayer.connection.send(new ClientboundSetTitleTextPacket(
                                Component.literal(countdownText)));

                        Thread.sleep(1000);
                    }

                    if (shouldContinue.get()) {
                        serverPlayer.connection.send(new ClientboundSetTitleTextPacket(
                                Component.literal("§aDecollo!")));
                        launchRocket();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void launchRocket() {
        if (mountedPlayer == null || level == null || rocketEntity == null) return;

        // Avvia il razzo usando il metodo launch() della RocketEntity
        rocketEntity.launch();

        // Il resto della logica di volo è gestita nella RocketEntity
        if (mountedPlayer instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetTitleTextPacket(
                    Component.literal("§6Decollo iniziato!")));
        }
    }

    private void spawnRocketParticles() {
        if (level != null && !level.isClientSide && rocketEntity != null) {
            double x = rocketEntity.getX();
            double y = rocketEntity.getY();
            double z = rocketEntity.getZ();

            // Spawn flame particles sotto il razzo
            for (int i = 0; i < 10; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (random.nextDouble() - 0.5) * 0.5;
                level.addParticle(ParticleTypes.FLAME,
                        x + offsetX, y - 0.5, z + offsetZ,
                        0.0D, -0.2D, 0.0D);
            }
        }
    }

    public void dropInventory() {
        shouldContinue.set(false);
        SimpleContainer inventory = new SimpleContainer(1);
        if (currentFuel > 0) {
            // Qui potresti voler droppare i tank di idrogeno
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);

        if (rocketEntity != null) {
            rocketEntity.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        currentFuel = tag.getInt("Fuel");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Fuel", currentFuel);
    }
}