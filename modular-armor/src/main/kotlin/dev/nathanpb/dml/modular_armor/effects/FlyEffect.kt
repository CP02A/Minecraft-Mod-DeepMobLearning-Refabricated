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
import dev.nathanpb.dml.modular_armor.core.EffectStackOption
import dev.nathanpb.dml.modular_armor.core.ModularEffectContext
import dev.nathanpb.dml.modular_armor.core.ModularEffectTriggerPayload
import dev.nathanpb.dml.modular_armor.data.ModularArmorData
import dev.nathanpb.dml.modular_armor.modularArmorConfig
import io.github.ladysnake.pal.VanillaAbilities
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.InteractionResult

class FlyEffect : AbilityBasedEffect(
    identifier("fly"),
    EntityCategory.GHOST,
    modularArmorConfig.glitchArmor.costs::fly,
    VanillaAbilities.ALLOW_FLYING
) {
    override fun registerEvents() {
        super.registerEvents()
        VanillaEvents.PlayerEntityTickEvent.register { player ->
            // Check if the data should be consumed
            if (
                !player.level().isClientSide
                && player.level().gameTime % 20 == 0L
                && abilitySource.grants(player, ability)
                && player.level().getBlockState(player.blockPosition().below()).isAir
            ) {
                ModularEffectContext.from(player)
                    .run(EffectStackOption.PRIORITIZE_GREATER.apply)
                    .firstOrNull { context ->
                        attemptToApply(context, ModularEffectTriggerPayload.EMPTY) == InteractionResult.SUCCESS
                    }
            }
        }
    }

    override fun createEntityAttributeModifier(armor: ModularArmorData): AttributeModifier {
        return AttributeModifier(modifierId(armor), 1.0, AttributeModifier.Operation.ADD_VALUE)
    }

    override fun acceptTier(tier: DataModelTier) = tier.isMaxTier()
    override fun minimumTier(): DataModelTier = DataModelTier.SELF_AWARE

}
