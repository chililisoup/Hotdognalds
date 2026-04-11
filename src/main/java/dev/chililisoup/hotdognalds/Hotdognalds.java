package dev.chililisoup.hotdognalds;

import dev.chililisoup.hotdognalds.reg.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Hotdognalds implements ModInitializer {
    public static final String MOD_ID = "hotdognalds";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModCreativeTabs.init();
        ModBlocks.init();
        ModItems.init();
        ModEntityTypes.init();
    }
}
