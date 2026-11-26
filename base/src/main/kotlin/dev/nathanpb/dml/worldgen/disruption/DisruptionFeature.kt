package dev.nathanpb.dml.worldgen.disruption

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.nathanpb.dml.block.BLOCK_DISRUPTIONS_CORE
import dev.nathanpb.dml.block.BLOCK_FADING_GLITCHED_TILE
import dev.nathanpb.dml.block.BlockDisruptionsCore
import dev.nathanpb.dml.blockEntity.BlockEntityFadingGlitchedTile
import dev.nathanpb.dml.identifier
import net.minecraft.core.BlockPos
import net.minecraft.core.BlockPos.MutableBlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.util.RandomSource
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.util.valueproviders.IntProviders
import net.minecraft.world.RandomizableContainer
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.feature.Feature

/**
 * A circle of fading glitched tiles (that remember the blocks they replaced) with a Disruptions Core in the middle.
 * Since 26.1 features carry their configuration: [radius] comes from the feature file.
 */
class DisruptionFeature(val radius: IntProvider): Feature {

    companion object {
        val CODEC: MapCodec<DisruptionFeature> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                IntProviders.codec(0, 8).fieldOf("radius").forGetter(DisruptionFeature::radius),
            ).apply(instance, ::DisruptionFeature)
        }

        private val LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, identifier("chests/disruption"))
    }

    override fun codec() = CODEC

    override fun place(world: WorldGenLevel, chunkGenerator: ChunkGenerator, random: RandomSource, origin0: BlockPos): Boolean {
        val origin = origin0.above()

        var hasGeneratedAny = false
        val radius = radius.sample(random)

        val cx: Int = origin.x
        val cy: Int = origin.y - 1
        val cz: Int = origin.z

        for(blockPos in BlockPos.betweenClosed(
            BlockPos(cx - radius, cy, cz - radius),
            BlockPos(cx + radius, cy, cz + radius)
        )) {
            if(blockPos.distToLowCornerSqr(cx.toDouble(), blockPos.y.toDouble(), cz.toDouble()) <= (radius * radius + 1).toDouble()) {
                var pos = MutableBlockPos(blockPos.x, cy, blockPos.z)
                var offset: Direction? = null
                var canGenerate = true

                if(world.getBlockEntity(pos) != null) { // Abort if block entity
                    canGenerate = false
                } else if(world.getBlockState(pos).canBeReplaced()) { // Offset downward
                    if(testPos(pos, Direction.DOWN, world)) {
                        offset = Direction.DOWN
                    } else if(!testPos(pos, Direction.UP, world)) { // Abort if block would be floating
                        canGenerate = false
                    }
                } else if(!world.getBlockState(pos.above()).canBeReplaced()) { // Offset upward
                    if(testPos(pos, Direction.UP, world)) {
                        offset = Direction.UP
                    } else {
                        canGenerate = false
                    }
                }

                if(canGenerate) {
                    if(pos != origin) { // block below core can never be offset
                        pos = verticalOffset(pos, offset)
                    }

                    val blockState = world.getBlockState(pos)
                    world.setBlock(pos, BLOCK_FADING_GLITCHED_TILE.defaultBlockState(), Block.UPDATE_CLIENTS)
                    (world.getBlockEntity(pos) as? BlockEntityFadingGlitchedTile)?.replacedState = blockState
                    hasGeneratedAny = true
                }
            }
        }

        if(hasGeneratedAny) {
            world.setBlock(origin, BLOCK_DISRUPTIONS_CORE.defaultBlockState().setValue(BlockDisruptionsCore.FADING, true), Block.UPDATE_CLIENTS)
            RandomizableContainer.setBlockEntityLootTable(world, random, origin, LOOT_TABLE)
        }

        return hasGeneratedAny
    }

    private fun testPos(pos: BlockPos, direction: Direction, world: WorldGenLevel): Boolean {
        val offsetPos = pos.relative(direction)
        val isReplaceable = world.getBlockState(offsetPos).canBeReplaced()
        val isAboveReplaceable = world.getBlockState(offsetPos.above()).canBeReplaced()

        return !isReplaceable && isAboveReplaceable
    }

    private fun verticalOffset(pos: MutableBlockPos, direction: Direction?): MutableBlockPos {
        return when(direction) {
            Direction.DOWN -> pos.move(Direction.DOWN)
            Direction.UP -> pos.move(Direction.UP)
            else -> pos
        }
    }

}
