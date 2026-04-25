package dev.chililisoup.hotdognalds.block;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CounterBlock extends Block {
    public static final MapCodec<CounterBlock> CODEC = simpleCodec(CounterBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
    private static final Map<Direction, VoxelShape> SHAPE_OUTER;
    private static final Map<Direction, VoxelShape> SHAPE_STRAIGHT;
    private static final Map<Direction, VoxelShape> SHAPE_INNER;

    @Override
    public @NotNull MapCodec<CounterBlock> codec() {
        return CODEC;
    }

    public CounterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SHAPE, StairsShape.STRAIGHT)
        );
    }

    @Override
    protected @NotNull VoxelShape getShape(
            BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context
    ) {
        Direction facing = state.getValue(FACING);

        Map<Direction, VoxelShape> shape = switch (state.getValue(SHAPE)) {
            case STRAIGHT -> SHAPE_STRAIGHT;
            case OUTER_LEFT, OUTER_RIGHT -> SHAPE_OUTER;
            case INNER_RIGHT, INNER_LEFT -> SHAPE_INNER;
        };

        return shape.get(switch (state.getValue(SHAPE)) {
            case STRAIGHT, OUTER_LEFT, INNER_RIGHT -> facing;
            case INNER_LEFT -> facing.getCounterClockWise();
            case OUTER_RIGHT -> facing.getClockWise();
        });
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
        return state.setValue(SHAPE, getCounterShape(state, context.getLevel(), context.getClickedPos()));
    }

    @Override
    protected @NotNull BlockState updateShape(
            @NotNull BlockState state,
            @NotNull LevelReader level,
            @NotNull ScheduledTickAccess ticks,
            @NotNull BlockPos pos,
            @NotNull Direction directionToNeighbour,
            @NotNull BlockPos neighbourPos,
            @NotNull BlockState neighbourState,
            @NotNull RandomSource random
    ) {
        return directionToNeighbour.getAxis().isHorizontal()
                ? state.setValue(SHAPE, getCounterShape(state, level, pos))
                : super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    private static StairsShape getCounterShape(final BlockState state, final BlockGetter level, final BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockState behindState = level.getBlockState(pos.relative(facing));
        if (isCounter(behindState)) {
            Direction behindFacing = behindState.getValue(FACING);
            if (behindFacing.getAxis() != state.getValue(FACING).getAxis() && canTakeShape(state, level, pos, behindFacing.getOpposite()))
                return behindFacing == facing.getCounterClockWise() ?
                        StairsShape.INNER_LEFT :
                        StairsShape.INNER_RIGHT;
        }

        BlockState frontState = level.getBlockState(pos.relative(facing.getOpposite()));
        if (isCounter(frontState)) {
            Direction frontFacing = frontState.getValue(FACING);
            if (frontFacing.getAxis() != state.getValue(FACING).getAxis() && canTakeShape(state, level, pos, frontFacing))
                return frontFacing == facing.getCounterClockWise() ?
                        StairsShape.OUTER_LEFT :
                        StairsShape.OUTER_RIGHT;
        }

        return StairsShape.STRAIGHT;
    }

    private static boolean canTakeShape(final BlockState state, final BlockGetter level, final BlockPos pos, final Direction neighbour) {
        BlockState neighborState = level.getBlockState(pos.relative(neighbour));
        return !isCounter(neighborState) || neighborState.getValue(FACING) != state.getValue(FACING);
    }

    public static boolean isCounter(final BlockState state) {
        return state.getBlock() instanceof CounterBlock;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SHAPE);
    }

    static {
        VoxelShape outer = Shapes.or(
                Block.column(16, 14, 16),
                Block.box(2, 3, 2, 16.0, 14.0, 16.0)
        );
        VoxelShape straight = Shapes.or(outer, Shapes.rotate(outer, OctahedralGroup.BLOCK_ROT_Y_90));
        VoxelShape inner = Shapes.or(straight, Shapes.rotate(straight, OctahedralGroup.BLOCK_ROT_Y_90));

        VoxelShape outerFeet = Shapes.or(
                Block.box(3, 0, 3, 5, 3, 5),
                Block.box(13, 0, 3, 15, 3, 5),
                Block.box(13, 0, 13, 15, 3, 15),
                Block.box(3, 0, 13, 5, 3, 15)
        );
        VoxelShape straightFeet = Shapes.or(
                Block.box(1, 0, 3, 3, 3, 5),
                Block.box(13, 0, 3, 15, 3, 5),
                Block.box(13, 0, 13, 15, 3, 15),
                Block.box(1, 0, 13, 3, 3, 15)
        );
        VoxelShape innerFeet = Shapes.rotate(outerFeet, OctahedralGroup.BLOCK_ROT_Y_90);

        SHAPE_OUTER = Shapes.rotateHorizontal(Shapes.or(outer, outerFeet));
        SHAPE_STRAIGHT = Shapes.rotateHorizontal(Shapes.or(straight, straightFeet));
        SHAPE_INNER = Shapes.rotateHorizontal(Shapes.or(inner, innerFeet));
    }
}
