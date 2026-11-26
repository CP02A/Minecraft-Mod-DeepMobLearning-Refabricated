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

package dev.nathanpb.dml.modular_armor

import dev.nathanpb.dml.identifier
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.resources.Identifier

/** Translated as key.category.dml-refabricated.modular_armor (since 1.21.9 categories are registered ids). */
val CATEGORY_MODULAR_ARMOR: KeyMapping.Category = KeyMapping.Category.register(identifier("modular_armor"))

val TELEPORT_KEYBINDING = register(identifier("teleport"), InputConstants.KEY_V, CATEGORY_MODULAR_ARMOR)
val SOUL_VISION_KEYBINDING = register(identifier("soul_vision"), InputConstants.KEY_B, CATEGORY_MODULAR_ARMOR)

private fun register(
    id: Identifier,
    default: Int,
    category: KeyMapping.Category,
    type: InputConstants.Type = InputConstants.Type.KEYBOARD
): KeyMapping {
    return KeyMappingHelper.registerKeyMapping(
        KeyMapping(
            "key.${id.namespace}.${id.path}",
            type,
            default,
            category
        )
    )
}

fun registerKeybindings() {
    TELEPORT_KEYBINDING
    SOUL_VISION_KEYBINDING
}

