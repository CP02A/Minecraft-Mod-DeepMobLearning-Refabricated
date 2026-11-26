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
import dev.nathanpb.dml.recipe.CrushingRecipe
import dev.nathanpb.dml.recipe.LootFabricatorRecipe
import dev.nathanpb.dml.recipe.TrialKeystoneRecipe
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry
import me.shedaniel.rei.api.common.plugins.REICommonPlugin
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry

/**
 * Since 1.21.2 the client has no recipes: the server makes the displays from its recipes and REI sends them to the
 * clients with these serializers.
 */
class ReiCommonPlugin : REICommonPlugin {

    override fun registerDisplaySerializer(registry: DisplaySerializerRegistry) {
        registry.register(identifier("crushing"), CrushingRecipeDisplay.SERIALIZER)
        registry.register(identifier("trial"), TrialRecipeDisplay.SERIALIZER)
        registry.register(identifier("loot_fabricator"), LootFabricatorRecipeDisplay.SERIALIZER)
    }

    override fun registerDisplays(registry: ServerDisplayRegistry) {
        registry.beginRecipeFiller<CrushingRecipe, CrushingRecipeDisplay>(CrushingRecipe::class.java).fill { holder ->
            CrushingRecipeDisplay(holder.value(), holder.id().identifier())
        }
        registry.beginRecipeFiller<TrialKeystoneRecipe, TrialRecipeDisplay>(TrialKeystoneRecipe::class.java).fill { holder ->
            TrialRecipeDisplay(holder.value(), holder.id().identifier())
        }
        registry.beginRecipeFiller<LootFabricatorRecipe, LootFabricatorRecipeDisplay>(LootFabricatorRecipe::class.java).fill { holder ->
            LootFabricatorRecipeDisplay(holder.value(), holder.id().identifier())
        }
    }
}
