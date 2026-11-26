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

package dev.nathanpb.dml.trial

import dev.nathanpb.dml.baseConfig
import dev.nathanpb.dml.misc.TRIAL_GRIEF_WHITELIST
import dev.nathanpb.dml.utils.runningTrials
import dev.nathanpb.dml.utils.squared
import dev.nathanpb.dml.utils.toBlockPos
import dev.nathanpb.dml.utils.toVec3i
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.world.entity.Entity
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.monster.Enderman
import net.minecraft.world.entity.player.Player
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import net.minecraft.world.level.ExplosionDamageCalculator

class TrialGriefPrevention : AttackBlockCallback, UseBlockCallback {

    companion object {
        fun isInArea(trialPos: BlockPos, pos: BlockPos): Boolean {
            return trialPos.distSqr(pos.toVec3i()) <= baseConfig.trial.arenaRadius.squared()
        }

        fun isBlockProtected(world: Level, pos: BlockPos): Boolean {
            return world.runningTrials.any {
                isBlockProtected(pos, it)
            }
        }

        fun isBlockProtected(pos: BlockPos, trial: Trial) : Boolean {
            return isInArea(trial.pos, pos) && pos.y >= trial.pos.y - 1
        }
    }

    override fun interact(player: Player, world: Level, hand: InteractionHand, pos: BlockPos, direction: Direction): InteractionResult {
        return if (
            !world.isClientSide
            && baseConfig.trial.interactGriefPrevention
            && isBlockProtected(world, pos)
            && !world.getBlockState(pos).`is`(TRIAL_GRIEF_WHITELIST)
            && "grave" !in BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).block).path
        ) {
            InteractionResult.FAIL
        } else InteractionResult.PASS
    }

    override fun interact(player: Player, world: Level, hand: InteractionHand, hitResult: BlockHitResult): InteractionResult {
        if (baseConfig.trial.buildGriefPrevention) {
            val posOfPlacedBlock = hitResult.blockPos.relative(hitResult.direction)
            if (!world.isClientSide && isBlockProtected(world, posOfPlacedBlock)) {
                return InteractionResult.FAIL
            }
        }
        return InteractionResult.PASS
    }

    fun explode(
        world: Level,
        entity: Entity?,
        damageSource: DamageSource?,
        behavior: ExplosionDamageCalculator?,
        pos: BlockPos,
        power: Float,
        createFire: Boolean,
        destructionType: Level.ExplosionInteraction
    ): InteractionResult {
        if (
            !world.isClientSide
            && baseConfig.trial.explosionGriefPrevention
            && destructionType != Level.ExplosionInteraction.NONE
            && isBlockProtected(world, pos)
        ) {
            world.explode(entity, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), power, createFire, Level.ExplosionInteraction.NONE)
            return InteractionResult.FAIL
        }
        return InteractionResult.PASS
    }

    fun onEndermanTeleport(entity: Enderman, pos: Vec3): InteractionResult {
        if (!baseConfig.trial.allowMobsLeavingArena) {
            val isInProtectedArea = isBlockProtected(entity.level(), entity.blockPosition())
            val toProtectedArea = isBlockProtected(entity.level(), pos.toBlockPos())

            // The first real use of xor in my entire life
            // 22/07/2020, 5:19 AM - Passo Fundo, Brazil
            if (isInProtectedArea xor toProtectedArea) {
                return InteractionResult.FAIL
            }
        }
        return InteractionResult.SUCCESS
    }
}
