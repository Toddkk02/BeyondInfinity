package net.todd.beyondinfinity.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.todd.beyondinfinity.world.dimension.DimensionTeleporter;
import net.todd.beyondinfinity.world.dimension.ModDimensions;

public class RocketEntity extends Entity {
    private static final EntityDataAccessor<Boolean> LAUNCHED =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_DESCENDING =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.BOOLEAN);

    // Costanti per la fisica del razzo
    private static final int MOON_TRANSITION_HEIGHT = 320;
    private static final int MOON_ENTRY_HEIGHT = 200;
    private static final double LAUNCH_SPEED = 1.0;
    private static final double DESCENT_SPEED = -1.0;
    private static final double FINAL_DESCENT_SPEED = -0.3;
    private static final double HORIZONTAL_SPEED_MULTIPLIER = 0.1;
    private static final double ACCELERATION = 0.05;
    private static final double MAX_SPEED = 2.0;

    // Variabile per la velocità verticale
    private double verticalSpeed = 0.0;

    public RocketEntity(EntityType<? extends RocketEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(LAUNCHED, false);
        this.entityData.define(IS_DESCENDING, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Launched")) {
            setLaunched(tag.getBoolean("Launched"));
        }
        if (tag.contains("IsDescending")) {
            setDescending(tag.getBoolean("IsDescending"));
        }
        if (tag.contains("VerticalSpeed")) {
            verticalSpeed = tag.getDouble("VerticalSpeed");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("Launched", isLaunched());
        tag.putBoolean("IsDescending", isDescending());
        tag.putDouble("VerticalSpeed", verticalSpeed);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level().isClientSide) {
            if (!isLaunched() && this.getPassengers().isEmpty()) {
                player.startRiding(this);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (isLaunched()) {
                if (this.level().dimension().equals(Level.OVERWORLD)) {
                    handleOverworldFlight();
                } else if (this.level().dimension().equals(ModDimensions.MOON_KEY)) {
                    handleMoonFlight();
                }

                // Aggiorna la posizione
                this.move(MoverType.SELF, this.getDeltaMovement());
            }

            // Sincronizza i passeggeri
            for (Entity passenger : this.getPassengers()) {
                passenger.setPos(this.getX(), this.getY() + getPassengersRidingOffset(), this.getZ());
                passenger.setDeltaMovement(this.getDeltaMovement());
            }
        }
    }

    private void handleOverworldFlight() {
        if (this.getY() >= MOON_TRANSITION_HEIGHT) {
            handleMoonTransition();
        } else {
            // Accelerazione graduale
            verticalSpeed = Math.min(verticalSpeed + ACCELERATION, MAX_SPEED);

            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(
                    motion.x * HORIZONTAL_SPEED_MULTIPLIER,
                    verticalSpeed,
                    motion.z * HORIZONTAL_SPEED_MULTIPLIER
            );

            // Effetti particelle
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.FLAME,
                        this.getX(), this.getY() - 0.5, this.getZ(),
                        10, 0.2, 0.0, 0.2, 0.01
                );
            }
        }
    }

    private void handleMoonFlight() {
        if (isDescending()) {
            double descentSpeed = this.getY() > 10 ? DESCENT_SPEED : FINAL_DESCENT_SPEED;
            this.setDeltaMovement(0, descentSpeed, 0);

            if (this.onGround()) {
                setLaunched(false);
                setDescending(false);
                verticalSpeed = 0;
            }
        }
    }

    private void handleMoonTransition() {
        if (!this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof ServerPlayer player) {
            ServerLevel moonLevel = player.getServer().getLevel(ModDimensions.MOON_KEY_LEVEL);
            if (moonLevel != null) {
                // Smonta il giocatore temporaneamente
                player.stopRiding();

                // Teleporta il razzo
                Vec3 targetPos = new Vec3(this.getX(), MOON_ENTRY_HEIGHT, this.getZ());
                DimensionTeleporter teleporter = new DimensionTeleporter(moonLevel, targetPos);
                Entity newRocket = this.changeDimension(moonLevel, teleporter);

                if (newRocket instanceof RocketEntity rocketEntity) {
                    // Teleporta il giocatore
                    player.changeDimension(moonLevel, teleporter);

                    // Rimonta il giocatore e imposta la discesa
                    player.startRiding(rocketEntity);
                    rocketEntity.setDescending(true);
                    rocketEntity.verticalSpeed = DESCENT_SPEED;
                }
            }
        }
    }

    public void launch() {
        if (!isLaunched() && !this.getPassengers().isEmpty()) {
            setLaunched(true);
            this.noPhysics = true;
            verticalSpeed = LAUNCH_SPEED;

            // Imposta il movimento iniziale
            this.setDeltaMovement(0, LAUNCH_SPEED, 0);
        }
    }

    public boolean isLaunched() {
        return this.entityData.get(LAUNCHED);
    }

    public void setLaunched(boolean launched) {
        this.entityData.set(LAUNCHED, launched);
    }

    public boolean isDescending() {
        return this.entityData.get(IS_DESCENDING);
    }

    public void setDescending(boolean descending) {
        this.entityData.set(IS_DESCENDING, descending);
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return this.getPassengers().size() < 1;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 1.0D;
    }

    @Override
    public void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            Vec3 pos = new Vec3(this.getX(), this.getY() + getPassengersRidingOffset(), this.getZ());
            moveFunction.accept(passenger, pos.x, pos.y, pos.z);
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!this.level().isClientSide && !isLaunched()) {
            Vec3 dismountPos = this.getDismountLocationForPassenger((LivingEntity)passenger);
            passenger.setPos(dismountPos.x, dismountPos.y, dismountPos.z);
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        double angle = (passenger.getYRot() + 90) * Math.PI / 180.0;
        double distance = this.getBbWidth() + passenger.getBbWidth();

        double x = this.getX() + Math.cos(angle) * distance;
        double z = this.getZ() + Math.sin(angle) * distance;

        BlockPos dismountPos = new BlockPos((int)x, (int)this.getY(), (int)z);
        return !this.level().getBlockState(dismountPos).isAir() ?
                new Vec3(x, this.getY() + 1, z) :
                new Vec3(x, this.getY(), z);
    }

    @Override
    public void push(Entity entity) {
        if (!isLaunched()) {
            super.push(entity);
        }
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return !isLaunched();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}