package io.github.moulberry.notenoughupdates.recipes;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import net.minecraft.item.ItemStack;

public class Ingredient {

    private final int count;
    private final String internalItemId;
    private final NEUManager manager;
    private ItemStack itemStack;

    public Ingredient(NEUManager manager, String ingredientIdentifier) {
        this.manager = manager;
        String[] parts = ingredientIdentifier.split(":");
        internalItemId = parts[0];
        if (parts.length == 2) {
            count = Integer.parseInt(parts[1]);
        } else if (parts.length == 1) {
            count = 1;
        } else {
            throw new IllegalArgumentException("Could not parse ingredient " + ingredientIdentifier);
        }
    }

    public int getCount() {
        return count;
    }

    public String getInternalItemId() {
        return internalItemId;
    }

    public ItemStack getItemStack() {
        if (itemStack != null) return itemStack;
        JsonObject itemInfo = manager.getItemInformation().get(internalItemId);
        itemStack = manager.jsonToStack(itemInfo);
        itemStack.stackSize = count;
        return itemStack;
    }

}
