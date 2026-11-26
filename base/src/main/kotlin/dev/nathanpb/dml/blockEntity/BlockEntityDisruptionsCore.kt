package dev.nathanpb.dml.blockEntity

import dev.nathanpb.dml.block.BlockDisruptionsCore
import dev.nathanpb.dml.screen.handler.DisruptionsCoreScreenHandler
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.NonNullList
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.ContainerUser
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.monster.Shulker
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.material.PushReaction
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

/** A shulker-box-like container that opens upwards (the animation is the shulker box's). */
class BlockEntityDisruptionsCore(
    pos: BlockPos,
    state: BlockState
): RandomizableContainerBlockEntity(BLOCKENTITY_DISRUPTIONS_CORE, pos, state) {


    var inventory: NonNullList<ItemStack> = NonNullList.withSize(27, ItemStack.EMPTY)
    var viewerCount = 0
    var animationStage: AnimationStage = AnimationStage.CLOSED
    var animationProgress = 0f
    var prevAnimationProgress = 0f


    companion object {
        fun tick(world: Level, pos: BlockPos, state: BlockState, blockEntity: BlockEntityDisruptionsCore) {
            blockEntity.updateAnimation(world, pos, state)
        }
    }


    fun updateAnimation(world: Level, pos: BlockPos, state: BlockState) {
        prevAnimationProgress = animationProgress
        when(animationStage) {
            AnimationStage.CLOSED -> animationProgress = 0.0f
            AnimationStage.OPENING -> {
                animationProgress += 0.1f
                if(prevAnimationProgress == 0.0f) {
                    updateNeighborStates(world, pos, state)
                }
                if(animationProgress >= 1.0f) {
                    animationStage = AnimationStage.OPENED
                    animationProgress = 1.0f
                    updateNeighborStates(world, pos, state)
                }
                pushEntities(world, pos, state)
            }

            AnimationStage.CLOSING -> {
                animationProgress -= 0.1f
                if(prevAnimationProgress == 1.0f) {
                    updateNeighborStates(world, pos, state)
                }
                if(animationProgress <= 0.0f) {
                    animationStage = AnimationStage.CLOSED
                    animationProgress = 0.0f
                    updateNeighborStates(world, pos, state)
                }
            }

            AnimationStage.OPENED -> animationProgress = 1.0f
        }
    }

    /** The shape relative to the block position. */
    fun getBoundingBox(): AABB {
        return Shulker.getProgressAabb(1.0F, Direction.UP, 0.5f * getAnimationProgress(1.0f), Vec3(0.5, 0.0, 0.5))
    }

    private fun pushEntities(world: Level, pos: BlockPos, state: BlockState) {
        if(state.block !is BlockDisruptionsCore) return
        val box = Shulker.getProgressDeltaAabb(1.0F, Direction.UP, prevAnimationProgress, animationProgress, Vec3.atBottomCenterOf(pos))

        for(entity in world.getEntities(null, box)) {
            if(entity.pistonPushReaction != PushReaction.IGNORE_ENTITY) {
                entity.move(MoverType.SHULKER_BOX, Vec3(0.0, box.ysize + 0.01, 0.0))
            }
        }
    }

    override fun getContainerSize(): Int {
        return inventory.size
    }

    override fun triggerEvent(type: Int, data: Int): Boolean {
        return if(type == 1) {
            viewerCount = data
            if(data == 0) {
                animationStage = AnimationStage.CLOSING
            }
            if(data == 1) {
                animationStage = AnimationStage.OPENING
            }
            true
        } else {
            super.triggerEvent(type, data)
        }
    }

    private fun updateNeighborStates(world: Level, pos: BlockPos, state: BlockState) {
        state.updateNeighbourShapes(world, pos, 3)
        world.updateNeighborsAt(pos, state.block)
    }

    override fun startOpen(user: ContainerUser) {
        val world = level ?: return
        if(!isRemoved && !user.livingEntity.isSpectator) {
            if(viewerCount < 0) {
                viewerCount = 0
            }
            ++viewerCount
            world.blockEvent(blockPos, blockState.block, 1, viewerCount)
            if(viewerCount == 1) {
                world.gameEvent(user.livingEntity, GameEvent.CONTAINER_OPEN, blockPos)
                world.playSound(null, blockPos, SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f)
            }
        }
    }

    override fun stopOpen(user: ContainerUser) {
        val world = level ?: return
        if(!isRemoved && !user.livingEntity.isSpectator) {
            --viewerCount
            world.blockEvent(blockPos, blockState.block, 1, viewerCount)
            if(viewerCount <= 0) {
                world.gameEvent(user.livingEntity, GameEvent.CONTAINER_CLOSE, blockPos)
                world.playSound(null, blockPos, SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f)
            }
        }
    }

    override fun getDefaultName(): Component {
        return Component.empty()
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        inventory = NonNullList.withSize(containerSize, ItemStack.EMPTY)
        if(!tryLoadLootTable(input)) {
            ContainerHelper.loadAllItems(input, inventory)
        }
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        if(!trySaveLootTable(output)) {
            ContainerHelper.saveAllItems(output, inventory, false)
        }
    }

    override fun getItems(): NonNullList<ItemStack> {
        return inventory
    }

    override fun setItems(list: NonNullList<ItemStack>) {
        inventory = list
    }

    fun getAnimationProgress(delta: Float): Float {
        return Mth.lerp(delta, prevAnimationProgress, animationProgress)
    }

    override fun createMenu(syncId: Int, playerInventory: Inventory): AbstractContainerMenu {
        return DisruptionsCoreScreenHandler(syncId, playerInventory, ContainerLevelAccess.create(level!!, blockPos))
    }

    fun suffocates(): Boolean {
        return animationStage == AnimationStage.CLOSED
    }


    enum class AnimationStage {
        CLOSED,
        OPENING,
        OPENED,
        CLOSING
    }

}
