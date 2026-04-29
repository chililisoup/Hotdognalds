package dev.chililisoup.hotdognalds.reg;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.hotdognalds.Hotdognalds;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Function;

public final class ModParticles {
    public static final ParticleType<ColorParticleOption> COLORED_FALL = register(
            "colored_fall", ColorParticleOption::codec, ColorParticleOption::streamCodec
    );
    public static final ParticleType<ColorParticleOption> COLORED_LAND = register(
            "colored_land", ColorParticleOption::codec, ColorParticleOption::streamCodec
    );

    private static <T extends ParticleType<?>> T register(String name, T particleType) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, Hotdognalds.id(name), particleType);
    }

    private static SimpleParticleType register(String name) {
        return register(name, FabricParticleTypes.simple());
    }

    private static <T extends ParticleOptions> ParticleType<T> register(
            String name,
            Function<ParticleType<T>, MapCodec<T>> codec,
            Function<ParticleType<T>, StreamCodec<? super RegistryFriendlyByteBuf, T>> streamCodec
    ) {
        return register(name, FabricParticleTypes.complex(codec, streamCodec));
    }

    public static void init() {}
}
