package io.github.moulberry.notenoughupdates.recipes;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CraftingRecipe implements NeuRecipe {

    private static final int EXTRA_STRING_X = 132;
    private static final int EXTRA_STRING_Y = 25;

    private final NEUManager manager;
    private final Ingredient[] inputs;
    private final String extraText;
    private final Ingredient outputIngredient;
    private List<RecipeSlot> slots;

    public CraftingRecipe(NEUManager manager, Ingredient[] inputs, Ingredient output, String extra) {
        this.manager = manager;
        this.inputs = inputs;
        this.outputIngredient = output;
        this.extraText = extra;
        if (inputs.length != 9)
            throw new IllegalArgumentException("Cannot construct crafting recipe with non standard crafting grid size");
    }

    @Override
    public Set<Ingredient> getIngredients() {
        Set<Ingredient> ingredients = Sets.newHashSet(inputs);
        ingredients.remove(null);
        return ingredients;
    }

    @Override
    public Set<Ingredient> getOutputs() {
        return Collections.singleton(getOutput());
    }

    public Ingredient getOutput() {
        return outputIngredient;
    }

    public Ingredient[] getInputs() {
        return inputs;
    }

    @Override
    public List<RecipeSlot> getSlots() {
        if (slots != null) return slots;
        slots = new ArrayList<>();
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                Ingredient input = inputs[x + y * 3];
                if (input == null) continue;
                ItemStack item = input.getItemStack();
                if (item == null) continue;
                slots.add(new RecipeSlot(30 + x * GuiItemRecipe.SLOT_SPACING, 17 + y * GuiItemRecipe.SLOT_SPACING, item));
            }
        }
        slots.add(new RecipeSlot(124, 35, outputIngredient.getItemStack()));
        return slots;
    }

    public String getCraftText() {
        return extraText;
    }

    @Override
    public void drawExtraInfo(GuiItemRecipe gui) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        String craftingText = getCraftText();
        if (craftingText != null)
            Utils.drawStringCenteredScaledMaxWidth(craftingText, fontRenderer,
                    gui.guiLeft + EXTRA_STRING_X, gui.guiTop + EXTRA_STRING_Y, false, 75, 0x404040);
    }

    public static CraftingRecipe parseCraftingRecipe(NEUManager manager, JsonObject recipe, JsonObject outputItem) {
        Ingredient[] craftMatrix = new Ingredient[9];

        String[] x = {"1", "2", "3"};
        String[] y = {"A", "B", "C"};
        for (int i = 0; i < 9; i++) {
            String name = y[i / 3] + x[i % 3];
            if (!recipe.has(name)) continue;
            String item = recipe.get(name).getAsString();
            if (item == null || item.isEmpty()) continue;
            craftMatrix[i] = new Ingredient(manager, item);
        }
        int resultCount = 1;
        if (recipe.has("count"))
            resultCount = recipe.get("count").getAsInt();
        String extra = null;
        if (outputItem.has("crafttext"))
            extra = outputItem.get("crafttext").getAsString();
        if (recipe.has("crafttext"))
            extra = recipe.get("crafttext").getAsString();
        String outputItemId = outputItem.get("internalname").getAsString();
        return new CraftingRecipe(manager, craftMatrix, new Ingredient(manager, outputItemId, resultCount), extra);
    }
}
