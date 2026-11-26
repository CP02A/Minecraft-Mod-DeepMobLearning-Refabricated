package dev.nathanpb.dml.misc.lootfunction

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.nathanpb.dml.baseConfig
import dev.nathanpb.dml.data.dataModel
import dev.nathanpb.dml.item.ItemDataModel
import dev.nathanpb.dml.misc.ATTUNED_DATA_MODELS
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import java.util.Optional

/** Replaces the stack with a random attuned data model with some data. */
class RandomDataModelLootFunction(condition: Optional<Holder<LootItemCondition>>): LootItemConditionalFunction(condition) {

    companion object {
        val CODEC: MapCodec<RandomDataModelLootFunction> = RecordCodecBuilder.mapCodec { i ->
            i.group(LootItemCondition.CODEC.optionalFieldOf("condition").forGetter { it.condition })
                .apply(i, ::RandomDataModelLootFunction)
        }
    }

    override fun run(stack: ItemStack, ctx: LootContext): ItemStack {
        val dataModelStack = ItemStack(BuiltInRegistries.ITEM.getTagOrEmpty(ATTUNED_DATA_MODELS).toList().random())

        if(dataModelStack.item is ItemDataModel) {
            val maxData = baseConfig.misc.disruption.maxDataModelData
            dataModelStack.dataModel.dataAmount = ctx.random.nextInt(maxData + 1).coerceIn(0, baseConfig.dataModel.selfAwareDataRequired)
        }

        return dataModelStack
    }

    override fun codec() = CODEC
}
