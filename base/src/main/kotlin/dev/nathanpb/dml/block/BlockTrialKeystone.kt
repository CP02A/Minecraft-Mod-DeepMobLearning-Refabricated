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

import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.baseConfig
import dev.nathanpb.dml.blockEntity.BlockEntityTrialKeystone
import dev.nathanpb.dml.data.trialKeyData
import dev.nathanpb.dml.item.ItemTrialKey
import dev.nathanpb.dml.recipe.TrialKeystoneRecipe
import dev.nathanpb.dml.trial.TrialKeystoneIllegalStartException
import dev.nathanpb.dml.trial.TrialKeystoneWrongTerrainException
import dev.nathanpb.dml.utils.RenderUtils
import dev.nathanpb.dml.utils.takeOrNull
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.ChatFormatting
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level

class BlockTrialKeystone(properties: Properties) : Block(
    properties.strength(10F, 12F)
), EntityBlock {

    override fun useItemOn(stackInHand: ItemStack, state: BlockState, world: Level, pos: BlockPos, player: Player, hand: InteractionHand, hit: BlockHitResult): InteractionResult {
        if (!world.isClientSide) {
            (world.getBlockEntity(pos) as? BlockEntityTrialKeystone)?.let { blockEntity ->
                if (stackInHand.item is ItemTrialKey) {
                    stackInHand.trialKeyData?.let { data ->
                        val recipe = TrialKeystoneRecipe.findOrNull(world, data)
                        if (recipe != null) {
                            try {
                                val trial = blockEntity.createTrial(recipe, data.affixes)
                                blockEntity.startTrial(
                                    trial,
                                    takeOrNull(
                                        !player.isCreative
                                        && baseConfig.trial.trialKeyConsume
                                        && baseConfig.trial.trialKeyReturnIfSucceed
                                    ) { stackInHand }
                                )

                                if (!player.isCreative && baseConfig.trial.trialKeyConsume)
                                    stackInHand.shrink(1)

                                return InteractionResult.CONSUME
                            } catch (ex: TrialKeystoneIllegalStartException) {
                                return InteractionResult.PASS
                            } catch (ex: TrialKeystoneWrongTerrainException) {
                                player.sendSystemMessage(
                                    Component.translatable("chat.$MOD_ID.trial_wrong_terrain").setStyle(RenderUtils.STYLE)
                                )
                                blockEntity.checkTerrain().forEach {
                                    player.sendSystemMessage(
                                        getInvalidTerrainText(
                                            it.x, it.y, it.z,
                                            !world.getBlockState(it).isFaceSturdy(world, it, Direction.UP)
                                        )
                                    )
                                }
                            }
                        } else {
                            player.sendSystemMessage(Component.translatable("chat.${MOD_ID}.trial_no_recipe"))
                        }
                        return InteractionResult.FAIL
                    }
                }
            }
        }
        return super.useItemOn(stackInHand, state, world, pos, player, hand, hit)
    }

    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return Shapes.box(0.0, 0.0, 0.0, 1.0, 0.5, 1.0)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : BlockEntity> getTicker(world: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T> {
        return BlockEntityTrialKeystone.ticker as BlockEntityTicker<T>
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState) = BlockEntityTrialKeystone(pos, state)


    private fun getInvalidTerrainText(x: Int, y: Int, z: Int, isFloor: Boolean): MutableComponent {
        return Component.literal("- ").withStyle(ChatFormatting.WHITE)
            .append(Component.translatable("chat.$MOD_ID.trial_wrong_terrain." + (if(isFloor) "floor" else "dome"))
                .setStyle(RenderUtils.STYLE))
            .append(Component.literal(String.format(" (%d, %d, %d)", x, y, z))
                .withStyle(ChatFormatting.WHITE))
    }
}