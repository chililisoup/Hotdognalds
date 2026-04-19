package dev.chililisoup.hotdognalds.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpawnItem<T extends Entity> extends Item {
    private final EntityType<T> entityType;
    private final EntityCreator<T> entityCreator;

    public SpawnItem(Properties properties, EntityType<T> entityType, EntityCreator<T> entityCreator) {
        super(properties);
        this.entityType = entityType;
        this.entityCreator = entityCreator;
    }

    @Override
    public @NotNull InteractionResult useOn(final UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) return InteractionResult.PASS;

        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        if (level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty())
            return InteractionResult.PASS;

        Vec3 pos = context.getClickLocation();
        AABB box = this.entityType.getDimensions().makeBoundingBox(pos.x(), pos.y(), pos.z());

        if (!level.noCollision(null, box) || !level.getEntities(null, box).isEmpty())
            return InteractionResult.PASS;

        ItemStack itemStack = context.getItemInHand();
        if (level instanceof ServerLevel serverLevel) {
            T entity = this.entityCreator.create(
                    serverLevel,
                    pos,
                    context.getRotation() + 180F,
                    EntitySpawnReason.SPAWN_ITEM_USE,
                    itemStack,
                    context.getPlayer()
            );
            if (entity == null) return InteractionResult.FAIL;
            serverLevel.addFreshEntityWithPassengers(entity);
        }

        itemStack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    public interface EntityCreator<T extends Entity> {
        T create(
                ServerLevel serverLevel,
                Vec3 position,
                float rotation,
                EntitySpawnReason entitySpawnReason,
                ItemStack itemStack,
                @Nullable Player player
        );
    }
}
