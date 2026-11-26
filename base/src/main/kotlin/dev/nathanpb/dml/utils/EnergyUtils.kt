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
import dev.nathanpb.dml.utils.RenderUtils.Companion.ALT_STYLE
import dev.nathanpb.dml.utils.RenderUtils.Companion.ENERGY_STYLE
import dev.nathanpb.dml.utils.RenderUtils.Companion.STYLE
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container
import net.minecraft.world.level.ItemLike
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import team.reborn.energy.api.EnergyStorage
import team.reborn.energy.api.EnergyStorageUtil
import team.reborn.energy.api.base.SimpleEnergyItem
import team.reborn.energy.api.base.SimpleEnergyStorage
import java.util.function.Predicate
import kotlin.math.roundToInt


@Suppress("UNCHECKED_CAST")
val SIDED_PRISTINE: BlockApiLookup<EnergyStorage, Direction?> =
    BlockApiLookup.get(identifier("sided_pristine"), EnergyStorage::class.java, Direction::class.java) as BlockApiLookup<EnergyStorage, Direction?>
val ITEM_PRISTINE = ItemApiLookup.get(identifier("item_pristine"), EnergyStorage::class.java, ContainerItemContext::class.java)


fun moveToStorage(
    energyStorage: EnergyStorage,
    inventory: Container,
    index: Int
) {
    moveToStorage(energyStorage, inventory, index, EnergyStorage.ITEM)
}

fun moveToStack(
    energyStorage: EnergyStorage,
    inventory: Container,
    index: Int
) {
    moveToStack(energyStorage, inventory, index, EnergyStorage.ITEM)
}

fun moveToStoragePristine(
    energyStorage: EnergyStorage,
    inventory: Container,
    index: Int
) {
    moveToStorage(energyStorage, inventory, index, ITEM_PRISTINE)
}

fun moveToStackPristine(
    energyStorage: EnergyStorage,
    inventory: Container,
    index: Int
) {
    moveToStack(energyStorage, inventory, index, ITEM_PRISTINE)
}

private fun moveToStorage(
    energyStorage: EnergyStorage,
    inventory: Container,
    index: Int,
    energyLookup: ItemApiLookup<EnergyStorage, ContainerItemContext>
) {
    val stack = inventory.getItem(index)
    if(stack.isEmpty) return

    val stackEnergy = energyLookup.find(stack, ContainerItemContext.ofSingleSlot(ContainerStorage.of(inventory, null).getSlot(index)))
    if(stackEnergy?.supportsExtraction() == true) {
        EnergyStorageUtil.move(stackEnergy, energyStorage, Long.MAX_VALUE, null)
    }
}

private fun moveToStack(
    energyStorage: EnergyStorage,
    inventory: Container,
    index: Int,
    energyLookup: ItemApiLookup<EnergyStorage, ContainerItemContext>
) {
    val stack = inventory.getItem(index)
    if(stack.isEmpty) return

    val stackEnergy = energyLookup.find(stack, ContainerItemContext.ofSingleSlot(ContainerStorage.of(inventory, null).getSlot(index)))
    if(stackEnergy?.supportsInsertion() == true) {
        EnergyStorageUtil.move(energyStorage, stackEnergy, Long.MAX_VALUE, null)
    }
}

fun pushEnergyToAllSides(
    world: Level,
    pos: BlockPos,
    originStorage: EnergyStorage
) {
    pushEnergyExcept(world, pos, setOf(), originStorage, EnergyStorage.SIDED)
}

fun pushPristineEnergyToAllSides(
    world: Level,
    pos: BlockPos,
    originStorage: EnergyStorage
) {
    pushEnergyExcept(world, pos, setOf(), originStorage, SIDED_PRISTINE)
}

private fun pushEnergyToAllSides(
    world: Level,
    pos: BlockPos,
    originStorage: EnergyStorage,
    energyLookup: BlockApiLookup<EnergyStorage, Direction?>
) {
    pushEnergyExcept(world, pos, setOf(), originStorage, energyLookup)
}

fun pushEnergyExcept(
    world: Level,
    pos: BlockPos,
    exceptDirections: Set<Direction>,
    originStorage: EnergyStorage
) {
    val directions = mutableSetOf(*Direction.values())
    directions.removeAll(exceptDirections)
    for(direction in directions) {
        pushEnergy(world, pos, direction, originStorage, EnergyStorage.SIDED)
    }
}

fun pushPristineEnergyExcept(
    world: Level,
    pos: BlockPos,
    exceptDirections: Set<Direction>,
    originStorage: EnergyStorage
) {
    val directions = mutableSetOf(*Direction.values())
    directions.removeAll(exceptDirections)
    for(direction in directions) {
        pushEnergy(world, pos, direction, originStorage, SIDED_PRISTINE)
    }
}

private fun pushEnergyExcept(
    world: Level,
    pos: BlockPos,
    exceptDirections: Set<Direction>,
    originStorage: EnergyStorage,
    energyLookup: BlockApiLookup<EnergyStorage, Direction?>
) {
    val directions = mutableSetOf(*Direction.values())
    directions.removeAll(exceptDirections)
    for(direction in directions) {
        pushEnergy(world, pos, direction, originStorage, energyLookup)
    }
}

