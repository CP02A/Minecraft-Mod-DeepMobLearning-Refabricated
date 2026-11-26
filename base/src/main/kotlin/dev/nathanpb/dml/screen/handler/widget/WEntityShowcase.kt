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

package dev.nathanpb.dml.screen.handler.widget

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.EntitySpawnReason
import dev.nathanpb.dml.screen.handler.DeepLearnerScreenHandler
import dev.nathanpb.dml.utils.drawEntity
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WWidget
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class WEntityShowcase(
    private val root: SyncedGuiDescription
) : WWidget() {

    var entityTypes: List<EntityType<*>> = emptyList()

    private var tickCount = 0

    val entityType: EntityType<*>?
        get() {
            return entityTypes.takeIf { entityTypes.isNotEmpty() }?.let { values ->
                values.toTypedArray()[(tickCount / 60) % values.size]
            }
        }

    override fun tick() {
        super.tick()
        tickCount++
    }

    override fun canResize() = true

    /** The entities shown, made once per type (they are never added to the level). */
    private val entities = HashMap<EntityType<*>, LivingEntity?>()

    override fun paint(ctx: GuiGraphicsExtractor, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        super.paint(ctx, x, y, mouseX, mouseY)

        val entityType = entityType ?: return
        val world = Minecraft.getInstance().level ?: return
        val entity = entities.getOrPut(entityType) { createShowcaseEntity(entityType, world) } ?: return

        val w = (this.width / 2)
        val h = (height / 1.25).roundToInt()

        val scaleFactor = min(w, h) * 1.5
        val entityScaleFactor = max(entity.bbWidth, entity.bbHeight)

        drawEntity(
            ctx,
            entity,
            x, y, x + width, y + height,
            (scaleFactor / entityScaleFactor).roundToInt(),
            (tickCount % 360F) * 2F + 150F
        )

        // only update at the exact tick the mob changes
        if(tickCount % 3 == 0) {
            (root as DeepLearnerScreenHandler).updateEntityInformation()
        }
    }

}

/**
 * An entity for GUIs that is never added to the level. Since 1.21.6 rendering it reads its id, which only the level
 * assigns: it gets a fixed negative one.
 */
fun createShowcaseEntity(type: EntityType<*>, world: net.minecraft.world.level.Level): LivingEntity? {
    return (type.create(world, EntitySpawnReason.LOAD) as? LivingEntity)?.also { it.id = -1000 - BuiltInRegistries.ENTITY_TYPE.getId(type) }
}
