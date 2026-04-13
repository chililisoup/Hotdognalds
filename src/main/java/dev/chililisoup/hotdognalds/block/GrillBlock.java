package dev.chililisoup.hotdognalds.block;

import dev.chililisoup.hotdognalds.entity.Hotdog;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class GrillBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public GrillBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(LIT, context.getLevel().hasNeighborSignal(context.getClickedPos()));
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }
}
