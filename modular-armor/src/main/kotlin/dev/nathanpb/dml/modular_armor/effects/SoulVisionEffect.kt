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

import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.enums.DataModelTier
import dev.nathanpb.dml.enums.EntityCategory
import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.modular_armor.SOUL_VISION_EFFECT
import dev.nathanpb.dml.modular_armor.SOUL_VISION_KEYBINDING
import dev.nathanpb.dml.modular_armor.core.EffectStackOption
import dev.nathanpb.dml.modular_armor.core.ModularEffect
import dev.nathanpb.dml.modular_armor.core.ModularEffectContext
import dev.nathanpb.dml.modular_armor.core.ModularEffectTriggerPayload
import dev.nathanpb.dml.modular_armor.data.ModularArmorData
import dev.nathanpb.dml.modular_armor.event.ModularArmorEvents
import dev.nathanpb.dml.modular_armor.modularArmorConfig
import dev.nathanpb.dml.modular_armor.net.SoulVisionRequestedPayload
import io.netty.buffer.Unpooled
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.player.Player
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult

class SoulVisionEffect : ModularEffect<ModularEffectTriggerPayload>(
    identifier("soul_vision"),
    EntityCategory.GHOST,
    modularArmorConfig.glitchArmor.costs::soulVision
) {

    override val name = Component.translatable("effect.${MOD_ID}.soul_vision")
    override val description = Component.translatable("effect.${MOD_ID}.soul_vision.description")

    override fun registerEvents() {
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
                if (Minecraft.getInstance().level != null && SOUL_VISION_KEYBINDING.isDown) {
                    SOUL_VISION_KEYBINDING.isDown = false
                    ClientPlayNetworking.send(SoulVisionRequestedPayload.INSTANCE)
                }
            })
        }

        ModularArmorEvents.SoulVisionEffectRequestedEvent.register { player: Player ->
            if (!player.level().isClientSide && !player.hasEffect(SOUL_VISION_EFFECT)) {
                ModularEffectContext.from(player)
                    .run(EffectStackOption.PRIORITIZE_GREATER.apply)
                    .any { context ->
                        attemptToApply(context, ModularEffectTriggerPayload.EMPTY) { _, _ ->
                            player.addEffect(MobEffectInstance(SOUL_VISION_EFFECT, 16*20, context.tier.ordinal))
                        }.result == InteractionResult.SUCCESS
                    }
            }
        }
    }

    override fun acceptTier(tier: DataModelTier) = true
    override fun minimumTier() = DataModelTier.FAULTY
    override fun isScaled() = true
    override fun getEnergyConsumptionType() = EffectInfo.EnergyConsumptionType.USE

    override fun createEntityAttributeModifier(armor: ModularArmorData): AttributeModifier {
        return AttributeModifier(modifierId(armor), 1.0, AttributeModifier.Operation.ADD_VALUE)
    }

}
