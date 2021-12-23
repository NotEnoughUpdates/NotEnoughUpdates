package io.github.moulberry.notenoughupdates.recipes;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Set;

public interface NeuRecipe {
    Set<Ingredient> getIngredients();

    Set<Ingredient> getOutputs();

    List<RecipeSlot> getSlots();

    void drawExtraInfo(GuiItemRecipe gui);

    default void drawExtraBackground(GuiItemRecipe gui) {
    }

    default void drawHoverInformation(GuiItemRecipe gui, int mouseX, int mouseY) {
    }

    JsonObject serialize();

    ResourceLocation getBackground();

    static NeuRecipe parseRecipe(NEUManager manager, JsonObject recipe, JsonObject output) {
        if (recipe.has("type")) {
            switch (recipe.get("type").getAsString().intern()) {
                case "forge":
                    return ForgeRecipe.parseForgeRecipe(manager, recipe, output);
            }
        }
        return CraftingRecipe.parseCraftingRecipe(manager, recipe, output);
    }
}
