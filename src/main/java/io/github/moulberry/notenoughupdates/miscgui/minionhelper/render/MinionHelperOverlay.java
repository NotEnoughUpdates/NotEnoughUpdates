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
	private final MinionHelperManager manager;
	private final MinionHelperOverlayHover hover;

	private LinkedHashMap<String, OverviewLine> cacheRenderMap = null;
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
	}

	//TODO use different texture
	public final ResourceLocation auctionProfitImage = new ResourceLocation("notenoughupdates:auction_profit.png");

	@SubscribeEvent
	public void onDrawBackground(GuiScreenEvent.BackgroundDrawnEvent event) {
		if (!manager.inCraftedMinionsInventory()) return;
		if (!manager.isReadyToUse()) return;

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
	}

	@SubscribeEvent
	public void onMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (!manager.inCraftedMinionsInventory()) return;
		if (!manager.isReadyToUse()) return;
		if (!Mouse.getEventButtonState()) return;

		OverviewLine overviewLine = getObjectOverMouse(getRenderMap());
		if (overviewLine != null) {
			overviewLine.onClick();
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
		minecraft.getTextureManager().bindTexture(auctionProfitImage);
		GL11.glColor4f(1, 1, 1, 1);
		GlStateManager.disableLighting();
		Utils.drawTexturedRect(guiLeft + xSize + 4, guiTop, 180, 101, 0, 180 / 256f, 0, 101 / 256f, GL11.GL_NEAREST);

		int a = guiLeft + xSize + 4;
		FontRenderer fontRendererObj = minecraft.fontRendererObj;

		int index = 0;
		int extra = 0;
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
			fontRendererObj.drawString(prefix + line, a + 6, guiTop + 6 + extra, -1, false);
			extra += 10;
			if (extra == maxPerPage + 2) extra = 15;
			index++;
			if (index == renderMap.values().size() - 2) extra = maxPerPage * 10 + 20;
		}
	}

	private LinkedHashMap<String, OverviewLine> getRenderMap() {
		if (cacheRenderMap != null) return cacheRenderMap;

		Map<Minion, Long> prices = getMissing();
		LinkedHashMap<String, OverviewLine> renderMap = new LinkedHashMap<>();
		int totalPages = getTotalPages(prices);

		addTitle(prices, renderMap, totalPages);

		if (!prices.isEmpty()) {
			addMinions(prices, renderMap);
		}

		if (totalPages != currentPage + 1) {
			renderMap.put(
				"   §eNext Page ->",
				new OverviewText(Collections.singletonList("§eClick to show page " + (currentPage + 2)), () -> {
					if (totalPages == currentPage + 1) return;
					currentPage++;
					resetCache();
				})
			);
		} else {
			renderMap.put("   §7Next Page ->", new OverviewText(Collections.singletonList("§7There is no next page"), () -> {
			}));
		}
		if (currentPage != 0) {
			renderMap.put(
				"§e<- Previous Page",
				new OverviewText(Collections.singletonList("§eClick to show page " + currentPage), () -> {
					if (currentPage == 0) return;
					currentPage--;
					resetCache();
				})
			);
		} else {
			renderMap.put(
				"§7<- Previous Page",
				new OverviewText(Collections.singletonList("§7There is no previous page"), () -> {
				})
			);
		}

		cacheRenderMap = renderMap;
		return renderMap;
	}

	private void addTitle(Map<Minion, Long> prices, LinkedHashMap<String, OverviewLine> renderMap, int totalPages) {
		String name;
		String hoverText;
		String pagePrefix = "(" + (currentPage + 1) + "/" + totalPages + ") ";
		if (prices.isEmpty()) {
			name = pagePrefix + (showOnlyAvailable ? "No minion obtainable!" : "§aAll minions collected!");
			hoverText = "No minions to craft avaliable!";
		} else {
			name = pagePrefix + (showOnlyAvailable ? "Obtainable" : "All") + ": " + prices.size();
			if (showOnlyAvailable) {
			hoverText = "There are " + prices.size() + " more minions in total!";
			} else {
			hoverText = "You can craft " + prices.size() + " more minions!";
			}
		}
		String toggleText = "§eClick to " + (showOnlyAvailable ? "show" : "hide") + " minion upgrades without requirements";
		renderMap.put(name, new OverviewText(Arrays.asList(hoverText, "", toggleText), this::toggleShowAvailable));
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

	private int getTotalPages(Map<Minion, Long> prices) {
		int totalPages = (int) ((double) prices.size() / maxPerPage);
		if (prices.size() % maxPerPage != 0) {
			totalPages++;
		}
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

		int x = guiLeft + xSize + 4;
		int y = guiTop;

		final ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		final int scaledWidth = scaledresolution.getScaledWidth();
		final int scaledHeight = scaledresolution.getScaledHeight();
		int mouseX = Mouse.getX() * scaledWidth / Minecraft.getMinecraft().displayWidth;
		int mouseY = scaledHeight - Mouse.getY() * scaledHeight / Minecraft.getMinecraft().displayHeight - 1;

		int index = 0;
		int extra = 0;
		for (OverviewLine overviewLine : renderMap.values()) {

			if (mouseX > x && mouseX < x + 130 &&
				mouseY > y + extra && mouseY < y + 13 + extra) {
				return overviewLine;
			}
			extra += 10;
			if (extra == maxPerPage + 2) extra = 15;
			index++;
			if (index == renderMap.values().size() - 2) extra = maxPerPage * 10 + 20;
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
}
