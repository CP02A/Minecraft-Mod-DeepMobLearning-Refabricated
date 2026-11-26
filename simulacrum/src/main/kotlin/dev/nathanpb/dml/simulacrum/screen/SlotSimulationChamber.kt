package dev.nathanpb.dml.simulacrum.screen

import dev.nathanpb.dml.data.dataModel
import dev.nathanpb.dml.item.ITEM_POLYMER_CLAY
import dev.nathanpb.dml.item.ItemDataModel
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.Slot

class SlotSimulationChamber(inventory: Container, index: Int, x: Int, y: Int) : Slot(inventory, index, x, y) {

    override fun mayPlace(stack: ItemStack): Boolean {
        return when(index) {
            0 -> {
                !stack.isEmpty &&
                stack.item is ItemDataModel &&
                stack.dataModel.category != null
            }
            1 -> {
                !stack.isEmpty &&
                stack.`is`(ITEM_POLYMER_CLAY)
            }
            else -> false
        }
    }

}