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

package dev.nathanpb.dml.entity.goal

import dev.nathanpb.dml.baseConfig
import dev.nathanpb.dml.entity.SystemGlitchEntity
import dev.nathanpb.dml.trial.TrialGriefPrevention
import dev.nathanpb.dml.utils.getPlayersByUUID
import dev.nathanpb.dml.utils.squared
import net.minecraft.world.entity.ai.targeting.TargetingConditions
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import kotlin.random.Random

/**
 * Makes the Glitch teleports towards a player, in a location of 2 blocks around its target
 *
 * If the current glitch is the boss of a trial, it will pick a random participant of the trial as its target
 * If not, the target picked will be the nearest player
 *
 * This has a timeout of 100 ticks between teleports
 * The entity has 5% chance of teleporting per tick if the timeout its cleared
 * It also sets the entity's target to the target the goal picked
 */
class GlitchTeleportTowardsPlayerGoal(private val glitch: SystemGlitchEntity) : NearestAttackableTargetGoal<Player>(
    glitch,
    Player::class.java,
    false
) {

    private var ticksToTeleportCountdown = 0

    override fun canUse(): Boolean {
        if (baseConfig.trial.systemGlitch.teleportChance <= 0) {
            return false
        }

        glitch.trial.let { trial ->
            target = trial?.world?.getPlayersByUUID(trial.players)?.filter {
                baseConfig.trial.allowPlayersLeavingArena || TrialGriefPrevention.isInArea(trial.pos, it.blockPosition())
            }?.randomOrNull() ?: (glitch.level() as? ServerLevel)?.getNearestPlayer(TargetingConditions.forCombat(), glitch)
        }

        return target != null
    }

    override fun stop() {
        target = null
        ticksToTeleportCountdown = 0
        super.stop()
    }

    override fun tick() {
        if (ticksToTeleportCountdown > 0) {
            ticksToTeleportCountdown--
        }
        if (target != null && ticksToTeleportCountdown <= 0 && Random.nextFloat() <= baseConfig.trial.systemGlitch.teleportChance) {
            if (target!!.distanceToSqr(glitch) >= baseConfig.trial.systemGlitch.teleportMinDistance.squared()) {
                if (glitch.tryTeleportRandomly(target!!.blockPosition(), baseConfig.trial.systemGlitch.teleportAroundPlayerRadius)) {
                    ticksToTeleportCountdown = baseConfig.trial.systemGlitch.teleportDelay
                    glitch.target = target
                    return
                }
            }
        }
    }
}
