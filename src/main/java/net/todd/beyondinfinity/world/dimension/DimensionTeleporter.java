package net.todd.beyondinfinity.world.dimension;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class DimensionTeleporter implements ITeleporter {
    private final ServerLevel level;
    private final Vec3 targetPos;

    public DimensionTeleporter(ServerLevel level, Vec3 targetPos) {
        this.level = level;
        this.targetPos = targetPos;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        Entity repositioned = repositionEntity.apply(false);
        repositioned.teleportTo(targetPos.x, targetPos.y, targetPos.z);
        return repositioned;
    }

    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld,
                                    Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        return new PortalInfo(targetPos, Vec3.ZERO, entity.getYRot(), entity.getXRot());
    }
}