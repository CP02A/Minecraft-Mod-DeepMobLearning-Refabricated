package dev.nathanpb.dml.simulacrum.item

import dev.nathanpb.dml.utils.RenderUtils
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.level.Level
import java.util.function.Consumer
import kotlin.random.Random.Default.nextFloat

class ItemMatter(properties: Properties, private val experience: Int) : Item(properties) {

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResult {
        val stack = user.getItemInHand(hand)
        if (!world.isClientSide) {
            var count = 1
            if(user.isCrouching) {
                count = stack.count
                stack.shrink(count)
            } else {
                stack.shrink(1)
                world.playSound(null, user.x, user.y, user.z, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.1f, nextFloat())
            }
            user.giveExperiencePoints(count * experience)
        }
        return InteractionResult.CONSUME
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        display: TooltipDisplay,
        tooltip: Consumer<Component>,
        flag: TooltipFlag
    ) {
        tooltip.accept(
            Component.translatable("tooltip.dml-refabricated.matter.1").setStyle(RenderUtils.STYLE).append(
            Component.translatable("tooltip.dml-refabricated.matter.2").setStyle(RenderUtils.ALT_STYLE))
        )
        tooltip.accept(
            Component.translatable("tooltip.dml-refabricated.matter.3").setStyle(RenderUtils.STYLE).append(
            Component.translatable("tooltip.dml-refabricated.matter.4").setStyle(RenderUtils.ALT_STYLE))
        )
        tooltip.accept(
            Component.translatable("tooltip.dml-refabricated.matter.5").setStyle(RenderUtils.STYLE).append(
            Component.literal(experience.toString()).setStyle(RenderUtils.ALT_STYLE))
        )
    }
}
