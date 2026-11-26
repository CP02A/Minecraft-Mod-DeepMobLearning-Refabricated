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

package dev.nathanpb.dml.client

import dev.nathanpb.dml.identifier
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes
import net.minecraft.client.Minecraft

/**
 * The mod's recipes as the server synchronized them (see registerRecipeSerializers): the client has no recipe manager
 * since 1.21.2. Kept before the other listeners of the event run, as recipe viewers reload their recipes in it.
 */
object ClientRecipes {

    private val EARLY_PHASE = identifier("early")

    @Volatile
    private var synced: SynchronizedRecipes? = null

    /** The last synchronized recipes, or those of the current level. */
    val recipes: SynchronizedRecipes?
        get() = synced ?: Minecraft.getInstance().level?.recipeAccess()?.synchronizedRecipes

    fun register() {
        ClientRecipeSynchronizedEvent.EVENT.addPhaseOrdering(EARLY_PHASE, Event.DEFAULT_PHASE)
        ClientRecipeSynchronizedEvent.EVENT.register(EARLY_PHASE) { _, recipes -> synced = recipes }
    }
}
