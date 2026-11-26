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

package dev.nathanpb.dml.entity

import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.baseConfig
import dev.nathanpb.dml.blockEntity.BlockEntityTrialKeystone
import dev.nathanpb.dml.entity.goal.GlitchTeleportTowardsPlayerGoal
import dev.nathanpb.dml.enums.DataModelTier
import dev.nathanpb.dml.item.ItemEmeritusHat
import dev.nathanpb.dml.item.ItemGlitchSword
import dev.nathanpb.dml.trial.TrialGriefPrevention
import dev.nathanpb.dml.utils.randomAround
import dev.nathanpb.dml.utils.runningTrials
import dev.nathanpb.dml.utils.toVec3d
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import kotlin.math.min
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal

class SystemGlitchEntity(type: EntityType<out SystemGlitchEntity>, world: Level) : Monster(type, world) {
    companion object {
        fun createMobAttributes(): AttributeSupplier.Builder = LivingEntity.createLivingAttributes()
            .add(Attributes.FOLLOW_RANGE, 4.0)
            .add(Attributes.ATTACK_KNOCKBACK)
            .add(Attributes.ATTACK_DAMAGE, 12.0)
            .add(Attributes.MOVEMENT_SPEED, 1.0)
    }

    /**
     * This property is used to set the glitch's max health in the Trial's start time
     * Because at that time the entity isn't bound to the trial yet
     * And the lazy property [trial] will be null at that time, causing it to have null value forever
     *
     * If its value is null, the entity is not attuned to a trial yet (in case of reading in the entity spawn time)
     * Or it just doesn't belong to a trial (in case of reading when a trial is already started)
     */
    var tier: DataModelTier? = null

    /**
     * This property is persisted in the Entity's tag. Its main use if to tell whether the entity should despawn on world reloads
     */
    var belongsToTrial = false

    /**
     * Find's the trial that this entity belongs to
     *
     * PLEASE do not call this during the trial initialization, this will ruin this entity's instance
     * causing its trial to be null until the game reload
     */
    val trial by lazy {
        level().runningTrials.firstOrNull {
            it.systemGlitch == this
        }
    }

    /**
     * Modify the base attack damage of the glitch based in the current tier of the trial
     */
    override fun getAttributeValue(attribute: Holder<Attribute>): Double {
        val tier = tier
        if (attribute == Attributes.ATTACK_DAMAGE && tier != null) {
            val tierMultiplier = (tier.ordinal / DataModelTier.values().size.dec()) + .5
            return super.getAttributeValue(attribute) * tierMultiplier
        }
        return super.getAttributeValue(attribute)
    }

    override fun tick() {
        super.tick()
        if (tier == null && belongsToTrial) {
            remove(RemovalReason.DISCARDED)
        }
    }

    override fun registerGoals() {
        super.registerGoals()
        goalSelector.addGoal(1, RandomStrollGoal(this, 1.0))
        goalSelector.addGoal(1, RandomLookAroundGoal(this))
        goalSelector.addGoal(2, MeleeAttackGoal(this, 1.0, true))

        targetSelector.addGoal(1, GlitchTeleportTowardsPlayerGoal(this))
        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
        targetSelector.addGoal(3, HurtByTargetGoal(this))

        // TODO rage attacks
        // TODO ranged attack
    }

    /**
     * Teleports the entity in a random block of the radius of [radius] around [at]
     *
     * If the entity belongs to a trial, the entity will not be teleported to blocks outside its trial area
     * In that case, the entity will attempt to teleport maximum [maxAttempts] times checking if the random picked block is inside its [trial] area
     * And successfully teleports if it does
     *
     * If the config entry "trial.allowPlayersLeavingArena" is set to true, then it will completely
     * ignore the protections and just teleport the entity
     *
     * @param at The initial block to find random ones around
     * @param radius The radius to find random blocks around
     * @param maxAttempts The maximum attempts to teleport the entity, in case of trying to teleport outside trials
     * @returns True if the entity was teleported, false if the max teleport attempts was reached and the entity was not teleported
     */
    fun tryTeleportRandomly(at: BlockPos, radius: Int, maxAttempts: Int = 5): Boolean {
        var teleportsAttempt = 0
        do {
            val pos = at.randomAround(radius, 0, radius)
            val canTeleport =
            baseConfig.trial.allowPlayersLeavingArena || trial?.let { trial ->
                TrialGriefPrevention.isBlockProtected(pos, trial)
            } ?: true

            if (canTeleport) {
                pos.toVec3d().apply {
                    teleportTo(x, y, z)
                    return true
                }
            }
        } while (teleportsAttempt++ < maxAttempts)
        return false
    }

    override fun requiresCustomPersistence() = true

    override fun canRide(entity: Entity): Boolean {
        return false
    }

    override fun canBeAffected(effect: MobEffectInstance): Boolean {
        return effect.effect != MobEffects.POISON && effect.effect != MobEffects.WITHER
    }

    override fun addAdditionalSaveData(output: ValueOutput) {
        super.addAdditionalSaveData(output)
        output.putBoolean("${MOD_ID}.belongsToTrial", belongsToTrial)
    }

    override fun readAdditionalSaveData(input: ValueInput) {
        super.readAdditionalSaveData(input)
        belongsToTrial = input.getBooleanOr("${MOD_ID}.belongsToTrial", false)
    }

    override fun actuallyHurt(world: ServerLevel, source: DamageSource, amount: Float) {
        val finalAmount = ItemGlitchSword.getIncreasedDamage(source, amount)
        if(!source.`is`(DamageTypes.FELL_OUT_OF_WORLD) && baseConfig.trial.systemGlitch.damageLimiter > 0) {
            super.actuallyHurt(world, source, min(finalAmount, baseConfig.trial.systemGlitch.damageLimiter))
        } else {
            super.actuallyHurt(world, source, finalAmount)
        }

        trial?.pos?.let { pos ->
            world.getBlockEntity(pos)?.setChanged()
        }
    }
}
