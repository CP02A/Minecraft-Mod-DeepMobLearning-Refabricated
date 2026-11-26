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

package dev.nathanpb.dml.modular_armor.hud

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.renderer.RenderPipelines
import dev.nathanpb.dml.identifier
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import com.mojang.blaze3d.vertex.PoseStack

class UndyingCooldownHud {

    companion object {
        val INSTANCE = UndyingCooldownHud()
        private val TEXTURE = identifier("textures/gui/undying_cooldown.png")

        /** Drawn after the vanilla HUD (it was a mixin into Gui#renderStatusBars). */
        fun register() {
            HudElementRegistry.addLast(identifier("undying_cooldown")) { ctx, _ -> INSTANCE.render(ctx) }
        }
    }

    private val client = Minecraft.getInstance()

    var cooldownTime = 0
    var maxCooldownTime = 0

    fun render(ctx: GuiGraphicsExtractor) {
        if (cooldownTime == 0 || maxCooldownTime == 0) {
            return
        }

        // TODO render a progress bar or something
        // val percent = cooldownTime / maxCooldownTime
        val width = client.window.guiScaledWidth
        val height = client.window.guiScaledHeight

        ctx.blit(
            RenderPipelines.GUI_TEXTURED,
            TEXTURE,
            width - 64,
            height - 18,
            0F, 0F,
            16, 16,
            16, 16
        )

    }
}
