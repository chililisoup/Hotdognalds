package dev.chililisoup.hotdognalds.block.entity;

import com.mojang.serialization.Codec;
import dev.chililisoup.hotdognalds.block.SodaFountainBlock;
import dev.chililisoup.hotdognalds.entity.Cup;
import dev.chililisoup.hotdognalds.reg.ModBlockEntityTypes;
import dev.chililisoup.hotdognalds.reg.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SodaFountainBlockEntity extends BlockEntity {
    private static final int DISPENSE_TICKS = 5;

    private final List<Dispenser> dispensers = List.of(
            new Dispenser("dispenser0", 0.125F, 0xBD341005, 0xBDFFFD10, 0xBDFFB1E3),
            new Dispenser("dispenser1", 0.375F, 0xBDC251FF, 0xBDFF5157, 0xBDBEED7F),
            new Dispenser("dispenser2", 0.625F, 0xBD36DEE7, 0x80CDF2F2, 0xBD4F104A),
            new Dispenser("dispenser3", 0.875F, 0xBD51ED0B, 0xBDFBA414, 0xBDFFE4D0)
    );
    private boolean ticking;

    public SodaFountainBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntityTypes.SODA_FOUNTAIN, worldPosition, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SodaFountainBlockEntity fountain) {
        if (!fountain.ticking) return;

        Direction facing = state.getValue(SodaFountainBlock.FACING);
        Vec3 bottomCenter = pos.getBottomCenter();
        int colorIndex = state.getValue(SodaFountainBlock.VARIANT);

        if (level.isClientSide()) {

            for (Dispenser dispenser : fountain.dispensers) {
                if (dispenser.isInactive()) continue;

                Vec3 nozzlePos = bottomCenter.add(dispenser.pos(facing));
                level.addParticle(ColorParticleOption.create(
                        ModParticles.COLORED_FALL, dispenser.colors[colorIndex]
                ), nozzlePos.x, nozzlePos.y, nozzlePos.z, 0, 0, 0);
            }

            return;
        }

        boolean changed = false;
        for (Dispenser dispenser : fountain.dispensers) {
            if (dispenser.isInactive()) continue;
            if (--dispenser.value == 0) changed = true;

            Vec3 nozzlePos = bottomCenter.add(dispenser.pos(facing));
            int color = dispenser.colors[colorIndex];

            level.getEntitiesOfClass(Cup.class, AABB.ofSize(
                    nozzlePos.subtract(0, 0.28125, 0),
                    0.25,
                    0.28125,
                    0.25
            )).forEach(cup -> cup.mixDrink(0.025F, color));

            if (level.getRandom().nextFloat() > 0.5F) level.playSound(
                    null, pos, SoundEvents.MOSS_BREAK, SoundSource.BLOCKS, 1F, 0.625F
            );
        }

        if (changed) {
            fountain.updateClients(level, pos, state);
            fountain.updateTicking();
        }
    }
    
    public void startDispensing(int dispenserIndex) {
        Dispenser dispenser = this.dispensers.get(dispenserIndex);
        boolean changed = dispenser.isInactive();
        dispenser.value = DISPENSE_TICKS;
        this.ticking = true;
        if (changed) this.updateClients();
    }
    
    private void updateClients(Level level, BlockPos pos, BlockState state) {
        level.gameEvent(GameEvent.BLOCK_CHANGE, this.worldPosition, GameEvent.Context.of(state));
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }
    
    private void updateClients() {
        if (this.level != null) this.updateClients(this.level, this.getBlockPos(), this.getBlockState());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void updateTicking() {
        this.ticking = this.dispensers.stream().anyMatch(Dispenser::isDispensing);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        this.dispensers.forEach(dispenser -> dispenser.value = input
                .read(dispenser.name, Codec.BYTE)
                .orElse((byte) 0)
        );
        this.updateTicking();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.dispensers.forEach(dispenser -> tag.putInt(dispenser.name, dispenser.value));
        return tag;
    }

    private static final class Dispenser {
        private final String name;
        private final float nozzlePos;
        private final int[] colors = new int[SodaFountainBlock.VARIANT_COUNT];
        private byte value = 0;

        private Dispenser(String name, float nozzlePos, int... colors) {
            this.name = name;
            this.nozzlePos = nozzlePos - 0.5F;
            for (int i = 0; i < colors.length && i < this.colors.length; i++)
                this.colors[i] = colors[i];
        }

        private boolean isDispensing() {
            return this.value > 0;
        }

        private boolean isInactive() {
            return !this.isDispensing();
        }

        private Vec3 pos(Direction facing) {
            return switch (facing) {
                case NORTH -> new Vec3(-this.nozzlePos, 0.5625F, -0.375F);
                case SOUTH -> new Vec3(this.nozzlePos, 0.5625F, 0.375F);
                case EAST -> new Vec3(0.375F, 0.5625F, -this.nozzlePos);
                default -> new Vec3(-0.375F, 0.5625F, this.nozzlePos);
            };
        }
    }
}
