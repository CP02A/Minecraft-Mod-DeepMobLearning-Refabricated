package dev.nathanpb.dml.block

import dev.nathanpb.dml.blockEntity.BlockEntityDataSynthesizer
import dev.nathanpb.dml.screen.handler.DataSynthesizerScreenHandler
import dev.nathanpb.dml.screen.handler.DataSynthesizerScreenHandlerFactory
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Containers
import net.minecraft.world.InteractionResult
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.WorldlyContainerHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.phys.BlockHitResult

class BlockDataSynthesizer(properties: Properties) : HorizontalDirectionalBlock(
    properties.strength(5F, 6F)
), WorldlyContainerHolder, EntityBlock {

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : BlockEntity> getTicker(
        world: Level,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return if (world.isClientSide) null else BlockEntityDataSynthesizer.ticker as BlockEntityTicker<T>
    }

    override fun useWithoutItem(state: BlockState, world: Level, pos: BlockPos, player: Player, hit: BlockHitResult): InteractionResult {
        if (!world.isClientSide) {
            player.openMenu(DataSynthesizerScreenHandlerFactory(pos) { syncId, inventory, context ->
                DataSynthesizerScreenHandler(syncId, inventory, context)
            })
        }
        return InteractionResult.SUCCESS
    }

    override fun affectNeighborsAfterRemoval(state: BlockState, world: ServerLevel, pos: BlockPos, movedByPiston: Boolean) {
        Containers.updateNeighboursAfterDestroy(state, world, pos)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState = horizontalPlacementState(this, ctx)

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = BlockEntityDataSynthesizer(pos, state)

    override fun getContainer(state: BlockState, world: LevelAccessor, pos: BlockPos): WorldlyContainer {
        return (world.getBlockEntity(pos) as BlockEntityDataSynthesizer).inventory
    }

    override fun hasAnalogOutputSignal(state: BlockState) = true

    override fun getAnalogOutputSignal(state: BlockState, world: Level, pos: BlockPos, direction: Direction): Int {
        return AbstractContainerMenu.getRedstoneSignalFromContainer((world.getBlockEntity(pos) as? BlockEntityDataSynthesizer)?.inventory)
    }
}
