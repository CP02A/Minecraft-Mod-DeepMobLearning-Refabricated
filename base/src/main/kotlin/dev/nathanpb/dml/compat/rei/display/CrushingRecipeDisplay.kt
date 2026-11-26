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

package dev.nathanpb.dml.compat.rei.display

import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.nathanpb.dml.compat.rei.ReiCategories
import dev.nathanpb.dml.recipe.CrushingRecipe
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.display.DisplaySerializer
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.util.EntryIngredients
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.Identifier
import java.util.Optional

/** Since 1.21.2 the server makes the displays and sends them to the clients (which have no recipes). */
class CrushingRecipeDisplay(
    val recipe: CrushingRecipe,
    val id: Identifier
) : Display {

    override fun getCategoryIdentifier(): CategoryIdentifier<CrushingRecipeDisplay> = ReiCategories.CRUSHING_CATEGORY

    override fun getInputEntries(): List<EntryIngredient> {
        return listOf(
            EntryIngredients.ofIngredient(recipe.input),
            EntryIngredients.of(recipe.block)
        )
    }

    override fun getOutputEntries(): List<EntryIngredient> = listOf(EntryIngredients.of(recipe.output.create()))

    override fun getDisplayLocation(): Optional<Identifier> = Optional.of(id)

    override fun getSerializer() = SERIALIZER

    companion object {
        val SERIALIZER: DisplaySerializer<CrushingRecipeDisplay> = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec { i ->
                i.group(
                    CrushingRecipe.CODEC.codec().fieldOf("recipe").forGetter(CrushingRecipeDisplay::recipe),
                    Identifier.CODEC.fieldOf("id").forGetter(CrushingRecipeDisplay::id)
                ).apply(i, ::CrushingRecipeDisplay)
            },
            StreamCodec.composite(
                CrushingRecipe.STREAM_CODEC, CrushingRecipeDisplay::recipe,
                Identifier.STREAM_CODEC, CrushingRecipeDisplay::id,
                ::CrushingRecipeDisplay
            )
        )
    }
}
