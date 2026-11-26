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

package dev.nathanpb.dml.compat.jei

import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.block.BLOCK_LOOT_FABRICATOR
import dev.nathanpb.dml.block.BLOCK_TRIAL_KEYSTONE
import dev.nathanpb.dml.compat.rei.widgets.EntityDisplayWidget
import dev.nathanpb.dml.data.TrialKeyData
import dev.nathanpb.dml.entity.SYSTEM_GLITCH_ENTITY_TYPE
import dev.nathanpb.dml.entity.SystemGlitchEntity
import dev.nathanpb.dml.item.ITEM_GLITCH_UPGRADE_SMITHING_TEMPLATE
import dev.nathanpb.dml.item.ITEM_SOOT_REDSTONE
import dev.nathanpb.dml.item.ITEM_TRIAL_KEY
import dev.nathanpb.dml.recipe.CrushingRecipe
import dev.nathanpb.dml.recipe.LootFabricatorRecipe
import dev.nathanpb.dml.recipe.RECIPE_CRUSHING
import dev.nathanpb.dml.recipe.RECIPE_LOOT_FABRICATOR
import dev.nathanpb.dml.recipe.RECIPE_TRIAL_KEYSTONE
import dev.nathanpb.dml.recipe.TrialKeystoneRecipe
import dev.nathanpb.dml.screen.handler.widget.createShowcaseEntity
import dev.nathanpb.dml.utils.RenderUtils
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder
import mezz.jei.api.gui.widgets.IRecipeWidget
import mezz.jei.api.helpers.IGuiHelper
import mezz.jei.api.recipe.IFocusGroup
import mezz.jei.api.recipe.RecipeIngredientRole
import mezz.jei.api.recipe.category.AbstractRecipeCategory
import mezz.jei.api.recipe.types.IRecipeHolderType
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenPosition
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.RecipeHolder

/** The JEI recipe types: the mod's recipe types (the recipes are synchronized from the server). */
object JeiRecipeTypes {
    val CRUSHING: IRecipeHolderType<CrushingRecipe> by lazy { IRecipeHolderType.create(RECIPE_CRUSHING) }
    val LOOT_FABRICATOR: IRecipeHolderType<LootFabricatorRecipe> by lazy { IRecipeHolderType.create(RECIPE_LOOT_FABRICATOR) }
    val TRIAL: IRecipeHolderType<TrialKeystoneRecipe> by lazy { IRecipeHolderType.create(RECIPE_TRIAL_KEYSTONE) }
}

/** Draws an [EntityDisplayWidget] whose position is relative to the recipe (JEI translates the widgets). */
private class EntityWidget(private val display: EntityDisplayWidget) : IRecipeWidget {
    override fun getPosition() = ScreenPosition(0, 0)

    override fun drawWidget(graphics: GuiGraphicsExtractor, mouseX: Double, mouseY: Double) {
        // GUI entities are placed in screen coordinates: the pose holds where JEI draws the recipe
        val pose = graphics.pose()
        display.extract(graphics, pose.m20().toInt(), pose.m21().toInt())
    }
}

/** Hitting [CrushingRecipe.input] on [CrushingRecipe.block] makes [CrushingRecipe.output]. */
class CrushingCategory(gui: IGuiHelper) : AbstractRecipeCategory<RecipeHolder<CrushingRecipe>>(
    JeiRecipeTypes.CRUSHING,
    Component.translatable("rei.$MOD_ID.category.crushing"),
    gui.createDrawableItemLike(ITEM_SOOT_REDSTONE),
    116, 26
) {
    override fun setRecipe(builder: IRecipeLayoutBuilder, holder: RecipeHolder<CrushingRecipe>, focuses: IFocusGroup) {
        val recipe = holder.value()
        builder.addInputSlot(1, 5).setStandardSlotBackground().add(recipe.input)
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 27, 5).add(recipe.block)
        builder.addOutputSlot(95, 5).setOutputSlotBackground().add(recipe.output)
    }

    override fun createRecipeExtras(builder: IRecipeExtrasBuilder, holder: RecipeHolder<CrushingRecipe>, focuses: IFocusGroup) {
        builder.addRecipeArrow().setPosition(57, 4)
    }
}

