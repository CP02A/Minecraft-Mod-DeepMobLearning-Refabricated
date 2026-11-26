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
import dev.nathanpb.dml.data.dataModel
import dev.nathanpb.dml.inventory.DataSynthesizerInventory
import dev.nathanpb.dml.item.ITEM_DATA_MODEL
import dev.nathanpb.dml.item.ItemDataModel
import dev.nathanpb.dml.misc.DATA_SYNTHESIZER_PROCESS
import dev.nathanpb.dml.utils.*
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.WorldlyContainerHolder
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.ContainerHelper
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.Containers
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.inventory.SimpleContainerData
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.core.NonNullList
import net.minecraft.core.BlockPos
import net.minecraft.world.level.LevelAccessor
import team.reborn.energy.api.base.SimpleEnergyStorage
import kotlin.properties.Delegates


class BlockEntityDataSynthesizer(pos: BlockPos, state: BlockState) :
    ClientSyncedBlockEntity(BLOCKENTITY_DATA_SYNTHESIZER, pos, state),
    WorldlyContainerHolder,
    PropertyDelegateHolder
{

    /**
     * 0 - [progress]
     * 1 - [maxProgress] (const)
     * 2 - [canProgress]
     * 3 - [mute]
     */
    private val _propertyDelegate = SimpleContainerData(4)
    val inventory = DataSynthesizerInventory()
    val energyCapacity = baseConfig.machines.dataSynthesizer.energyCapacity
    private val energyIO = baseConfig.machines.dataSynthesizer.energyIO
    val energyStorage: SimpleEnergyStorage = object : SimpleEnergyStorage(energyCapacity, energyIO, energyIO) {

        override fun onFinalCommit() {
            setChanged()
        }
    }

    val maxProgress = 3 * 20
    var progress by Delegates.observable(0) { _, _, newValue ->
        propertyDelegate[0] = newValue
    }
    var canProgress by Delegates.observable(false) { _, _, newValue ->
        propertyDelegate[2] = if(newValue) 1 else 0
    }
    var mute by Delegates.observable(false) { _, _, newValue ->
        propertyDelegate[3] = if(newValue) 1 else 0
    }

    init {
        propertyDelegate[1] = maxProgress
    }

    companion object {
        val ticker = BlockEntityTicker<BlockEntityDataSynthesizer> { world, pos, _, blockEntity ->
            val dataModelStack = blockEntity.inventory.getItem(0)
            if(!dataModelStack.isEmpty && dataModelStack.item is ItemDataModel && !dataModelStack.`is`(ITEM_DATA_MODEL)) {
                val dataEnergyValue = dataModelStack.dataModel.category!!.energyValue
                if(blockEntity.energyStorage.amount <= (blockEntity.energyCapacity - dataEnergyValue)) {
                    if(dataModelStack.hasSimUnrestrictedData()) {
                        blockEntity.canProgress = true
                        blockEntity.setChanged()
                        blockEntity.sync()
                    } else { // ensure the laser on the UI doesn't flicker by only removing flag on the start of the next loop, if invalid
                        removeCanProgressFlag(blockEntity)
                    }
                    if(blockEntity.canProgress) {
                        if(blockEntity.progress == 0) {
                            SoundPlayer(
                                DATA_SYNTHESIZER_PROCESS,
                                !blockEntity.mute
                            ).playSound(world, pos)
                        }
                        blockEntity.progress++
                        blockEntity.sync()

                        if(blockEntity.progress >= blockEntity.maxProgress) {
                            dataModelStack.dataModel.dataAmount--
                            blockEntity.energyStorage.amount += dataEnergyValue
                            resetProgress(blockEntity)
                            blockEntity.setChanged()
                            blockEntity.sync()
                        }
                    }
                } else {
                    removeCanProgressFlag(blockEntity)
                }

                // Remove invalid 'simulated' tag
                if(dataModelStack.dataModel.simulated && dataModelStack.dataModel.dataAmount <= 0) {
                    dataModelStack.dataModel.simulated = false
                    blockEntity.setChanged()
                }
            } else {
                removeCanProgressFlag(blockEntity)
            }

            pushEnergyExcept(world, pos, setOf(world.getBlockState(pos).getValue(BlockStateProperties.HORIZONTAL_FACING)), blockEntity.energyStorage)
            moveToStorage(blockEntity.energyStorage, blockEntity.inventory, 1)
            moveToStack(blockEntity.energyStorage, blockEntity.inventory, 2)
        }


        private fun removeCanProgressFlag(blockEntity: BlockEntityDataSynthesizer) {
            blockEntity.canProgress = false
            resetProgress(blockEntity)
            blockEntity.setChanged()
            blockEntity.sync()
        }

        private fun resetProgress(blockEntity: BlockEntityDataSynthesizer) {
            blockEntity.progress = 0
        }
    }

    override fun getContainer(state: BlockState, world: LevelAccessor, pos: BlockPos): WorldlyContainer {
        return (world.getBlockEntity(pos) as BlockEntityDataSynthesizer).inventory
    }

    override fun toTag(output: ValueOutput) {
        ContainerHelper.saveAllItems(output.child("${MOD_ID}:inventory"), inventory.items())

        output.putLong("${MOD_ID}:energy", energyStorage.amount)
        output.putBoolean("${MOD_ID}:can_progress", canProgress)
        output.putInt("${MOD_ID}:progress", progress)
        output.putBoolean("${MOD_ID}:mute", mute)
    }


    override fun fromTag(input: ValueInput) {
        val stacks = NonNullList.withSize(inventory.containerSize, ItemStack.EMPTY)
        ContainerHelper.loadAllItems(input.childOrEmpty("${MOD_ID}:inventory"), stacks)
        inventory.setStacks(stacks)

        energyStorage.amount = input.getLongOr("${MOD_ID}:energy", 0L)
        canProgress = input.getBooleanOr("${MOD_ID}:can_progress", false)
        progress = input.getIntOr("${MOD_ID}:progress", 0)
        mute = input.getBooleanOr("${MOD_ID}:mute", false)
    }

    override fun toClientTag(output: ValueOutput) {
        toTag(output)
    }

    override fun fromClientTag(input: ValueInput) {
        fromTag(input)
    }

    // Before 1.21.5 the block dropped the inventory when it was replaced
    override fun preRemoveSideEffects(pos: BlockPos, state: BlockState) {
        level?.let { Containers.dropContents(it, pos, inventory) }
    }

    override fun getPropertyDelegate(): SimpleContainerData = _propertyDelegate

}