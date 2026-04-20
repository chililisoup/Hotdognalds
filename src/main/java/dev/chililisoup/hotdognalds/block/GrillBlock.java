package dev.chililisoup.hotdognalds.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.hotdognalds.entity.Hotdog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class GrillBlock extends Block {
    public static final MapCodec<GrillBlock> CODEC = simpleCodec(GrillBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(
            Shapes.or(
                    Block.column(16, 3, 10),
                    Block.boxZ(16, 10, 16, 2, 16),
                    Block.boxZ(16, 16, 19, 14, 16),
                    Block.boxZ(16, 12, 14, -1, 2),
                    Block.box(2, 0, 2, 3, 3, 4),
                    Block.box(13, 0, 2, 14, 3, 4),
                    Block.box(2, 0, 12, 3, 3, 14),
                    Block.box(13, 0, 12, 14, 3, 14)
            )
    );

    @Override
    public @NotNull MapCodec<GrillBlock> codec() {
        return CODEC;
    }

    public GrillBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LIT, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected void neighborChanged(
            @NotNull BlockState state,
            Level level,
            @NotNull BlockPos pos,
            @NotNull Block block,
            @Nullable Orientation orientation,
            boolean movedByPiston
    ) {
        if (level.isClientSide()) return;
        boolean isLit = state.getValue(LIT);
        if (isLit != level.hasNeighborSignal(pos)) {
            if (isLit) level.scheduleTick(pos, this, 4);
            else level.setBlock(pos, state.cycle(LIT), 2);
        }
    }

    @Override
    protected void tick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (state.getValue(LIT) && !level.hasNeighborSignal(pos))
            level.setBlock(pos, state.cycle(LIT), 2);
    }


    @Override
    public void stepOn(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState onState, @NotNull Entity entity) {
        if (onState.getValue(LIT)
                && entity.getY() % 1.0 < 0.01
                && !entity.isSteppingCarefully()
                && (entity instanceof LivingEntity || entity instanceof Hotdog)
        ) {
            if (level instanceof ServerLevel serverLevel)
                entity.hurtServer(serverLevel, serverLevel.damageSources().hotFloor(), 1.0F);
            if (entity instanceof Hotdog hotdog) hotdog.doCookEffect();
        }

        super.stepOn(level, pos, onState, entity);
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType type) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }
}
