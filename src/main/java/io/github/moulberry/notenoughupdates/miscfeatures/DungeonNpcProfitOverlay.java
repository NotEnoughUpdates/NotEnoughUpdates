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

package io.github.moulberry.notenoughupdates.miscfeatures;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonNpcProfitOverlay {

	public static final ResourceLocation dungeonProfitResource =
		new ResourceLocation("notenoughupdates:dungeon_chest_worth.png");

	private final Pattern chestNamePattern = Pattern.compile(".+ Catacombs - Floor .+");
	private final Pattern essencePattern = Pattern.compile("^§.(?<essenceType>\\w+) \\w+ §.x(?<essenceAmount>\\d+)$");
	private final Pattern enchantedBookPattern = Pattern.compile("^§.Enchanted Book \\((?<enchantName>.*)\\)");
	private List<DungeonChest> chestProfits;
	private long lastUpdate = -1;

	@SubscribeEvent
	public void onDrawBackground(GuiScreenEvent.BackgroundDrawnEvent event) {
		if (!(event.gui instanceof GuiChest)) return;
		String lastOpenChestName = SBInfo.getInstance().lastOpenChestName;
		Matcher matcher = chestNamePattern.matcher(lastOpenChestName);
		if (!matcher.matches()) {
			chestProfits = null;
			return;
		}
		GuiChest guiChest = (GuiChest) event.gui;

		if (chestProfits == null) {
			chestProfits = new ArrayList<>();
			updateDungeonChests(guiChest);
		} else if ((System.currentTimeMillis()) > lastUpdate + 1000) {
			lastUpdate = System.currentTimeMillis();
			updateDungeonChests(guiChest);
		}
		//render(guiChest);
	}

	private @Nullable SkyblockItem tryToParseLoreLine(String line) {
		Matcher essenceMatcher = essencePattern.matcher(line);
		Matcher enchantedBookMatcher = enchantedBookPattern.matcher(line);

		if (enchantedBookMatcher.matches()) {
			String enchant = StringUtils.cleanColour(enchantedBookMatcher.group("enchantName"));

			for (Map.Entry<String, JsonObject> entry : NotEnoughUpdates.INSTANCE.manager
				.getItemInformation()
				.entrySet()) {
				String displayName = StringUtils.cleanColour(entry.getValue().get("displayname").getAsString());
				if (displayName.equals("Enchanted Book")) {
					JsonArray lore = entry.getValue().get("lore").getAsJsonArray();
					String enchantName = StringUtils.cleanColour(lore.get(0).getAsString());
					if (enchant.equals(enchantName)) {
						return new SkyblockItem(entry.getKey(), 1);
					}
				}
			}
		} else if (essenceMatcher.matches()) {
			String essenceType = essenceMatcher.group("essenceType");
			String essenceAmount = essenceMatcher.group("essenceAmount");
			if (essenceType == null || essenceAmount == null) {
				return null;
			}

			String internalName = "ESSENCE_" + essenceType.toUpperCase(Locale.ROOT);
			if (!NotEnoughUpdates.INSTANCE.manager.isValidInternalName(internalName)) {
				return null;
			}

			//this can only be an integer if the regex matches
			int amount = Integer.parseInt(essenceAmount);
			return new SkyblockItem(internalName, amount);
		} else {
			String s = StringUtils.cleanColour(line.trim());
			for (Map.Entry<String, JsonObject> entries : NotEnoughUpdates.INSTANCE.manager
				.getItemInformation()
				.entrySet()) {
				String displayName = entries.getValue().get("displayname").getAsString();
				String cleanDisplayName = StringUtils.cleanColour(displayName);
				if (s.equals(cleanDisplayName)) {
					return new SkyblockItem(entries.getKey(), 1);
				}
			}
		}
		return null;
	}

	private void updateDungeonChests(GuiChest guiChest) {
		chestProfits.clear();
		List<Slot> inventorySlots = guiChest.inventorySlots.inventorySlots;
		//loop through the upper chest
		for (int i = 0; i < 27; i++) {
			Slot inventorySlot = inventorySlots.get(i);
			if (inventorySlot == null) {
				continue;
			}

			ItemStack stack = inventorySlot.getStack();
			if (stack != null && stack.getItem() != null && stack.getItem() == Items.skull) {
				//each item is a DungeonChest
				DungeonChest dungeonChest = new DungeonChest();

				List<String> lore = ItemUtils.getLore(stack);
				if ("§7Contents".equals(lore.get(0))) {
					List<SkyblockItem> items = new ArrayList<>();
					for (String s : lore) {
						//check if this line is showing the cost of opening the Chest
						if (s.endsWith(" Coins")) {
							String coinString = StringUtils.cleanColour(s);
							int whitespace = coinString.indexOf(' ');
							if (whitespace != -1) {
								String amountString = coinString.substring(0, whitespace).replace(",", "");
								dungeonChest.cost = Integer.parseInt(amountString);
								continue;
							}
						}

						//check if the line can be converted to a SkyblockItem
						SkyblockItem skyblockItem = tryToParseLoreLine(s);
						if (skyblockItem != null) {
							items.add(skyblockItem);
						}
					}
					dungeonChest.items = items;
					if (dungeonChest.cost != -1) {
						chestProfits.add(dungeonChest);
					}
				}
			}
		}
	}

	public void render(GuiChest guiChest) {
		int xSize = ((AccessorGuiContainer) guiChest).getXSize();
		int guiLeft = ((AccessorGuiContainer) guiChest).getGuiLeft();
		int guiTop = ((AccessorGuiContainer) guiChest).getGuiTop();
		Minecraft.getMinecraft().getTextureManager().bindTexture(dungeonProfitResource);
		GL11.glColor4f(1, 1, 1, 1);
		GlStateManager.disableLighting();
		Utils.drawTexturedRect(guiLeft + xSize + 4, guiTop, 180, 101, 0, 180 / 256f, 0, 101 / 256f, GL11.GL_NEAREST);

		for (int i = 0; i < chestProfits.size(); i++) {
			DungeonChest chestProfit = chestProfits.get(i);
			Utils.renderAlignedString(
				": ",
				"lol",
				guiLeft + xSize + 4 + 10,
				guiTop + 2 + (i * 3),
				160
			);
		}
	}

	private static class DungeonChest {
		private List<SkyblockItem> items = new ArrayList<>();
		private int cost = -1;
	}

	private static class SkyblockItem {
		private final String internalName;
		private final int amount;

		private SkyblockItem(String internalName, int amount) {
			this.internalName = internalName;
			this.amount = amount;
		}

		public double calculateCost() {
			return 1d;
		}
	}
}
