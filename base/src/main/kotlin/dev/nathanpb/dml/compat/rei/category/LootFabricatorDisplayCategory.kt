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

package dev.nathanpb.dml.compat.rei.category

import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import net.minecraft.world.entity.EntitySpawnReason
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import dev.nathanpb.dml.block.BLOCK_LOOT_FABRICATOR
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import dev.nathanpb.dml.compat.rei.ReiCategories
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import dev.nathanpb.dml.compat.rei.display.LootFabricatorRecipeDisplay
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import dev.nathanpb.dml.compat.rei.widgets.EntityDisplayWidget
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import me.shedaniel.math.Point
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import me.shedaniel.math.Rectangle
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import me.shedaniel.rei.api.client.gui.widgets.Widget
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import me.shedaniel.rei.api.common.entry.EntryStack
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import me.shedaniel.rei.api.common.util.EntryStacks
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import net.minecraft.client.Minecraft
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import net.minecraft.world.entity.EntityType
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import net.minecraft.world.entity.LivingEntity
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import net.minecraft.world.item.ItemStack
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import net.minecraft.core.registries.BuiltInRegistries
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import net.minecraft.network.chat.MutableComponent
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import net.minecraft.network.chat.Component
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import net.minecraft.resources.Identifier

class LootFabricatorDisplayCategory: DisplayCategory<LootFabricatorRecipeDisplay> {

    override fun getIdentifier(): Identifier = ReiCategories.LOOT_FABRICATOR_CATEGORY.identifier

    override fun getCategoryIdentifier(): CategoryIdentifier<out LootFabricatorRecipeDisplay> = ReiCategories.LOOT_FABRICATOR_CATEGORY

    override fun getIcon(): EntryStack<ItemStack> = EntryStacks.of(BLOCK_LOOT_FABRICATOR)

    override fun getTitle(): MutableComponent = Component.translatable("rei.$MOD_ID.category.loot_fabricator")


    override fun setupDisplay(recipeDisplay: LootFabricatorRecipeDisplay, bounds: Rectangle): MutableList<Widget> {
        val centerX = bounds.centerX - 8
        val centerY = bounds.centerY - 8

        val entities: MutableList<EntityType<*>> = arrayListOf()
        BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(recipeDisplay.recipe.category.tagKey).forEach {
            entities.add(it.value())
        }

        return mutableListOf<Widget>(
            Widgets.createRecipeBase(bounds),
            Widgets.createSlot(Point(centerX - 40, centerY)).entries(recipeDisplay.inputEntries.flatten()),
            Widgets.createArrow(Point(centerX - 10, centerY))
        ).also { widgets ->
            Minecraft.getInstance().player?.let { player ->
                widgets += Widgets.wrapVanillaWidget(EntityDisplayWidget(
                    entities.mapNotNull { createShowcaseEntity(it, player.level()) },
                    centerX + 40,
                    centerY + 30,
                    0F, 0F, 24
                ))
            }
        }
    }
}
