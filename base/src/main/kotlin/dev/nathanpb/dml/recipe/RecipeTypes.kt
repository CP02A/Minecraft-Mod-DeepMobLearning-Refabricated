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

import dev.nathanpb.dml.identifier
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeInput
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level

lateinit var RECIPE_TRIAL_KEYSTONE: RecipeType<TrialKeystoneRecipe>
lateinit var RECIPE_CRUSHING: RecipeType<CrushingRecipe>
lateinit var RECIPE_LOOT_FABRICATOR: RecipeType<LootFabricatorRecipe>

private fun <T : Recipe<*>> register(id: Identifier): RecipeType<T> = Registry.register(
    BuiltInRegistries.RECIPE_TYPE,
    id,
    object : RecipeType<T> {
        override fun toString() = id.toString()
    })

fun registerRecipeTypes() {
    RECIPE_TRIAL_KEYSTONE = register(identifier("trial_keystone"))
    RECIPE_CRUSHING = register(identifier("crushing"))
    RECIPE_LOOT_FABRICATOR = register(identifier("loot_fabricator"))
}

/** A recipe that remembers its id (since 1.20.2 only the recipe holder knows it). */
interface IdentifiedRecipe {
    var id: Identifier
}

/**
 * All recipes of [type]. Since 1.21.2 recipes only exist on the server (the client gets none), so on the client
 * this is empty.
 */
fun <T> recipesOf(world: Level, type: RecipeType<T>): List<T> where T : Recipe<*>, T : IdentifiedRecipe {
    val server = world as? ServerLevel ?: return emptyList()
    @Suppress("UNCHECKED_CAST")
    val holders = server.recipeAccess().getAllOfType(type as RecipeType<Recipe<RecipeInput>>) as Collection<RecipeHolder<T>>
    return holders.map { holder ->
        holder.value().also { it.id = holder.id().identifier() }
    }
}

fun <T> recipeById(world: Level, type: RecipeType<T>, id: Identifier): T? where T : Recipe<*>, T : IdentifiedRecipe {
    val server = world as? ServerLevel ?: return null
    val holder = server.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, id)).orElse(null) ?: return null
    if (holder.value().type != type) return null
    @Suppress("UNCHECKED_CAST")
    return (holder.value() as T).also { it.id = id }
}
