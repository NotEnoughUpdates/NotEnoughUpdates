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

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.events.ButtonExclusionZoneEvent;
import io.github.moulberry.notenoughupdates.events.DrawSlotReturnEvent;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.util.ItemResolutionQuery;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.Rectangle;
import io.github.moulberry.notenoughupdates.util.Utils;
import lombok.Getter;
import lombok.var;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@NEUAutoSubscribe
public class DungeonNpcProfitOverlay {

	private static final ResourceLocation dungeonProfitResource =
		new ResourceLocation("notenoughupdates:dungeon_chest_worth.png");

	private static final Pattern chestNamePattern = Pattern.compile(".+ Catacombs - Floor .+");
	private static final Pattern essencePattern = Pattern.compile(
		"^§.(?<essenceType>\\w+) Essence §.x(?<essenceAmount>\\d+)$");
	private static final Pattern enchantedBookPattern = Pattern.compile("^§.Enchanted Book \\((?<enchantName>.*)§.\\)");
	private static final Map<Integer, DungeonChest> chestProfits = new HashMap<>();
	private static List<DungeonChest> orderedChestProfits = new ArrayList<>();

	/**
	 * Check the current status for the overlay
	 *
	 * @return if the overlay is rendering right now
	 */
	public boolean isRendering() {
		return NotEnoughUpdates.INSTANCE.config.dungeons.croesusProfitOverlay && !chestProfits.isEmpty();
	}

	private boolean isChestOverview(IInventory inventory) {
		return chestNamePattern.matcher(StringUtils.cleanColour(
			inventory.getDisplayName()
							 .getUnformattedText())).matches();
	}

	private boolean isChestOverview(GuiChest chest) {
		ContainerChest inventorySlots = (ContainerChest) chest.inventorySlots;
		return isChestOverview(inventorySlots.getLowerChestInventory());
	}

