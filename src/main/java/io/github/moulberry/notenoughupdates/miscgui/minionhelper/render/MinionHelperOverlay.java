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
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MinionHelperOverlay {
	private final MinionHelperManager manager;
	private final MinionHelperOverlayHover hover;

	private LinkedHashMap<String, OverviewLine> cacheRenderMapShift = null;
	private LinkedHashMap<String, OverviewLine> cacheRenderMapNoShift = null;

	public MinionHelperOverlay(MinionHelperManager manager) {
		this.manager = manager;
		hover = new MinionHelperOverlayHover(this, manager);
	}

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		resetCache();
	}

	public void resetCache() {
		cacheRenderMapShift = null;
		cacheRenderMapNoShift = null;
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

	private Map<Minion, Long> getMissing(boolean shift) {
		Map<Minion, Long> prices = new HashMap<>();
		for (Minion minion : manager.getAllMinions().values()) {

			if (!minion.doesMeetRequirements() && !shift) continue;
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
		}
	}

	private LinkedHashMap<String, OverviewLine> getRenderMap() {
		boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		if (!shift) {
			if (cacheRenderMapNoShift != null) return cacheRenderMapNoShift;
		} else {
			if (cacheRenderMapShift != null) return cacheRenderMapShift;
		}
		Map<Minion, Long> prices = getMissing(shift);

		LinkedHashMap<String, OverviewLine> renderMap = new LinkedHashMap<>();
		if (prices.isEmpty()) {
			renderMap.put("all minions collected!", new OverviewText(Arrays.asList("No minions to craft avaliable!"), () -> {
				//TODO formatting
				Utils.addChatMessage("you can't craft anything rn!");

			}));
		} else {
			renderMap.put(
				"To craft: " + prices.size(),
				//TODO formatting
				new OverviewText(Arrays.asList("You can craft " + prices.size() + " more minions!"), () -> {
					Utils.addChatMessage("craft them now!");
				})
			);
			int i = 0;

			//TODO change
			int max = 9;

			Map<Minion, Long> sort = TrophyRewardOverlay.sortByValue(prices);
			for (Minion minion : sort.keySet()) {
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

				i++;
				if (i == max) break;
			}
		}

		if (shift) {
			cacheRenderMapShift = renderMap;
		} else {
			cacheRenderMapNoShift = renderMap;
		}

		return renderMap;
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
		for (OverviewLine overviewLine : renderMap.values()) {

			if (mouseX > x && mouseX < x + 130 &&
				mouseY > y + index && mouseY < y + 13 + index) {
				return overviewLine;
			}
			index += 10;
		}

		return null;
	}
}
