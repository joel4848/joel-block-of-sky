package net.voidblock;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.voidblock.config.VoidBlockConfig;

public class VoidBlock extends BaseEntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final MapCodec<VoidBlock> CODEC = simpleCodec(VoidBlock::new);

    protected VoidBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ACTIVE, true));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new VoidBlockEntity(blockPos, blockState);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        if (VoidBlockConfig.getInstance().enableRedstoneControl) {
            final boolean powered = ctx.getLevel().hasNeighborSignal(ctx.getClickedPos());
            return this.defaultBlockState().setValue(ACTIVE, !powered);
        } else {
            return this.defaultBlockState().setValue(ACTIVE, true);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof VoidBlockEntity voidBlockEntity) {
            voidBlockEntity.neighborChanged();
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborChanged(state, level, pos, block, fromPos, notify);

        final BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof VoidBlockEntity voidBlockEntity) {
            voidBlockEntity.neighborChanged();
        }

        if (!level.isClientSide && VoidBlockConfig.getInstance().enableRedstoneControl) {
            var hasSignal = level.hasNeighborSignal(pos);
            if (state.getValue(ACTIVE) == hasSignal) {
                level.setBlock(pos, state.setValue(ACTIVE, !hasSignal), 2);
            }
        }
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLightBlock(BlockState state, BlockGetter world, BlockPos pos) {
        if (state.isSolidRender(world, pos)) {
            return world.getMaxLightLevel();
        } else {
            return state.propagatesSkylightDown(world, pos) ? 0 : 1;
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        if (VoidBlockConfig.getInstance().enableRedstoneControl) {
            return state.getValue(ACTIVE) ? RenderShape.INVISIBLE : RenderShape.MODEL;
        } else {
            return RenderShape.INVISIBLE;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }
}