package dev.nathanpb.dml.simulacrum

import dev.nathanpb.dml.enums.DataModelTier
import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.item.ITEM_GLITCH_SWORD
import dev.nathanpb.dml.itemgroup.ITEM_GROUP_KEY
import dev.nathanpb.dml.screen.handler.registerScreenHandlerForBlockEntity
import dev.nathanpb.dml.simulacrum.block.chamber.BlockEntitySimulationChamber
import dev.nathanpb.dml.simulacrum.block.chamber.BlockSimulationChamber
import dev.nathanpb.dml.simulacrum.item.registerItems
import dev.nathanpb.dml.simulacrum.screen.ScreenHandlerSimulationChamber
import dev.nathanpb.dml.utils.MODULAR_ARMOR_ID
import dev.nathanpb.dml.utils.getItemFromRegistry
import dev.nathanpb.dml.utils.initConfig
import dev.nathanpb.dml.utils.isModLoaded
import dev.nathanpb.dml.utils.registerBlock
import dev.nathanpb.dml.utils.registerBlockItem
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import team.reborn.energy.api.EnergyStorage


val SIMULATION_CHAMBER: Block = registerBlock("simulation_chamber", factory = ::BlockSimulationChamber)

val SIMULATION_CHAMBER_ENTITY: BlockEntityType<BlockEntitySimulationChamber> = Registry.register(
    BuiltInRegistries.BLOCK_ENTITY_TYPE, identifier("simulation_chamber_entity"),
    FabricBlockEntityTypeBuilder.create(::BlockEntitySimulationChamber, SIMULATION_CHAMBER).build()
)

// The client gets the block position when the menu opens
val SCS_HANDLER_TYPE: ExtendedMenuType<ScreenHandlerSimulationChamber, BlockPos> =
    registerScreenHandlerForBlockEntity(identifier("simulation")) { syncId, playerInventory, context ->
        ScreenHandlerSimulationChamber(syncId, playerInventory, context.evaluate { _, pos -> pos }.orElseThrow())
    }

val simulacrumConfig: SimulacrumConfig = initConfig("simulacrum", SimulacrumConfig(), SimulacrumConfig::class.java)

var PRISTINE_CHANCE = hashMapOf(
    DataModelTier.BASIC to simulacrumConfig.simulationChamber.basicTierPristineChance,
    DataModelTier.ADVANCED to simulacrumConfig.simulationChamber.advancedTierPristineChance,
    DataModelTier.SUPERIOR to simulacrumConfig.simulationChamber.superiorTierPristineChance,
    DataModelTier.SELF_AWARE to simulacrumConfig.simulationChamber.selfAwareTierPristineChance
)


@Suppress("unused")
fun init() {
    // Loading the vals above registers the block, its block entity type and the menu type
    registerBlockItem(SIMULATION_CHAMBER)
    SCS_HANDLER_TYPE

    CreativeModeTabEvents.modifyOutputEvent(ITEM_GROUP_KEY).register {
        val energyCube = getItemFromRegistry("energy_cube")
        if(isModLoaded(MODULAR_ARMOR_ID) && energyCube != null) {
            it.insertAfter(energyCube, SIMULATION_CHAMBER)
        } else {
            it.insertAfter(ITEM_GLITCH_SWORD, SIMULATION_CHAMBER)
        }
    }

    registerItems()

    EnergyStorage.SIDED.registerForBlockEntity(
        { blockEntity, _ -> blockEntity.energyStorage },
        SIMULATION_CHAMBER_ENTITY
    )
}
