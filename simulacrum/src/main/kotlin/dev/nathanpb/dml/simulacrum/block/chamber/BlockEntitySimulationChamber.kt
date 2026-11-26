package dev.nathanpb.dml.simulacrum.block.chamber

import dev.nathanpb.dml.data.dataModel
import dev.nathanpb.dml.enums.DataModelTier
import dev.nathanpb.dml.enums.EntityCategory
import dev.nathanpb.dml.item.ITEM_POLYMER_CLAY
import dev.nathanpb.dml.item.ItemDataModel
import dev.nathanpb.dml.item.ItemPristineMatter
import dev.nathanpb.dml.simulacrum.PRISTINE_CHANCE
import dev.nathanpb.dml.simulacrum.SIMULATION_CHAMBER_ENTITY
import dev.nathanpb.dml.simulacrum.item.ItemMatter
import dev.nathanpb.dml.simulacrum.screen.ScreenHandlerSimulationChamber
import dev.nathanpb.dml.simulacrum.util.DataModelUtil
import dev.nathanpb.dml.simulacrum.util.DataModelUtil.Companion.dataModel2MatterMap
import dev.nathanpb.dml.simulacrum.util.ImplementedInventory
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.ContainerHelper
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import team.reborn.energy.api.base.SimpleEnergyStorage
import java.util.*


/** As a Container, its items are dropped by BlockEntity#preRemoveSideEffects when the block is removed. */
class BlockEntitySimulationChamber(pos: BlockPos, state: BlockState) : BlockEntity(SIMULATION_CHAMBER_ENTITY, pos, state), ImplementedInventory, ExtendedMenuProvider<BlockPos>, WorldlyContainer {

    private val inventory = NonNullList.withSize(4, ItemStack.EMPTY)
    var ticks = 0
    var percentDone = 0
    var isCrafting = false
        private set
    var byproductSuccess = false
    private var currentDataModelType: EntityCategory? = null
    var energyStorage = SimpleEnergyStorage(2000000, 25600, 0)

    var propertyDelegate: ContainerData = object : ContainerData {
        override fun get(index: Int): Int {
            return energyStorage.getAmount().toInt()
        }

        override fun set(index: Int, value: Int) {
            energyStorage.amount = value.toLong()
        }

        override fun getCount(): Int {
            return 1
        }
    }

    fun updateState() {
        val world = level ?: return
        val state = world.getBlockState(blockPos)
        world.sendBlockUpdated(blockPos, state, state, 3)
    }

    fun dataModelTypeChanged(): Boolean {
        return currentDataModelType != dataModelStack.dataModel.category
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        energyStorage.amount = input.getLongOr("energy", 0L)
        byproductSuccess = input.getBooleanOr("byproductSuccess", false)
        isCrafting = input.getBooleanOr("isCrafting", false)
        percentDone = input.getIntOr("percentDone", 0)

        val currentDataModelType = input.getStringOr("currentDataModelType", "")
        if(currentDataModelType.isNotEmpty()) {
            this.currentDataModelType = enumValueOf<EntityCategory>(currentDataModelType)
        }
        inventory.fill(ItemStack.EMPTY)
        ContainerHelper.loadAllItems(input, inventory)
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        output.putLong("energy", energyStorage.amount)
        output.putBoolean("byproductSuccess", byproductSuccess)
        output.putBoolean("isCrafting", isCrafting)
        output.putInt("percentDone", percentDone)
        currentDataModelType?.let {
            output.putString("currentDataModelType", it.toString().uppercase())
        }
        ContainerHelper.saveAllItems(output, inventory)
    }

