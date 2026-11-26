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

import dev.nathanpb.dml.identifier
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour

// Since 1.21.2 items and blocks know their id when they are constructed (Properties#setId), so they are created and
// registered in one step.

fun itemKey(id: Identifier): ResourceKey<Item> = ResourceKey.create(Registries.ITEM, id)
fun blockKey(id: Identifier): ResourceKey<Block> = ResourceKey.create(Registries.BLOCK, id)

fun <T : Item> registerItem(id: Identifier, factory: (Item.Properties) -> T): T =
    Registry.register(BuiltInRegistries.ITEM, itemKey(id), factory(Item.Properties().setId(itemKey(id))))

fun <T : Item> registerItem(id: String, factory: (Item.Properties) -> T): T = registerItem(identifier(id), factory)

/** [properties] are the base settings, e.g. `BlockBehaviour.Properties.ofFullCopy(otherBlock)`; the id is added here. */
fun <T : Block> registerBlock(
    id: Identifier,
    properties: BlockBehaviour.Properties = BlockBehaviour.Properties.of(),
    factory: (BlockBehaviour.Properties) -> T
): T = Registry.register(BuiltInRegistries.BLOCK, blockKey(id), factory(properties.setId(blockKey(id))))

fun <T : Block> registerBlock(
    id: String,
    properties: BlockBehaviour.Properties = BlockBehaviour.Properties.of(),
    factory: (BlockBehaviour.Properties) -> T
): T = registerBlock(identifier(id), properties, factory)

/** Registers the [BlockItem] of [block], with the same id. */
fun registerBlockItem(block: Block, settings: (Item.Properties) -> Item.Properties = { it }): BlockItem {
    val id = BuiltInRegistries.BLOCK.getKey(block)
    return registerItem(id) { BlockItem(block, settings(it.useBlockDescriptionPrefix())) }
}

/** Removes a component the properties set before (e.g. through a tool material). */
fun Item.Properties.removeComponent(type: DataComponentType<*>): Item.Properties =
    ItemPropertiesUtils.removeComponent(this, type)
