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
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.Minion;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperManager;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.render.renderables.RenderableObject;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.render.renderables.RenderableText;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.CollectionRequirement;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.MinionRequirement;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.CraftingSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.MinionSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.NpcSource;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MinionHelperOverlayHover {

	private final MinionHelperOverlay overlay;
	private final MinionHelperManager manager;
	private Minion lastHovered = null;

	public MinionHelperOverlayHover(MinionHelperOverlay overlay, MinionHelperManager manager) {
		this.overlay = overlay;
		this.manager = manager;
	}

	void renderHover(LinkedHashMap<String, RenderableObject> renderMap) {
		lastHovered = null;

		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;

		final ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		final int scaledWidth = scaledresolution.getScaledWidth();
		final int scaledHeight = scaledresolution.getScaledHeight();
		int mouseX = Mouse.getX() * scaledWidth / Minecraft.getMinecraft().displayWidth;
		int mouseY = scaledHeight - Mouse.getY() * scaledHeight / Minecraft.getMinecraft().displayHeight - 1;

		RenderableObject mouseObject = overlay.getObjectOverMouse(renderMap);
		if (mouseObject != null) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(2f / scaledresolution.getScaleFactor(), 2f / scaledresolution.getScaleFactor(), 1);
			Utils.drawHoveringText(getTooltip(mouseObject),
				mouseX * scaledresolution.getScaleFactor() / 2,
				mouseY * scaledresolution.getScaleFactor() / 2,
				scaledWidth * scaledresolution.getScaleFactor() / 2,
				scaledHeight * scaledresolution.getScaleFactor() / 2, -1, Minecraft.getMinecraft().fontRendererObj
			);
			GlStateManager.popMatrix();
		}
	}

	private List<String> getTooltip(RenderableObject renderableObject) {
		List<String> lines = new ArrayList<>();

		if (renderableObject instanceof RenderableText) {
			RenderableText renderableText = (RenderableText) renderableObject;
			lines.addAll(renderableText.getLines());
		} else if (renderableObject instanceof Minion) {

			Minion minion = (Minion) renderableObject;
			MinionSource minionSource = minion.getMinionSource();
			lastHovered = minion;
			String displayName = minion.getDisplayName();
			lines.add("§9" + displayName + " " + minion.getTier());
			List<MinionRequirement> requirements = manager.getRequirementsManager().getRequirements(minion);
			if (!requirements.isEmpty()) {
				for (MinionRequirement requirement : requirements) {
					//TODO maybe change the §7 color
					String color = manager.getRequirementsManager().meetRequirement(minion, requirement) ? "§a" : "§7";
					if (requirement instanceof CollectionRequirement && manager.getApi().isCollectionApiDisabled()) {
						color = "§cAPI DISABLED! §7";
					}
					lines.add(" §8- " + color + requirement.printDescription());
				}
			} else {
				lines.add("§cNo requirements loaded!");
			}

			if (minionSource instanceof CraftingSource) {
				CraftingSource craftingSource = (CraftingSource) minionSource;
				lines.add("");
				String format = manager.getPriceCalculation().calculateUpgradeCostsFormat(minion, true);
				if (minion.getTier() == 1) {
					lines.add("§7Full crafting costs: " + format);
				} else {
					lines.add("§7Upgrade costs: " + format);
				}
				formatItems(lines, grabAllItems(craftingSource.getItems()));

			} else if (minionSource instanceof NpcSource) {
				NpcSource npcSource = (NpcSource) minionSource;
				String npcName = npcSource.getNpcName();
				lines.add("");
				lines.add("§7Buy from: §9" + npcName + " (NPC)");
				lines.add("");
				lines.add("§7Buy costs: " + manager.getPriceCalculation().calculateUpgradeCostsFormat(minion, true));
				lines.add(" §8- " + manager.getPriceCalculation().formatCoins(npcSource.getCoins()));
				formatItems(lines, grabAllItems(npcSource.getItems()));
			}

			lines.add("");
			lines.add("§eClick to view recipe");
		}
		return lines;
	}

	private void formatItems(List<String> lines, Map<String, Integer> allItems) {
		for (Map.Entry<String, Integer> entry : allItems.entrySet()) {
			String internalName = entry.getKey();
			String name = manager.getRepo().getDisplayName(internalName);

			int amount = entry.getValue();
			String amountText = amount != 1 ? amount + "§7x " : "";
			String price = manager.getPriceCalculation().formatCoins(
				manager.getPriceCalculation().getPrice(internalName) * amount);
			lines.add(" §8- §a" + amountText + "§f" + name + " " + price);
		}
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

	public Minion getLastHovered() {
		return lastHovered;
	}
}
