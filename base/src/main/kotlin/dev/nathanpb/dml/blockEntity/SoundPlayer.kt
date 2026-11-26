package dev.nathanpb.dml.blockEntity

import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvent
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

class SoundPlayer(
    private val soundEvent: SoundEvent,
    private val canPlay: Boolean
) {

    fun playSound(world: Level, pos: BlockPos) {
        if(!canPlay) return
        world.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1F, 1F)
    }

}