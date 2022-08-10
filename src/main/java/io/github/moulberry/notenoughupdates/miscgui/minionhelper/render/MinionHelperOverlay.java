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

import com.google.common.collect.Lists;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.ArrowPagesUtils;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.miscgui.TrophyRewardOverlay;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.Minion;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperManager;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.render.renderables.OverviewLine;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.render.renderables.OverviewText;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.util.NotificationHandler;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
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
import java.util.Map;

public class MinionHelperOverlay {

	private final ResourceLocation minionOverlayImage = new ResourceLocation("notenoughupdates:minion_overlay.png");

	private final MinionHelperManager manager;
	private final MinionHelperOverlayHover hover;
	private int[] topLeft = new int[]{237, 110};

	private LinkedHashMap<String, OverviewLine> cacheRenderMap = null;
	private int cacheTotalPages = -1;

	private boolean showOnlyAvailable = true;

	private int maxPerPage = 8;
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
	public void onDrawBackground(GuiScreenEvent.BackgroundDrawnEvent event) {
		if (!manager.inCraftedMinionsInventory()) return;
		if (!NotEnoughUpdates.INSTANCE.config.minionHelper.gui) return;
		if (!manager.isReadyToUse()) {
			LinkedHashMap<String, OverviewLine> map = new LinkedHashMap<>();
			map.put("§cLoading...", new OverviewText(Collections.emptyList(), () -> {}));
			render(event, map);
			return;
		}

		if (manager.getApi().isNotifyNoCollectionApi()) {
			NotificationHandler.displayNotification(Lists.newArrayList(
				"",
				"§cCollection API is disabled!",
				"§cMinion Helper will not filter minions that",
				"§cdo not meet the collection requirements!"
			), false, true);
			//TODO add tutorial how to enable collection api
			manager.getApi().setNotifyNoCollectionApi(false);
		}

		LinkedHashMap<String, OverviewLine> renderMap = getRenderMap();

		hover.renderHover(renderMap);
		render(event, renderMap);

		renderArrows(event);
	}

	private void renderArrows(GuiScreenEvent.BackgroundDrawnEvent event) {
		GuiScreen gui = event.gui;
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
		if (!manager.isReadyToUse()) return;
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
	}

	@SubscribeEvent
	public void onMouseClick(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (!manager.inCraftedMinionsInventory()) return;
		if (!NotEnoughUpdates.INSTANCE.config.minionHelper.gui) return;
		if (!manager.isReadyToUse()) return;

		int totalPages = getTotalPages();
		if (ArrowPagesUtils.onPageSwitchKey(currentPage, totalPages, pageChange -> {
			currentPage = pageChange;
			resetCache();
		})) {
			event.setCanceled(true);
		}
	}

	private Map<Minion, Long> getMissing() {
		Map<Minion, Long> prices = new HashMap<>();
		for (Minion minion : manager.getAllMinions().values()) {

			if (!minion.doesMeetRequirements() && showOnlyAvailable) continue;
			if (!minion.isCrafted()) {
				long price = manager.getPriceCalculation().calculateUpgradeCosts(minion, true);
				prices.put(minion, price);
			}
		}
		return prices;
	}

	private void render(GuiScreenEvent.BackgroundDrawnEvent event, Map<String, OverviewLine> renderMap) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;
		Gui gui = event.gui;
		int xSize = ((AccessorGuiContainer) gui).getXSize();
		int guiLeft = ((AccessorGuiContainer) gui).getGuiLeft();
		int guiTop = ((AccessorGuiContainer) gui).getGuiTop();
		minecraft.getTextureManager().bindTexture(minionOverlayImage);
		GL11.glColor4f(1, 1, 1, 1);
		GlStateManager.disableLighting();
		Utils.drawTexturedRect(guiLeft + xSize + 4, guiTop, 168, 128, 0, 1f, 0, 1f, GL11.GL_NEAREST);

		int a = guiLeft + xSize + 4;
		FontRenderer fontRendererObj = minecraft.fontRendererObj;

