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

package dev.nathanpb.dml.test

import dev.nathanpb.dml.baseConfig
import dev.nathanpb.dml.blockEntity.BlockEntityDataSynthesizer
import dev.nathanpb.dml.blockEntity.BlockEntityLootFabricator
import dev.nathanpb.dml.inventory.LootFabricatorInventory
import dev.nathanpb.dml.recipe.RECIPE_CRUSHING
import dev.nathanpb.dml.recipe.RECIPE_LOOT_FABRICATOR
import dev.nathanpb.dml.recipe.RECIPE_TRIAL_KEYSTONE
import dev.nathanpb.dml.recipe.recipesOf
import dev.nathanpb.dml.data.DeepLearnerData
import dev.nathanpb.dml.data.dataModel
import dev.nathanpb.dml.enums.DataModelTier
import dev.nathanpb.dml.item.ITEM_DATA_MODEL_ZOMBIE
import dev.nathanpb.dml.item.ITEM_DEEP_LEARNER
import dev.nathanpb.dml.item.ITEM_PRISTINE_MATTER_ZOMBIE
import dev.nathanpb.dml.modular_armor.ItemModularGlitchArmor
import dev.nathanpb.dml.modular_armor.data.ModularArmorData
import dev.nathanpb.dml.modular_armor.screen.ModularArmorScreenHandlerFactory
import dev.nathanpb.dml.screen.handler.DeepLearnerScreenHandlerFactory
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.core.Direction

/**
 * Plays the mod in a real game window: places every machine (their renderers and models), opens every GUI and spawns
 * the System Glitch, taking screenshots (runs/clientGametest/screenshots). Any exception fails the test.
 */
@Suppress("unused")
class DmlClientGameTest : FabricClientGameTest {

