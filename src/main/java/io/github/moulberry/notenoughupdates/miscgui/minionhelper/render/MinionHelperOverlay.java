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
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.render.renderables.RenderableObject;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.render.renderables.RenderableText;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.MinionSource;
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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MinionHelperOverlay {
	private final MinionHelperManager manager;
	private final MinionHelperOverlayHover hover;

	private LinkedHashMap<String, RenderableObject> cacheRenderMapShift = null;
	private LinkedHashMap<String, RenderableObject> cacheRenderMapNoShift = null;

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

		LinkedHashMap<String, RenderableObject> renderMap = getRenderMap();

		hover.renderHover(renderMap);
		render(event, renderMap);
	}

	@SubscribeEvent
	public void onMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (!manager.inCraftedMinionsInventory()) return;
		if (!manager.isReadyToUse()) return;
		if (!Mouse.getEventButtonState()) return;

		RenderableObject mouseObject = getObjectOverMouse(getRenderMap());

		if (mouseObject != null) {
			if (mouseObject instanceof MinionSource) {
				event.setCanceled(true);
				Minion minion = ((MinionSource) mouseObject).getMinion();
				NotEnoughUpdates.INSTANCE.manager.displayGuiItemRecipe(minion.getInternalName());
			}
		}
	}

	private Map<Minion, Long> getMissing(boolean shift) {
		Map<Minion, Long> prices = new HashMap<>();
		for (Minion minion : manager.getAllMinions().values()) {

			if (!minion.doesMeetRequirements() && !shift) continue;
			if (!minion.isCrafted()) {
				long price = manager.getPriceCalculation().calculateUpgradeCosts(minion.getMinionSource(), true);
				prices.put(minion, price);
			}
		}
		return prices;
	}

	private void render(GuiScreenEvent.BackgroundDrawnEvent event, Map<String, RenderableObject> renderMap) {
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
		for (Map.Entry<String, RenderableObject> entry : renderMap.entrySet()) {
			String line = entry.getKey();
			RenderableObject renderableObject = entry.getValue();
			String prefix = "";
			if (renderableObject instanceof MinionSource) {
				Minion minion = ((MinionSource) renderableObject).getMinion();
				if (minion == hover.getLastHovered()) {
					prefix = "§e";
				}
			}
			fontRendererObj.drawString(prefix + line, a + 6, guiTop + 6 + extra, -1, false);
			extra += 10;
		}
	}

	private LinkedHashMap<String, RenderableObject> getRenderMap() {
		boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		if (!shift) {
			if (cacheRenderMapNoShift != null) return cacheRenderMapNoShift;
		} else {
			if (cacheRenderMapShift != null) return cacheRenderMapShift;
		}
		Map<Minion, Long> prices = getMissing(shift);

		LinkedHashMap<String, RenderableObject> renderMap = new LinkedHashMap<>();
		if (prices.isEmpty()) {
			renderMap.put("all minions collected!", new RenderableText("You have all the minions available collected! :)"));
		} else {
			renderMap.put(
				"To craft: " + prices.size(),
				//TODO formulierung
				new RenderableText("you can craft that many more minions!")
			);
			int i = 0;

			//TODO change
			int max = 20;

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
				String format = manager.getPriceCalculation().calculateUpgradeCostsFormat(minion.getMinionSource(), true);
				String requirementFormat = !minion.doesMeetRequirements() ? "§7§o" : "";
				renderMap.put(
					requirementFormat + displayName + "§r " + requirementFormat + minion.getTier() + " §r§8- " + format,
					minion.getMinionSource()
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

	RenderableObject getObjectOverMouse(LinkedHashMap<String, RenderableObject> renderMap) {
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
		for (RenderableObject renderableObject : renderMap.values()) {

			if (mouseX > x && mouseX < x + 130 &&
				mouseY > y + index && mouseY < y + 13 + index) {
				return renderableObject;
			}
			index += 10;
		}

		return null;
	}
}
