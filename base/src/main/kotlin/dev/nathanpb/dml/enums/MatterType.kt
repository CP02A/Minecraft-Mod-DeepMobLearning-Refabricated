package dev.nathanpb.dml.enums

import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.utils.getItemFromRegistry
import net.minecraft.world.item.Item
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting

/** simulacrum only! */
enum class MatterType(id: String, color: ChatFormatting) {

    OVERWORLD("overworld", ChatFormatting.GREEN),
    HELLISH("hellish", ChatFormatting.RED),
    EXTRATERRESTRIAL("extraterrestrial", ChatFormatting.LIGHT_PURPLE);


    val text: Component = Component.translatable("modelType.${MOD_ID}.${id}").withStyle(color)

    val matter: Item? by lazy { getItemFromRegistry(identifier("${id}_matter")) }

}