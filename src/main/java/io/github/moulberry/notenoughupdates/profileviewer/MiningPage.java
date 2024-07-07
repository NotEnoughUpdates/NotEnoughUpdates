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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MiningPage extends GuiProfileViewerPage {

	private static final ResourceLocation miningPageTexture = new ResourceLocation("notenoughupdates:pv_mining.png");
	private static final ItemStack hotmSkillIcon = new ItemStack(Items.iron_pickaxe);
	private static final Map<String, EnumChatFormatting> crystalToColor = new HashMap<String, EnumChatFormatting>() {{
		put("jade", EnumChatFormatting.GREEN);
		put("amethyst", EnumChatFormatting.DARK_PURPLE);
		put("amber", EnumChatFormatting.GOLD);
		put("sapphire", EnumChatFormatting.AQUA);
		put("topaz", EnumChatFormatting.YELLOW);
		put("jasper", EnumChatFormatting.LIGHT_PURPLE);
		put("ruby", EnumChatFormatting.RED);
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
		float mithrilPowderTotal = core.powder_spent_mithril;
		float gemstonePowderTotal = core.powder_spent_gemstone;

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
			EnumChatFormatting.DARK_GREEN + "Mithril Powder",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(mithrilPowder),
			guiLeft + xStart,
			guiTop + yStartTop + 24,
			115
		);
		Utils.renderAlignedString(
			EnumChatFormatting.LIGHT_PURPLE + "Gemstone Powder",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(gemstonePowder),
			guiLeft + xStart,
			guiTop + yStartTop + 44,
			115
		);
		Utils.renderAlignedString(
			EnumChatFormatting.DARK_GREEN + "Total Mithril Powder",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(mithrilPowderTotal + mithrilPowder),
			guiLeft + xStart,
			guiTop + yStartTop + 34,
			115
		);
		Utils.renderAlignedString(
			EnumChatFormatting.LIGHT_PURPLE + "Total Gemstone Powder",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(gemstonePowderTotal + gemstonePowder),
			guiLeft + xStart,
			guiTop + yStartTop + 54,
			115
		);

		// Crystals
		int idx = 0;
		for (Map.Entry<String, EnumChatFormatting> crystal : crystalToColor.entrySet()) {
			String crystalState = Utils.getElementAsString(Utils.getElement(
				miningCore,
				"crystals." + crystal.getKey() + "_crystal.state"
			), "NOT_FOUND");
			String crystalStr = crystalState.equals("FOUND") ? "§a✔" : "§c✖";
			Utils.renderAlignedString(
				crystal.getValue() + WordUtils.capitalizeFully(crystal.getKey()) + " Crystal:",
				EnumChatFormatting.WHITE + crystalStr,
				guiLeft + xStart,
				guiTop + yStartTop + 74 + idx * 10,
				110
			);
			idx++; // Move text down 10 px every crystal
		}

		Utils.renderAlignedString(
			EnumChatFormatting.BLUE + "Nucleus Runs Completed:",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(nucleusRunsCompleted),
			guiLeft + xStart,
			guiTop + yStartTop + 149,
			110
		);

		renderHotmTree(
			guiLeft + 249,
			guiTop + 19,
			guiLeft + 412,
			guiTop + 182,
			nodes,
			mouseX,
			mouseY
		);
	}

	private void renderHotmTree(
		int left, int top, int right, int bottom, Map<String, JsonElement> levels,
		int mouseX,
		int mouseY
	) {
		ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
		GlScissorStack.push(left, top, right, bottom, sr);
		var renderer = HotmTreeRenderer.Companion
			.getRenderer();
		if (renderer != null) {
			renderer.renderPerks(
				levels, left, top, mouseX, mouseY,
				left < mouseX && mouseX < right &&
					top < mouseY && mouseY < bottom,
				24
			);
		} else {
			Utils.showOutdatedRepoNotification("hotmLayout.json");
		}
		GlScissorStack.pop(sr);
	}

	/**
	 * Renders a standard HOTM perk that can be levelled.
	 */
	private void renderHotmPerk(
		int perkLevel,
		int xPosition,
		int yPosition,
		int mouseX,
		int mouseY,
		Supplier<ArrayList<String>> tooltipSupplier,
		int maxLevel
	) {
		renderHotmPerk(perkLevel, xPosition, yPosition, mouseX, mouseY, tooltipSupplier, false, maxLevel);
	}

	/**
	 * Renders a pickaxe ability that can be unlocked once and not levelled.
	 */
	private void renderPickaxeAbility(
		int perkLevel,
		int xPosition,
		int yPosition,
		int mouseX,
		int mouseY,
		Supplier<ArrayList<String>> tooltipSupplier
	) {
		renderHotmPerk(perkLevel, xPosition, yPosition, mouseX, mouseY, tooltipSupplier, true, -1);
	}

	/**
	 * Renders a HOTM perk. This method is only called from its overloads above.
	 */
	private void renderHotmPerk(
		int perkLevel,
		int xPosition,
		int yPosition,
		int mouseX,
		int mouseY,
		Supplier<ArrayList<String>> tooltipSupplier,
		boolean isPickaxeAbility,
		int maxLevel
	) {
		boolean unlocked = perkLevel > 0;
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.disableLighting();

		ItemStack itemStack;
		if (isPickaxeAbility) {
			RenderHelper.enableGUIStandardItemLighting(); // GUI standard item lighting must be enabled for items that are rendered as blocks, like emerald blocks.
			itemStack = new ItemStack(unlocked
				? Blocks.emerald_block
				: Blocks.coal_block); // Pickaxe abilities are rendered as blocks
		} else { // Non-pickaxe abilities are rendered as items
			itemStack = new ItemStack(unlocked ? (perkLevel >= maxLevel ? Items.diamond : Items.emerald) : Items.coal);
		}

		ArrayList<String> tooltip = tooltipSupplier.get();
		// Prepend the green, yellow, or red color on the first line of each tooltip depending on if the perk is unlocked
		tooltip.set(
			0,
			(unlocked
				? (perkLevel >= maxLevel ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW)
				: EnumChatFormatting.RED) +
				tooltip.get(0)
		);

		NBTTagCompound nbt = new NBTTagCompound(); //Adding NBT Data for Custom Resource Packs
		NBTTagCompound display = new NBTTagCompound();
		display.setString("Name", tooltip.get(0));
		nbt.setTag("display", display);
		itemStack.setTagCompound(nbt);

		Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(itemStack, xPosition, yPosition);
		GlStateManager.enableLighting();
		if (mouseX >= xPosition && mouseX < xPosition + 16) {
			if (mouseY >= yPosition && mouseY <= yPosition + 16) {
				Utils.drawHoveringText(tooltip, mouseX, mouseY, getInstance().width, getInstance().height, -1);
			}
		}
	}

	/**
	 * A separate method similar to the one above, but allowing the caller to specify an ItemStack to render.
	 * Used for rendering Peak of the Mountain and perks that are unlocked once and not upgraded.
	 */
	private void renderHotmPerk(
		int perkLevel,
		int xPosition,
		int yPosition,
		int mouseX,
		int mouseY,
		Supplier<ArrayList<String>> tooltipSupplier,
		ItemStack itemStack
	) {
		renderHotmPerk(perkLevel, xPosition, yPosition, mouseX, mouseY, tooltipSupplier, itemStack, false);
	}

	/**
	 * This method renders a HOTM perk using the provided ItemStack.
	 * It is overloaded by the method above, and is only called directly to render Peak of the Mountain.
	 */
	private void renderHotmPerk(
		int perkLevel,
		int xPosition,
		int yPosition,
		int mouseX,
		int mouseY,
		Supplier<ArrayList<String>> tooltipSupplier,
		ItemStack itemStack,
		boolean isRenderingBlock
	) {
		boolean unlocked = perkLevel > 0;
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.disableLighting();
		if (isRenderingBlock) RenderHelper.enableGUIStandardItemLighting();

		ArrayList<String> tooltip = tooltipSupplier.get();
		// Prepend the green or red color on the first line of each tooltip depending on if the perk is unlocked
		if (!tooltip.get(0).contains("Peak of the Mountain")) tooltip.set(
			0,
			(unlocked ? EnumChatFormatting.GREEN : EnumChatFormatting.RED) + tooltip.get(0)
		); //Peak of the Moutain has three color options, and is set already

		NBTTagCompound nbt = new NBTTagCompound(); //Adding NBT Data for Resource Packs
		NBTTagCompound display = new NBTTagCompound();
		display.setString("Name", tooltip.get(0));
		if (tooltip.get(0).contains("Peak of the Mountain")) display.setString("Lore", tooltip.get(1)); //Set Lore to Level
		nbt.setTag("display", display);
		itemStack.setTagCompound(nbt);

		Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(itemStack, xPosition, yPosition);
		GlStateManager.enableLighting();
		if (mouseX >= xPosition && mouseX < xPosition + 16) {
			if (mouseY >= yPosition && mouseY <= yPosition + 16) {
				Utils.drawHoveringText(tooltip, mouseX, mouseY, getInstance().width, getInstance().height, -1);
			}
		}
	}
}
