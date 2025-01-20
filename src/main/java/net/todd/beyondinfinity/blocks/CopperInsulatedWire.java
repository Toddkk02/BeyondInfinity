package net.todd.beyondinfinity.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.todd.beyondinfinity.entity.CopperInsulatedWireBlockEntity;
import net.todd.beyondinfinity.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class CopperInsulatedWire extends Block implements EntityBlock {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    private static final double SIZE = 4;
    private static final double CENTER = 8 - (SIZE/2);

    private static final VoxelShape CORE_SHAPE = Block.box(CENTER, CENTER, CENTER,
            CENTER + SIZE, CENTER + SIZE, CENTER + SIZE);
    private static final VoxelShape NORTH_SHAPE = Block.box(CENTER, CENTER, 0,
            CENTER + SIZE, CENTER + SIZE, CENTER);
    private static final VoxelShape SOUTH_SHAPE = Block.box(CENTER, CENTER, CENTER + SIZE,
            CENTER + SIZE, CENTER + SIZE, 16);
    private static final VoxelShape EAST_SHAPE = Block.box(CENTER + SIZE, CENTER, CENTER,
            16, CENTER + SIZE, CENTER + SIZE);
    private static final VoxelShape WEST_SHAPE = Block.box(0, CENTER, CENTER,
            CENTER, CENTER + SIZE, CENTER + SIZE);
    private static final VoxelShape UP_SHAPE = Block.box(CENTER, CENTER + SIZE, CENTER,
            CENTER + SIZE, 16, CENTER + SIZE);
    private static final VoxelShape DOWN_SHAPE = Block.box(CENTER, 0, CENTER,
            CENTER + SIZE, CENTER, CENTER + SIZE);

    public CopperInsulatedWire() {
        super(Properties.of()
                .strength(0.3f) // Ridotta resistenza per essere più facile da rompere a mano
                .noOcclusion()
                .instabreak()); // Rende il blocco rompibile istantaneamente a mano

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE_SHAPE;
        if (state.getValue(NORTH)) shape = Shapes.or(shape, NORTH_SHAPE);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, SOUTH_SHAPE);
        if (state.getValue(EAST)) shape = Shapes.or(shape, EAST_SHAPE);
        if (state.getValue(WEST)) shape = Shapes.or(shape, WEST_SHAPE);
        if (state.getValue(UP)) shape = Shapes.or(shape, UP_SHAPE);
        if (state.getValue(DOWN)) shape = Shapes.or(shape, DOWN_SHAPE);
        return shape;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CopperInsulatedWireBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.COPPER_INSULATED_WIRE.get() ? (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof CopperInsulatedWireBlockEntity wireEntity) {
                CopperInsulatedWireBlockEntity.tick(level1, pos, state1, wireEntity);
            }
        } : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();


        return this.defaultBlockState()
                .setValue(NORTH, canConnectTo(level, pos, Direction.NORTH))
                .setValue(SOUTH, canConnectTo(level, pos, Direction.SOUTH))
                .setValue(EAST, canConnectTo(level, pos, Direction.EAST))
                .setValue(WEST, canConnectTo(level, pos, Direction.WEST))
                .setValue(UP, canConnectTo(level, pos, Direction.UP))
                .setValue(DOWN, canConnectTo(level, pos, Direction.DOWN));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean north = canConnectTo(level, pos, Direction.NORTH);
            boolean south = canConnectTo(level, pos, Direction.SOUTH);
            boolean east = canConnectTo(level, pos, Direction.EAST);
            boolean west = canConnectTo(level, pos, Direction.WEST);
            boolean up = canConnectTo(level, pos, Direction.UP);
            boolean down = canConnectTo(level, pos, Direction.DOWN);


            if (north != state.getValue(NORTH) || south != state.getValue(SOUTH) ||
                    east != state.getValue(EAST) || west != state.getValue(WEST) ||
                    up != state.getValue(UP) || down != state.getValue(DOWN)) {

                level.setBlock(pos, state
                                .setValue(NORTH, north)
                                .setValue(SOUTH, south)
                                .setValue(EAST, east)
                                .setValue(WEST, west)
                                .setValue(UP, up)
                                .setValue(DOWN, down),
                        Block.UPDATE_ALL);
            }
        }
    }

    private boolean canConnectTo(BlockGetter world, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = world.getBlockState(neighborPos);
        BlockEntity neighborEntity = world.getBlockEntity(neighborPos);

        boolean canConnect = false;

        if (neighborState.getBlock() instanceof CopperInsulatedWire) {
            canConnect = true;
        } else if (neighborEntity != null) {
            var energyCap = neighborEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite());
            canConnect = energyCap.isPresent() &&
                    (energyCap.map(IEnergyStorage::canExtract).orElse(false) ||
                            energyCap.map(IEnergyStorage::canReceive).orElse(false));

        }


        return canConnect;
    }
}