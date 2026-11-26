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

package dev.nathanpb.dml.screen.handler

import dev.nathanpb.dml.identifier
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.resources.Identifier
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess

val HANDLER_DEEP_LEARNER = registerScreenHandlerForItemStack(identifier("deep_learner"), ::DeepLearnerScreenHandler)
val HANDLER_DATA_SYNTHESIZER = registerScreenHandlerForBlockEntity(identifier("data_synthesizer"), ::DataSynthesizerScreenHandler)
val HANDLER_LOOT_FABRICATOR = registerScreenHandlerForBlockEntity(identifier("loot_fabricator"), ::LootFabricatorScreenHandler)

val HANDLER_DISRUPTIONS_CORE = registerScreenHandlerForBlockEntity(identifier("disruptions_core"), ::DisruptionsCoreScreenHandler)

/** A menu of a block: the client gets the block position when the menu opens. */
fun <T: AbstractContainerMenu>registerScreenHandlerForBlockEntity(
    id: Identifier,
    f: (Int, Inventory, ContainerLevelAccess) -> T
): ExtendedMenuType<T, BlockPos> {
    return Registry.register(BuiltInRegistries.MENU, id, ExtendedMenuType({ syncId, inventory, pos ->
        f(syncId, inventory, ContainerLevelAccess.create(inventory.player.level(), pos))
    }, BlockPos.STREAM_CODEC))
}

/** A menu of the item in a hand: the client gets the hand when the menu opens. */
fun <T: AbstractContainerMenu>registerScreenHandlerForItemStack(
    id: Identifier,
    f: (Int, Inventory, InteractionHand) -> T
): ExtendedMenuType<T, InteractionHand> {
    return Registry.register(BuiltInRegistries.MENU, id, ExtendedMenuType({ syncId, inventory, hand ->
        f(syncId, inventory, hand)
    }, InteractionHand.STREAM_CODEC))
}

fun registerScreenHandlers() {
    HANDLER_DEEP_LEARNER
    HANDLER_DATA_SYNTHESIZER
    HANDLER_LOOT_FABRICATOR

    HANDLER_DISRUPTIONS_CORE
}
