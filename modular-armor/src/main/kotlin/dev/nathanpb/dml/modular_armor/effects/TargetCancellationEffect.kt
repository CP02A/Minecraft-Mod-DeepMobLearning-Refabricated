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

import net.minecraft.world.entity.EntityTypes
import dev.nathanpb.dml.enums.EntityCategory
import dev.nathanpb.dml.modular_armor.core.*
import dev.nathanpb.dml.modular_armor.data.ModularArmorData
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.InteractionResult
import net.minecraft.resources.Identifier
import kotlin.math.sqrt

abstract class TargetCancellationEffect(
    id: Identifier,
    category: EntityCategory,
    applyCost: () -> Long
) : ModularEffect<WrappedEffectTriggerPayload<LivingEntity>>(id, category, applyCost) {

    companion object {
        private val INSTANCES by lazy {
            ModularEffectRegistry.INSTANCE.all.filterIsInstance<TargetCancellationEffect>()
        }

        fun attemptToCancel(mob: Mob, target: LivingEntity): InteractionResult {
            INSTANCES.forEach { instance ->
                if (target is Player && !target.level().isClientSide) {
                    ModularEffectContext.from(target)
                        .run(EffectStackOption.RANDOMIZE.apply)
                        .any {
                            instance.attemptToApply(it, ModularEffectTriggerPayload.wrap(mob)) == InteractionResult.SUCCESS
                        }.let {
                            if (it) {
                                return InteractionResult.FAIL
                            }
                        }
                }
            }
            return InteractionResult.PASS
        }
    }

    override fun registerEvents() {
    }

    override fun createEntityAttributeModifier(armor: ModularArmorData): AttributeModifier {
        return AttributeModifier(modifierId(armor), 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
    }

    // Make sure it will only cancel the target when absolutely needed
    override fun canApply(context: ModularEffectContext, payload: WrappedEffectTriggerPayload<LivingEntity>): Boolean {
        val entity = payload.value
        return entity.isAlive
            && entity.canAttack(context.player)
            && super.canApply(context, payload)
            && sqrt(entity.distanceTo(context.player)) <= entity.getAttributeValue(Attributes.FOLLOW_RANGE)
    }

    // Data draining goes brrrrrr
    override fun shouldConsumeEnergy(context: ModularEffectContext): Boolean {
        return super.shouldConsumeEnergy(context) && context.player.level().gameTime % 20 == 0L
    }

}
