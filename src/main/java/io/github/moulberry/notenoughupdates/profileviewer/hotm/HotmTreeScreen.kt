/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.profileviewer.hotm

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.core.GlScissorStack
import io.github.moulberry.notenoughupdates.core.util.StringUtils
import io.github.moulberry.notenoughupdates.events.RepositoryReloadEvent
import io.github.moulberry.notenoughupdates.util.ItemUtils
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.kotlin.KotlinTypeAdapterFactory
import moe.nea.lisp.*
import moe.nea.lisp.bind.AutoBinder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.roundToInt

class HotmTreeRenderer(val hotmLayout: HotmTreeLayout, val prelude: List<String>) {
    val lec = LispExecutionContext()

    init {
        lec.setupStandardBindings()
        AutoBinder().bindTo(ExtraLispMethods(), lec.rootStackFrame)
        prelude.forEachIndexed { index, it ->
            lec.executeProgram(lec.rootStackFrame, LispParser.parse("hotmlayout.json:prelude[$index]", it))
        }
    }

    @NEUAutoSubscribe
    companion object {

        val gson = GsonBuilder()
            .registerTypeAdapterFactory(KotlinTypeAdapterFactory)
            .registerTypeAdapterFactory(LoreLineSerializer)
            .registerTypeAdapterFactory(LispProgramSerializer)
            .create()

        var renderer: HotmTreeRenderer? = null

        @SubscribeEvent
        fun onRepoReload(event: RepositoryReloadEvent) {
            renderer = runCatching {
                val hotmLayoutFile = NotEnoughUpdates.INSTANCE.manager.repoLocation
                    .resolve("constants/hotmlayout.json")
                val hotmLayout = gson.fromJson(hotmLayoutFile.readText(), HotmTreeLayoutFile::class.java)
                HotmTreeRenderer(hotmLayout.hotm, hotmLayout.prelude)
            }.onFailure {
                Utils.showOutdatedRepoNotification("constants/hotmlayout.json")
            }.getOrNull()
        }

        val perkBackground = ResourceLocation("notenoughupdates:profile_viewer/mining/perk_background.png")
        val perkConnectionX = ResourceLocation("notenoughupdates:profile_viewer/mining/perk_connection_x.png")
        val perkConnectionY = ResourceLocation("notenoughupdates:profile_viewer/mining/perk_connection_y.png")
    }

    val gridNodes = hotmLayout.perks.map { (it.value.x to it.value.y) to it }.toMap()
    val ySize = hotmLayout.perks.maxOf { it.value.y }
    val xSize = hotmLayout.perks.maxOf { it.value.x }

    fun renderPerks(
        levels: Map<String, JsonElement>,
        x: Int, y: Int,
        mouseX: Int, mouseY: Int,
        renderTooltip: Boolean,
        gridSize: Int,
        gridSpacing: Int,
    ) {
        val sr = ScaledResolution(Minecraft.getMinecraft())
        val relX = mouseX - x
        val relY = mouseY - y
        val gridOffset = (gridSize - 16) / 2
        for ((key, perk) in hotmLayout.perks) {
            val level = levels[key]?.asInt ?: 0
            val (values, bindings) = calculatePerkProperties(perk, level, levels)
            val tooltip = createPerkTooltip(perk, level, values, bindings)
            val perkItem = getPerkItem(perk, level, values, bindings, tooltip) ?: ItemStack(Items.painting, 1, 10)
            Minecraft.getMinecraft().textureManager.bindTexture(perkBackground)
            Utils.drawTexturedRect(
                (perk.x * gridSize + x + gridSpacing / 2).toFloat(),
                (perk.y * gridSize + y + gridSpacing / 2).toFloat(),
                gridSize.toFloat() - gridSpacing, gridSize.toFloat() - gridSpacing,
                0F, 1f, 0f, 1f
            )
            if (Pair(perk.x - 1, perk.y) in gridNodes) {
                Minecraft.getMinecraft().textureManager.bindTexture(perkConnectionX)
                Utils.drawTexturedRect(
                    (perk.x * gridSize + x - gridSpacing / 2).toFloat(),
                    (perk.y * gridSize + y).toFloat(),
                    gridSpacing.toFloat(), gridSize.toFloat(),
                    0F, 1f, 0f, 1f
                )
            }
            if (Pair(perk.x, perk.y - 1) in gridNodes) {
                Minecraft.getMinecraft().textureManager.bindTexture(perkConnectionY)
                Utils.drawTexturedRect(
                    (perk.x * gridSize + x).toFloat(),
                    (perk.y * gridSize + y - gridSpacing / 2).toFloat(),
                    gridSize.toFloat(), gridSpacing.toFloat(),
                    0F, 1f, 0f, 1f
                )
            }
            if (renderTooltip &&
                relX in (perk.x * gridSize..perk.x * gridSize + gridSize) &&
                relY in (perk.y * gridSize..perk.y * gridSize + gridSize)) {
                GlScissorStack.disableTemporary()
                Utils.drawHoveringText(
                    tooltip,
                    mouseX, mouseY,
                    sr.scaledWidth, sr.scaledHeight, -1
                )
                GlScissorStack.refresh(sr)
            }
            Utils.drawItemStack(perkItem, perk.x * gridSize + x + gridOffset, perk.y * gridSize + y + gridOffset)
        }
    }

