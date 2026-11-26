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
import dev.nathanpb.dml.data.DataModelData
import dev.nathanpb.dml.data.TrialKeyData
import dev.nathanpb.dml.data.dataModel
import dev.nathanpb.dml.data.trialKeyData
import dev.nathanpb.dml.item.ItemDataModel
import dev.nathanpb.dml.item.ItemTrialKey
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.NormalCraftingRecipe
import net.minecraft.world.item.crafting.PlacementInfo
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.Level

/** A shapeless recipe that binds the trial key to the category and tier of the data model in the grid. */
class TrialKeyAttuneRecipe (
    val inputs: List<Ingredient>,
    private val output: ItemStackTemplate
) : NormalCraftingRecipe(Recipe.CommonInfo(true), CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, "")) {

    val result: ItemStack get() = output.create()

    override fun assemble(input: CraftingInput): ItemStack = output.create().apply {
        findDataModel(input)?.let {
            trialKeyData = TrialKeyData.fromDataModelData(it)
        }
    }

    override fun getSerializer() = SERIALIZER

    override fun createPlacementInfo(): PlacementInfo = PlacementInfo.create(inputs)

    override fun matches(input: CraftingInput, world: Level): Boolean {
        if (input.ingredientCount() != inputs.size || !input.stackedContents().canCraft(this, null)) return false
        val dataModel = findDataModel(input)
        return dataModel != null
                && !hasBoundedTrialKey(input)
                && hasTrialKeyRecipe(dataModel, world)
    }

    private fun findDataModel(inv: CraftingInput) = inv.items().firstOrNull {
        it.item is ItemDataModel && it.dataModel.category != null
    }?.dataModel

    private fun hasBoundedTrialKey(inv: CraftingInput) = inv.items().any {
        it.item is ItemTrialKey && it.trialKeyData != null
    }

    /** Recipes only exist on the server: the client accepts every data model (the server has the last word). */
    private fun hasTrialKeyRecipe(model: DataModelData, world: Level) : Boolean {
        if (world.isClientSide) return true
        return recipesOf(world, RECIPE_TRIAL_KEYSTONE).any {
            model.category == it.category && it.tier == model.tier()
        }
    }

    companion object {
        val CODEC: MapCodec<TrialKeyAttuneRecipe> = RecordCodecBuilder.mapCodec { i ->
            i.group(
                Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(TrialKeyAttuneRecipe::inputs),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter { it.output }
            ).apply(i, ::TrialKeyAttuneRecipe)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, TrialKeyAttuneRecipe> = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), TrialKeyAttuneRecipe::inputs,
            ItemStackTemplate.STREAM_CODEC, { it.output },
            ::TrialKeyAttuneRecipe
        )

        @Suppress("DEPRECATION")
        val SERIALIZER = RecipeSerializer(CODEC, STREAM_CODEC)
    }
}
