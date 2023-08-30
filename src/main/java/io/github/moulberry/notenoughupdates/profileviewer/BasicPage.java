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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.profileviewer.level.LevelPage;
import io.github.moulberry.notenoughupdates.profileviewer.weight.lily.LilyWeight;
import io.github.moulberry.notenoughupdates.profileviewer.weight.senither.SenitherWeight;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.Weight;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.PronounDB;
import io.github.moulberry.notenoughupdates.util.Rectangle;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static io.github.moulberry.notenoughupdates.util.Utils.roundToNearestInt;

public class BasicPage extends GuiProfileViewerPage {

	private static final ResourceLocation pv_basic = new ResourceLocation("notenoughupdates:pv_basic.png");

	public static final ItemStack skull = Utils.createSkull(
		"egirlefe",
		"152de44a-43a3-46e1-badc-66cca2793471",
		"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODdkODg1YjMyYjBkZDJkNmI3ZjFiNTgyYTM0MTg2ZjhhNTM3M2M0NjU4OWEyNzM0MjMxMzJiNDQ4YjgwMzQ2MiJ9fX0="
	);

	private static final LinkedHashMap<String, ItemStack> dungeonsModeIcons = new LinkedHashMap<String, ItemStack>() {
		{
			put(
				"first_page",
				Utils.editItemStackInfo(
					new ItemStack(Items.paper),
					EnumChatFormatting.GRAY + "Front Page",
					true
				)
			);
			put(
				"second_page",
				Utils.editItemStackInfo(
					skull,
					EnumChatFormatting.GRAY + "Level Page",
					true
				)
			);
		}
	};

	private static final ExecutorService profileLoader = Executors.newFixedThreadPool(1);
	public EntityOtherPlayerMP entityPlayer = null;
	private ResourceLocation playerLocationSkin = null;
	private final GuiProfileViewer guiProfileViewer;

	private final String[] medalNames = {
		"§cBronze",
		"§fSilver",
		"§6Gold"
	};
	private ResourceLocation playerLocationCape = null;
	private String skinType = null;
	private boolean loadingProfile = false;

	private int backgroundClickedX = -1;

	private boolean onSecondPage;

	private final LevelPage levelPage;
	private boolean clickedLoadStatusButton = false;

	public BasicPage(GuiProfileViewer instance) {
		super(instance);
		this.guiProfileViewer = instance;
		this.levelPage = new LevelPage(guiProfileViewer, this);
	}

	@Override
	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
		SkyblockProfiles profile = GuiProfileViewer.getProfile();
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		if (onSecondPage) {
			levelPage.drawPage(mouseX, mouseY, partialTicks);
			return;
		}

		String location = null;
		JsonObject status = clickedLoadStatusButton ? profile.getPlayerStatus() : null;
		if (status != null && status.has("mode")) {
			location = status.get("mode").getAsString();
		}

