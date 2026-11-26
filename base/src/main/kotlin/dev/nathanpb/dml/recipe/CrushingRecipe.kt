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

package dev.nathanpb.dml.recipe

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.nathanpb.dml.identifier
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.PlacementInfo
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeBookCategories
import net.minecraft.world.item.crafting.RecipeBookCategory
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block

/** Hitting [block] while holding [input] turns one input item into [output]. */
class CrushingRecipe (
    val input: Ingredient,
    val block: Block,
    val output: ItemStackTemplate
) : Recipe<SingleRecipeInput>, IdentifiedRecipe {

    override var id: Identifier = identifier("unknown")

    override fun getType() = RECIPE_CRUSHING
    override fun getSerializer() = SERIALIZER

    /** The result; the caller shrinks the input stack. */
    override fun assemble(input: SingleRecipeInput): ItemStack = output.create()

    override fun matches(input: SingleRecipeInput, world: Level) = this.input.test(input.item())

    override fun showNotification() = false
    override fun group() = ""
    override fun isSpecial() = true
    override fun placementInfo(): PlacementInfo = PlacementInfo.NOT_PLACEABLE
    override fun recipeBookCategory(): RecipeBookCategory = RecipeBookCategories.CRAFTING_MISC

    companion object {
        val CODEC: MapCodec<CrushingRecipe> = RecordCodecBuilder.mapCodec { i ->
            i.group(
                Ingredient.CODEC.fieldOf("input").forGetter(CrushingRecipe::input),
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(CrushingRecipe::block),
                ItemStackTemplate.CODEC.fieldOf("output").forGetter(CrushingRecipe::output)
            ).apply(i, ::CrushingRecipe)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CrushingRecipe::input,
            ByteBufCodecs.registry(Registries.BLOCK), CrushingRecipe::block,
            ItemStackTemplate.STREAM_CODEC, CrushingRecipe::output,
            ::CrushingRecipe
        )

        @Suppress("DEPRECATION")
        val SERIALIZER = RecipeSerializer(CODEC, STREAM_CODEC)
    }
}
