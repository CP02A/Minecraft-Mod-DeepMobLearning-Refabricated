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

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.level.Level
import java.util.*

fun <T: Entity> Level.getEntitiesAroundCircle(filter: EntityTypeTest<Entity, T>, pos: BlockPos, radius: Double) : List<T> {
    val squaredRadius = radius * radius
    val pos3d = pos.toVec3d()
    return this.getEntities(filter, AABB(
        pos.x - radius,
        pos.y - 1.0,
        pos.z - radius,
        pos.x + radius,
        pos.y + radius,
        pos.z + radius
    )) {
        it.distanceToSqr(pos3d) <= squaredRadius
    }.orEmpty()
}

fun Level.getPlayersByUUID(uuids: HashSet<UUID>): List<Player> {
    return players().filter { it.uuid in uuids }
}
