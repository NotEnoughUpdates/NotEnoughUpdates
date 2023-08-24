/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.commands.help

import io.github.moulberry.moulconfig.GuiTextures
import io.github.moulberry.moulconfig.annotations.ConfigOption
import io.github.moulberry.moulconfig.gui.GuiOptionEditor
import io.github.moulberry.moulconfig.gui.GuiScreenElementWrapper
import io.github.moulberry.moulconfig.gui.MoulConfigEditor
import io.github.moulberry.moulconfig.processor.*
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.core.config.GuiOptionEditorBlocked
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.miscfeatures.EnforcedConfigValues
import io.github.moulberry.notenoughupdates.miscfeatures.IQTest
import io.github.moulberry.notenoughupdates.options.NEUConfig
import io.github.moulberry.notenoughupdates.util.brigadier.*
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.lang.reflect.Field

@NEUAutoSubscribe
object SettingsCommand {
    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neu", "neusettings") {
            thenArgument("search", RestArgumentType) { search ->
                val searchSpace = mutableListOf<String>()
                ConfigProcessorDriver.processConfig(
                    NotEnoughUpdates.INSTANCE.config.javaClass,
                    NotEnoughUpdates.INSTANCE.config,
                    object : ConfigStructureReader {
                        override fun beginCategory(p0: Any?, p1: Field?, p2: String, p3: String) {
                            searchSpace.add(p2)
                            searchSpace.add(p3)
                        }

                        override fun endCategory() {
                        }

                        override fun beginAccordion(p0: Any?, p1: Field?, p2: ConfigOption, p3: Int) {
                            searchSpace.add(p2.name)
                            searchSpace.add(p2.desc)
                        }

                        override fun endAccordion() {
                        }

                        override fun emitOption(p0: Any?, p1: Field?, p2: ConfigOption) {
                            searchSpace.add(p2.name)
                            searchSpace.add(p2.desc)
                        }

                        override fun emitGuiOverlay(p0: Any?, p1: Field?, p2: ConfigOption?) {
                        }

                    }
                )
                suggestsList(searchSpace)
                thenExecute {
                    NotEnoughUpdates.INSTANCE.openGui = createConfigScreen(this[search])
                }
            }.withHelp("Search the NEU settings")
            thenExecute {
                NotEnoughUpdates.INSTANCE.openGui = createConfigScreen("")
            }
        }.withHelp("Open the NEU settings")
    }

    class BlockingMoulConfigProcessor : MoulConfigProcessor<NEUConfig>(NotEnoughUpdates.INSTANCE.config) {
        override fun createOptionGui(
            processedOption: ProcessedOption,
            field: Field,
            option: ConfigOption
        ): GuiOptionEditor? {
            val default = super.createOptionGui(processedOption, field, option) ?: return null
            if (EnforcedConfigValues.isBlockedFromEditing(processedOption.path)) {
                return GuiOptionEditorBlocked(default)
            }
            return default
        }

        var iqTestCopy: LinkedHashMap<String, ProcessedCategory>? = null
        override fun getAllCategories(): LinkedHashMap<String, ProcessedCategory> {
            val s = super.getAllCategories()
            if (iqTestCopy == null) {
                iqTestCopy = s.clone() as LinkedHashMap<String, ProcessedCategory>
            }
            iqTestCopy!!["apiData"] = IQTest.options
            if (NotEnoughUpdates.INSTANCE.config.apiData.apiDataUnlocked) {
                return s
            }
            return iqTestCopy!!
        }
    }

    var lastEditor = null as MoulConfigEditor<NEUConfig>?
    fun createConfigScreen(search: String): GuiScreen {
        return object : GuiScreenElementWrapper(createConfigElement(search)) {
        }
    }
    fun createConfigElement(search: String): MoulConfigEditor<NEUConfig> {
        val processor = BlockingMoulConfigProcessor()
        BuiltinMoulConfigGuis.addProcessors(processor)
        ConfigProcessorDriver.processConfig(
            NotEnoughUpdates.INSTANCE.config.javaClass,
            NotEnoughUpdates.INSTANCE.config,
            processor
        )
        val editor = MoulConfigEditor(processor)
        editor.search(search)
        lastEditor = editor
        return editor
    }
    init {
        GuiTextures.setTextureRoot(ResourceLocation("notenoughupdates:core"))
    }
}
