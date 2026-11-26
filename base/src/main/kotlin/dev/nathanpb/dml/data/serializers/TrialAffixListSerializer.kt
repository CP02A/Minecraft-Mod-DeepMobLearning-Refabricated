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

package dev.nathanpb.dml.data.serializers

import dev.nathanpb.dml.data.tag.DataSerializer
import dev.nathanpb.dml.trial.affix.core.TrialAffix
import dev.nathanpb.dml.trial.affix.core.TrialAffixRegistry
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.resources.Identifier

/** The affixes as a list of their ids. */
class TrialAffixListSerializer : DataSerializer<List<TrialAffix>> {
    override fun read(tag: CompoundTag, key: String): List<TrialAffix> = tag.getListOrEmpty(key).mapNotNull { element ->
        element.asString().flatMap { Identifier.read(it).result() }.map { id ->
            TrialAffixRegistry.INSTANCE.findById(id)
                ?: TrialAffixRegistry.INSTANCE.all.first() // Trying to recover the error, may throw NPE anyway
        }.orElse(null)
    }

    override fun write(tag: CompoundTag, key: String, data: List<TrialAffix>) {
        tag.put(key, ListTag().also { list -> data.forEach { list.add(StringTag.valueOf(it.id.toString())) } })
    }
}
