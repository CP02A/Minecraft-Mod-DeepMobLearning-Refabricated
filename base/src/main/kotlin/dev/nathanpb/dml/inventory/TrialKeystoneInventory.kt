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

package dev.nathanpb.dml.inventory

import dev.nathanpb.dml.utils.toVec3d
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.SimpleContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

class TrialKeystoneInventory : SimpleContainer(9), WorldlyContainer {

    fun dropAll(world: Level, pos: BlockPos) {
        val pos3d = pos.toVec3d()
        removeAllItems().map {
            ItemEntity(world, pos3d.x, pos3d.y, pos3d.z, it)
        }.forEach {
            world.addFreshEntity(it)
        }
    }

    override fun canTakeItemThroughFace(slot: Int, stack: ItemStack, dir: Direction) = false

    override fun canPlaceItemThroughFace(slot: Int, stack: ItemStack, dir: Direction?) = false

    override fun getSlotsForFace(side: Direction) = intArrayOf()
}
