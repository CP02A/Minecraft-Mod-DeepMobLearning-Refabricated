package dev.nathanpb.dml.misc.lootfunction

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.nathanpb.dml.baseConfig
import net.minecraft.core.Holder
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import team.reborn.energy.api.base.SimpleEnergyItem
import java.util.Optional

/** Charges an energy item to a random part of its capacity. */
class RandomEnergyLootFunction(condition: Optional<Holder<LootItemCondition>>): LootItemConditionalFunction(condition) {

    companion object {
        val CODEC: MapCodec<RandomEnergyLootFunction> = RecordCodecBuilder.mapCodec { i ->
            i.group(LootItemCondition.CODEC.optionalFieldOf("condition").forGetter { it.condition })
                .apply(i, ::RandomEnergyLootFunction)
        }
    }

    override fun run(stack: ItemStack, ctx: LootContext): ItemStack {
        val item = stack.item
        if(item is SimpleEnergyItem) {
            val capacity = item.getEnergyCapacity(stack)
            val percentage = ctx.random.nextFloat().coerceIn(0F, baseConfig.misc.disruption.maxEnergyOctahedronEnergyPercentage)
            item.setStoredEnergy(stack, (capacity * percentage).toLong())
        }
        return stack
    }

    override fun codec() = CODEC
}