		int i = 0;
		int y = 0;
		for (Map.Entry<String, OverviewLine> entry : renderMap.entrySet()) {
			String line = entry.getKey();
			OverviewLine overviewLine = entry.getValue();
			String prefix = "";
			if (overviewLine instanceof Minion) {
				Minion minion = (Minion) overviewLine;
				if (minion == hover.getLastHovered()) {
					prefix = "§e";
				}
			}
			fontRendererObj.drawString(prefix + line, a + 6, guiTop + 6 + y, -1, false);
			i++;
			if (i == 2) {
				y += 15;
			} else {
				y += 10;
			}
		}
	}

	private LinkedHashMap<String, OverviewLine> getRenderMap() {
		if (cacheRenderMap != null) return cacheRenderMap;

		Map<Minion, Long> prices = getMissing();
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
		Map<Minion, Long> prices,
		LinkedHashMap<String, OverviewLine> renderMap
	) {
		int neededForNextSlot = manager.getNeedForNextSlot();
		String color = showOnlyAvailable ? "§e" : "§c";
		if (neededForNextSlot == -1) {
			renderMap.put(color + "Next slot in: ?", new OverviewText(Collections.emptyList(), () -> {}));
			return;
		}

		long priceNeeded = 0;
		int index = 0;
		for (Long price : TrophyRewardOverlay.sortByValue(prices).values()) {
			priceNeeded += price;
			index++;
			if (index == neededForNextSlot) break;
		}
		String format = manager.getPriceCalculation().formatCoins(priceNeeded);
		String text = color + "Next slot in: §b" + neededForNextSlot + " §8- " + format;
		renderMap.put(text, new OverviewText(Collections.emptyList(), () -> {}));
	}

	private void addTitle(Map<Minion, Long> prices, LinkedHashMap<String, OverviewLine> renderMap) {
		String name;
		String hoverText;
		if (prices.isEmpty()) {
			name = (showOnlyAvailable ? "No minion obtainable!" : "§aAll minions collected!");
			hoverText = "No minions to craft available!";
		} else {
			name = (showOnlyAvailable ? "Obtainable" : "All") + ": " + prices.size();
			if (showOnlyAvailable) {
				hoverText = "There are " + prices.size() + " more minions in total!";
			} else {
				hoverText = "You can craft " + prices.size() + " more minions!";
			}
		}
		String toggleText = "§eClick to " + (showOnlyAvailable ? "show" : "hide") + " minion upgrades without requirements";
		renderMap.put("§e" + name, new OverviewText(Arrays.asList(hoverText, "", toggleText), this::toggleShowAvailable));
	}

	private void addMinions(Map<Minion, Long> prices, LinkedHashMap<String, OverviewLine> renderMap) {
		int skipPreviousPages = currentPage * maxPerPage;
		int i = 0;
		Map<Minion, Long> sort = TrophyRewardOverlay.sortByValue(prices);
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
				String requirementFormat = !minion.doesMeetRequirements() ? "§7§o" : "";
				renderMap.put(
					requirementFormat + displayName + " " + minion.getTier() + " §r§8- " + format,
					minion
				);
			}

			i++;
			if (i == ((currentPage + 1) * maxPerPage)) break;
		}
	}

	private int getTotalPages() {
		if (cacheTotalPages != -1) return cacheTotalPages;

		Map<Minion, Long> prices = getMissing();
		int totalPages = (int) ((double) prices.size() / maxPerPage);
		if (prices.size() % maxPerPage != 0) {
			totalPages++;
		}

		cacheTotalPages = totalPages;
		return totalPages;
	}

	private void toggleShowAvailable() {
		showOnlyAvailable = !showOnlyAvailable;
		currentPage = 0;
		resetCache();
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
			if (mouseX > x && mouseX < x + width &&
				mouseY > y && mouseY < y + 11) {
				return entry.getValue();
			}
			i++;
			if (i == 2) {
				y += 15;
			} else {
				y += 10;
			}
		}

		return null;
	}

	public void onProfileSwitch() {
		currentPage = 0;
		showOnlyAvailable = true;
	}

	public void setMaxPerPage(int maxPerPage) {
		this.maxPerPage = maxPerPage;
	}

	public void setTopLeft(int[] topLeft) {
		this.topLeft = topLeft;
	}
}
