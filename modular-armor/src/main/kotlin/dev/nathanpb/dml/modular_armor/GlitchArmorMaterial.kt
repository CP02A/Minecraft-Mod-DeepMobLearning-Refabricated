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

import dev.nathanpb.dml.enums.DataModelTier
import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.utils.lerp
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.equipment.ArmorMaterial
import net.minecraft.world.item.equipment.ArmorType
import net.minecraft.world.item.equipment.EquipmentAssets
import kotlin.math.floor

/**
 * The armor material is a record since 1.21.2. Its defense, toughness and knockback resistance are 0: the real values
 * depend on the tier of the data model in each armor piece (see [ItemModularGlitchArmor.updateAttributeModifiers]).
 */
object GlitchArmorMaterial {

    /** Glitch ingots (data/dml-refabricated/tags/item/repairs_glitch_armor.json) */
    val REPAIR_TAG = TagKey.create(Registries.ITEM, identifier("repairs_glitch_armor"))

    /** assets/dml-refabricated/equipment/glitch.json */
    val ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, identifier("glitch"))

    val MATERIAL = ArmorMaterial(
        1,
        ArmorType.entries.associateWith { 0 },
        16,
        SoundEvents.ARMOR_EQUIP_GENERIC,
        0F,
        0F,
        REPAIR_TAG,
        ASSET
    )

    fun getKnockbackResistance(tier: DataModelTier): Float {
        return (tier.ordinal.inc() / DataModelTier.values().size.toDouble()).lerp(1.0, 3.5).toFloat() / 10
    }

    fun getProtectionAmount(slot: EquipmentSlot, tier: DataModelTier): Int {
        val multiplier = when (slot) {
            EquipmentSlot.HEAD -> 0.9
            EquipmentSlot.CHEST -> 1.8
            EquipmentSlot.LEGS -> 1.4
            EquipmentSlot.FEET -> 0.8
            else -> 0.0
        }
        val result = (tier.ordinal / DataModelTier.values().size.toDouble()).lerp(4.0, 5.5)
        return floor(result * multiplier).toInt()
    }

    fun getToughness(tier: DataModelTier): Float {
        return tier.ordinal + 2F
    }

}
