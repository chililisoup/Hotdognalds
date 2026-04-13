package dev.chililisoup.hotdognalds.item;

import dev.chililisoup.hotdognalds.entity.Hotdog;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import dev.chililisoup.hotdognalds.reg.ModEntityTypes;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class HotdogItem extends Item {
    public HotdogItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(final UseOnContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace == Direction.DOWN) return InteractionResult.FAIL;

        Level level = context.getLevel();
        Vec3 pos = context.getClickLocation();
        AABB box = ModEntityTypes.HOTDOG.getDimensions().makeBoundingBox(pos.x(), pos.y(), pos.z());

        if (!level.noCollision(null, box) || !level.getEntities(null, box).isEmpty())
            return InteractionResult.FAIL;

        ItemStack itemStack = context.getItemInHand();
        if (level instanceof ServerLevel serverLevel) {
            Hotdog hotdog = Hotdog.create(
                    serverLevel,
                    pos,
                    context.getRotation(),
                    EntitySpawnReason.SPAWN_ITEM_USE,
                    itemStack,
                    context.getPlayer()
            );
            if (hotdog == null) return InteractionResult.FAIL;
            serverLevel.addFreshEntityWithPassengers(hotdog);
        }

        itemStack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack itemStack) {
        float cookAmt = itemStack.getOrDefault(ModComponents.COOK_AMOUNT, 0F);
        Component prefix;
        if (cookAmt <= 0F) prefix = Component.translatable("item.hotdognalds.hotdog.raw");
        else if (cookAmt < 1F) prefix = Component.translatable("item.hotdognalds.hotdog.rare");
        else if (cookAmt <= 2F) prefix = Component.translatable("item.hotdognalds.hotdog.cooked");
        else if (cookAmt < 3F) prefix = Component.translatable("item.hotdognalds.hotdog.burnt");
        else prefix = Component.translatable("item.hotdognalds.hotdog.congratulation");

        return Component.translatable("item.hotdognalds.hotdog", prefix);
    }
}
