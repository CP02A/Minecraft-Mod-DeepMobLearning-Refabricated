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

package dev.nathanpb.dml.data

import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.NotDataModelException
import dev.nathanpb.dml.enums.DataModelTier
import dev.nathanpb.dml.enums.EntityCategory
import dev.nathanpb.dml.item.ItemDataModel
import dev.nathanpb.dml.data.tag.MutableCompoundData
import dev.nathanpb.dml.data.tag.Serializers
import dev.nathanpb.dml.data.tag.stackData
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack

class DataModelData private constructor(val stack: ItemStack, val category: EntityCategory?, data: Pair<CompoundTag, (CompoundTag) -> Unit>) :
    MutableCompoundData(data.first, data.second) {

    constructor(stack: ItemStack, category: EntityCategory?) : this(stack, category, stackData(stack))

    companion object {
        const val DATA_AMOUNT_TAG_KEY = "${MOD_ID}.datamodel.dataAmount"
        const val SIMULATED_TAG_KEY = "${MOD_ID}.datamodel.simulated"
        const val SIMULATION_COUNT_TAG_KEY = "simulationCount" // TODO update on 1.21
    }

    var dataAmount by persistentDefaulted(0, Serializers.INT, DATA_AMOUNT_TAG_KEY)

    /** write on simulacrum only, might be read by any module */
    var simulated by persistentDefaulted(false, Serializers.BOOLEAN, SIMULATED_TAG_KEY)
    var simulationCount by persistentDefaulted(0, Serializers.INT, SIMULATION_COUNT_TAG_KEY)


    fun tier() = DataModelTier.fromDataAmount(dataAmount)

}

val ItemStack.dataModel: DataModelData
    get() {
        item.let { item ->
            if (item is ItemDataModel) {
                return DataModelData(this, item.category)
            }
        }
        throw NotDataModelException()
    }
