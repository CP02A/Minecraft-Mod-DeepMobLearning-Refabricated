package dev.nathanpb.dml.misc.lootfunction

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.nathanpb.dml.baseConfig
import dev.nathanpb.dml.misc.PRISTINE_MATTER
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import java.util.Optional

/** Replaces the stack with a random amount of a random pristine matter. */
class RandomPristineMatterLootFunction(condition: Optional<Holder<LootItemCondition>>): LootItemConditionalFunction(condition) {

    companion object {
        val CODEC: MapCodec<RandomPristineMatterLootFunction> = RecordCodecBuilder.mapCodec { i ->
            i.group(LootItemCondition.CODEC.optionalFieldOf("condition").forGetter { it.condition })
                .apply(i, ::RandomPristineMatterLootFunction)
        }
    }

    override fun run(stack: ItemStack, ctx: LootContext): ItemStack {
        val pristineMatterStack = ItemStack(BuiltInRegistries.ITEM.getTagOrEmpty(PRISTINE_MATTER).toList().random())
        val maxStackSize = baseConfig.misc.disruption.maxPristineMatterStackSize
        pristineMatterStack.count = ctx.random.nextInt(maxStackSize + 1).coerceIn(0, 64)

        return pristineMatterStack
    }

    override fun codec() = CODEC
}
