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

package dev.nathanpb.dml.utils

import com.mojang.serialization.DynamicOps
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.core.HolderLookup
import net.minecraft.server.MinecraftServer

/**
 * The registries of the running game, for code that has no level at hand (item stack data stored in custom data).
 * The server's registries win; on a client connected to a remote server the client sets [clientLookup].
 */
object RegistryAccessHolder {
    var server: MinecraftServer? = null
        private set

    /** Set by the client entrypoint: the registries of the client level, or null when not in a world. */
    var clientLookup: () -> HolderLookup.Provider? = { null }

    fun init() {
        ServerLifecycleEvents.SERVER_STARTING.register { server = it }
        ServerLifecycleEvents.SERVER_STOPPED.register { server = null }
    }

    fun lookup(): HolderLookup.Provider? = server?.registryAccess() ?: clientLookup()

    /** Registry-aware ops when the game has registries, otherwise the plain ops (only static registries resolve). */
    fun <T : Any> ops(ops: DynamicOps<T>): DynamicOps<T> = lookup()?.createSerializationContext(ops) ?: ops
}
