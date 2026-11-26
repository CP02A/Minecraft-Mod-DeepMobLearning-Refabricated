package dev.nathanpb.dml.blockEntity.renderer

import com.mojang.blaze3d.vertex.PoseStack
import dev.nathanpb.dml.blockEntity.BlockEntityDisruptionsCore
import dev.nathanpb.dml.identifier
import net.minecraft.client.model.Model
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
import net.minecraft.client.renderer.feature.ModelFeatureRenderer
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.phys.Vec3

/** Renders the core like an upward shulker box, whose lid turns less (225Â° instead of 270Â°). */
class BlockEntityRendererDisruptionsCore(
    ctx: BlockEntityRendererProvider.Context
): BlockEntityRenderer<BlockEntityDisruptionsCore, BlockEntityRendererDisruptionsCore.State> {

    class State : BlockEntityRenderState() {
        var progress = 0F
    }

    private class CoreModel(root: ModelPart) : Model<Float>(root, RenderTypes::entityCutout) {
        private val lid: ModelPart = root.getChild("lid")

        override fun setupAnim(progress: Float) {
            super.setupAnim(progress)
            lid.setPos(0F, 24F - progress * 0.5F * 16F, 0F)
            lid.yRot = 225F * progress * 0.017453292F
        }
    }

    private val model = CoreModel(ctx.bakeLayer(ModelLayers.SHULKER_BOX))
    private val renderType = RenderTypes.entityCutout(identifier("textures/block/disruptions_core.png"))

    override fun createRenderState() = State()

    override fun extractRenderState(
        blockEntity: BlockEntityDisruptionsCore,
        state: State,
        partialTicks: Float,
        cameraPosition: Vec3,
        breakProgress: ModelFeatureRenderer.CrumblingOverlay?
    ) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress)
        state.progress = blockEntity.getAnimationProgress(partialTicks)
    }

    override fun submit(state: State, matrixStack: PoseStack, collector: SubmitNodeCollector, camera: CameraRenderState) {
        matrixStack.pushPose()
        matrixStack.translate(0.5F, 0.5F, 0.5F)
        matrixStack.scale(0.9995F, 0.9995F, 0.9995F)
        matrixStack.scale(1F, -1F, -1F)
        matrixStack.translate(0F, -1F, 0F)

        model.setupAnim(state.progress)
        collector.submitModel(model, state.progress, matrixStack, renderType, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, 0)
        matrixStack.popPose()
    }

}