/** The loot fabricator turns the pristine matter of a category into loot of the category's mobs (shown cycling). */
class LootFabricatorCategory(gui: IGuiHelper) : AbstractRecipeCategory<RecipeHolder<LootFabricatorRecipe>>(
    JeiRecipeTypes.LOOT_FABRICATOR,
    Component.translatable("rei.$MOD_ID.category.loot_fabricator"),
    gui.createDrawableItemLike(BLOCK_LOOT_FABRICATOR),
    120, 60
) {
    override fun setRecipe(builder: IRecipeLayoutBuilder, holder: RecipeHolder<LootFabricatorRecipe>, focuses: IFocusGroup) {
        builder.addInputSlot(10, 22).setStandardSlotBackground().add(holder.value().input)
    }

    override fun createRecipeExtras(builder: IRecipeExtrasBuilder, holder: RecipeHolder<LootFabricatorRecipe>, focuses: IFocusGroup) {
        builder.addRecipeArrow().setPosition(38, 21)
        val level = Minecraft.getInstance().level ?: return
        val entities = BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(holder.value().category.tagKey)
            .mapNotNull { createShowcaseEntity(it.value(), level) }
        if (entities.isNotEmpty()) {
            builder.addWidget(EntityWidget(EntityDisplayWidget(entities, 92, 56, 0F, 0F, 24)))
        }
    }
}

/** A trial key of a category and tier, used on the trial keystone, gives the rewards. */
class TrialCategory(gui: IGuiHelper) : AbstractRecipeCategory<RecipeHolder<TrialKeystoneRecipe>>(
    JeiRecipeTypes.TRIAL,
    Component.translatable("rei.$MOD_ID.category.trial"),
    gui.createDrawableItemLike(ITEM_TRIAL_KEY),
    150, 72
) {
    override fun setRecipe(builder: IRecipeLayoutBuilder, holder: RecipeHolder<TrialKeystoneRecipe>, focuses: IFocusGroup) {
        val recipe = holder.value()
        val key = ItemStack(ITEM_TRIAL_KEY).also {
            TrialKeyData(it).apply {
                category = recipe.category
                dataAmount = recipe.tier.dataAmount
            }
        }
        builder.addInputSlot(67, 2).setStandardSlotBackground().add(key)
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 67, 20).add(BLOCK_TRIAL_KEYSTONE)

        val rewards = recipe.copyRewards(true)
        rewards.forEachIndexed { index, reward ->
            val x = 75 - rewards.size * 9 + index * 18
            val slot = builder.addOutputSlot(x, 52).setOutputSlotBackground().add(reward)
            if (reward.`is`(ITEM_GLITCH_UPGRADE_SMITHING_TEMPLATE)) {
                // Not every trial of the tier gives it
                slot.addRichTooltipCallback { _, tooltip ->
                    tooltip.add(recipe.tier.glitchUpgradeOddsText.copy().setStyle(RenderUtils.STYLE))
                }
            }
        }
    }

    override fun createRecipeExtras(builder: IRecipeExtrasBuilder, holder: RecipeHolder<TrialKeystoneRecipe>, focuses: IFocusGroup) {
        val player = Minecraft.getInstance().player ?: return
        builder.addWidget(EntityWidget(EntityDisplayWidget(listOf(player), 30, 44, -120F, 20F, 16, ItemStack(Items.DIAMOND_SWORD))))
        val glitch = SystemGlitchEntity(SYSTEM_GLITCH_ENTITY_TYPE, player.level()).also { it.id = -999 }
        builder.addWidget(EntityWidget(EntityDisplayWidget(listOf(glitch), 120, 44, 120F, 20F, 16)))
    }
}
