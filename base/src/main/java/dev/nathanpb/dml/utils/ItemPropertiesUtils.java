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

package dev.nathanpb.dml.utils;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;

public final class ItemPropertiesUtils {
    private ItemPropertiesUtils() {
    }

    /**
     * Removes a component the properties set before (e.g. through a tool material): a null value removes it. In Java
     * because Kotlin does not accept null for the (non-null) component value type.
     */
    @SuppressWarnings({"unchecked", "rawtypes", "DataFlowIssue"})
    public static Item.Properties removeComponent(Item.Properties properties, DataComponentType<?> type) {
        return properties.component((DataComponentType) type, null);
    }
}
