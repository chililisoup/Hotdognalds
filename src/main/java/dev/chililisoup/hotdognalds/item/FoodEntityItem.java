package dev.chililisoup.hotdognalds.item;

import dev.chililisoup.hotdognalds.entity.FoodEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;

public abstract class FoodEntityItem<T extends FoodEntity> extends SpawnItem<T> {
    public FoodEntityItem(Properties properties, EntityType<T> entityType) {
        super(properties, entityType, FoodEntity::create, EntitySpawnReason.TRIGGERED);
    }
}
