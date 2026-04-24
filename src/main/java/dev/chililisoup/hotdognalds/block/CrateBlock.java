package dev.chililisoup.hotdognalds.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.hotdognalds.block.entity.CrateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class CrateBlock extends BaseEntityBlock {
    public static final MapCodec<CrateBlock> CODEC = simpleCodec(CrateBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    protected static final VoxelShape SHAPE = Shapes.join(
            Shapes.block(),
            Block.column(14, 12, 16),
            BooleanOp.ONLY_FIRST
    );

    @Override
    public @NotNull MapCodec<CrateBlock> codec() {
        return CODEC;
    }

    public CrateBlock(Properties properties) {
        super(properties);
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull BlockHitResult hitResult
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof CrateBlockEntity crateBlockEntity))
            return InteractionResult.PASS;

        ItemStack crateStack = crateBlockEntity.getTheItem();

        if (player.hasInfiniteMaterials() && player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                crateBlockEntity.removeItemStack();
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.5F, 1.0F);
            }
            return InteractionResult.SUCCESS_SERVER;
        }

        if (crateStack.isEmpty()) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            if (player.addItem(crateStack))
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 2.0F);
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected @NotNull InteractionResult useItemOn(
            @NotNull ItemStack itemStack,
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull InteractionHand hand,
            @NotNull BlockHitResult hitResult
    ) {
        if (!player.hasInfiniteMaterials())
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof CrateBlockEntity crateBlockEntity))
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        ItemStack crateStack = crateBlockEntity.getTheItem();
        if (!crateStack.isEmpty())
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (!level.isClientSide()) {
            crateBlockEntity.setItemStack(itemStack);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.5F, 1.0F);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos worldPosition, @NotNull BlockState blockState) {
        return new CrateBlockEntity(worldPosition, blockState);
    }
}
