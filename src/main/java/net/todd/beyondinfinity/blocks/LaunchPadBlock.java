package net.todd.beyondinfinity.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LaunchPadBlock extends Block {
    public static final BooleanProperty MASTER = BooleanProperty.create("master");
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 4, 16);

    public LaunchPadBlock() {
        super(Properties.of()
                .strength(4.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(MASTER, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MASTER);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        
        // Verifica se questo blocco può essere il master (centro) di una struttura 3x3
        if (canFormMultiblock(level, pos)) {
            return this.defaultBlockState().setValue(MASTER, true);
        }
        
        return this.defaultBlockState();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && state.getValue(MASTER)) {
            // Se questo è il blocco master, verifica e forma il multiblock
            if (!validateMultiblock(level, pos)) {
                // Se non può formare un multiblock valido, rimuovi il flag master
                level.setBlock(pos, state.setValue(MASTER, false), 3);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getValue(MASTER) && state.getBlock() != newState.getBlock()) {
            // Quando il blocco master viene rimosso, aggiorna i blocchi circostanti
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) continue;
                    BlockPos relativePos = pos.offset(x, 0, z);
                    BlockState relativeState = level.getBlockState(relativePos);
                    if (relativeState.is(this)) {
                        level.setBlock(relativePos, relativeState.setValue(MASTER, false), 3);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private boolean canFormMultiblock(Level level, BlockPos pos) {
        // Verifica se ci sono 8 blocchi di launch pad attorno a questo
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                
                BlockPos checkPos = pos.offset(x, 0, z);
                BlockState checkState = level.getBlockState(checkPos);
                
                if (!checkState.is(this) || checkState.getValue(MASTER)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateMultiblock(Level level, BlockPos pos) {
        // Verifica che tutti i blocchi necessari siano presenti
        return canFormMultiblock(level, pos);
    }

    // Metodo di utilità per verificare se una posizione è parte di una piattaforma di lancio valida
    public static boolean isValidLaunchPad(Level level, BlockPos pos) {
        // Cerca il blocco master nella zona 3x3
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = pos.offset(x, 0, z);
                BlockState checkState = level.getBlockState(checkPos);
                
                if (checkState.getBlock() instanceof LaunchPadBlock && 
                    checkState.getValue(MASTER)) {
                    // Trovato il master, verifica che la struttura sia valida
                    return ((LaunchPadBlock)checkState.getBlock()).validateMultiblock(level, checkPos);
                }
            }
        }
        return false;
    }
}
