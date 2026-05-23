package dev.chililisoup.hotdognalds.reg;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import dev.chililisoup.hotdognalds.Hotdognalds;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.*;

import java.util.function.ToIntFunction;

public final class ModGameRules {
    public static final GameRule<Integer> HOTDOG_COOK_RATE = registerInteger(
            "hotdog_cook_rate", GameRuleCategory.MISC, 100, 0, 100000
    );

    private static <T> GameRule<T> register(
            String name,
            GameRuleCategory category,
            GameRuleType typeHint,
            ArgumentType<T> argumentType,
            Codec<T> codec,
            T defaultValue,
            GameRules.VisitorCaller<T> visitorCaller,
            ToIntFunction<T> commandResultFunction
    ) {
        return Registry.register(
                BuiltInRegistries.GAME_RULE,
                Hotdognalds.id(name),
                new GameRule<>(category, typeHint, argumentType, visitorCaller, codec, commandResultFunction, defaultValue, FeatureFlagSet.of())
        );
    }

    private static GameRule<Integer> registerInteger(
            String name, GameRuleCategory category, int defaultValue, int min, int max
    ) {
        return register(
                name,
                category,
                GameRuleType.INT,
                IntegerArgumentType.integer(min, max),
                Codec.intRange(min, max),
                defaultValue,
                GameRuleTypeVisitor::visitInteger,
                i -> i
        );
    }

    public static void init() {}
}
