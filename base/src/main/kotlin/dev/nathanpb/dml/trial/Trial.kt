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

import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.blockEntity.BlockEntityTrialKeystone
import dev.nathanpb.dml.baseConfig
import dev.nathanpb.dml.data.TrialData
import dev.nathanpb.dml.entity.SYSTEM_GLITCH_ENTITY_TYPE
import dev.nathanpb.dml.entity.SystemGlitchEntity
import dev.nathanpb.dml.enums.EntityCategory
import dev.nathanpb.dml.event.ModEvents
import dev.nathanpb.dml.item.ITEM_EMERITUS_HAT
import dev.nathanpb.dml.recipe.TrialKeystoneRecipe
import dev.nathanpb.dml.trial.affix.core.TrialAffix
import dev.nathanpb.dml.utils.*
import net.minecraft.world.BossEvent
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import java.util.*
import kotlin.random.Random
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.world.entity.item.ItemEntity

class Trial (
    val world: Level,
    val pos: BlockPos,
    val recipe: TrialKeystoneRecipe,
    val players: HashSet<UUID>,
    val affixes: List<TrialAffix>
) {

    constructor(
        keystone: BlockEntityTrialKeystone,
        recipe: TrialKeystoneRecipe,
        players: List<UUID>,
        affixes: List<TrialAffix>
    ): this(
        keystone.level!!,
        keystone.blockPos,
        recipe,
        players.toHashSet(),
        affixes
    )

    constructor(keystone: BlockEntityTrialKeystone, recipe: TrialKeystoneRecipe, data: TrialData): this(
        keystone,
        recipe,
        data.playersUuids,
        data.affixes
    ) {
        state = data.state
        tickCount = data.tickCount
        if (state in arrayOf(TrialState.WARMUP, TrialState.RUNNING)) {
            start(true)
        }
        loadedSystemGlitchHealth = data.glitchHealth
    }

    companion object {
        val BAR_TEXT: MutableComponent by lazy {
            Component.translatable("bar.${MOD_ID}.trial")
        }

        val BAR_TEXT_SUCCESS: MutableComponent by lazy {
            Component.translatable("bar.${MOD_ID}.trial_success")
        }

        val BAR_TEXT_FAIL: MutableComponent by lazy {
            Component.translatable("bar.${MOD_ID}.trial_fail")
        }
        val BAR_TEXT_FAIL_TIMEOUT: MutableComponent by lazy {
            Component.translatable("bar.${MOD_ID}.trial_fail.timeout")
        }
    }

    var systemGlitch: SystemGlitchEntity? = null
        private set

    var state: TrialState = TrialState.NOT_STARTED
        private set

    var loadedSystemGlitchHealth: Float? = null
        private set

    private val tickableAffixes = affixes.filterIsInstance<TrialAffix.TickableAffix>()


    var tickCount = 0; private set
    private val bar = ServerBossEvent(UUID.randomUUID(), BAR_TEXT, BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10).also {
        it.isVisible = false
    }

    fun tick() {
        if (!world.isClientSide && state != TrialState.NOT_STARTED) {
            if (state in arrayOf(TrialState.WARMUP, TrialState.RUNNING)) {
                // Resyncs which players will have the bossbar every second
                if (tickCount % 20 == 0) {
                    bar.removeAllPlayers()
                    world.getPlayersByUUID(players)
                        .filterIsInstance<ServerPlayer>()
                        .forEach(bar::addPlayer)
                }

                when (state) {
                    TrialState.RUNNING -> {
                        if (tickCount % recipe.waveRespawnTimeout == 0) {
                            if (getMonstersInArena().size < baseConfig.trial.maxMobsInArena) {
                                spawnWave()
                            }
                        }

                        systemGlitch?.let { systemGlitch ->
                            if (!baseConfig.trial.allowPlayersLeavingArena) {
                                val glitchPos = systemGlitch.blockPosition()
                                if (glitchPos.y < pos.y || !TrialGriefPrevention.isInArea(pos, glitchPos)) {
                                    pos.toVec3d().apply {
                                        systemGlitch.teleportTo(x, y, z)
                                    }
                                }
                            }
                            bar.progress = systemGlitch.health / systemGlitch.maxHealth
                        }

                        if (systemGlitch?.isAlive != true) {
                            end(TrialEndReason.SUCCESS)
                        }

                        if (tickCount > baseConfig.trial.maxTime) {
                            end(TrialEndReason.TIMED_OUT)
                        }

                        tickableAffixes.forEach {
                            it.tick(this)
                        }
                    }

                    TrialState.WARMUP -> {
                        if (tickCount >= baseConfig.trial.warmupTime) {
                            state = TrialState.RUNNING
                            ModEvents.TrialStateChanged.invoker().invoke(this)
                            spawnSystemGlitch()
                            world.playSound(
                                null,
                                pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                                SoundEvents.NOTE_BLOCK_BASS,
                                SoundSource.BLOCKS,
                                1F, 1F
                            )
                        } else if (tickCount % 20 == 0) {
                            world.playSound(
                                null,
                                pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                                SoundEvents.NOTE_BLOCK_BASS,
                                SoundSource.BLOCKS,
                                1.5F, .75F
                            )
                        }
                    }
                    else -> {} // Suppress non-exhaustive when
                }
            }
            tickCount++
        }
    }

    fun start(forceStart: Boolean = false) {
        if (forceStart || state == TrialState.NOT_STARTED) {
            state = TrialState.WARMUP
            ModEvents.TrialStateChanged.invoker().invoke(this)
            world.runningTrials += this
            bar.isVisible = true
        } else throw TrialKeystoneIllegalStartException(this)
    }

    fun end(reason: TrialEndReason) {
        if (state == TrialState.RUNNING) {
            when (reason) {
                TrialEndReason.SUCCESS -> {
                    world.getPlayersByUUID(players).forEach {
                        it.sendSystemMessage(BAR_TEXT_SUCCESS)
                    }
                    dropRewards()
                }
                TrialEndReason.NO_ONE_IS_AROUND -> {
                    world.getPlayersByUUID(players).forEach {
                        it.sendSystemMessage(BAR_TEXT_FAIL)
                    }
                    playFailSounds()
                }
                TrialEndReason.TIMED_OUT -> {
                    world.getPlayersByUUID(players).forEach {
                        it.sendSystemMessage(BAR_TEXT_FAIL_TIMEOUT)
                    }
                    playFailSounds()
                }
            }

            bar.isVisible = false
            world.runningTrials -= this
            state = TrialState.FINISHED
            systemGlitch?.remove(Entity.RemovalReason.DISCARDED)
            ModEvents.TrialStateChanged.invoker().invoke(this)
            ModEvents.TrialEndEvent.invoker().invoke(this, reason)
        } else throw TrialKeystoneIllegalEndException(this)
    }

    private fun playFailSounds() {
        world.getPlayersByUUID(players).filterIsInstance<ServerPlayer>().forEach {
            it.connection.send(ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.WITHER_SPAWN), SoundSource.BLOCKS,
                it.x, it.y, it.z, 1F, 1F, it.random.nextLong()
            ))
        }
    }

    private fun spawnSystemGlitch() {
        (world as? ServerLevel)?.let { world ->
            systemGlitch = SYSTEM_GLITCH_ENTITY_TYPE.spawn(
                world, null, null, pos.offset(0, 2, 0), EntitySpawnReason.EVENT, false, false
            )?.also {
                it.tier = recipe.tier
                it.health = loadedSystemGlitchHealth ?: it.maxHealth
                if (recipe.category == EntityCategory.GHOST && recipe.tier.isMaxTier() && Random.nextFloat() < .0666 ) {
                    it.setItemSlot(EquipmentSlot.HEAD, ItemStack(ITEM_EMERITUS_HAT))
                    // Always dropped (vanilla equipment only drops sometimes)
                    it.setGuaranteedDrop(EquipmentSlot.HEAD)
                }
                it.belongsToTrial = true
            }
        }
    }

    private fun dropRewards() {
        recipe.copyRewards().map {
            pos.toVec3d().run {
                ItemEntity(world, x, y + 1, z, it)
            }
        }.forEach {
            world.addFreshEntity(it)
        }
    }

    private fun spawnWave() {
        (world as? ServerLevel)?.let { world ->
            (0 until recipe.waveEntityCount).map {
                val distributionTable = BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(recipe.category.tagKey)
                    .associateWith {
                        val id = BuiltInRegistries.ENTITY_TYPE.getKey(it.value()).toString()
                        recipe.spawnRate.entries
                            .last { (k) -> k.matches(id) }
                            .value
                    }

                val entity = discreteDistribution(distributionTable).value()
                entity.spawn(
                    world,
                    null, null,
                    pos.offset(Random.nextInt(-2, 2), 5, Random.nextInt(-2, 2)),
                    EntitySpawnReason.SPAWNER,
                    false, false
                )
            }.let {
                ModEvents.TrialWaveSpawnEvent.invoker().invoke(
                    this,
                    it.filterIsInstance<LivingEntity>()
                )
            }
        }
    }

    fun getMonstersInArena(): List<Monster> {
        return world.getEntitiesAroundCircle(
            EntityTypeTest.forClass(Monster::class.java),
            pos,
            baseConfig.trial.arenaRadius.toDouble()
        )
    }
}
