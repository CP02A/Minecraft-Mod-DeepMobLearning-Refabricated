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

import dev.nathanpb.dml.block.BLOCK_LOOT_FABRICATOR
import dev.nathanpb.dml.block.BLOCK_TRIAL_KEYSTONE
import dev.nathanpb.dml.client.ClientRecipes
import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.item.ITEM_DML
import dev.nathanpb.dml.item.ITEM_EMERITUS_HAT
import dev.nathanpb.dml.item.ITEM_GLITCH_UPGRADE_SMITHING_TEMPLATE
import dev.nathanpb.dml.recipe.RECIPE_CRUSHING
import dev.nathanpb.dml.recipe.RECIPE_LOOT_FABRICATOR
import dev.nathanpb.dml.recipe.RECIPE_TRIAL_KEYSTONE
import dev.nathanpb.dml.utils.MODULAR_ARMOR_ID
import dev.nathanpb.dml.utils.isModLoaded
import mezz.jei.api.IModPlugin
import mezz.jei.api.constants.VanillaTypes
import mezz.jei.api.registration.IRecipeCatalystRegistration
import mezz.jei.api.registration.IRecipeCategoryRegistration
import mezz.jei.api.registration.IRecipeRegistration
import mezz.jei.api.runtime.IJeiRuntime
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack

/**
 * JEI integration: the crushing, loot fabricator and trial categories (like the REI plugin). The client has no recipes
 * since 1.21.2: they come from Fabric's recipe synchronization (see registerRecipeSerializers and [ClientRecipes]).
 */
class DmlJeiPlugin : IModPlugin {

    override fun getPluginUid(): Identifier = identifier("jei_plugin")

    override fun registerCategories(registration: IRecipeCategoryRegistration) {
        val gui = registration.jeiHelpers.guiHelper
        registration.addRecipeCategories(CrushingCategory(gui), LootFabricatorCategory(gui), TrialCategory(gui))
    }

    override fun registerRecipes(registration: IRecipeRegistration) {
        val recipes = ClientRecipes.recipes ?: return
        registration.addRecipes(JeiRecipeTypes.CRUSHING, recipes.getAllOfType(RECIPE_CRUSHING).toList())
        registration.addRecipes(JeiRecipeTypes.LOOT_FABRICATOR, recipes.getAllOfType(RECIPE_LOOT_FABRICATOR)
            .sortedBy { it.value().category.ordinal })
        registration.addRecipes(JeiRecipeTypes.TRIAL, recipes.getAllOfType(RECIPE_TRIAL_KEYSTONE)
            .sortedWith(compareBy({ it.value().category.ordinal }, { it.value().tier.ordinal })))
    }

    override fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
        registration.addCraftingStation(JeiRecipeTypes.LOOT_FABRICATOR, BLOCK_LOOT_FABRICATOR)
        registration.addCraftingStation(JeiRecipeTypes.TRIAL, BLOCK_TRIAL_KEYSTONE)
    }

    override fun onRuntimeAvailable(runtime: IJeiRuntime) {
        // Like the REI plugin: the Emeritus Hat and the creative tab icon aren't obtainable, the template is only for glitch armor
        val hidden = mutableListOf(ItemStack(ITEM_EMERITUS_HAT), ItemStack(ITEM_DML))
        if (!isModLoaded(MODULAR_ARMOR_ID)) {
            hidden += ItemStack(ITEM_GLITCH_UPGRADE_SMITHING_TEMPLATE)
        }
        runtime.ingredientManager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, hidden)
    }
}
