package dev.nathanpb.dml.simulacrum.screen

import net.minecraft.client.renderer.RenderPipelines
import dev.nathanpb.dml.data.dataModel
import dev.nathanpb.dml.enums.DataModelTier
import dev.nathanpb.dml.identifier
import dev.nathanpb.dml.item.ItemDataModel
import dev.nathanpb.dml.simulacrum.PRISTINE_CHANCE
import dev.nathanpb.dml.simulacrum.block.chamber.BlockEntitySimulationChamber
import dev.nathanpb.dml.simulacrum.util.Animation
import dev.nathanpb.dml.simulacrum.util.DataModelUtil
import dev.nathanpb.dml.utils.RenderUtils.Companion.ENERGY_STYLE
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.world.level.Level
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

open class ScreenSimulationChamber(handler: ScreenHandlerSimulationChamber, inventory: Inventory, title: Component):
    AbstractContainerScreen<ScreenHandlerSimulationChamber>(handler, inventory, title, 232, 230) {

    private val WHITE = -1
    private val gui = identifier("textures/gui/simulation_chamber_base.png")
    private val defaultGui = identifier("textures/gui/default_gui.png")
    private var blockEntity: BlockEntitySimulationChamber
    private var maxEnergy = 0.0
    private var simulationText: HashMap<String, String> = HashMap<String, String>()
    private var simulationAnimations: HashMap<String, Animation> = HashMap<String, Animation>()
    private var currentDataModel = ItemStack.EMPTY
    private var renderer: Font = Minecraft.getInstance().font
    private var world: Level? = null

    init {
        blockEntity = Minecraft.getInstance().level!!.getBlockEntity(handler.blockPos) as BlockEntitySimulationChamber
        maxEnergy = blockEntity.energyStorage.getCapacity().toDouble()
        world = blockEntity.level
    }


    override fun extractBackground(ctx: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        super.extractBackground(ctx, mouseX, mouseY, delta)
        val f = DecimalFormat("0.#")
        val x: Int = leftPos + 8
        val y: Int = topPos
        val spacing = 12
        val yStart: Int = y - 3

        //Main Chamber GUI
        ctx.blit(RenderPipelines.GUI_TEXTURED, gui, x, y, 0F, 0F, 216, 141, 256, 256)
        ctx.blit(RenderPipelines.GUI_TEXTURED, gui, x, y + 145, 216F, 0F, 18, 18, 256, 256)

        //Energy Bar Rendering
        val energyBarHeight: Int = (menu.syncedEnergy / (maxEnergy - 64) * 89).toInt().coerceAtLeast(0).coerceAtMost(89)
        val energyBarOffset: Int = 89 - energyBarHeight
        ctx.blit(RenderPipelines.GUI_TEXTURED, gui, x + 202, (y + 47) + energyBarOffset, 243F, 0F, 9, energyBarHeight, 256, 256)


        val lines: Array<String>
        if(!blockEntity.hasDataModel()) {
            lines = arrayOf("text.dml-refabricated.simulation_chamber.insert_data_model.1", "text.dml-refabricated.simulation_chamber.insert_data_model.2")
            val a1 = getAnimation("pleaseInsert1")
            val a2 = getAnimation("pleaseInsert2")
            animateString(ctx, lines[0], a1, null, 1, false, x + 10, yStart + spacing, WHITE)
            animateString(ctx, lines[1], a2, a1, 1, false, x + 10, yStart + spacing * 2, WHITE)
        } else if(blockEntity.dataModelStack.dataModel.tier() == DataModelTier.FAULTY) {
            lines = arrayOf("text.dml-refabricated.simulation_chamber.insuficiente_data.1", "text.dml-refabricated.simulation_chamber.insuficiente_data.2", "text.dml-refabricated.simulation_chamber.insuficiente_data.3")
            val insufData = getAnimation("insufData1")
            val insufData2 = getAnimation("insufData2")
            val insufData3 = getAnimation("insufData3")
            animateString(ctx, lines[0], insufData, null, 1, false, x + 10, yStart + spacing, WHITE)
            animateString(ctx, lines[1], insufData2, insufData, 1, false, x + 10, yStart + spacing * 2, WHITE)
            animateString(ctx, lines[2], insufData3, insufData2, 1, false, x + 10, yStart + spacing * 3, WHITE)
        } else {
            // Draw current data model data
            if (blockEntity.dataModelStack.dataModel.tier() == DataModelTier.SELF_AWARE) {
                ctx.blit(RenderPipelines.GUI_TEXTURED, gui, x + 6, y + 48, 18F, 141F, 7, 87, 256, 256)
            } else {
                val collectedData = blockEntity.dataModelStack.dataModel.tier().dataAmount - blockEntity.dataModelStack.dataModel.tier().dataAmount
                val tierRoof = DataModelUtil.getTierRoof(blockEntity.dataModelStack) - blockEntity.dataModelStack.dataModel.tier().dataAmount
                val experienceBarHeight = (collectedData.toFloat() / tierRoof * 89).toInt()
                val experienceBarOffset = 89 - experienceBarHeight
                ctx.blit(RenderPipelines.GUI_TEXTURED, gui, x + 5, y + 47 + experienceBarOffset, 234F, 0F, 9, experienceBarHeight, 256, 256)
            }
            ctx.text(
                renderer, Component.translatable("tooltip.dml-refabricated.data_model.3").copy().append(
                    blockEntity.dataModelStack.dataModel.tier().text
                ), x + 10, yStart + spacing, WHITE, true
            )
            ctx.text(
                renderer, Component.translatable("text.dml-refabricated.simulation_chamber.iterations", f.format(
                    blockEntity.dataModelStack.dataModel.simulationCount)
                ), x + 10, yStart + spacing * 2, WHITE, true
            )
            ctx.text(
                renderer, Component.translatable("text.dml-refabricated.simulation_chamber.pristine_chance",
                        PRISTINE_CHANCE[blockEntity.dataModelStack.dataModel.tier()] ?: 0
                ).append("%"), x + 10, yStart + spacing * 3, WHITE, true
            )
        }
        ctx.blit(RenderPipelines.GUI_TEXTURED, defaultGui, x + 20, y + 145, 0F, 0F, 176, 90, 256, 256)


        drawConsoleText(ctx, x, y, spacing)
        if(blockEntity.isCrafting && !hasDataModelChanged()) updateSimulationText()
        if((!blockEntity.isCrafting && blockEntity.canStartSimulation()) || hasDataModelChanged() || blockEntity.percentDone == 100) {
            resetAnimations()
        }
    }

    private fun resetAnimations() {
        simulationAnimations = HashMap<String, Animation>()
        simulationText = HashMap<String, String>()
    }

    // No title labels, only the tooltips of the bars (in screen coordinates since 1.21.6)
    override fun extractLabels(ctx: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        val x: Int = mouseX - leftPos
        val y: Int = mouseY - topPos
        val f = NumberFormat.getNumberInstance(Locale.ENGLISH)
        val tooltip: MutableList<Component> = ArrayList()
        if (y in 47..135) {
            if (x in 13..21) {
                // Tooltip for data model data bar
                if (blockEntity.hasDataModel()) {
                    if (blockEntity.dataModelStack.dataModel.tier() != DataModelTier.SELF_AWARE) {
                        val currentTierCount = blockEntity.dataModelStack.dataModel.tier().dataAmount - blockEntity.dataModelStack.dataModel.tier().dataAmount
                        val currentTierRoof = (DataModelUtil.getTierRoof(blockEntity.dataModelStack) - blockEntity.dataModelStack.dataModel.tier().dataAmount)
                        tooltip.add(Component.translatable("text.dml-refabricated.simulation_chamber.data_collected", currentTierCount, currentTierRoof))
                    } else {
                        tooltip.add(Component.translatable("text.dml-refabricated.simulation_chamber.max_tier"))
                    }
                } else {
                    tooltip.add(Component.translatable("text.dml-refabricated.simulation_chamber.missing_data_model"))
                }
                ctx.setComponentTooltipForNextFrame(font, tooltip, mouseX + 2, mouseY + 2)
            } else if (x in 210..219) {
                val energyText = Component.literal(f.format(menu.syncedEnergy.toLong()) + "/" + f.format(maxEnergy) + " E").setStyle(ENERGY_STYLE)
                tooltip.add(energyText)
                ctx.setComponentTooltipForNextFrame(font, tooltip, mouseX - 90, mouseY - 16)
            }
        }
    }

    private fun getAnimation(key: String): Animation? {
        if (!simulationAnimations.containsKey(key)) {
            simulationAnimations[key] = Animation()
        }
        return simulationAnimations[key]
    }

    private fun animateString(ctx: GuiGraphicsExtractor, string: String, anim: Animation?, precedingAnim: Animation?, delay: Int, loop: Boolean, x: Int, y: Int, color: Int) {
        if (precedingAnim != null) {
            if (precedingAnim.hasFinished()) {
                val result = anim!!.animate(string, delay, world!!.gameTime, loop)
                ctx.text(
                    renderer,
                    result,
                    x, y,
                    color, true
                )
            } else {
                return
            }
        }
        val result = anim!!.animate(string, delay, world!!.gameTime, loop)
        ctx.text(
            renderer,
            result,
            x, y,
            color, true
        )
    }

    private fun animate(string: String, anim: Animation?, precedingAnim: Animation?, delayInTicks: Int, loop: Boolean): String {
        return animate(string, anim, precedingAnim, delayInTicks, loop, -1)
    }

    private fun animate(string: String, anim: Animation?, precedingAnim: Animation?, delayInTicks: Int, loop: Boolean, intArg: Int): String {
        return if (precedingAnim != null) {
            if (precedingAnim.hasFinished()) {
                anim!!.animate(string, delayInTicks, world!!.gameTime, loop, intArg)
            } else {
                ""
            }
        } else anim!!.animate(string, delayInTicks, world!!.gameTime, loop, intArg)
    }

    private fun drawConsoleText(ctx: GuiGraphicsExtractor, x: Int, y: Int, spacing: Int) {
        val lines: Array<String>
        if (!blockEntity.hasDataModel() || blockEntity.dataModelStack.dataModel.tier() == DataModelTier.FAULTY) {
            animateString(ctx, "_", getAnimation("blinkingUnderline"), null, 16, true, x + 21, y + 49, WHITE)
        } else if (!blockEntity.hasPolymerClay() && !blockEntity.isCrafting) {
            lines = arrayOf("text.dml-refabricated.simulation_chamber.cant_begin", "text.dml-refabricated.simulation_chamber.missing_polymer", "_")
            val a1 = getAnimation("inputSlotEmpty1")
            val a2 = getAnimation("inputSlotEmpty2")
            val a3 = getAnimation("blinkingUnderline1")
            animateString(ctx, lines[0], a1, null, 1, false, x + 21, y + 51, WHITE)
            animateString(ctx, lines[1], a2, a1, 1, false, x + 21, y + 51 + spacing, WHITE)
            animateString(ctx, lines[2], a3, a2, 16, true, x + 21, y + 51 + spacing * 2, WHITE)
        } else if (!hasEnergy() && !blockEntity.isCrafting) {
            lines = arrayOf("text.dml-refabricated.simulation_chamber.cant_begin", "text.dml-refabricated.simulation_chamber.missing_energy", "_")
            val a1 = getAnimation("lowEnergy1")
            val a2 = getAnimation("lowEnergy2")
            val a3 = getAnimation("blinkingUnderline2")
            animateString(ctx, lines[0], a1, null, 1, false, x + 21, y + 51, WHITE)
            animateString(ctx, lines[1], a2, a1, 1, false, x + 21, y + 51 + spacing, WHITE)
            animateString(ctx, lines[2], a3, a2, 16, true, x + 21, y + 51 + spacing * 2, WHITE)
        } else if (blockEntity.outputIsFull() || blockEntity.pristineIsFull()) {
            lines = arrayOf("text.dml-refabricated.simulation_chamber.cant_begin", "text.dml-refabricated.simulation_chamber.output_full", "_")
            val a1 = getAnimation("outputSlotFilled1")
            val a2 = getAnimation("outputSlotFilled2")
            val a3 = getAnimation("blinkingUnderline3")
            animateString(ctx, lines[0], a1, null, 1, false, x + 21, y + 51, WHITE)
            animateString(ctx, lines[1], a2, a1, 1, false, x + 21, y + 51 + spacing, WHITE)
            animateString(ctx, lines[2], a3, a2, 16, true, x + 21, y + 51 + spacing * 2, WHITE)
        } else if (blockEntity.isCrafting) {
            ctx.text(
                renderer,
                blockEntity.percentDone.toString() + "%",
                x + 176,
                y + 123,
                0xFF62D8FF.toInt(),
                true
            )
            ctx.text(
                renderer,
                getSimulationText("simulationProgressLine1"),
                x + 21,
                y + 51,
                WHITE,
                true
            )
            ctx.text(
                renderer,
                getSimulationText("simulationProgressLine1Version"),
                x + 124,
                y + 51,
                WHITE,
                true
            )
            ctx.text(
                renderer,
                getSimulationText("simulationProgressLine2"),
                x + 21,
                y + 51 + spacing,
                WHITE,
                true
            )
            ctx.text(
                renderer, getSimulationText("simulationProgressLine3"), x + 21,
                y + 51 + spacing * 2, WHITE, true
            )
            ctx.text(
                renderer, getSimulationText("simulationProgressLine4"), x + 21,
                y + 51 + spacing * 3, WHITE, true
            )
            ctx.text(
                renderer, getSimulationText("simulationProgressLine5"), x + 21,
                y + 51 + spacing * 4, WHITE, true
            )
            ctx.text(
                renderer, getSimulationText("simulationProgressLine6"), x + 21,
                y + 51 + spacing * 5, WHITE, true
            )
            ctx.text(
                renderer, getSimulationText("simulationProgressLine6Result"), x + 140,
                y + 51 + spacing * 5, WHITE, true
            )
            ctx.text(
                renderer, getSimulationText("simulationProgressLine7"), x + 21,
                y + 51 + spacing * 6, WHITE, true
            )
            ctx.text(
                renderer, getSimulationText("blinkingDots1"), x + 128,
                y + 51 + spacing * 6, WHITE, true
            )
        } else {
            animateString(ctx, "_", getAnimation("blinkingUnderline"), null, 16, true, x + 21, y + 49, WHITE)
        }
    }

    private fun updateSimulationText() {
        val lines = arrayOf(
            "text.dml-refabricated.simulation_chamber.sim.1",
            "text.dml-refabricated.simulation_chamber.sim.2",
            "text.dml-refabricated.simulation_chamber.sim.3",
            "text.dml-refabricated.simulation_chamber.sim.4",
            "text.dml-refabricated.simulation_chamber.sim.5",
            "text.dml-refabricated.simulation_chamber.sim.6",
            "text.dml-refabricated.simulation_chamber.sim.7",
            if(blockEntity.byproductSuccess) "text.dml-refabricated.simulation_chamber.succeeded" else "text.dml-refabricated.simulation_chamber.fail",
            "text.dml-refabricated.simulation_chamber.sim.8",
            "..."
        )
        val resultPrefix = if(blockEntity.byproductSuccess) "§a" else "§c"
        val aLine1: Animation? = getAnimation("simulationProgressLine1")
        val aLine1Version: Animation? = getAnimation("simulationProgressLine1Version")
        val aLine2: Animation? = getAnimation("simulationProgressLine2")
        val aLine3: Animation? = getAnimation("simulationProgressLine3")
        val aLine4: Animation? = getAnimation("simulationProgressLine4")
        val aLine5: Animation? = getAnimation("simulationProgressLine5")
        val aLine6: Animation? = getAnimation("simulationProgressLine6")
        val aLine6Result: Animation? = getAnimation("simulationProgressLine6Result")
        val aLine7: Animation? = getAnimation("simulationProgressLine7")
        val aLine8: Animation? = getAnimation("blinkingDots1")
        simulationText["simulationProgressLine1"] = animate(lines[0], aLine1, null, 1, false)
        simulationText["simulationProgressLine1Version"] = "§6" + animate(lines[1], aLine1Version, aLine1, 1, false) + "§r"

        if(currentDataModel.item !is ItemDataModel) return
        simulationText["simulationProgressLine2"] = animate(lines[2], aLine2, aLine1Version, 1, false, (currentDataModel.dataModel.simulationCount + 1))
        simulationText["simulationProgressLine3"] = animate(lines[3], aLine3, aLine2, 2, false)
        simulationText["simulationProgressLine4"] = animate(lines[4], aLine4, aLine3, 1, false)
        simulationText["simulationProgressLine5"] = animate(lines[5], aLine5, aLine4, 2, false)
        simulationText["simulationProgressLine6"] = animate(lines[6], aLine6, aLine5, 2, false)
        simulationText["simulationProgressLine6Result"] =
            resultPrefix + animate(lines[7], aLine6Result, aLine6, 2, false) + "§r"
        simulationText["simulationProgressLine7"] = animate(lines[8], aLine7, aLine6Result, 1, false)
        simulationText["blinkingDots1"] = animate(lines[9], aLine8, aLine7, 8, true)
    }

    private fun getSimulationText(key: String): String? {
        if (!simulationText.containsKey(key)) {
            simulationText[key] = ""
        }
        return simulationText[key]
    }

    private fun hasEnergy(): Boolean {
        return blockEntity.hasEnergyForSimulation()
    }

    private fun hasDataModelChanged(): Boolean {
        if(ItemStack.matches(currentDataModel, blockEntity.dataModelStack)) return false

        currentDataModel = blockEntity.dataModelStack
        return true
    }
}