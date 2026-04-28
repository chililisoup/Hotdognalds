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
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateBlockEntity extends BlockEntity implements ContainerSingleItem.BlockContainerSingleItem, ItemOwner {
    private ItemStack itemStack = ItemStack.EMPTY;

    public CrateBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntityTypes.CRATE, worldPosition, blockState);
    }

    public boolean isCreative() {
        return this.getBlockState().getBlock() instanceof CrateBlock crateBlock
                && crateBlock.isCreative();
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

    @Override
    public @NotNull ItemStack getTheItem() {
        return this.isCreative() ? this.itemStack.copy() : this.itemStack;
    }

    public ItemStack takeAll() {
        if (this.isCreative()) return this.itemStack.copyWithCount(this.itemStack.getMaxStackSize());
        ItemStack toTake = this.itemStack;
        this.itemStack = ItemStack.EMPTY;
        this.itemChanged();
        return toTake;
    }

    public ItemStack takeOne() {
        if (this.isCreative()) return this.itemStack.copy();
        if (this.itemStack.isEmpty()) return ItemStack.EMPTY;
        ItemStack toTake = this.itemStack.copyWithCount(1);
        this.itemStack.shrink(1);
        this.itemChanged();
        return toTake;
    }

    @Override
    public void setTheItem(@NotNull ItemStack itemStack) {
        if (!this.isCreative()) this.itemStack = itemStack;
    }

    public boolean addFromItem(ItemStack itemStack) {
        boolean sameItem = ItemStack.isSameItemSameComponents(this.itemStack, itemStack);
        if (this.isCreative()) {
            if (sameItem) return false;
            this.itemStack = itemStack.copyWithCount(1);
            this.itemChanged();
            return true;
        }

        if (this.itemStack.isEmpty()) {
            this.itemStack = itemStack.copyAndClear();
            this.itemChanged();
            return true;
        }

        if (!sameItem) return false;
        int maxToAdd = this.itemStack.getMaxStackSize() - this.itemStack.count();
        int toAdd = Math.min(itemStack.count(), maxToAdd);
        if (toAdd <= 0) return true;

        itemStack.shrink(toAdd);
        this.itemStack.grow(toAdd);
        this.itemChanged();
        return true;
    }

    public void removeItemStack() {
        if (this.itemStack.isEmpty()) return;
        this.itemStack = ItemStack.EMPTY;
        this.itemChanged();
    }

    public void setChanged(Holder.Reference<GameEvent> event) {
        super.setChanged();
        if (this.level == null) return;
        if (event != null) this.level.gameEvent(event, this.worldPosition, GameEvent.Context.of(this.getBlockState()));
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
    }
    
    public void itemChanged() {
        this.setChanged(GameEvent.ITEM_INTERACT_FINISH);
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

    @Override
    public @NotNull BlockEntity getContainerBlockEntity() {
        return this;
    }
}
