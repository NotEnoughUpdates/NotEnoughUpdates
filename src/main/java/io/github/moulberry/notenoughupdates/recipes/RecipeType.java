package io.github.moulberry.notenoughupdates.recipes;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import net.minecraft.util.ResourceLocation;

public enum RecipeType {
    CRAFTING("crafting", "Crafting", CraftingRecipe::parseCraftingRecipe),
    FORGE("forge", "Forge", ForgeRecipe::parseForgeRecipe),
    TRADE("trade", "Trade", VillagerTradeRecipe::parseStaticRecipe),
    MOB_LOOT("drops", "Mob Loot", MobLootRecipe::parseRecipe);

    private final String id;
    private final String label;
    private final RecipeFactory recipeFactory;
    private final ResourceLocation icon;

    RecipeType(String id, String label, RecipeFactory recipeFactory) {
        this.id = id;
        this.label = label;
        this.recipeFactory = recipeFactory;
        this.icon = new ResourceLocation("notenoughupdates", "textures/icons/recipe_" + id + ".png");
    }

    public String getId() {
        return id;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public String getLabel() {
        return label;
    }

    public RecipeFactory getRecipeFactory() {
        return recipeFactory;
    }

    public NeuRecipe createRecipe(NEUManager manager, JsonObject recipe, JsonObject outputItemJson) {
        return recipeFactory.createRecipe(manager, recipe, outputItemJson);
    }

    public static RecipeType getRecipeTypeForId(String id) {
        for (RecipeType value : values()) {
            if (value.id.equals(id)) return value;
        }
        return null;
    }

    @FunctionalInterface
    interface RecipeFactory {
        NeuRecipe createRecipe(NEUManager manager, JsonObject recipe, JsonObject outputItemJson);
    }
}
