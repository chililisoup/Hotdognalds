package dev.chililisoup.hotdognalds.client.reg;

import dev.chililisoup.hotdognalds.client.particle.ColoredFallAndLandParticle;
import dev.chililisoup.hotdognalds.reg.ModParticles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

@Environment(EnvType.CLIENT)
public final class ModParticleProviders {
    public static void registerProviders(ParticleProviderConsumer consumer) {
        consumer.accept(ModParticles.COLORED_FALL, ColoredFallAndLandParticle.ColoredFallProvider::new);
        consumer.accept(ModParticles.COLORED_LAND, ColoredFallAndLandParticle.ColoredLandParticle.ColoredLandProvider::new);
    }

    @FunctionalInterface
    public interface ParticleProviderConsumer {
        <T extends ParticleOptions> void accept(
                ParticleType<T> type,
                ParticleResources.SpriteParticleRegistration<T> provider
        );
    }
}
