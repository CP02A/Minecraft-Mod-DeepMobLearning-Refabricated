package dev.nathanpb.dml.block

import dev.nathanpb.dml.blockEntity.BLOCKENTITY_DISRUPTIONS_CORE
import dev.nathanpb.dml.blockEntity.BlockEntityDisruptionsCore
import dev.nathanpb.dml.screen.handler.DisruptionsCoreScreenHandler
import dev.nathanpb.dml.screen.handler.DisruptionsCoreScreenHandlerFactory
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.Container
import net.minecraft.world.Containers
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.monster.Shulker
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

/** Built from a copy of the shulker box's properties (see Blocks.kt). */
class BlockDisruptionsCore(properties: Properties): BaseEntityBlock(
    properties.lightLevel { 7 }.noLootTable()
) {

    init {
        registerDefaultState(stateDefinition.any().setValue(FADING, false))
    }

    companion object {
        val FADING: BooleanProperty = BooleanProperty.create("fading")
    }

    private val shape = box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0)

    override fun useWithoutItem(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player,
        hit: BlockHitResult
    ): InteractionResult {
        if(world !is ServerLevel) return InteractionResult.SUCCESS
        if(player.isSpectator) return InteractionResult.CONSUME

        val blockEntity = world.getBlockEntity(pos)
        if(blockEntity is BlockEntityDisruptionsCore) {
            if(canOpen(world, pos, blockEntity)) {
                player.openMenu(DisruptionsCoreScreenHandlerFactory(pos) { syncId, inventory, context ->
                    DisruptionsCoreScreenHandler(syncId, inventory, context)
                })


                if(state.getValue(FADING)) {
                    world.scheduleTick(pos, BLOCK_DISRUPTIONS_CORE, 4*20)

                    val belowPos = pos.below()
                    if(world.getBlockState(belowPos).`is`(BLOCK_FADING_GLITCHED_TILE)) {
                        world.scheduleTick(belowPos, BLOCK_FADING_GLITCHED_TILE, 4*20 + 6)
                    }
                }
            }
            return InteractionResult.CONSUME
        }
        return InteractionResult.PASS
    }

    override fun tick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        world.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), UPDATE_ALL)
        world.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(state))
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return BlockEntityDisruptionsCore(pos, state)
    }

    override fun <T : BlockEntity> getTicker(
        world: Level,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return createTickerHelper(type, BLOCKENTITY_DISRUPTIONS_CORE, BlockEntityDisruptionsCore::tick)
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.INVISIBLE
    }

    private fun canOpen(world: Level, pos: BlockPos, entity: BlockEntityDisruptionsCore): Boolean {
        if(entity.animationStage != BlockEntityDisruptionsCore.AnimationStage.CLOSED) return true
        val box = Shulker.getProgressAabb(1.0F, Direction.UP, 0.5f, Vec3.atBottomCenterOf(pos)).deflate(1.0E-6)
        return world.noCollision(box)
    }

    override fun affectNeighborsAfterRemoval(state: BlockState, world: ServerLevel, pos: BlockPos, movedByPiston: Boolean) {
        Containers.updateNeighboursAfterDestroy(state, world, pos)
    }

    override fun getBlockSupportShape(state: BlockState, world: BlockGetter, pos: BlockPos): VoxelShape {
        val blockEntity = world.getBlockEntity(pos)
        return if(blockEntity is BlockEntityDisruptionsCore && !blockEntity.suffocates()) {
            shape
        } else Shapes.block()
    }

    override fun getShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        val blockEntity = world.getBlockEntity(pos)
        return if(blockEntity is BlockEntityDisruptionsCore) {
            Shapes.create(blockEntity.getBoundingBox())
        } else Shapes.block()
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FADING)
    }

    override fun hasAnalogOutputSignal(state: BlockState): Boolean {
        return true
    }

    override fun getAnalogOutputSignal(state: BlockState, world: Level, pos: BlockPos, direction: Direction): Int {
        return AbstractContainerMenu.getRedstoneSignalFromContainer(world.getBlockEntity(pos) as? Container)
    }

}
