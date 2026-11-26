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

package dev.nathanpb.dml.modular_armor.effects

import dev.nathanpb.dml.enums.DataModelTier
import dev.nathanpb.dml.enums.EntityCategory
import dev.nathanpb.dml.event.VanillaEvents
import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.modular_armor.core.*
import dev.nathanpb.dml.modular_armor.modularArmorConfig
import dev.nathanpb.dml.modular_armor.net.UndyingCooldownUpdatePayload
import dev.nathanpb.dml.modular_armor.undyingLastUsage
import dev.nathanpb.dml.utils.firstInstanceOrNull
import dev.nathanpb.dml.utils.`if`
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.advancements.triggers.CriteriaTriggers
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionResult
import kotlin.math.max

class UndyingEffect : ModularEffect<ModularEffectTriggerPayload>(
    identifier("undying"),
    EntityCategory.ILLAGER,
    modularArmorConfig.glitchArmor.costs::undying
) {

    companion object {
        private val INSTANCE by lazy {
            ModularEffectRegistry.INSTANCE.all.firstInstanceOrNull<UndyingEffect>()
        }

        fun trigger(player: Player) = INSTANCE?.run {
            return@run ModularEffectContext.from(player)
                .run(EffectStackOption.RANDOMIZE.apply)
                .any { attemptToApply(it, ModularEffectTriggerPayload.EMPTY) == InteractionResult.SUCCESS }
                .`if` {
                    player.undyingLastUsage = player.level().gameTime
                    player.health = 1.0f
                    player.removeAllEffects()
                    player.addEffect(MobEffectInstance(MobEffects.REGENERATION, 900, 1))
                    player.addEffect(MobEffectInstance(MobEffects.ABSORPTION, 100, 1))
                    player.addEffect(MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0))
                    player.level().broadcastEntityEvent(player, 35.toByte())

                    if (player is ServerPlayer) {
                        player.awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING))
                        CriteriaTriggers.USED_TOTEM.trigger(player, ItemStack(Items.TOTEM_OF_UNDYING))
                    }
                    syncCooldown(player, modularArmorConfig.glitchArmor.undyingCooldownTime)
                    true
                }
        } ?: false
    }

    override fun registerEvents() {
        VanillaEvents.PlayerEntityTickEvent.register { player ->
            if (!player.level().isClientSide && player.level().gameTime % 40 == 0L) {
                ModularEffectContext.from(player)
                    .any { super.canApply(it, ModularEffectTriggerPayload.EMPTY) }
                    .let {
                       syncCooldown(player, if (it) modularArmorConfig.glitchArmor.undyingCooldownTime else 0)
                    }
            }
        }

    }

    override fun acceptTier(tier: DataModelTier) = tier.isMaxTier()
    override fun minimumTier(): DataModelTier = DataModelTier.SELF_AWARE
    override fun getEnergyConsumptionType() = EffectInfo.EnergyConsumptionType.USE

    private fun remainingCooldownTime(player: Player): Int = player.undyingLastUsage?.let {
        max(modularArmorConfig.glitchArmor.undyingCooldownTime - (player.level().gameTime - it), 0).toInt()
    } ?: 0

    private fun isUnderCooldown(player: Player) : Boolean {
        return remainingCooldownTime(player) > 0
    }

    override fun canApply(context: ModularEffectContext, payload: ModularEffectTriggerPayload): Boolean {
        return super.canApply(context, payload) && !isUnderCooldown(context.player)
    }

    fun syncCooldown(player: Player, maxCooldownTime: Int) {
        if (player is ServerPlayer) {
            ServerPlayNetworking.send(player, UndyingCooldownUpdatePayload(remainingCooldownTime(player).toInt(), maxCooldownTime))
        }
    }
}