    override fun runTest(context: ClientGameTestContext) {
        context.worldBuilder().create().use { world ->
            val server = world.server
            server.runCommand("gamemode creative @a")
            server.runCommand("time set day")
            server.runCommand("gamerule doDaylightCycle false")
            server.runCommand("gamerule doMobSpawning false")
            context.waitTicks(20)

            val base: BlockPos = server.computeOnServer<BlockPos, RuntimeException> { it.playerList.players.first().blockPosition() }
            // A row of machines in front of the player (who looks south)
            val machines = listOf(
                "dml-refabricated:data_synthesizer",
                "dml-refabricated:loot_fabricator",
                "dml-refabricated:trial_keystone",
                "dml-refabricated:disruptions_core",
                "dml-refabricated:matter_condenser",
                "dml-refabricated:simulation_chamber",
                "dml-refabricated:cafeteria",
                "dml-refabricated:glitched_tile",
                "dml-refabricated:glitched_stairs",
                "dml-refabricated:glitched_slab",
                "dml-refabricated:glitched_wall",
            )
            machines.forEachIndexed { i, id ->
                val pos = base.offset(i * 2 - machines.size, 0, 4)
                server.runCommand("setblock ${pos.x} ${pos.y} ${pos.z} $id[facing=north]")
                server.runCommand("setblock ${pos.x} ${pos.y} ${pos.z} $id keep")
            }
            val synthesizerPos = base.offset(-machines.size, 0, 4)

            // A data model inside the synthesizer: its renderer draws it and the scanning grid
            server.runOnServer<RuntimeException> {
                val blockEntity = it.overworld().getBlockEntity(synthesizerPos) as BlockEntityDataSynthesizer
                blockEntity.inventory.setItem(0, ItemStack(ITEM_DATA_MODEL_ZOMBIE).also { stack ->
                    stack.dataModel.dataAmount = DataModelTier.BASIC.dataAmount
                })
            }

            // Recipes are server-only since 1.21.2: the mod's recipe types must load from the data packs
            server.runOnServer<RuntimeException> {
                val level = it.overworld()
                check(recipesOf(level, RECIPE_TRIAL_KEYSTONE).size == 45) { "trial keystone recipes: " + recipesOf(level, RECIPE_TRIAL_KEYSTONE).size }
                check(recipesOf(level, RECIPE_LOOT_FABRICATOR).size == 9) { "loot fabricator recipes" }
                check(recipesOf(level, RECIPE_CRUSHING).isNotEmpty()) { "crushing recipes" }
            }

            // The loot fabricator turns pristine matter into loot, the synthesizer turns data into energy
            val fabricatorPos = base.offset(2 - machines.size, 0, 4)
            server.runOnServer<RuntimeException> {
                val level = it.overworld()
                val fabricator = level.getBlockEntity(fabricatorPos) as BlockEntityLootFabricator
                fabricator.inventory.setItem(0, ItemStack(ITEM_PRISTINE_MATTER_ZOMBIE, 2))
                fabricator.energyStorage.amount = fabricator.energyStorage.capacity
            }
            context.waitTicks(baseConfig.machines.lootFabricator.processTime + 40)
            server.runOnServer<RuntimeException> {
                val level = it.overworld()
                val fabricator = level.getBlockEntity(fabricatorPos) as BlockEntityLootFabricator
                check(LootFabricatorInventory.OUTPUT_SLOTS.any { slot -> !fabricator.inventory.getItem(slot).isEmpty }) { "the loot fabricator made no loot" }
                // (like 1.20.1, after the first one the following matter is processed without waiting again)
                check(fabricator.inventory.getItem(0).count < 2) { "the loot fabricator used no pristine matter" }
                val synthesizer = level.getBlockEntity(synthesizerPos) as BlockEntityDataSynthesizer
                check(synthesizer.energyStorage.amount > 0) { "the data synthesizer made no energy" }
            }

            server.runCommand("tp @a ${base.x} ${base.y + 1} ${base.z - 2} 0 20")
            context.waitTicks(40)
            context.takeScreenshot("dml-machines")

            server.runCommand("summon dml-refabricated:system_glitch ${base.x} ${base.y} ${base.z + 3}")
            context.waitTicks(20)
            context.takeScreenshot("dml-system-glitch")
            server.runCommand("kill @e[type=dml-refabricated:system_glitch]")

            // Block GUIs: right click every machine with an empty hand
            listOf(0, 1, 4, 5).forEach { i ->
                val pos = base.offset(i * 2 - machines.size, 0, 4)
                openBlockMenu(context, world, pos)
                context.waitForScreen(AbstractContainerScreen::class.java)
                context.waitTicks(5)
                context.takeScreenshot("dml-gui-" + machines[i].substringAfter(':'))
                closeMenu(context)
                context.waitTicks(2)
            }

            // Item GUIs: deep learner with a data model, glitch armor with a data model
            server.runOnServer<RuntimeException> {
                val player = it.playerList.players.first()
                val learner = ItemStack(ITEM_DEEP_LEARNER)
                DeepLearnerData(learner).inventory = DeepLearnerData(learner).inventory.also { inv ->
                    inv[0] = ItemStack(ITEM_DATA_MODEL_ZOMBIE)
                }
                player.setItemInHand(InteractionHand.MAIN_HAND, learner)
                player.openMenu(DeepLearnerScreenHandlerFactory(InteractionHand.MAIN_HAND, ITEM_DEEP_LEARNER))
            }
            context.waitForScreen(CottonInventoryScreen::class.java)
            context.waitTicks(10)
            context.takeScreenshot("dml-gui-deep_learner")
            closeMenu(context)

            server.runOnServer<RuntimeException> {
                val player = it.playerList.players.first()
                val armor = ItemStack(ItemModularGlitchArmor.GLITCH_CHESTPLATE)
                ModularArmorData(armor).dataModel = ItemStack(ITEM_DATA_MODEL_ZOMBIE).also { stack ->
                    stack.dataModel.dataAmount = DataModelTier.SELF_AWARE.dataAmount
                }.dataModel
                ModularArmorData(armor).pristineEnergy = 1000
                player.setItemInHand(InteractionHand.MAIN_HAND, armor)
                player.openMenu(ModularArmorScreenHandlerFactory(InteractionHand.MAIN_HAND, ItemModularGlitchArmor.GLITCH_CHESTPLATE))
            }
            context.waitForScreen(CottonInventoryScreen::class.java)
            context.waitTicks(10)
            context.takeScreenshot("dml-gui-glitch_armor")
            closeMenu(context)

            // Wear the armor (its attributes come from the data model) and hold some items
            server.runOnServer<RuntimeException> {
                val player: ServerPlayer = it.playerList.players.first()
                player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, player.mainHandItem.copy())
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack(ITEM_PRISTINE_MATTER_ZOMBIE, 3))
            }
            server.runCommand("tp @a ${base.x} ${base.y} ${base.z - 2} 180 0")
            // In creative the inventory is the creative tab screen
            server.runCommand("gamemode survival @a")
            context.waitTicks(20)
            context.setScreen { InventoryScreen(Minecraft.getInstance().player!!) }
            context.waitForScreen(InventoryScreen::class.java)
            context.waitTicks(5)
            context.takeScreenshot("dml-inventory")
            closeMenu(context)

            val armorValue = server.computeOnServer<Int, RuntimeException> { it.playerList.players.first().armorValue }
            check(armorValue > 0) { "the glitch chestplate gives no armor" }
        }
    }

    /** Closes the open menu like the player would, so the server knows (setScreen(null) alone doesn't tell it). */
    private fun closeMenu(context: ClientGameTestContext) {
        context.runOnClient<RuntimeException> { it.player?.closeContainer() }
        context.waitTicks(2)
    }

    /** Uses the block at [pos] on the server, like a right click (the server opens the menu). */
    private fun openBlockMenu(context: ClientGameTestContext, world: TestSingleplayerContext, pos: BlockPos) {
        world.server.runOnServer<RuntimeException> {
            val player = it.playerList.players.first()
            val level = it.overworld()
            val state = level.getBlockState(pos)
            state.useWithoutItem(level, player, BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false))
        }
    }
}
