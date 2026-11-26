/*
 * Copyright (C) 2020 Nathan P. Bombana, IterationFunk
 *
 * This file is part of Deep Mob Learning: Refabricated.
 *
 * Deep Mob Learning: Refabricated is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Deep Mob Learning: Refabricated is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Deep Mob Learning: Refabricated.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.nathanpb.dml.item

import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.data.trialKeyData
import dev.nathanpb.dml.utils.RenderUtils
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import java.util.function.Consumer

class ItemTrialKey(properties: Properties) : Item(properties.stacksTo(1).fireResistant()) {
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        display: TooltipDisplay,
        tooltip: Consumer<Component>,
        flag: TooltipFlag
    ) {
        val data = stack.trialKeyData
        if (data != null) {
            tooltip.accept(
                Component.translatable("tooltip.${MOD_ID}.data_model.bound_to.1").setStyle(RenderUtils.STYLE).append(
                    Component.translatable("tooltip.${MOD_ID}.data_model.bound_to.2", data.category.displayName).setStyle(RenderUtils.ALT_STYLE)
                )
            )
            tooltip.accept(
                Component.translatable("tooltip.${MOD_ID}.tier.1").setStyle(RenderUtils.STYLE).append(
                    Component.translatable("tooltip.${MOD_ID}.tier.2", data.tier().text)
                )
            )

            if (data.affixes.isNotEmpty()) {
                tooltip.accept(Component.translatable("tooltip.${MOD_ID}.trial_key.affixes").setStyle(RenderUtils.STYLE))
                data.affixes.forEach {
                    tooltip.accept(Component.literal(" - ").append(it.name))
                }
            }
        } else {
            tooltip.accept(Component.translatable("tooltip.${MOD_ID}.data_model.unbound").withStyle(ChatFormatting.DARK_RED))
            tooltip.accept(Component.translatable("tooltip.${MOD_ID}.trial_key.unbound.tip").setStyle(RenderUtils.STYLE))
        }
    }
}
