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

package io.github.moulberry.notenoughupdates.miscgui.minionhelper;

import com.google.common.collect.ArrayListMultimap;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscgui.TrophyRewardOverlay;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.renderables.RenderableObject;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.renderables.RenderableText;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.MinionRequirement;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.CraftingSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.CustomSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.MinionSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.NpcSource;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MinionHelperOverlay {
	private static MinionHelperOverlay instance = null;
	private final MinionHelperManager manager = MinionHelperManager.getInstance();
	private Minion lastHovered = null;

	private LinkedHashMap<String, RenderableObject> cacheShift = null;
	private LinkedHashMap<String, RenderableObject> cacheNoShift = null;

	public static MinionHelperOverlay getInstance() {
		if (instance == null) {
			instance = new MinionHelperOverlay();
		}
		return instance;
	}

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		cacheShift = null;
		cacheNoShift = null;
	}

	public static final ResourceLocation auctionProfitImage =
		new ResourceLocation("notenoughupdates:auction_profit.png");

	@SubscribeEvent
	public void onDrawBackground(GuiScreenEvent.BackgroundDrawnEvent event) {
		if (!manager.inCraftedMinionsInventory()) return;

		boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		LinkedHashMap<String, RenderableObject> renderMap = getRenderMap(shift);

		renderHover(renderMap, shift);
		render(event, renderMap);
	}

	@SubscribeEvent
	public void onMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (!manager.inCraftedMinionsInventory()) return;
		if (!Mouse.getEventButtonState()) return;

		boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		LinkedHashMap<String, RenderableObject> renderMap = getRenderMap(shift);

		RenderableObject mouseObject = getObjectOverMouse(renderMap);

		if (mouseObject != null) {
			if (mouseObject instanceof MinionSource) {
				event.setCanceled(true);
				Minion minion = ((MinionSource) mouseObject).getMinion();
				NotEnoughUpdates.INSTANCE.manager.displayGuiItemRecipe(minion.getInternalName());
			}
		}
	}

	private RenderableObject getObjectOverMouse(LinkedHashMap<String, RenderableObject> renderMap) {
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

	private void renderHover(
		LinkedHashMap<String, RenderableObject> renderMap,
		boolean shift
	) {
		lastHovered = null;

		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;

		final ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		final int scaledWidth = scaledresolution.getScaledWidth();
		final int scaledHeight = scaledresolution.getScaledHeight();
		int mouseX = Mouse.getX() * scaledWidth / Minecraft.getMinecraft().displayWidth;
		int mouseY = scaledHeight - Mouse.getY() * scaledHeight / Minecraft.getMinecraft().displayHeight - 1;

		RenderableObject mouseObject = getObjectOverMouse(renderMap);
		if (mouseObject != null) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(2f / scaledresolution.getScaleFactor(), 2f / scaledresolution.getScaleFactor(), 1);
			Utils.drawHoveringText(getTooltip(shift, mouseObject),
				mouseX * scaledresolution.getScaleFactor() / 2,
				mouseY * scaledresolution.getScaleFactor() / 2,
				scaledWidth * scaledresolution.getScaleFactor() / 2,
				scaledHeight * scaledresolution.getScaleFactor() / 2, -1, Minecraft.getMinecraft().fontRendererObj
			);
			GlStateManager.popMatrix();
		}
	}

	private List<String> getTooltip(boolean shift, RenderableObject renderableObject) {
		List<String> lines = new ArrayList<>();

		if (renderableObject instanceof RenderableText) {
			RenderableText renderableText = (RenderableText) renderableObject;
			lines.addAll(renderableText.getLines());
		} else if (renderableObject instanceof MinionSource) {
			MinionSource minionSource = (MinionSource) renderableObject;

			Minion minion = minionSource.getMinion();
			lastHovered = minion;
			String displayName = minion.getDisplayName();
			lines.add(displayName + " " + minion.getTier());
			for (MinionRequirement requirement : manager.getRequirements(minionSource.getMinion())) {
				String color = manager.meetRequirement(minion, requirement) ? "§a" : "§c";
				lines.add("  " + color + "Requirement: §f" + requirement.printDescription());
			}

			if (minionSource instanceof CraftingSource) {
				CraftingSource craftingSource = (CraftingSource) minionSource;
				lines.add("");
				if (shift || minion.getTier() == 1) {
					lines.add("Full crafting costs:");
				} else {
					lines.add("Upgrade costs:");
				}
				String format = manager.calculateUpgradeCostsFormat(craftingSource, !shift);
				lines.add(format);

				lines.add("");

				Map<String, Integer> allItems = grabAllItems(craftingSource.getItems());

				lines.add("Crafting recipe requires:");
				for (Map.Entry<String, Integer> entry : allItems.entrySet()) {
					String name = entry.getKey();
					int amount = entry.getValue();
					lines.add(name + ": " + amount);
				}
			} else if (minionSource instanceof NpcSource) {
				NpcSource npcSource = (NpcSource) minionSource;
				int coins = npcSource.getCoins();
				String npcName = npcSource.getNpcName();
				lines.add("");
				lines.add("Can bet bought at the NPC");
				lines.add("name: " + npcName);
				lines.add("");
				lines.add("Cost:");
				String format = Utils.shortNumberFormat(coins, 0);
				String result = "§6" + format + " coins";
				lines.add("  coins: " + result);
				lines.add("  Items:");
				Map<String, Integer> allItems = grabAllItems(npcSource.getItems());
				for (Map.Entry<String, Integer> entry : allItems.entrySet()) {
					String name = entry.getKey();
					int amount = entry.getValue();
					lines.add("   " + name + ": " + amount);
				}

			} else if (minionSource instanceof CustomSource) {
				CustomSource customSource = (CustomSource) minionSource;
				String source = customSource.getCustomSource();
				lines.add("");
				lines.add(source);
			}

			lines.add("");
			lines.add("§eClick to view recipe");
			if (!shift) {
				lines.add("");
				lines.add("§8[SHIFT show full costs]");
			}
		}
		return lines;
	}

	private Map<String, Integer> grabAllItems(ArrayListMultimap<String, Integer> multimap) {
		Map<String, Integer> allItems = new HashMap<>();
		for (Map.Entry<String, Integer> entry : multimap.entries()) {
			String name = entry.getKey();
			int amount = entry.getValue();
			amount = allItems.getOrDefault(name, 0) + amount;
			allItems.put(name, amount);
		}
		return allItems;
	}

	private Map<Minion, Long> getMissing(boolean shift) {

		Map<Minion, Long> prices = new HashMap<>();
		for (Minion minion : manager.getAllMinions().values()) {

			if (!minion.doesMeetRequirements() && !shift) continue;
			if (!minion.isCrafted()) {
				long price = manager.calculateUpgradeCosts(minion.getMinionSource(), true);
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
				if (lastHovered == minion) {
					prefix = "§e";
				}
			}
			fontRendererObj.drawString(prefix + line, a + 6, guiTop + 6 + extra, -1, false);
			extra += 10;
		}
	}

	private LinkedHashMap<String, RenderableObject> getRenderMap(boolean shift) {
		if (!shift) {
			if (cacheNoShift != null) return cacheNoShift;
		} else {
			if (cacheShift != null) return cacheShift;
		}
		Map<Minion, Long> prices = getMissing(shift);

		LinkedHashMap<String, RenderableObject> renderMap = new LinkedHashMap<>();
		if (prices.isEmpty()) {
			renderMap.put("all minions collected!", new RenderableText("You have all the minions available collected! :)"));
		} else {
			renderMap.put(
				"To craft: " + prices.size(),
				new RenderableText("you can craft that many more minions!")
			);//TODO formulierung
			int i = 0;

			//TOOD change
			int max = 20;

			Map<Minion, Long> sort = TrophyRewardOverlay.sortByValue(prices);
			for (Minion minion : sort.keySet()) {
				String displayName = minion.getDisplayName();
				displayName = displayName.replace(" Minion", "");
				String format = manager.calculateUpgradeCostsFormat(minion.getMinionSource(), true);
				String requirementFormat = !minion.doesMeetRequirements() ? "§7§o" : "";
				renderMap.put(
					requirementFormat + displayName + "§r " + requirementFormat + minion.getTier() + "§r §8- " + format,
					minion.getMinionSource()
				);

				i++;
				if (i == max) break;
			}
		}

		if (shift) {
			cacheShift = renderMap;
		} else {
			cacheNoShift = renderMap;
		}

		return renderMap;
	}
}
