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

package dev.nathanpb.dml.modular_armor

import dev.nathanpb.dml.MOD_ID
import dev.nathanpb.dml.data.DataModelData
import dev.nathanpb.dml.item.ITEM_GLITCH_SWORD
import dev.nathanpb.dml.itemgroup.ITEM_GROUP_KEY
import dev.nathanpb.dml.modular_armor.core.ModularEffectRegistry
import dev.nathanpb.dml.modular_armor.data.ModularArmorData
import dev.nathanpb.dml.modular_armor.screen.ModularArmorScreenHandlerFactory
import dev.nathanpb.dml.utils.ITEM_PRISTINE
import dev.nathanpb.dml.utils.RenderUtils.Companion.ALT_STYLE
import dev.nathanpb.dml.utils.RenderUtils.Companion.STYLE
import dev.nathanpb.dml.utils.RenderUtils.Companion.TITLE_COLOR
import dev.nathanpb.dml.utils.getEmptyAndFullCapacityEnergyItem
import dev.nathanpb.dml.utils.getEnergyBarStep
import dev.nathanpb.dml.utils.getEnergyStorage
import dev.nathanpb.dml.utils.getFullCapacityStack
import dev.nathanpb.dml.utils.getInfoText
import dev.nathanpb.dml.utils.getParenthesisText
import dev.nathanpb.dml.utils.getPristineEnergyTooltipText
import dev.nathanpb.dml.utils.registerItem
import dev.nathanpb.dml.utils.removeComponent
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents
import net.fabricmc.fabric.api.item.v1.EnchantingContext
import net.minecraft.ChatFormatting
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Unit
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.item.equipment.ArmorType
import net.minecraft.world.level.Level
import team.reborn.energy.api.base.SimpleEnergyItem
import java.util.function.Consumer

/**
 * Armor that runs on pristine energy and gets its protection and modular effects from the data model inside.
 *
 * Since 1.20.5 attribute modifiers are a component of the stack instead of computed by the item: they are rebuilt
 * from the armor data in [inventoryTick] (see [updateAttributeModifiers]).
 */
