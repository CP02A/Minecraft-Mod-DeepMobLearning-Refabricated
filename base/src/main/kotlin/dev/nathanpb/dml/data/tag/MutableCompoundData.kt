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
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Small replacement for KtDataTagLib (which has no 26.x build): properties stored in a [CompoundTag].
 *
 * Item stacks have no NBT any more, so stack-backed data lives in the `minecraft:custom_data` component. That is
 * also where the vanilla data fixers move the NBT of old item stacks, so items from older worlds keep their data.
 * Every write calls [onWrite], which puts the tag back where it came from (see [ItemStack.customDataTag]).
 */
open class MutableCompoundData(val tag: CompoundTag, private val onWrite: (CompoundTag) -> Unit = {}) {

    protected fun <T> persistentDefaulted(default: T, serializer: DataSerializer<T>, key: String? = null) =
        object : ReadWriteProperty<MutableCompoundData, T> {
            override fun getValue(thisRef: MutableCompoundData, property: KProperty<*>): T {
                val k = key ?: property.name
                return if (tag.contains(k)) serializer.read(tag, k) ?: default else default
            }

            override fun setValue(thisRef: MutableCompoundData, property: KProperty<*>, value: T) {
                serializer.write(tag, key ?: property.name, value)
                onWrite(tag)
            }
        }
}

interface DataSerializer<T> {
    fun read(tag: CompoundTag, key: String): T?
    fun write(tag: CompoundTag, key: String, data: T)
}

/** Stores the value with a codec; values that don't decode fall back to the property's default. */
open class CodecSerializer<T : Any>(private val codec: Codec<T>) : DataSerializer<T> {
    override fun read(tag: CompoundTag, key: String): T? = tag.read(key, codec).orElse(null)
    override fun write(tag: CompoundTag, key: String, data: T) {
        tag.store(key, codec, data)
    }
}

class EnumSerializer<T : Enum<T>>(clazz: Class<T>) : DataSerializer<T> {
    private val values = clazz.enumConstants

    override fun read(tag: CompoundTag, key: String): T? = tag.getString(key).map { name ->
        values.firstOrNull { it.name == name }
    }.orElse(null)

    override fun write(tag: CompoundTag, key: String, data: T) {
        tag.putString(key, data.name)
    }
}

/** A copy of the stack's custom data; writing it with [setCustomDataTag] stores it back. */
val ItemStack.customDataTag: CompoundTag
    get() = getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()

fun ItemStack.setCustomDataTag(tag: CompoundTag) {
    CustomData.set(DataComponents.CUSTOM_DATA, this, tag)
}

/** Data backed by the whole custom data tag of [stack]. */
fun stackData(stack: ItemStack) = stack.customDataTag to { tag: CompoundTag -> stack.setCustomDataTag(tag) }

/** Data backed by the compound [key] inside the custom data tag of [stack]. */
fun stackSubData(stack: ItemStack, key: String): Pair<CompoundTag, (CompoundTag) -> Unit> {
    val root = stack.customDataTag
    return root.getCompoundOrEmpty(key) to { tag: CompoundTag ->
        val current = stack.customDataTag
        current.put(key, tag)
        stack.setCustomDataTag(current)
    }
}
