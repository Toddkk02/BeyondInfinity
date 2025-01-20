package net.todd.beyondinfinity.structure;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class CrystalCluster extends Block {
    protected final VoxelShape northAabb;
    protected final VoxelShape southAabb;
    protected final VoxelShape eastAabb;
    protected final VoxelShape westAabb;
    protected final VoxelShape upAabb;
    protected final VoxelShape downAabb;

    public CrystalCluster() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .noOcclusion()
                .randomTicks()
                .sound(SoundType.AMETHYST_CLUSTER)
                .strength(1.5F)
                .lightLevel((state) -> 5)
                .pushReaction(PushReaction.DESTROY));

        this.upAabb = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D);
        this.downAabb = Block.box(3.0D, 4.0D, 3.0D, 13.0D, 16.0D, 13.0D);
        this.northAabb = Block.box(3.0D, 3.0D, 4.0D, 13.0D, 13.0D, 16.0D);
        this.southAabb = Block.box(3.0D, 3.0D, 0.0D, 13.0D, 13.0D, 12.0D);
        this.eastAabb = Block.box(0.0D, 3.0D, 3.0D, 12.0D, 13.0D, 13.0D);
        this.westAabb = Block.box(4.0D, 3.0D, 3.0D, 16.0D, 13.0D, 13.0D);
    }

    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        Direction direction = Direction.UP; // Default direction, puoi implementare la direzionalità se necessario
        return switch (direction) {
            case NORTH -> this.northAabb;
            case SOUTH -> this.southAabb;
            case EAST -> this.eastAabb;
            case WEST -> this.westAabb;
            case DOWN -> this.downAabb;
            default -> this.upAabb;
        };
    }
}