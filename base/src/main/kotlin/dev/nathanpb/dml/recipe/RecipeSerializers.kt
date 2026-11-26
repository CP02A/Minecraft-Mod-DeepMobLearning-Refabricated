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

package dev.nathanpb.dml.recipe

import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization
import dev.nathanpb.dml.identifier
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer

// Since 1.21.2 a serializer is just a pair of codecs: each recipe class defines its own SERIALIZER
private fun <T: Recipe<*>> register(id: String, serializer: RecipeSerializer<T>): RecipeSerializer<T> = Registry.register(
    BuiltInRegistries.RECIPE_SERIALIZER,
    identifier(id),
    serializer
)

fun registerRecipeSerializers() {
    register("trial_key_attune", TrialKeyAttuneRecipe.SERIALIZER)
    register("trial_keystone", TrialKeystoneRecipe.SERIALIZER)
    register("crushing", CrushingRecipe.SERIALIZER)
    register("loot_fabricator", LootFabricatorRecipe.SERIALIZER)

    // Recipes only exist on the server since 1.21.2: Fabric sends these to the clients that have the mod, for the
    // recipe viewers (JEI reads them on the client; REI makes its displays on the server, see ReiCommonPlugin)
    RecipeSynchronization.synchronizeRecipeSerializer(TrialKeystoneRecipe.SERIALIZER)
    RecipeSynchronization.synchronizeRecipeSerializer(CrushingRecipe.SERIALIZER)
    RecipeSynchronization.synchronizeRecipeSerializer(LootFabricatorRecipe.SERIALIZER)
}
