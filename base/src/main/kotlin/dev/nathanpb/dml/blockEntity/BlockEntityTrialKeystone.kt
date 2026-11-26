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

import com.google.common.base.Preconditions
import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.baseConfig
import dev.nathanpb.dml.data.TrialData
import dev.nathanpb.dml.data.serializers.TrialDataSerializer
import dev.nathanpb.dml.entity.SystemGlitchEntity
import dev.nathanpb.dml.event.ModEvents
import dev.nathanpb.dml.inventory.TrialKeystoneInventory
import dev.nathanpb.dml.recipe.TrialKeystoneRecipe
import dev.nathanpb.dml.trial.*
import dev.nathanpb.dml.trial.affix.core.TrialAffix
import dev.nathanpb.dml.utils.*
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.WorldlyContainerHolder
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.ContainerHelper
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import dev.nathanpb.dml.recipe.RECIPE_TRIAL_KEYSTONE
import dev.nathanpb.dml.recipe.recipeById
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.NonNullList
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.LevelAccessor
import kotlin.math.min
import kotlin.random.Random


class BlockEntityTrialKeystone(pos: BlockPos, state: BlockState) :
    ClientSyncedBlockEntity(BLOCKENTITY_TRIAL_KEYSTONE, pos, state),
    WorldlyContainerHolder
{

    companion object {
        val BORDERS_RANGE
            get() = (baseConfig.trial.arenaRadius.squared() - 9).toDouble() .. (baseConfig.trial.arenaRadius.squared() + 9).toDouble()

        val ticker = BlockEntityTicker<BlockEntityTrialKeystone> { world, pos, _, blockEntity ->
            if (world.isClientSide) {
                when (blockEntity.clientTrialState) {
                    TrialState.NOT_STARTED -> {
                        if (!baseConfig.trial.allowStartInWrongTerrain) {
                            Minecraft.getInstance().player?.let { clientPlayer ->
                                if (clientPlayer.distanceToSqr(pos.toVec3d()) <= baseConfig.trial.arenaRadius.squared()) {
                                    blockEntity.spawnParticlesInWrongTerrain()
                                }
                            }
                        }
                    }

                    TrialState.RUNNING -> {
                        if (!baseConfig.trial.allowPlayersLeavingArena) {
                            blockEntity.pullMobsInBorders(listOf(Minecraft.getInstance().player as LivingEntity))
                        }
                    }
                    else -> {}
                }
                return@BlockEntityTicker
            }

            // lord forgive me for what i'm about to do
            // update Nov 11, 2020: what the fuck
            // update Feb 26, 2021: what the fuck
            // update Jun 09, 2021: what the fuck
            if (blockEntity.currentTrial == null) {
                blockEntity.trialToLoad?.let { trialToLoad ->
                    try {
                        val recipe = recipeById(world, RECIPE_TRIAL_KEYSTONE, trialToLoad.recipeId)
                        if (recipe != null) {
                            blockEntity.currentTrial = Trial(blockEntity, recipe, trialToLoad)
                        }
                    } catch (e: Exception) {
                        dev.nathanpb.dml.LOGGER.error("Failed to load trial at $pos: ${e.message}")
                    }
                    blockEntity.trialToLoad = null
                    blockEntity.sync()
                }
            }

            blockEntity.currentTrial?.let { trial ->
                val state = blockEntity.currentTrial?.state ?: TrialState.NOT_STARTED
                if (state != TrialState.NOT_STARTED && state != TrialState.FINISHED) {
                    if (state == TrialState.RUNNING) {
                        if (!baseConfig.trial.allowMobsLeavingArena) {
                            blockEntity.pullMobsInBorders(trial.getMonstersInArena())
                        }
                        if (!baseConfig.trial.allowPlayersLeavingArena) {

                            // Attempt to get the PlayerEntities of all the Players UUIDs participating the trial.
                            // If the entity list is empty but the list of Players UUIDs participating is not empty
                            // so it means that everyone is logged off, the trial should just keep "idling"

                            // If at least one Player is found, so it checks the distance of the list of found PlayerEntities
                            // and finalizes the trial if this one player is not near

                            // If the list of Player UUIDs participating the Trial is empty
                            // it means that some kind of failure occurred and the trial is finalized too

                            val playerEntities = trial.world.getPlayersByUUID(trial.players)
                            if (trial.players.isEmpty() || (playerEntities.isNotEmpty() && !blockEntity.arePlayersAround(playerEntities))) {
                                trial.end(TrialEndReason.NO_ONE_IS_AROUND)
                            }
                        }
                    }
                    trial.tick()
                }
            }
        }
    }

    var currentTrial: Trial? = null

    var clientTrialState = TrialState.NOT_STARTED

    private val internalInventory = TrialKeystoneInventory()

    private var trialToLoad: TrialData? = null

    init {
        ModEvents.TrialEndEvent.register { trial, reason ->
            if (currentTrial == trial && !trial.world.isClientSide) {
                currentTrial = null
                clientTrialState = TrialState.NOT_STARTED

                if (reason == TrialEndReason.SUCCESS) {
                    internalInventory.dropAll(trial.world, blockPos)
                } else {
                    internalInventory.clearContent()
                }

                sync()
            }
        }

        ModEvents.TrialStateChanged.register {
            // Keystones do not have finished state, instead this is handled in onTrialEnd
            if (level?.isClientSide == false && it.state != TrialState.FINISHED) {
                clientTrialState = it.state
                sync()
            }
        }
    }

    fun createTrial(recipe: TrialKeystoneRecipe, affixes: List<TrialAffix>): Trial {
        val players = level?.getEntitiesAroundCircle(
            EntityTypeTest.forClass(Player::class.java),
            blockPos, baseConfig.trial.arenaRadius.toDouble()
        ).orEmpty()

        if (players.isNotEmpty()) {
            return Trial(this, recipe, players.map(Player::getUUID), affixes)
        } else throw TrialKeystoneNoPlayersAround(this)
    }

    fun startTrial(trial: Trial, key: ItemStack?) {
        level?.let { world ->
            if (currentTrial?.state in listOf(TrialState.WARMUP, TrialState.RUNNING)) {
                throw TrialKeystoneIllegalStartException(trial)
            }
            internalInventory.dropAll(world, blockPos)
            checkTerrain().let { wrongTerrain ->
                if (wrongTerrain.isEmpty()) {
                    currentTrial = trial
                    trial.start()

                    if (key != null) {
                        internalInventory.addItem(key)
                    }
                } else throw TrialKeystoneWrongTerrainException(this, wrongTerrain)
            }
        }
    }

    /**
     * Spawns fire particles above the blocks in wrong place
     */
    private fun spawnParticlesInWrongTerrain() {
        checkTerrain().let { wrongTerrain ->
            if (wrongTerrain.isNotEmpty()) {
                (0 .. min(wrongTerrain.size / 4, 1)).let { _ ->
                    wrongTerrain.random().let {
                        level?.addParticle(
                            RenderUtils.GLITCH_PARTICLE,
                            true,
                            false,
                            it.x + Random.nextDouble() - .1,
                            it.y + 1.0,
                            it.z + Random.nextDouble() - .1,
                            0.0, 0.0, 0.0
                        )
                    }
                }
            }
        }
    }

    /**
     * Pulls the mobs spawned by the waves back to the center
     */
    private fun pullMobsInBorders(mobs: List<LivingEntity>) {
        val posVector = blockPos.toVec3d()
        mobs.filter(LivingEntity::isAlive)
            .filterNot {
                baseConfig.trial.allowPlayersLeavingArena
                        && currentTrial != null
                        && (it as? SystemGlitchEntity)?.trial == currentTrial
            }.filter {
                val squaredDistance = it.distanceToSqr(posVector.x, posVector.y, posVector.z)
                squaredDistance in BORDERS_RANGE
            }.forEach {
                val vector = it.position().subtract(blockPos.toVec3d()).scale(-0.1)
                it.push(vector.x, vector.y, vector.z)
            }
    }

    private fun arePlayersAround(players: List<Player>) = blockPos.toVec3d().let { posVec ->
        players.any {
            it.distanceToSqr(posVec.x, posVec.y, posVec.z) <= baseConfig.trial.arenaRadius.squared()
        }
    }

    /**
     * Checks the effective terrain and returns a list of wrong blocks
     *
     * @return the list of erroneous blocks
     */
    fun checkTerrain(): List<BlockPos> {
        return if (!baseConfig.trial.allowStartInWrongTerrain) {
            val pos = blockPos
            val world = level
            mutableListOf<BlockPos>().also { list ->
                val radInt = baseConfig.trial.arenaRadius
                (pos.x - radInt .. pos.x + radInt).forEach { x ->
                    (pos.z - radInt .. pos.z + radInt).forEach { z ->

                        // Searching for non-solid blocks bellow the circle
                        val floorPos = BlockPos(x, pos.y - 1, z)
                        if (floorPos.distSqr(pos) <= baseConfig.trial.arenaRadius.squared()) {
                            world?.getBlockState(floorPos)?.let { floorBlock ->
                                if (!floorBlock.isFaceSturdy(world!!, floorPos, Direction.UP)) {
                                    list += floorPos
                                }
                            }
                        }

                        // Searching for non-air blocks inside the dome
                        (pos.y .. pos.y + radInt).forEach { y ->
                            val innerBlockPos = BlockPos(x, y, z)
                            if (
                                innerBlockPos != pos
                                && innerBlockPos.distSqr(pos) < baseConfig.trial.arenaRadius.squared()
                                && world?.getBlockState(innerBlockPos)?.isAir != true
                            ) {
                                list += innerBlockPos
                            }
                        }
                    }
                }
            }.toList()
        } else emptyList()
    }

    override fun getContainer(state: BlockState, world: LevelAccessor, pos: BlockPos): WorldlyContainer {
        return (world.getBlockEntity(pos) as BlockEntityTrialKeystone).internalInventory
    }

    override fun toTag(output: ValueOutput) {
        ContainerHelper.saveAllItems(output, internalInventory.items())
        currentTrial?.also { trial ->
            if (trial.state in arrayOf(TrialState.WARMUP, TrialState.RUNNING)) {
                output.store("trial", CompoundTag.CODEC, TrialData(trial).tag)
            }
        }
    }

    override fun fromTag(input: ValueInput) {
        if (currentTrial == null) {
            val stacks = NonNullList.withSize(internalInventory.containerSize, ItemStack.EMPTY)
            ContainerHelper.loadAllItems(input, stacks)
            internalInventory.setStacks(stacks)
            input.read("trial", CompoundTag.CODEC).ifPresent {
                trialToLoad = TrialData(it)
            }
        }
    }

    override fun toClientTag(output: ValueOutput) {
        output.putString("${MOD_ID}:state", (currentTrial?.state ?: TrialState.NOT_STARTED).name)
    }

    override fun fromClientTag(input: ValueInput) {
        clientTrialState = input.getStringOr("${MOD_ID}:state", "").let { name ->
            if (name.isNotEmpty()) TrialState.valueOf(name) else TrialState.NOT_STARTED
        }
    }
}