		int extraRotation = 0;
		if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) {
			if (backgroundClickedX == -1) {
				if (mouseX > guiLeft + 23 && mouseX < guiLeft + 23 + 81) {
					if (mouseY > guiTop + 44 && mouseY < guiTop + 44 + 108) {
						backgroundClickedX = mouseX;
					}
				}
			}
		} else {
			if (backgroundClickedX != -1) {
				getInstance().backgroundRotation += mouseX - backgroundClickedX;
				backgroundClickedX = -1;
			}
		}
		if (backgroundClickedX == -1) {
			getInstance().backgroundRotation += (getInstance().currentTime - getInstance().lastTime) / 400f;
		} else {
			extraRotation = mouseX - backgroundClickedX;
		}
		getInstance().backgroundRotation %= 360;

		String panoramaIdentifier = "day";
		if (SBInfo.getInstance().currentTimeDate != null) {
			if (SBInfo.getInstance().currentTimeDate.getHours() <= 6 ||
				SBInfo.getInstance().currentTimeDate.getHours() >= 20) {
				panoramaIdentifier = "night";
			}
		}

		Panorama.drawPanorama(
			-getInstance().backgroundRotation - extraRotation,
			guiLeft + 23,
			guiTop + 44,
			81,
			108,
			0.37f,
			0.8f,
			Panorama.getPanoramasForLocation(location == null ? "unknown" : location, panoramaIdentifier)
		);

		if (Utils.isWithinRect(mouseX, mouseY, guiLeft + 23, guiTop + 44, 81, 108)) {
			Optional<PronounDB.PronounChoice> pronounChoice =
				GuiProfileViewer.pronouns
					.peekValue()
					.flatMap(it -> it); // Flatten: First optional is whether it loaded, second optional is whether it was successful
			if (pronounChoice.isPresent()) {
				PronounDB.PronounChoice pronouns = pronounChoice.get();
				if (pronouns.isConsciousChoice()) {
					getInstance().tooltipToDisplay = pronouns.render();
				}
			}
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_basic);
		Utils.drawTexturedRect(guiLeft, guiTop, getInstance().sizeX, getInstance().sizeY, GL11.GL_NEAREST);
		String profileName = GuiProfileViewer.getProfileName();
		SkyblockProfiles.SkyblockProfile selectedProfile = getSelectedProfile();
		if (selectedProfile == null) {
			return;
		}

		if (entityPlayer != null && profile.getHypixelProfile() != null) {
			String playerName = null;
			if (profile.getHypixelProfile().has("prefix")) {
				playerName = Utils.getElementAsString(profile.getHypixelProfile().get("prefix"), "") + " " +
					entityPlayer.getName();
			} else {
				String rank = Utils.getElementAsString(
					profile.getHypixelProfile().get("rank"),
					Utils.getElementAsString(profile.getHypixelProfile().get("newPackageRank"), "NONE")
				);
				String monthlyPackageRank = Utils.getElementAsString(
					profile.getHypixelProfile().get("monthlyPackageRank"),
					"NONE"
				);
				if (!rank.equals("YOUTUBER") && !monthlyPackageRank.equals("NONE")) {
					rank = monthlyPackageRank;
				}
				EnumChatFormatting rankPlusColorECF = EnumChatFormatting.getValueByName(
					Utils.getElementAsString(profile.getHypixelProfile().get("rankPlusColor"), "GOLD")
				);
				String rankPlusColor = EnumChatFormatting.GOLD.toString();
				if (rankPlusColorECF != null) {
					rankPlusColor = rankPlusColorECF.toString();
				}

				JsonObject misc = Constants.MISC;
				if (misc != null) {
					if (misc.has("ranks")) {
						String rankName = Utils.getElementAsString(Utils.getElement(misc, "ranks." + rank + ".tag"), null);
						String rankColor = Utils.getElementAsString(Utils.getElement(misc, "ranks." + rank + ".color"), "7");
						String rankPlus = Utils.getElementAsString(Utils.getElement(misc, "ranks." + rank + ".plus"), "");
						String rankTagColor = Utils.getElementAsString(
							Utils.getElement(misc, "ranks." + rank + ".tagColor"),
							rankColor
						);

						String name = entityPlayer.getName();

						if (misc.has("special_bois")) {
							JsonArray special_bois = misc.get("special_bois").getAsJsonArray();
							for (int i = 0; i < special_bois.size(); i++) {
								if (special_bois.get(i).getAsString().equals(profile.getUuid())) {
									name = Utils.chromaString(name);
									break;
								}
							}
						}

						playerName = EnumChatFormatting.GRAY + name;
						if (rankName != null) {
							String icon = selectedProfile.getGamemode() == null ? "" : getIcon(selectedProfile.getGamemode());
							playerName = MessageFormat.format(
								"§{0}[§{1}{2}{3}{4}§{5}] {6}",
								rankColor,
								rankTagColor,
								rankName,
								rankPlusColor,
								rankPlus,
								rankColor,
								name
							) + (icon.equals("") ? "" : " " + icon);
						}
					}
				}
			}
			if (playerName != null) {
				int rankPrefixLen = fr.getStringWidth(playerName);
				int halfRankPrefixLen = rankPrefixLen / 2;

				int x = guiLeft + 63;
				int y = guiTop + 54;

				GuiScreen.drawRect(
					x - halfRankPrefixLen - 1,
					y - 1,
					x + halfRankPrefixLen + 1,
					y + 8,
					new Color(0, 0, 0, 64).getRGB()
				);

				fr.drawString(playerName, x - halfRankPrefixLen, y, 0, true);
			}
		}

		String stateStr = EnumChatFormatting.RED + "An error occurred";
		long networth = -2;
		ArrayList<String> nwCategoryHover = new ArrayList<>();
		if (NotEnoughUpdates.INSTANCE.config.profileViewer.useSoopyNetworth) {
			SkyblockProfiles.SoopyNetworth nwData = selectedProfile.getSoopyNetworth(() -> {});
			networth = nwData.getNetworth();

			if (networth == -1) {
				stateStr = EnumChatFormatting.YELLOW + "Loading...";
			} else if (networth != -2) { // -2 indicates error
				for (Map.Entry<String, Long> entry : nwData.getCategoryToTotal().entrySet()) {
					nwCategoryHover.add(
						EnumChatFormatting.GREEN +
						WordUtils.capitalizeFully(entry.getKey().replace("_", " ")) +
						": " +
						EnumChatFormatting.GOLD +
						StringUtils.formatNumber(entry.getValue())
					);
				}
				nwCategoryHover.add("");
			}
		}

		// Calculate using NEU networth if not using soopy networth or soopy networth errored
		if (networth == -2) {
			networth = selectedProfile.getNetworth();
		}

		if (networth > 0) {
			int fontWidth = fr.getStringWidth("Net Worth: " + StringUtils.formatNumber(networth));
			int offset = (fontWidth >= 117 ? 63 + (fontWidth - 117) : 63);

			if (fontWidth >= 117) {
				fr.drawString(EnumChatFormatting.GREEN + "Net Worth: " + EnumChatFormatting.GOLD +
					StringUtils.formatNumber(networth), guiLeft + 8, guiTop + 38 - fr.FONT_HEIGHT / 2f, 0, true);
			} else {
				Utils.drawStringCentered(
					EnumChatFormatting.GREEN + "Net Worth: " + EnumChatFormatting.GOLD +
						StringUtils.formatNumber(networth),
					guiLeft + 68, guiTop + 38, true, 0
				);
			}
			if (NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo("BOOSTER_COOKIE") != null &&
				NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo("BOOSTER_COOKIE").has("avg_buy")) {
				double networthInCookies =
					(
						networth /
							NotEnoughUpdates.INSTANCE.manager.auctionManager
								.getBazaarInfo("BOOSTER_COOKIE")
								.get("avg_buy")
								.getAsDouble()
					);
				String networthIRLMoney = StringUtils.formatNumber(Math.round(
					((networthInCookies * 325) / 675) * 4.99));

				if (mouseX > guiLeft + offset - fontWidth / 2 && mouseX < guiLeft + offset + fontWidth / 2) {
					if (mouseY > guiTop + 32 && mouseY < guiTop + 38 + fr.FONT_HEIGHT) {
						getInstance().tooltipToDisplay = new ArrayList<>();
						getInstance()
							.tooltipToDisplay.add(
								EnumChatFormatting.GREEN +
									"Net worth in IRL money: " +
									EnumChatFormatting.DARK_GREEN +
									"$" +
									EnumChatFormatting.GOLD +
									networthIRLMoney
							);

						if (NotEnoughUpdates.INSTANCE.config.profileViewer.useSoopyNetworth
							&& profile.getSoopyNetworthLeaderboardPosition() >= 0
							&& profile.isProfileMaxSoopyWeight(profileName)) {
							getInstance().tooltipToDisplay.add("");
							String lbPosStr =
								EnumChatFormatting.DARK_GREEN + "#" + EnumChatFormatting.GOLD + StringUtils.formatNumber(
									profile.getSoopyNetworthLeaderboardPosition());
							getInstance().tooltipToDisplay.add(
								lbPosStr + EnumChatFormatting.GREEN + " on soopy's networth leaderboard!");
						}

						if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
							getInstance().tooltipToDisplay.addAll(nwCategoryHover);
							getInstance().tooltipToDisplay.add(EnumChatFormatting.RED + "This is calculated using the current");
							getInstance().tooltipToDisplay.add(
								EnumChatFormatting.RED + "price of booster cookies on bazaar and the price");
							getInstance().tooltipToDisplay.add(
								EnumChatFormatting.RED + "for cookies using gems, then the price of gems");
							getInstance().tooltipToDisplay.add(
								EnumChatFormatting.RED + "is where we get the amount of IRL money you");
							getInstance().tooltipToDisplay.add(
								EnumChatFormatting.RED + "theoretically have on SkyBlock in net worth.");
						} else {
							getInstance().tooltipToDisplay.add(EnumChatFormatting.GRAY + "[SHIFT for Info]");
						}
						if (!NotEnoughUpdates.INSTANCE.config.hidden.dev) {
							getInstance().tooltipToDisplay.add("");
							getInstance().tooltipToDisplay.add(EnumChatFormatting.RED + "THIS IS IN NO WAY ENDORSING IRL TRADING!");
						}
					}
				}
			}
		} else {
			int errFontWidth = fr.getStringWidth("Net Worth: " + stateStr);
			if (errFontWidth >= 117) {
				fr.drawString(EnumChatFormatting.GREEN + "Net Worth: " + stateStr,
					guiLeft + 8, guiTop + 38 - fr.FONT_HEIGHT / 2f, 0, true
				);
			} else {
				Utils.drawStringCentered(
					EnumChatFormatting.GREEN + "Net Worth: " + stateStr,
					guiLeft + 63, guiTop + 38, true, 0
				);
			}
		}

		if (status != null) {
			JsonElement onlineElement = Utils.getElement(status, "online");
			boolean online = onlineElement != null && onlineElement.isJsonPrimitive() && onlineElement.getAsBoolean();
			String statusStr = online ? EnumChatFormatting.GREEN + "ONLINE" : EnumChatFormatting.RED + "OFFLINE";
			String locationStr = null;
			if (profile.getUuid().equals("20934ef9488c465180a78f861586b4cf")) {
				locationStr = "Ignoring DMs";
			} else if (profile.getUuid().equals("b876ec32e396476ba1158438d83c67d4")) {
				statusStr = EnumChatFormatting.LIGHT_PURPLE + "Long live Potato King";
				ItemStack potato_crown = NotEnoughUpdates.INSTANCE.manager.jsonToStack(
					NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("POTATO_CROWN")
				);
				potato_crown.addEnchantment(Enchantment.unbreaking, 1656638942); // this number may be useful
				Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(
					new ItemStack(Items.potato),
					guiLeft + 35,
					guiTop + 160
				);
				Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(potato_crown, guiLeft + 50, guiTop + 162);
				Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(
					new ItemStack(Items.potato),
					guiLeft + 63,
					guiTop + 160
				);
			} else if (online) {
				locationStr = NotEnoughUpdates.INSTANCE.navigation.getNameForAreaModeOrUnknown(location);
			}
			if (locationStr != null) {
				statusStr += EnumChatFormatting.GRAY + " - " + EnumChatFormatting.GREEN + locationStr;
			}

			Utils.drawStringCentered(statusStr, guiLeft + 63, guiTop + 160, true, 0);
		} else {
			Rectangle buttonRect = new Rectangle(
				guiLeft + 24,
				guiTop + 155,
				80,
				12
			);

			RenderUtils.drawFloatingRectWithAlpha(buttonRect.getX(), buttonRect.getY(), buttonRect.getWidth(),
				buttonRect.getHeight(), 100, true
			);
			Utils.renderShadowedString(
				clickedLoadStatusButton
					? EnumChatFormatting.AQUA + "Loading..."
					: EnumChatFormatting.WHITE + "Load Status",
				guiLeft + 63,
				guiTop + 157,
				79
			);

			if (Mouse.getEventButtonState() && Utils.isWithinRect(mouseX, mouseY, buttonRect)) {
				clickedLoadStatusButton = true;
			}
		}

		if (entityPlayer == null) {
			if (!loadingProfile || ((ThreadPoolExecutor) profileLoader).getActiveCount() == 0) {
				loadingProfile = true;
				UUID playerUUID = UUID.fromString(niceUuid(profile.getUuid()));

				profileLoader.submit(() -> {
					GameProfile fakeProfile = Minecraft
						.getMinecraft()
						.getSessionService()
						.fillProfileProperties(new GameProfile(playerUUID, "CoolGuy123"), false);
					entityPlayer =
						new EntityOtherPlayerMP(Minecraft.getMinecraft().theWorld, fakeProfile) {
							public ResourceLocation getLocationSkin() {
								return playerLocationSkin == null
									? DefaultPlayerSkin.getDefaultSkin(this.getUniqueID())
									: playerLocationSkin;
							}

							public ResourceLocation getLocationCape() {
								return playerLocationCape;
							}

							public String getSkinType() {
								return skinType == null ? DefaultPlayerSkin.getSkinType(this.getUniqueID()) : skinType;
							}
						};
					entityPlayer.setAlwaysRenderNameTag(false);
					entityPlayer.setCustomNameTag("");
				});
			}
		} else {
			entityPlayer.refreshDisplayName();
			byte b = 0;
			for (EnumPlayerModelParts part : EnumPlayerModelParts.values()) {
				b |= part.getPartMask();
			}
			entityPlayer.getDataWatcher().updateObject(10, b);
		}

		Map<String, ProfileViewer.Level> skyblockInfo = getSelectedProfile().getLevelingInfo();
		Map<String, JsonArray> inventoryInfo = getSelectedProfile().getInventoryInfo();

		if (entityPlayer != null) {
			if (backgroundClickedX != -1 && Mouse.isButtonDown(1)) {
				Arrays.fill(entityPlayer.inventory.armorInventory, null);
			} else {
				if (inventoryInfo != null && inventoryInfo.containsKey("inv_armor")) {
					JsonArray items = inventoryInfo.get("inv_armor");
					if (items != null && items.size() == 4) {
						for (int i = 0; i < entityPlayer.inventory.armorInventory.length; i++) {
							JsonElement itemElement = items.get(i);
							if (itemElement != null && itemElement.isJsonObject()) {
								entityPlayer.inventory.armorInventory[i] =
									NotEnoughUpdates.INSTANCE.manager.jsonToStack(itemElement.getAsJsonObject(), false);
							}
						}
					}
				} else {
					Arrays.fill(entityPlayer.inventory.armorInventory, null);
				}
			}
			if (entityPlayer.getUniqueID().toString().equals("ae6193ab-494a-4719-b6e7-d50392c8f012")) {
				entityPlayer.inventory.armorInventory[3] =
					NotEnoughUpdates.INSTANCE.manager.jsonToStack(
						NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("SMALL_BACKPACK")
					);
			}
		}

		if (entityPlayer != null && playerLocationSkin == null) {
			try {
				Minecraft
					.getMinecraft()
					.getSkinManager()
					.loadProfileTextures(
						entityPlayer.getGameProfile(),
						(type, location1, profileTexture) -> {
							switch (type) {
								case SKIN:
									playerLocationSkin = location1;
									skinType = profileTexture.getMetadata("model");

									if (skinType == null) {
										skinType = "default";
									}

									break;
								case CAPE:
									playerLocationCape = location1;
							}
						},
						false
					);
			} catch (Exception ignored) {
			}
		}

		GlStateManager.color(1, 1, 1, 1);
		JsonObject petsInfo = profile.getProfile(profileName).getPetsInfo();
		if (petsInfo != null) {
			JsonElement activePetElement = petsInfo.get("active_pet");
			if (activePetElement != null && activePetElement.isJsonObject()) {
				JsonObject activePet = activePetElement.getAsJsonObject();

				String type = activePet.get("type").getAsString();

				for (int i = 0; i < 4; i++) {
					JsonObject item = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(type + ";" + i);
					if (item != null) {
						int x = guiLeft + 20;
						float y =
							guiTop +
								82 +
								15 *
									(float) Math.sin(((getInstance().currentTime - getInstance().startTime) / 800f) % (2 * Math.PI));
						GlStateManager.translate(x, y, 0);
						ItemStack stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(item, false);

						// Remove extra attributes so no CIT
						NBTTagCompound stackTag = stack.getTagCompound() == null ? new NBTTagCompound() : stack.getTagCompound();
						stackTag.removeTag("ExtraAttributes");
						stack.setTagCompound(stackTag);

						GlStateManager.scale(1.5f, 1.5f, 1);
						GlStateManager.enableDepth();
						Utils.drawItemStack(stack, 0, 0);
						GlStateManager.scale(1 / 1.5f, 1 / 1.5f, 1);
						GlStateManager.translate(-x, -y, 0);
						break;
					}
				}
			}
		}
		if (entityPlayer != null) {
			drawEntityOnScreen(
				guiLeft + 63,
				guiTop + 128 + 7,
				36,
				guiLeft + 63 - mouseX,
				guiTop + 129 - mouseY,
				entityPlayer
			);
		}

		// sb lvl

		int sbLevelX = guiLeft + 162;
		int sbLevelY = guiTop + 74;

		double skyblockLevel = profile.getProfile(profileName).getSkyblockLevel();
		EnumChatFormatting skyblockLevelColour = profile.getProfile(profileName).getSkyblockLevelColour();

		GlStateManager.pushMatrix();
		GlStateManager.translate(sbLevelX, sbLevelY, 0);
		GlStateManager.scale(1.5f, 1.5f, 1);
		Utils.drawItemStack(skull, 0, 0);
		GlStateManager.popMatrix();
		Utils.drawStringCenteredScaled(skyblockLevelColour.toString() + (int) skyblockLevel,
			sbLevelX + 9, sbLevelY - 12, true, 1.5f
		);

		float progress = (float) (skyblockLevel - (long) skyblockLevel);
		getInstance().renderBar(sbLevelX - 30, sbLevelY + 30, 80, progress);

		Utils.drawStringScaled(EnumChatFormatting.YELLOW.toString() + (int) (progress * 100) + "/100",
			sbLevelX - 30, sbLevelY + 20, true, 0, 0.9f
		);

		if (mouseX >= guiLeft + 128 && mouseX <= guiLeft + 216) {
			if (mouseY >= guiTop + 49 && mouseY <= guiTop + 113) {
				if (Mouse.isButtonDown(0)) onSecondPage = true;
			}
		}

		if (skyblockInfo != null && selectedProfile.skillsApiEnabled()) {
			int position = 0;
			for (Map.Entry<String, ItemStack> entry : ProfileViewer.getSkillToSkillDisplayMap().entrySet()) {
				if (entry.getValue() == null || entry.getKey() == null) {
					position++;
					continue;
				}

				int yPosition = position % 8;
				int xPosition = position / 8;

				String skillName = entry.getValue().getDisplayName();

				ProfileViewer.Level level = skyblockInfo.get(entry.getKey());
				int levelFloored = (int) Math.floor(level.level);

				int x = guiLeft + 237 + 86 * xPosition;
				int y = guiTop + 24 + 21 * yPosition;

				if (entry.getKey().equals("social")) {
					position--;
					x = guiLeft + 132;
					y = guiTop + 124;
				}

				Utils.renderAlignedString(skillName, EnumChatFormatting.WHITE.toString() + levelFloored, x + 14, y - 4, 60);

				if (level.maxed) {
					getInstance().renderGoldBar(x, y + 6, 80);
				} else {
					getInstance().renderBar(x, y + 6, 80, level.level % 1);
				}

				if (mouseX > x && mouseX < x + 80) {
					if (mouseY > y - 4 && mouseY < y + 13) {
						getInstance().tooltipToDisplay = new ArrayList<>();
						List<String> tooltipToDisplay = getInstance().tooltipToDisplay;
						tooltipToDisplay.add(skillName);
						if (level.maxed) {
							tooltipToDisplay.add(
								EnumChatFormatting.GRAY + "Progress: " + EnumChatFormatting.GOLD + "MAXED!");
						} else {
							int maxXp = (int) level.maxXpForLevel;
							getInstance()
								.tooltipToDisplay.add(
									EnumChatFormatting.GRAY +
										"Progress: " +
										EnumChatFormatting.DARK_PURPLE +
										StringUtils.shortNumberFormat(Math.round((level.level % 1) * maxXp)) +
										"/" +
										StringUtils.shortNumberFormat(maxXp));
						}
						String totalXpS = StringUtils.formatNumber((long) level.totalXp);
						tooltipToDisplay.add(EnumChatFormatting.GRAY + "Total XP: " + EnumChatFormatting.DARK_PURPLE + totalXpS +
							EnumChatFormatting.DARK_GRAY + " (" +
							StringUtils.formatToTenths(guiProfileViewer.getPercentage(entry.getKey().toLowerCase(), level)) +
							"% to " + level.maxLevel + ")");
						if (entry.getKey().equals("farming")) {
							// double drops + pelts
							int doubleDrops = Utils.getElementAsInt(Utils.getElement(selectedProfile.getProfileJson(), "jacob2.perks.double_drops"), 0);
							int peltCount = Utils.getElementAsInt(Utils.getElement(selectedProfile.getProfileJson(), "trapper_quest.pelt_count"), 0);

							if (doubleDrops == 15) {
								tooltipToDisplay.add("§7Double Drops: §6" + (doubleDrops * 2) + "%");
							} else tooltipToDisplay.add("§7Double Drops: §5" + (doubleDrops * 2) + "%");

							tooltipToDisplay.add("§7Pelts: §e" + peltCount);

							// medals
							JsonObject medals_inv = Utils.getElement(selectedProfile.getProfileJson(), "jacob2.medals_inv").getAsJsonObject();
							tooltipToDisplay.add(" ");
							for (String medalName : medalNames) {
								String textWithoutFormattingCodes =
									EnumChatFormatting.getTextWithoutFormattingCodes(medalName.toLowerCase());
								if (medals_inv.has(textWithoutFormattingCodes)) {
									int medalAmount = medals_inv.get(textWithoutFormattingCodes).getAsInt();
									tooltipToDisplay.add(EnumChatFormatting.GRAY + WordUtils.capitalize(medalName) + ": " +
										EnumChatFormatting.WHITE + medalAmount);
								} else {
									tooltipToDisplay.add(EnumChatFormatting.GRAY + WordUtils.capitalize(medalName) + ": " +
										EnumChatFormatting.WHITE + "0");
								}
							}
						}

						String slayerNameLower = entry.getKey().toLowerCase();
						if (Weight.SLAYER_NAMES.contains(slayerNameLower)) {
							JsonObject slayerToTier = Constants.LEVELING.getAsJsonObject("slayer_to_highest_tier");
							if (slayerToTier == null) {
								Utils.showOutdatedRepoNotification();
								return;
							}

							int maxLevel = slayerToTier.get(slayerNameLower).getAsInt();
							for (int i = 0; i < 5; i++) {
								if (i >= maxLevel) break;
								float tier = Utils.getElementAsFloat(
									Utils.getElement(selectedProfile.getProfileJson(), "slayer_bosses." + slayerNameLower + ".boss_kills_tier_" + i),
									0
								);
								tooltipToDisplay.add(EnumChatFormatting.GRAY + "T" + (i + 1) + " Kills: " +
									EnumChatFormatting.RED + (int) tier);
							}
						}
					}
				}

				GL11.glTranslatef((x), (y - 6f), 0);
				GL11.glScalef(0.7f, 0.7f, 1);
				Utils.drawItemStackLinear(entry.getValue(), 0, 0);
				GL11.glScalef(1 / 0.7f, 1 / 0.7f, 1);
				GL11.glTranslatef(-(x), -(y - 6f), 0);

				position++;
			}
		} else {
			Utils.drawStringCentered(
				EnumChatFormatting.RED + "Skills API not enabled!",
				guiLeft + 322, guiTop + 101, true, 0
			);
		}

		drawSideButtons();
		if (NotEnoughUpdates.INSTANCE.config.profileViewer.displayWeight) {
			renderWeight(mouseX, mouseY, selectedProfile);
		}
	}

	private String getIcon(String gameModeType) {
		switch (gameModeType) {
			case "island":
				return "§a☀";
			case "bingo":
				return "§7Ⓑ";
			case "ironman":
				return "§7♲";
			default:
				return "";
		}
	}

	@Override
	public void resetCache() {
		entityPlayer = null;
		playerLocationSkin = null;
		playerLocationCape = null;
		skinType = null;
	}

	private String niceUuid(String uuidStr) {
		if (uuidStr.length() != 32) return uuidStr;

		return (
			uuidStr.substring(0, 8) +
				"-" +
				uuidStr.substring(8, 12) +
				"-" +
				uuidStr.substring(12, 16) +
				"-" +
				uuidStr.substring(16, 20) +
				"-" +
				uuidStr.substring(20, 32)
		);
	}

	private void renderWeight(
		int mouseX,
		int mouseY,
		SkyblockProfiles.SkyblockProfile selectedProfile
	) {
		if (!selectedProfile.skillsApiEnabled()) {
			return;
		}

		Map<String, ProfileViewer.Level> skyblockInfo = selectedProfile.getLevelingInfo();
		if (skyblockInfo == null) {
			return;
		}

		SkyblockProfiles profile = GuiProfileViewer.getProfile();
		String profileName = GuiProfileViewer.getProfileName();

		if (Constants.WEIGHT == null || Utils.getElement(Constants.WEIGHT, "lily.skills.overall") == null ||
			!Utils.getElement(Constants.WEIGHT, "lily.skills.overall").isJsonPrimitive()) {
			Utils.showOutdatedRepoNotification();
			return;
		}

		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		SenitherWeight senitherWeight = new SenitherWeight(skyblockInfo);
		LilyWeight lilyWeight = new LilyWeight(skyblockInfo, selectedProfile.getProfileJson());

		long weight = -2L;
		if (NotEnoughUpdates.INSTANCE.config.profileViewer.useSoopyNetworth) {
			weight = profile.getSoopyWeightLeaderboardPosition();
		}

		Utils.drawStringCentered(
			EnumChatFormatting.GREEN +
				"Senither Weight: " +
				EnumChatFormatting.GOLD +
				StringUtils.formatNumber(roundToNearestInt(senitherWeight.getTotalWeight().getRaw())),
			guiLeft + 63, guiTop + 18, true, 0
		);

		int textWidth = fr.getStringWidth(
			"Senither Weight: " +
				StringUtils.formatNumber(roundToNearestInt(senitherWeight.getTotalWeight().getRaw()))
		);
		if (mouseX > guiLeft + 63 - textWidth / 2 && mouseX < guiLeft + 63 + textWidth / 2) {
			if (mouseY > guiTop + 12 && mouseY < guiTop + 12 + fr.FONT_HEIGHT) {
				getInstance().tooltipToDisplay = new ArrayList<>();
				getInstance()
					.tooltipToDisplay.add(
						EnumChatFormatting.GREEN +
							"Skills: " +
							EnumChatFormatting.GOLD +
							StringUtils.formatNumber(roundToNearestInt(senitherWeight
								.getSkillsWeight()
								.getWeightStruct()
								.getRaw()))
					);
				getInstance()
					.tooltipToDisplay.add(
						EnumChatFormatting.GREEN +
							"Slayer: " +
							EnumChatFormatting.GOLD +
							StringUtils.formatNumber(roundToNearestInt(senitherWeight
								.getSlayerWeight()
								.getWeightStruct()
								.getRaw()))
					);
				getInstance()
					.tooltipToDisplay.add(
						EnumChatFormatting.GREEN +
							"Dungeons: " +
							EnumChatFormatting.GOLD +
							StringUtils.formatNumber(
								roundToNearestInt(senitherWeight.getDungeonsWeight().getWeightStruct().getRaw())
							)
					);

				if (NotEnoughUpdates.INSTANCE.config.profileViewer.useSoopyNetworth
					&& profile.isProfileMaxSoopyWeight(profileName)) {

					String lbPosStr =
						EnumChatFormatting.DARK_GREEN + "#" + EnumChatFormatting.GOLD + StringUtils.formatNumber(
							profile.getSoopyWeightLeaderboardPosition());
					getInstance().tooltipToDisplay.add("");
					String stateStr = EnumChatFormatting.RED + "An error occurred";
					if (weight == -2) {
						stateStr = EnumChatFormatting.YELLOW + "Loading";
					}
					if (weight > 0)
						getInstance().tooltipToDisplay.add(lbPosStr + EnumChatFormatting.GREEN + " on soopy's weight leaderboard!");
					else
						getInstance().tooltipToDisplay.add(stateStr + " on soopy's weight leaderboard");
				}
			}
		}

		Utils.drawStringCentered(
			EnumChatFormatting.GREEN +
				"Lily Weight: " + EnumChatFormatting.GOLD +
				StringUtils.formatNumber(roundToNearestInt(lilyWeight.getTotalWeight().getRaw())),
			guiLeft + 63, guiTop + 28, true, 0
		);

		int fontWidth = fr.getStringWidth(
			"Lily Weight: " + StringUtils.formatNumber(roundToNearestInt(lilyWeight.getTotalWeight().getRaw()))
		);
		if (mouseX > guiLeft + 63 - fontWidth / 2 && mouseX < guiLeft + 63 + fontWidth / 2) {
			if (mouseY > guiTop + 22 && mouseY < guiTop + 22 + fr.FONT_HEIGHT) {
				getInstance().tooltipToDisplay = new ArrayList<>();
				getInstance()
					.tooltipToDisplay.add(
						EnumChatFormatting.GREEN +
							"Skills: " +
							EnumChatFormatting.GOLD +
							StringUtils.formatNumber(roundToNearestInt(lilyWeight
								.getSkillsWeight()
								.getWeightStruct()
								.getRaw()))
					);
				getInstance()
					.tooltipToDisplay.add(
						EnumChatFormatting.GREEN +
							"Slayer: " +
							EnumChatFormatting.GOLD +
							StringUtils.formatNumber(roundToNearestInt(lilyWeight
								.getSlayerWeight()
								.getWeightStruct()
								.getRaw()))
					);
				getInstance()
					.tooltipToDisplay.add(
						EnumChatFormatting.GREEN +
							"Dungeons: " +
							EnumChatFormatting.GOLD +
							StringUtils.formatNumber(roundToNearestInt(lilyWeight
								.getDungeonsWeight()
								.getWeightStruct()
								.getRaw()))
					);
			}
		}
	}

	private void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {

		ent.onUpdate();

		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) posX, (float) posY, 50.0F);
		GlStateManager.scale((float) (-scale), (float) scale, (float) scale);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		float renderYawOffset = ent.renderYawOffset;
		float f1 = ent.rotationYaw;
		float f2 = ent.rotationPitch;
		float f3 = ent.prevRotationYawHead;
		float f4 = ent.rotationYawHead;
		GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(25, 1.0F, 0.0F, 0.0F);
		ent.renderYawOffset = (float) Math.atan(mouseX / 40.0F) * 20.0F;
		ent.rotationYaw = (float) Math.atan(mouseX / 40.0F) * 40.0F;
		ent.rotationPitch = -((float) Math.atan(mouseY / 40.0F)) * 20.0F;
		ent.rotationYawHead = ent.rotationYaw;
		ent.prevRotationYawHead = ent.rotationYaw;
		RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
		rendermanager.setPlayerViewY(180.0F);
		rendermanager.setRenderShadow(false);
		rendermanager.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);

		ent.renderYawOffset = renderYawOffset;
		ent.rotationYaw = f1;
		ent.rotationPitch = f2;
		ent.prevRotationYawHead = f3;
		ent.rotationYawHead = f4;
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		int i = onSlotToChangePage(mouseX, mouseY, guiLeft, guiTop);
		switch (i) {
			case 1:
				onSecondPage = false;
				break;
			case 2:
				onSecondPage = true;
				break;

			default:
				break;
		}

		return false;
	}

	public int onSlotToChangePage(int mouseX, int mouseY, int guiLeft, int guiTop) {
		if (mouseX >= guiLeft - 29 && mouseX <= guiLeft) {
			if (mouseY >= guiTop && mouseY <= guiTop + 28) {
				return 1;
			} else if (mouseY + 28 >= guiTop && mouseY <= guiTop + 28 * 2) {
				return 2;
			}
		}
		return 0;
	}

	public void drawSideButtons() {
		GlStateManager.enableDepth();
		GlStateManager.translate(0, 0, 5);
		if (onSecondPage) {
			Utils.drawPvSideButton(1, dungeonsModeIcons.get("second_page"), true, guiProfileViewer);
		} else {
			Utils.drawPvSideButton(0, dungeonsModeIcons.get("first_page"), true, guiProfileViewer);
		}
		GlStateManager.translate(0, 0, -3);

		GlStateManager.translate(0, 0, -2);
		if (!onSecondPage) {
			Utils.drawPvSideButton(1, dungeonsModeIcons.get("second_page"), false, guiProfileViewer);
		} else {
			Utils.drawPvSideButton(0, dungeonsModeIcons.get("first_page"), false, guiProfileViewer);
		}
		GlStateManager.disableDepth();
	}
}
