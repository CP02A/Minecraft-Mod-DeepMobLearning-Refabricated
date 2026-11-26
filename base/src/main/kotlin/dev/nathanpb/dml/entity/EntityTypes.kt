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

package dev.nathanpb.dml.entity

import dev.nathanpb.dml.entity.renderer.SystemGlitchRenderer
import dev.nathanpb.dml.identifier
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory

lateinit var SYSTEM_GLITCH_ENTITY_TYPE: EntityType<SystemGlitchEntity>

private fun registerAttributes() {
    FabricDefaultAttributeRegistry.register(SYSTEM_GLITCH_ENTITY_TYPE, SystemGlitchEntity.createMobAttributes())
}

fun registerEntityTypes() {
    val key = ResourceKey.create(Registries.ENTITY_TYPE, identifier("system_glitch"))
    SYSTEM_GLITCH_ENTITY_TYPE = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        key,
        EntityType.Builder.of(::SystemGlitchEntity, MobCategory.MISC)
            .sized(1F, 2F)
            .build(key)
    )

    registerAttributes()
}

fun registerEntityRenderer() {
    EntityRendererRegistry.register(SYSTEM_GLITCH_ENTITY_TYPE) { context ->
        SystemGlitchRenderer(context, ModelLayers.ZOMBIE)
    }
}