	/**
	 * Highlight the slot that is being drawn if applicable. Called by MixinGuiContainer
	 *
	 * @see io.github.moulberry.notenoughupdates.mixins.MixinGuiContainer#drawSlotRet(Slot, CallbackInfo)
	 */
	@SubscribeEvent
	public void onDrawSlot(DrawSlotReturnEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.dungeons.croesusProfitOverlay
			|| !NotEnoughUpdates.INSTANCE.config.dungeons.croesusHighlightHighestProfit
			|| !isChestOverview(event.getSlot().inventory)) {
			return;
		}
		var slot = event.getSlot();
		var chestProfit = getPotentialChest(slot);
		if (chestProfit == null || !chestProfit.shouldHighlight || slot.slotNumber != chestProfit.slot) {
			return;
		}
		Gui.drawRect(
			slot.xDisplayPosition,
			slot.yDisplayPosition,
			slot.xDisplayPosition + 16,
			slot.yDisplayPosition + 16,
			Color.GREEN.getRGB()
		);
	}

	@SubscribeEvent
	public void onButtonExclusionZones(ButtonExclusionZoneEvent event) {
		if (isRendering())
			event.blockArea(
				new Rectangle(
					event.getGuiBaseRect().getRight(),
					event.getGuiBaseRect().getTop(),
					180 /*width*/ + 4 /*space*/, 101
				),
				ButtonExclusionZoneEvent.PushDirection.TOWARDS_RIGHT
			);
	}

	@SubscribeEvent
	public void onDrawBackground(GuiScreenEvent.BackgroundDrawnEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.dungeons.croesusProfitOverlay || !(event.gui instanceof GuiChest)) {
			chestProfits.clear();
			return;
		}
		GuiChest guiChest = (GuiChest) event.gui;
		if (!isChestOverview(guiChest)) {
			chestProfits.clear();
			return;
		}
		render(guiChest);
	}

	private @Nullable DungeonChest getPotentialChest(@Nullable Slot slot) {
		if (slot == null) return null;
		DungeonChest chestProfit = chestProfits.get(slot.slotNumber);
		if (chestProfit == null) {
			long s = System.currentTimeMillis();
			updatePotentialChest(slot.getStack(), slot);
			long d = System.currentTimeMillis() - s;
			if (d > 10) {
				System.out.println("Finished analyzing slow croesus slot. Took " + d + " ms");
				ItemUtils.getLore(slot.getStack()).forEach(System.out::println);
			}
		}
		return chestProfits.get(slot.slotNumber);
	}

	private void updatePotentialChest(ItemStack stack, Slot slot) {
		if (stack == null || stack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane)) return;
		DungeonChest dungeonChest = new DungeonChest();
		dungeonChest.slot = slot.slotNumber;

		List<String> lore = ItemUtils.getLore(stack);
		if (lore.size() == 0 || !"§7Contents".equals(lore.get(0))) {
			return;
		}
		dungeonChest.name = stack.getDisplayName();
		List<SkyblockItem> items = new ArrayList<>();
		for (String s : lore) {

			if ("§7Contents".equals(s) || "".equals(s) || "§7Cost".equals(s) || "§cCan't open another chest!".equals(s) ||
				"§aAlready opened!".equals(s) ||
				"§eClick to open!".equals(s)) continue;

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
				continue;
			}

			//check if the line can be converted to a SkyblockItem
			SkyblockItem skyblockItem = SkyblockItem.createFromLoreEntry(s);
			if (skyblockItem != null) {
				items.add(skyblockItem);
			} else
				System.out.println("Unexpected line " + s + " while analyzing croesus lore");
		}
		dungeonChest.items = items;
		if (dungeonChest.costToOpen != -1) {
			dungeonChest.calculateProfitAndBuildLore();
			chestProfits.put(slot.slotNumber, dungeonChest);
		}
		orderedChestProfits = chestProfits.values().stream()
																			.sorted(NotEnoughUpdates.INSTANCE.config.dungeons.croesusSortByProfit
																				? Comparator.comparing(DungeonChest::getProfit).reversed()
																				: Comparator.comparing(DungeonChest::getSlot))
																			.collect(Collectors.toList());
		chestProfits.values().forEach(it -> it.shouldHighlight = false);
		chestProfits.values().stream().max(Comparator.comparing(DungeonChest::getProfit)).ifPresent(it ->
			it.shouldHighlight = true);
	}

	public void render(GuiChest guiChest) {
		int xSize = ((AccessorGuiContainer) guiChest).getXSize();
		int guiLeft = ((AccessorGuiContainer) guiChest).getGuiLeft();
		int guiTop = ((AccessorGuiContainer) guiChest).getGuiTop();
		Minecraft.getMinecraft().getTextureManager().bindTexture(dungeonProfitResource);
		GL11.glColor4f(1, 1, 1, 1);
		GlStateManager.disableLighting();
		Utils.drawTexturedRect(guiLeft + xSize + 4, guiTop, 180, 101, 0, 180 / 256f, 0, 101 / 256f, GL11.GL_NEAREST);

		for (int i = 0; i < orderedChestProfits.size(); i++) {
			DungeonChest chestProfit = orderedChestProfits.get(i);
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
				Utils.drawHoveringText(chestProfit.lore, mouseX, mouseY, width, height, -1);
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
		@Getter
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
				String enchantName = ItemResolutionQuery.resolveEnchantmentByName(enchantedBookMatcher.group("enchantName"));
				if (enchantName == null) return null;
				return new SkyblockItem(enchantName, 1);
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
				// Remove Book (from hot potato book), as a perf optimization since "book" is a very common phrase
				String trimmedLine = line.trim();
				String id =
					ItemResolutionQuery.filterInternalNameCandidates(ItemResolutionQuery.findInternalNameCandidatesForDisplayName(
						trimmedLine.replace("Book", "")), trimmedLine, true);
				if (id == null) return null;
				return new SkyblockItem(id, 1);
			}
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
