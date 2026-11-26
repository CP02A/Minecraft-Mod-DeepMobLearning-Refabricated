package dev.nathanpb.dml.simulacrum.screen

import dev.nathanpb.dml.simulacrum.SCS_HANDLER_TYPE
import dev.nathanpb.dml.simulacrum.block.chamber.BlockEntitySimulationChamber
import net.minecraft.core.BlockPos
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level


/** Opened with the position of the chamber (see [BlockEntitySimulationChamber.getScreenOpeningData]). */
class ScreenHandlerSimulationChamber(syncId: Int, playerInventory: Inventory, var blockPos: BlockPos) :
    AbstractContainerMenu(SCS_HANDLER_TYPE, syncId) {

    private val inventory: Container
    private val player: Player = playerInventory.player
    private val blockEntity: BlockEntitySimulationChamber
    private val world: Level = player.level()
    var propertyDelegate: ContainerData

    init {
        blockEntity = world.getBlockEntity(blockPos) as BlockEntitySimulationChamber
        inventory = blockEntity
        propertyDelegate = blockEntity.propertyDelegate
        checkContainerSize(inventory, 4)
        // The client reads the energy of its copy of the block entity (synced through the update tag)
        if (world.isClientSide) addDataSlots(propertyDelegate)
        addSlots()
        addInventorySlots()
    }

    val syncedEnergy: Int
        get() = propertyDelegate[0]

    override fun stillValid(player: Player): Boolean {
        return inventory.stillValid(player)
    }

    private fun addSlots() {
        addSlot(SlotSimulationChamber(inventory, 0, 9, 146))
        addSlot(SlotSimulationChamber(inventory, 1, 176, 17))
        addSlot(SlotSimulationChamber(inventory, 2, 196, 7))
        addSlot(SlotSimulationChamber(inventory, 3, 196, 27))
    }

    private fun addInventorySlots() {
        // Bind actionbar
        for (row in 0..8) {
            val slot = Slot(player.inventory, row, 36 + row * 18, 211)
            addSlot(slot)
        }

        // 3 Top rows, starting with the bottom one
        for (row in 0..2) {
            for (column in 0..8) {
                val x = 36 + column * 18
                val y = 153 + row * 18
                val index = column + row * 9 + 9
                val slot = Slot(player.inventory, index, x, y)
                addSlot(slot)
            }
        }
    }

    override fun broadcastChanges() {
        super.broadcastChanges()
        if (!world.isClientSide) {
            // Update the BE every tick while container is open
            blockEntity.updateState()
        }
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        var newStack = ItemStack.EMPTY
        if (!world.isClientSide) {
            val slot = slots[index]
            if (slot.hasItem()) {
                val originalStack = slot.item
                newStack = originalStack.copy()
                if (index < inventory.containerSize) {
                    if (!moveItemStackTo(originalStack, inventory.containerSize, slots.size, true)) {
                        return ItemStack.EMPTY
                    }
                } else if (!moveItemStackTo(originalStack, 0, inventory.containerSize, false)) {
                    return ItemStack.EMPTY
                }
                if (originalStack.isEmpty) {
                    slot.setByPlayer(ItemStack.EMPTY)
                } else {
                    slot.setChanged()
                }
            }
        }
        return newStack
    }

    override fun clicked(slotIndex: Int, button: Int, actionType: ContainerInput, player: Player) {
        if (!world.isClientSide) {
            super.clicked(slotIndex, button, actionType, player)
        }
    }

}
