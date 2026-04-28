package dev.chililisoup.hotdognalds.reg;

import dev.chililisoup.hotdognalds.Hotdognalds;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ModParticles {
    public static final SimpleParticleType COLORED_FALL = register("colored_fall");
    public static final SimpleParticleType COLORED_LAND = register("colored_land");

    private static SimpleParticleType register(String name) {
        return Registry.register(
                BuiltInRegistries.PARTICLE_TYPE, Hotdognalds.id(name), new SimpleParticleType(false)
        );
    }

    public static void addColoredFall(Level level, Vec3 pos, int color) {
        level.addParticle(
                COLORED_FALL,
                pos.x,
                pos.y,
                pos.z,
                ARGB.redFloat(color),
                ARGB.greenFloat(color),
                ARGB.blueFloat(color)
        );
    }

    public static void init() {}
}
