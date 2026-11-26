package dev.nathanpb.dml.item.battery

import dev.nathanpb.dml.data.tag.customDataTag
import dev.nathanpb.dml.data.tag.setCustomDataTag
import dev.nathanpb.dml.utils.RenderUtils.Companion.ENERGY_STYLE
import dev.nathanpb.dml.utils.distributeEnergyToInventory
import dev.nathanpb.dml.utils.getBooleanInfoText
import dev.nathanpb.dml.utils.getEnergyTooltipText
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.level.Level
import team.reborn.energy.api.base.SimpleEnergyItem
import java.awt.Color
import java.util.function.Consumer

abstract class AbstractItemBattery(properties: Properties): Item(properties.stacksTo(1)), SimpleEnergyItem {

    abstract fun isPristineEnergy(): Boolean /** true on modular-armor only! */

    abstract fun getColor(): Color

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResult {
        if(world.isClientSide()) return InteractionResult.PASS
        if(user.isCrouching) {
            val stack = user.getItemInHand(hand)
            val hasDistributeEnergy = hasDistributeEnergy(stack)

            stack.setCustomDataTag(stack.customDataTag.also { it.putBoolean(distributeEnergyKey, !hasDistributeEnergy) })
            (user as ServerPlayer).connection.send(
                ClientboundSoundPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(
                        if(hasDistributeEnergy) SoundEvents.IRON_TRAPDOOR_CLOSE else SoundEvents.IRON_TRAPDOOR_OPEN
                    ),
                    SoundSource.PLAYERS,
                    user.x, user.y, user.z,
                    1.0f, 1.0f,
                    user.blockPosition().asLong()
                )
            )
            user.sendOverlayMessage(getEnabledText(stack))
            return InteractionResult.SUCCESS_SERVER
        }
        return super.use(world, user, hand)
    }

    override fun inventoryTick(stack: ItemStack, world: ServerLevel, entity: Entity, slot: EquipmentSlot?) {
        if(entity !is Player) return
        if(!hasDistributeEnergy(stack)) return

        distributeEnergyToInventory(
            entity,
            stack,
            getEnergyMaxOutput(stack),
            { predicateStack -> predicateStack.item !is AbstractItemBattery },
            isPristineEnergy()
        )
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        display: TooltipDisplay,
        tooltip: Consumer<Component>,
        flag: TooltipFlag
    ) {
        tooltip.accept(getEnergyTooltipText(stack, primaryStyle, secondaryStyle, isPristineEnergy()))
        tooltip.accept(getEnabledText(stack))
    }

    override fun isFoil(stack: ItemStack): Boolean {
        return getStoredEnergy(stack) >= getEnergyCapacity(stack)
    }

    fun getScaledColor(stack: ItemStack): Int {
        val hsbColor = Color.RGBtoHSB(getColor().red, getColor().green, getColor().blue, null)
        val energyPercentage: Float = ((getStoredEnergy(stack).toFloat() / getEnergyCapacity(stack).toFloat()))

        hsbColor[1] = hsbColor[1] * energyPercentage

        return Color.HSBtoRGB(hsbColor[0], hsbColor[1], hsbColor[2])
    }

    open val primaryStyle: Style = ENERGY_STYLE
    open val secondaryStyle: Style = Style.EMPTY.applyFormats(ChatFormatting.YELLOW)


    private fun getEnabledText(stack: ItemStack): Component {
        val enabledText = Component.translatable("tooltip.dml-refabricated.distribute_energy")
        return getBooleanInfoText(enabledText, hasDistributeEnergy(stack), primaryStyle, secondaryStyle)
    }

    private fun hasDistributeEnergy(stack: ItemStack): Boolean {
        return stack.customDataTag.getBooleanOr(distributeEnergyKey, false)
    }

    private val distributeEnergyKey = "dml-refabricated:distribute_energy"

}
