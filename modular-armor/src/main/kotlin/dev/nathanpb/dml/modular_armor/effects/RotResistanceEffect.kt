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

import com.mojang.datafixers.util.Pair
import dev.nathanpb.dml.enums.DataModelTier
import dev.nathanpb.dml.enums.EntityCategory
import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.modular_armor.core.*
import dev.nathanpb.dml.modular_armor.data.ModularArmorData
import dev.nathanpb.dml.modular_armor.modularArmorConfig
import dev.nathanpb.dml.utils.firstInstanceOrNull
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.InteractionResult
import kotlin.random.Random

class RotResistanceEffect : ModularEffect<ModularEffectTriggerPayload>(
    identifier("rot_resistance"),
    EntityCategory.ZOMBIE,
    modularArmorConfig.glitchArmor.costs::rotResistance
) {

    companion object {
        private val INSTANCE = ModularEffectRegistry.INSTANCE.all.firstInstanceOrNull<RotResistanceEffect>()

        fun attemptToCancelHunger(player: LivingEntity, stack: ItemStack, effects: List<MobEffectInstance>): List<MobEffectInstance> {
            INSTANCE?.run {
                if (player is Player && !player.level().isClientSide && stack.item == Items.ROTTEN_FLESH) {
                    ModularEffectContext.from(player)
                        .run(EffectStackOption.PRIORITIZE_GREATER.apply)
                        .firstOrNull { context ->
                            attemptToApply(context, ModularEffectTriggerPayload.EMPTY) == InteractionResult.SUCCESS
                        }?.let { context ->
                            if (context.tier.ordinal > 0 || Random.nextFloat() <= .5) {
                                return effects.filter { effect ->
                                    effect.effect != MobEffects.HUNGER
                                }
                            }
                        }
                }
            }


            return effects
        }
    }

    override fun registerEvents() {
    }

    override fun acceptTier(tier: DataModelTier) = true
    override fun minimumTier(): DataModelTier = DataModelTier.FAULTY
    override fun isScaled() = true
    override fun getEnergyConsumptionType() = EffectInfo.EnergyConsumptionType.USE

    override fun createEntityAttributeModifier(armor: ModularArmorData): AttributeModifier {
        val value = if (armor.tier() == DataModelTier.FAULTY) 0.5 else 1.0
        return AttributeModifier(modifierId(armor), value, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
    }

}
