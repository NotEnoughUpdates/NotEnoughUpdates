/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
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
import io.github.moulberry.notenoughupdates.miscfeatures.PetInfoOverlay
import io.github.moulberry.notenoughupdates.miscfeatures.PetInfoOverlay.Pet
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer
import io.github.moulberry.notenoughupdates.util.ItemUtils
import io.github.moulberry.notenoughupdates.util.PetLeveling
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.toJsonArray
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
data class KatRecipe(
    val manager: NEUManager,
    val inputPet: Ingredient,
    val outputPet: Ingredient,
    val items: List<Ingredient>,
    val coins: Long,
) : NeuRecipe {


    var inputLevel = 25

    private val basicIngredients = items.toSet() + setOf(inputPet, Ingredient.coinIngredient(manager, coins.toInt()))
    override fun getIngredients(): Set<Ingredient> = basicIngredients

    override fun getOutputs(): Set<Ingredient> {
        return setOf(outputPet)
    }


    fun getInputPetForCurrentLevel(): Pet {
        return PetInfoOverlay.getPetFromStack(inputPet.itemStack.tagCompound).also {
            it.petLevel = PetLeveling.getPetLevelingForPet(it.petType, it.rarity).getPetLevel(0.0)
        }
    }

    fun getOutputPetForCurrentLevel(): Pet {
        return getInputPetForCurrentLevel()
    }

    val radius get() = 50 / 2
    val circleCenter = 33 + 110 / 2 to 19 + 110 / 2
    val textPosition get() = circleCenter.first to circleCenter.second + 90 / 2
    var rotation = 0.0
    var wasShiftDown = false
    var lastTimestamp = TimeSource.Monotonic.markNow()

    fun positionOnCircle(i: Int, max: Int): Pair<Int, Int> {
        val radians = PI * 2 * i / max - rotation * PI / 2
        val offsetX = cos(radians) * radius
        val offsetY = sin(radians) * radius
        return (circleCenter.first + offsetX).roundToInt() to (circleCenter.second + offsetY).roundToInt()
    }

    override fun drawExtraInfo(gui: GuiItemRecipe, mouseX: Int, mouseY: Int) {
        Utils.drawStringCentered(
            "This will take 5 years lmao",
            Minecraft.getMinecraft().fontRendererObj,
            gui.guiLeft + textPosition.first.toFloat(), gui.guiTop + textPosition.second.toFloat(),
            false, 0xff00ff
        )
    }

    override fun getSlots(): List<RecipeSlot> {
        val isShiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)
        if (!isShiftDown) {
            if (!wasShiftDown) {
                rotation += lastTimestamp.elapsedNow().toDouble(DurationUnit.SECONDS)
            }
            lastTimestamp = TimeSource.Monotonic.markNow()
        }
        wasShiftDown = isShiftDown
        return basicIngredients.mapIndexed { index, ingredient ->
            val (x, y) = positionOnCircle(index, basicIngredients.size)
            RecipeSlot(x - 18 / 2, y - 18 / 2, ingredient.itemStack)
        } + listOf(RecipeSlot(circleCenter.first - 9, circleCenter.second - 9, outputPet.itemStack))
    }

    override fun getType(): RecipeType = RecipeType.KAT_UPGRADE

    override fun hasVariableCost(): Boolean = false

    override fun serialize(): JsonObject {
        return JsonObject().apply {
            addProperty("coins", coins)
            addProperty("input", inputPet.serialize())
            addProperty("output", outputPet.serialize())
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
                recipe["coins"].asLong
            )
        }
    }

    override fun getBackground(): ResourceLocation {
        return ResourceLocation("notenoughupdates:textures/gui/katting_tall.png")
    }
}
