package dev.chililisoup.hotdognalds.client.particle;

import dev.chililisoup.hotdognalds.reg.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

public class ColoredFallAndLandParticle extends DripParticle.FallAndLandParticle {
    public ColoredFallAndLandParticle(
            final ClientLevel level,
            final double x,
            final double y,
            final double z,
            final TextureAtlasSprite sprite
    ) {
        super(level, x, y, z, Fluids.EMPTY, ModParticles.COLORED_LAND, sprite);
    }

    @Override
    protected void postMoveUpdate() {
        if (this.onGround) {
            this.remove();
            this.level.addParticle(this.landParticle, this.x, this.y, this.z, this.rCol, this.gCol, this.bCol);
            float volume = Mth.randomBetween(this.random, 0.3F, 1.0F);
            this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.BEEHIVE_DRIP, SoundSource.BLOCKS, volume, 1.0F, false);
        }
    }

    public static class ColoredFallProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public ColoredFallProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(
                @NotNull SimpleParticleType options,
                @NotNull ClientLevel level,
                double x,
                double y,
                double z,
                double r,
                double g,
                double b,
                @NotNull RandomSource random
        ) {
            DripParticle particle = new ColoredFallAndLandParticle(
                    level, x, y, z, this.sprite.get(random)
            );
            particle.gravity = 0.01F;
            particle.setColor((float) r, (float) g, (float) b);
            return particle;
        }
    }

    public static class ColoredLandProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public ColoredLandProvider(final SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(
                @NotNull SimpleParticleType options,
                @NotNull ClientLevel level,
                double x,
                double y,
                double z,
                double r,
                double g,
                double b,
                @NotNull RandomSource random
        ) {
            DripParticle particle = new DripParticle.DripLandParticle(level, x, y, z, Fluids.EMPTY, this.sprite.get(random));
            particle.setLifetime((int) (128.0 / (random.nextFloat() * 0.8 + 0.2)));
            particle.setColor((float) r, (float) g, (float) b);
            return particle;
        }
    }
}
