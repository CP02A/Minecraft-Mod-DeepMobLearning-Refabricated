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

package dev.nathanpb.dml.utils

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import org.joml.Quaternionf
import org.joml.Vector3f
import com.mojang.blaze3d.systems.RenderSystem
import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.utils.RenderUtils.Companion.ALT_STYLE
import dev.nathanpb.dml.utils.RenderUtils.Companion.STYLE
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.NinePatchBackgroundPainter
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.entity.LivingEntity
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import com.mojang.math.Axis
import net.minecraft.world.phys.Vec3
import java.text.NumberFormat
import java.util.*

// FIXME rename to something less client-sidey??
class RenderUtils {

    companion object {
        /* Textures */
        val DEFAULT_BACKGROUND_PAINTER: NinePatchBackgroundPainter by lazy {
            BackgroundPainter.createNinePatch(identifier("textures/gui/dml_background_painter.png"))
        }
        val INNER_BACKGROUND_PAINTER: NinePatchBackgroundPainter by lazy {
            BackgroundPainter.createNinePatch(identifier("textures/gui/inner_background_painter.png"))
        }
        val DML_WIDGETS: Identifier = identifier("textures/gui/dml_widgets.png")

        val ARROW: Identifier = identifier("textures/gui/arrow.png")
        val ARROW_BACKGROUND: Identifier = identifier("textures/gui/arrow_background.png")



        /* Energy Bars */
        val ENERGY_BAR_ARROW: Identifier = identifier("textures/gui/energy_bar/energy_bar_arrow.png")

        val ENERGY_BAR_BACKGROUND: Identifier = identifier("textures/gui/energy_bar/energy_bar_background.png")
        val ENERGY_BAR_BACKGROUND_BIG: Identifier = identifier("textures/gui/energy_bar/energy_bar_background_big.png")

        val ENERGY_BAR: Identifier = identifier("textures/gui/energy_bar/energy_bar.png")
        val ENERGY_BAR_BIG: Identifier = identifier("textures/gui/energy_bar/energy_bar_big.png")

        val PRISTINE_ENERGY_BAR: Identifier = identifier("textures/gui/energy_bar/pristine_energy_bar.png")
        val PRISTINE_ENERGY_BAR_BIG: Identifier = identifier("textures/gui/energy_bar/pristine_energy_bar_big.png")



        /* Component Colors & Styles */
        const val TITLE_COLOR: Int = 0x04FCC4
        const val ALT_TITLE_COLOR: Int = 0x62D8FF
        val STYLE: Style = Style.EMPTY.withColor(TITLE_COLOR)
        val ALT_STYLE: Style = Style.EMPTY.withColor(ALT_TITLE_COLOR)
        val ENERGY_COLOR: Int = 0xFCD904
        val ENERGY_STYLE: Style = Style.EMPTY.withColor(ENERGY_COLOR)

        /* Particles */
        val GLITCH_PARTICLE: DustParticleOptions = DustParticleOptions(TITLE_COLOR, 1F)
        val ALT_GLITCH_PARTICLE: DustParticleOptions = DustParticleOptions(ALT_TITLE_COLOR, 1F)

    }
}

fun getBooleanInfoText(primaryText: MutableComponent, boolean: Boolean, primaryStyle: Style, secondaryStyle: Style): MutableComponent {
    val onOffText = CommonComponents.optionStatus(boolean).copy()
    return getInfoText(primaryText, onOffText, primaryStyle, secondaryStyle)
}

fun getInfoText(primaryText: MutableComponent, secondaryText: MutableComponent, primaryStyle: Style = STYLE, secondaryStyle: Style = ALT_STYLE): MutableComponent {
    primaryText.style = primaryStyle
    secondaryText.style = secondaryStyle

    return primaryText.append(secondaryText)
}

// primaryText | secondaryText
fun getPipeText(primaryText: MutableComponent, secondaryText: MutableComponent, parenthesisStyle: Style = ALT_STYLE): MutableComponent {
    val pipeText = Component.translatable(
        "tooltip.dml-refabricated.pipe",
        primaryText,
        secondaryText
    )
    pipeText.style = parenthesisStyle

    return pipeText
}

// primaryText (value)
fun getParenthesisText(primaryText: MutableComponent, value: Any, parenthesisStyle: Style = ALT_STYLE, addSpace: Boolean = true): MutableComponent {
    val parenthesisText = if(addSpace) Component.literal(" ") else Component.empty()
    parenthesisText.append(Component.translatable(
        "tooltip.dml-refabricated.parenthesis",
        value
    ))
    parenthesisText.style = parenthesisStyle

    return primaryText.append(parenthesisText)
}

/*
 * Used to apply commas and periods to numbers according to the client's language
 *
 * en_us: 10000 -> 10,000
 * pt_br: 10000 -> 10.000
 */
@Environment(EnvType.CLIENT)
fun formatAccordingToLanguage(): NumberFormat {
    val locale: Locale = Locale.forLanguageTag(
        Minecraft.getInstance().languageManager.selected.replace(
            "_",
            "-"
        )
    )

    return NumberFormat.getNumberInstance(locale)
}

// not stolen from mojang, I promise
/**
 * Draws [entity] spinning around the vertical axis inside the box (x0, y0)-(x1, y1) of a GUI.
 * Before 1.21.6 this rendered the entity directly; now the GUI renders a render state of it.
 */
@Environment(EnvType.CLIENT)
fun drawEntity(
    ctx: GuiGraphicsExtractor,
    entity: LivingEntity,
    x0: Int, y0: Int, x1: Int, y1: Int,
    size: Int,
    rotationY: Float,
) {
    val rotation = Quaternionf().rotateZ(Math.PI.toFloat())
    val renderer = Minecraft.getInstance().entityRenderDispatcher.getRenderer(entity)
    val renderState = renderer.createRenderState(entity, 1.0F)
    renderState.shadowPieces.clear()
    renderState.outlineColor = 0
    if (renderState is LivingEntityRenderState) {
        renderState.bodyRot = rotationY
        renderState.yRot = 0F
        renderState.xRot = 0F
        renderState.boundingBoxWidth = renderState.boundingBoxWidth / renderState.scale
        renderState.boundingBoxHeight = renderState.boundingBoxHeight / renderState.scale
        renderState.scale = 1.0F
    }
    val translation = Vector3f(0.0F, renderState.boundingBoxHeight / 2.0F, 0.0F)
    ctx.entity(renderState, size.toFloat(), translation, rotation, null, x0, y0, x1, y1)
}
