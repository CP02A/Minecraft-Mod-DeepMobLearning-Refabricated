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

package dev.nathanpb.dml.modular_armor.net

import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.modular_armor.ItemModularGlitchArmor
import dev.nathanpb.dml.modular_armor.core.ModularEffectRegistry
import dev.nathanpb.dml.modular_armor.data.ModularArmorData
import dev.nathanpb.dml.modular_armor.event.ModularArmorEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.Vec3

// Since 1.20.5 packets are typed payloads with a stream codec (they were raw buffers). The ids stayed the same.

/** The client asks to teleport (the Teleport effect): where to and the look direction. */
data class TeleportEffectRequestedPayload(val pos: Vec3, val rotation: Vec3) : CustomPacketPayload {
    override fun type() = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<TeleportEffectRequestedPayload>(identifier("teleport_effect_requested"))
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, TeleportEffectRequestedPayload> = StreamCodec.composite(
            Vec3.STREAM_CODEC, TeleportEffectRequestedPayload::pos,
            Vec3.STREAM_CODEC, TeleportEffectRequestedPayload::rotation,
            ::TeleportEffectRequestedPayload
        )
    }
}

/** The client asks for soul vision (the Soul Vision effect's key). */
class SoulVisionRequestedPayload : CustomPacketPayload {
    override fun type() = TYPE

    companion object {
        val INSTANCE = SoulVisionRequestedPayload()
        val TYPE = CustomPacketPayload.Type<SoulVisionRequestedPayload>(identifier("soul_vision_requested"))
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, SoulVisionRequestedPayload> = StreamCodec.unit(INSTANCE)
    }
}

/** The client turns a modular effect of the armor in [hand] on or off. */
data class ModularEffectTogglePayload(val id: Identifier, val enabled: Boolean, val hand: InteractionHand) : CustomPacketPayload {
    override fun type() = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<ModularEffectTogglePayload>(identifier("modular_effect_toggle"))
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, ModularEffectTogglePayload> = StreamCodec.composite(
            Identifier.STREAM_CODEC, ModularEffectTogglePayload::id,
            ByteBufCodecs.BOOL, ModularEffectTogglePayload::enabled,
            InteractionHand.STREAM_CODEC, ModularEffectTogglePayload::hand,
            ::ModularEffectTogglePayload
        )
    }
}

/** The server tells the client the cooldown of the Undying effect, for the HUD. */
data class UndyingCooldownUpdatePayload(val cooldownTime: Int, val maxCooldownTime: Int) : CustomPacketPayload {
    override fun type() = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<UndyingCooldownUpdatePayload>(identifier("undying_cooldown_update"))
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, UndyingCooldownUpdatePayload> = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, UndyingCooldownUpdatePayload::cooldownTime,
            ByteBufCodecs.VAR_INT, UndyingCooldownUpdatePayload::maxCooldownTime,
            ::UndyingCooldownUpdatePayload
        )
    }
}

/** Payload types exist on both sides, so this runs in the common entrypoint. */
fun registerPayloadTypes() {
    PayloadTypeRegistry.serverboundPlay().register(TeleportEffectRequestedPayload.TYPE, TeleportEffectRequestedPayload.CODEC)
    PayloadTypeRegistry.serverboundPlay().register(SoulVisionRequestedPayload.TYPE, SoulVisionRequestedPayload.CODEC)
    PayloadTypeRegistry.serverboundPlay().register(ModularEffectTogglePayload.TYPE, ModularEffectTogglePayload.CODEC)
    PayloadTypeRegistry.clientboundPlay().register(UndyingCooldownUpdatePayload.TYPE, UndyingCooldownUpdatePayload.CODEC)
}

/** Handlers run on the server thread. */
fun registerServerSidePackets() {
    ServerPlayNetworking.registerGlobalReceiver(TeleportEffectRequestedPayload.TYPE) { payload, context ->
        val player = context.player()
        val rotation = payload.rotation
        if (
            payload.pos.distanceToSqr(player.position()) <= 4*4
            && arrayOf(rotation.x, rotation.y, rotation.z).all { it in -128.0..128.0 }
        ) {
            ModularArmorEvents.TeleportEffectRequestedEvent.invoker().invoke(player, payload.pos, rotation)
        }
    }

    ServerPlayNetworking.registerGlobalReceiver(SoulVisionRequestedPayload.TYPE) { _, context ->
        ModularArmorEvents.SoulVisionEffectRequestedEvent.invoker().invoke(context.player())
    }

    ServerPlayNetworking.registerGlobalReceiver(ModularEffectTogglePayload.TYPE) { payload, context ->
        val stack = context.player().getItemInHand(payload.hand)
        if (stack.item is ItemModularGlitchArmor) {
            if (ModularEffectRegistry.INSTANCE.fromId(payload.id) != null) {
                val data = ModularArmorData(stack)
                data.disabledEffects = if (payload.enabled) {
                    data.disabledEffects - payload.id
                } else {
                    data.disabledEffects + payload.id
                }
            }
        }
    }
}