private fun pushEnergy(
    world: Level,
    pos: BlockPos,
    direction: Direction,
    originStorage: EnergyStorage,
    energyLookup: BlockApiLookup<EnergyStorage, Direction?>
): Long {
    val externalStorage = energyLookup.find(world, pos.relative(direction), direction.opposite)
    return EnergyStorageUtil.move(originStorage, externalStorage, Long.MAX_VALUE, null)
}

fun SimpleEnergyStorage.addEnergy(
    amount: Long
): Boolean {
    var commit: Boolean
    Transaction.openOuter().use { transaction ->
        commit = insert(amount, transaction) > 0
        if(commit) {
            transaction.commit()
        }
    }
    return commit
}

fun SimpleEnergyStorage.removeEnergy(
    amount: Long
): Boolean {
    var commit: Boolean
    Transaction.openOuter().use { transaction ->
        commit = extract(amount, transaction) > 0
        if(commit) {
            transaction.commit()
        }
    }
    return commit
}

fun getEnergyBarStep(stack: ItemStack): Int {
    if(stack.item !is SimpleEnergyItem) throw IllegalStateException("Item must implement SimpleEnergyItem!")
    val max = (stack.item as SimpleEnergyItem).getEnergyCapacity(stack)
    val current = (stack.item as SimpleEnergyItem).getStoredEnergy(stack)

    return if(current > max) 13 else ((13f * current) / max).roundToInt()
}

fun distributeEnergyToInventory(
    player: Player,
    itemStack: ItemStack,
    maxOutput: Long,
    filter: Predicate<ItemStack>,
    isPristine: Boolean = false
) {
    val playerInv = PlayerInventoryStorage.of(player)
    var sourceSlot: SingleSlotStorage<ItemVariant>? = null

    for(i in 0 until player.inventory.getContainerSize()) {
        if(player.inventory.getItem(i) == itemStack) {
            sourceSlot = playerInv.slots[i]
            break
        }
    }

    if(sourceSlot == null) throw IllegalArgumentException("Failed to locate current stack in the player inventory.")
    val energyType = if(isPristine) ITEM_PRISTINE else EnergyStorage.ITEM
    val sourceStorage = ContainerItemContext.ofPlayerSlot(player, sourceSlot).find(energyType) ?: return

    for(i in 0 until player.inventory.getContainerSize()) {
        val invStack = player.inventory.getItem(i)

        if(invStack.isEmpty || !filter.test(invStack)) continue
        EnergyStorageUtil.move(
            sourceStorage,
            ContainerItemContext.ofPlayerSlot(player, playerInv.slots[i]).find(energyType),
            maxOutput,
            null
        )
    }
}

fun getEnergyStorage(stack: ItemStack, ctx: ContainerItemContext): EnergyStorage? {
    val pristineItem = stack.item
    if (pristineItem is SimpleEnergyItem) {
        return SimpleEnergyItem.createStorage(
            ctx,
            pristineItem.getEnergyCapacity(stack),
            pristineItem.getEnergyMaxInput(stack),
            pristineItem.getEnergyMaxOutput(stack)
        )
    }
    return null
}

fun getEnergyTooltipText(stack: ItemStack): Component {
    return getEnergyTooltipText(stack, ENERGY_STYLE, Style.EMPTY.applyFormats(ChatFormatting.YELLOW))
}

fun getPristineEnergyTooltipText(stack: ItemStack): Component {
    return getEnergyTooltipText(stack, STYLE, ALT_STYLE, true)
}

fun getEnergyTooltipText(stack: ItemStack, primaryStyle: Style, secondaryStyle: Style, isPristine: Boolean = false): Component {
    if(stack.item !is SimpleEnergyItem) throw IllegalStateException("Item must implement SimpleEnergyItem!")

    val energyText = Component.translatable("text.dml-refabricated.energy.prefix")

    val numberFormatter = formatAccordingToLanguage()
    val energyAmountText = Component.translatable(
        "tooltip.dml-refabricated.data_amount.2",
        numberFormatter.format((stack.item as SimpleEnergyItem).getStoredEnergy(stack)),
        numberFormatter.format((stack.item as SimpleEnergyItem).getEnergyCapacity(stack))
    )
    val formattedEnergyAmountText = Component.translatable(getShortEnergyKey(isPristine), energyAmountText)

    return getInfoText(energyText, formattedEnergyAmountText, primaryStyle, secondaryStyle)
}

fun getShortEnergyKey(isPristine: Boolean): String {
    return if(isPristine) {
        "text.dml-refabricated.pristine_energy.short"
    } else {
        "text.dml-refabricated.energy.short"
    }
}


fun getEmptyAndFullCapacityEnergyItem(item: ItemLike): List<ItemStack> {
    return listOf(getFullCapacityStack(item), ItemStack(item))
}

fun getFullCapacityStack(item: ItemLike): ItemStack {
    if(item !is SimpleEnergyItem) throw IllegalStateException("Item must implement SimpleEnergyItem!")
    val stack = ItemStack(item)
    item.setStoredEnergy(stack, item.getEnergyCapacity(stack))
    return stack
}