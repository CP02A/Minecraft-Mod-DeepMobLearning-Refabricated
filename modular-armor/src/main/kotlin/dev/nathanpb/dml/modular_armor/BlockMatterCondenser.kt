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

import dev.nathanpb.dml.block.horizontalPlacementState
import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.itemgroup.ITEM_GROUP_KEY
import dev.nathanpb.dml.modular_armor.ItemModularGlitchArmor.Companion.GLITCH_BOOTS
import dev.nathanpb.dml.modular_armor.screen.MatterCondenserScreenHandler
import dev.nathanpb.dml.modular_armor.screen.MatterCondenserScreenHandlerFactory
import dev.nathanpb.dml.utils.getFullCapacityStack
import dev.nathanpb.dml.utils.registerBlock
import dev.nathanpb.dml.utils.registerBlockItem
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Containers
import net.minecraft.world.InteractionResult
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.WorldlyContainerHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
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
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class BlockMatterCondenser(properties: Properties) : HorizontalDirectionalBlock(
    properties.strength(5F, 6F)
), WorldlyContainerHolder, EntityBlock {

    companion object {
        lateinit var BLOCK_MATTER_CONDENSER: BlockMatterCondenser
        val IDENTIFIER = identifier("matter_condenser")

        fun register() {
            BLOCK_MATTER_CONDENSER = registerBlock(IDENTIFIER, factory = ::BlockMatterCondenser)
            registerBlockItem(BLOCK_MATTER_CONDENSER) { it.rarity(Rarity.RARE) }

            CreativeModeTabEvents.modifyOutputEvent(ITEM_GROUP_KEY).register {
                it.insertAfter(getFullCapacityStack(GLITCH_BOOTS), BLOCK_MATTER_CONDENSER)
            }
        }
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState = horizontalPlacementState(this, ctx)

    override fun useWithoutItem(state: BlockState, world: Level, pos: BlockPos, player: Player, hit: BlockHitResult): InteractionResult {
        if (!world.isClientSide) {
            player.openMenu(MatterCondenserScreenHandlerFactory(pos) {
                syncId, playerInventory, context -> MatterCondenserScreenHandler(syncId, playerInventory, context)
            })
        }
        return InteractionResult.SUCCESS
    }

    // The block entity drops the inventory (BlockEntityMatterCondenser#preRemoveSideEffects)
    override fun affectNeighborsAfterRemoval(state: BlockState, world: ServerLevel, pos: BlockPos, movedByPiston: Boolean) {
        Containers.updateNeighboursAfterDestroy(state, world, pos)
    }

    override fun hasAnalogOutputSignal(state: BlockState) = true

    override fun getAnalogOutputSignal(state: BlockState, world: Level, pos: BlockPos, direction: Direction): Int {
        return AbstractContainerMenu.getRedstoneSignalFromContainer((world.getBlockEntity(pos) as? BlockEntityMatterCondenser)?.inventory)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : BlockEntity> getTicker(world: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
        return if(!world.isClientSide) BlockEntityMatterCondenser.ticker as BlockEntityTicker<T> else null
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return BlockEntityMatterCondenser(pos, state)
    }

    override fun getContainer(state: BlockState, world: LevelAccessor, pos: BlockPos): WorldlyContainer {
        return (world.getBlockEntity(pos) as BlockEntityMatterCondenser).inventory
    }

    override fun getShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        return voxelShape
    }

    private val voxelShape: VoxelShape = box(0.0, 0.0, 0.0, 16.0, 15.99, 16.0)

}
