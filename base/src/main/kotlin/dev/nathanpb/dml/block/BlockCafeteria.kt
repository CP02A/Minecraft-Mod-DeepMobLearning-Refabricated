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

package dev.nathanpb.dml.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class BlockCafeteria(properties: Properties) : HorizontalDirectionalBlock(
    properties
        .destroyTime(1F)
        .explosionResistance(.5F)
        .noOcclusion()
        .mapColor(MapColor.COLOR_YELLOW)
) {
    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState = horizontalPlacementState(this, ctx)

    override fun getShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        return when(state.getValue(FACING)) {
            Direction.NORTH -> cafeteriaNorth
            Direction.SOUTH -> cafeteriaSouth
            Direction.WEST -> cafeteriaWest
            Direction.EAST -> cafeteriaEast
            else -> Shapes.empty()
        }
    }

    private val baseNorth: VoxelShape = box(4.0, 0.0, 2.0, 16.0, 12.0, 14.0)
    private val handleNorth: VoxelShape = box(0.0, 2.0, 7.0, 4.0, 10.0, 9.0)
    private val cafeteriaNorth: VoxelShape = Shapes.or(baseNorth, handleNorth)

    private val baseSouth: VoxelShape = box(0.0, 0.0, 2.0, 12.0, 12.0, 14.0)
    private val handleSouth: VoxelShape = box(12.0, 2.0, 7.0, 16.0, 10.0, 9.0)
    private val cafeteriaSouth: VoxelShape = Shapes.or(baseSouth, handleSouth)

    private val baseWest: VoxelShape = box(2.0, 0.0, 0.0, 14.0, 12.0, 12.0)
    private val handleWest: VoxelShape = box(7.0, 2.0, 12.0, 9.0, 10.0, 16.0)
    private val cafeteriaWest: VoxelShape = Shapes.or(baseWest, handleWest)

    private val baseEast: VoxelShape = box(2.0, 0.0, 4.0, 14.0, 12.0, 16.0)
    private val handleEast: VoxelShape = box(7.0, 2.0, 0.0, 9.0, 10.0, 4.0)
    private val cafeteriaEast: VoxelShape = Shapes.or(baseEast, handleEast)

}

/** Faces the player: the first horizontal direction the player looks at (opposite) where the block can survive. */
fun horizontalPlacementState(block: Block, ctx: BlockPlaceContext): BlockState {
    var blockState = block.defaultBlockState()

    for(direction in ctx.nearestLookingDirections) {
        if(direction.axis.isHorizontal) {
            blockState = blockState.setValue(HorizontalDirectionalBlock.FACING, direction.opposite)
            if(blockState.canSurvive(ctx.level, ctx.clickedPos)) return blockState
        }
    }
    return blockState.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
}
