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

package dev.nathanpb.dml.item

import dev.nathanpb.dml.baseConfig
import dev.nathanpb.dml.utils.RenderUtils
import dev.nathanpb.dml.utils.getEnergyBarStep
import dev.nathanpb.dml.utils.getEnergyTooltipText
import dev.nathanpb.dml.utils.removeComponent
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.Unit
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.ToolMaterial
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import team.reborn.energy.api.base.SimpleEnergyItem
import java.util.function.Consumer

/**
 * A netherite-like sword that runs on energy instead of durability. Without enough energy it hits like an empty hand
 * (before 1.20.5 its attribute modifiers were removed, now [getAttackDamageBonus] cancels the sword's damage).
 */
class ItemGlitchSword(properties: Properties) : Item(
    properties.sword(ToolMaterial.NETHERITE, 3F, -2.4F)
        .fireResistant()
        .rarity(Rarity.EPIC)
        // Energy instead of durability, and no enchanting (the material makes it damageable and enchantable)
        .removeComponent(DataComponents.MAX_DAMAGE)
        .removeComponent(DataComponents.DAMAGE)
        .removeComponent(DataComponents.ENCHANTABLE)
        .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
), SimpleEnergyItem {

    private val usageCost get() = baseConfig.misc.glitchSword.usageCost

    override fun getAttackDamageBonus(victim: Entity, damage: Float, damageSource: DamageSource): Float {
        val stack = damageSource.weaponItem ?: return 0F
        // Down to the damage of an empty hand (1)
        return if(getStoredEnergy(stack) >= usageCost) 0F else 1F - damage
    }

    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity) {
        if(getStoredEnergy(stack) >= usageCost) {
            tryUseEnergy(stack, usageCost)
        }
    }

    override fun getEnergyCapacity(stack: ItemStack) = baseConfig.misc.glitchSword.energyCapacity
    override fun getEnergyMaxInput(stack: ItemStack) = baseConfig.misc.glitchSword.energyInput
    override fun getEnergyMaxOutput(stack: ItemStack) = 0L

    override fun getBarWidth(stack: ItemStack): Int = getEnergyBarStep(stack)
    override fun getBarColor(stack: ItemStack) = RenderUtils.ENERGY_COLOR
    override fun isBarVisible(stack: ItemStack) = true

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        display: TooltipDisplay,
        tooltip: Consumer<Component>,
        flag: TooltipFlag
    ) {
        tooltip.accept(getEnergyTooltipText(stack))
    }

    companion object {

        fun getIncreasedDamage(source: DamageSource, amount: Float): Float {
            var finalAmount = amount
            val attacker = source.entity
            if(source.`is`(DamageTypes.PLAYER_ATTACK) && attacker is Player) {
                if(attacker.mainHandItem.`is`(ITEM_GLITCH_SWORD)) {
                    finalAmount *= 2F
                }
            }
            return finalAmount
        }
    }

}
