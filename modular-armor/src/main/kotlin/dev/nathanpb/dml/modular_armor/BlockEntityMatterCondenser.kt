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

package dev.nathanpb.dml.modular_armor

import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.blockEntity.ClientSyncedBlockEntity
import dev.nathanpb.dml.item.ItemPristineMatter
import dev.nathanpb.dml.modular_armor.data.ModularArmorData
import dev.nathanpb.dml.modular_armor.inventory.MatterCondenserInventory
import dev.nathanpb.dml.utils.*
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.WorldlyContainerHolder
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.ContainerHelper
import net.minecraft.world.item.ItemStack
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.world.Containers
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry
import net.minecraft.core.NonNullList
import net.minecraft.core.BlockPos
import net.minecraft.world.level.LevelAccessor
import team.reborn.energy.api.base.SimpleEnergyStorage

class BlockEntityMatterCondenser(pos: BlockPos, state: BlockState) :
    ClientSyncedBlockEntity(BLOCK_ENTITY_TYPE, pos, state),
    WorldlyContainerHolder
{

    val energyCapacity = modularArmorConfig.machines.matterCondenser.energyCapacity
    val energyIO = modularArmorConfig.machines.matterCondenser.energyIO
    var inventory = MatterCondenserInventory()
    val energyStorage: SimpleEnergyStorage = object : SimpleEnergyStorage(energyCapacity, energyIO, energyIO) {

        override fun onFinalCommit() {
            setChanged()
        }
    }

    companion object {
        val ticker = BlockEntityTicker<BlockEntityMatterCondenser> { world, pos, _, blockEntity ->
            val armorStack = blockEntity.inventory.armorStack
            val inputStack = blockEntity.inventory.pristineInputStack

            if(armorStack.item is ItemModularGlitchArmor) {
                val data = ModularArmorData(armorStack)

                // TODO Remove in 1.21
                if(data.dataAmount > 0) {
                    data.pristineEnergy += (data.dataAmount * 1024).coerceAtMost(64 * 1024)
                    data.dataAmount = -1
                }
                moveToStackPristine(blockEntity.energyStorage, blockEntity.inventory, 0)
            }

            // Insert
            if(inputStack.item is ItemPristineMatter) {
                val pristineEnergyValue = (inputStack.item as ItemPristineMatter).entityCategory.energyValue * modularArmorConfig.machines.matterCondenser.normalToPristineEnergyMultiplier
                if(blockEntity.energyStorage.addEnergy(pristineEnergyValue)) {
                    inputStack.shrink(1)
                    blockEntity.setChanged()
                    blockEntity.sync()
                }
            }

            pushPristineEnergyToAllSides(world, pos, blockEntity.energyStorage)
            moveToStoragePristine(blockEntity.energyStorage, blockEntity.inventory, 1)
            moveToStackPristine(blockEntity.energyStorage, blockEntity.inventory, 2)
            blockEntity.sync()
        }

        val BLOCK_ENTITY_TYPE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            BlockMatterCondenser.IDENTIFIER,
            FabricBlockEntityTypeBuilder.create(::BlockEntityMatterCondenser, BlockMatterCondenser.BLOCK_MATTER_CONDENSER).build()
        ).also {
            SIDED_PRISTINE.registerForBlockEntity(
                { blockEntity, _ -> blockEntity.energyStorage },
                it
            )
        }
    }

    override fun getContainer(state: BlockState, world: LevelAccessor, pos: BlockPos) = inventory

    override fun toTag(output: ValueOutput) {
        ContainerHelper.saveAllItems(output, inventory.items())
        output.putLong("${MOD_ID}:energy", energyStorage.amount)
    }

    override fun fromTag(input: ValueInput) {
        val list = NonNullList.withSize(inventory.containerSize, ItemStack.EMPTY)
        ContainerHelper.loadAllItems(input, list)
        inventory.setStacks(list)

        energyStorage.amount = input.getLongOr("$MOD_ID:energy", 0L)
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

}