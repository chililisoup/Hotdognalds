package dev.chililisoup.hotdognalds.block.entity;

import dev.chililisoup.hotdognalds.block.CrateBlock;
import dev.chililisoup.hotdognalds.reg.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateBlockEntity extends BlockEntity implements ItemOwner {
    private ItemStack itemStack = ItemStack.EMPTY;

    public CrateBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntityTypes.CRATE, worldPosition, blockState);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        this.itemStack = input.read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("item", ItemStack.CODEC, this.itemStack.isEmpty() ? null : this.itemStack);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.storeNullable("item", ItemStack.CODEC, this.itemStack.isEmpty() ? null : this.itemStack);
        return tag;
    }

    public ItemStack getItemStack() {
        return this.itemStack.copy();
    }

    public void setItemStack(ItemStack itemStack) {
        if (ItemStack.isSameItemSameComponents(this.itemStack, itemStack)) return;
        this.itemStack = itemStack.copyWithCount(1);
        this.setChanged(GameEvent.ITEM_INTERACT_FINISH);
    }

    public void removeItemStack() {
        if (this.itemStack.isEmpty()) return;
        this.itemStack = ItemStack.EMPTY;
        this.setChanged(GameEvent.ITEM_INTERACT_FINISH);
    }

    public void setChanged(Holder.Reference<GameEvent> event) {
        super.setChanged();
        if (this.level == null) return;
        if (event != null) this.level.gameEvent(event, this.worldPosition, GameEvent.Context.of(this.getBlockState()));
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void setChanged() {
        this.setChanged(GameEvent.BLOCK_ACTIVATE);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public @Nullable Level level() {
        return this.level;
    }

    @Override
    public @NotNull Vec3 position() {
        return this.getBlockPos().getCenter();
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return this.getBlockState().getValue(CrateBlock.FACING).getOpposite().toYRot();
    }
}
