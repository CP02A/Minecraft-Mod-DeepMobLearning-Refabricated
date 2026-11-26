package dev.nathanpb.dml.modular_armor

import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.item.battery.AbstractItemBattery
import dev.nathanpb.dml.itemgroup.ITEM_GROUP_KEY
import dev.nathanpb.dml.utils.ITEM_PRISTINE
import dev.nathanpb.dml.utils.RenderUtils.Companion.ALT_STYLE
import dev.nathanpb.dml.utils.RenderUtils.Companion.STYLE
import dev.nathanpb.dml.utils.getEmptyAndFullCapacityEnergyItem
import dev.nathanpb.dml.utils.getEnergyStorage
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents
import dev.nathanpb.dml.utils.registerItem
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry
import net.minecraft.network.chat.Style
import team.reborn.energy.api.EnergyStorage
import java.awt.Color

class ItemPristineEnergyCube(properties: Properties) : AbstractItemBattery(properties) {

    companion object {
        lateinit var ITEM_PRISTINE_ENERGY_CUBE: ItemPristineEnergyCube

        fun register() {
            ITEM_PRISTINE_ENERGY_CUBE = registerItem("pristine_energy_cube", ::ItemPristineEnergyCube)

            ITEM_PRISTINE.registerForItems(::getEnergyStorage, ITEM_PRISTINE_ENERGY_CUBE)
            EnergyStorage.ITEM.registerForItems({ _, _ ->
                return@registerForItems EnergyStorage.EMPTY
            }, ITEM_PRISTINE_ENERGY_CUBE)
            CreativeModeTabEvents.modifyOutputEvent(ITEM_GROUP_KEY).register {
                it.insertAfter(BlockMatterCondenser.BLOCK_MATTER_CONDENSER, getEmptyAndFullCapacityEnergyItem(ITEM_PRISTINE_ENERGY_CUBE))
            }

        }
    }

    override fun getEnergyCapacity(stack: ItemStack) = modularArmorConfig.misc.pristineEnergyCube.energyCapacity

    override fun getEnergyMaxInput(stack: ItemStack) = modularArmorConfig.misc.pristineEnergyCube.energyIO

    override fun getEnergyMaxOutput(stack: ItemStack) = modularArmorConfig.misc.pristineEnergyCube.energyIO

    override fun isPristineEnergy(): Boolean = true

    override fun getColor(): Color = Color.decode("#04FCC4")

    override val primaryStyle: Style = STYLE
    override val secondaryStyle: Style = ALT_STYLE

}