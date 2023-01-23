/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.profileviewer.level;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUOverlay;
import io.github.moulberry.notenoughupdates.profileviewer.BasicPage;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.level.task.CoreTaskLevel;
import io.github.moulberry.notenoughupdates.profileviewer.level.task.DungeonTaskLevel;
import io.github.moulberry.notenoughupdates.profileviewer.level.task.EssenceTaskLevel;
import io.github.moulberry.notenoughupdates.profileviewer.level.task.MiscTaskLevel;
import io.github.moulberry.notenoughupdates.profileviewer.level.task.SkillRelatedTaskLevel;
import io.github.moulberry.notenoughupdates.profileviewer.level.task.SlayingTaskLevel;
import io.github.moulberry.notenoughupdates.profileviewer.level.task.StoryTaskLevel;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

public class LevelPage {

	private final GuiProfileViewer instance;
	private final BasicPage basicPage;
	private ProfileViewer.Profile profile;

	private String profileId;

	private int guiLeft, guiTop;

	private JsonObject constant;

	private final CoreTaskLevel coreTaskLevel;
	private final DungeonTaskLevel dungeonTaskLevel;
	private final EssenceTaskLevel essenceTaskLevel;
	private final MiscTaskLevel miscTaskLevel;
	private final SkillRelatedTaskLevel skillRelatedTaskLevel;
	private final SlayingTaskLevel slayingTaskLevel;
	private final StoryTaskLevel storyTaskLevel;

	private static final ResourceLocation pv_levels = new ResourceLocation("notenoughupdates:pv_levels.png");

	public LevelPage(GuiProfileViewer instance, BasicPage basicPage) {
		this.instance = instance;
		this.basicPage = basicPage;
		constant = Constants.SBLEVELS;

		coreTaskLevel = new CoreTaskLevel(this);
		dungeonTaskLevel = new DungeonTaskLevel(this);
		essenceTaskLevel = new EssenceTaskLevel(this);
		miscTaskLevel = new MiscTaskLevel(this);
		skillRelatedTaskLevel = new SkillRelatedTaskLevel(this);
		slayingTaskLevel = new SlayingTaskLevel(this);
		storyTaskLevel = new StoryTaskLevel(this);
	}

	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		this.guiLeft = GuiProfileViewer.getGuiLeft();
		this.guiTop = GuiProfileViewer.getGuiTop();
		this.profile = GuiProfileViewer.getProfile();
		this.profileId = GuiProfileViewer.getProfileId();

		basicPage.drawSideButtons();

		if (constant == null) {
			Utils.showOutdatedRepoNotification();
			return;
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_levels);
		Utils.drawTexturedRect(guiLeft, guiTop, instance.sizeX, instance.sizeY, GL11.GL_NEAREST);

		double skyblockLevel = profile.getSkyblockLevel(profileId);
		JsonObject profileInfo = profile.getProfileInformation(profileId);

		drawMainBar(skyblockLevel, mouseX, mouseY, guiLeft, guiTop);
		coreTaskLevel.drawTask(profileInfo, mouseX, mouseY, guiLeft, guiTop);
		dungeonTaskLevel.drawTask(profileInfo, mouseX, mouseY, guiLeft, guiTop);
		essenceTaskLevel.drawTask(profileInfo, mouseX, mouseY, guiLeft, guiTop);
		miscTaskLevel.drawTask(profileInfo, mouseX, mouseY, guiLeft, guiTop);
		skillRelatedTaskLevel.drawTask(profileInfo, mouseX, mouseY, guiLeft, guiTop);
		slayingTaskLevel.drawTask(profileInfo, mouseX, mouseY, guiLeft, guiTop);
		storyTaskLevel.drawTask(profileInfo, mouseX, mouseY, guiLeft, guiTop);
	}

	private void drawMainBar(double skyblockLevel, int mouseX, int mouseY, int guiLeft, int guiTop) {
		instance.renderLevelBar(
			"Level",
			BasicPage.skull,
			guiLeft + 163,
			guiTop + 30,
			110,
			skyblockLevel,
			(skyblockLevel - (long) skyblockLevel) * 100,
			100,
			mouseX,
			mouseY,
			false,
			Collections.emptyList()
		);
	}

	public String buildLore(String name, double xpGotten, double xpGainful, boolean hasNoLimit) {
		String xpGottenFormatted = NumberFormat.getInstance().format((int) xpGotten);
		String xpGainfulFormatted = NumberFormat.getInstance().format((int) xpGainful);

		if (xpGainful == 0 && xpGotten == 0 && !hasNoLimit) {
			return EnumChatFormatting.GOLD + name + ": §c§lNOT DETECTABLE!";
		}
		if (hasNoLimit) {
			return EnumChatFormatting.GOLD + name + ": " + EnumChatFormatting.YELLOW + xpGottenFormatted + " XP";
		}
		int percentage = (int) ((xpGotten / xpGainful) * 100);
		if (xpGotten >= xpGainful) {
			return EnumChatFormatting.GOLD + name + ": " + EnumChatFormatting.GREEN
				+ percentage + "%" + " §8(" + xpGottenFormatted + "/" + xpGainfulFormatted + " XP)";
		} else if (xpGotten == -1) {
			return EnumChatFormatting.GOLD + name + ": §c§lCOLLECTION DISABLED!";
		} else {

			return EnumChatFormatting.GOLD + name + ": " + EnumChatFormatting.YELLOW
				+ percentage + "%" + " §8(" + xpGottenFormatted + "/" + xpGainfulFormatted + " XP)";
		}
	}

	public JsonObject getConstant() {
		return constant;
	}

	public ProfileViewer.Profile getProfile() {
		return profile;
	}

	public String getProfileId() {
		return profileId;
	}

	public GuiProfileViewer getInstance() {
		return instance;
	}
}
