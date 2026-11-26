package dev.nathanpb.dml.itemgroup

import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.block.BLOCK_CAFETERIA
import dev.nathanpb.dml.block.BLOCK_DATA_SYNTHESIZER
import dev.nathanpb.dml.block.BLOCK_DISRUPTIONS_CORE
import dev.nathanpb.dml.block.BLOCK_GLITCHED_SLAB
import dev.nathanpb.dml.block.BLOCK_GLITCHED_STAIRS
import dev.nathanpb.dml.block.BLOCK_GLITCHED_TILE
import dev.nathanpb.dml.block.BLOCK_GLITCHED_WALL
import dev.nathanpb.dml.block.BLOCK_LOOT_FABRICATOR
import dev.nathanpb.dml.block.BLOCK_TRIAL_KEYSTONE
import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.item.ITEM_DML
import dev.nathanpb.dml.item.ITEM_ENERGY_OCTAHEDRON
import dev.nathanpb.dml.item.ITEM_GLITCH_INGOT
import dev.nathanpb.dml.item.ITEM_GLITCH_SWORD
import dev.nathanpb.dml.item.ITEM_GLITCH_UPGRADE_SMITHING_TEMPLATE
import dev.nathanpb.dml.item.ITEM_PRISTINE_MATTER_OVERWORLD
import dev.nathanpb.dml.item.ITEM_TRIAL_KEY
import dev.nathanpb.dml.utils.getEmptyAndFullCapacityEnergyItem
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike


val ITEM_GROUP_KEY: ResourceKey<CreativeModeTab> = ResourceKey.create(Registries.CREATIVE_MODE_TAB, identifier("tab_${MOD_ID}"))
/** The items of the tab, in order (stacks are made when the tab is filled: item components are bound after init). */
val ITEMS = ArrayList<ItemLike>()

fun registerItemGroup() {
    ITEMS.add(0, BLOCK_CAFETERIA)

    Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ITEM_GROUP_KEY, FabricCreativeModeTab.builder()
        .title(Component.translatable("itemGroup.$MOD_ID.tab_$MOD_ID"))
        .icon { ItemStack(ITEM_DML) }
        .displayItems { _: CreativeModeTab.ItemDisplayParameters, entries: CreativeModeTab.Output ->
            ITEMS.forEach(entries::accept)
        }
    .build())

    CreativeModeTabEvents.modifyOutputEvent(ITEM_GROUP_KEY).register {
        val glitchedBlocks = listOf(
            BLOCK_DISRUPTIONS_CORE,
            BLOCK_GLITCHED_WALL,
            BLOCK_GLITCHED_SLAB,
            BLOCK_GLITCHED_STAIRS,
            BLOCK_GLITCHED_TILE,
        )

        glitchedBlocks.forEach { block ->
            it.insertAfter(BLOCK_CAFETERIA, block)
        }

        it.insertBefore(ITEM_TRIAL_KEY, BLOCK_DATA_SYNTHESIZER)
        getEmptyAndFullCapacityEnergyItem(ITEM_ENERGY_OCTAHEDRON).forEach { stack ->
            it.insertAfter(BLOCK_DATA_SYNTHESIZER, stack)
        }

        it.insertAfter(ITEM_TRIAL_KEY, BLOCK_TRIAL_KEYSTONE)
        it.insertBefore(ITEM_PRISTINE_MATTER_OVERWORLD, BLOCK_LOOT_FABRICATOR)


        it.insertAfter(ITEM_GLITCH_INGOT, ITEM_GLITCH_UPGRADE_SMITHING_TEMPLATE)

        getEmptyAndFullCapacityEnergyItem(ITEM_GLITCH_SWORD).forEach { stack ->
            it.insertAfter(ITEM_GLITCH_UPGRADE_SMITHING_TEMPLATE, stack)
        }
    }

}
