package dev.nathanpb.dml.blockEntity.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Axis
import dev.nathanpb.dml.block.BLOCK_DATA_SYNTHESIZER
import dev.nathanpb.dml.blockEntity.BlockEntityDataSynthesizer
import dev.nathanpb.dml.identifier
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
import net.minecraft.client.renderer.feature.ModelFeatureRenderer
import net.minecraft.client.renderer.item.ItemModelResolver
import net.minecraft.client.renderer.item.ItemStackRenderState
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.util.LightCoordsUtil
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.Vec3

/**
 * Renders the data model inside the machine (lit like the block in front of it) and the scanning grid that moves
 * down and up while the machine works.
 */
class BlockEntityRendererDataSynthesizer(
    ctx: BlockEntityRendererProvider.Context
): BlockEntityRenderer<BlockEntityDataSynthesizer, BlockEntityRendererDataSynthesizer.State> {

    class State : BlockEntityRenderState() {
        val item = ItemStackRenderState()
        var direction: Direction = Direction.NORTH
        var frontLight = 0
        var progress = 0F
    }

    private val itemModelResolver: ItemModelResolver = ctx.itemModelResolver()
    private val gridRenderType = RenderTypes.entityTranslucent(identifier("textures/block/data_synthesizer_grid.png"))

    override fun createRenderState() = State()

    override fun extractRenderState(
        blockEntity: BlockEntityDataSynthesizer,
        state: State,
        partialTicks: Float,
        cameraPosition: Vec3,
        breakProgress: ModelFeatureRenderer.CrumblingOverlay?
    ) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress)
        val world = blockEntity.level
        val blockState = blockEntity.blockState
        state.item.clear()
        if(world == null || !blockState.`is`(BLOCK_DATA_SYNTHESIZER)) return

        state.direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)
        state.frontLight = LightCoordsUtil.getLightCoords(world, blockEntity.blockPos.relative(state.direction))
        state.progress = Mth.clamp(blockEntity.progress.toFloat() / blockEntity.maxProgress.toFloat(), 0F, 1F)
        itemModelResolver.updateForTopItem(
            state.item, blockEntity.inventory.dataModelStack, ItemDisplayContext.FIXED, world, null,
            blockEntity.blockPos.asLong().toInt()
        )
    }

    override fun submit(state: State, ms: PoseStack, collector: SubmitNodeCollector, camera: CameraRenderState) {
        if(state.item.isEmpty) return
        val light = state.frontLight

        // Data Model
        ms.pushPose()
        ms.translate(0.5, 0.5, 0.5)

        if(state.direction != Direction.NORTH) { // no rotation required
            val angles = angleMap[state.direction] as Angle
            ms.rotateDegrees(Axis.XP, angles.x)
            ms.rotateDegrees(Axis.YP, angles.y)
            ms.rotateDegrees(Axis.ZP, angles.z)
        }

        ms.scale(0.5F, 0.5F, 0.5F)
        state.item.submit(ms, collector, light, OverlayTexture.NO_OVERLAY, 0)
        ms.popPose()

        // Grid: a 12x12 pixel plane at the top of the machine (it was models/block/data_synthesizer_grid.json)
        val percentage = state.progress
        val y = if(percentage <= 0.5F) { // First half, downward
            Mth.lerp(percentage * 2F, 0.0315F, 0.77F)
        } else { // Second half, upward
            Mth.lerp(2F * (1F - percentage), 0.0315F, 0.77F)
        }

        ms.pushPose()
        ms.translate(0F, -y, 0F)
        collector.submitCustomGeometry(ms, gridRenderType) { pose, buffer ->
            val min = 2F / 16F
            val max = 14F / 16F
            val height = 14.5F / 16F
            val uvMax = 12F / 16F
            // Up
            vertex(buffer, pose, min, height, min, 0F, 0F, light, 1F)
            vertex(buffer, pose, min, height, max, 0F, uvMax, light, 1F)
            vertex(buffer, pose, max, height, max, uvMax, uvMax, light, 1F)
            vertex(buffer, pose, max, height, min, uvMax, 0F, light, 1F)
            // Down
            vertex(buffer, pose, min, height, min, 0F, 0F, light, -1F)
            vertex(buffer, pose, max, height, min, uvMax, 0F, light, -1F)
            vertex(buffer, pose, max, height, max, uvMax, uvMax, light, -1F)
            vertex(buffer, pose, min, height, max, 0F, uvMax, light, -1F)
        }
        ms.popPose()
    }

    private fun vertex(buffer: VertexConsumer, pose: PoseStack.Pose, x: Float, y: Float, z: Float, u: Float, v: Float, light: Int, normalY: Float) {
        buffer.addVertex(pose, x, y, z)
            .setColor(-1)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(pose, 0F, normalY, 0F)
    }


    private val angleMap: HashMap<Direction, Angle> = hashMapOf(
        Direction.SOUTH to Angle(180F, 0F, 180F),
        Direction.WEST to Angle(180F, 90F, 180F),
        Direction.EAST to Angle(90F, 270F, 90F)
    )

    data class Angle(val x: Float, val y: Float, val z: Float)

}
