package dev.chililisoup.hotdognalds.reg;

import dev.chililisoup.hotdognalds.item.HotdogContents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.core.cauldron.CauldronInteractions.WATER;

public final class ModCauldronInteractions {
    public static void bootstrap() {
        WATER.put(ModItems.HOTDOG, ModCauldronInteractions::hotdogInteraction);
    }

    private static InteractionResult hotdogInteraction(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack itemInHand
    ) {
        HotdogContents contents = itemInHand.getOrDefault(ModComponents.HOTDOG_CONTENTS, HotdogContents.DOG);
        if (contents.sauce().isEmpty())
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (!level.isClientSide()) {
            ItemStack wipedHotdog = itemInHand.copyWithCount(1);
            wipedHotdog.set(ModComponents.HOTDOG_CONTENTS, contents.toMutable().takeSauce().toImmutable());
            player.setItemInHand(hand, ItemUtils.createFilledResult(itemInHand, player, wipedHotdog, false));
            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
        }

        return InteractionResult.SUCCESS;
    }
}
