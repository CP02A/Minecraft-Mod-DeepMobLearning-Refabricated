package dev.nathanpb.dml.simulacrum

import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.data.dataModel
import dev.nathanpb.dml.item.ItemDataModel
import dev.nathanpb.dml.simulacrum.screen.ScreenSimulationChamber
import dev.nathanpb.dml.simulacrum.util.DataModelUtil
import dev.nathanpb.dml.utils.getInfoText
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component

@Suppress("unused")
fun initClient() {
    MenuScreens.register(SCS_HANDLER_TYPE) { handler, inventory, title ->
        ScreenSimulationChamber(handler, inventory, title)
    }

    ItemTooltipCallback.EVENT.register(ItemTooltipCallback { stack: ItemStack, _, _, tooltip: MutableList<Component> ->
        if (stack.item is ItemDataModel && stack.dataModel.category != null) {
            tooltip.add(getInfoText(
                Component.translatable(
                    "tooltip.$MOD_ID.data_model.1"
                ),
                Component.translatable(
                    "tooltip.$MOD_ID.data_model.2",
                    DataModelUtil.getEnergyCost(stack)
                )
            ))
            tooltip.add(getInfoText(
                Component.translatable(
                    "tooltip.$MOD_ID.data_model.3"
                ),
                Component.translatable(
                    "tooltip.$MOD_ID.data_model.4",
                    stack.dataModel.category?.matterType?.text ?: Component.empty()
                )
            ))
        }
    })
}