package dev.chililisoup.hotdognalds.client.particle;

import dev.chililisoup.hotdognalds.reg.ModParticles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class ColoredFallAndLandParticle extends DripParticle.FallAndLandParticle {
    private static final float GRAVITY = 0.02F;

    public ColoredFallAndLandParticle(
            ColorParticleOption options,
            ClientLevel level,
            double x,
            double y,
            double z,
            double xAux,
            double yAux,
            double zAux,
            TextureAtlasSprite sprite
    ) {
        super(level, x, y, z, Fluids.EMPTY, ColorParticleOption.create(
                ModParticles.COLORED_LAND,
                ARGB.colorFromFloat(options.getAlpha(), options.getRed(), options.getGreen(), options.getBlue())
        ), sprite);

        this.xd = xAux + 0.005F * (this.random.nextFloat() - 0.5F);
        this.yd = yAux + 0.002F * (this.random.nextFloat() - 0.5F);
        this.zd = zAux + 0.005F * (this.random.nextFloat() - 0.5F);
    }

    @Override
    public @NotNull SingleQuadParticle.Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    protected void postMoveUpdate() {
        if (!this.onGround) return;

        this.remove();
        this.level.addParticle(this.landParticle, this.x, this.y, this.z, this.rCol, this.gCol, this.bCol);
        float volume = Mth.randomBetween(this.random, 0.3F, 1.0F);
        this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.BEEHIVE_DRIP, SoundSource.BLOCKS, volume, 1.0F, false);
    }

    public static class ColoredFallProvider implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet sprite;

        public ColoredFallProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(
                @NotNull ColorParticleOption options,
                @NotNull ClientLevel level,
                double x,
                double y,
                double z,
                double xAux,
                double yAux,
                double zAux,
                @NotNull RandomSource random
        ) {
            ColoredFallAndLandParticle particle = new ColoredFallAndLandParticle(
                    options, level, x, y, z, xAux, yAux, zAux, this.sprite.get(random)
            );
            particle.gravity = GRAVITY;
            particle.setColor(options.getRed(), options.getGreen(), options.getBlue());
            particle.setAlpha(options.getAlpha());
            return particle;
        }
    }

    public static class ColoredLandParticle extends DripParticle.DripLandParticle {
        public ColoredLandParticle(
                ClientLevel level,
                double x,
                double y,
                double z,
                TextureAtlasSprite sprite
        ) {
            super(level, x, y, z, Fluids.EMPTY, sprite);
        }

        @Override
        public @NotNull SingleQuadParticle.Layer getLayer() {
            return Layer.TRANSLUCENT;
        }

        public static class ColoredLandProvider implements ParticleProvider<ColorParticleOption> {
            private final SpriteSet sprite;

            public ColoredLandProvider(final SpriteSet sprite) {
                this.sprite = sprite;
            }

            @Override
            public Particle createParticle(
                    @NotNull ColorParticleOption options,
                    @NotNull ClientLevel level,
                    double x,
                    double y,
                    double z,
                    double xAux,
                    double yAux,
                    double zAux,
                    @NotNull RandomSource random
            ) {
                ColoredLandParticle particle = new ColoredLandParticle(
                        level, x, y, z, this.sprite.get(random)
                );
                particle.gravity = GRAVITY;
                particle.setLifetime((int) (32.0 / (random.nextFloat() * 0.8 + 0.2)));
                particle.setColor(options.getRed(), options.getGreen(), options.getBlue());
                particle.setAlpha(options.getAlpha());
                return particle;
            }
        }
    }
}
