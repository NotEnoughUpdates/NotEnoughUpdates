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
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.GlScissorStack;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.profileviewer.data.APIDataJson;
import io.github.moulberry.notenoughupdates.profileviewer.hotm.HotmTreeRenderer;
import io.github.moulberry.notenoughupdates.recipes.ForgeRecipe;
import io.github.moulberry.notenoughupdates.recipes.NeuRecipe;
import io.github.moulberry.notenoughupdates.util.ItemResolutionQuery;
import io.github.moulberry.notenoughupdates.util.Utils;
import lombok.var;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class MiningPage extends GuiProfileViewerPage {

	private static final ResourceLocation miningPageTexture = new ResourceLocation(
		"notenoughupdates:profile_viewer/mining/background.png");
	private static final ResourceLocation FORGE_SLOT_BACKGROUND = new ResourceLocation("notenoughupdates:profile_viewer/mining/perk_background.png");

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
		ProfileViewer.Level hotmLevelingInfo = null;
		if (levelingInfo != null) {
			hotmLevelingInfo = levelingInfo.get("hotm");

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
			mouseY,
			hotmLevelingInfo
		);

		int forgeX = guiLeft + 149;
		int forgeY = guiTop + 20;

		Minecraft.getMinecraft().getTextureManager().bindTexture(FORGE_SLOT_BACKGROUND);
		Utils.drawTexturedRect(
			forgeX, forgeY,
			16, 16, 0F, 1f, 0f, 1f
		);

		ItemStack anvil = new ItemStack(Item.getItemFromBlock(Blocks.anvil));
		Utils.drawItemStack(anvil, forgeX, forgeY);

		if (mouseX >= forgeX && mouseX < forgeX + 16 && mouseY >= forgeY && mouseY < forgeY + 16) {
			if (data.forge == null) return;
			if (data.forge.forge_processes == null) return;
			Map<String, APIDataJson.ForgeData.ForgeProcessesData.Node> forgeData = data.forge.forge_processes.forge_1;
			if (forgeData == null) return;

			ArrayList<String> tooltip = new ArrayList<>();

			ItemResolutionQuery itemResolutionQuery = NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery();

			int hotmLevel = 0;
			if (hotmLevelingInfo != null) hotmLevel = (int) Math.floor(hotmLevelingInfo.level);
			ArrayList<String> forgeSlots = new ArrayList<String>() {{
				add("1: §cEmpty");
				add("2: §cEmpty");
				add("3: §7Locked");
				add("4: §7Locked");
				add("5: §7Locked");
				add("6: §7Locked");
				add("7: §7Locked");
			}};

			for (int i = 2; i < 7; i++) {
				if (hotmLevel > i) {
					forgeSlots.set(i, (i+1) + ": §cEmpty");
				}
			}

			long currentTime = System.currentTimeMillis();
			boolean showError = false;

			for (APIDataJson.ForgeData.ForgeProcessesData.Node value : forgeData.values()) {
				if (value.slot > 7) {
					forgeSlots.add(value.slot + ": §cEmpty");
				}

				String id = value.id;
				Set<NeuRecipe> recipes = NotEnoughUpdates.INSTANCE.manager.getRecipesFor(id);

				//just assuming that every forge pet will be legendary
				if (recipes.isEmpty() && value.type.equals("PETS")) {
					id = id + ";4";
					recipes = NotEnoughUpdates.INSTANCE.manager.getRecipesFor(id);
				}

				if (recipes != null) {
					ForgeRecipe forgeRecipe = null;
					for (NeuRecipe recipe : recipes) {
						if (recipe instanceof ForgeRecipe) {
							forgeRecipe = (ForgeRecipe) recipe;
							break;
						}
					}
					if (forgeRecipe == null) {
						tooltip.add("§cMissing recipe " + id);
						showError = true;
					}

					int level = 0;
					JsonObject hotm = miningCore.getAsJsonObject();
					if (hotm.has("nodes") && hotm.get("nodes").getAsJsonObject().has("forge_time")) {
						level = hotm.get("nodes").getAsJsonObject().get("forge_time").getAsInt();
					}
					int duration = 0;
					if (forgeRecipe != null) {
						duration = forgeRecipe.getReducedTime(level);
					}

					//convert to ms
					duration = duration * 1000;
					//we do this so all the timers update at the same time
					long startTime = (value.startTime / 1000 * 1000);
					long durationLeft = startTime + duration - currentTime;
					boolean finished  = durationLeft < 0;
					ItemStack itemStack = itemResolutionQuery.withKnownInternalName(id).resolveToItemStack();
					String displayName = id;
					if (itemStack == null) {
						tooltip.add("§cMissing item " + id);
						showError = true;
					} else {
						displayName = itemStack.getDisplayName().replace("[Lvl {LVL}] ", "");
					}

					if (forgeRecipe == null) {
						forgeSlots.set(value.slot - 1, value.slot + ": §4" + displayName + " §7Time Remaining: §cUnknown");
					} else {
						if (finished) {
							forgeSlots.set(value.slot - 1, value.slot + ": §4" + displayName + " §aCompleted!");
						} else {
							forgeSlots.set(
								value.slot - 1,
								value.slot + ": §4" + displayName + " §7Time Remaining: §a" + Utils.prettyTime(durationLeft)
							);
						}
					}

				} else {
					tooltip.add("§cCant find item: " + value.id);
					showError = true;
				}
			}

			if (showError) {
				tooltip.add("§cPlease report this to the NEU Discord");
				tooltip.add("§cdiscord.gg/moulberry");
			}

			tooltip.add(EnumChatFormatting.GOLD + "Forge");
			tooltip.add("");
			tooltip.addAll(forgeSlots);
			getInstance().tooltipToDisplay = tooltip;
		}
	}

	int scroll = 0;

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private void renderHotmTree(
		int left, int top, int right, int bottom, Map<String, JsonElement> levels,
		int mouseX,
		int mouseY,
		@Nullable ProfileViewer.Level hotmLevelingInfo
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
				hotmLevelingInfo,
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
