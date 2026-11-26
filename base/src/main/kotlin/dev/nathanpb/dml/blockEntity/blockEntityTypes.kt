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

import dev.nathanpb.dml.block.BLOCK_DATA_SYNTHESIZER
import dev.nathanpb.dml.block.BLOCK_DISRUPTIONS_CORE
import dev.nathanpb.dml.block.BLOCK_FADING_GLITCHED_TILE
import dev.nathanpb.dml.block.BLOCK_LOOT_FABRICATOR
import dev.nathanpb.dml.block.BLOCK_TRIAL_KEYSTONE
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import team.reborn.energy.api.EnergyStorage

lateinit var BLOCKENTITY_TRIAL_KEYSTONE: BlockEntityType<BlockEntityTrialKeystone>
lateinit var BLOCKENTITY_DATA_SYNTHESIZER: BlockEntityType<BlockEntityDataSynthesizer>
lateinit var BLOCKENTITY_LOOT_FABRICATOR: BlockEntityType<BlockEntityLootFabricator>
lateinit var BLOCKENTITY_DISRUPTIONS_CORE: BlockEntityType<BlockEntityDisruptionsCore>
lateinit var BLOCKENTITY_FADING_GLITCHED_TILE: BlockEntityType<BlockEntityFadingGlitchedTile>

private fun <E: BlockEntity> register(block: Block, builder: (BlockPos, BlockState)->E): BlockEntityType<E> = Registry.register(
    BuiltInRegistries.BLOCK_ENTITY_TYPE,
    BuiltInRegistries.BLOCK.getKey(block),
    FabricBlockEntityTypeBuilder.create(builder, block).build()
)

fun registerBlockEntityTypes() {
    BLOCKENTITY_TRIAL_KEYSTONE = register(BLOCK_TRIAL_KEYSTONE, ::BlockEntityTrialKeystone)
    BLOCKENTITY_DATA_SYNTHESIZER = register(BLOCK_DATA_SYNTHESIZER, ::BlockEntityDataSynthesizer).also {
        EnergyStorage.SIDED.registerForBlockEntity(
            { blockEntity, direction ->
                if(blockEntity.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING) == direction) {
                    return@registerForBlockEntity null
                }
                return@registerForBlockEntity blockEntity.energyStorage
            },
            it
        )
    }
    BLOCKENTITY_LOOT_FABRICATOR = register(BLOCK_LOOT_FABRICATOR, ::BlockEntityLootFabricator).also {
        EnergyStorage.SIDED.registerForBlockEntity(
            { blockEntity, _ -> blockEntity.energyStorage },
            it
        )
    }

    BLOCKENTITY_DISRUPTIONS_CORE = register(BLOCK_DISRUPTIONS_CORE, ::BlockEntityDisruptionsCore)
    BLOCKENTITY_FADING_GLITCHED_TILE = register(BLOCK_FADING_GLITCHED_TILE, ::BlockEntityFadingGlitchedTile)
}
