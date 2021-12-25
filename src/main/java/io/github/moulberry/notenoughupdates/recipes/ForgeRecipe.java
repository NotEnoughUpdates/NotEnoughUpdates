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
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class ForgeRecipe implements NeuRecipe {

    private static final ResourceLocation BACKGROUND = new ResourceLocation("notenoughupdates", "textures/gui/forge_recipe.png");

    private static final int SLOT_IMAGE_U = 176;
    private static final int SLOT_IMAGE_V = 0;
    private static final int SLOT_IMAGE_SIZE = 18;
    private static final int SLOT_PADDING = 1;
    private static final int EXTRA_INFO_MAX_WIDTH = 75;
    public static final int EXTRA_INFO_X = 132;
    public static final int EXTRA_INFO_Y = 25;

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
    public ResourceLocation getBackground() {
        return BACKGROUND;
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
            int[] slotCoordinates = getSlotCoordinates(i, inputs.size());
            slots.add(new RecipeSlot(slotCoordinates[0], slotCoordinates[1], itemStack));
        }
        slots.add(new RecipeSlot(124, 35, output.getItemStack()));
        return slots;
    }

    @Override
    public void drawExtraBackground(GuiItemRecipe gui) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND);
        for (int i = 0; i < inputs.size(); i++) {
            int[] slotCoordinates = getSlotCoordinates(i, inputs.size());
            gui.drawTexturedModalRect(
                    gui.guiLeft + slotCoordinates[0] - SLOT_PADDING, gui.guiTop + slotCoordinates[1] - SLOT_PADDING,
                    SLOT_IMAGE_U, SLOT_IMAGE_V,
                    SLOT_IMAGE_SIZE, SLOT_IMAGE_SIZE);
        }
    }

    @Override
    public void drawExtraInfo(GuiItemRecipe gui) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        if (timeInSeconds > 0)
            Utils.drawStringCenteredScaledMaxWidth(formatDuration(timeInSeconds), fontRenderer, gui.guiLeft + EXTRA_INFO_X, gui.guiTop + EXTRA_INFO_Y, false, EXTRA_INFO_MAX_WIDTH, 0xff00ff);
    }

    @Override
    public void drawHoverInformation(GuiItemRecipe gui, int mouseX, int mouseY) {
        manager.hotm.getInformationOnCurrentProfile().ifPresent(hotmTree -> {
            if (timeInSeconds > 0 && gui.isWithinRect(
                    mouseX, mouseY,
                    gui.guiLeft + EXTRA_INFO_X - EXTRA_INFO_MAX_WIDTH / 2,
                    gui.guiTop + EXTRA_INFO_Y - 8,
                    EXTRA_INFO_MAX_WIDTH, 16
            )) {
                int qf = hotmTree.getLevel("forge_time");
                int reducedTime = getReducedTime(qf);
                if (qf > 0) {

                    Utils.drawHoveringText(Arrays.asList(EnumChatFormatting.YELLOW + formatDuration(reducedTime) + " with Quick Forge (Level " + qf + ")"), mouseX, mouseY, gui.width, gui.height, 500, Minecraft.getMinecraft().fontRendererObj);
                }
            }
        });
    }

    public int getReducedTime(int quickForgeUpgradeLevel) {
        if (quickForgeUpgradeLevel <= 0) return timeInSeconds;
        double reduction = Math.round(quickForgeUpgradeLevel * 5 + 100) / 1000.0;
        return (int) (timeInSeconds - timeInSeconds * reduction);
    }

    @Override
    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        JsonArray ingredients = new JsonArray();
        for (Ingredient input : inputs) {
            ingredients.add(new JsonPrimitive(input.serialize()));
        }
        object.addProperty("type", "forge");
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

    private static final int RECIPE_CENTER_X = 40;
    private static final int RECIPE_CENTER_Y = 34;
    private static final int SLOT_DISTANCE_FROM_CENTER = 22;
    private static final int RECIPE_FALLBACK_X = 20;
    private static final int RECIPE_FALLBACK_Y = 15;

    static int[] getSlotCoordinates(int slotNumber, int totalSlotCount) {
        if (totalSlotCount > 6) {
            return new int[]{
                    RECIPE_FALLBACK_X + (slotNumber % 4) * GuiItemRecipe.SLOT_SPACING,
                    RECIPE_FALLBACK_Y + (slotNumber / 4) * GuiItemRecipe.SLOT_SPACING,
            };
        }
        if (totalSlotCount == 1) {
            return new int[] {
                    RECIPE_CENTER_X - GuiItemRecipe.SLOT_SIZE / 2,
                    RECIPE_CENTER_Y - GuiItemRecipe.SLOT_SIZE / 2
            };
        }
        double rad = Math.PI * 2 * slotNumber / totalSlotCount;
        int x = (int) (Math.cos(rad) * SLOT_DISTANCE_FROM_CENTER);
        int y = (int) (Math.sin(rad) * SLOT_DISTANCE_FROM_CENTER);
        return new int[]{RECIPE_CENTER_X + x, RECIPE_CENTER_Y + y};
    }

    static String formatDuration(int seconds) {
        int minutes = seconds / 60;
        seconds %= 60;
        int hours = minutes / 60;
        minutes %= 60;
        int days = hours / 24;
        hours %= 24;
        StringBuilder sB = new StringBuilder();
        if (days != 0) sB.append(days).append("d ");
        if (hours != 0) sB.append(hours).append("h ");
        if (minutes != 0) sB.append(minutes).append("m ");
        if (seconds != 0) sB.append(seconds).append("s ");
        return sB.substring(0, sB.length() - 1);
    }
}
