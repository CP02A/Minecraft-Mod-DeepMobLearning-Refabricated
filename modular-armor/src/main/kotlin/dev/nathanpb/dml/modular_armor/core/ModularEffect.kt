/*
 *
 *  Copyright (C) 2021 Nathan P. Bombana, IterationFunk
 *
 *  This file is part of Deep Mob Learning: Refabricated.
 *
 *  Deep Mob Learning: Refabricated is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Deep Mob Learning: Refabricated is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Deep Mob Learning: Refabricated.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.nathanpb.dml.modular_armor.core

import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.enums.DataModelTier
import dev.nathanpb.dml.enums.EntityCategory
import dev.nathanpb.dml.modular_armor.ItemModularGlitchArmor
import dev.nathanpb.dml.modular_armor.data.ModularArmorData
import dev.nathanpb.dml.utils.RenderUtils
import dev.nathanpb.dml.utils.RenderUtils.Companion.STYLE
import dev.nathanpb.dml.utils.getPipeText
import net.minecraft.world.entity.ai.attributes.RangedAttribute
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.world.InteractionResult
import net.minecraft.ChatFormatting
import net.minecraft.resources.Identifier
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.ItemAttributeModifiers
import org.jetbrains.annotations.ApiStatus

abstract class ModularEffect<T: ModularEffectTriggerPayload>(
    val id: Identifier,
    val category: EntityCategory,
    val applyCost: ()->Long
) {

    var modIdFormat = "modulareffect.${MOD_ID}.${id.path}"
    var otherModIdFormat = "modulareffect.${MOD_ID}.${id.namespace}.${id.path}"

    val isEnabled = {
        applyCost() >= 0L
    }

    open val name = if(id.namespace == MOD_ID) {
        Component.translatable(modIdFormat)
    } else {
        Component.translatable(otherModIdFormat)
    }
    open val description = if(id.namespace == MOD_ID) {
        Component.translatable("$modIdFormat.description")
    } else {
        Component.translatable("$otherModIdFormat.description")
    }


    lateinit var entityAttribute: Holder<Attribute>

    @ApiStatus.OverrideOnly
    @ApiStatus.Internal
    abstract fun registerEvents()
    abstract fun acceptTier(tier: DataModelTier): Boolean

    /* Effect Info stuff - used only for display */
    abstract fun minimumTier(): DataModelTier
    open fun isScaled(): Boolean = false // true on effects that use the data model's tier ordinal value for odds or increased effects
    open fun getEnergyConsumptionType(): EffectInfo.EnergyConsumptionType = EffectInfo.EnergyConsumptionType.SECOND
    fun getEffectInfo(): EffectInfo {
        return EffectInfo(
            minimumTier(),
            isScaled(),
            applyCost(),
            getEnergyConsumptionType()
        )
    }

    @ApiStatus.OverrideOnly
    protected open fun createEntityAttribute(): Attribute {
        return RangedAttribute((name.contents as TranslatableContents).key, 0.0, 0.0, DataModelTier.values().size.dec() * 4.0)
            .setSyncable(true)
    }

    open fun createEntityAttributeModifier(armor: ModularArmorData): AttributeModifier {
        val level = DataModelTier.values().count {
            acceptTier(it) && it <= armor.tier()
        }
        return AttributeModifier(modifierId(armor), level.toDouble(), AttributeModifier.Operation.ADD_VALUE)
    }

    /**
     * Since 1.21 modifiers have ids instead of UUIDs, and modifiers with the same id replace each other: each armor
     * piece gets its own id so the pieces still add up.
     */
    protected fun modifierId(armor: ModularArmorData): Identifier {
        val slot = (armor.stack.item as? ItemModularGlitchArmor)?.slot?.getSerializedName() ?: "any"
        return id.withSuffix("_$slot")
    }

    @ApiStatus.Internal
    fun registerEntityAttribute() {
        entityAttribute = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, id, createEntityAttribute())
    }

    protected open fun canApply(context: ModularEffectContext, payload: T): Boolean {
        return isEnabled()
            && context.dataModel.category == category
            && acceptTier(context.tier)
            && context.armor.pristineEnergy >= applyCost()
            && id !in context.armor.disabledEffects
    }

    private fun attemptToConsumeEnergy(context: ModularEffectContext) {
        if(shouldConsumeEnergy(context)) {
            context.armor.pristineEnergy -= applyCost()
        }
    }

    fun <R>attemptToApply(context: ModularEffectContext, payload: T, body: (ModularEffectContext, T) -> R): EffectResult<R> {
        if(canApply(context, payload)) {
            attemptToConsumeEnergy(context)
            return EffectResult(InteractionResult.SUCCESS, body(context, payload))
        }
        return EffectResult(InteractionResult.FAIL, null)
    }

    fun attemptToApply(context: ModularEffectContext, payload: T): InteractionResult {
        return attemptToApply(context, payload) { _, _ -> }.result
    }

    open fun shouldConsumeEnergy(context: ModularEffectContext) = true

    fun sumLevelsOf(stack: ItemStack): Double {
        if (stack.item !is ItemModularGlitchArmor) return 0.0
        return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers()
            .filter { it.attribute() == entityAttribute }
            .sumOf { it.modifier().amount() }
    }

    fun sumLevelsOf(stacks: List<ItemStack>): Double {
        return stacks.sumOf(this::sumLevelsOf)
    }


    data class EffectInfo(
        val minimumTier: DataModelTier,
        val scaled: Boolean,
        val energyCost: Long,
        val energyConsumptionType: EnergyConsumptionType
    ) {

        fun text(): Component {
            val tierText = if(!scaled) {
                minimumTier.text
            } else {
                Component.translatable(
                    "tooltip.${MOD_ID}.data_tier_scales"
                ).withStyle(ChatFormatting.YELLOW)
            }

            val energyConsumptionText = if(energyCost > 0) {
                Component.translatable(
                    "tooltip.${MOD_ID}.data_amount.2",
                    energyCost,
                    energyConsumptionType.text
                )
            } else {
                Component.translatable("tooltip.${MOD_ID}.free")
            }.also {
                it.style = RenderUtils.ALT_STYLE
            }

            return getPipeText(
                tierText,
                energyConsumptionText,
                STYLE
            )
        }


        enum class EnergyConsumptionType(val text: Component) {

            SECOND(Component.translatable("energyConsumption.${MOD_ID}.second")),
            USE(Component.translatable("energyConsumption.${MOD_ID}.use"))

        }
    }
}

/** The outcome of [ModularEffect.attemptToApply] (InteractionResultHolder no longer exists). */
data class EffectResult<R>(val result: InteractionResult, val value: R?)