class ItemModularGlitchArmor(val type: ArmorType, properties: Properties) : Item(
    properties.humanoidArmor(GlitchArmorMaterial.MATERIAL, type)
        .fireResistant()
        .rarity(Rarity.EPIC)
        // Pristine energy instead of durability
        .removeComponent(DataComponents.MAX_DAMAGE)
        .removeComponent(DataComponents.DAMAGE)
        .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
), SimpleEnergyItem {

    val slot: EquipmentSlot get() = type.slot

    companion object {
        lateinit var GLITCH_HELMET: ItemModularGlitchArmor
        lateinit var GLITCH_CHESTPLATE: ItemModularGlitchArmor
        lateinit var GLITCH_LEGGINGS: ItemModularGlitchArmor
        lateinit var GLITCH_BOOTS: ItemModularGlitchArmor

        fun register() {
            GLITCH_BOOTS = registerItem("glitch_boots") { ItemModularGlitchArmor(ArmorType.BOOTS, it) }
            GLITCH_LEGGINGS = registerItem("glitch_leggings") { ItemModularGlitchArmor(ArmorType.LEGGINGS, it) }
            GLITCH_CHESTPLATE = registerItem("glitch_chestplate") { ItemModularGlitchArmor(ArmorType.CHESTPLATE, it) }
            GLITCH_HELMET = registerItem("glitch_helmet") { ItemModularGlitchArmor(ArmorType.HELMET, it) }

            listOf(GLITCH_BOOTS, GLITCH_LEGGINGS, GLITCH_CHESTPLATE, GLITCH_HELMET).forEach { item ->
                ITEM_PRISTINE.registerForItems(::getEnergyStorage, item)

                CreativeModeTabEvents.modifyOutputEvent(ITEM_GROUP_KEY).register {
                    it.insertAfter(getFullCapacityStack(ITEM_GLITCH_SWORD), getEmptyAndFullCapacityEnergyItem(item))
                }
            }
        }

        private val ARMOR_MODIFIER_ID = Identifier.withDefaultNamespace("armor")
    }

    override fun getEnergyCapacity(stack: ItemStack): Long = getEnergyProperties()!!.capacity

    override fun getEnergyMaxInput(stack: ItemStack): Long = getEnergyProperties()!!.input

    override fun getEnergyMaxOutput(stack: ItemStack): Long = getEnergyProperties()!!.output

    override fun getBarWidth(stack: ItemStack): Int = getEnergyBarStep(stack)

    override fun getBarColor(stack: ItemStack) = TITLE_COLOR

    override fun isBarVisible(stack: ItemStack): Boolean = true



    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        display: TooltipDisplay,
        tooltip: Consumer<Component>,
        flag: TooltipFlag
    ) {
        val data = ModularArmorData(stack)
        if(data.dataAmount > 0) { // TODO remove in 1.21
            tooltip.accept(Component.translatable("tooltip.dml-refabricated.glitch_armor_upgrade.1").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
            tooltip.accept(Component.translatable("tooltip.dml-refabricated.glitch_armor_upgrade.2").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
            return
        }

        tooltip.accept(getPristineEnergyTooltipText(stack))

        val dataModelText: Component
        val dataModel = data.dataModel
        @Suppress("LiftReturnOrAssignment")
        if(dataModel != null) {
            val tierText = getParenthesisText(
                Component.translatable("tooltip.${MOD_ID}.tier.2", dataModel.category!!.displayName),
                dataModel.tier().text

            )
            dataModelText = getInfoText(
                Component.translatable("tooltip.${MOD_ID}.data_model_header"),
                tierText,
                STYLE,
                ALT_STYLE
            )
        } else {
            dataModelText = Component.translatable("tooltip.${MOD_ID}.no_data_model").withStyle(ChatFormatting.DARK_RED)
        }
        tooltip.accept(dataModelText)
    }

    /** The armor, toughness, knockback resistance and modular effect levels of this stack's data model. */
    fun computeAttributeModifiers(stack: ItemStack): ItemAttributeModifiers {
        val builder = ItemAttributeModifiers.builder()
        val group = EquipmentSlotGroup.bySlot(slot)
        val data = ModularArmorData(stack)
        val modifierId = ARMOR_MODIFIER_ID.withSuffix(".${type.getName()}")

        val protection = GlitchArmorMaterial.getProtectionAmount(slot, data.tier()).toDouble()
        val toughness = GlitchArmorMaterial.getToughness(data.tier()).toDouble()
        val knockback = GlitchArmorMaterial.getKnockbackResistance(data.tier()).toDouble()

        builder.add(Attributes.ARMOR, AttributeModifier(modifierId, protection, AttributeModifier.Operation.ADD_VALUE), group)
        builder.add(Attributes.ARMOR_TOUGHNESS, AttributeModifier(modifierId, toughness, AttributeModifier.Operation.ADD_VALUE), group)
        builder.add(Attributes.KNOCKBACK_RESISTANCE, AttributeModifier(modifierId, knockback, AttributeModifier.Operation.ADD_VALUE), group)

        data.dataModel?.let { dataModel ->
            if (dataModel.category != null) {
                appendModularEffectModifiers(data, dataModel).forEach { (attribute, modifier) ->
                    builder.add(attribute, modifier, group)
                }
            }
        }
        return builder.build()
    }

    /** Stores the modifiers of [computeAttributeModifiers] in the stack, if they changed. */
    fun updateAttributeModifiers(stack: ItemStack) {
        val modifiers = computeAttributeModifiers(stack)
        if (stack.get(DataComponents.ATTRIBUTE_MODIFIERS) != modifiers) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers)
        }
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResult {
        if(world.isClientSide()) return InteractionResult.PASS

        /*if(user.isCreative && user.isCrouching) { // TODO add energy cheat
            val data = ModularArmorData(stack)

            data.dataAmount = ModularArmorData.amountRequiredTo(
                if(tier.isMaxTier()) DataModelTier.FAULTY else tier.nextTierOrCurrent()
            )
            return InteractionResultHolder.success(stack)
        }*/

        user.openMenu(ModularArmorScreenHandlerFactory(hand, this))
        return InteractionResult.SUCCESS
    }

    private fun appendModularEffectModifiers(armor: ModularArmorData, dataModel: DataModelData): List<Pair<Holder<net.minecraft.world.entity.ai.attributes.Attribute>, AttributeModifier>> {
        val category = dataModel.category ?: return emptyList()
        return ModularEffectRegistry.INSTANCE.allMatching(category, armor.tier())
            .filter { it.id !in armor.disabledEffects }
            .map { it.entityAttribute to it.createEntityAttributeModifier(armor) }
    }

    override fun inventoryTick(stack: ItemStack, world: ServerLevel, entity: Entity, slot: EquipmentSlot?) {
        super.inventoryTick(stack, world, entity, slot)
        updateAttributeModifiers(stack)
        // No mending on pristine energy armor (it can't be enchanted with it, see canBeEnchantedWith)
        if (stack.isEnchanted) {
            stack.enchantments.keySet().firstOrNull { it.`is`(Enchantments.MENDING) }?.let { mending ->
                net.minecraft.world.item.enchantment.EnchantmentHelper.updateEnchantments(stack) { it.removeIf { holder -> holder == mending } }
            }
        }
    }

    override fun canBeEnchantedWith(stack: ItemStack, enchantment: Holder<Enchantment>, context: EnchantingContext): Boolean {
        return !enchantment.`is`(Enchantments.MENDING) && super.canBeEnchantedWith(stack, enchantment, context)
    }

    private fun getEnergyProperties() = energyProperties[type]

    private val energyProperties: HashMap<ArmorType, EnergyProperties> = hashMapOf(
        ArmorType.HELMET to scaleEnergyProperties(5),
        ArmorType.CHESTPLATE to scaleEnergyProperties(8),
        ArmorType.LEGGINGS to scaleEnergyProperties(7),
        ArmorType.BOOTS to scaleEnergyProperties(4)
    )

    private fun scaleEnergyProperties(ingotsOnCrafting: Int): EnergyProperties {
        val energyIO = ingotsOnCrafting * modularArmorConfig.glitchArmor.energyIOMultiplier

        return EnergyProperties(
            ingotsOnCrafting * modularArmorConfig.glitchArmor.energyCapacityMultiplier,
            energyIO,
            if(modularArmorConfig.glitchArmor.allowEnergyOutput) energyIO else 0L
        )
    }

    data class EnergyProperties(val capacity: Long, val input: Long, val output: Long)

}
