package dev.nathanpb.dml.data

import dev.nathanpb.dml.data.tag.InventorySerializer
import dev.nathanpb.dml.data.tag.MutableCompoundData
import dev.nathanpb.dml.data.tag.stackData
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.core.NonNullList

class DeepLearnerData private constructor(val stack: ItemStack, data: Pair<CompoundTag, (CompoundTag) -> Unit>) :
    MutableCompoundData(data.first, data.second) {

    constructor(stack: ItemStack) : this(stack, stackData(stack))


    var inventory by persistentDefaulted(
        NonNullList.withSize(4, ItemStack.EMPTY),
        InventorySerializer(4)
    )

}
