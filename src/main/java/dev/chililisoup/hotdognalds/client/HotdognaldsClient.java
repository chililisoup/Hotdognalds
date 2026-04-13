package dev.chililisoup.hotdognalds.client;

import dev.chililisoup.hotdognalds.client.reg.ModEntityRenderers;
import dev.chililisoup.hotdognalds.client.reg.ModSpecialRenderers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class HotdognaldsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModEntityRenderers.init();
        ModSpecialRenderers.init();
    }
}
