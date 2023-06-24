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
import io.github.moulberry.notenoughupdates.ItemPriceInformation;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EssenceUpgrades implements NeuRecipe {

	private static final ResourceLocation BACKGROUND = new ResourceLocation("notenoughupdates", "textures/gui/essence_upgrades_tall.png");
	private static final List<RenderLocation> buttonLocations = new ArrayList<RenderLocation>() {{
		add(new RenderLocation(20, 20));
		add(new RenderLocation(40, 20));
		add(new RenderLocation(60, 20));
		add(new RenderLocation(80, 20));
		add(new RenderLocation(100, 20));
		add(new RenderLocation(120, 20));
		add(new RenderLocation(140, 20));

		add(new RenderLocation(10, 40));
		add(new RenderLocation(30, 40));
		add(new RenderLocation(50, 40));
		add(new RenderLocation(70, 40));
		add(new RenderLocation(90, 40));
		add(new RenderLocation(110, 40));
		add(new RenderLocation(130, 40));
		add(new RenderLocation(150, 40));
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
	private static final int outputX = 124;
	private static final int outputY = 66;
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
						String itemString = element.getAsString();

						int colon = itemString.indexOf(':');
						if (colon != -1) {
							String amount = itemString.substring(colon + 1);
							String requiredItem = itemString.substring(0, colon);

							items.put(requiredItem, Integer.parseInt(amount));
						}
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
		NEUManager manager = NotEnoughUpdates.INSTANCE.manager;
		List<RecipeSlot> slotList = new ArrayList<>();

		//output item
		String internalName = output.getInternalItemId();
		if (internalName == null) {
			return slotList;
		}
		List<String> lore = ItemUtils.getLore(initialItemStack);
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
		ItemUtils.setLore(output.getItemStack(), newLore);
		output.getItemStack().setStackDisplayName(
			initialItemStack.getDisplayName() + " " + Utils.getStarsString(selectedTier));
		slotList.add(new RecipeSlot(outputX, outputY, output.getItemStack()));

		//other required items and/or coins, if applicable
		TierUpgrade tierUpgrade = tierUpgradeMap.get(selectedTier);
		if (tierUpgrade == null) {
			return slotList;
		}

		//required essence
		String essenceInternalName = "ESSENCE_" + tierUpgrade.getEssenceType().toUpperCase(Locale.ROOT);
		if (manager.isValidInternalName(essenceInternalName)) {
			ItemStack essenceItemStack =
				manager.createItemResolutionQuery().withKnownInternalName(essenceInternalName).resolveToItemStack();
			if (essenceItemStack != null) {
				essenceItemStack = essenceItemStack.copy();
				essenceItemStack.setStackDisplayName(
					EnumChatFormatting.AQUA + StringUtils.formatNumber(tierUpgrade.getEssenceRequired()) + " " + EnumChatFormatting.DARK_GRAY +
						tierUpgrade.getEssenceType() + " Essence");

				essenceItemStack.getTagCompound().setInteger(
					ItemPriceInformation.STACKSIZE_OVERRIDE,
					tierUpgrade.getEssenceRequired()
				);
				RenderLocation renderLocation = slotLocations.get(0);
				slotList.add(new RecipeSlot(renderLocation.getX() + 1, renderLocation.getY() + 1, essenceItemStack));
			}
		}

		int i = 1;
		if (tierUpgrade.getItemsRequired() != null) {
			for (Map.Entry<String, Integer> requiredItem : tierUpgrade.getItemsRequired().entrySet()) {
				ItemStack itemStack;
				if (requiredItem.getKey().equals("SKYBLOCK_COIN")) {
					Ingredient ingredient = Ingredient.coinIngredient(
						manager,
						requiredItem.getValue()
					);
					itemStack = ingredient.getItemStack();
				} else {
					itemStack = manager.createItemResolutionQuery().withKnownInternalName(
						requiredItem.getKey()).resolveToItemStack();
					if (itemStack != null) {
						itemStack.stackSize = requiredItem.getValue();
					}
				}
				if (itemStack != null) {
					RenderLocation renderLocation = slotLocations.get(i++);
					if (renderLocation != null) {
						slotList.add(new RecipeSlot(renderLocation.getX() + 1, renderLocation.getY()+1, itemStack));
					}
				}
			}
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
				34 / 256f,
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
			if (i >= buttonLocations.size()) {
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.RED + "[NEU] Error: Item has more than " + buttonLocations.size() +
						" possible star upgrades"));
				break;
			}
			RenderLocation buttonLocation = buttonLocations.get(i);

			int x = guiItemRecipe.guiLeft + buttonLocation.getX();
			int y = guiItemRecipe.guiTop + buttonLocation.getY();

			if (Mouse.getEventButtonState() && Utils.isWithinRect(mouseX, mouseY, x, y, 16, 16)) {
				selectedTier = i + 1;
				slots = buildSlotList();
			}

			Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND);
			GlStateManager.color(1, 1, 1, 1);
			drawButton(x, y, i + 1 == selectedTier);
			Utils.drawStringCentered(String.valueOf(i + 1), x + 8, y + 9, false, 0x2d4ffc);
		}
	}

	private void drawSlots(int amount) {
		//-1 to not count the output slot
		for (int i = 0; i < amount - 1; i++) {
			RenderLocation renderLocation = slotLocations.get(i);
			if (renderLocation != null && guiItemRecipe != null) {
				drawSlot(guiItemRecipe.guiLeft + renderLocation.getX(), guiItemRecipe.guiTop + renderLocation.getY());
			}
		}
	}

	@Override
	public void drawExtraInfo(GuiItemRecipe gui, int mouseX, int mouseY) {
		guiItemRecipe = gui;
		if (slots.isEmpty()) {
			slots = buildSlotList();
		}
		drawButtons(mouseX, mouseY);
	}

	@Override
	public void drawExtraBackground(GuiItemRecipe gui, int mouseX, int mouseY) {
		drawSlots(slots.size());
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.isRepeatEvent()) {
			return;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) && selectedTier > 1) {
			selectedTier--;
			slots = buildSlotList();
		} else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) && selectedTier < amountOfTiers) {
			selectedTier++;
			slots = buildSlotList();
		} else if (Keyboard.isKeyDown(Keyboard.KEY_0) || Keyboard.isKeyDown(Keyboard.KEY_NUMPAD0)) {
			//cycle through tiers when pressing 0
			if (selectedTier < amountOfTiers) {
				selectedTier++;
			} else {
				selectedTier = 1;
			}
		}

		char pressedKey = Keyboard.getEventCharacter();
		if (Character.isDigit(pressedKey)) {
			//convert to number from 1-9
			pressedKey -= 48;
			if (pressedKey > 0 && pressedKey <= amountOfTiers) {
				selectedTier = pressedKey;
				slots = buildSlotList();
			}
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
	public @Nullable JsonObject serialize() {
		return null;
	}

	@Override
	public ResourceLocation getBackground() {
		return BACKGROUND;
	}

	/**
	 * Simple dataclass holding an x and y value to be used when describing the location of something to be rendered
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
	 * Dataclass holding information about the items and essence required to upgrade an item to a specific tier
	 */
	public static class TierUpgrade {
		private final int tier;
		private final String essenceType;
		private final int essenceRequired;
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
