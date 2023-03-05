/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
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
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.MinecraftExecutor;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NEUAutoSubscribe
public class DungeonNpcProfitOverlay {

	private static final ResourceLocation dungeonProfitResource =
		new ResourceLocation("notenoughupdates:dungeon_chest_worth.png");

	private static final Pattern chestNamePattern = Pattern.compile(".+ Catacombs - Floor .+");
	private static final Pattern essencePattern = Pattern.compile(
		"^§.(?<essenceType>\\w+) Essence §.x(?<essenceAmount>\\d+)$");
	private static final Pattern enchantedBookPattern = Pattern.compile("^§.Enchanted Book \\((?<enchantName>.*)\\)");
	private static final List<DungeonChest> chestProfits = new ArrayList<>();
	private static List<Slot> previousSlots;

	/**
	 * Check the current status for the overlay
	 *
	 * @return if the overlay is rendering right now
	 */
	public static boolean isRendering() {
		return NotEnoughUpdates.INSTANCE.config.dungeons.croesusProfitOverlay && !chestProfits.isEmpty();
	}

	/**
	 * Highlight the slot that is being drawn if applicable. Called by MixinGuiContainer
	 *
	 * @param slot the slot to be checked
	 * @see io.github.moulberry.notenoughupdates.mixins.MixinGuiContainer#drawSlotRet(Slot, CallbackInfo)
	 */
	public static void onDrawSlot(Slot slot) {
		if (NotEnoughUpdates.INSTANCE.config.dungeons.croesusHighlightHighestProfit && isRendering()) {
			synchronized (chestProfits) {
				for (DungeonChest chestProfit : chestProfits) {
					if (chestProfit.shouldHighlight && slot.slotNumber == chestProfit.slot) {
						Gui.drawRect(
							slot.xDisplayPosition,
							slot.yDisplayPosition,
							slot.xDisplayPosition + 16,
							slot.yDisplayPosition + 16,
							Color.GREEN.getRGB()
						);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onDrawBackground(GuiScreenEvent.BackgroundDrawnEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.dungeons.croesusProfitOverlay || !(event.gui instanceof GuiChest)) {
			chestProfits.clear();
			previousSlots = null;
			return;
		}

		String lastOpenChestName = SBInfo.getInstance().lastOpenChestName;
		Matcher matcher = chestNamePattern.matcher(lastOpenChestName);
		if (!matcher.matches()) {
			chestProfits.clear();
			previousSlots = null;
			return;
		}
		GuiChest guiChest = (GuiChest) event.gui;
		List<Slot> slots = guiChest.inventorySlots.inventorySlots;

		if (chestProfits.isEmpty() || !slots.equals(previousSlots)) {
			chestProfits.clear();
			updateDungeonChests(slots);
		}
		previousSlots = guiChest.inventorySlots.inventorySlots;

		render(guiChest);
	}

	/**
	 * Update the profit applicable for the chests currently visible
	 *
	 * @param inventorySlots list of Slots from the GUI containing the dungeon chest previews
	 */
	private void updateDungeonChests(List<Slot> inventorySlots) {
		MinecraftExecutor.OffThread.execute(() -> {
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
					dungeonChest.slot = i;

					List<String> lore = ItemUtils.getLore(stack);
					if ("§7Contents".equals(lore.get(0))) {
						dungeonChest.name = stack.getDisplayName();
						List<SkyblockItem> items = new ArrayList<>();
						for (String s : lore) {
							//check if this line is showing the cost of opening the Chest
							if (s.endsWith(" Coins")) {
								String coinString = StringUtils.cleanColour(s);
								int whitespace = coinString.indexOf(' ');
								if (whitespace != -1) {
									String amountString = coinString.substring(0, whitespace).replace(",", "");
									dungeonChest.costToOpen = Integer.parseInt(amountString);
									continue;
								}
							} else if (s.equals("§aFREE")) {
								dungeonChest.costToOpen = 0;
							}

							//check if the line can be converted to a SkyblockItem
							SkyblockItem skyblockItem = SkyblockItem.createFromLoreEntry(s);
							if (skyblockItem != null) {
								items.add(skyblockItem);
							}
						}
						dungeonChest.items = items;
						if (dungeonChest.costToOpen != -1) {
							synchronized (chestProfits) {
								// Check if there is already a chest that has more items
								if (chestProfits.stream().anyMatch(c -> c.name.equals(dungeonChest.name) &&
									c.items.size() > dungeonChest.items.size())) {
									return;
								}
								chestProfits.removeIf(c -> c.name.equals(dungeonChest.name));
								dungeonChest.calculateProfitAndBuildLore();
								chestProfits.add(dungeonChest);
							}
						}
					}
				}
			}

			if (NotEnoughUpdates.INSTANCE.config.dungeons.croesusSortByProfit) {
				chestProfits.sort(Comparator.comparing(DungeonChest::getProfit).reversed());
			}

			if (NotEnoughUpdates.INSTANCE.config.dungeons.croesusHighlightHighestProfit && chestProfits.size() >= 1) {
				List<DungeonChest> copiedList = new ArrayList<>(chestProfits);
				copiedList.sort(Comparator.comparing(DungeonChest::getProfit).reversed());

				copiedList.get(0).shouldHighlight = true;
			}
		});
	}

	public void render(GuiChest guiChest) {
		int xSize = ((AccessorGuiContainer) guiChest).getXSize();
		int guiLeft = ((AccessorGuiContainer) guiChest).getGuiLeft();
		int guiTop = ((AccessorGuiContainer) guiChest).getGuiTop();
		Minecraft.getMinecraft().getTextureManager().bindTexture(dungeonProfitResource);
		GL11.glColor4f(1, 1, 1, 1);
		GlStateManager.disableLighting();
		Utils.drawTexturedRect(guiLeft + xSize + 4, guiTop, 180, 101, 0, 180 / 256f, 0, 101 / 256f, GL11.GL_NEAREST);
		if (chestProfits.isEmpty()) {
			return;
		}

		for (int i = 0; i < chestProfits.size(); i++) {
			DungeonChest chestProfit = chestProfits.get(i);
			int x = guiLeft + xSize + 14;
			int y = guiTop + 6 + (i * 10);
			Utils.renderAlignedString(
				chestProfit.name,
				(chestProfit.profit > 0
					? EnumChatFormatting.GREEN + Utils.shortNumberFormat(chestProfit.profit, 0)
					: EnumChatFormatting.RED + "-" + Utils.shortNumberFormat(-chestProfit.profit, 0)),
				x,
				y,
				160
			);

			ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
			int width = scaledResolution.getScaledWidth();
			int height = scaledResolution.getScaledHeight();

			int mouseX = Utils.getMouseX();
			int mouseY = Utils.getMouseY();

			if (Utils.isWithinRect(mouseX, mouseY, x, y, 160, 10))
				Utils.drawHoveringText(
					chestProfit.lore,
					mouseX,
					mouseY,
					width,
					height,
					-1,
					Minecraft.getMinecraft().fontRendererObj
				);
		}

	}

	/**
	 * Dataclass holding info on a single Dungeon Chest Preview
	 * <p>
	 * This includes:
	 * <ul>
	 *   <li>The items, represented as a SkyblockItem</li>
	 *   <li>The cost to open the chest</li>
	 * </ul>
	 *
	 * @see SkyblockItem
	 */
	private static class DungeonChest {
		private List<SkyblockItem> items = new ArrayList<>();
		private List<String> lore;
		private int costToOpen = -1;
		private String name;
		private int slot;
		private boolean shouldHighlight;
		private double profit;

		public double getProfit() {
			return profit;
		}

		public void calculateProfitAndBuildLore() {
			profit = 0d;
			lore = new ArrayList<>();
			lore.add(name);
			for (SkyblockItem item : items) {
				double cost = item.calculateCost();
				profit += cost;
				lore.add(
					EnumChatFormatting.AQUA + " - " + item.getDisplayName() + EnumChatFormatting.RESET + " " +
						EnumChatFormatting.GREEN +
						Utils.shortNumberFormat(cost, 0));
			}
			lore.add("");
			profit -= costToOpen;
			lore.add(
				EnumChatFormatting.AQUA + "Cost to open: " + EnumChatFormatting.RED + Utils.shortNumberFormat(costToOpen, 0));
			lore.add(
				EnumChatFormatting.AQUA + "Total profit: " +
					(profit > 0 ? EnumChatFormatting.GREEN + Utils.shortNumberFormat(profit, 0)
						: EnumChatFormatting.RED + "-" + Utils.shortNumberFormat(
							-profit,
							0
						)));
		}
	}

	/**
	 * Dataclass holding info on a single skyblock item which is part of a DungeonChest
	 * <p>
	 * This includes:
	 * <ul>
	 *   <li>The internal name of the item</li>
	 *   <li>The amount</li>
	 * </ul>
	 *
	 * @see DungeonChest
	 */
	private static class SkyblockItem {
		private final String internalName;
		private final int amount;

		private SkyblockItem(String internalName, int amount) {
			this.internalName = internalName;
			this.amount = amount;
		}

		/**
		 * Try to create a SkyblockItem from the given lore line.
		 * <p>
		 * This involves checking for:
		 * <ul>
		 *   <li>Enchanted books</li>
		 *   <li>Dungeon essence</li>
		 *   <li>Normal items that can appear in dungeon chests</li>
		 * </ul>
		 *
		 * @param line the line to be parsed
		 * @return a new SkyblockItem if possible, otherwise null
		 */
		public static @Nullable SkyblockItem createFromLoreEntry(String line) {
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
			} else if (essenceMatcher.matches() && NotEnoughUpdates.INSTANCE.config.dungeons.useEssenceCostFromBazaar) {
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

		/**
		 * Calculate the price of this item, factoring in the amount
		 *
		 * @return total price
		 */
		public double calculateCost() {
			double price = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarOrBin(internalName, true);
			if (price != -1) {
				return price * amount;
			}
			return 0d;
		}

		public String getDisplayName() {
			JsonObject entry = NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery().withKnownInternalName(
				internalName).resolveToItemListJson();
			if (entry != null) {
				String displayName = entry.get("displayname").getAsString();
				String cleanedDisplayName = StringUtils.cleanColour(displayName);
				if ("Enchanted Book".equals(cleanedDisplayName)) {
					return entry.get("lore").getAsJsonArray().get(0).getAsString();
				} else {
					return entry.get("displayname").getAsString();
				}
			}
			return "ERROR";
		}
	}
}
