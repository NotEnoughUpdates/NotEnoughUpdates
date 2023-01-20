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

package io.github.moulberry.notenoughupdates.miscgui.minionhelper.render;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.ArrowPagesUtils;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.events.ButtonExclusionZoneEvent;
import io.github.moulberry.notenoughupdates.miscgui.TrophyRewardOverlay;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.Minion;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperManager;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.render.renderables.OverviewLine;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.render.renderables.OverviewText;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.MinionSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.NpcSource;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.NotificationHandler;
import io.github.moulberry.notenoughupdates.util.Rectangle;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MinionHelperOverlay {

	private final ResourceLocation minionOverlayImage = new ResourceLocation("notenoughupdates:minion_overlay.png");
	private final ResourceLocation greenCheckImage = new ResourceLocation("notenoughupdates:dungeon_map/green_check.png");
	private final ResourceLocation whiteCheckImage = new ResourceLocation("notenoughupdates:dungeon_map/white_check.png");

	private final MinionHelperManager manager;
	private final MinionHelperOverlayHover hover;
	private int[] topLeft = new int[]{237, 110};

	private LinkedHashMap<String, OverviewLine> cacheRenderMap = null;
	private int cacheTotalPages = -1;

	private boolean filterEnabled = true;
	private boolean useInstantBuyPrice = true;

	private int maxPerPage = 7;
	private int currentPage = 0;

	public MinionHelperOverlay(MinionHelperManager manager) {
		this.manager = manager;
		hover = new MinionHelperOverlayHover(this, manager);
	}

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		resetCache();
	}

	public void resetCache() {
		cacheRenderMap = null;
		cacheTotalPages = -1;
	}

	@SubscribeEvent
	public void onButtonExclusionZones(ButtonExclusionZoneEvent event) {
		if (manager.inCraftedMinionsInventory() && NotEnoughUpdates.INSTANCE.config.minionHelper.gui) {
			event.blockArea(
				new Rectangle(
					event.getGuiBaseRect().getRight(),
					event.getGuiBaseRect().getTop(),
					168 /*width*/ + 4 /*space*/, 128
				),
				ButtonExclusionZoneEvent.PushDirection.TOWARDS_RIGHT
			);
		}
	}

	@SubscribeEvent
	public void onDrawBackground(GuiScreenEvent.BackgroundDrawnEvent event) {
		if (!manager.inCraftedMinionsInventory()) return;
		if (!NotEnoughUpdates.INSTANCE.config.minionHelper.gui) return;
		if (manager.isInvalidApiKey()) {
			LinkedHashMap<String, OverviewLine> map = new LinkedHashMap<>();
			map.put("§cInvalid API Key!", new OverviewText(Collections.emptyList(), () -> {}));
			render(map);
			return;
		}
		if (manager.notReady()) {
			LinkedHashMap<String, OverviewLine> map = new LinkedHashMap<>();
			map.put("§cLoading...", new OverviewText(Collections.emptyList(), () -> {}));
			render(map);
			return;
		}

		if (manager.getApi().isNotifyNoCollectionApi()) {
			NotificationHandler.displayNotification(Lists.newArrayList(
				"",
				"§cCollection API is disabled!",
				"§cMinion Helper will not filter minions that",
				"§cdo not meet the collection requirements!"
			), false, true);
			manager.getApi().setNotifyNoCollectionApi(false);
		}

		LinkedHashMap<String, OverviewLine> renderMap = getRenderMap();

		hover.renderHover(renderMap);
		render(renderMap);

		renderArrows();
	}

	private void renderArrows() {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if (gui instanceof AccessorGuiContainer) {
			AccessorGuiContainer container = (AccessorGuiContainer) gui;
			int guiLeft = container.getGuiLeft();
			int guiTop = container.getGuiTop();
			int totalPages = getTotalPages();
			ArrowPagesUtils.onDraw(guiLeft, guiTop, topLeft, currentPage, totalPages);
		}
	}

	@SubscribeEvent
	public void onMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (!manager.inCraftedMinionsInventory()) return;
		if (!NotEnoughUpdates.INSTANCE.config.minionHelper.gui) return;
		if (manager.notReady()) return;
		if (!Mouse.getEventButtonState()) return;

		OverviewLine overviewLine = getObjectOverMouse(getRenderMap());
		if (overviewLine != null) {
			overviewLine.onClick();
			event.setCanceled(true);
		}

		int totalPages = getTotalPages();
		if (event.gui instanceof AccessorGuiContainer) {
			int guiLeft = ((AccessorGuiContainer) event.gui).getGuiLeft();
			int guiTop = ((AccessorGuiContainer) event.gui).getGuiTop();
			if (ArrowPagesUtils.onPageSwitchMouse(guiLeft, guiTop, topLeft, currentPage, totalPages, pageChange -> {
				currentPage = pageChange;
				resetCache();
			})) {
				event.setCanceled(true);
			}
		}
		checkButtonClick();
	}

	private void checkButtonClick() {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if (!(gui instanceof GuiChest)) return;

		int xSize = ((AccessorGuiContainer) gui).getXSize();
		int guiLeft = ((AccessorGuiContainer) gui).getGuiLeft();
		int guiTop = ((AccessorGuiContainer) gui).getGuiTop();

		final ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		final int scaledWidth = scaledresolution.getScaledWidth();
		final int scaledHeight = scaledresolution.getScaledHeight();
		int mouseX = Mouse.getX() * scaledWidth / Minecraft.getMinecraft().displayWidth;
		int mouseY = scaledHeight - Mouse.getY() * scaledHeight / Minecraft.getMinecraft().displayHeight - 1;

		int x = guiLeft + xSize + 4 + 149 - 3;
		int y = guiTop + 109 - 3;
		if (mouseX > x && mouseX < x + 16 &&
			mouseY > y && mouseY < y + 16) {
			toggleShowAvailable();
		}

		x = guiLeft + xSize + 4 + 149 - 3 - 16 - 3;
		y = guiTop + 109 - 3;
		if (mouseX > x && mouseX < x + 16 &&
			mouseY > y && mouseY < y + 16) {
			toggleUseInstantBuyPrice();
		}
	}

	@SubscribeEvent
	public void onMouseClick(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (!manager.inCraftedMinionsInventory()) return;
		if (!NotEnoughUpdates.INSTANCE.config.minionHelper.gui) return;
		if (manager.notReady()) return;

		int totalPages = getTotalPages();
		if (ArrowPagesUtils.onPageSwitchKey(currentPage, totalPages, pageChange -> {
			currentPage = pageChange;
			resetCache();
		})) {
			event.setCanceled(true);
		}
	}

	private Map<Minion, Double> getMissing() {
		Map<Minion, Double> prices = new HashMap<>();
		for (Minion minion : manager.getAllMinions().values()) {

			if (!minion.doesMeetRequirements() && filterEnabled) continue;
			if (!minion.isCrafted()) {
				double price = manager.getPriceCalculation().calculateUpgradeCosts(minion, true);
				prices.put(minion, price);
			}
		}
		return prices;
	}

	private void render(Map<String, OverviewLine> renderMap) {
		Minecraft minecraft = Minecraft.getMinecraft();
		Gui gui = Minecraft.getMinecraft().currentScreen;
		if (!(gui instanceof GuiChest)) return;
		int xSize = ((AccessorGuiContainer) gui).getXSize();
		int guiLeft = ((AccessorGuiContainer) gui).getGuiLeft();
		int guiTop = ((AccessorGuiContainer) gui).getGuiTop();
		minecraft.getTextureManager().bindTexture(minionOverlayImage);
		GL11.glColor4f(1, 1, 1, 1);
		GlStateManager.disableLighting();
		Utils.drawTexturedRect(guiLeft + xSize + 4, guiTop, 168, 128, 0, 1f, 0, 1f, GL11.GL_NEAREST);

		if (filterEnabled) {
			minecraft.getTextureManager().bindTexture(greenCheckImage);
		} else {
			minecraft.getTextureManager().bindTexture(whiteCheckImage);
		}
		Utils.drawTexturedRect(guiLeft + xSize + 4 + 149, guiTop + 109, 10, 10, 0, 1f, 0, 1f, GL11.GL_NEAREST);
		GlStateManager.disableLighting();

		RenderHelper.enableGUIStandardItemLighting();
		ItemStack itemStack;
		if (useInstantBuyPrice) {
			itemStack = ItemUtils.getCoinItemStack(10_000_000);
		} else {
			itemStack = ItemUtils.getCoinItemStack(100_000);
		}
		Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(
			itemStack,
			guiLeft + xSize + 4 + 149 - 3 - 16 - 3,
			guiTop + 109 - 3
		);

		RenderHelper.disableStandardItemLighting();

		int x = guiLeft + xSize + 10;
		int i = 0;
		int y = guiTop + 6;
		FontRenderer fontRendererObj = minecraft.fontRendererObj;
		for (Map.Entry<String, OverviewLine> entry : renderMap.entrySet()) {
			String line = entry.getKey();

			/*
			 * Renders the part of the string after '§6' and before '§7' with shadows.
			 *
			 * I don't know how to tell mixin to "only capture part x if part y is present"
			 * Therefore I use these bad splits. I'm Sorry!
			 */
			if (line.contains("§6")) {
				String[] split = line.split("§6");
				line = split[0];
				String price = "§6§l" + split[1];

				if (price.contains("§8")) {
					split = price.split("§8");
					String newPrice = split[0];
					String stuffBehindPricePart = "§8" + price.substring(newPrice.length() + 2);
					price = newPrice;
					int lineLen = Minecraft.getMinecraft().fontRendererObj.getStringWidth(line + price);
					fontRendererObj.drawString(stuffBehindPricePart, x + lineLen, y, -1, false);
				}

				int lineLen = Minecraft.getMinecraft().fontRendererObj.getStringWidth(line);
				fontRendererObj.drawString(price, x + lineLen, y, -1, true);
			}

			fontRendererObj.drawString(line, x, y, -1, false);
			i++;
			if (i == 3) {
				y += 13;
			} else {
				y += 10;
			}
		}
	}

	private LinkedHashMap<String, OverviewLine> getRenderMap() {
		if (cacheRenderMap != null) return cacheRenderMap;

		Map<Minion, Double> prices = getMissing();
		LinkedHashMap<String, OverviewLine> renderMap = new LinkedHashMap<>();

		addTitle(prices, renderMap);
		addNeedToNextSlot(prices, renderMap);

		if (!prices.isEmpty()) {
			addMinions(prices, renderMap);
		}

		cacheRenderMap = renderMap;
		return renderMap;
	}

	private void addNeedToNextSlot(
		Map<Minion, Double> prices,
		LinkedHashMap<String, OverviewLine> renderMap
	) {
		int neededForNextSlot = manager.getNeedForNextSlot();
		if (neededForNextSlot == -1) {
			renderMap.put("§8Next slot: ?", new OverviewText(Collections.emptyList(), () -> {}));
			return;
		}

		double priceNeeded = 0;
		int peltsNeeded = 0;
		int northStarsNeeded = 0;
		int xpGain = 0;
		int index = 0;
		for (Minion minion : TrophyRewardOverlay.sortByValue(prices).keySet()) {
			Double price = prices.get(minion);
			priceNeeded += price;
			xpGain += minion.getXpGain();
			index++;
			peltsNeeded += getSpecialItemNeeds(minion, "SKYBLOCK_PELT");
			northStarsNeeded += getSpecialItemNeeds(minion, "SKYBLOCK_NORTH_STAR");
			if (index == neededForNextSlot) break;
		}
		String costFormat = manager.getPriceCalculation().formatCoins(priceNeeded);
		costFormat = costFormat.replace(" coins", "");

		if (peltsNeeded > 0) {
			costFormat = costFormat + " §8+ §5" + peltsNeeded + " Pelts";
		}
		if (northStarsNeeded > 0) {
			costFormat = costFormat + " §8+ §d" + northStarsNeeded + " North Stars";
		}

		List<String> lore;
		if (xpGain == 0) {
			if (index == 0) {
				lore = Arrays.asList("§aAll minions bought!", "§cNo more SkyBlock XP to gain!");
			} else {
				lore = Collections.singletonList("§cCould not load SkyBlock XP for next slot!");
			}
		} else {
			lore = Arrays.asList(EnumChatFormatting.DARK_AQUA.toString() + xpGain + " Skyblock XP §efor next slot",
				"§8DISCLAIMER: This only works if", "§8you follow the helper."
			);
		}
		OverviewText overviewText = new OverviewText(lore, () -> {});
		renderMap.put("§8Next slot: §3" + neededForNextSlot + " minions", overviewText);
		renderMap.put("§8Cost: " + costFormat, overviewText);
	}

	private static int getSpecialItemNeeds(Minion minion, String specialItem) {
		int count = 0;
		MinionSource minionSource = minion.getMinionSource();

		if (minionSource instanceof NpcSource) {
			NpcSource source = (NpcSource) minionSource;
			ArrayListMultimap<String, Integer> items = source.getItems();
			if (items.containsKey(specialItem)) {
				for (Integer amount : items.get(specialItem)) {
					count += amount;
				}
			}
		}
		return count;
	}

	private void addTitle(Map<Minion, Double> prices, LinkedHashMap<String, OverviewLine> renderMap) {
		String name = "§8" + prices.size() + " " + (filterEnabled ? "craftable" : "total") + " minions";
		renderMap.put(name, new OverviewText(Collections.emptyList(), () -> {}));
	}

	private void addMinions(Map<Minion, Double> prices, LinkedHashMap<String, OverviewLine> renderMap) {
		int skipPreviousPages = currentPage * maxPerPage;
		int i = 0;
		Map<Minion, Double> sort = TrophyRewardOverlay.sortByValue(prices);
		for (Minion minion : sort.keySet()) {
			if (i >= skipPreviousPages) {
				String displayName = minion.getDisplayName();
				if (displayName == null) {
					if (NotEnoughUpdates.INSTANCE.config.hidden.dev) {
						Utils.addChatMessage("§cDisplayname is null for " + minion.getInternalName());
					}
					continue;
				}

				displayName = displayName.replace(" Minion", "");
				String format = manager.getPriceCalculation().calculateUpgradeCostsFormat(minion, true);
				format = format.replace(" coins", "");
				String requirementFormat = minion.doesMeetRequirements() ? "§9" : "§c";
				renderMap.put(
					requirementFormat + displayName + " " + minion.getTier() + " §8- " + format,
					minion
				);
			}

			i++;
			if (i == ((currentPage + 1) * maxPerPage)) break;
		}
	}

	private int getTotalPages() {
		if (cacheTotalPages != -1) return cacheTotalPages;

		Map<Minion, Double> prices = getMissing();
		int totalPages = (int) ((double) prices.size() / maxPerPage);
		if (prices.size() % maxPerPage != 0) {
			totalPages++;
		}

		cacheTotalPages = totalPages;
		return totalPages;
	}

	private void toggleShowAvailable() {
		filterEnabled = !filterEnabled;
		currentPage = 0;
		resetCache();
	}

	private void toggleUseInstantBuyPrice() {
		useInstantBuyPrice = !useInstantBuyPrice;
		currentPage = 0;
		resetCache();
		manager.getPriceCalculation().resetCache();
	}

	OverviewLine getObjectOverMouse(LinkedHashMap<String, OverviewLine> renderMap) {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if (!(gui instanceof GuiChest)) return null;

		int xSize = ((AccessorGuiContainer) gui).getXSize();
		int guiLeft = ((AccessorGuiContainer) gui).getGuiLeft();
		int guiTop = ((AccessorGuiContainer) gui).getGuiTop();

		int x = guiLeft + xSize + 9;
		int y = guiTop + 5;

		final ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		final int scaledWidth = scaledresolution.getScaledWidth();
		final int scaledHeight = scaledresolution.getScaledHeight();
		int mouseX = Mouse.getX() * scaledWidth / Minecraft.getMinecraft().displayWidth;
		int mouseY = scaledHeight - Mouse.getY() * scaledHeight / Minecraft.getMinecraft().displayHeight - 1;

		int i = 0;
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		for (Map.Entry<String, OverviewLine> entry : renderMap.entrySet()) {
			String text = entry.getKey();
			int width = fontRenderer.getStringWidth(StringUtils.cleanColour(text));
			if (mouseX > x && mouseX < x + width + 4 &&
				mouseY > y && mouseY < y + 11) {
				return entry.getValue();
			}
			i++;
			if (i == 3) {
				y += 13;
			} else {
				y += 10;
			}
		}

		return null;
	}

	public void onProfileSwitch() {
		currentPage = 0;
		filterEnabled = true;
		useInstantBuyPrice = true;
	}

	public void setMaxPerPage(int maxPerPage) {
		this.maxPerPage = maxPerPage;
	}

	public void setTopLeft(int[] topLeft) {
		this.topLeft = topLeft;
	}

	public boolean isFilterEnabled() {
		return filterEnabled;
	}

	public boolean isUseInstantBuyPrice() {
		return useInstantBuyPrice;
	}
}
