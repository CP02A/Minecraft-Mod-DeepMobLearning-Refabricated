package dev.nathanpb.dml.utils

import net.minecraft.world.item.Rarity

data class ItemTuple(val identifier: String, val shown: Boolean = true)

data class RarityTuple(val identifier: String, val rarity: Rarity = Rarity.COMMON)