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

package dev.nathanpb.dml.screen.handler

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess

/** Opens the menu of the block at [pos] (see [registerScreenHandlerForBlockEntity]). */
open class BlockMenuProvider(
    private val title: Component,
    private val pos: BlockPos,
    private val handlerFactory: (Int, Inventory, ContainerLevelAccess) -> AbstractContainerMenu
) : ExtendedMenuProvider<BlockPos> {

    override fun getDisplayName(): Component = title

    override fun createMenu(syncId: Int, inv: Inventory, player: Player): AbstractContainerMenu {
        return handlerFactory(syncId, inv, ContainerLevelAccess.create(inv.player.level(), pos))
    }

    override fun getScreenOpeningData(player: ServerPlayer): BlockPos = pos
}
