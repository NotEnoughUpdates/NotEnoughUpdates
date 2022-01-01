package io.github.moulberry.notenoughupdates.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Debouncer;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeGenerator {
    public static final String DURATION = "Duration: ";
    public static final String COINS_SUFFIX = " Coins";

    private final NotEnoughUpdates neu;

    private final Map<String, String> savedForgingDurations = new HashMap<>();

    private final Debouncer debouncer = new Debouncer(1000 * 1000 * 50 /* 50 ms */);
    private final Debouncer durationDebouncer = new Debouncer(1000 * 1000 * 500);

    public RecipeGenerator(NotEnoughUpdates neu) {
        this.neu = neu;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!neu.config.hidden.enableItemEditing) return;
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (currentScreen == null) return;
        if (!(currentScreen instanceof GuiChest)) return;
        analyzeUI((GuiChest) currentScreen);
    }

    private boolean shouldSaveRecipe() {
        return Keyboard.isKeyDown(Keyboard.KEY_O) && debouncer.trigger();
    }

    public void analyzeUI(GuiChest gui) {
        ContainerChest container = (ContainerChest) gui.inventorySlots;
        IInventory menu = container.getLowerChestInventory();
        String uiTitle = menu.getDisplayName().getUnformattedText();
        EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
        if (uiTitle.startsWith("Item Casting") || uiTitle.startsWith("Refine")) {
            if (durationDebouncer.trigger())
                parseAllForgeItemMetadata(menu);
        }
        boolean saveRecipe = shouldSaveRecipe();
        if (uiTitle.equals("Confirm Process") && saveRecipe) {
            ForgeRecipe recipe = parseSingleForgeRecipe(menu);
            if (recipe == null) {
                p.addChatMessage(new ChatComponentText("" + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + "Could not parse recipe for this UI"));
            } else {
                p.addChatMessage(new ChatComponentText("" + EnumChatFormatting.GREEN + EnumChatFormatting.BOLD + "Parsed recipe:"));
                p.addChatMessage(new ChatComponentText("" + EnumChatFormatting.AQUA + " Inputs:"));
                for (Ingredient i : recipe.getInputs())
                    p.addChatMessage(new ChatComponentText("  - " + EnumChatFormatting.AQUA + i.getInternalItemId() + " x " + i.getCount()));
                p.addChatMessage(new ChatComponentText("" + EnumChatFormatting.AQUA + " Output: " + EnumChatFormatting.GOLD + recipe.getOutput().getInternalItemId() + " x " + recipe.getOutput().getCount()));
                p.addChatMessage(new ChatComponentText("" + EnumChatFormatting.AQUA + " Time: " + EnumChatFormatting.GRAY + recipe.getTimeInSeconds() + " seconds (no QF) ."));
                boolean saved = false;
                try {
                    saved = saveRecipe(recipe);
                } catch (IOException e) {
                }
                if (!saved)
                    p.addChatMessage(new ChatComponentText("" +
                            EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + EnumChatFormatting.OBFUSCATED + "#" +
                            EnumChatFormatting.RESET + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + " ERROR " +
                            EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + EnumChatFormatting.OBFUSCATED + "#" +
                            EnumChatFormatting.RESET + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + " Failed to save recipe. Does the item already exist?"));
            }
        }
    }

    public boolean saveRecipe(NeuRecipe recipe) throws IOException {
        JsonObject recipeJson = recipe.serialize();
        for (Ingredient i : recipe.getOutputs()) {
            if (i.isCoins()) continue;
            JsonObject outputJson = neu.manager.readJsonDefaultDir(i.getInternalItemId() + ".json");
            if (outputJson == null) return false;
            outputJson.addProperty("clickcommand", "viewrecipe");
            JsonArray array = new JsonArray();
            array.add(recipeJson);
            outputJson.add("recipes", array);
            neu.manager.writeJsonDefaultDir(outputJson, i.getInternalItemId() + ".json");
            neu.manager.loadItem(i.getInternalItemId());
        }
        return true;
    }


    public ForgeRecipe parseSingleForgeRecipe(IInventory chest) {
        int durationInSeconds = -1;
        List<Ingredient> inputs = new ArrayList<>();
        Ingredient output = null;
        for (int i = 0; i < chest.getSizeInventory(); i++) {
            int col = i % 9;
            ItemStack itemStack = chest.getStackInSlot(i);
            if (itemStack == null) continue;
            String name = Utils.cleanColour(itemStack.getDisplayName());
            String internalId = neu.manager.getInternalNameForItem(itemStack);
            Ingredient ingredient = null;
            if (itemStack.getDisplayName().endsWith(COINS_SUFFIX)) {
                int coinCost = Integer.parseInt(
                        name.substring(0, name.length() - COINS_SUFFIX.length())
                                .replace(",", ""));
                ingredient = Ingredient.coinIngredient(neu.manager, coinCost);
            } else if (internalId != null) {
                ingredient = new Ingredient(neu.manager, internalId, itemStack.stackSize);
            }
            if (ingredient == null) continue;
            if (col < 4) {
                inputs.add(ingredient);
            } else {
                output = ingredient;
            }
        }
        if (output == null || inputs.isEmpty()) return null;
        if (savedForgingDurations.containsKey(output.getInternalItemId()))
            durationInSeconds = parseDuration(savedForgingDurations.get(output.getInternalItemId()));
        return new ForgeRecipe(neu.manager, new ArrayList<>(Ingredient.mergeIngredients(inputs)), output, durationInSeconds, -1);
    }

    private static Map<Character, Integer> durationSuffixLengthMap = new HashMap<Character, Integer>() {{
        put('d', 60 * 60 * 24);
        put('h', 60 * 60);
        put('m', 60);
        put('s', 1);
    }};

    public int parseDuration(String durationString) {
        String[] parts = durationString.split(" ");
        int timeInSeconds = 0;
        for (String part : parts) {
            char signifier = part.charAt(part.length() - 1);
            int value = Integer.parseInt(part.substring(0, part.length() - 1));
            if (!durationSuffixLengthMap.containsKey(signifier)) {
                return -1;
            }
            timeInSeconds += value * durationSuffixLengthMap.get(signifier);
        }
        return timeInSeconds;
    }

    private void parseAllForgeItemMetadata(IInventory chest) {
        for (int i = 0; i < chest.getSizeInventory(); i++) {
            ItemStack stack = chest.getStackInSlot(i);
            if (stack == null) continue;
            String internalName = neu.manager.getInternalNameForItem(stack);
            if (internalName == null) continue;
            List<String> tooltip = stack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
            String durationInfo = null;
            for (String s : tooltip) {
                String info = Utils.cleanColour(s);
                if (info.startsWith(DURATION)) {
                    durationInfo = info.substring(DURATION.length());
                }
            }
            if (durationInfo != null)
                savedForgingDurations.put(internalName, durationInfo);
        }
    }

}
