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

package dev.nathanpb.dml.listener

import dev.nathanpb.dml.recipe.RECIPE_CRUSHING
import dev.nathanpb.dml.recipe.recipesOf
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.Prediction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.level.Level

class CrushingRecipeListener : AttackBlockCallback {  // FIXME
    override fun interact(
        player: Player,
        world: Level,
        hand: InteractionHand,
        pos: BlockPos,
        direction: Direction
    ): InteractionResult {
        if (!world.isClientSide) {
            val stack = player.getItemInHand(hand)
            val input = SingleRecipeInput(stack)
            val state = world.getBlockState(pos)
            recipesOf(world, RECIPE_CRUSHING).firstOrNull {
                state.block == it.block && it.matches(input, world)
            }?.apply {
                val result = assemble(input)
                stack.shrink(1)
                player.inventory.placeItemBackInInventory(result, Prediction.SERVER_ONLY)
                return InteractionResult.CONSUME
            }
        }
        return InteractionResult.PASS
    }

}
