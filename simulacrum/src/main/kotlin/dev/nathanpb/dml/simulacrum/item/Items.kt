package dev.nathanpb.dml.simulacrum.item

import dev.nathanpb.dml.itemgroup.ITEM_GROUP_KEY
import dev.nathanpb.dml.simulacrum.SIMULATION_CHAMBER
import dev.nathanpb.dml.simulacrum.simulacrumConfig
import dev.nathanpb.dml.utils.registerItem
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents

// Created and registered together (item properties need the id) when registerItems() loads this file's class
val EXTRATERRESTRIAL_MATTER = registerItem("extraterrestrial_matter") { ItemMatter(it, simulacrumConfig.matterXP.extraterrestrialMatterXP) }
val HELLISH_MATTER = registerItem("hellish_matter") { ItemMatter(it, simulacrumConfig.matterXP.hellishMatterXP) }
val OVERWORLD_MATTER = registerItem("overworld_matter") { ItemMatter(it, simulacrumConfig.matterXP.overworldMatterXP) }

fun registerItems() {
    listOf(EXTRATERRESTRIAL_MATTER, HELLISH_MATTER, OVERWORLD_MATTER).forEach { item ->
        CreativeModeTabEvents.modifyOutputEvent(ITEM_GROUP_KEY).register {
            it.insertAfter(SIMULATION_CHAMBER, item)
        }
    }
}
