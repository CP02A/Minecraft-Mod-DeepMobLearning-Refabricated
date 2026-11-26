package dev.nathanpb.dml.modular_armor.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Axis
import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.modular_armor.BlockEntityMatterCondenser
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
import net.minecraft.client.renderer.feature.ModelFeatureRenderer
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3

/**
 * Two small cubes spinning inside the condenser, one turn every 30 seconds. They were the block model
 * models/block/matter_condenser_core.json (two 2x2x2 cubes colored by one pixel of the palette texture); now they are
 * drawn directly, as extra block models can't be rendered by block entity renderers the old way since 1.21.
 */
class BlockEntityRendererMatterCondenser(
    ctx: BlockEntityRendererProvider.Context
): BlockEntityRenderer<BlockEntityMatterCondenser, BlockEntityRendererMatterCondenser.State> {

    class State : BlockEntityRenderState() {
        var angle = 0F
    }

    private val renderType = RenderTypes.entityCutout(identifier("textures/block/palette.png"))
    private val cycleDuration = 30F * 20F

    override fun createRenderState() = State()

    override fun extractRenderState(
        blockEntity: BlockEntityMatterCondenser,
        state: State,
        partialTicks: Float,
        cameraPosition: Vec3,
        breakProgress: ModelFeatureRenderer.CrumblingOverlay?
    ) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress)
        val time = (blockEntity.level?.gameTime ?: 0L) % cycleDuration.toLong() + partialTicks
        state.angle = time * 360F / cycleDuration
    }

    override fun submit(state: State, ms: PoseStack, collector: SubmitNodeCollector, camera: CameraRenderState) {
        val light = state.lightCoords
        ms.pushPose()
        ms.translate(0.5, 0.4375, 0.5)
        ms.rotateDegrees(Axis.YN, state.angle)
        collector.submitCustomGeometry(ms, renderType) { pose, buffer ->
            cube(buffer, pose, 2F, 0F, 2F, light)
            cube(buffer, pose, -4F, 0F, -4F, light)
        }
        ms.popPose()
    }

    /** A 2x2x2 pixel cube from (x, y, z), in pixels, with the palette pixel (1, 0) on every face. */
    private fun cube(buffer: VertexConsumer, pose: PoseStack.Pose, x: Float, y: Float, z: Float, light: Int) {
        val x0 = x / 16F; val y0 = y / 16F; val z0 = z / 16F
        val x1 = x0 + 2F / 16F; val y1 = y0 + 2F / 16F; val z1 = z0 + 2F / 16F
        val u = 1.5F / 16F; val v = 0.5F / 16F
        fun quad(d: Direction, vararg corners: Float) {
            for (i in 0 until 4) {
                buffer.addVertex(pose, corners[i * 3], corners[i * 3 + 1], corners[i * 3 + 2])
                    .setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                    .setNormal(pose, d.stepX.toFloat(), d.stepY.toFloat(), d.stepZ.toFloat())
            }
        }
        quad(Direction.DOWN, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1)
        quad(Direction.UP, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0)
        quad(Direction.NORTH, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0)
        quad(Direction.SOUTH, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1)
        quad(Direction.WEST, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0)
        quad(Direction.EAST, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1)
    }

}
