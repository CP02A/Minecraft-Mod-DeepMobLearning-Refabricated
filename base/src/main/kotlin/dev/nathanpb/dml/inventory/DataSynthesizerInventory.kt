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

import net.minecraft.core.Direction
import net.minecraft.world.SimpleContainer
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.item.ItemStack

class DataSynthesizerInventory : SimpleContainer(3), WorldlyContainer {

    companion object {
        const val INPUT_SLOT = 0
    }

    val dataModelStack: ItemStack
        get() = getItem(INPUT_SLOT)

    override fun canTakeItemThroughFace(slot: Int, stack: ItemStack, dir: Direction) = false

    override fun canPlaceItemThroughFace(slot: Int, stack: ItemStack, dir: Direction?) = false

    override fun getSlotsForFace(side: Direction) = intArrayOf()
}
