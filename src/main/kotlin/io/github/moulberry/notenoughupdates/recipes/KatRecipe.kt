/*
 * Copyright (C) 2022 Linnea Gräf
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
package io.github.moulberry.notenoughupdates.recipes

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import io.github.moulberry.notenoughupdates.NEUManager
import io.github.moulberry.notenoughupdates.core.util.GuiElementSlider
import io.github.moulberry.notenoughupdates.miscfeatures.PetInfoOverlay
import io.github.moulberry.notenoughupdates.miscfeatures.PetInfoOverlay.Pet
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe
import io.github.moulberry.notenoughupdates.util.ItemUtils
import io.github.moulberry.notenoughupdates.util.PetLeveling
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.toJsonArray
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import java.time.Duration
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class KatRecipe(
    val manager: NEUManager,
    val inputPet: Ingredient,
    val outputPet: Ingredient,
    val items: List<Ingredient>,
    val coins: Long,
    val time: Duration,
) : NeuRecipe {
    var inputLevel = 1
    val radius get() = 50 / 2
    val circleCenter get() = 33 + 110 / 2 to 19 + 110 / 2
    val textPosition get() = circleCenter.first to circleCenter.second + 90 / 2
    val sliderPos get() = 40 to 15
    val levelTextPos
        get() = sliderPos.first - 4 - Minecraft.getMinecraft().fontRendererObj.getStringWidth("100") to
                sliderPos.second + 16 / 2 - Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT / 2

    val levelSlider = GuiElementSlider(0, 0, 100, 1F, 100F, 1F, inputLevel.toFloat()) { inputLevel = it.toInt() }
    val coinsAdjustedForLevel: Int
        get() = (coins.toInt() * (1 - 0.003F * (getOutputPetForCurrentLevel()?.petLevel?.currentLevel ?: 0))).toInt()
    private val basicIngredients = items.toSet() + setOf(inputPet, Ingredient.coinIngredient(manager, coins.toInt()))
    override fun getIngredients(): Set<Ingredient> = basicIngredients

    override fun getOutputs(): Set<Ingredient> {
        return setOf(outputPet)
    }

    fun getInputPetForCurrentLevel(): Pet? {
        return PetInfoOverlay.getPetFromStack(inputPet.itemStack.tagCompound)?.also {
            val petLeveling = PetLeveling.getPetLevelingForPet(it.petType, it.rarity)
            it.petLevel = petLeveling.getPetLevel(petLeveling.getPetExpForLevel(inputLevel).toDouble())
        }
    }

    fun getOutputPetForCurrentLevel(): Pet? {
        return PetInfoOverlay.getPetFromStack(outputPet.itemStack.tagCompound)?.also {
            val petLeveling = PetLeveling.getPetLevelingForPet(it.petType, it.rarity)
            it.petLevel = petLeveling.getPetLevel(getInputPetForCurrentLevel()?.petLevel?.expTotal?.toDouble() ?: 0.0)
        }
    }

    private fun positionOnCircle(i: Int, max: Int): Pair<Int, Int> {
        val radians = PI * 2 * i / max
        val offsetX = cos(radians) * radius
        val offsetY = sin(radians) * radius
        return (circleCenter.first + offsetX).roundToInt() to (circleCenter.second + offsetY).roundToInt()
    }

    override fun drawExtraInfo(gui: GuiItemRecipe, mouseX: Int, mouseY: Int) {
        levelSlider.x = gui.guiLeft + sliderPos.first
        levelSlider.y = gui.guiTop + sliderPos.second
        levelSlider.render()
        Minecraft.getMinecraft().fontRendererObj.drawString(
            "$inputLevel",
            gui.guiLeft + levelTextPos.first,
            gui.guiTop + levelTextPos.second,
            0xFF0000
        )
        Utils.drawStringCentered(
            Utils.prettyTime(time),
            gui.guiLeft + textPosition.first.toFloat(), gui.guiTop + textPosition.second.toFloat(),
            false, 0xff00ff
        )
        GlStateManager.color(1F, 1F, 1F, 1F)
    }

    override fun genericMouseInput(mouseX: Int, mouseY: Int) {
        levelSlider.mouseInput(mouseX, mouseY)
    }

    override fun getSlots(): List<RecipeSlot> {
        val advancedIngredients = items.map { it.itemStack } + listOf(
            ItemUtils.createPetItemstackFromPetInfo(getInputPetForCurrentLevel()),
            Ingredient.coinIngredient(
                manager,
                coinsAdjustedForLevel
            ).itemStack
        )
        return advancedIngredients.mapIndexed { index, itemStack ->
            val (x, y) = positionOnCircle(index, advancedIngredients.size)
            RecipeSlot(x - 18 / 2, y - 18 / 2, itemStack)
        } + listOf(
            RecipeSlot(
                circleCenter.first - 9,
                circleCenter.second - 9,
                ItemUtils.createPetItemstackFromPetInfo(getOutputPetForCurrentLevel())
            )
        )
    }

    override fun getType(): RecipeType = RecipeType.KAT_UPGRADE

    override fun hasVariableCost(): Boolean = false

    override fun serialize(): JsonObject {
        return JsonObject().apply {
            addProperty("type", type.id)
            addProperty("coins", coins)
            addProperty("input", inputPet.serialize())
            addProperty("output", outputPet.serialize())
            addProperty("time", time.seconds)
            add("items", items.map { JsonPrimitive(it.serialize()) }.toJsonArray())
        }
    }

    companion object {
        @JvmStatic
        fun parseRecipe(manager: NEUManager, recipe: JsonObject, output: JsonObject): NeuRecipe {
            return KatRecipe(
                manager,
                Ingredient(manager, recipe["input"].asString),
                Ingredient(manager, recipe["output"].asString),
                recipe["items"]?.asJsonArray?.map { Ingredient(manager, it.asString) } ?: emptyList(),
                recipe["coins"].asLong,
                Duration.ofSeconds(recipe["time"].asLong)
            )
        }
    }

    override fun getBackground(): ResourceLocation {
        return ResourceLocation("notenoughupdates:textures/gui/katting_tall.png")
    }

    override fun shouldUseForCraftCost() = false
}
