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
import dev.nathanpb.dml.data.dataModel
import dev.nathanpb.dml.enums.DataModelTier
import dev.nathanpb.dml.enums.EntityCategory
import dev.nathanpb.dml.utils.getInfoText
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.level.Level
import java.util.function.Consumer

class ItemDataModel(properties: Properties, val category: EntityCategory? = null) : Item(properties.stacksTo(1).fireResistant()) {

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        display: TooltipDisplay,
        tooltip: Consumer<Component>,
        flag: TooltipFlag
    ) {
        if(category == null) return
        stack.dataModel.let { data ->
            if(!data.tier().isMaxTier()) {
                tooltip.accept(getInfoText(
                    Component.translatable(
                            "tooltip.${MOD_ID}.data_amount.1"
                    ),
                    Component.translatable(
                        "tooltip.${MOD_ID}.data_amount.2",
                        data.dataAmount, data.tier().nextTierOrCurrent().dataAmount
                    )
                ))
            }

            tooltip.accept(getInfoText(
                Component.translatable(
                    "tooltip.${MOD_ID}.tier.1"
                ),
                Component.translatable(
                    "tooltip.${MOD_ID}.tier.2",
                    data.tier().text
                )
            ))

            if(flag.isCreative) {
                tooltip.accept(Component.translatable("tooltip.${MOD_ID}.cheat").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
            }
        }
    }

    // cheat/debug
    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResult {
        if(world.isClientSide) return InteractionResult.PASS
        if(!user.isCreative || !user.isCrouching) return InteractionResult.PASS

        val stack = user.getItemInHand(hand)
        if(stack.item is ItemDataModel) {
            val tier = stack.dataModel.tier()
            stack.dataModel.dataAmount = if(!tier.isMaxTier()) tier.nextTierOrCurrent().dataAmount else DataModelTier.FAULTY.dataAmount
            user.sendOverlayMessage(stack.dataModel.tier().text)
            return InteractionResult.SUCCESS_SERVER
        }
        return InteractionResult.PASS
    }
}
