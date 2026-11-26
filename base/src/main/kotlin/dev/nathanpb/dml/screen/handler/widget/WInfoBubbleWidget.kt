package dev.nathanpb.dml.screen.handler.widget

import dev.nathanpb.dml.identifier
import net.minecraft.client.Minecraft
import io.github.cottonmc.cotton.gui.widget.WSprite
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

class WInfoBubbleWidget(
    texture: Identifier,
    val text: List<Component>,
    var hidden: Boolean = false
): WSprite(texture) {

    companion object {
        val INFO_BUBBLE = identifier("textures/gui/info_bubble.png")
        val WARNING_BUBBLE = identifier("textures/gui/warning_bubble.png")
    }

    override fun paint(context: GuiGraphicsExtractor, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        if(hidden) return
        super.paint(context, x, y, mouseX, mouseY)

        if(!isHovered) return
        context.setComponentTooltipForNextFrame(
            Minecraft.getInstance().font,
            text,
            x + mouseX, y + mouseY
        )
    }

}