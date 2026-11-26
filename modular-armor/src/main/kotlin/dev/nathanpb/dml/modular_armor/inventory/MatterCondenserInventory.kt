/*
 *
 *  Copyright (C) 2021 Nathan P. Bombana, IterationFunk
 *
 *  This file is part of Deep Mob Learning: Refabricated.
 *
 *  Deep Mob Learning: Refabricated is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Deep Mob Learning: Refabricated is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Deep Mob Learning: Refabricated.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.nathanpb.dml.modular_armor.inventory

import dev.nathanpb.dml.item.ItemPristineMatter
import dev.nathanpb.dml.modular_armor.ItemModularGlitchArmor
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.SimpleContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.core.Direction

class MatterCondenserInventory : SimpleContainer(3), WorldlyContainer {
    companion object {
        const val ARMOR_SLOT = 0
        const val PRISTINE_ENERGY_IN = 1
        const val PRISTINE_ENERGY_OUT = 2
    }

    override fun canTakeItemThroughFace(slot: Int, stack: ItemStack, dir: Direction) = slot == ARMOR_SLOT

    override fun canPlaceItemThroughFace(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
        return when(stack.item) {
            is ItemPristineMatter -> slot == PRISTINE_ENERGY_IN
            is ItemModularGlitchArmor -> slot == ARMOR_SLOT
            else -> true
        }
    }

    override fun getSlotsForFace(side: Direction): IntArray {
      return intArrayOf(ARMOR_SLOT, PRISTINE_ENERGY_IN, PRISTINE_ENERGY_OUT)
    }

    var armorStack: ItemStack
        get() = getItem(ARMOR_SLOT)
        set(value) = setItem(ARMOR_SLOT, value)

    var pristineInputStack: ItemStack
        get() = getItem(PRISTINE_ENERGY_IN)
        set(value) = setItem(PRISTINE_ENERGY_IN, value)

    var pristineOutputStack: ItemStack
        get() = getItem(PRISTINE_ENERGY_OUT)
        set(value) = setItem(PRISTINE_ENERGY_OUT, value)

}
