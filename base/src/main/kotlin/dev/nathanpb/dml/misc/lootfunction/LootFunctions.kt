package dev.nathanpb.dml.misc.lootfunction

import com.mojang.serialization.MapCodec
import dev.nathanpb.dml.identifier
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.storage.loot.functions.LootItemFunction

// Since 1.20.5 a loot function type is its codec
private fun <T : LootItemFunction> registerLootFunction(id: String, codec: MapCodec<T>): MapCodec<T> {
    return Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, identifier(id), codec)
}

fun registerLootFunctions() {
    registerLootFunction("random_energy", RandomEnergyLootFunction.CODEC)
    registerLootFunction("random_data_model", RandomDataModelLootFunction.CODEC)
    registerLootFunction("random_pristine_matter", RandomPristineMatterLootFunction.CODEC)
}
