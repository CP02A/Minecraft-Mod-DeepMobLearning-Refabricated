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
import dev.nathanpb.dml.modular_armor.data.ModularArmorData
import dev.nathanpb.dml.modular_armor.modularArmorConfig
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.tags.DamageTypeTags
import net.minecraft.world.level.Level
import kotlin.math.min

class ResistanceEffect : ProtectionLikeEffect(
    identifier("resistance"),
    EntityCategory.ILLAGER,
    modularArmorConfig.glitchArmor.costs::resistance
) {

    override fun protectsAgainst(world: Level, source: DamageSource): Boolean {
        return !source.`is`(DamageTypeTags.BYPASSES_ARMOR) && !source.`is`(DamageTypeTags.WITCH_RESISTANT_TO)
    }

    override fun acceptTier(tier: DataModelTier) = true
    override fun minimumTier(): DataModelTier = DataModelTier.FAULTY
    override fun isScaled() = true

    override fun createEntityAttributeModifier(armor: ModularArmorData): AttributeModifier {
        return AttributeModifier(modifierId(armor), armor.tier().ordinal.inc() / 20.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
    }

    override fun inflictDamage(event: VanillaEvents.LivingEntityDamageContext, armorValues: Double): Float {
        return event.damage * (1 - min(0.25, armorValues)).toFloat()
    }

}
