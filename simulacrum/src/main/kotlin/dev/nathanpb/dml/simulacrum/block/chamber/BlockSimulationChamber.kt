package dev.nathanpb.dml.simulacrum.block.chamber

import dev.nathanpb.dml.block.horizontalPlacementState
import dev.nathanpb.dml.simulacrum.SIMULATION_CHAMBER_ENTITY
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Containers
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.phys.BlockHitResult

class BlockSimulationChamber(properties: Properties) : BaseEntityBlock(properties.destroyTime(4F).explosionResistance(3000F)) {

    override fun useWithoutItem(state: BlockState, world: Level, pos: BlockPos, player: Player, hit: BlockHitResult): InteractionResult {
        if(world.isClientSide()) return InteractionResult.SUCCESS
        state.getMenuProvider(world, pos)?.let {
            player.openMenu(it)
        }
        return InteractionResult.SUCCESS
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return BlockEntitySimulationChamber(pos, state)
    }

    override fun getMenuProvider(state: BlockState, world: Level, pos: BlockPos): MenuProvider? {
        val blockEntity = world.getBlockEntity(pos)
        return if(blockEntity is MenuProvider) blockEntity else null
    }

    override fun rotate(state: BlockState, rotation: Rotation): BlockState {
        return state.setValue(facing, rotation.rotate(state.getValue(facing)))
    }

    override fun mirror(state: BlockState, mirror: Mirror): BlockState {
        return state.rotate(mirror.getRotation(state.getValue(facing)))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState = horizontalPlacementState(this, ctx)

    // The inventory is dropped by the block entity (BlockEntity#preRemoveSideEffects, since 1.21.5)
    override fun affectNeighborsAfterRemoval(state: BlockState, world: ServerLevel, pos: BlockPos, movedByPiston: Boolean) {
        Containers.updateNeighboursAfterDestroy(state, world, pos)
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun <T : BlockEntity> getTicker(world: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
        return createTickerHelper(type, SIMULATION_CHAMBER_ENTITY, BlockEntitySimulationChamber::tick)
    }

    private val facing: EnumProperty<Direction> = BlockStateProperties.HORIZONTAL_FACING
}
