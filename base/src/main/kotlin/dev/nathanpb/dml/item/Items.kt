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

package dev.nathanpb.dml.item

import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.enums.EntityCategory
import dev.nathanpb.dml.item.battery.ItemEnergyOctahedron
import dev.nathanpb.dml.itemgroup.ITEMS
import dev.nathanpb.dml.utils.registerItem
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.SmithingTemplateItem

// Items are created and registered together (their properties need the id), in creative tab order, when this file's
// class is loaded by registerItems(). Items with shown = false are left out of the tab or added elsewhere.
private val SHOWN = ArrayList<Item>()

private fun <T : Item> item(id: String, shown: Boolean = true, factory: (Item.Properties) -> T): T =
    registerItem(id, factory).also { if(shown) SHOWN += it }

//val ITEM_GUIDE_TABLET = ItemGuideTablet()
val ITEM_DML = item(MOD_ID, false, ::Item)

val ITEM_SOOT_REDSTONE = item("soot_redstone", factory = ::Item)
val ITEM_SOOT_PLATE = item("soot_plate", factory = ::Item)
val ITEM_SOOT_MACHINE_CASE = item("machine_casing", factory = ::Item)
val ITEM_POLYMER_CLAY = item("polymer_clay", factory = ::Item)

val ITEM_DEEP_LEARNER = item("deep_learner", factory = ::ItemDeepLearner)

val ITEM_DATA_MODEL = item("data_model") { ItemDataModel(it) }
val ITEM_DATA_MODEL_OVERWORLD = item("data_model_overworld") { ItemDataModel(it, EntityCategory.OVERWORLD) }
val ITEM_DATA_MODEL_ZOMBIE = item("data_model_zombie") { ItemDataModel(it, EntityCategory.ZOMBIE) }
val ITEM_DATA_MODEL_SKELETON = item("data_model_skeleton") { ItemDataModel(it, EntityCategory.SKELETON) }
val ITEM_DATA_MODEL_SLIMY = item("data_model_slimy") { ItemDataModel(it, EntityCategory.SLIMY) }
val ITEM_DATA_MODEL_ILLAGER = item("data_model_illager") { ItemDataModel(it, EntityCategory.ILLAGER) }
val ITEM_DATA_MODEL_OCEAN = item("data_model_ocean") { ItemDataModel(it, EntityCategory.OCEAN) }
val ITEM_DATA_MODEL_GHOST = item("data_model_ghost") { ItemDataModel(it, EntityCategory.GHOST) }
val ITEM_DATA_MODEL_NETHER = item("data_model_nether") { ItemDataModel(it, EntityCategory.NETHER) }
val ITEM_DATA_MODEL_END = item("data_model_end") { ItemDataModel(it, EntityCategory.END) }

val ITEM_TRIAL_KEY = item("trial_key", factory = ::ItemTrialKey)

val ITEM_PRISTINE_MATTER_OVERWORLD = item("pristine_matter_overworld") { ItemPristineMatter(it, EntityCategory.OVERWORLD) }
val ITEM_PRISTINE_MATTER_ZOMBIE = item("pristine_matter_zombie") { ItemPristineMatter(it, EntityCategory.ZOMBIE) }
val ITEM_PRISTINE_MATTER_SKELETON = item("pristine_matter_skeleton") { ItemPristineMatter(it, EntityCategory.SKELETON) }
val ITEM_PRISTINE_MATTER_SLIMY = item("pristine_matter_slimy") { ItemPristineMatter(it, EntityCategory.SLIMY) }
val ITEM_PRISTINE_MATTER_ILLAGER = item("pristine_matter_illager") { ItemPristineMatter(it, EntityCategory.ILLAGER) }
val ITEM_PRISTINE_MATTER_OCEAN = item("pristine_matter_ocean") { ItemPristineMatter(it, EntityCategory.OCEAN) }
val ITEM_PRISTINE_MATTER_GHOST = item("pristine_matter_ghost") { ItemPristineMatter(it, EntityCategory.GHOST) }
val ITEM_PRISTINE_MATTER_NETHER = item("pristine_matter_nether") { ItemPristineMatter(it, EntityCategory.NETHER) }
val ITEM_PRISTINE_MATTER_END = item("pristine_matter_end") { ItemPristineMatter(it, EntityCategory.END) }

val ITEM_EMERITUS_HAT = item("emeritus_hat", false, ::ItemEmeritusHat)

val ITEM_PHYSICALLY_CONDENSED_MATRIX_FRAGMENT = item("physically_condensed_matrix_fragment") {
    Item(it.fireResistant().rarity(Rarity.RARE))
}
val ITEM_GLITCH_INGOT = item("glitch_ingot") { Item(it.fireResistant().rarity(Rarity.RARE)) }
val ITEM_GLITCH_UPGRADE_SMITHING_TEMPLATE = item("glitch_upgrade_smithing_template", false) {
    SmithingTemplateItem(
        Component.translatable("item.${MOD_ID}.glitch_upgrade_smithing_template.applies_to").withStyle(ChatFormatting.BLUE),
        Component.translatable("item.${MOD_ID}.glitch_upgrade_smithing_template.ingredients").withStyle(ChatFormatting.BLUE),
        Component.translatable("item.${MOD_ID}.glitch_upgrade_smithing_template.base_slot_description"),
        Component.translatable("item.${MOD_ID}.glitch_upgrade_smithing_template.additions_slot_description"),
        // The vanilla empty slot icons of the armor trim (base) and netherite upgrade (addition) templates
        listOf("helmet", "chestplate", "leggings", "boots").map { slot -> Identifier.withDefaultNamespace("container/slot/$slot") },
        listOf(Identifier.withDefaultNamespace("container/slot/ingot")),
        it
    )
}
val ITEM_GLITCH_SWORD = item("glitch_sword", false, ::ItemGlitchSword)
val ITEM_ENERGY_OCTAHEDRON = item("energy_octahedron", false, ::ItemEnergyOctahedron)

fun registerItems() {
    // Loading the vals above registered the items
    ITEMS.addAll(SHOWN)
}
