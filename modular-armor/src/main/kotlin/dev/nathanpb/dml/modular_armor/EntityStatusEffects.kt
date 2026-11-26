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

package dev.nathanpb.dml.modular_armor

import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.core.Holder
import dev.nathanpb.dml.identifier
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry

class UnderwaterHasteEffect : MobEffect(MobEffectCategory.BENEFICIAL, 0x46D99C)
/**
 * Depth strider is an attribute since 1.21: the effect adds 0.25 water movement efficiency per level (before, a mixin
 * into LivingEntity#travel added 0.25 per amplifier level, so level I did nothing).
 */
class DepthStriderEffect : MobEffect(MobEffectCategory.BENEFICIAL, 0x02A7D9) {
    init {
        addAttributeModifier(Attributes.WATER_MOVEMENT_EFFICIENCY, identifier("effect.depth_strider"), 0.25, AttributeModifier.Operation.ADD_VALUE)
    }
}
class SoulVisionEffect : MobEffect(MobEffectCategory.BENEFICIAL, 0xFFD900)

val UNDERWATER_HASTE_EFFECT = register("underwater_haste", UnderwaterHasteEffect())
val DEPTH_STRIDER_EFFECT = register("depth_strider", DepthStriderEffect())
val SOUL_VISION_EFFECT = register("soul_vision", SoulVisionEffect())

private fun register(idPath: String, entry: MobEffect): Holder<MobEffect> {
    return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, identifier(idPath), entry)
}

fun registerStatusEffects() {
    UNDERWATER_HASTE_EFFECT
    DEPTH_STRIDER_EFFECT
    SOUL_VISION_EFFECT
}