    fun getPerkItem(
        perk: LayoutedHotmPerk,
        level: Int,
        values: Map<String, LispData?>,
        bindings: StackFrame,
        tooltip: List<String>
    ): ItemStack? {
        val itemId = values["item"] ?: LispData.LispNil

        val repoId = when (itemId) {
            is LispData.Atom -> itemId.label
            is LispData.LispString -> itemId.string
            else -> return null
        }
        val item = NotEnoughUpdates.INSTANCE.manager.createItem(repoId)
        ItemUtils.setLore(item, tooltip)
        return item
    }

    fun calculatePerkProperties(
        perk: LayoutedHotmPerk,
        level: Int,
        levels: Map<String, JsonElement>
    ): Pair<Map<String, LispData?>, StackFrame> {
        val bindings = lec.genBindings()
        bindings.setValueLocal("potm", LispData.LispNumber((levels["special_0"]?.asInt ?: 0).toDouble()))
        bindings.setValueLocal("level", LispData.LispNumber(if (level == 0) 1.0 else level.toDouble()))
        bindings.setValueLocal("maxLevel", LispData.LispNumber(perk.maxLevel.toDouble()))
        bindings.setValueLocal("level0", LispData.LispNumber(level.toDouble()))
        val values = perk.compiledFunctions.mapValues {
            lec.executeProgram(bindings.fork(), it.value)
        }
        return values to bindings
    }

    private fun createPerkTooltip(
        perk: LayoutedHotmPerk,
        level: Int,
        values: Map<String, LispData?>,
        bindings: StackFrame
    ): List<String> {
        val perkTitle = when (level) {
            perk.maxLevel -> "§a${perk.name}"
            0 -> "§c${perk.name}"
            else -> "§e${perk.name}"
        }
        val begin =
            if (perk.maxLevel == 1) listOf(perkTitle)
            else listOf(
                perkTitle,
                if (level != perk.maxLevel) "§7Level $level§8/${perk.maxLevel}"
                else "§7Level $level",
                ""
            )
        val end: List<String> = if (level == 0 || level == perk.maxLevel) listOf() else listOf(
            "",
            hotmLayout.powders[(lec.executeProgram(bindings.fork().also {
                for (powder in hotmLayout.powders) {
                    it.setValueLocal(powder.key, LispData.LispString(powder.key))
                }
            }, perk.powder) as? LispData.LispString)?.string ?: ""]?.costLine ?: "<lisp-error>"
        )
        return (begin + perk.lore.filter {
            if (it.condition != null)
                CoreBindings.isTruthy(lec.executeProgram(bindings.fork(), it.condition) ?: LispData.LispNil) ?: true
            else
                true
        }.map { it.text } + end).map {
            it.replace("\\{([a-z\\-A-Z_0-9]+)\\}".toRegex()) {
                (when (val value = values[it.groupValues[1]]) {
                    is LispData.LispString -> value.string
                    is LispData.LispNumber -> StringUtils.formatNumber(if (it.groupValues[1] == "cost") value.value.roundToInt() else value.value)
                    else -> "<lisp-error>"
                })
            }
        }
    }
}
