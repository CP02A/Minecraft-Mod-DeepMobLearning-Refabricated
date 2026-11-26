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

package dev.nathanpb.dml.compat.rei.widgets

import dev.nathanpb.dml.compat.rei.isInReiScreen
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.InteractionHand

class EntityDisplayWidget(
    private val entities: List<LivingEntity>,
    private val x: Int,
    private val y: Int,
    private val mouseX: Float,
    private val mouseY: Float,
    private val size: Int,
    private val stackInMainHand: ItemStack? = null,
    private val preRender: ((LivingEntity)->Unit) = {}
) : Renderable, GuiEventListener {

    private var tickCount = 0

    override fun extractRenderState(ctx: GuiGraphicsExtractor, mX: Int, mY: Int, delta: Float) = extract(ctx, 0, 0)

    /** Draws with (x, y) moved by ([offsetX], [offsetY]): GUI entities are placed in screen coordinates. */
    fun extract(ctx: GuiGraphicsExtractor, offsetX: Int, offsetY: Int) {
        val x = x + offsetX
        val y = y + offsetY
        tickCount++
        val entity = entities[(tickCount / 60) % entities.size]
        entity.isInReiScreen = true
        val initialStackMainHand = stackInMainHand?.let {
            entity.getItemInHand(InteractionHand.MAIN_HAND).also {
                entity.setItemInHand(InteractionHand.MAIN_HAND, stackInMainHand)
            }
        }

        preRender.invoke(entity)
        // (x, y) is the bottom center of the entity, (mouseX, mouseY) where it looks at
        InventoryScreen.extractEntityInInventoryFollowsMouse(ctx, x - size, y - size * 2, x + size, y, size, 0F, x + mouseX, y + mouseY, entity)

        if (stackInMainHand != null) {
            entity.setItemInHand(InteractionHand.MAIN_HAND, initialStackMainHand ?: ItemStack.EMPTY)
        }
        entity.isInReiScreen = false
    }

    override fun setFocused(focused: Boolean) {
        throw IllegalStateException("don't?")
    }

    override fun isFocused(): Boolean {
        return true
    }

}
