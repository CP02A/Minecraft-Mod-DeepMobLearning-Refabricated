/*
 * Copyright (C) 2020 Nathan P. Bombana, IterationFunk
 *
 * This file is part of Deep Mob Learning: Refabricated.
 *
 * Deep Mob Learning: Refabricated is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Deep Mob Learning: Refabricated is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Deep Mob Learning: Refabricated.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.nathanpb.dml.block

import dev.nathanpb.dml.blockEntity.BlockEntityLootFabricator
import dev.nathanpb.dml.screen.handler.LootFabricatorScreenHandler
import dev.nathanpb.dml.screen.handler.LootFabricatorScreenHandlerFactory
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Containers
import net.minecraft.world.InteractionResult
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.WorldlyContainerHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.phys.BlockHitResult

class BlockLootFabricator(properties: Properties) : HorizontalDirectionalBlock(
    properties.strength(5F, 6F)
), WorldlyContainerHolder, EntityBlock {

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : BlockEntity> getTicker(
        world: Level,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return if (world.isClientSide) null else BlockEntityLootFabricator.ticker as BlockEntityTicker<T>
    }

    override fun useWithoutItem(state: BlockState, world: Level, pos: BlockPos, player: Player, hit: BlockHitResult): InteractionResult {
        if (!world.isClientSide) {
            player.openMenu(LootFabricatorScreenHandlerFactory(pos) { syncId, inventory, context ->
                LootFabricatorScreenHandler(syncId, inventory, context)
            })
        }
        return InteractionResult.SUCCESS
    }

    override fun affectNeighborsAfterRemoval(state: BlockState, world: ServerLevel, pos: BlockPos, movedByPiston: Boolean) {
        Containers.updateNeighboursAfterDestroy(state, world, pos)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState = horizontalPlacementState(this, ctx)

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = BlockEntityLootFabricator(pos, state)

    override fun getContainer(state: BlockState, world: LevelAccessor, pos: BlockPos): WorldlyContainer {
        return (world.getBlockEntity(pos) as BlockEntityLootFabricator).inventory
    }

    override fun hasAnalogOutputSignal(state: BlockState) = true

    override fun getAnalogOutputSignal(state: BlockState, world: Level, pos: BlockPos, direction: Direction): Int {
        return AbstractContainerMenu.getRedstoneSignalFromContainer((world.getBlockEntity(pos) as? BlockEntityLootFabricator)?.inventory)
    }
}
