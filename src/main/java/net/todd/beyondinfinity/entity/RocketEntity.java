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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.todd.beyondinfinity.world.dimension.DimensionTeleporter;
import net.todd.beyondinfinity.world.dimension.ModDimensions;

import java.util.List;

public class RocketEntity extends Entity {
    private static final EntityDataAccessor<Boolean> LAUNCHED =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_DESCENDING =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int MOON_TRANSITION_HEIGHT = 3000;
    private static final int MOON_ENTRY_HEIGHT = 1000;
    private static final double LAUNCH_SPEED = 2.0;
    private static final double DESCENT_SPEED = -75.0;
    private static final double FINAL_DESCENT_SPEED = -2.0;
    private static final double HORIZONTAL_SPEED_MULTIPLIER = 0.2;

    public RocketEntity(EntityType<? extends RocketEntity> type, Level level) {
        super(type, level);
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
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("Launched", isLaunched());
        tag.putBoolean("IsDescending", isDescending());
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level().isClientSide) {
            if (!isLaunched()) {
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
                if (this.level().dimension() == Level.OVERWORLD) {
                    handleOverworldFlight();
                } else if (this.level().dimension() == ModDimensions.MOON_KEY_LEVEL) {
                    handleMoonFlight();
                }
            }

            // Sync position with passengers
            if (!this.getPassengers().isEmpty()) {
                this.getPassengers().get(0).setPos(this.getX(), this.getY() + 1, this.getZ());
            }
        }
    }

    private void handleOverworldFlight() {
        if (this.getY() >= MOON_TRANSITION_HEIGHT) {
            handleMoonTransition();
        } else {
            // Continue ascending
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(
                    motion.x * HORIZONTAL_SPEED_MULTIPLIER,
                    LAUNCH_SPEED,
                    motion.z * HORIZONTAL_SPEED_MULTIPLIER
            );
        }
    }

    private void handleMoonFlight() {
        if (isDescending()) {
            double descentSpeed = this.getY() > 10 ? DESCENT_SPEED : FINAL_DESCENT_SPEED;
            this.setDeltaMovement(0, descentSpeed, 0);

            if (this.onGround()) {
                setLaunched(false);
                setDescending(false);
            }
        }
    }

    private void handleMoonTransition() {
        if (!this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof ServerPlayer player) {
            ServerLevel moonLevel = player.getServer().getLevel(ModDimensions.MOON_KEY_LEVEL);
            if (moonLevel != null) {
                // Calculate target position in moon dimension
                Vec3 targetPos = new Vec3(this.getX(), MOON_ENTRY_HEIGHT, this.getZ());

                // Create teleporter
                DimensionTeleporter teleporter = new DimensionTeleporter(moonLevel, targetPos);

                // Teleport rocket and player
                Entity newRocket = this.changeDimension(moonLevel, teleporter);
                if (newRocket instanceof RocketEntity rocketEntity) {
                    rocketEntity.setDescending(true);
                    player.changeDimension(moonLevel, teleporter);
                    player.startRiding(newRocket);
                }
            }
        }
    }

    public void launch() {
        if (!isLaunched() && !this.getPassengers().isEmpty()) {
            setLaunched(true);
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
            // Only allow dismounting when not launched
            Vec3 dismountPos = this.getDismountLocationForPassenger((LivingEntity)passenger);
            passenger.setPos(dismountPos.x, dismountPos.y, dismountPos.z);
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        double d0 = Math.cos((passenger.getYRot() + 90) * Math.PI / 180.0);
        double d1 = Math.sin((passenger.getYRot() + 90) * Math.PI / 180.0);
        double d2 = this.getBbWidth() + passenger.getBbWidth();

        double x = this.getX() + d0 * d2;
        double z = this.getZ() + d1 * d2;
        BlockPos blockPos = new BlockPos((int)x, (int)this.getY(), (int)z);

        if (!this.level().getBlockState(blockPos).isAir()) {
            return new Vec3(x, this.getY() + 1, z);
        }

        return new Vec3(x, this.getY(), z);
    }

    @Override
    public void push(Entity entity) {
        // Disable pushing when launched
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