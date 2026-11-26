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

package dev.nathanpb.dml.screen.handler

import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.inventory.LootFabricatorInventory.Companion.ENERGY_INPUT
import dev.nathanpb.dml.item.ItemPristineMatter
import dev.nathanpb.dml.recipe.RECIPE_LOOT_FABRICATOR
import dev.nathanpb.dml.recipe.recipesOf
import dev.nathanpb.dml.screen.handler.widget.WEnergyComponent
import dev.nathanpb.dml.utils.RenderUtils
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WBar
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.Insets
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.SimpleContainer
import net.minecraft.world.inventory.ContainerLevelAccess

class LootFabricatorScreenHandler(
    syncId: Int,
    playerInventory: Inventory,
    ctx: ContainerLevelAccess
) : SyncedGuiDescription(
    HANDLER_LOOT_FABRICATOR,
    syncId, playerInventory,
    getBlockInventory(ctx),
    getBlockPropertyDelegate(ctx)
) {
    init {
        val root = WPlainPanel()
        root.insets = Insets.ROOT_PANEL
        setRootPanel(root)

        val inputSlot = WItemSlot(blockInventory!!, 0, 1, 1, false).apply {
            // Recipes only exist on the server: the client accepts all pristine matter (the server decides)
            setInputFilter { stack ->
                if (level.isClientSide) {
                    stack.item is ItemPristineMatter
                } else {
                    recipesOf(level, RECIPE_LOOT_FABRICATOR).any { it.input.test(stack) }
                }
            }

            icon = TextureIcon(identifier("textures/gui/slot_background/pristine_matter_slot_background.png"))
        }
        root.add(inputSlot, (2 * 18) + 16, 2 * 18)

        val progressBar = WBar(RenderUtils.ARROW_BACKGROUND, RenderUtils.ARROW, 0, 1, WBar.Direction.RIGHT)
        root.add(progressBar, (4 * 18) + 5, (2 * 18) + 1, 22, 16)


        (0 until 9).forEach {
            val x = (it % 3)
            val y = (it / 3)
            val slot = WItemSlot.of(blockInventory!!, it + 1).setInputFilter { false }
            root.add(slot, (x + 6) * 18, (y + 1) * 18)
        }

        val energyComponent = WEnergyComponent(2, 3, blockInventory!!, ENERGY_INPUT)
        root.add(energyComponent, 0, (1 * 18) - 6)

        root.add(createPlayerInventoryPanel(), 0, 5 * 18)
        setTitleAlignment(HorizontalAlignment.CENTER)
        root.validate(this)
    }

    override fun stillValid(player: Player) = true

    override fun addPainters() {
        rootPanel.backgroundPainter = RenderUtils.DEFAULT_BACKGROUND_PAINTER
    }

    override fun getTitleColor(): Int {
        return RenderUtils.TITLE_COLOR
    }

}