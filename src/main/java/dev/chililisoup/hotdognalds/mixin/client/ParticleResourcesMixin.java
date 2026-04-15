package dev.chililisoup.hotdognalds.mixin.client;

import dev.chililisoup.hotdognalds.client.particle.ColoredFallAndLandParticle;
import dev.chililisoup.hotdognalds.reg.ModParticles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ParticleResources.class)
public abstract class ParticleResourcesMixin {
    @Shadow protected abstract <T extends ParticleOptions> void register(ParticleType<T> type, ParticleResources.SpriteParticleRegistration<T> provider);

    @Inject(method = "registerProviders", at = @At("TAIL"))
    private void registerModProviders(CallbackInfo ci) {
        this.register(ModParticles.COLORED_FALL, ColoredFallAndLandParticle.ColoredFallProvider::new);
        this.register(ModParticles.COLORED_LAND, ColoredFallAndLandParticle.ColoredLandProvider::new);
    }
}
