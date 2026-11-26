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

package dev.nathanpb.dml.data.tag

import com.mojang.serialization.Codec
import dev.nathanpb.dml.utils.RegistryAccessHolder
import net.minecraft.core.NonNullList
import net.minecraft.core.UUIDUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import java.util.UUID

object Serializers {
    val INT = CodecSerializer(Codec.INT)
    val LONG = CodecSerializer(Codec.LONG)
    val FLOAT = CodecSerializer(Codec.FLOAT)
    val BOOLEAN = CodecSerializer(Codec.BOOL)
    val STRING = CodecSerializer(Codec.STRING)
    val IDENTIFIER = CodecSerializer(Identifier.CODEC)
    val IDENTIFIER_LIST = CodecSerializer(Identifier.CODEC.listOf())
    val UUID_LIST = CodecSerializer<List<UUID>>(UUIDUtil.CODEC.listOf())

    /** Item stacks need the registries (enchantments, ...), so they are written with registry-aware ops. */
    val ITEM_STACK = object : DataSerializer<ItemStack> {
        override fun read(tag: CompoundTag, key: String): ItemStack? {
            val element = tag.get(key) ?: return null
            return ItemStack.OPTIONAL_CODEC.parse(RegistryAccessHolder.ops(NbtOps.INSTANCE), element).result().orElse(null)
        }

        override fun write(tag: CompoundTag, key: String, data: ItemStack) {
            ItemStack.OPTIONAL_CODEC.encodeStart(RegistryAccessHolder.ops(NbtOps.INSTANCE), data).result()
                .ifPresent { tag.put(key, it) }
        }
    }
}

/** A fixed-size inventory, stored like vanilla containers (a list of slot-indexed stacks). */
class InventorySerializer(val size: Int) : DataSerializer<NonNullList<ItemStack>> {
    private val codec = ItemStack.OPTIONAL_CODEC.listOf()

    override fun read(tag: CompoundTag, key: String): NonNullList<ItemStack> {
        val list = NonNullList.withSize(size, ItemStack.EMPTY)
        val element = tag.get(key) ?: return list
        codec.parse(RegistryAccessHolder.ops(NbtOps.INSTANCE), element).result().ifPresent { stacks ->
            stacks.take(size).forEachIndexed { i, stack -> list[i] = stack }
        }
        return list
    }

    override fun write(tag: CompoundTag, key: String, data: NonNullList<ItemStack>) {
        codec.encodeStart(RegistryAccessHolder.ops(NbtOps.INSTANCE), data.toList()).result().ifPresent { tag.put(key, it) }
    }
}
