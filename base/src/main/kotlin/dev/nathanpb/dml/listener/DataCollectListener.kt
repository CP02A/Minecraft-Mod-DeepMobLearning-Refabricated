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

package dev.nathanpb.dml.listener

import dev.nathanpb.dml.baseConfig
import dev.nathanpb.dml.data.DataModelData
import dev.nathanpb.dml.data.DeepLearnerData
import dev.nathanpb.dml.data.dataModel
import dev.nathanpb.dml.item.ITEM_GLITCH_SWORD
import dev.nathanpb.dml.item.ItemDataModel
import dev.nathanpb.dml.item.ItemDeepLearner
import dev.nathanpb.dml.utils.firstOrNullMapping
import dev.nathanpb.dml.utils.hotbar
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource

class DataCollectListener : ServerEntityCombatEvents.AfterKilledOtherEntity {

    override fun afterKilledOtherEntity(world: ServerLevel, player: Entity, entity: LivingEntity, source: DamageSource) {
        if (player !is ServerPlayer) return

        // The data models in a deep learner are decoded copies: the changed inventory is written back to the learner
        for (learner in (player.inventory.hotbar() + player.offhandItem).filter { it.item is ItemDeepLearner }) {
            val data = DeepLearnerData(learner)
            val inventory = data.inventory
            val model = inventory
                .filter { it.item is ItemDataModel }
                .firstOrNullMapping(
                    map = { it.dataModel },
                    accept = { it.category?.tagKey?.let { tag -> entity.type.builtInRegistryHolder().`is`(tag) } == true && !it.tier().isMaxTier() }
                ) ?: continue
            modifyDataAmount(model, player.mainHandItem.`is`(ITEM_GLITCH_SWORD))
            data.inventory = inventory
            return
        }
    }

    private fun modifyDataAmount(dataModelData: DataModelData, isGlitchSword: Boolean) {
        dataModelData.dataAmount += if(isGlitchSword) {
            baseConfig.dataModel.dataCollection.glitchSwordDataGainPerKill
        } else {
            baseConfig.dataModel.dataCollection.dataGainPerKill
        }
    }

}