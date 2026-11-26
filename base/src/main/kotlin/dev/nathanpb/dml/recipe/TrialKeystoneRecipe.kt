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
import dev.nathanpb.dml.data.TrialKeyData
import dev.nathanpb.dml.enums.DataModelTier
import dev.nathanpb.dml.enums.EntityCategory
import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.item.ITEM_GLITCH_UPGRADE_SMITHING_TEMPLATE
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.crafting.PlacementInfo
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeBookCategories
import net.minecraft.world.item.crafting.RecipeBookCategory
import net.minecraft.world.item.crafting.RecipeInput
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.Level
import java.util.Optional
import kotlin.random.Random

class TrialKeystoneRecipe (
    val category: EntityCategory,
    val tier: DataModelTier,
    /** The spawn weights by entity id pattern, as in the recipe file. */
    private val spawnWeights: Map<String, Int>,
    private val rewards: List<ItemStackTemplate>,
    private val waveEntityCountOverride: Optional<Int>,
    private val waveRespawnTimeoutOverride: Optional<Int>
) : Recipe<RecipeInput>, IdentifiedRecipe {

    override var id: Identifier = identifier("unknown")

    /** The spawn chance of each entity id pattern (the weights normalized to a sum of 1). */
    val spawnRate: Map<Regex, Float> = spawnWeights.values.sum().toFloat().let { sum ->
        spawnWeights.entries.associate { (k, v) -> k.toRegex() to v / sum }
    }

    val waveEntityCount = waveEntityCountOverride.orElse(tier.defaultWaveEntityCount)
    val waveRespawnTimeout = waveRespawnTimeoutOverride.orElse(tier.defaultWaveRespawnTimeout)

    companion object {
        fun findOrNull(world: Level, data: TrialKeyData) = recipesOf(world, RECIPE_TRIAL_KEYSTONE)
            .firstOrNull { it.category == data.category && it.tier == data.tier() }

        private val TIER_CODEC: Codec<DataModelTier> = Codec.INT.xmap(
            { DataModelTier.fromIndex(it) ?: DataModelTier.FAULTY },
            DataModelTier::ordinal
        )

        val CODEC: MapCodec<TrialKeystoneRecipe> = RecordCodecBuilder.mapCodec { i ->
            i.group(
                ENTITY_CATEGORY_CODEC.fieldOf("category").forGetter(TrialKeystoneRecipe::category),
                TIER_CODEC.fieldOf("tier").forGetter(TrialKeystoneRecipe::tier),
                Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("spawnRates").forGetter { it.spawnWeights },
                ItemStackTemplate.CODEC.listOf().fieldOf("rewards").forGetter { it.rewards },
                Codec.INT.optionalFieldOf("waveEntityCount").forGetter { it.waveEntityCountOverride },
                Codec.INT.optionalFieldOf("waveRespawnTimeout").forGetter { it.waveRespawnTimeoutOverride }
            ).apply(i, ::TrialKeystoneRecipe)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, TrialKeystoneRecipe> = StreamCodec.composite(
            ENTITY_CATEGORY_STREAM_CODEC, TrialKeystoneRecipe::category,
            ByteBufCodecs.VAR_INT.map({ DataModelTier.fromIndex(it) ?: DataModelTier.FAULTY }, DataModelTier::ordinal), TrialKeystoneRecipe::tier,
            ByteBufCodecs.map(::HashMap, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT), { HashMap(it.spawnWeights) },
            ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()), { it.rewards },
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), { it.waveEntityCountOverride },
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), { it.waveRespawnTimeoutOverride },
            ::TrialKeystoneRecipe
        )

        @Suppress("DEPRECATION")
        val SERIALIZER = RecipeSerializer(CODEC, STREAM_CODEC)
    }

    fun copyRewards(onREI: Boolean = false): MutableList<ItemStack> {
        val rewardsCopy = rewards.map(ItemStackTemplate::create).toMutableList()

        // Glitch Upgrade
        if(tier.glitchUpgradeOdds > 0 && (Random.nextDouble() < tier.glitchUpgradeOdds || onREI)) {
            rewardsCopy.add(ItemStack(ITEM_GLITCH_UPGRADE_SMITHING_TEMPLATE))
        }


        // TODO System Glitch Head
        /*
        if(tier == DataModelTier.SELF_AWARE) {
            if((Random.nextDouble() < 0.15 || onREI)) {
                //rewardsCopy.add(ItemStack(SYSTEM_GLITCH_HEAD))
            }
        }*/

        return rewardsCopy
    }

    @Deprecated("", ReplaceWith("copyRewards", "dev.nathanpb.dml.recipe"))
    override fun assemble(input: RecipeInput): ItemStack = ItemStack.EMPTY

    override fun getType() = RECIPE_TRIAL_KEYSTONE

    override fun getSerializer() = SERIALIZER

    override fun matches(input: RecipeInput, world: Level): Boolean {
        return false
    }

    override fun showNotification() = false
    override fun group() = ""
    override fun isSpecial() = true
    override fun placementInfo(): PlacementInfo = PlacementInfo.NOT_PLACEABLE
    override fun recipeBookCategory(): RecipeBookCategory = RecipeBookCategories.CRAFTING_MISC
}
