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

package io.github.moulberry.notenoughupdates.profileviewer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.core.GlScissorStack;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.profileviewer.data.APIDataJson;
import io.github.moulberry.notenoughupdates.profileviewer.hotm.HotmTreeRenderer;
import io.github.moulberry.notenoughupdates.util.Utils;
import lombok.var;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class MiningPage extends GuiProfileViewerPage {

	private static final ResourceLocation miningPageTexture = new ResourceLocation(
		"notenoughupdates:profile_viewer/mining/background.png");
	private static final ItemStack hotmSkillIcon = new ItemStack(Items.iron_pickaxe);
	private static final Map<String, EnumChatFormatting> crystalToColor =
		new LinkedHashMap<String, EnumChatFormatting>() {{
			put("jade", EnumChatFormatting.GREEN);
			put("amethyst", EnumChatFormatting.DARK_PURPLE);
			put("amber", EnumChatFormatting.GOLD);
			put("sapphire", EnumChatFormatting.AQUA);
			put("topaz", EnumChatFormatting.YELLOW);
			put("jasper", EnumChatFormatting.LIGHT_PURPLE);
			put("ruby", EnumChatFormatting.RED);
			put("opal", EnumChatFormatting.WHITE);
			put("aquamarine", EnumChatFormatting.BLUE);
			put("peridot", EnumChatFormatting.DARK_GREEN);
			put("onyx", EnumChatFormatting.DARK_GRAY);
			put("citrine", EnumChatFormatting.DARK_RED);
		}};

	public MiningPage(GuiProfileViewer instance) {
		super(instance);
	}

	@Override
	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		Minecraft.getMinecraft().getTextureManager().bindTexture(miningPageTexture);
		Utils.drawTexturedRect(guiLeft, guiTop, getInstance().sizeX, getInstance().sizeY, GL11.GL_NEAREST);

		SkyblockProfiles.SkyblockProfile selectedProfile = getSelectedProfile();
		if (selectedProfile == null) {
			return;
		}

		APIDataJson data = selectedProfile.getAPIDataJson();
		if (data == null) {
			return;
		}
		var core = data.mining_core;
		var nodes = core.nodes;
		JsonObject profileInfo = selectedProfile.getProfileJson();

		float xStart = 22;
		float yStartTop = 27;

		int x = guiLeft + 23;
		int y = guiTop + 25;
		int sectionWidth = 110;

		// Get stats
		JsonElement miningCore = profileInfo.get("mining_core");

		float mithrilPowder = core.powder_mithril;
		float gemstonePowder = core.powder_gemstone;
		float glacitePowder = core.powder_glacite;
		float mithrilPowderTotal = core.powder_spent_mithril;
		float gemstonePowderTotal = core.powder_spent_gemstone;
		float glacitePowderTotal = core.powder_spent_glacite;

		double nucleusRunsCompleted = Stream.of("amber", "amethyst", "jade", "sapphire", "topaz")
																				.mapToDouble(crystal -> Utils.getElementAsFloat(Utils.getElement(
																					miningCore,
																					"crystals." + crystal + "_crystal.total_placed"
																				), 0))
																				.min()
																				.orElse(0);

		// Render stats
		Map<String, ProfileViewer.Level> levelingInfo = selectedProfile.getLevelingInfo();
		if (levelingInfo != null) {
			ProfileViewer.Level hotmLevelingInfo = levelingInfo.get("hotm");

			// HOTM
			getInstance().renderXpBar(
				EnumChatFormatting.RED + "HOTM",
				hotmSkillIcon,
				x,
				y,
				sectionWidth,
				hotmLevelingInfo,
				mouseX,
				mouseY
			);
		}

		// Powder
		Utils.renderAlignedString(
			EnumChatFormatting.DARK_GREEN + "Mithril",
			"",
			guiLeft + xStart + 45,
			guiTop + yStartTop + 24,
			115
		);
		Utils.renderAlignedString(
			EnumChatFormatting.LIGHT_PURPLE + "Gemstone",
			"",
			guiLeft + xStart + 85,
			guiTop + yStartTop + 24,
			115
		);
		Utils.renderAlignedString(
			EnumChatFormatting.AQUA + "Glacite",
			"",
			guiLeft + xStart + 145,
			guiTop + yStartTop + 24,
			115
		);
		Utils.renderAlignedString(
			EnumChatFormatting.WHITE + "Powder:",
			"",
			guiLeft + xStart,
			guiTop + yStartTop + 44,
			115
		);
		Utils.renderAlignedString(
			EnumChatFormatting.DARK_GREEN + StringUtils.shortNumberFormat(mithrilPowder),
			"",
			guiLeft + xStart + 50,
			guiTop + yStartTop + 44,
			115
		);
		Utils.renderAlignedString(
			EnumChatFormatting.LIGHT_PURPLE + StringUtils.shortNumberFormat(gemstonePowder),
			"",
			guiLeft + xStart + 100,
			guiTop + yStartTop + 44,
			115
		);
		Utils.renderAlignedString(
			EnumChatFormatting.AQUA + StringUtils.shortNumberFormat(glacitePowder),
			"",
			guiLeft + xStart + 150,
			guiTop + yStartTop + 44,
			115
		);
		Utils.renderAlignedString(
			EnumChatFormatting.WHITE + "Total:",
			"",
			guiLeft + xStart,
			guiTop + yStartTop + 54,
			115
		);
		Utils.renderAlignedString(
			EnumChatFormatting.DARK_GREEN + StringUtils.shortNumberFormat(mithrilPowder + mithrilPowderTotal),
			"",
			guiLeft + xStart + 50,
			guiTop + yStartTop + 54,
			115
		);
		Utils.renderAlignedString(
			EnumChatFormatting.LIGHT_PURPLE + StringUtils.shortNumberFormat(gemstonePowder + gemstonePowderTotal),
			"",
			guiLeft + xStart + 100,
			guiTop + yStartTop + 54,
			115
		);
		Utils.renderAlignedString(
			EnumChatFormatting.AQUA + StringUtils.shortNumberFormat(glacitePowder + glacitePowderTotal),
			"",
			guiLeft + xStart + 150,
			guiTop + yStartTop + 54,
			115
		);
		{
			// Crystals
			int padding = 4;
			int rectStartX = 16;
			rectStartX += padding;
			int rectStartY = 97;
			rectStartY += padding;
			int rectXSize = 214;
			rectXSize -= padding * 2;
			int rectYSize = 89;
			int originalRectYSize = rectYSize;
			rectYSize -= padding * 2;
			int rowHeight = 10;
			int totalColumns = 2;
			rectYSize = rowHeight * (int) Math.ceil((((double) crystalToColor.size()) / totalColumns));
			int totalRows = rectYSize / rowHeight;

			int idx = 0;
			for (Map.Entry<String, EnumChatFormatting> crystal : crystalToColor.entrySet()) {

				int currentRow = idx % totalRows;
				int currentCol = idx / totalRows;
				int columnWidth = rectXSize / totalColumns - padding / totalColumns;
				int columnOffset = columnWidth + padding;

				String crystalState = Utils.getElementAsString(Utils.getElement(
					miningCore,
					"crystals." + crystal.getKey() + "_crystal.state"
				), "NOT_FOUND");
				String crystalStr = crystalState.equals("FOUND") ? "§a✔" : "§c✖";
				Utils.renderAlignedString(
					crystal.getValue() + WordUtils.capitalizeFully(crystal.getKey()) + ":",
					EnumChatFormatting.WHITE + crystalStr,
					guiLeft + rectStartX + currentCol * columnOffset,
					guiTop + rectStartY + currentRow * rowHeight,
					columnWidth
				);
				idx++;
			}

			Utils.renderAlignedString(
				EnumChatFormatting.BLUE + "Nucleus Runs Completed:",
				EnumChatFormatting.WHITE + StringUtils.formatNumber(nucleusRunsCompleted),
				guiLeft + rectStartX,
				guiTop + rectStartY + originalRectYSize - padding - 1.5F * Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT,
				rectXSize
			);
		}

		renderHotmTree(
			guiLeft + 249,
			guiTop + 16,
			guiLeft + 412,
			guiTop + 185,
			nodes,
			mouseX,
			mouseY
		);
	}

	int scroll = 0;

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private void renderHotmTree(
		int left, int top, int right, int bottom, Map<String, JsonElement> levels,
		int mouseX,
		int mouseY
	) {
		ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
		GlScissorStack.push(left, top, right, bottom, sr);
		var isHovered = left < mouseX && mouseX < right &&
			top < mouseY && mouseY < bottom;
		var renderer = HotmTreeRenderer.Companion
			.getRenderer();
		if (isHovered) {
			scroll += Mouse.getDWheel();
		}
		if (renderer != null) {
			var maxScroll = renderer.getYSize() * 24 - (bottom - top) - 24 / 2;
			scroll = Math.min(maxScroll + 4, Math.max(0, scroll));
			renderer.renderPerks(
				levels,
				left + (right - left) / 2 - renderer.getXSize() * 24 / 2 + 4 / 2,
				top - maxScroll + scroll, mouseX, mouseY,
				isHovered,
				20,
				4
			);
		} else {
			Utils.showOutdatedRepoNotification("hotmLayout.json");
		}
		GlScissorStack.pop(sr);
	}
}
