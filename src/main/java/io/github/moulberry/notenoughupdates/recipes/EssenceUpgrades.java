/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EssenceUpgrades implements NeuRecipe {

	private static final ResourceLocation BACKGROUND = new ResourceLocation("notenoughupdates", "textures/gui/aaa.png");
	private static final List<RenderLocation> buttonLocations = new ArrayList<RenderLocation>() {{
		add(new RenderLocation(20, 20));
		add(new RenderLocation(40, 20));
		add(new RenderLocation(60, 20));
		add(new RenderLocation(80, 20));
		add(new RenderLocation(100, 20));
		add(new RenderLocation(120, 20));
		add(new RenderLocation(140, 20));

		add(new RenderLocation(60, 40));
		add(new RenderLocation(80, 40));
		add(new RenderLocation(100, 40));
	}};

	private static final List<RenderLocation> slotLocations = new ArrayList<RenderLocation>() {{
		add(new RenderLocation(20, 60));
		add(new RenderLocation(45, 60));
		add(new RenderLocation(70, 60));

		add(new RenderLocation(20, 85));
		add(new RenderLocation(45, 85));
		add(new RenderLocation(70, 85));

		add(new RenderLocation(20, 110));
		add(new RenderLocation(45, 110));
		add(new RenderLocation(70, 110));
	}};

	private static final Pattern loreStatPattern = Pattern.compile("^.+: §.\\+(?<value>[\\d.]+).*$");

	private final Ingredient output;
	private final ItemStack initialItemStack;
	private final Map<Integer, TierUpgrade> tierUpgradeMap;
	private final int amountOfTiers;
	private int selectedTier;
	private static final int outputX = 123;
	private static final int outputY = 65;
	private List<RecipeSlot> slots;
	private GuiItemRecipe guiItemRecipe;

	public EssenceUpgrades(Ingredient output, Map<Integer, TierUpgrade> tierUpgradeMap) {
		this.output = output;
		this.tierUpgradeMap = tierUpgradeMap;

		initialItemStack = output.getItemStack().copy();
		amountOfTiers = tierUpgradeMap.keySet().size();
		selectedTier = amountOfTiers;
		slots = new ArrayList<>();
	}

	/**
	 * Parses an entry from essencecosts.json to a NeuRecipe, containing information on how to upgrade the item with Essence
	 *
	 * @param entry Entry from essencecosts.json
	 * @return parsed NeuRecipe
	 * @see Constants#parseEssenceCosts()
	 */
	public static @Nullable NeuRecipe parseFromEssenceCostEntry(Map.Entry<String, JsonElement> entry) {
		NEUManager manager = NotEnoughUpdates.INSTANCE.manager;
		if (!manager.isValidInternalName(entry.getKey())) {
			System.err.println("Invalid internalname: " + entry.getKey());
			return null;
		}

		String internalName = entry.getKey();
		JsonObject jsonObject = entry.getValue().getAsJsonObject();

		Ingredient output = new Ingredient(manager, internalName);

		if (!jsonObject.has("type")) {
			System.err.println("Invalid essence entry for: " + internalName);
			System.err.println("Missing: Essence type");
			return null;
		}
		String essenceType = jsonObject.get("type").getAsString();

		Map<Integer, TierUpgrade> upgradeMap = new HashMap<>();
		for (Map.Entry<String, JsonElement> entries : jsonObject.entrySet()) {
			if (StringUtils.isNumeric(entries.getKey())) {
				int tier = Integer.parseInt(entries.getKey());
				int essenceCost = Integer.parseInt(entries.getValue().getAsString());
				upgradeMap.put(tier, new TierUpgrade(tier, essenceType, essenceCost, null));
			} else if (entries.getKey().equals("items")) {
				for (Map.Entry<String, JsonElement> requiredItems : entries
					.getValue()
					.getAsJsonObject()
					.entrySet()) {
					Integer tier = Integer.parseInt(requiredItems.getKey());
					Map<String, Integer> items = new HashMap<>();
					for (JsonElement element : requiredItems.getValue().getAsJsonArray()) {
						//todo switch to new things
						if(true){
							continue;
						}

						String[] item = element.getAsString().split("x");
						String amount = item[item.length - 1];
						String itemName = element.getAsString().substring(0, element.getAsString().length() - amount.length() - 1);

						items.put(itemName, Integer.parseInt(amount));
					}
					upgradeMap.get(tier).itemsRequired = items;
				}
			}
		}
		return new EssenceUpgrades(output, upgradeMap);
	}

	/**
	 * Builds a list containing all the RecipeSlots that should be rendered right now:
	 * <ul>
	 *   <li>The output</li>
	 *   <li>The ingredients</li>
	 * </ul>
	 *
	 * @return the list of RecipeSlots
	 * @see EssenceUpgrades#getSlots()
	 */
	private List<RecipeSlot> buildSlotList() {
		List<RecipeSlot> slotList = new ArrayList<>();

		//output item
		String internalName = output.getInternalItemId();
		if (internalName == null) {
			return slotList;
		}
		List<String> lore = ItemUtils.getLore(initialItemStack);
//		JsonArray lore = outputItem.get("lore").getAsJsonArray();
		List<String> newLore = new ArrayList<>();

		for (String loreEntry : lore) {
			Matcher matcher = loreStatPattern.matcher(loreEntry);
			if (matcher.matches()) {
				String valueString = matcher.group("value");
				if (valueString == null) {
					newLore.add(loreEntry);
					continue;
				}

				float value = Float.parseFloat(valueString);
				int matchStart = matcher.start("value");
				float newValue = value * (1 + (selectedTier / 50f));
				StringBuilder newLine = new StringBuilder(loreEntry.substring(0, matchStart) + String.format("%.1f", newValue));
				if (loreEntry.length() - 1 > matcher.end("value")) {
					newLine.append(loreEntry, matcher.end("value"), loreEntry.length() - 1);
				}

				newLore.add(newLine.toString());
			} else {
				//simply append this entry to the new lore
				newLore.add(loreEntry);
			}
		}
		;
		ItemUtils.setLore(output.getItemStack(), newLore);
		slotList.add(new RecipeSlot(outputX, outputY, output.getItemStack()));

		for (RenderLocation slotLocation : slotLocations) {
			slotList.add(new RecipeSlot(slotLocation.getX() + 1, slotLocation.getY(), new ItemStack(Items.item_frame)));
		}

		return slotList;
	}

	/**
	 * Draws an empty slot texture at the specified location
	 *
	 * @param x     x location
	 * @param y     y location
	 */
	private void drawSlot(int x, int y) {
		GlStateManager.color(1, 1, 1, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND);
		Utils.drawTexturedRect(
			x,
			y,
			18,
			18,
			176 / 256f,
			194 / 256f,
			0 / 256f,
			18 / 256f
		);
	}

	/**
	 * Draws a button using a part of the texture
	 *
	 * @param x        x location
	 * @param y        y location
	 * @param selected whether the button should look like its pressed down or not
	 */
	private void drawButton(int x, int y, boolean selected) {
		if (selected) {
			Utils.drawTexturedRect(
				x,
				y,
				16,
				16,
				176 / 256f,
				192 / 256f,
				35 / 256f,
				50 / 256f
			);
		} else {
			Utils.drawTexturedRect(
				x,
				y,
				16,
				16,
				176 / 256f,
				192 / 256f,
				18 / 256f,
				34 / 256f
			);
		}
	}

	/**
	 * Draws all Buttons applicable for the item and checks if a button has been clicked on
	 *
	 * @see EssenceUpgrades#buttonLocations
	 */
	private void drawButtons(int mouseX, int mouseY) {
		for (int i = 0; i < amountOfTiers; i++) {
			RenderLocation buttonLocation = buttonLocations.get(i);
			if (buttonLocation == null) {
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.RED + "[NEU] Error: Item has more than 10 possible star upgrades"));
				break;
			}

			int x = guiItemRecipe.guiLeft + buttonLocation.getX();
			int y = guiItemRecipe.guiTop + buttonLocation.getY();

			if (Mouse.getEventButtonState() && Utils.isWithinRect(mouseX, mouseY, x, y, 16, 16)) {
				selectedTier = i + 1;
				slots = buildSlotList();
			}

			Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND);
			GlStateManager.color(1, 1, 1, 1);
			drawButton(x, y, i + 1 == selectedTier);
			Utils.drawStringCentered(
				String.valueOf(i + 1),
				Minecraft.getMinecraft().fontRendererObj,
				x + 8,
				y + 9,
				false,
				0x2d4ffc
			);

