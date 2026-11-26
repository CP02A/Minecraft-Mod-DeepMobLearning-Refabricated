package dev.nathanpb.dml.simulacrum.util

import net.minecraft.core.NonNullList
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

interface ImplementedInventory: Container {

    /**
     * Retrieves the item list of this inventory.
     * Must return the same instance every time it's called.
     */
    fun getItems(): NonNullList<ItemStack>

    /**
     * Returns the inventory size.
     */
    override fun getContainerSize(): Int {
        return getItems().size
    }

    /**
     * Checks if the inventory is empty.
     * @return true if this inventory has only empty stacks, false otherwise.
     */
    override fun isEmpty(): Boolean {
        return getItems().all { it.isEmpty }
    }

    /**
     * Retrieves the item in the slot.
     */
    override fun getItem(slot: Int): ItemStack {
        return getItems()[slot]
    }

    /**
     * Removes items from an inventory slot.
     * @param slot  The slot to remove from.
     * @param count How many items to remove. If there are fewer items in the slot than what are requested,
     * takes all items in that slot.
     */
    override fun removeItem(slot: Int, count: Int): ItemStack {
        val result = ContainerHelper.removeItem(getItems(), slot, count)
        if (!result.isEmpty) {
            setChanged()
        }
        return result
    }

    /**
     * Removes all items from an inventory slot.
     * @param slot The slot to remove from.
     */
    override fun removeItemNoUpdate(slot: Int): ItemStack {
        return ContainerHelper.takeItem(getItems(), slot)
    }

    /**
     * Replaces the current stack in an inventory slot with the provided stack.
     * If the stack is too big for this inventory ([Container.getMaxStackSize]), it gets resized.
     */
    override fun setItem(slot: Int, stack: ItemStack) {
        getItems()[slot] = stack
        if (stack.count > maxStackSize) {
            stack.count = maxStackSize
        }
    }

    /**
     * Clears the inventory.
     */
    override fun clearContent() {
        getItems().clear()
    }

    /**
     * Marks the state as dirty.
     * Must be called after changes in the inventory, so that the game can properly save
     * the inventory contents and notify neighboring blocks of inventory changes.
     */
    override fun setChanged() {
        // Override if you want behavior.
    }

    /**
     * @return true if the player can use the inventory, false otherwise.
     */
    override fun stillValid(player: Player): Boolean {
        return true
    }
}
