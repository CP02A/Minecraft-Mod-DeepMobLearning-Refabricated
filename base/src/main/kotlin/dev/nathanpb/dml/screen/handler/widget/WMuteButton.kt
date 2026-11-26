package dev.nathanpb.dml.screen.handler.widget

import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.utils.RenderUtils.Companion.DML_WIDGETS
import io.github.cottonmc.cotton.gui.widget.data.InputResult
import net.minecraft.client.input.MouseButtonEvent

abstract class WMuteButton(
    defaultIndex: Int
) : WStylizedButton(
    DML_WIDGETS,
    listOf(
        identifier("textures/gui/sound_on_icon.png"),
        identifier("textures/gui/sound_off_icon.png")
    )
) {

    init {
        overlayTextureIndex = defaultIndex.coerceIn(0, 1)
    }

    abstract fun toggleMute()

    override fun onClick(click: MouseButtonEvent, doubled: Boolean): InputResult {
        toggleMute()
        overlayTextureIndex = if(overlayTextureIndex == 0) 1 else 0
        return super.onClick(click, doubled)
    }

}