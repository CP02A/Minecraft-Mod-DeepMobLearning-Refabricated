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

package dev.nathanpb.dml.entity.renderer

import dev.nathanpb.dml.entity.SystemGlitchEntity
import dev.nathanpb.dml.entity.model.SystemGlitchModel
import dev.nathanpb.dml.identifier
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.HumanoidMobRenderer
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer
import net.minecraft.client.renderer.entity.state.HumanoidRenderState
import net.minecraft.resources.Identifier

/** A zombie-shaped mob; wearables on its head (the Emeritus Hat) are a bit bigger than on a zombie. */
class SystemGlitchRenderer(context: EntityRendererProvider.Context, layer: ModelLayerLocation) :
    HumanoidMobRenderer<SystemGlitchEntity, HumanoidRenderState, SystemGlitchModel>(
        context,
        SystemGlitchModel(context.bakeLayer(layer)),
        SystemGlitchModel(context.bakeLayer(layer)),
        0.5F,
        CustomHeadLayer.Transforms(0F, 0F, 1.15F, 1.05F)
    )
{
    private val texture = identifier("textures/entity/system_glitch.png")

    override fun createRenderState() = HumanoidRenderState()

    override fun getTextureLocation(state: HumanoidRenderState): Identifier = texture
}
