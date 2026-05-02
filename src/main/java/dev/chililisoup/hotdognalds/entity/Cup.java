package dev.chililisoup.hotdognalds.entity;

import dev.chililisoup.hotdognalds.item.CupContents;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import dev.chililisoup.hotdognalds.reg.ModItems;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Cup extends FoodEntity implements CondimentCollector {
    private float lastFillLevel = 0F;
    private float fillLevel = 0F;

    public Cup(EntityType<Cup> type, Level level) {
        super(type, level);
    }

    @Override
    protected void doClientCookEffect() {
        CupContents contents = this.getContents();
        this.level().addParticle(new DustParticleOptions(
                contents.hasDrink() ? contents.drinkColor() : 0, 0.5F
        ), this.getX(), this.getEyeY(), this.getZ(), 0.0, 1.0, 0.0);
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            if (this.firstTick) {
                this.fillLevel = this.getContents().fillLevel();
                this.lastFillLevel = this.fillLevel;
            } else {
                this.lastFillLevel = this.fillLevel;
                this.fillLevel = Mth.lerp(0.25F, this.fillLevel, this.getContents().fillLevel());
            }
        }

        super.tick();
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand, @NotNull Vec3 location) {
        if (player.isSpectator()) return InteractionResult.SUCCESS;

        if (!this.isRemoved() && player.level() instanceof ServerLevel serverLevel) {
            this.kill(serverLevel);
            this.markHurt();

            ItemStack cupStack = this.getItem();
            if (hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).isEmpty())
                player.setItemInHand(hand, cupStack);
            else player.addItem(cupStack);

            this.playTakeSound();
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource source, float damage) {
        if (this.isInvulnerableToBase(source)) return false;
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING) && source.getEntity() instanceof Mob)
            return false;
        if (this.isRemoved()) return true;

        if (source.is(DamageTypes.HOT_FLOOR)) {
            CupContents contents = this.getContents();
            float fillLevel = contents.fillLevel();
            if (fillLevel > 0) this.setContents(contents.withFillLevel(fillLevel - 0.005F * damage));
            else if (this.random.nextFloat() > 0.98F) this.placeFire(level);
        } else {
            this.kill(level);
            this.markHurt();

            if (!source.isCreativePlayer()) {
                ItemStack drop = this.getItem();
                Block.popResource(this.level(), this.blockPosition(), drop);
            }
            this.playTakeSound();
        }

        return true;
    }

    public CupContents getContents() {
        return this.getItemRaw().getOrDefault(ModComponents.CUP_CONTENTS, CupContents.EMPTY);
    }

    public void setContents(CupContents contents) {
        ItemStack stack = this.getItemRaw().copy();
        stack.set(ModComponents.CUP_CONTENTS, contents);
        this.setItem(stack);
    }

    public float getFillLevel(float partialTick) {
        return this.firstTick ?
                this.getContents().fillLevel() :
                Mth.lerp(partialTick, this.lastFillLevel, this.fillLevel);
    }

    public void mixDrink(float amount, int color) {
        CupContents contents = this.getContents();
        if (contents.isFull()) return;

        float fillLevel = Math.min(contents.fillLevel() + amount, 1F);
        int drinkColor = ARGB.srgbLerp(
                contents.fillLevel() / fillLevel,
                color,
                contents.drinkColor()
        );
        this.setContents(new CupContents(fillLevel, drinkColor));
    }

    @Override
    public void collectCondiment(int color) {
        this.mixDrink(0.25F, color);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.CUP;
    }

    private ItemStack getItemRaw() {
        return super.getItem();
    }

    @Override
    protected ItemStack getItem() {
        ItemStack stack = this.getItemRaw().copy();
        CupContents.updateItem(stack);
        return stack;
    }
}
