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

package dev.nathanpb.dml.modular_armor.data

import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.data.DataModelData
import dev.nathanpb.dml.data.dataModel
import dev.nathanpb.dml.enums.DataModelTier
import dev.nathanpb.dml.item.ItemDataModel
import dev.nathanpb.dml.utils.takeOrNull
import dev.nathanpb.dml.data.tag.MutableCompoundData
import dev.nathanpb.dml.data.tag.Serializers
import dev.nathanpb.dml.data.tag.setCustomDataTag
import dev.nathanpb.dml.data.tag.stackData
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import team.reborn.energy.api.base.SimpleEnergyItem

class ModularArmorData private constructor(val stack: ItemStack, data: Pair<CompoundTag, (CompoundTag) -> Unit>) :
    MutableCompoundData(data.first, data.second) {

    constructor(stack: ItemStack) : this(stack, stackData(stack))


    /**
     * The armor's pristine energy: the energy component of Team Reborn Energy (before 1.20.5 its "energy" NBT key).
     * Armor from older worlds still has the old key in its custom data: it's moved to the component on first read.
     */
    var pristineEnergy: Long
        get() {
            if (tag.contains(LEGACY_ENERGY_KEY)) {
                val legacy = tag.getLongOr(LEGACY_ENERGY_KEY, 0L)
                tag.remove(LEGACY_ENERGY_KEY)
                stack.setCustomDataTag(tag)
                SimpleEnergyItem.setStoredEnergyUnchecked(stack, legacy)
            }
            return SimpleEnergyItem.getStoredEnergyUnchecked(stack)
        }
        set(value) = SimpleEnergyItem.setStoredEnergyUnchecked(stack, value)

    var disabledEffects by persistentDefaulted(emptyList(), Serializers.IDENTIFIER_LIST)

    private var dataModelStack by persistentDefaulted(ItemStack.EMPTY, Serializers.ITEM_STACK, "${MOD_ID}.dataModel")

    var dataModel: DataModelData?
        get() {
            val stack = dataModelStack
            return takeOrNull(!stack.isEmpty && (stack.item as? ItemDataModel)?.category != null) {
                stack.dataModel
            }
        }
        set(value) {
            dataModelStack = value?.stack ?: ItemStack.EMPTY
        }

    fun tier() = dataModel?.tier() ?: DataModelTier.FAULTY

    // TODO Remove in 1.21
    /** DEPRECATED - this is being kept for a while to allow worlds to be upgraded smoothly.*/
    var dataAmount by persistentDefaulted(-1, Serializers.INT, "${MOD_ID}.dataAmount")


    companion object {
        private const val LEGACY_ENERGY_KEY = "energy"
    }
}