//			Minecraft.getMinecraft().fontRendererObj.drawString(String.valueOf(i + 1), x + 5, y + 4, 0x2d4ffc);
		}
	}

	@Override
	public void drawExtraInfo(GuiItemRecipe gui, int mouseX, int mouseY) {
		if (slots.isEmpty()) {
			slots = buildSlotList();
		}
		guiItemRecipe = gui;
//		drawSlot(gui.guiLeft + 50, gui.guiTop + 70);
		drawButtons(mouseX, mouseY);
		for (RenderLocation slotLocation : slotLocations) {
			drawSlot(gui.guiLeft + slotLocation.getX(), gui.guiTop + slotLocation.getY());
		}
	}

	@Override
	public Set<Ingredient> getIngredients() {
		return Collections.singleton(output);
	}

	@Override
	public Set<Ingredient> getOutputs() {
		return Collections.singleton(output);
	}

	@Override
	public List<RecipeSlot> getSlots() {
		return slots;
	}

	@Override
	public RecipeType getType() {
		return RecipeType.ESSENCE_UPGRADES;
	}

	@Override
	public boolean hasVariableCost() {
		return false;
	}

	@Override
	public JsonObject serialize() {
		return null;
	}

	@Override
	public ResourceLocation getBackground() {
		return BACKGROUND;
	}

	/**
	 * Simple dataclass holding an x and y value to be used when describing the location of a button
	 */
	private static class RenderLocation {
		private final int x;
		private final int y;

		public RenderLocation(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
	}

	/**
	 * Dataclass holding information about the items and essence required to upgrade an item to a specifc tier
	 */
	public static class TierUpgrade {
		private int tier;
		private String essenceType;
		private int essenceRequired;
		private Map<String, Integer> itemsRequired;

		public TierUpgrade(int tier, String essenceType, int essenceRequired, Map<String, Integer> itemsRequired) {
			this.tier = tier;
			this.essenceType = essenceType;
			this.essenceRequired = essenceRequired;
			this.itemsRequired = itemsRequired;
		}

		public int getTier() {
			return tier;
		}

		public String getEssenceType() {
			return essenceType;
		}

		public int getEssenceRequired() {
			return essenceRequired;
		}

		public Map<String, Integer> getItemsRequired() {
			return itemsRequired;
		}
	}
}
