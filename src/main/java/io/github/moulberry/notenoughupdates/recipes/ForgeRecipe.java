package io.github.moulberry.notenoughupdates.recipes;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
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

public class ForgeRecipe implements NeuRecipe {

    private final NEUManager manager;
    private final List<Ingredient> inputs;
    private final JsonObject output;
    private final ItemStack outputItem;
    private List<RecipeSlot> slots;

    public ForgeRecipe(NEUManager manager, List<Ingredient> inputs, JsonObject output) {
        this.manager = manager;
        this.inputs = inputs;
        this.output = output;
        this.outputItem = manager.jsonToStack(output);
    }

    @Override
    public Set<Ingredient> getIngredients() {
        return Sets.newHashSet(inputs);
    }

    @Override
    public Set<String> getOutputs() {
        return Collections.singleton(manager.getInternalNameForItem(outputItem));
    }

    @Override
    public List<RecipeSlot> getSlots() {
        if (slots != null) return slots;
        slots = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            Ingredient input = inputs.get(i);
            ItemStack itemStack = input.getItemStack();
            if (itemStack == null) continue;
            int x = i % 3;
            int y = i / 3;
            slots.add(new RecipeSlot(30 + x * GuiItemRecipe.SLOT_SPACING, 17 + y * GuiItemRecipe.SLOT_SPACING, itemStack));
        }
        slots.add(new RecipeSlot(124, 35, outputItem));
        return slots;
    }

    @Override
    public void drawExtraInfo(GuiItemRecipe gui) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        Utils.drawStringCenteredScaledMaxWidth("Forge Recipe", fontRenderer, gui.guiLeft + 132, gui.guiTop + 25, false, 75, 0xff00ff);
    }

    static ForgeRecipe parseForgeRecipe(NEUManager manager, JsonObject recipe, JsonObject output) {
        List<Ingredient> ingredients = new ArrayList<>();
        for (JsonElement element : recipe.getAsJsonArray("inputs")) {
            String ingredientString = element.getAsString();
            ingredients.add(new Ingredient(manager, ingredientString));
        }
        return new ForgeRecipe(manager, ingredients, output);
    }
}
