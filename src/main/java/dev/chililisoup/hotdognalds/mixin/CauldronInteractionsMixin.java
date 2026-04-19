package dev.chililisoup.hotdognalds.mixin;

import dev.chililisoup.hotdognalds.reg.ModCauldronInteractions;
import net.minecraft.core.cauldron.CauldronInteractions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CauldronInteractions.class)
public abstract class CauldronInteractionsMixin {
    @Inject(method = "bootStrap", at = @At("TAIL"))
    private static void registerModInteractions(CallbackInfo ci) {
        ModCauldronInteractions.bootstrap();
    }
}
