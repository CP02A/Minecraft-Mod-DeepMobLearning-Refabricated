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

import dev.nathanpb.dml.baseConfig
import dev.nathanpb.dml.identifier
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams

/** The loot [player] would get for killing an entity of this type with [source]. */
fun EntityType<*>.simulateLootDroppedStacks(world: ServerLevel, player: Player, source: DamageSource): List<ItemStack> {
    val lootTableKey = defaultLootTable.orElse(null) ?: return emptyList()
    val lootTable = world.server.reloadableRegistries().getLootTable(lootTableKey)
    val entity = create(world, EntitySpawnReason.LOAD) ?: return emptyList()

    val parameters = LootParams.Builder(world)
        .withParameter(LootContextParams.ORIGIN, entity.position())
        .withParameter(LootContextParams.THIS_ENTITY, entity)
        .withParameter(LootContextParams.DAMAGE_SOURCE, source)
        .withParameter(LootContextParams.ATTACKING_ENTITY, player)
        .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
        .create(LootContextParamSets.ENTITY)

    val lootList = lootTable.getRandomItems(parameters, world.random)
    lootList.removeIf { stack: ItemStack -> !stack.isStackable && world.random.nextDouble() < baseConfig.machines.lootFabricator.unstackableNullificationChance }

    return lootList
}

fun initTag(id: String): TagKey<EntityType<*>> {
    return TagKey.create(Registries.ENTITY_TYPE, identifier(id))
}
