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

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

/** Remembers the block this tile replaced, to turn back into it. */
class BlockEntityFadingGlitchedTile(
    pos: BlockPos, state: BlockState
) : BlockEntity(BLOCKENTITY_FADING_GLITCHED_TILE, pos, state) {


    var replacedState: BlockState = Blocks.AIR.defaultBlockState()

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        replacedState = input.read("BlockState", BlockState.CODEC).orElse(Blocks.AIR.defaultBlockState())
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        output.store("BlockState", BlockState.CODEC, replacedState)
    }

}
