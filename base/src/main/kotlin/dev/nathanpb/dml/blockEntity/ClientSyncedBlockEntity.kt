package dev.nathanpb.dml.blockEntity

import com.mojang.serialization.Codec
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

/* A special implementation of BlockEntity, syncing nbt data to client, as a fix to old BEs using the now removed BlockEntityClientSerializable.
*
* This was made by Technici4n, so props there :)
*/
abstract class ClientSyncedBlockEntity(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState
) : BlockEntity(type, pos, state) {

    @JvmOverloads
    fun sync(shouldRemesh: Boolean = true) {
        val world = checkNotNull(level) // Maintain distinct failure case from below
        check(world is ServerLevel) { "Cannot call sync() on the logical client! Did you check world.isClient first?" }
        shouldClientRemesh = shouldRemesh or shouldClientRemesh
        world.chunkSource.blockChanged(blockPos)
    }

    abstract fun toTag(output: ValueOutput)
    abstract fun fromTag(input: ValueInput)

    abstract fun toClientTag(output: ValueOutput)
    abstract fun fromClientTag(input: ValueInput)

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag {
        clientTag = true
        try {
            val nbt = saveCustomOnly(registries)
            nbt.putBoolean(CLIENT_TAG_KEY, shouldClientRemesh) // mark client tag
            shouldClientRemesh = false
            return nbt
        } finally {
            clientTag = false
        }
    }

    override fun saveAdditional(output: ValueOutput) {
        if(clientTag) toClientTag(output) else toTag(output)
    }

    override fun loadAdditional(input: ValueInput) {
        val remesh = input.read(CLIENT_TAG_KEY, Codec.BOOL)
        if(remesh.isPresent) {
            fromClientTag(input)
            if(remesh.get()) {
                remesh()
            }
        } else {
            fromTag(input)
        }
    }

    fun remesh() {
        val world = checkNotNull(level)
        check(world is ClientLevel) { "Cannot call remesh() on the server!" }
        world.sendBlockUpdated(blockPos, blockState, blockState, 0)
    }

    protected val isClientSide: Boolean
        get() {
            checkNotNull(level) { "Cannot determine if the BE is client-side if it has no world yet" }
            return level!!.isClientSide()
        }

    private var shouldClientRemesh = true

    /** True while [getUpdateTag] writes the tag for the client. */
    private var clientTag = false

    companion object {
        private const val CLIENT_TAG_KEY = "#c"
    }

}
