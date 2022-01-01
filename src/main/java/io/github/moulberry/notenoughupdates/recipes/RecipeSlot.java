package io.github.moulberry.notenoughupdates.recipes;

import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import net.minecraft.item.ItemStack;

public class RecipeSlot {
    private final int x;
    private final int y;
    private final ItemStack itemStack;

    public RecipeSlot(int x, int y, ItemStack itemStack) {
        this.x = x;
        this.y = y;
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getX(GuiItemRecipe recipe) {
        return recipe.guiLeft + x;
    }

    public int getY(GuiItemRecipe recipe) {
        return recipe.guiTop + y;
    }
}
