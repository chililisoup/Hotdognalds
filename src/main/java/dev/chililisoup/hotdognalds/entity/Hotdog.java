package dev.chililisoup.hotdognalds.entity;

import dev.chililisoup.hotdognalds.item.HotdogContents;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import dev.chililisoup.hotdognalds.reg.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
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

public class Hotdog extends FoodEntity implements CondimentCollector {
    public Hotdog(EntityType<Hotdog> type, Level level) {
        super(type, level);
    }

    @Override
    protected void doClientCookEffect() {
        float halfWidth = this.hasBun() ? 0.10375F : 0.0725F;
        float length = this.hasBun() ? 0.375F : 0.5F;

        float xd = this.random.nextBoolean() ? -halfWidth : halfWidth;
        float zd = (this.random.nextFloat() - 0.5F) * length;

        float angle = Mth.PI * this.getYRot() / 180F;
        float sin = Mth.sin(angle);
        float cos = Mth.cos(angle);

        float xn = xd * cos - zd * sin;
        float zn = xd * sin + zd * cos;

        this.level().addParticle(ParticleTypes.SMALL_FLAME, this.getX() + xn, this.getY(), this.getZ() + zn, 0.0, 0.0, 0.0);
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand, @NotNull Vec3 location) {
        ItemStack handStack = player.getItemInHand(hand);
        boolean offHand = hand == InteractionHand.OFF_HAND;

        if (!this.hasDog()) {
            HotdogContents handContents = handStack.get(ModComponents.HOTDOG_CONTENTS);
            if (handContents != null && handContents.hasDog() && !handContents.hasBun()) {
                if (!this.isRemoved() && !player.level().isClientSide()) {
                    this.setMutable(handContents.toMutable().bunCookAmt(
                            this.getContents().bunCookAmt().orElse(0F)
                    ));
                    if (!this.hasCustomName())
                        this.setCustomName(handStack.getCustomName());
                    handStack.consume(1, player);

                    this.playPlaceSound();
                }

                return InteractionResult.SUCCESS_SERVER;
            }
        } else if (this.hasBun() && player.isShiftKeyDown()) {
            ItemStack hotdogStack = this.getMutable().takeBun().toImmutable().createRoundedItem();
            if ((!offHand && handStack.isEmpty()) || ItemStack.isSameItemSameComponents(handStack, hotdogStack)) {
                if (!this.isRemoved() && !player.level().isClientSide()) {
                    this.setMutable(this.getMutable().takeDog());

                    if (handStack.isEmpty()) {
                        if (offHand) player.addItem(hotdogStack);
                        else player.setItemInHand(hand, hotdogStack);
                    } else player.addItem(hotdogStack);

                    this.playTakeSound();
                }

                return InteractionResult.SUCCESS_SERVER;
            }
        }

        return super.interact(player, hand, location);
    }

    public HotdogContents getContents() {
        return this.getItemRaw().getOrDefault(ModComponents.HOTDOG_CONTENTS, HotdogContents.DOG);
    }

    public void setContents(HotdogContents contents) {
        ItemStack stack = this.getItemRaw().copy();
        stack.set(ModComponents.HOTDOG_CONTENTS, contents);
        this.setItem(stack);
    }

    public HotdogContents.Mutable getMutable() {
        return this.getContents().toMutable();
    }

    public void setMutable(HotdogContents.Mutable mutable) {
        this.setContents(mutable.toImmutable());
    }

    public boolean hasDog() {
        return this.getContents().hasDog();
    }

    public boolean hasBun() {
        return this.getContents().hasBun();
    }

    public void setCookAmt(float cookAmt) {
        this.setMutable(this.getMutable().cookAmt(cookAmt));
    }

    public void setBunCookAmt(float bunCookAmt) {
        this.setMutable(this.getMutable().bunCookAmt(bunCookAmt));
    }

    @Override
    public void collectCondiment(int color) {
        HotdogContents contents = this.getContents();
        if (!contents.hasDog()) return;

        int sauceAmount = contents.sauceAmount();
        this.setMutable(contents.toMutable().sauce(
                Math.min(sauceAmount + 1, 3),
                sauceAmount > 0 ?
                        ARGB.average(color, contents.sauceColor()) :
                        color
        ));
    }

    @Override
    public boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource source, float damage) {
        if (this.isInvulnerableToBase(source)) return false;
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING) && source.getEntity() instanceof Mob)
            return false;
        if (this.isRemoved()) return true;

        if (source.is(DamageTypes.HOT_FLOOR)) {
            HotdogContents contents = this.getContents();
            if (contents.cookAmt().isPresent()) {
                float cookAmt = contents.cookAmt().get();
                if (cookAmt < 3F) {
                    this.setCookAmt(
                            cookAmt + 0.001F * damage * (contents.hasBun() ? 0.5F : 1F)
                    );
                } else if (this.random.nextFloat() > 0.99F) this.placeFire(level);
            }

            if (contents.bunCookAmt().isPresent()) {
                float bunCookAmt = contents.bunCookAmt().get();
                if (bunCookAmt < 3F) this.setBunCookAmt(bunCookAmt + 0.002F * damage);
                else if (this.random.nextFloat() > 0.98F) this.placeFire(level);
            }
        } else if (source.is(DamageTypeTags.IS_FIRE)) {
            if (this.hasDog()) this.setCookAmt(3F);
            if (this.hasBun()) this.setBunCookAmt(3F);
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

    @Override
    protected Item getDefaultItem() {
        return ModItems.HOTDOG;
    }

    private ItemStack getItemRaw() {
        return super.getItem();
    }

    @Override
    protected ItemStack getItem() {
        ItemStack stack = this.getItemRaw().copy();
        HotdogContents.roundItem(stack);
        return stack;
    }
}
