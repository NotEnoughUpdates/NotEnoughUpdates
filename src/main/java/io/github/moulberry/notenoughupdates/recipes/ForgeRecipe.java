package io.github.moulberry.notenoughupdates.recipes;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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

    public enum ForgeType {
        REFINING, ITEM_FORGING
    }

    private final NEUManager manager;
    private final List<Ingredient> inputs;
    private final Ingredient output;
    private final int hotmLevel;
    private final int timeInSeconds; // TODO: quick forge
    private List<RecipeSlot> slots;

    public ForgeRecipe(NEUManager manager, List<Ingredient> inputs, Ingredient output, int durationInSeconds, int hotmLevel) {
        this.manager = manager;
        this.inputs = inputs;
        this.output = output;
        this.hotmLevel = hotmLevel;
        this.timeInSeconds = durationInSeconds;
    }

    public List<Ingredient> getInputs() {
        return inputs;
    }

    public Ingredient getOutput() {
        return output;
    }

    public int getHotmLevel() {
        return hotmLevel;
    }

    public int getTimeInSeconds() {
        return timeInSeconds;
    }

    @Override
    public Set<Ingredient> getIngredients() {
        return Sets.newHashSet(inputs);
    }

    @Override
    public Set<Ingredient> getOutputs() {
        return Collections.singleton(output);
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
        slots.add(new RecipeSlot(124, 35, output.getItemStack()));
        return slots;
    }

    @Override
    public void drawExtraInfo(GuiItemRecipe gui) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        Utils.drawStringCenteredScaledMaxWidth("Forge Recipe", fontRenderer, gui.guiLeft + 132, gui.guiTop + 25, false, 75, 0xff00ff);
    }

    @Override
    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        JsonArray ingredients = new JsonArray();
        for (Ingredient input : inputs) {
            ingredients.add(new JsonPrimitive(input.serialize()));
        }
        object.add("inputs", ingredients);
        object.addProperty("count", output.getCount());
        object.addProperty("overrideOutputId", output.getInternalItemId());
        if (hotmLevel >= 0)
            object.addProperty("hotmLevel", hotmLevel);
        if (timeInSeconds >= 0)
            object.addProperty("duration", timeInSeconds);
        return object;
    }

    static ForgeRecipe parseForgeRecipe(NEUManager manager, JsonObject recipe, JsonObject output) {
        List<Ingredient> ingredients = new ArrayList<>();
        for (JsonElement element : recipe.getAsJsonArray("inputs")) {
            String ingredientString = element.getAsString();
            ingredients.add(new Ingredient(manager, ingredientString));
        }
        String internalItemId = output.get("internalname").getAsString();
        if (recipe.has("overrideOutputId"))
            internalItemId = recipe.get("overrideOutputId").getAsString();
        int resultCount = 1;
        if (recipe.has("count")) {
            resultCount = recipe.get("count").getAsInt();
        }
        int duration = -1;
        if (recipe.has("duration")) {
            duration = recipe.get("duration").getAsInt();
        }
        int hotmLevel = -1;
        if (recipe.has("hotmLevel")) {
            hotmLevel = recipe.get("hotmLevel").getAsInt();
        }
        return new ForgeRecipe(manager, ingredients, new Ingredient(manager, internalItemId, resultCount), duration, hotmLevel);
    }
}
