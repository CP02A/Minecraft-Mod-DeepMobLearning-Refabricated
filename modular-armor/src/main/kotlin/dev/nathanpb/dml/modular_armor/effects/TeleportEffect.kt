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
import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.modular_armor.TELEPORT_KEYBINDING
import dev.nathanpb.dml.modular_armor.core.ModularEffect
import dev.nathanpb.dml.modular_armor.core.ModularEffectContext
import dev.nathanpb.dml.modular_armor.data.ModularArmorData
import dev.nathanpb.dml.modular_armor.event.ModularArmorEvents
import dev.nathanpb.dml.modular_armor.modularArmorConfig
import dev.nathanpb.dml.modular_armor.net.TeleportEffectRequestedPayload
import dev.nathanpb.dml.modular_armor.payload.TeleportEffectPayload
import dev.nathanpb.dml.utils.toVec3d
import dev.nathanpb.dml.utils.writeVec3d
import io.netty.buffer.Unpooled
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.InteractionResult
import net.minecraft.world.phys.HitResult
import net.minecraft.core.Direction
import net.minecraft.world.level.ClipContext

class TeleportEffect : ModularEffect<TeleportEffectPayload>(
    identifier("teleport"),
    EntityCategory.END,
    modularArmorConfig.glitchArmor.costs::teleports
) {

    @Environment(EnvType.CLIENT)
    private var clientLastUse = 0L

    override fun registerEvents() {
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
                if (TELEPORT_KEYBINDING.isDown) {
                    client.player?.let { player ->
                        val world = player.level()
                        if (world.gameTime - clientLastUse >= 20) {
                            clientLastUse = world.gameTime

                            ClientPlayNetworking.send(TeleportEffectRequestedPayload(
                                player.position().add(0.0, player.getEyeHeight(player.pose).toDouble(), 0.0),
                                player.forward
                            ))
                        }
                    }
                }
            })
        }

        ModularArmorEvents.TeleportEffectRequestedEvent.register { player, pos, looking ->
            if (!player.level().isClientSide) {
                return@register ModularEffectContext.from(player)
                    .any {
                        attemptToApply(it, TeleportEffectPayload(pos, looking)) { _, _ ->
                            val lookingAt = looking.scale(10.0).add(pos)
                            val hitResult = player.level().clip(ClipContext(
                                pos,
                                lookingAt,
                                ClipContext.Block.OUTLINE,
                                ClipContext.Fluid.NONE,
                                player
                            ))

                            val positionToTeleport = if (hitResult.type == HitResult.Type.MISS) {
                                lookingAt.add(0.0, -1.0 ,0.0)
                            } else {
                                hitResult.blockPos.relative(hitResult.direction).toVec3d().run {
                                    if (hitResult.direction != Direction.UP) {
                                        add(0.0, -1.0, 0.0)
                                    } else this
                                }.add(0.5, 0.0, 0.5)
                            }

                            positionToTeleport.apply {
                                player.teleportTo(x, y, z)
                            }

                        }.result == InteractionResult.SUCCESS
                    }
            }
            return@register false
        }
    }

    override fun acceptTier(tier: DataModelTier) = tier.ordinal >= 3
    override fun minimumTier(): DataModelTier = DataModelTier.SUPERIOR
    override fun getEnergyConsumptionType() = EffectInfo.EnergyConsumptionType.USE

    override fun createEntityAttributeModifier(armor: ModularArmorData): AttributeModifier {
        return AttributeModifier(modifierId(armor), 1.0, AttributeModifier.Operation.ADD_VALUE)
    }

}
