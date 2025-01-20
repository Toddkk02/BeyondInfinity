package net.todd.beyondinfinity.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.todd.beyondinfinity.entity.RocketBlockEntity;
import net.todd.beyondinfinity.item.HydrogenTank;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class RocketBlock extends BaseEntityBlock {
    public static final BooleanProperty FUELED = BooleanProperty.create("fueled");
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 64, 14); // Altezza aumentata a 64



    public RocketBlock() {
        super(Properties.of()
                .strength(0.5f)
                .noOcclusion()
                .dynamicShape());
        this.registerDefaultState(this.stateDefinition.any().setValue(FUELED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return LaunchPadBlock.isValidLaunchPad((Level)level, pos.below());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FUELED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RocketBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RocketBlockEntity rocket) {
                ItemStack heldItem = player.getItemInHand(hand);

                if (heldItem.getItem() instanceof HydrogenTank && !state.getValue(FUELED)) {
                    if (rocket.addFuel(heldItem)) {
                        level.setBlock(pos, state.setValue(FUELED, true), 3);
                        return InteractionResult.SUCCESS;
                    }
                } else if (state.getValue(FUELED)) {
                    if (!rocket.hasEnoughFuel()) {
                        player.sendSystemMessage(Component.literal("§cCarica il razzo con l'idrogeno"));
                        return InteractionResult.FAIL;
                    }

                    // Monta il giocatore sul razzo
                    player.startRiding(rocket.getRocketEntity());
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof RocketBlockEntity rocket) {
                rocket.dropInventory();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}