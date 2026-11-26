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

package dev.nathanpb.dml.modular_armor.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.nathanpb.dml.modular_armor.effects.RotResistanceEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * Rot Resistance: rotten flesh may not give hunger. Food effects are consume effects of the item since 1.21.2 (it was
 * LivingEntity#applyFoodEffects).
 */
@Mixin(ApplyStatusEffectsConsumeEffect.class)
public class ApplyStatusEffectsConsumeEffectMixin {

    @ModifyExpressionValue(
        method = "apply",
        at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/consume_effects/ApplyStatusEffectsConsumeEffect;effects:Ljava/util/List;")
    )
    private List<MobEffectInstance> dml$rotResistance(List<MobEffectInstance> effects, Level level, ItemStack stack, LivingEntity user) {
        return RotResistanceEffect.Companion.attemptToCancelHunger(user, stack, effects);
    }
}
