package dev.nathanpb.dml

import dev.nathanpb.dml.blockEntity.BLOCKENTITY_DATA_SYNTHESIZER
import dev.nathanpb.dml.blockEntity.BLOCKENTITY_DISRUPTIONS_CORE
import dev.nathanpb.dml.blockEntity.renderer.BlockEntityRendererDataSynthesizer
import dev.nathanpb.dml.blockEntity.renderer.BlockEntityRendererDisruptionsCore
import dev.nathanpb.dml.client.registerItemTints
import dev.nathanpb.dml.entity.registerEntityRenderer
import dev.nathanpb.dml.screen.registerScreens
import dev.nathanpb.dml.utils.RegistryAccessHolder
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers

@Suppress("unused")
fun initClient() {
    RegistryAccessHolder.clientLookup = { Minecraft.getInstance().level?.registryAccess() }

    registerScreens()
    registerEntityRenderer()

    BlockEntityRenderers.register(
        BLOCKENTITY_DATA_SYNTHESIZER,
        ::BlockEntityRendererDataSynthesizer
    )
    BlockEntityRenderers.register(
        BLOCKENTITY_DISRUPTIONS_CORE,
        ::BlockEntityRendererDisruptionsCore
    )

    // Item colors moved into the item model definitions (assets/*/items): the tint sources used there
    registerItemTints()
}
