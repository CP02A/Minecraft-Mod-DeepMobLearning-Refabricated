package dev.nathanpb.dml.command

import com.mojang.brigadier.CommandDispatcher
import dev.nathanpb.dml.utils.*
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.Commands.CommandSelection
import net.minecraft.commands.Commands.literal
import java.net.URI
import net.minecraft.commands.CommandSourceStack
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style


class DMLCommand: CommandRegistrationCallback {

    override fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        registryAccess: CommandBuildContext,
        environment: CommandSelection
    ) {
        dispatcher.register(literal("dml-refabricated")
            // Modules
            .then(literal("modules")
                .executes {
                    it.source.sendSystemMessage(appendModuleText(BASE_ID, "Base"))
                    it.source.sendSystemMessage(appendModuleText(MODULAR_ARMOR_ID, "Glitch Armor"))
                    it.source.sendSystemMessage(appendModuleText(SIMULACRUM_ID, "Simulacrum"))
                    return@executes 1
                }
            )

            // Main Command
            .executes {
                val metadata by lazy {
                    getModContainer(CORE_ID).metadata
                }
                val authors by lazy {
                    metadata.authors.filter { person -> person.name != "IterationFunk" }.joinToString {
                            person -> person.name
                    }
                }
                val contributors by lazy {
                    metadata.contributors.joinToString {
                            person -> person.name
                    }
                }

                it.source.sendSystemMessage(coloredText("-=  ", true).append(coloredText("${metadata.name} v${metadata.version}")).append(coloredText("  =-", true)))
                it.source.sendSystemMessage(coloredText("Original mod by ").append(coloredText("IterationFunk", true)))
                it.source.sendSystemMessage(coloredText("Reimagining/port by ").append(coloredText(authors, true)))
                it.source.sendSystemMessage(coloredText("With contributions of ").append(coloredText(contributors, true)))
                if(it.source.isPlayer) {
                    it.source.sendSystemMessage(Component.empty())
                    it.source.sendSystemMessage(
                        hyperlink("CurseForge", "https://curseforge.com/minecraft/mc-mods/deep-mob-learning-refabricated", 0xF46434).append(
                        " ").append(
                        hyperlink("Modrinth", "https://modrinth.com/mod/deep-mob-learning-refabricated", 0x1CD368)).append(
                        " ").append(
                        hyperlink("Issue Tracker", "https://github.com/CafeteriaGuild/DeepMobLearning-Refabricated/issues", 0x6E5494))
                    )
                }


                return@executes 1
            })
    }


    private fun appendModuleText(modId: String, fancyName: String): Component {
        val isLoaded = isModLoaded(modId)
        val version: String = getModContainer(modId).let {
            if(it != null) "v${it.metadata.version}" else ""
        }

        return coloredText("- [")
            .append(Component.literal(if(isLoaded) "✔" else "✕").withStyle(if(isLoaded) ChatFormatting.GREEN else ChatFormatting.RED))
            .append(coloredText("] "))
            .append(coloredText("$fancyName $version"))
    }

    private fun coloredText(message: String): MutableComponent {
        return coloredText(message, false)
    }

    private fun coloredText(message: String, altStyle: Boolean): MutableComponent {
        return Component.literal(message).setStyle(if(altStyle) RenderUtils.ALT_STYLE else RenderUtils.STYLE)
    }

    private fun hyperlink(title: String, url: String, color: Int): MutableComponent {
        return Component.literal("[$title]").setStyle(Style.EMPTY
                .withClickEvent(ClickEvent.OpenUrl(URI.create(url)))
                .withHoverEvent(HoverEvent.ShowText(Component.literal("Click to open link").setStyle(Style.EMPTY.withColor(color))))
                .withColor(color)
                .withBold(true)
        )
    }
}
