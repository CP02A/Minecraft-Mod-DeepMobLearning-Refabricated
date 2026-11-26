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

package dev.nathanpb.dml.compat.rei

import dev.nathanpb.dml.compat.rei.display.CrushingRecipeDisplay
import dev.nathanpb.dml.compat.rei.display.LootFabricatorRecipeDisplay
import dev.nathanpb.dml.compat.rei.display.TrialRecipeDisplay
import dev.nathanpb.dml.identifier
import me.shedaniel.rei.api.common.category.CategoryIdentifier

/** The REI categories, shared by the common (server) and client plugins. */
object ReiCategories {
    val CRUSHING_CATEGORY: CategoryIdentifier<CrushingRecipeDisplay> = CategoryIdentifier.of(identifier("crushing"))
    val TRIAL_CATEGORY: CategoryIdentifier<TrialRecipeDisplay> = CategoryIdentifier.of(identifier("trial"))
    val LOOT_FABRICATOR_CATEGORY: CategoryIdentifier<LootFabricatorRecipeDisplay> = CategoryIdentifier.of(identifier("loot_fabricator"))
}
