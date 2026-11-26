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

package dev.nathanpb.dml.blockEntity

import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.baseConfig
import dev.nathanpb.dml.enums.EntityCategory
import dev.nathanpb.dml.inventory.LootFabricatorInventory
import dev.nathanpb.dml.item.ItemPristineMatter
import dev.nathanpb.dml.recipe.RECIPE_LOOT_FABRICATOR
import dev.nathanpb.dml.utils.*
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import net.fabricmc.fabric.api.entity.FakePlayer
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.WorldlyContainerHolder
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.ContainerHelper
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.SimpleContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.Containers
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.inventory.StackedContentsCompatible
import net.minecraft.world.entity.player.StackedItemContents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.inventory.SimpleContainerData
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.NonNullList
import net.minecraft.core.BlockPos
import net.minecraft.world.level.LevelAccessor
import team.reborn.energy.api.base.SimpleEnergyStorage
import kotlin.properties.Delegates

class BlockEntityLootFabricator(pos: BlockPos, state: BlockState) :
    BlockEntity(BLOCKENTITY_LOOT_FABRICATOR, pos, state),
    WorldlyContainerHolder,
    PropertyDelegateHolder,
    StackedContentsCompatible
{

    /**
     * 0 - [progress]
     * 1 - [maxProgress] (const)
     * 2 - [SimpleEnergyStorage.amount]
     * 3 - [energyCapacity] (const)
     */
    private val _propertyDelegate = SimpleContainerData(4)
    private var isDumpingBufferedInventory = false
    val bufferedInternalInventory = SimpleContainer(64)
    val inventory = LootFabricatorInventory().apply {
        onChanged = {
            dumpInternalInventory()
        }
    }

    private var progress by Delegates.observable(0) { _, _, newValue ->
        propertyDelegate[0] = newValue
    }
    private val maxProgress = baseConfig.machines.lootFabricator.processTime
    private val energyCapacity = baseConfig.machines.lootFabricator.energyCapacity
    private val energyInput = baseConfig.machines.lootFabricator.energyInput
    val energyStorage: SimpleEnergyStorage = object : SimpleEnergyStorage(energyCapacity, energyInput, 0) {

        override fun onFinalCommit() {
            setChanged()
            propertyDelegate[2] = amount.toInt()
        }
    }

    init {
        propertyDelegate[1] = maxProgress
        propertyDelegate[3] = energyCapacity.toInt()
    }
    
    companion object {
        private fun calculateEnergyCost(pristineMatter: ItemStack): Long {
            if(!baseConfig.machines.lootFabricator.isEnergyCostScaledToMatterType) {
                return baseConfig.machines.lootFabricator.fixedCost
            }
            if(pristineMatter.item !is ItemPristineMatter) throw IllegalStateException("Loot Fabricator has non-pristine matter item at its input!")
            return ((pristineMatter.item as ItemPristineMatter).entityCategory.energyValue.toFloat() * baseConfig.machines.lootFabricator.energyCostMultiplier).toLong()
        }

        val ticker = BlockEntityTicker<BlockEntityLootFabricator> { world, _, _, blockEntity ->

            moveToStorage(blockEntity.energyStorage, blockEntity.inventory, LootFabricatorInventory.ENERGY_INPUT)

            if(blockEntity.bufferedInternalInventory.isEmpty) {
                val pristineMatterStack = blockEntity.inventory.stackInInputSlot
                if(pristineMatterStack.isEmpty) return@BlockEntityTicker blockEntity.resetProgress()

                val recipe = (world as ServerLevel).recipeAccess()
                    .getRecipeFor(RECIPE_LOOT_FABRICATOR, SingleRecipeInput(pristineMatterStack), world).orElse(null)?.value()
                    ?: return@BlockEntityTicker blockEntity.resetProgress()

                val energyCost = calculateEnergyCost(pristineMatterStack)

                if(blockEntity.energyStorage.amount >= energyCost) {
                    if(blockEntity.progress >= blockEntity.maxProgress) {
                        val generatedLoot = blockEntity.generateLoot((world as ServerLevel), recipe.category).also {
                            // O(n²) goes brrrr
                            it.forEach { stackSource ->
                                it.forEach { stackTarget ->
                                    combineStacksIfPossible(
                                        stackSource,
                                        stackTarget,
                                        blockEntity.bufferedInternalInventory.maxStackSize
                                    )
                                }
                            }
                        }.filterNot(ItemStack::isEmpty)
                        blockEntity.bufferedInternalInventory.setStacks(
                            NonNullList.of(ItemStack.EMPTY, *generatedLoot.toTypedArray())
                        )
                        pristineMatterStack.shrink(1)
                        blockEntity.dumpInternalInventory()

                        blockEntity.energyStorage.amount -= energyCost
                        blockEntity.propertyDelegate[2] = blockEntity.energyStorage.amount.toInt()
                        blockEntity.setChanged()
                    } else {
                        blockEntity.progress++
                        blockEntity.setChanged()
                        return@BlockEntityTicker
                    }
                } else {
                    blockEntity.resetProgress()
                }
            }
        }
    }
    
    private fun generateLoot(world: ServerLevel, category: EntityCategory): List<ItemStack> {
        val entityList = BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(category.tagKey).toList()
        return (0 until category.exchangeRatio).map {
            entityList.random().value()
                .simulateLootDroppedStacks(world, FakePlayer.get(world), world.damageSources().generic())
        }.flatten().let { stacks ->
            SimpleContainer(stacks.size).also { tempInventory ->
                stacks.forEach { tempInventory.addItem(it) }
            }
        }.items()
    }

    // if you know a better way to do this please tell me
    private fun dumpInternalInventory() {
        if (bufferedInternalInventory.isEmpty || isDumpingBufferedInventory) return
        isDumpingBufferedInventory = true


        LootFabricatorInventory.OUTPUT_SLOTS.forEach { invIndex ->
            bufferedInternalInventory
                .items()
                .filterNot(ItemStack::isEmpty)
                .forEach { buffStack ->
                    val invStack = inventory.getItem(invIndex)
                    if (invStack.isEmpty) {
                        inventory.setItem(invIndex, buffStack.copy())
                        buffStack.count = 0
                    } else {
                        if (combineStacksIfPossible(buffStack, invStack, inventory.maxStackSize)) {
                            inventory.setChanged()
                        }
                    }
                }
        }
        isDumpingBufferedInventory = false
    }

    private fun resetProgress() {
        if(progress == 0) return
        progress = 0
        setChanged()
    }

    override fun getContainer(state: BlockState, world: LevelAccessor, pos: BlockPos): WorldlyContainer {
        return (world.getBlockEntity(pos) as BlockEntityLootFabricator).inventory
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        output.putLong("${MOD_ID}:energy", energyStorage.amount)
        output.putInt("${MOD_ID}:progress", progress)
        ContainerHelper.saveAllItems(output.child("${MOD_ID}:inventory"), inventory.items())
        ContainerHelper.saveAllItems(output.child("${MOD_ID}:bufferedInventory"), bufferedInternalInventory.items())
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        energyStorage.amount = input.getLongOr("${MOD_ID}:energy", 0L).also {
            propertyDelegate[2] = it.toInt()
        }
        progress = input.getIntOr("${MOD_ID}:progress", 0)

        val stacks = NonNullList.withSize(inventory.containerSize, ItemStack.EMPTY)
        ContainerHelper.loadAllItems(input.childOrEmpty("${MOD_ID}:inventory"), stacks)
        inventory.setStacks(stacks)

        val stacksBufferedInventory = NonNullList.withSize(bufferedInternalInventory.containerSize, ItemStack.EMPTY)
        ContainerHelper.loadAllItems(input.childOrEmpty("${MOD_ID}:bufferedInventory"), stacksBufferedInventory)
        bufferedInternalInventory.setStacks(stacksBufferedInventory)
    }

    // Before 1.21.5 the block dropped the inventories when it was replaced
    override fun preRemoveSideEffects(pos: BlockPos, state: BlockState) {
        level?.let {
            Containers.dropContents(it, pos, inventory)
            Containers.dropContents(it, pos, bufferedInternalInventory)
        }
    }

    override fun getPropertyDelegate(): SimpleContainerData = _propertyDelegate

    override fun fillStackedContents(finder: StackedItemContents) {
        finder.accountStack(inventory.stackInInputSlot)
    }
}
