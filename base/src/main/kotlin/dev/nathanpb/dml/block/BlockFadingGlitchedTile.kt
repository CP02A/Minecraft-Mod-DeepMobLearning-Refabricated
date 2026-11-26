package dev.nathanpb.dml.block

import dev.nathanpb.dml.blockEntity.BlockEntityFadingGlitchedTile
import dev.nathanpb.dml.utils.RenderUtils.Companion.ALT_GLITCH_PARTICLE
import dev.nathanpb.dml.utils.RenderUtils.Companion.GLITCH_PARTICLE
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult

/** Built from a copy of stone's properties (see Blocks.kt). Turns back into the block it replaced when used, and makes its neighbours do the same shortly after. */
class BlockFadingGlitchedTile(properties: Properties): BaseEntityBlock(
    properties.mapColor(MapColor.COLOR_CYAN).noLootTable()
) {

    private fun transformBlock(world: Level, pos: BlockPos) {
        val blockState = (world.getBlockEntity(pos) as? BlockEntityFadingGlitchedTile)?.replacedState ?: return
        world.setBlockAndUpdate(pos, blockState)

        if(world.getRandom().nextInt(10) > 6) {
            world.playSound(
                null,
                pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.BLOCKS,
                1F, 1F
            )
        }
    }


    override fun useWithoutItem(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player,
        hit: BlockHitResult
    ): InteractionResult {
        transformBlock(world, pos)
        return InteractionResult.SUCCESS
    }


    override fun tick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        transformBlock(world, pos)

        BlockPos.betweenClosedStream(
            AABB.encapsulatingFullBlocks(
                pos.above().north().west(), // Top Corner
                pos.below().south().east() // Bottom Corner
            )
        )
        .filter { world.getBlockState(it).`is`(this) }
        .forEach {
            world.scheduleTick(it.immutable(), BLOCK_FADING_GLITCHED_TILE, 6)
        }
    }


    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: RandomSource) {
        if(random.nextInt(100) > 0) return // 0.01%

        val x = pos.x.toDouble() + 0.1 + random.nextDouble() * 0.8
        val y = pos.y.toDouble() + 1.2
        val z = pos.z.toDouble() + 0.1 + random.nextDouble() * 0.8

        var particle = GLITCH_PARTICLE
        if(random.nextInt(10) > 7) {
            particle = ALT_GLITCH_PARTICLE
        }

        world.addParticle(particle, x, y, z, 0.0, 0.0, 0.0)
    }


    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return BlockEntityFadingGlitchedTile(pos, state)
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }

}
