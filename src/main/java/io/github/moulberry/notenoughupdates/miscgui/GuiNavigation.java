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

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.GuiElementTextField;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GuiNavigation extends GuiScreen {

	public static ResourceLocation BACKGROUND = new ResourceLocation(
		"notenoughupdates",
		"textures/gui/navigation.png"
	);

	public static final int PIN_POSITION_U = 182;
	public static final int PIN_POSITION_V = 3;
	public static final int TICK_POSITION_U = 182;
	public static final int TICK_POSITION_V = 34;
	public static final int ICON_SIZE = 26;

	public static final int SEARCH_BAR_X = 14;
	public static final int SEARCH_BAR_Y = 11;
	public static final int SEARCH_BAR_WIDTH = 151;
	public static final int SEARCH_BAR_HEIGHT = 24;

	public static final int LIST_START_X = 14;
	public static final int LIST_START_Y = 43;
	public static final int LIST_OFFSET_Y = 28;
	public static final int TEXT_OFFSET_X = 28;
	public static final int LIST_COUNT = 6;

	List<String> searchResults = new ArrayList<>();

	public int xSize = 176;
	public int ySize = 222;
	public int guiLeft, guiTop;

	public GuiElementTextField textField = new GuiElementTextField("", SEARCH_BAR_WIDTH, SEARCH_BAR_HEIGHT, 0);

	@Override
	public void initGui() {
		super.initGui();
		NotEnoughUpdates.INSTANCE.config.hidden.hasOpenedWaypointMenu = true;
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		textField.render(guiLeft + SEARCH_BAR_X, guiTop + SEARCH_BAR_Y);

		refreshResults();
		for (int i = 0; i < LIST_COUNT; i++) {
			if (i < searchResults.size()) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND);
				String name = searchResults.get(i);
				JsonObject json = NotEnoughUpdates.INSTANCE.navigation.getWaypoints().get(name);

				//to prevent an NPE when trying to render a waypoint from AbiphoneContactExtraInformation
				if (json == null) continue;

				boolean selected = name.equals(NotEnoughUpdates.INSTANCE.navigation.getInternalname());
				int baseX = guiLeft + LIST_START_X;
				int baseY = guiTop + LIST_START_Y + LIST_OFFSET_Y * i;

				GlStateManager.color(1F, 1F, 1F);
				drawTexturedModalRect(
					baseX,
					baseY,
					selected ? TICK_POSITION_U : PIN_POSITION_U, selected ? TICK_POSITION_V : PIN_POSITION_V,
					ICON_SIZE, ICON_SIZE
				);
				Utils.drawStringF(
					json.get("displayname").getAsString(),
					baseX + TEXT_OFFSET_X,
					baseY + LIST_OFFSET_Y / 2F - Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT / 2F,
					false,
					0x000000
				);
			}
		}
	}

	private void refreshResults() {
		String text = textField.getText().toLowerCase();
		List<String> results = NotEnoughUpdates.INSTANCE.navigation
			.getWaypoints()
			.values()
			.stream()
			.filter(it ->
				it.get("internalname").getAsString().toLowerCase().contains(text)
					|| it.get("displayname").getAsString().toLowerCase().contains(text))
			.map(it -> it.get("internalname").getAsString())
			.sorted(Comparator.comparing(String::length)
												.thenComparing(String.CASE_INSENSITIVE_ORDER))
			.collect(Collectors.toList());

		String internalname = NotEnoughUpdates.INSTANCE.navigation.getInternalname();
		if (internalname != null) {
			results.remove(internalname);
			results.add(0, internalname);
		}
		searchResults = results;
	}

	@Override
	protected void keyTyped(char p_keyTyped_1_, int p_keyTyped_2_) throws IOException {
		super.keyTyped(p_keyTyped_1_, p_keyTyped_2_);
		textField.keyTyped(p_keyTyped_1_, p_keyTyped_2_);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (Utils.isWithinRect(
			mouseX,
			mouseY,
			guiLeft + SEARCH_BAR_X,
			guiTop + SEARCH_BAR_Y,
			SEARCH_BAR_WIDTH,
			SEARCH_BAR_HEIGHT
		)) {
			textField.mouseClicked(mouseX, mouseY, mouseButton);
		} else {
			textField.setFocus(false);
		}
		for (int i = 0; i < LIST_COUNT; i++) {
			if (i < searchResults.size()) {
				int baseX = guiLeft + LIST_START_X;
				int baseY = guiTop + LIST_START_Y + LIST_OFFSET_Y * i;
				if (Utils.isWithinRect(mouseX, mouseY, baseX, baseY, ICON_SIZE, ICON_SIZE)) {
					String thing = searchResults.get(i);
					boolean selected = thing.equals(NotEnoughUpdates.INSTANCE.navigation.getInternalname());
					if (selected) {
						NotEnoughUpdates.INSTANCE.navigation.untrackWaypoint();
					} else {
						NotEnoughUpdates.INSTANCE.navigation.trackWaypoint(thing);
					}
					Utils.playPressSound();
				}
			}
		}
	}
}
