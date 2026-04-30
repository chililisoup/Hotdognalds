package dev.chililisoup.hotdognalds.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class TrashCanBlock extends Block implements WorldlyContainerHolder {
    public static final MapCodec<TrashCanBlock> CODEC = simpleCodec(TrashCanBlock::new);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private static final VoxelShape SHAPE = Shapes.or(
            Block.column(12, 0, 16),
            Block.column(14, 10, 12)
    );

    @Override
    public @NotNull MapCodec<TrashCanBlock> codec() {
        return CODEC;
    }

    public TrashCanBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AXIS, Direction.Axis.Z)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(AXIS, context.getHorizontalDirection().getAxis());
    }

    @Override
    protected @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(
            @NotNull BlockState state,
            Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull BlockHitResult hitResult
    ) {
        if (!level.isClientSide()) player.openMenu(state.getMenuProvider(level, pos));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected MenuProvider getMenuProvider(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, inventory, _) -> ChestMenu.oneRow(containerId, inventory),
                this.getName()
        );
    }

    @Override
    public @NotNull WorldlyContainer getContainer(@NotNull BlockState state, @NotNull LevelAccessor level, @NotNull BlockPos pos) {
        return new TrashContainer();
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType type) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    private static class TrashContainer extends SimpleContainer implements WorldlyContainer {
        public TrashContainer() {
            super(1);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int @NotNull [] getSlotsForFace(@NotNull Direction direction) {
            return new int[]{0};
        }

        @Override
        public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack itemStack, @Nullable Direction direction) {
            return true;
        }

        @Override
        public boolean canTakeItemThroughFace(int slot, @NotNull ItemStack itemStack, @NotNull Direction direction) {
            return false;
        }

        @Override
        public void setChanged() {
            if (!this.getItem(0).isEmpty())
                this.removeItemNoUpdate(0);
        }
    }
}
