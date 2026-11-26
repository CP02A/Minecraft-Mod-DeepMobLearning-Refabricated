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
import dev.nathanpb.dml.utils.firstInstanceOrNull
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.InteractionResult
import kotlin.math.roundToInt

class ArcheryEffect : ModularEffect<ModularEffectTriggerPayload>(
    identifier("archery"),
    EntityCategory.SKELETON,
    modularArmorConfig.glitchArmor.costs::archery
) {

    companion object {

        private val INSTANCE by lazy {
            ModularEffectRegistry.INSTANCE.all.firstInstanceOrNull<ArcheryEffect>()
        }

        private fun levels(player: Player): Int {
            return ModularEffectContext.from(player)
                .run(EffectStackOption.PRIORITIZE_GREATER.apply)
                .firstOrNull {
                    INSTANCE?.canApply(it, ModularEffectTriggerPayload.EMPTY) == true
                }?.let {
                    INSTANCE?.sumLevelsOf(it.armor.stack)?.roundToInt()?.coerceAtMost(5)
                } ?: 0
        }

        fun crossbowFastpullReducedTicks(player: Player): Int {
            return (levels(player) * 2.5).roundToInt()
        }

        fun bowFastpullLevels(player: Player) : Float {
            return levels(player) / 2.5F
        }

    }

    override fun registerEvents() {
        fun trigger(player: LivingEntity, stack: ItemStack) { // FIXME not working
            if (player is Player && !player.level().isClientSide) {
                ModularEffectContext.from(player)
                    .run(EffectStackOption.PRIORITIZE_GREATER.apply)
                    .any {
                        attemptToApply(it, ModularEffectTriggerPayload.EMPTY) == InteractionResult.SUCCESS
                    }
            }
        }

        VanillaEvents.BowShotEvent.register(::trigger)
        VanillaEvents.CrossbowReloadedEvent.register(::trigger)
    }

    override fun acceptTier(tier: DataModelTier) = true
    override fun minimumTier(): DataModelTier = DataModelTier.FAULTY
    override fun isScaled(): Boolean = true
    override fun getEnergyConsumptionType(): EffectInfo.EnergyConsumptionType = EffectInfo.EnergyConsumptionType.USE

}