    override fun setChanged() {
        super<BlockEntity>.setChanged()
        val world = level
        if (world is ServerLevel) {
            world.chunkSource.blockChanged(blockPos)
        }
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag {
        return saveWithoutMetadata(registries)
    }

    private fun startSimulation() {
        isCrafting = true
        currentDataModelType = dataModelStack.dataModel.category
        inventory[1].count = polymerClay.count - 1
    }

    private fun finishSimulation(abort: Boolean) {
        percentDone = 0
        isCrafting = false
        // Only decrease input and increase output if not aborted, and only if on the server's BE
        if (!abort && level?.isClientSide == false) {
            energyStorage.amount -= DataModelUtil.getEnergyCost(dataModelStack)
            dataModelStack.dataModel.simulationCount++
            DataModelUtil.updateDataModel(dataModelStack)
            if (inventory[2].item is ItemMatter) {
                inventory[2].count = living.count + 1
            } else {
                inventory[2] = dataModel2MatterMap[currentDataModelType]?.type?.matter?.let { ItemStack(it, 1) } ?: ItemStack.EMPTY
            }
            if (byproductSuccess) {
                byproductSuccess = false
                if (inventory[3].item is ItemPristineMatter) {
                    inventory[3].grow(1)
                } else {
                    inventory[3] = dataModel2MatterMap[currentDataModelType]?.pristine?.let { ItemStack(it, 1) } ?: ItemStack.EMPTY
                }
            }
            updateState()
        }
    }

    fun canStartSimulation(): Boolean {
        return hasEnergyForSimulation() && canContinueSimulation() && !outputIsFull() && !pristineIsFull() && hasPolymerClay()
    }

    fun canContinueSimulation(): Boolean {
        return hasDataModel() && dataModelStack.dataModel.tier() != DataModelTier.FAULTY
    }

    fun hasEnergyForSimulation(): Boolean {
        if(!hasDataModel()) return false
        return energyStorage.amount > DataModelUtil.getEnergyCost(dataModelStack)
    }



    val dataModelStack: ItemStack
        get() = getItem(0)
    private val polymerClay: ItemStack
        get() = getItem(1)
    private val living: ItemStack
        get() = getItem(2)
    private val pristine: ItemStack
        get() = getItem(3)

    fun hasDataModel(): Boolean {
        return dataModelStack.item is ItemDataModel
    }

    fun hasPolymerClay(): Boolean {
        val stack = polymerClay
        return stack.`is`(ITEM_POLYMER_CLAY) && stack.count > 0
    }

    fun outputIsFull(): Boolean {
        val stack = living
        if (stack.isEmpty) {
            return false
        }
        val stackLimitReached = stack.count == living.maxStackSize
        val outputMatches = dataModelMatchesOutput(
            dataModelStack,
            living
        )
        return stackLimitReached || !outputMatches
    }

    fun pristineIsFull(): Boolean {
        val stack = pristine
        if (stack.isEmpty) {
            return false
        }
        val stackLimitReached = stack.count == inventory[3].maxStackSize
        val outputMatches = dataModelMatchesPristine(
            dataModelStack,
            pristine
        )
        return stackLimitReached || !outputMatches
    }

    override fun getDisplayName(): Component {
        return Component.translatable("block.dml-refabricated.simulation_chamber")
    }

    override fun createMenu(syncId: Int, inv: Inventory, player: Player): AbstractContainerMenu {
        return ScreenHandlerSimulationChamber(syncId, inv, blockPos)
    }

    override fun getItems(): NonNullList<ItemStack> {
        return inventory
    }

    override fun getScreenOpeningData(player: ServerPlayer): BlockPos = blockPos

    override fun stillValid(player: Player): Boolean = net.minecraft.world.Container.stillValidBlockEntity(this, player)

    override fun getSlotsForFace(side: Direction): IntArray {
        return if (side == Direction.UP) {
            intArrayOf(0, 1)
        } else intArrayOf(2, 3)
    }

    override fun canPlaceItemThroughFace(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
        return if (dir == Direction.UP) {
            when (slot) {
                0 -> stack.item is ItemDataModel && stack.dataModel.category != null
                1 -> stack.`is`(ITEM_POLYMER_CLAY)
                else -> false
            }
        } else false
    }

    override fun canTakeItemThroughFace(slot: Int, stack: ItemStack, dir: Direction): Boolean {
        return if (dir != Direction.UP) {
            when (slot) {
                2, 3 -> true
                else -> false
            }
        } else false
    }

    companion object {
        private fun dataModelMatchesOutput(stack: ItemStack, output: ItemStack): Boolean {
            val livingMatter: Item? = stack.dataModel.category?.matterType?.matter
            return livingMatter == output.item
        }

        private fun dataModelMatchesPristine(stack: ItemStack, pristine: ItemStack): Boolean {
            val pristineMatter: ItemPristineMatter? = dataModel2MatterMap[stack.dataModel.category]?.pristine
            return pristineMatter == pristine.item
        }

        fun tick(world: Level, pos: BlockPos, state: BlockState, blockEntity: BlockEntitySimulationChamber) {
            blockEntity.ticks++
            if (!world.isClientSide) {
                if (!blockEntity.isCrafting) {
                    if (blockEntity.canStartSimulation()) {
                        blockEntity.startSimulation()
                    }
                } else {
                    if (!blockEntity.canContinueSimulation() || blockEntity.dataModelTypeChanged()) {
                        blockEntity.finishSimulation(true)
                        return
                    }
                    if (blockEntity.percentDone == 0) {
                        val rand = Random()
                        val num = rand.nextInt(100)
                        val chance: Int? = PRISTINE_CHANCE[blockEntity.dataModelStack.dataModel.tier()]
                        blockEntity.byproductSuccess = num <= ((chance)?.coerceAtLeast(1)?.coerceAtMost(100) ?: 0)
                    }
                    if (blockEntity.ticks % (20 * 15 / 100) == 0) {
                        blockEntity.percentDone++
                    }

                    // Notify while crafting every other second, this is done more frequently when the container is open
                    if (blockEntity.ticks % (20 * 2) == 0) {
                        blockEntity.updateState()
                    }
                }
                if (blockEntity.percentDone == 100) {
                    blockEntity.finishSimulation(false)
                    return
                }
                blockEntity.setChanged()
            }
        }
    }
}
