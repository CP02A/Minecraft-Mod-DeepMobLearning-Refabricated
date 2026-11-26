/*
 * Copyright (C) 2020 Nathan P. Bombana, IterationFunk
 *
 * This file is part of Deep Mob Learning: Refabricated.
 *
 * Deep Mob Learning: Refabricated is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Deep Mob Learning: Refabricated is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Deep Mob Learning: Refabricated.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.nathanpb.dml.block

import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.utils.RenderUtils
import dev.nathanpb.dml.utils.registerBlock
import dev.nathanpb.dml.utils.registerBlockItem
import dev.nathanpb.dml.utils.registerItem
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.WallBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import java.util.function.Consumer

// Blocks are created and registered together (their properties need the id) when this file's class is loaded by
// registerBlocks(); their items are registered there afterwards.
val BLOCK_TRIAL_KEYSTONE = registerBlock("trial_keystone", factory = ::BlockTrialKeystone)
val BLOCK_DATA_SYNTHESIZER = registerBlock("data_synthesizer", factory = ::BlockDataSynthesizer)
val BLOCK_LOOT_FABRICATOR = registerBlock("loot_fabricator", factory = ::BlockLootFabricator)
/*val SYSTEM_GLITCH_HEAD = SkullBlock(SkullBlock.Type.CREEPER, FabricBlockSettings.create().instrument(Instrument.CREEPER).strength(1.0f).pushReaction(PistonBehavior.DESTROY))
val SYSTEM_GLITCH_WALL_HEAD = WallSkullBlock(SkullBlock.Type.CREEPER, FabricBlockSettings.create().strength(1.0f).dropsLike(SYSTEM_GLITCH_HEAD).pushReaction(PistonBehavior.DESTROY))*/
val BLOCK_DISRUPTIONS_CORE = registerBlock("disruptions_core", copyOf(Blocks.SHULKER_BOX), ::BlockDisruptionsCore)
val BLOCK_FADING_GLITCHED_TILE = registerBlock("fading_glitched_tile", copyOf(Blocks.STONE), ::BlockFadingGlitchedTile)
val BLOCK_GLITCHED_TILE = registerBlock("glitched_tile", copyOf(Blocks.STONE)) { Block(it.mapColor(MapColor.COLOR_CYAN)) }
val BLOCK_GLITCHED_STAIRS = registerBlock("glitched_stairs", copyOf(BLOCK_GLITCHED_TILE)) {
    StairBlock(BLOCK_GLITCHED_TILE.defaultBlockState(), it)
}
val BLOCK_GLITCHED_SLAB = registerBlock("glitched_slab", copyOf(BLOCK_GLITCHED_TILE), ::SlabBlock)
val BLOCK_GLITCHED_WALL = registerBlock("glitched_wall", copyOf(BLOCK_GLITCHED_TILE).forceSolidOn(), ::WallBlock)
val BLOCK_CAFETERIA = registerBlock("cafeteria", factory = ::BlockCafeteria)

private fun copyOf(block: Block) = BlockBehaviour.Properties.ofFullCopy(block)

fun registerBlocks() {
    linkedMapOf(
        BLOCK_TRIAL_KEYSTONE to Rarity.UNCOMMON,
        BLOCK_DATA_SYNTHESIZER to Rarity.UNCOMMON,
        BLOCK_LOOT_FABRICATOR to Rarity.UNCOMMON,
        BLOCK_DISRUPTIONS_CORE to Rarity.UNCOMMON,
        BLOCK_FADING_GLITCHED_TILE to Rarity.COMMON,
        BLOCK_GLITCHED_TILE to Rarity.COMMON,
        BLOCK_GLITCHED_STAIRS to Rarity.COMMON,
        BLOCK_GLITCHED_SLAB to Rarity.COMMON,
        BLOCK_GLITCHED_WALL to Rarity.COMMON,
    ).forEach { (block, rarity) ->
        registerBlockItem(block) { it.rarity(rarity) }
    }

    // Before 1.21.5 blocks had their own tooltips, now only items have
    registerItem(BuiltInRegistries.BLOCK.getKey(BLOCK_CAFETERIA)) { properties ->
        object : BlockItem(BLOCK_CAFETERIA, properties.useBlockDescriptionPrefix().rarity(Rarity.EPIC)) {
            override fun appendHoverText(
                stack: ItemStack,
                context: TooltipContext,
                display: TooltipDisplay,
                tooltip: Consumer<Component>,
                flag: TooltipFlag
            ) {
                tooltip.accept(
                    Component.translatable("tooltip.${MOD_ID}.cafeteria.joinus").setStyle(RenderUtils.STYLE).append(
                        Component.literal("https://discord.gg/G4PjhEf").setStyle(RenderUtils.ALT_STYLE)
                    )
                )
            }
        }
    }
}
