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

package dev.nathanpb.dml.client

import com.mojang.serialization.MapCodec
import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.item.battery.AbstractItemBattery
import net.minecraft.client.color.item.ItemTintSource
import net.minecraft.client.color.item.ItemTintSources
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.util.ARGB
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

/** The color of a battery, fading with its charge (models: "type": "dml-refabricated:battery_charge"). */
object BatteryChargeTint : ItemTintSource {
    val MAP_CODEC: MapCodec<BatteryChargeTint> = MapCodec.unit(BatteryChargeTint)

    override fun calculate(stack: ItemStack, level: ClientLevel?, owner: LivingEntity?): Int {
        val item = stack.item as? AbstractItemBattery ?: return -1
        return ARGB.opaque(item.getScaledColor(stack))
    }

    override fun type() = MAP_CODEC
}

fun registerItemTints() {
    ItemTintSources.ID_MAPPER.put(identifier("battery_charge"), BatteryChargeTint.MAP_CODEC)
}
