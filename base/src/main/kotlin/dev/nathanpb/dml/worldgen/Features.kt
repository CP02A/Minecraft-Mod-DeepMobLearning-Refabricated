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

package dev.nathanpb.dml.worldgen

import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.worldgen.disruption.DisruptionFeature
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import java.util.function.Predicate

fun registerFeatures() {
    // Since 26.1 the feature type registry holds the codecs of the (configured) features
    Registry.register(BuiltInRegistries.FEATURE_TYPE, identifier("disruption"), DisruptionFeature.CODEC)

    /*addFeature(
        DISRUPTION_KEY,
        BiomeSelectors.foundInOverworld().and(
            Predicate.not(BiomeSelectors.tag(ConventionalBiomeTags.AQUATIC))
        ),
        true
    ) // TODO add config to disable*/
}


@Suppress("unused")
private fun addFeature(registryKey: ResourceKey<PlacedFeature>, biomeSelector: Predicate<BiomeSelectionContext>, enabled: Boolean) {
    if (!enabled) return
    BiomeModifications.addFeature(biomeSelector, GenerationStep.Decoration.VEGETAL_DECORATION, registryKey)
}

val DISRUPTION_KEY: ResourceKey<PlacedFeature> = ResourceKey.create(Registries.PLACED_FEATURE, identifier("disruption"))
