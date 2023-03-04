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

package io.github.moulberry.notenoughupdates.miscgui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.events.ButtonExclusionZoneEvent;
import io.github.moulberry.notenoughupdates.events.RepositoryReloadEvent;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Rectangle;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NEUAutoSubscribe
public class TrophyRewardOverlay {
	private static TrophyRewardOverlay instance = null;

	private final Map<String, Integer> data = new HashMap<>();
	private boolean reloadNeeded = true;

	public static final ResourceLocation trophyProfitImage =
		new ResourceLocation("notenoughupdates:trophy_profit.png");

	public static TrophyRewardOverlay getInstance() {
		if (instance == null) {
			instance = new TrophyRewardOverlay();
		}
		return instance;
	}

	/**
	 * This adds support for the /neureloadrepo command
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onRepoReload(RepositoryReloadEvent event) {
		reloadNeeded = true;
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onItemTooltipLow(ItemTooltipEvent event) {
		if (!inTrophyFishingInventory()) return;

		ItemStack itemStack = event.itemStack;
		if (itemStack == null) return;
		if (!"§aFillet Trophy Fish".equals(itemStack.getDisplayName())) return;

		event.toolTip.add(4, getToolTip());
		event.toolTip.add(4, "");
	}

	private String getToolTip() {
		List<String> line = createText();
		if (line.size() == 1) {
			return line.get(0);
		}

		return line.get(1);
	}

	@SubscribeEvent
	public void onButtonExclusionZones(ButtonExclusionZoneEvent event) {
		if (inTrophyFishingInventory()) {
			event.blockArea(
				new Rectangle(
					event.getGuiBaseRect().getRight(),
					event.getGuiBaseRect().getTop(),
					168 /*width*/ + 4 /*space*/,
					128
				),
				ButtonExclusionZoneEvent.PushDirection.TOWARDS_RIGHT
			);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onDrawBackground(GuiScreenEvent.BackgroundDrawnEvent event) {
		if (!inTrophyFishingInventory()) return;

		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		if (!(screen instanceof GuiChest)) return;
		Gui gui = event.gui;
		int xSize = ((AccessorGuiContainer) gui).getXSize();
		int guiLeft = ((AccessorGuiContainer) gui).getGuiLeft();
		int guiTop = ((AccessorGuiContainer) gui).getGuiTop();

		List<String> list = createText();
		int removed = 0;
		if (list.size() > 11) {
			while (list.size() > 10) {
				removed++;
				list.remove(9);
			}
			list.add("§8And " + removed + " more..");
		}
		renderBasicOverlay(event, guiLeft + xSize + 3, guiTop, list);
	}

	private void load() {
		data.clear();

		JsonObject jsonObject = Constants.TROPHYFISH;
		if (jsonObject == null) {
			return;
		}

		String[] tiers = new String[]{"_BRONZE", "_SILVER", "_GOLD", "_DIAMOND"};

		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String name = entry.getKey();

			int i = 0;
			for (JsonElement element : entry.getValue().getAsJsonArray()) {
				int price = element.getAsInt();
				data.put(name + tiers[i], price);
				i++;
			}
		}
	}

	private List<String> createText() {
		if (reloadNeeded) {
			load();
			reloadNeeded = false;
		}

		List<String> texts = new ArrayList<>();
		if (data.isEmpty()) {
			texts.add("§cNo data in Repo found!");
			return texts;
		}

		Map<String, Integer> totalAmount = new HashMap<>();
		Map<String, Integer> totalExchange = new HashMap<>();
		readInventory(totalAmount, totalExchange);

		int total = totalExchange.values().stream().mapToInt(value -> value).sum();
		texts.add("Trophy Fish Exchange");
		texts.add("Magma Fish: §e" + total);

		for (Map.Entry<String, Integer> entry : sortByValueReverse(totalExchange).entrySet()) {
			String name = entry.getKey();
			int amount = totalAmount.get(name);
			String[] split = name.split(" ");
			String rarity = split[split.length - 1];
			name = name.substring(0, name.length() - rarity.length() - 1);

			if (name.length() > 20) {
				name = name.substring(0, 18) + "..";
			}

			String rarityColor = rarity.replace("§l", "").substring(0, 2);
			texts.add(String.format("%s%dx §r%s§f: §e%d", rarityColor, amount, name, entry.getValue()));
		}

		return texts;
	}

	private void readInventory(Map<String, Integer> totalAmount, Map<String, Integer> totalExchange) {
		if (Minecraft.getMinecraft().thePlayer.openContainer instanceof ContainerChest) {

			for (Slot slot : Minecraft.getMinecraft().thePlayer.openContainer.inventorySlots) {
				if (!slot.getHasStack()) continue;
				ItemStack stack = slot.getStack();
				if (stack != null) {
					String internalId = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(stack);
					if (data.containsKey(internalId)) {
						String displayName = stack.getDisplayName();
						int stackSize = stack.stackSize;

						int amount = totalAmount.getOrDefault(displayName, 0) + stackSize;
						totalAmount.put(displayName, amount);

						int exchangeRate = data.get(internalId);
						int exchangeValue = totalExchange.getOrDefault(displayName, 0) + exchangeRate * stackSize;
						totalExchange.put(displayName, exchangeValue);
					}
				}
			}
		}
	}

	//TODO move into utils class maybe?
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Map.Entry.comparingByValue());

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

	//TODO move into utils class maybe?
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueReverse(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Map.Entry.comparingByValue());
		Collections.reverse(list);

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

	private void renderBasicOverlay(
		GuiScreenEvent.BackgroundDrawnEvent event,
		int x,
		int y,
		List<String> texts
	) {

		Gui gui = event.gui;
		int xSize = ((AccessorGuiContainer) gui).getXSize();
		int guiLeft = ((AccessorGuiContainer) gui).getGuiLeft();
		int guiTop = ((AccessorGuiContainer) gui).getGuiTop();
		Minecraft minecraft = Minecraft.getMinecraft();
		minecraft.getTextureManager().bindTexture(trophyProfitImage);
		GL11.glColor4f(1, 1, 1, 1);
		GlStateManager.disableLighting();

		Utils.drawTexturedRect(guiLeft + xSize + 4, guiTop, 158, 128, 0, 1, 0, 1, GL11.GL_NEAREST);

		int a = guiLeft + xSize + 4;
		FontRenderer fontRendererObj = minecraft.fontRendererObj;

		//Render first two header lines
		int i = 0;
		for (String text : texts) {
			fontRendererObj.drawString("§8" + text, a + 10, guiTop + 6 + i, -1, false);
			i += 10;
			if (i == 20) break;
		}

		//Render all other lines
		i = 25;
		int index = 0;
		for (String text : texts) {
			if (index > 1) {
				fontRendererObj.drawString(text, a + 10, guiTop + 6 + i, -1, false);
				i += 10;
			} else {
				index++;
			}
		}
	}

	public static boolean inTrophyFishingInventory() {
		if (!NotEnoughUpdates.INSTANCE.isOnSkyblock()) return false;
		if (!NotEnoughUpdates.INSTANCE.config.fishing.trophyRewardOverlay) return false;

		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft == null || minecraft.thePlayer == null) return false;

		Container inventoryContainer = minecraft.thePlayer.openContainer;
		if (!(inventoryContainer instanceof ContainerChest)) return false;
		ContainerChest containerChest = (ContainerChest) inventoryContainer;
		return containerChest.getLowerChestInventory().getDisplayName()
												 .getUnformattedText().equalsIgnoreCase("Trophy Fishing");
	}
}
