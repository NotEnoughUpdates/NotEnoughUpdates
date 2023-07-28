/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.cosmetics.ShaderManager;
import io.github.moulberry.notenoughupdates.itemeditor.GuiElementTextField;
import io.github.moulberry.notenoughupdates.profileviewer.bestiary.BestiaryPage;
import io.github.moulberry.notenoughupdates.profileviewer.rift.RiftPage;
import io.github.moulberry.notenoughupdates.profileviewer.trophy.TrophyFishPage;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.DungeonsWeight;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.SkillsWeight;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.Weight;
import io.github.moulberry.notenoughupdates.util.AsyncDependencyLoader;
import io.github.moulberry.notenoughupdates.util.PronounDB;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Matrix4f;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GuiProfileViewer extends GuiScreen {

	public static final ResourceLocation pv_dropdown = new ResourceLocation("notenoughupdates:pv_dropdown.png");
	public static final ResourceLocation pv_bg = new ResourceLocation("notenoughupdates:pv_bg.png");
	public static final ResourceLocation pv_elements = new ResourceLocation("notenoughupdates:pv_elements.png");
	private static final Map<String, ResourceLocation> gamemodeToIcon = new HashMap<String, ResourceLocation>() {{
		put("ironman", new ResourceLocation("notenoughupdates:pv_ironman.png"));
		put("bingo", new ResourceLocation("notenoughupdates:pv_bingo.png"));
		put("island", new ResourceLocation("notenoughupdates:pv_stranded.png")); // Stranded
	}};
	private static final ResourceLocation gamemodeIconUnknown = new ResourceLocation("notenoughupdates:pv_unknown.png");
	public static final ResourceLocation resource_packs =
		new ResourceLocation("minecraft:textures/gui/resource_packs.png");
	public static final ResourceLocation icons = new ResourceLocation("textures/gui/icons.png");
	public static final HashMap<String, HashMap<String, Float>> PET_STAT_BOOSTS =
		new HashMap<String, HashMap<String, Float>>() {
			{
				put(
					"PET_ITEM_BIG_TEETH_COMMON",
					new HashMap<String, Float>() {
						{
							put("CRIT_CHANCE", 5f);
						}
					}
				);
				put(
					"PET_ITEM_HARDENED_SCALES_UNCOMMON",
					new HashMap<String, Float>() {
						{
							put("DEFENCE", 25f);
						}
					}
				);
				put(
					"PET_ITEM_LUCKY_CLOVER",
					new HashMap<String, Float>() {
						{
							put("MAGIC_FIND", 7f);
						}
					}
				);
				put(
					"PET_ITEM_SHARPENED_CLAWS_UNCOMMON",
					new HashMap<String, Float>() {
						{
							put("CRIT_DAMAGE", 15f);
						}
					}
				);
			}
		};
	public static final HashMap<String, HashMap<String, Float>> PET_STAT_BOOSTS_MULT =
		new HashMap<String, HashMap<String, Float>>() {
			{
				put(
					"PET_ITEM_IRON_CLAWS_COMMON",
					new HashMap<String, Float>() {
						{
							put("CRIT_DAMAGE", 1.4f);
							put("CRIT_CHANCE", 1.4f);
						}
					}
				);
				put(
					"PET_ITEM_TEXTBOOK",
					new HashMap<String, Float>() {
						{
							put("INTELLIGENCE", 2f);
						}
					}
				);
			}
		};
	public static final HashMap<String, String> RARITY_TO_NUM = new HashMap<String, String>() {
		{
			put("COMMON", "0");
			put("UNCOMMON", "1");
			put("RARE", "2");
			put("EPIC", "3");
			put("LEGENDARY", "4");
			put("MYTHIC", "5");
		}
	};
	public static ProfileViewerPage currentPage = ProfileViewerPage.BASIC;
	private static int guiLeft;
	private static int guiTop;
	private static SkyblockProfiles profile;
	private static String profileName = null;
	public static AsyncDependencyLoader<Optional<PronounDB.PronounChoice>> pronouns =
		AsyncDependencyLoader.withEqualsInvocation(
			() ->
				NotEnoughUpdates.INSTANCE.config.profileViewer.showPronounsInPv
					? Optional.ofNullable(profile).map(it -> Utils.parseDashlessUUID(it.getUuid()))
					: Optional.<UUID>empty(),
			uuid -> uuid.isPresent()
				? PronounDB.getPronounsFor(uuid.get())
				: CompletableFuture.completedFuture(Optional.empty())
		);
	public final GuiElementTextField playerNameTextField;
	public final GuiElementTextField inventoryTextField = new GuiElementTextField("", GuiElementTextField.SCALE_TEXT);
	public final GuiElementTextField killDeathSearchTextField = new GuiElementTextField(
		"",
		GuiElementTextField.SCALE_TEXT
	);
	private final Map<ProfileViewerPage, GuiProfileViewerPage> pages = new HashMap<>();
	public int sizeX;
	public int sizeY;
	public float backgroundRotation = 0;
	public long currentTime = 0;
	public long lastTime = 0;
	public long startTime = 0;
	public List<String> tooltipToDisplay = null;
	private Shader blurShaderHorz = null;
	private Framebuffer blurOutputHorz = null;
	private Shader blurShaderVert = null;
	private Framebuffer blurOutputVert = null;
	private boolean profileDropdownSelected = false;
	private double lastBgBlurFactor = -1;
	private boolean showBingoPage;

	public GuiProfileViewer(SkyblockProfiles profile) {
		GuiProfileViewer.profile = profile;
		GuiProfileViewer.profileName = profile.getLatestProfileName();

		String playerName = "";
		if (profile.getHypixelProfile() != null) {
			playerName = profile.getHypixelProfile().get("displayname").getAsString();
		}
		playerNameTextField = new GuiElementTextField(playerName, GuiElementTextField.SCALE_TEXT);
		playerNameTextField.setSize(100, 20);

		if (currentPage == ProfileViewerPage.LOADING) {
			currentPage = ProfileViewerPage.BASIC;
		}

		pages.put(ProfileViewerPage.BASIC, new BasicPage(this));
		pages.put(ProfileViewerPage.DUNGEON, new DungeonPage(this));
		pages.put(ProfileViewerPage.EXTRA, new ExtraPage(this));
		pages.put(ProfileViewerPage.INVENTORIES, new InventoriesPage(this));
		pages.put(ProfileViewerPage.COLLECTIONS, new CollectionsPage(this));
		pages.put(ProfileViewerPage.PETS, new PetsPage(this));
		pages.put(ProfileViewerPage.MINING, new MiningPage(this));
		pages.put(ProfileViewerPage.BINGO, new BingoPage(this));
		pages.put(ProfileViewerPage.TROPHY_FISH, new TrophyFishPage(this));
		pages.put(ProfileViewerPage.BESTIARY, new BestiaryPage(this));
		pages.put(ProfileViewerPage.CRIMSON_ISLE, new CrimsonIslePage(this));
		pages.put(ProfileViewerPage.MUSEUM, new MuseumPage(this));
		pages.put(ProfileViewerPage.RIFT, new RiftPage(this));
	}

	public static int getGuiLeft() {
		return guiLeft;
	}

	public static int getGuiTop() {
		return guiTop;
	}

	public static SkyblockProfiles getProfile() {
		return profile;
	}

	public static String getProfileName() {
		return profileName;
	}

	public static @Nullable SkyblockProfiles.SkyblockProfile getSelectedProfile() {
		return profile.getProfile(profileName);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		currentTime = System.currentTimeMillis();
		if (startTime == 0) startTime = currentTime;

		ProfileViewerPage page = currentPage;
		if (profile == null) {
			page = ProfileViewerPage.INVALID_NAME;
		} else if (profile.getOrLoadSkyblockProfiles(null) == null) {
			page = ProfileViewerPage.LOADING;
		}

		if (profile != null && profile.getLatestProfileName() == null &&
			!profile.getUpdatingSkyblockProfilesState().get()) {
			page = ProfileViewerPage.NO_SKYBLOCK;
		}

		if (profile != null) {
			if (profileName == null && profile.getLatestProfileName() != null) {
				profileName = profile.getLatestProfileName();
			}

		}

		this.sizeX = 431;
		this.sizeY = 202;
		guiLeft = (this.width - this.sizeX) / 2;
		guiTop = (this.height - this.sizeY) / 2;

		SkyblockProfiles.SkyblockProfile selectedProfile = profile != null ? profile.getProfile(profileName) : null;
		if (NotEnoughUpdates.INSTANCE.config.profileViewer.alwaysShowBingoTab) {
			showBingoPage = true;
		} else {
			showBingoPage = selectedProfile != null && selectedProfile.getGamemode() != null && selectedProfile.getGamemode().equals("bingo");
		}

		if (!showBingoPage && currentPage == ProfileViewerPage.BINGO) {
			currentPage = ProfileViewerPage.BASIC;
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
		drawDefaultBackground();

		blurBackground();
		renderBlurredBackground(width, height, guiLeft + 2, guiTop + 2, sizeX - 4, sizeY - 4);

		GlStateManager.enableDepth();
		GlStateManager.translate(0, 0, 5);
		renderTabs(true);
		renderRecentPlayers(true);
		GlStateManager.translate(0, 0, -3);

		GlStateManager.disableDepth();
		GlStateManager.translate(0, 0, -2);
		renderTabs(false);
		renderRecentPlayers(false);
		GlStateManager.translate(0, 0, 2);

		GlStateManager.disableLighting();
		GlStateManager.enableDepth();
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_bg);
		Utils.drawTexturedRect(guiLeft, guiTop, sizeX, sizeY, GL11.GL_NEAREST);

		if (page != ProfileViewerPage.LOADING) {
			playerNameTextField.render(guiLeft + sizeX - 100, guiTop + sizeY + 5);
			ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

			if (profile != null) {
				//Render Profile chooser button
				renderBlurredBackground(width, height, guiLeft + 2, guiTop + sizeY + 3 + 2, 100 - 4, 20 - 4);
				Minecraft.getMinecraft().getTextureManager().bindTexture(pv_dropdown);
				Utils.drawTexturedRect(guiLeft, guiTop + sizeY + 3, 100, 20, 0, 100 / 200f, 0, 20 / 185f, GL11.GL_NEAREST);
				Utils.drawStringCenteredScaledMaxWidth(
					profileName,
					guiLeft + 50,
					guiTop + sizeY + 3 + 10,
					true,
					90,
					new Color(63, 224, 208, 255).getRGB()
				);

				if (selectedProfile != null && selectedProfile.getGamemode() != null) {
					GlStateManager.color(1, 1, 1, 1);
					ResourceLocation gamemodeIcon = gamemodeToIcon.getOrDefault(selectedProfile.getGamemode(), gamemodeIconUnknown);
					Minecraft.getMinecraft().getTextureManager().bindTexture(gamemodeIcon);
					Utils.drawTexturedRect(guiLeft - 16 - 5, guiTop + sizeY + 5, 16, 16, GL11.GL_NEAREST);
				}

				// Render Open In SkyCrypt button
				renderBlurredBackground(width, height, guiLeft + 100 + 6 + 2, guiTop + sizeY + 3 + 2, 100 - 4, 20 - 4);
				Minecraft.getMinecraft().getTextureManager().bindTexture(pv_dropdown);
				Utils.drawTexturedRect(
					guiLeft + 100 + 6,
					guiTop + sizeY + 3,
					100,
					20,
					0,
					100 / 200f,
					0,
					20 / 185f,
					GL11.GL_NEAREST
				);
				Utils.drawStringCenteredScaledMaxWidth(
					"Open in SkyCrypt",
					guiLeft + 50 + 100 + 6,
					guiTop + sizeY + 3 + 10,
					true,
					90,
					new Color(63, 224, 208, 255).getRGB()
				);

				if (profileDropdownSelected && !profile.getProfileNames().isEmpty() && scaledResolution.getScaleFactor() < 4) {
					int dropdownOptionSize = scaledResolution.getScaleFactor() == 3 ? 10 : 20;
					int sizeYDropdown = profile.getProfileNames().size() * dropdownOptionSize;
					renderBlurredBackground(width, height, guiLeft + 2, guiTop + sizeY + 23, 100 - 4, sizeYDropdown - 2);
					Minecraft.getMinecraft().getTextureManager().bindTexture(pv_dropdown);
					Utils.drawTexturedRect(guiLeft, guiTop + sizeY + 23 - 3, 100, 3, 100 / 200f, 1, 0, 3 / 185f, GL11.GL_NEAREST);
					Utils.drawTexturedRect(
						guiLeft,
						guiTop + sizeY + 23 + sizeYDropdown - 4,
						100,
						4,
						100 / 200f,
						1,
						181 / 185f,
						1,
						GL11.GL_NEAREST
					);
					Utils.drawTexturedRect(
						guiLeft,
						guiTop + sizeY + 23,
						100,
						sizeYDropdown - 4,
						100 / 200f,
						1,
						(181 - sizeYDropdown) / 185f,
						181 / 185f,
						GL11.GL_NEAREST
					);

					for (int yIndex = 0; yIndex < profile.getProfileNames().size(); yIndex++) {
						String otherProfileName = profile.getProfileNames().get(yIndex);
						selectedProfile = profile.getProfile(otherProfileName);

						Utils.drawStringCenteredScaledMaxWidth(
							otherProfileName,
							guiLeft + 50,
							guiTop + sizeY + 23 + dropdownOptionSize / 2f + dropdownOptionSize * yIndex,
							true,
							90,
							new Color(33, 112, 104, 255).getRGB()
						);

						if (selectedProfile != null && selectedProfile.getGamemode() != null) {
							GlStateManager.color(1, 1, 1, 1);
							ResourceLocation gamemodeIcon = gamemodeToIcon.getOrDefault(selectedProfile.getGamemode(), gamemodeIconUnknown);
							Minecraft.getMinecraft().getTextureManager().bindTexture(gamemodeIcon);
							Utils.drawTexturedRect(
								guiLeft - 16 - 5,
								guiTop + sizeY + 2 + 23 + dropdownOptionSize * yIndex,
								16,
								16,
								GL11.GL_NEAREST
							);
						}
					}
				}
			}
		}

		GlStateManager.color(1, 1, 1, 1);

		if (pages.containsKey(page)) {
			pages.get(page).drawPage(mouseX, mouseY, partialTicks);
		} else {
			switch (page) {
				case LOADING:
					String str = EnumChatFormatting.YELLOW + "Loading player profiles.";
					long currentTimeMod = System.currentTimeMillis() % 1000;
					if (currentTimeMod > 333) {
						if (currentTimeMod < 666) {
							str += ".";
						} else {
							str += "..";
						}
					}

					Utils.drawStringCentered(str, guiLeft + sizeX / 2f, guiTop + 101, true, 0);

					//This is just here to inform the player what to do
					//like telling them to go find a psychotherapist
					long timeDiff = System.currentTimeMillis() - startTime;

					if (timeDiff > 20000) {
						Utils.drawStringCentered(
							EnumChatFormatting.YELLOW + "Its taking a while...",
							guiLeft + sizeX / 2f, guiTop + 111, true, 0
						);
						Utils.drawStringCentered(
							EnumChatFormatting.YELLOW + "Might be hypixel's fault.",
							guiLeft + sizeX / 2f, guiTop + 121, true, 0
						);
						if (timeDiff > 60000) {
							Utils.drawStringCentered(
								EnumChatFormatting.YELLOW + "Might be our fault :/.",
								guiLeft + sizeX / 2f, guiTop + 131, true, 0
							);
							if (timeDiff > 180000) {
								Utils.drawStringCentered(
									EnumChatFormatting.YELLOW + "Wow you're still here?",
									guiLeft + sizeX / 2f, guiTop + 141, true, 0
								);
								if (timeDiff > 360000) {
									long second = (timeDiff / 1000) % 60;
									long minute = (timeDiff / (1000 * 60)) % 60;
									long hour = (timeDiff / (1000 * 60 * 60)) % 24;

									String time = String.format("%02d:%02d:%02d", hour, minute, second);
									Utils.drawStringCentered(
										EnumChatFormatting.YELLOW + "You've wasted your time here for: " + time,
										guiLeft + sizeX / 2f, guiTop + 151, true, 0
									);
									Utils.drawStringCentered(
										EnumChatFormatting.YELLOW + String.valueOf(EnumChatFormatting.BOLD) + "What are you doing with your life?",
										guiLeft + sizeX / 2f, guiTop + 161, true, 0
									);
									if (timeDiff > 600000) {
										Utils.drawStringCentered(
											EnumChatFormatting.RED + String.valueOf(EnumChatFormatting.BOLD) + "Maniac",
											guiLeft + sizeX / 2f, guiTop + 171, true, 0
										);
										if (timeDiff > 1200000) {
											Utils.drawStringCentered(
												EnumChatFormatting.RED + String.valueOf(EnumChatFormatting.BOLD) + "You're a menace to society",
												guiLeft + sizeX / 2f, guiTop + 181, true, 0
											);
											if (timeDiff > 1800000) {
												Utils.drawStringCentered(
													EnumChatFormatting.RED + String.valueOf(EnumChatFormatting.BOLD) +
														"You don't know what's gonna happen to you",
													guiLeft + sizeX / 2f, guiTop + 191, true, 0
												);
												if (timeDiff > 3000000) {
													Utils.drawStringCentered(
														EnumChatFormatting.RED + String.valueOf(EnumChatFormatting.BOLD) + "You really want this?",
														guiLeft + sizeX / 2f, guiTop + 91, true, 0
													);
													if (timeDiff > 3300000) {
														Utils.drawStringCentered(
															EnumChatFormatting.DARK_RED +
																String.valueOf(EnumChatFormatting.BOLD) +
																"OW LORD FORGIVE ME FOR THIS",
															guiLeft + sizeX / 2f, guiTop + 71, true, 0
														);
														if (timeDiff > 3600000) {
															throw new Error("Go do something productive") {
																@Override
																public void printStackTrace() {
																	throw new Error("Go do something productive");
																}
															};
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}

					break;
				case INVALID_NAME:
					Utils.drawStringCentered(
						EnumChatFormatting.RED + "Invalid name or API is down!",
						guiLeft + sizeX / 2f, guiTop + 101, true, 0
					);
					break;
				case NO_SKYBLOCK:
					Utils.drawStringCentered(
						EnumChatFormatting.RED + "No SkyBlock data found!",
						guiLeft + sizeX / 2f, guiTop + 101, true, 0
					);
					break;
			}
		}

		lastTime = currentTime;

		if (currentPage != ProfileViewerPage.LOADING && currentPage != ProfileViewerPage.INVALID_NAME) {
			int ignoredTabs = 0;
			List<Integer> configList = NotEnoughUpdates.INSTANCE.config.profileViewer.pageLayout;
			for (int i = 0; i < configList.size(); i++) {
				ProfileViewerPage iPage = ProfileViewerPage.getById(configList.get(i));
				if (iPage == null) continue;
				if (iPage.stack == null || (iPage == ProfileViewerPage.BINGO && !showBingoPage)) {
					ignoredTabs++;
					continue;
				}
				int i2 = i - ignoredTabs;
				int x = guiLeft + i2 * 28;
				int y = guiTop - 28;

				if (mouseX > x && mouseX < x + 28) {
					if (mouseY > y && mouseY < y + 32) {
						if (!iPage.stack
							.getTooltip(Minecraft.getMinecraft().thePlayer, false)
							.isEmpty()) {
							tooltipToDisplay = Collections.singletonList(iPage.stack
								.getTooltip(Minecraft.getMinecraft().thePlayer, false)
								.get(0));
						}
					}
				}
			}
		}
		int x = guiLeft + sizeX;
		int y = guiTop;
		List<String> previousProfileSearches = NotEnoughUpdates.INSTANCE.config.hidden.previousProfileSearches;

		if (mouseX > x && mouseX < x + 29) {
			if (mouseY > y && mouseY < y + 28) {
				tooltipToDisplay = new ArrayList<>();
				tooltipToDisplay.add(Minecraft.getMinecraft().thePlayer.getName());
			}
		}

		for (int i = 0; i < previousProfileSearches.size(); i++) {
			if (mouseX > x && mouseX < x + 28) {
				if (mouseY > y + 28 * (i + 1) && mouseY < y + 28 * (i + 2)) {
					tooltipToDisplay = new ArrayList<>();
					tooltipToDisplay.add(previousProfileSearches.get(i));
				}
			}
		}

		if (tooltipToDisplay != null) {
			List<String> grayTooltip = new ArrayList<>(tooltipToDisplay.size());
			for (String line : tooltipToDisplay) {
				grayTooltip.add(EnumChatFormatting.GRAY + line);
			}
			Utils.drawHoveringText(grayTooltip, mouseX, mouseY, width, height, -1);
			tooltipToDisplay = null;
		}
	}

	private void renderTabs(boolean renderPressed) {
		int ignoredTabs = 0;
		List<Integer> configList = NotEnoughUpdates.INSTANCE.config.profileViewer.pageLayout;
		for (int i = 0; i < configList.size(); i++) {
			ProfileViewerPage page = ProfileViewerPage.getById(configList.get(i));
			if (page == null) continue;
			if (page.stack == null || (page == ProfileViewerPage.BINGO && !showBingoPage)) {
				ignoredTabs++;
				continue;
			}
			boolean pressed = page == currentPage;
			if (pressed == renderPressed) {
				renderTab(page.stack, i - ignoredTabs, pressed);
			}
		}
	}

	private void renderTab(ItemStack stack, int xIndex, boolean pressed) {
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);

		int x = guiLeft + xIndex * 28;
		int y = guiTop - 28;

		float uMin = 0;
		float uMax = 28 / 256f;
		float vMin = 20 / 256f;
		float vMax = 51 / 256f;
		if (pressed) {
			vMin = 52 / 256f;
			vMax = 84 / 256f;

			if (xIndex != 0) {
				uMin = 28 / 256f;
				uMax = 56 / 256f;
			}

			renderBlurredBackground(width, height, x + 2, y + 2, 28 - 4, 28 - 4);
		} else {
			renderBlurredBackground(width, height, x + 2, y + 4, 28 - 4, 28 - 4);
		}

		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_elements);
		Utils.drawTexturedRect(x, y, 28, pressed ? 32 : 31, uMin, uMax, vMin, vMax, GL11.GL_NEAREST);

		GlStateManager.enableDepth();
		Utils.drawItemStack(stack, x + 6, y + 9);
	}

	private void renderRecentPlayers(boolean renderCurrent) {
		String playerName = "";
		if (profile.getHypixelProfile() != null) {
			playerName = profile.getHypixelProfile().get("displayname").getAsString();
		}

		boolean selected = Objects.equals(Minecraft.getMinecraft().thePlayer.getName(), playerName);
		if (selected == renderCurrent) {
			renderRecentPlayer(Minecraft.getMinecraft().thePlayer.getName().toLowerCase(), 0, selected);
		}

		List<String> previousProfileSearches = NotEnoughUpdates.INSTANCE.config.hidden.previousProfileSearches;

		for (int i = 0; i < previousProfileSearches.size(); i++) {
			selected = Objects.equals(previousProfileSearches.get(i), playerName.toLowerCase());
			if (selected == renderCurrent) {
				renderRecentPlayer(previousProfileSearches.get(i), i + 1, selected);
			}
		}
	}

	private void renderRecentPlayer(String name, int yIndex, boolean selected) {
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

		int x = guiLeft + sizeX;
		int y = guiTop + yIndex * 28;

		float uMin = 226 / 256f;
		float uMax = 1f;
		float vMin = 144 / 256f;
		float vMax = 172 / 256f;

		if (yIndex != 0) {
			vMin = 172 / 256f;
			vMax = 200 / 256f;
		}

		if (selected) {
			uMin = 196 /256f;
			uMax = 226 / 256f;

			renderBlurredBackground(width, height, x - 2, y + 2, 30 - 2, 28 - 4);
		} else {
			renderBlurredBackground(width, height, x, y + 2, 28 - 2, 28 - 4);
		}

		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_elements);
		Utils.drawTexturedRect(x - 3, y, 32, 28, uMin, uMax, vMin, vMax, GL11.GL_NEAREST);

		GlStateManager.enableDepth();

		ItemStack playerHead = ProfileViewerUtils.getPlayerData(name);

		Utils.drawItemStack(playerHead, x + 3, y + 6);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (currentPage != ProfileViewerPage.LOADING && currentPage != ProfileViewerPage.INVALID_NAME) {
			int ignoredTabs = 0;
			List<Integer> configList = NotEnoughUpdates.INSTANCE.config.profileViewer.pageLayout;
			for (int i = 0; i < configList.size(); i++) {
				ProfileViewerPage page = ProfileViewerPage.getById(configList.get(i));
				if (page == null) continue;
				if (page.stack == null || (page == ProfileViewerPage.BINGO && !showBingoPage)) {
					ignoredTabs++;
					continue;
				}
				int i2 = i - ignoredTabs;
				int x = guiLeft + i2 * 28;
				int y = guiTop - 28;

				if (mouseX > x && mouseX < x + 28) {
					if (mouseY > y && mouseY < y + 32) {
						if (currentPage != page) Utils.playPressSound();
						currentPage = page;
						inventoryTextField.otherComponentClick();
						playerNameTextField.otherComponentClick();
						killDeathSearchTextField.otherComponentClick();
						return;
					}
				}
			}
		}

		if (pages.containsKey(currentPage)) {
			if (pages.get(currentPage).mouseClicked(mouseX, mouseY, mouseButton)) {
				return;
			}
		}

		String playerName = "";
		if (profile.getHypixelProfile() != null) {
			playerName = profile.getHypixelProfile().get("displayname").getAsString().toLowerCase();
		}
		int x = guiLeft + sizeX;
		int y = guiTop;
		List<String> previousProfileSearches = NotEnoughUpdates.INSTANCE.config.hidden.previousProfileSearches;

		if (mouseX > x && mouseX < x + 29) {
			if (mouseY > y && mouseY < y + 28) {
				if (!playerName.equals(Minecraft.getMinecraft().thePlayer.getName().toLowerCase())) {
					NotEnoughUpdates.profileViewer.loadPlayerByName(Minecraft.getMinecraft().thePlayer.getName(), profile -> {
						profile.resetCache();
						NotEnoughUpdates.INSTANCE.openGui = new GuiProfileViewer(profile);
					});
				}
			}
		}

		for (int i = 0; i < previousProfileSearches.size(); i++) {
			if (mouseX > x && mouseX < x + 28) {
				if (mouseY > y + 28 * (i + 1) && mouseY < y + 28 * (i + 2)) {
					if (!playerName.equals(previousProfileSearches.get(i))) {
						NotEnoughUpdates.profileViewer.loadPlayerByName(previousProfileSearches.get(i), profile -> {
							profile.resetCache();
							NotEnoughUpdates.INSTANCE.openGui = new GuiProfileViewer(profile);
						});
					}
				}
			}
		}

		if (mouseX > guiLeft + sizeX - 100 && mouseX < guiLeft + sizeX) {
			if (mouseY > guiTop + sizeY + 5 && mouseY < guiTop + sizeY + 25) {
				playerNameTextField.mouseClicked(mouseX, mouseY, mouseButton);
				inventoryTextField.otherComponentClick();
				killDeathSearchTextField.otherComponentClick();
				return;
			}
		}
		if (
			mouseX > guiLeft + 106 &&
				mouseX < guiLeft + 106 + 100 &&
				profile != null &&
				!profile.getProfileNames().isEmpty() &&
				profileName != null
		) {
			if (mouseY > guiTop + sizeY + 3 && mouseY < guiTop + sizeY + 23) {
				String url =
					"https://sky.shiiyu.moe/stats/" + profile.getHypixelProfile().get("displayname").getAsString() + "/" +
						profileName;
				Utils.openUrl(url);
				Utils.playPressSound();
				return;
			}
		}

		if (mouseX > guiLeft && mouseX < guiLeft + 100 && profile != null && !profile.getProfileNames().isEmpty()) {
			ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
			if (mouseY > guiTop + sizeY + 3 && mouseY < guiTop + sizeY + 23) {
				if (scaledResolution.getScaleFactor() >= 4) {
					profileDropdownSelected = false;
					int profileNum = 0;
					for (int index = 0; index < profile.getProfileNames().size(); index++) {
						if (profile.getProfileNames().get(index).equals(profileName)) {
							profileNum = index;
							break;
						}
					}
					if (mouseButton == 0) {
						profileNum++;
					} else {
						profileNum--;
					}
					if (profileNum >= profile.getProfileNames().size()) profileNum = 0;
					if (profileNum < 0) profileNum = profile.getProfileNames().size() - 1;

					String newProfileName = profile.getProfileNames().get(profileNum);
					if (profileName != null && !profileName.equals(newProfileName)) {
						resetCache();
					}
					profileName = newProfileName;
				} else {
					profileDropdownSelected = !profileDropdownSelected;
				}
			} else if (scaledResolution.getScaleFactor() < 4 && profileDropdownSelected) {
				int dropdownOptionSize = scaledResolution.getScaleFactor() == 3 ? 10 : 20;
				int extraY = mouseY - (guiTop + sizeY + 23);
				int index = extraY / dropdownOptionSize;
				if (index >= 0 && index < profile.getProfileNames().size()) {
					String newProfileName = profile.getProfileNames().get(index);
					if (profileName != null && !profileName.equals(newProfileName)) {
						resetCache();
					}
					profileName = newProfileName;
				}
			}
			playerNameTextField.otherComponentClick();
			inventoryTextField.otherComponentClick();
			killDeathSearchTextField.otherComponentClick();
			return;
		}
		profileDropdownSelected = false;
		playerNameTextField.otherComponentClick();
		inventoryTextField.otherComponentClick();
		killDeathSearchTextField.otherComponentClick();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);

		if (pages.containsKey(currentPage)) {
			pages.get(currentPage).keyTyped(typedChar, keyCode);
		}

		if (playerNameTextField.getFocus()) {
			if (keyCode == Keyboard.KEY_RETURN) {
				currentPage = ProfileViewerPage.LOADING;
				NotEnoughUpdates.profileViewer.loadPlayerByName(
					playerNameTextField.getText(),
					profile -> { //todo: invalid name
						if (profile != null) {
							profile.resetCache();
							ProfileViewerUtils.saveSearch(playerNameTextField.getText());
						}
						Minecraft.getMinecraft().displayGuiScreen(new GuiProfileViewer(profile));
					}
				);
			}
			playerNameTextField.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		super.mouseReleased(mouseX, mouseY, mouseButton);

		if (pages.containsKey(currentPage)) {
			pages.get(currentPage).mouseReleased(mouseX, mouseY, mouseButton);
		}
	}

	public void renderXpBar(
		String skillName,
		ItemStack stack,
		int x,
		int y,
		int xSize,
		ProfileViewer.Level levelObj,
		int mouseX,
		int mouseY
	) {
		float level = levelObj.level;
		int levelFloored = (int) Math.floor(level);

		Utils.renderAlignedString(skillName, EnumChatFormatting.WHITE.toString() + levelFloored, x + 14, y - 4, xSize - 20);

		if (levelObj.maxed) {
			renderGoldBar(x, y + 6, xSize);
		} else {
			if ((skillName.contains("Catacombs") || Weight.DUNGEON_CLASS_NAMES.stream().anyMatch(e -> skillName.toLowerCase().contains(e))) && levelObj.level >= 50) {
				renderGoldBar(x, y + 6, xSize);
			} else {
				renderBar(x, y + 6, xSize, level % 1);
			}
		}

		if (mouseX > x && mouseX < x + 120) {
			if (mouseY > y - 4 && mouseY < y + 13) {
				String levelStr;
				String totalXpStr = null;
				if (skillName.contains("Catacombs")) {
					totalXpStr = EnumChatFormatting.GRAY + "Total XP: " + EnumChatFormatting.DARK_PURPLE +
						StringUtils.formatNumber(levelObj.totalXp) + EnumChatFormatting.DARK_GRAY + " (" +
						StringUtils.formatToTenths(getPercentage(skillName.toLowerCase(), levelObj)) + "% to 50)";
				}
				// Adds overflow level to each level object that is maxed, avoids hotm level as there is no overflow xp for it
				if (levelObj.maxed) {
					levelStr = levelObj.maxLevel != 7 ?
						EnumChatFormatting.GOLD + "MAXED!" + EnumChatFormatting.GRAY + " (Overflow level: " + String.format(
							"%.2f",
							levelObj.level
						) + ")" :
						EnumChatFormatting.GOLD + "MAXED!";
				} else {
					if (skillName.contains("Class Average")) {
						levelStr = "Progress: " + EnumChatFormatting.DARK_PURPLE + String.format("%.1f", (level % 1 * 100)) + "%";
						totalXpStr = "Exact Class Average: " + EnumChatFormatting.WHITE + String.format("%.2f", levelObj.level);
					} else {
						int maxXp = (int) levelObj.maxXpForLevel;
						levelStr =
							EnumChatFormatting.DARK_PURPLE +
								StringUtils.shortNumberFormat(Math.round((level % 1) * maxXp)) +
								"/" +
								StringUtils.shortNumberFormat(maxXp) +
								// Since catacombs isn't considered 'maxed' at level 50 (since the cap is '99'), we can add
								// a conditional here to add the overflow level rather than above
								(skillName.contains("Catacombs") && levelObj.level >= 50 ?
									EnumChatFormatting.GRAY + " (Overflow level: " + String.format("%.2f", levelObj.level) + ")" : "");
					}
				}
				if (totalXpStr != null) {
					tooltipToDisplay = Utils.createList(levelStr, totalXpStr);
				} else {
					tooltipToDisplay = Utils.createList(levelStr);
				}
			}
		}

		NBTTagCompound nbt = new NBTTagCompound(); //Adding NBT Data for Custom Resource Packs
		NBTTagCompound display = new NBTTagCompound();
		display.setString("Name", skillName);
		nbt.setTag("display", display);
		stack.setTagCompound(nbt);

		GL11.glTranslatef((x), (y - 6f), 0);
		GL11.glScalef(0.7f, 0.7f, 1);
		Utils.drawItemStackLinear(stack, 0, 0);
		GL11.glScalef(1 / 0.7f, 1 / 0.7f, 1);
		GL11.glTranslatef(-(x), -(y - 6f), 0);
	}

	public EntityOtherPlayerMP getEntityPlayer() {
		return ((BasicPage) pages.get(ProfileViewerPage.BASIC)).entityPlayer;
	}

	public void renderGoldBar(float x, float y, float xSize) {
		if (!OpenGlHelper.areShadersSupported()) {
			renderBar(x, y, xSize, 1);
			return;
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(icons);
		ShaderManager shaderManager = ShaderManager.getInstance();
		shaderManager.loadShader("make_gold");
		shaderManager.loadData("make_gold", "amount", (startTime - System.currentTimeMillis()) / 10000f);

		Utils.drawTexturedRect(x, y, xSize / 2f, 5, 0 / 256f, (xSize / 2f) / 256f, 79 / 256f, 84 / 256f, GL11.GL_NEAREST);
		Utils.drawTexturedRect(
			x + xSize / 2f,
			y,
			xSize / 2f,
			5,
			(182 - xSize / 2f) / 256f,
			182 / 256f,
			79 / 256f,
			84 / 256f,
			GL11.GL_NEAREST
		);

		GL20.glUseProgram(0);
	}

	public void renderBar(float x, float y, float xSize, float completed) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(icons);

		completed = Math.round(completed / 0.05f) * 0.05f;
		float notCompleted = 1 - completed;
		GlStateManager.color(1, 1, 1, 1);
		float width;

		if (completed < 0.5f) {
			width = (0.5f - completed) * xSize;
			Utils.drawTexturedRect(
				x + xSize * completed,
				y,
				width,
				5,
				xSize * completed / 256f,
				(xSize / 2f) / 256f,
				74 / 256f,
				79 / 256f,
				GL11.GL_NEAREST
			);
		}
		if (completed < 1f) {
			width = Math.min(xSize * notCompleted, xSize / 2f);
			Utils.drawTexturedRect(
				x + (xSize / 2f) + Math.max(xSize * (completed - 0.5f), 0),
				y,
				width,
				5,
				(182 - (xSize / 2f) + Math.max(xSize * (completed - 0.5f), 0)) / 256f,
				182 / 256f,
				74 / 256f,
				79 / 256f,
				GL11.GL_NEAREST
			);
		}

		if (completed > 0f) {
			width = Math.min(xSize * completed, xSize / 2f);
			Utils.drawTexturedRect(x, y, width, 5, 0 / 256f, width / 256f, 79 / 256f, 84 / 256f, GL11.GL_NEAREST);
		}
		if (completed > 0.5f) {
			width = Math.min(xSize * (completed - 0.5f), xSize / 2f);
			Utils.drawTexturedRect(
				x + (xSize / 2f),
				y,
				width,
				5,
				(182 - (xSize / 2f)) / 256f,
				(182 - (xSize / 2f) + width) / 256f,
				79 / 256f,
				84 / 256f,
				GL11.GL_NEAREST
			);
		}
	}

	public void resetCache() {
		pages.values().forEach(GuiProfileViewerPage::resetCache);
	}

	/**
	 * Creates a projection matrix that projects from our coordinate space [0->width; 0->height] to OpenGL coordinate
	 * space [-1 -> 1; 1 -> -1] (Note: flipped y-axis).
	 * <p>
	 * This is so that we can render to and from the framebuffer in a way that is familiar to us, instead of needing to
	 * apply scales and translations manually.
	 */
	private Matrix4f createProjectionMatrix(int width, int height) {
		Matrix4f projMatrix = new Matrix4f();
		projMatrix.setIdentity();
		projMatrix.m00 = 2.0F / (float) width;
		projMatrix.m11 = 2.0F / (float) (-height);
		projMatrix.m22 = -0.0020001999F;
		projMatrix.m33 = 1.0F;
		projMatrix.m03 = -1.0F;
		projMatrix.m13 = 1.0F;
		projMatrix.m23 = -1.0001999F;
		return projMatrix;
	}

	private void blurBackground() {
		if (!OpenGlHelper.isFramebufferEnabled()) return;

		int width = Minecraft.getMinecraft().displayWidth;
		int height = Minecraft.getMinecraft().displayHeight;

		if (blurOutputHorz == null) {
			blurOutputHorz = new Framebuffer(width, height, false);
			blurOutputHorz.setFramebufferFilter(GL11.GL_NEAREST);
		}
		if (blurOutputVert == null) {
			blurOutputVert = new Framebuffer(width, height, false);
			blurOutputVert.setFramebufferFilter(GL11.GL_NEAREST);
		}
		if (blurOutputHorz.framebufferWidth != width || blurOutputHorz.framebufferHeight != height) {
			blurOutputHorz.createBindFramebuffer(width, height);
			blurShaderHorz.setProjectionMatrix(createProjectionMatrix(width, height));
			Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
		}
		if (blurOutputVert.framebufferWidth != width || blurOutputVert.framebufferHeight != height) {
			blurOutputVert.createBindFramebuffer(width, height);
			blurShaderVert.setProjectionMatrix(createProjectionMatrix(width, height));
			Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
		}

		if (blurShaderHorz == null) {
			try {
				blurShaderHorz =
					new Shader(
						Minecraft.getMinecraft().getResourceManager(),
						"blur",
						Minecraft.getMinecraft().getFramebuffer(),
						blurOutputHorz
					);
				blurShaderHorz.getShaderManager().getShaderUniform("BlurDir").set(1, 0);
				blurShaderHorz.setProjectionMatrix(createProjectionMatrix(width, height));
			} catch (Exception ignored) {
			}
		}
		if (blurShaderVert == null) {
			try {
				blurShaderVert = new Shader(
					Minecraft.getMinecraft().getResourceManager(),
					"blur",
					blurOutputHorz,
					blurOutputVert
				);
				blurShaderVert.getShaderManager().getShaderUniform("BlurDir").set(0, 1);
				blurShaderVert.setProjectionMatrix(createProjectionMatrix(width, height));
			} catch (Exception ignored) {
			}
		}
		if (blurShaderHorz != null && blurShaderVert != null) {
			if (15 != lastBgBlurFactor) {
				blurShaderHorz.getShaderManager().getShaderUniform("Radius").set((float) 15);
				blurShaderVert.getShaderManager().getShaderUniform("Radius").set((float) 15);
				lastBgBlurFactor = 15;
			}
			GL11.glPushMatrix();
			blurShaderHorz.loadShader(0);
			blurShaderVert.loadShader(0);
			GlStateManager.enableDepth();
			GL11.glPopMatrix();

			Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
		}
	}

	public float getPercentage(String skillName, ProfileViewer.Level level) {
		if (level.maxed) {
			return 100;
		}
		if (skillName.contains("catacombs")) {
			return (level.totalXp / DungeonsWeight.CATACOMBS_LEVEL_50_XP) * 100;
		} else if (Weight.SLAYER_NAMES.contains(skillName)) {
			return (level.totalXp / 1000000) * 100;
		} else if (skillName.equalsIgnoreCase("social")) {
			return (level.totalXp / 272800) * 100;
		} else {
			if (level.maxLevel == 60) {
				return (level.totalXp / SkillsWeight.SKILLS_LEVEL_60) * 100;
			} else {
				return (level.totalXp / SkillsWeight.SKILLS_LEVEL_50) * 100;
			}
		}
	}

	/**
	 * Renders a subsection of the blurred framebuffer on to the corresponding section of the screen.
	 * Essentially, this method will "blur" the background inside the bounds specified by [x->x+blurWidth, y->y+blurHeight]
	 */
	public void renderBlurredBackground(int width, int height, int x, int y, int blurWidth, int blurHeight) {
		if (!OpenGlHelper.isFramebufferEnabled()) return;

		float uMin = x / (float) width;
		float uMax = (x + blurWidth) / (float) width;
		float vMin = (height - y) / (float) height;
		float vMax = (height - y - blurHeight) / (float) height;

		blurOutputVert.bindFramebufferTexture();
		GlStateManager.color(1f, 1f, 1f, 1f);
		//Utils.setScreen(width*f, height*f, f);
		Utils.drawTexturedRect(x, y, blurWidth, blurHeight, uMin, uMax, vMin, vMax);
		//Utils.setScreen(width, height, f);
		blurOutputVert.unbindFramebufferTexture();
	}

	public enum ProfileViewerPage {
		LOADING(),
		INVALID_NAME(),
		NO_SKYBLOCK(),
		BASIC(0, Items.paper, "§9Skills"),
		DUNGEON(1, Item.getItemFromBlock(Blocks.deadbush), "§eDungeoneering"),
		EXTRA(2, Items.book, "§7Profile Stats"),
		INVENTORIES(3, Item.getItemFromBlock(Blocks.ender_chest), "§bStorage"),
		COLLECTIONS(4, Items.painting, "§6Collections"),
		PETS(5, Items.bone, "§aPets"),
		MINING(6, Items.iron_pickaxe, "§5Heart of the Mountain"),
		BINGO(7, Items.filled_map, "§zBingo"),
		TROPHY_FISH(8, Items.fishing_rod, "§3Trophy Fish"),
		BESTIARY(9, Items.iron_sword, "§cBestiary"),
		CRIMSON_ISLE(10, Item.getItemFromBlock(Blocks.netherrack), "§4Crimson Isle"),
		MUSEUM(11, Items.leather_chestplate, "§6Museum"),
		RIFT(12, Items.ender_eye, "§5Rift");

		public final ItemStack stack;
		public final int id;

		ProfileViewerPage() {
			this(-1, null, null);
		}

		ProfileViewerPage(int id, Item item, String name) {
			this.id = id;
			if (item == null) {
				stack = null;
			} else {
				stack = new ItemStack(item);
				NBTTagCompound nbt = new NBTTagCompound(); //Adding NBT Data for Custom Resource Packs
				NBTTagCompound display = new NBTTagCompound();
				display.setString("Name", name);
				nbt.setTag("display", display);
				stack.setTagCompound(nbt);
			}
		}

		public static ProfileViewerPage getById(int id) {
			for (ProfileViewerPage page : values()) {
				if (page.id == id) {
					return page;
				}
			}
			return null;
		}

		public Optional<ItemStack> getItem() {
			return Optional.ofNullable(stack);
		}
	}
}
