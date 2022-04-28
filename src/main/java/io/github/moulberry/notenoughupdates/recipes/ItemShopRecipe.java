package io.github.moulberry.notenoughupdates.recipes;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import io.github.moulberry.notenoughupdates.util.JsonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemShopRecipe implements NeuRecipe {
	public static ResourceLocation BACKGROUND = new ResourceLocation(
		"notenoughupdates",
		"textures/gui/item_shop_recipe.png"
	);

	private static final int SLOT_IMAGE_U = 176;
	private static final int SLOT_IMAGE_V = 0;
	private static final int SLOT_IMAGE_SIZE = 18;
	public static final int RESULT_SLOT_Y = 66;
	public static final int RESULT_SLOT_X = 124;

	public static final int BUTTON_X = 20;
	public static final int BUTTON_Y = 100;
	private final List<Ingredient> cost;
	private final Ingredient result;

	private final Ingredient npcIngredient;
	private final JsonObject npcObject;
	private final GuiButton trackButton = new GuiButton(0, 80, 36, 65, 15, "Uninitialized");

	public ItemShopRecipe(Ingredient npcIngredient, List<Ingredient> cost, Ingredient result, JsonObject npcObject) {
		this.npcIngredient = npcIngredient;
		this.cost = cost;
		this.result = result;
		this.npcObject = npcObject;
	}

	@Override
	public Set<Ingredient> getCatalystItems() {
		return Sets.newHashSet(npcIngredient);
	}

	@Override
	public Set<Ingredient> getIngredients() {
		return new HashSet<>(cost);
	}

	@Override
	public Set<Ingredient> getOutputs() {
		return Sets.newHashSet(result);
	}

	@Override
	public List<RecipeSlot> getSlots() {
		List<RecipeSlot> slots = new ArrayList<>();
		int rowCount = cost.size() / 3;
		int startX = 36;
		int startY = 66 - rowCount * 8;
		int i = 0;
		for (Ingredient ingredient : cost) {
			int x = i % 3;
			int y = i / 3;
			slots.add(new RecipeSlot(startX + x * 18, startY + y * 18, ingredient.getItemStack()));
			i++;
		}
		slots.add(new RecipeSlot(RESULT_SLOT_X, RESULT_SLOT_Y, result.getItemStack()));
		return slots;
	}

	public GuiButton getAndUpdateTrackButton() {
		if (NotEnoughUpdates.INSTANCE.navigation.getTrackedWaypoint() == npcObject) {
			trackButton.displayString = EnumChatFormatting.GREEN + "NPC Tracked";
			trackButton.enabled = false;
		} else {
			trackButton.displayString = "Track NPC";
			trackButton.enabled = true;
		}
		return trackButton;
	}

	@Override
	public List<GuiButton> getExtraButtons(GuiItemRecipe guiItemRecipe) {
		trackButton.xPosition = BUTTON_X + guiItemRecipe.guiLeft;
		trackButton.yPosition = BUTTON_Y + guiItemRecipe.guiTop;
		return Arrays.asList(new GuiButton[]{getAndUpdateTrackButton()});
	}

	@Override
	public void actionPerformed(GuiButton button) {
		if (button == trackButton) {
			NotEnoughUpdates.INSTANCE.navigation.trackWaypoint(npcObject);
			getAndUpdateTrackButton();
		}
	}

	@Override
	public void drawExtraBackground(GuiItemRecipe gui, int mouseX, int mouseY) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND);
		int rowCount = cost.size() / 3;
		int startX = 36 - 1;
		int startY = 66 - rowCount * 8 - 1;
		for (int i = 0; i < cost.size(); i++) {
			int x = i % 3;
			int y = i / 3;

			gui.drawTexturedModalRect(
				gui.guiLeft + startX + x * 18,
				gui.guiTop + startY + y * 18,
				SLOT_IMAGE_U, SLOT_IMAGE_V,
				SLOT_IMAGE_SIZE, SLOT_IMAGE_SIZE
			);
		}

	}

	@Override
	public RecipeType getType() {
		return RecipeType.NPC_SHOP;
	}

	@Override
	public boolean hasVariableCost() {
		return false;
	}

	@Override
	public JsonObject serialize() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", "npc_shop");
		jsonObject.addProperty("result", result.serialize());
		jsonObject.add(
			"cost",
			JsonUtils.transformListToJsonArray(cost, costItem -> new JsonPrimitive(costItem.serialize()))
		);
		return jsonObject;
	}

	@Override
	public ResourceLocation getBackground() {
		return BACKGROUND;
	}

	public static NeuRecipe parseItemRecipe(NEUManager neuManager, JsonObject recipe, JsonObject outputItemJson) {
		return new ItemShopRecipe(
			new Ingredient(neuManager, outputItemJson.get("internalname").getAsString()),
			JsonUtils.transformJsonArrayToList(
				recipe.getAsJsonArray("cost"),
				it -> new Ingredient(neuManager, it.getAsString())
			),
			new Ingredient(neuManager, recipe.get("result").getAsString()),
			outputItemJson
		);
	}
}
