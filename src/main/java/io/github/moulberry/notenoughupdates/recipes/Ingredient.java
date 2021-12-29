package io.github.moulberry.notenoughupdates.recipes;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Ingredient {

    public static final String SKYBLOCK_COIN = "SKYBLOCK_COIN";
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

    public Ingredient(NEUManager manager, String internalItemId, int count) {
        this.manager = manager;
        this.count = count;
        this.internalItemId = internalItemId;
    }

    private Ingredient(NEUManager manager, int coinValue) {
        this.manager = manager;
        this.internalItemId = SKYBLOCK_COIN;
        this.count = coinValue;
    }

    public static Set<Ingredient> mergeIngredients(Iterable<Ingredient> ingredients) {
        Map<String, Ingredient> newIngredients = new HashMap<>();
        for (Ingredient i : ingredients) {
            newIngredients.merge(i.getInternalItemId(), i, (a, b) -> new Ingredient(i.manager, i.internalItemId, a.count + b.count));
        }
        return new HashSet<>(newIngredients.values());
    }

    public static Ingredient coinIngredient(NEUManager manager, int coins) {
        return new Ingredient(manager, coins);
    }

    public boolean isCoins() {
        return "SKYBLOCK_COIN".equals(internalItemId);
    }

    public int getCount() {
        return count;
    }

    public String getInternalItemId() {
        return internalItemId;
    }

    public ItemStack getItemStack() {
        if (itemStack != null) return itemStack;
        if(isCoins()) {
            itemStack = new ItemStack(Items.gold_nugget);
            itemStack.setStackDisplayName("\u00A7r\u00A76" + Utils.formatNumberWithDots(getCount()) + " Coins");
            return itemStack;
        }
        JsonObject itemInfo = manager.getItemInformation().get(internalItemId);
        itemStack = manager.jsonToStack(itemInfo);
        itemStack.stackSize = count;
        return itemStack;
    }

    public String serialize() {
        return internalItemId + ":" + count;
    }
}
