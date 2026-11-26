package dev.nathanpb.dml.misc

import dev.nathanpb.dml.identifier
import net.minecraft.world.level.block.Block
import net.minecraft.world.item.Item
import net.minecraft.core.registries.Registries

import net.minecraft.tags.TagKey


val TRIAL_GRIEF_WHITELIST: TagKey<Block> = TagKey.create(Registries.BLOCK, identifier("trial_grief_whitelist"))

val ATTUNED_DATA_MODELS: TagKey<Item> = TagKey.create(Registries.ITEM, identifier("attuned_data_models"))
val PRISTINE_MATTER: TagKey<Item> = TagKey.create(Registries.ITEM, identifier("pristine_matter"))