package dev.chililisoup.hotdognalds.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.hotdognalds.block.entity.SodaFountainBlockEntity;
import dev.chililisoup.hotdognalds.reg.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class SodaFountainBlock extends BaseEntityBlock {
    public static final MapCodec<SodaFountainBlock> CODEC = simpleCodec(SodaFountainBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty VARIANT = ModBlockStateProperties.SODA_FOUNTAIN_VARIANT;
    public static final int VARIANT_COUNT = VARIANT.getPossibleValues().size();
    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(
            Shapes.or(
                    Block.column(16, 0, 2),
                    Block.boxZ(16, 2, 16, 5, 16),
                    Block.box(0.5, 12, 0, 3.5, 16, 5),
                    Block.box(4.5, 12, 0, 7.5, 16, 5),
                    Block.box(8.5, 12, 0, 11.5, 16, 5),
                    Block.box(12.5, 12, 0, 15.5, 16, 5)
            )
    );

    @Override
    public @NotNull MapCodec<SodaFountainBlock> codec() {
        return CODEC;
    }

    public SodaFountainBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(VARIANT, 0)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(VARIANT, context.getLevel().getRandom().nextInt(VARIANT_COUNT));
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
    protected @NotNull InteractionResult useWithoutItem(
            BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            BlockHitResult hitResult
    ) {
        Direction facing = state.getValue(FACING);
        if (hitResult.getDirection() != facing)
            return InteractionResult.PASS;

        Vec2 hitPos = getRelativeHitCoordinatesForBlockFace(
                hitResult, facing
        ).orElse(null);
        if (hitPos == null || hitPos.y < 0.75)
            return InteractionResult.PASS;

        Optional<Integer> dispenser = getDispenser(hitPos.x * 16);
        if (dispenser.isEmpty())
            return InteractionResult.PASS;

        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SodaFountainBlockEntity fountain)
            fountain.startDispensing(dispenser.get());

        return InteractionResult.SUCCESS;
    }

    private static Optional<Integer> getDispenser(float px) {
        if (px < 0.5 || px > 15.5) return Optional.empty();
        if (px <= 3.5) return Optional.of(0);
        if (px >= 12.5) return Optional.of(3);
        if (px < 4.5 || px > 11.5) return Optional.empty();
        if (px <= 7.5) return Optional.of(1);
        if (px >= 8.5) return Optional.of(2);
        return Optional.empty();
    }

    private static Optional<Vec2> getRelativeHitCoordinatesForBlockFace(BlockHitResult hitResult, Direction blockFacing) {
        Direction hitDirection = hitResult.getDirection();
        if (blockFacing != hitDirection) return Optional.empty();

        BlockPos hitBlockPos = hitResult.getBlockPos().relative(hitDirection);
        Vec3 relativeHit = hitResult.getLocation().subtract(hitBlockPos.getX(), hitBlockPos.getY(), hitBlockPos.getZ());
        double relativeX = relativeHit.x();
        double relativeY = relativeHit.y();
        double relativeZ = relativeHit.z();

        return switch (hitDirection) {
            case NORTH -> Optional.of(new Vec2((float) (1.0 - relativeX), (float) relativeY));
            case SOUTH -> Optional.of(new Vec2((float) relativeX, (float) relativeY));
            case WEST -> Optional.of(new Vec2((float) relativeZ, (float) relativeY));
            case EAST -> Optional.of(new Vec2((float) (1.0 - relativeZ), (float) relativeY));
            case DOWN, UP -> Optional.empty();
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VARIANT);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos worldPosition, @NotNull BlockState blockState) {
        return new SodaFountainBlockEntity(worldPosition, blockState);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level, @NotNull BlockState blockState, @NotNull BlockEntityType<T> type
    ) {
        return createTickerHelper(
                type, ModBlockEntityTypes.SODA_FOUNTAIN, SodaFountainBlockEntity::tick
        );
    }
}
