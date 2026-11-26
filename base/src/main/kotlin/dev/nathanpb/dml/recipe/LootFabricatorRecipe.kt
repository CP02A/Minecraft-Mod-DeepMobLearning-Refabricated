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

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.nathanpb.dml.enums.EntityCategory
import dev.nathanpb.dml.identifier
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.PlacementInfo
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeBookCategories
import net.minecraft.world.item.crafting.RecipeBookCategory
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.level.Level

/**
 * This is a kinda dummy recipe class made in order to provide compatibility with REI
 */
class LootFabricatorRecipe (
    val input: Ingredient,
    val category: EntityCategory
) : Recipe<SingleRecipeInput>, IdentifiedRecipe {

    override var id: Identifier = identifier("unknown")

    override fun getType() = RECIPE_LOOT_FABRICATOR

    override fun assemble(input: SingleRecipeInput): ItemStack = ItemStack.EMPTY

    override fun getSerializer() = SERIALIZER

    override fun matches(input: SingleRecipeInput, world: Level): Boolean {
        return this.input.test(input.item())
    }

    override fun showNotification() = false
    override fun group() = ""
    override fun isSpecial() = true
    override fun placementInfo(): PlacementInfo = PlacementInfo.NOT_PLACEABLE
    override fun recipeBookCategory(): RecipeBookCategory = RecipeBookCategories.CRAFTING_MISC

    override fun equals(other: Any?): Boolean {
        if (other is LootFabricatorRecipe) {
            return other.category.name == category.name
                && other.input.items().allMatch { input.test(ItemStack(it)) }
                && input.items().allMatch { other.input.test(ItemStack(it)) }
        }
        return false
    }

    override fun hashCode() = category.hashCode()

    companion object {
        val CODEC: MapCodec<LootFabricatorRecipe> = RecordCodecBuilder.mapCodec { i ->
            i.group(
                Ingredient.CODEC.fieldOf("input").forGetter(LootFabricatorRecipe::input),
                ENTITY_CATEGORY_CODEC.fieldOf("category").forGetter(LootFabricatorRecipe::category)
            ).apply(i, ::LootFabricatorRecipe)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, LootFabricatorRecipe> = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, LootFabricatorRecipe::input,
            ENTITY_CATEGORY_STREAM_CODEC, LootFabricatorRecipe::category,
            ::LootFabricatorRecipe
        )

        @Suppress("DEPRECATION")
        val SERIALIZER = RecipeSerializer(CODEC, STREAM_CODEC)
    }

}

/** Entity categories by their enum name, as the recipe files have them. */
val ENTITY_CATEGORY_CODEC: Codec<EntityCategory> = Codec.STRING.xmap(EntityCategory::valueOf, EntityCategory::name)
val ENTITY_CATEGORY_STREAM_CODEC: StreamCodec<io.netty.buffer.ByteBuf, EntityCategory> =
    ByteBufCodecs.STRING_UTF8.map(EntityCategory::valueOf, EntityCategory::name)
