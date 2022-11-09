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

import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.profileviewer.weight.lily.LilyWeight;
import io.github.moulberry.notenoughupdates.profileviewer.weight.senither.SenitherWeight;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.PronounDB;
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
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer.DECIMAL_FORMAT;
import static io.github.moulberry.notenoughupdates.util.Utils.roundToNearestInt;

public class BasicPage extends GuiProfileViewerPage {

	private static final ResourceLocation pv_basic = new ResourceLocation("notenoughupdates:pv_basic.png");
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

	public BasicPage(GuiProfileViewer instance) {
		super(instance);
		this.guiProfileViewer = instance;
	}

	@Override
	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
		ProfileViewer.Profile profile = GuiProfileViewer.getProfile();
		String profileId = GuiProfileViewer.getProfileId();
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		String location = null;
		JsonObject status = profile.getPlayerStatus();
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
		JsonObject profileInfo = profile.getProfileInformation(profileId);
		if (profileInfo == null) return;

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
							String icon = getIcon(getGameModeType(profileInfo));
							playerName =
								"\u00A7" + rankColor + "[" + rankName + rankPlusColor + rankPlus + "\u00A7" + rankColor + "] " + name +
									(icon.equals("") ? "" : " " + icon);
							;
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

		long networth;
		ArrayList<String> nwCategoryHover = new ArrayList<>();
		if (NotEnoughUpdates.INSTANCE.config.profileViewer.useSoopyNetworth) {
			ProfileViewer.Profile.SoopyNetworthData nwData = profile.getSoopyNetworth(profileId, () -> {});
			if (nwData == null) {
				networth = -2l;
			} else {
				networth = nwData.getTotal();

				for (String category : nwData.getCategories()) {
					if (nwData.getCategory(category) == 0) continue;

					nwCategoryHover.add(EnumChatFormatting.GREEN +
						WordUtils.capitalizeFully(category.replace("_", " ")) +
						": " +
						EnumChatFormatting.GOLD +
						GuiProfileViewer.numberFormat.format(nwData.getCategory(category)));
				}

				nwCategoryHover.add("");
			}
		} else {
			networth = profile.getNetWorth(profileId);
		}

		if (networth > 0) {
			Utils.drawStringCentered(
				EnumChatFormatting.GREEN + "Net Worth: " + EnumChatFormatting.GOLD +
					GuiProfileViewer.numberFormat.format(networth),
				fr,
				guiLeft + 63,
				guiTop + 38,
				true,
				0
			);
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
				String networthIRLMoney = GuiProfileViewer.numberFormat.format(Math.round(
					((networthInCookies * 325) / 675) * 4.99));
				if (
					mouseX > guiLeft + 8 &&
						mouseX < guiLeft + 8 + fr.getStringWidth("Net Worth: " + GuiProfileViewer.numberFormat.format(networth))
				) {
					if (mouseY > guiTop + 32 && mouseY < guiTop + 32 + fr.FONT_HEIGHT) {
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
						getInstance().tooltipToDisplay.add("");

						if (NotEnoughUpdates.INSTANCE.config.profileViewer.useSoopyNetworth
							&& profile.getSoopyNetworthLeaderboardPosition() >= 0
							&& profile.isProfileMaxSoopyNetworth(profileId)) {

							String lbPosStr =
								EnumChatFormatting.DARK_GREEN + "#" + EnumChatFormatting.GOLD + GuiProfileViewer.numberFormat.format(
									profile.getSoopyNetworthLeaderboardPosition());
							getInstance().tooltipToDisplay.add(lbPosStr + EnumChatFormatting.GREEN + " on the networth leaderboard!");
							getInstance().tooltipToDisplay.add("");
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
			//Networth is under 0
			//If = -1 -> an error occured
			//If = -2 -> still loading networth

			String stateStr = EnumChatFormatting.RED + "An error occured";
			if (networth == -2) {
				stateStr = EnumChatFormatting.YELLOW + "Loading...";
			}

			Utils.drawStringCentered(
				EnumChatFormatting.GREEN + "Net Worth: " + stateStr,
				fr,
				guiLeft + 63,
				guiTop + 38,
				true,
				0
			);
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

			Utils.drawStringCentered(statusStr, fr, guiLeft + 63, guiTop + 160, true, 0);
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

		Map<String, ProfileViewer.Level> skyblockInfo = profile.getSkyblockInfo(profileId);
		JsonObject inventoryInfo = profile.getInventoryInfo(profileId);

		if (entityPlayer != null) {
			if (backgroundClickedX != -1 && Mouse.isButtonDown(1)) {
				Arrays.fill(entityPlayer.inventory.armorInventory, null);
			} else {
				if (inventoryInfo != null && inventoryInfo.has("inv_armor")) {
					JsonArray items = inventoryInfo.get("inv_armor").getAsJsonArray();
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
		JsonObject petsInfo = profile.getPetsInfo(profileId);
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

						//Remove extra attributes so no CIT
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

		PlayerStats.Stats stats = profile.getStats(profileId);

		if (stats != null) {
			Splitter splitter = Splitter.on(" ").omitEmptyStrings().limit(2);
			for (int i = 0; i < PlayerStats.defaultStatNames.length; i++) {
				String statName = PlayerStats.defaultStatNames[i];
				//if (statName.equals("mining_fortune") || statName.equals("mining_speed")) continue;
				String statNamePretty = PlayerStats.defaultStatNamesPretty[i];

				int val = Math.round(stats.get(statName));

				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.enableBlend();
				GL14.glBlendFuncSeparate(
					GL11.GL_SRC_ALPHA,
					GL11.GL_ONE_MINUS_SRC_ALPHA,
					GL11.GL_ONE,
					GL11.GL_ONE_MINUS_SRC_ALPHA
				);
				Utils.renderAlignedString(
					statNamePretty,
					EnumChatFormatting.WHITE.toString() + val,
					guiLeft + 132,
					guiTop + 21 + 11f * i,
					80
				);

				if (mouseX > guiLeft + 132 && mouseX < guiLeft + 212) {
					if (mouseY > guiTop + 21 + 11f * i && mouseY < guiTop + 37 + 11f * i) {
						List<String> split = splitter.splitToList(statNamePretty);
						PlayerStats.Stats baseStats = PlayerStats.getBaseStats();
						getInstance().tooltipToDisplay = new ArrayList<>();
						getInstance().tooltipToDisplay.add(statNamePretty);
						int base = Math.round(baseStats.get(statName));
						getInstance()
							.tooltipToDisplay.add(
								EnumChatFormatting.GRAY +
									"Base " +
									split.get(1) +
									": " +
									EnumChatFormatting.GREEN +
									base +
									" " +
									split.get(0)
							);
						int passive = Math.round(profile.getPassiveStats(profileId).get(statName) - baseStats.get(statName));
						getInstance()
							.tooltipToDisplay.add(
								EnumChatFormatting.GRAY +
									"Passive " +
									split.get(1) +
									" Bonus: +" +
									EnumChatFormatting.YELLOW +
									passive +
									" " +
									split.get(0)
							);
						int itemBonus = Math.round(stats.get(statName) - profile.getPassiveStats(profileId).get(statName));
						getInstance()
							.tooltipToDisplay.add(
								EnumChatFormatting.GRAY +
									"Item " +
									split.get(1) +
									" Bonus: +" +
									EnumChatFormatting.DARK_PURPLE +
									itemBonus +
									" " +
									split.get(0)
							);
						int finalStat = Math.round(stats.get(statName));
						getInstance()
							.tooltipToDisplay.add(
								EnumChatFormatting.GRAY +
									"Final " +
									split.get(1) +
									": +" +
									EnumChatFormatting.RED +
									finalStat +
									" " +
									split.get(0)
							);
					}
				}
			}
		} else {
			Utils.drawStringCentered(
				EnumChatFormatting.RED + "Skill/Inv/Coll",
				Minecraft.getMinecraft().fontRendererObj,
				guiLeft + 172,
				guiTop + 101 - 10,
				true,
				0
			);
			Utils.drawStringCentered(
				EnumChatFormatting.RED + "APIs not",
				Minecraft.getMinecraft().fontRendererObj,
				guiLeft + 172,
				guiTop + 101,
				true,
				0
			);
			Utils.drawStringCentered(
				EnumChatFormatting.RED + "enabled!",
				Minecraft.getMinecraft().fontRendererObj,
				guiLeft + 172,
				guiTop + 101 + 10,
				true,
				0
			);
		}

		if (skyblockInfo != null) {
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
						String totalXpS = GuiProfileViewer.numberFormat.format((int) level.totalXp);
						tooltipToDisplay.add(EnumChatFormatting.GRAY + "Total XP: " + EnumChatFormatting.DARK_PURPLE + totalXpS +
							EnumChatFormatting.DARK_GRAY + " (" +
							DECIMAL_FORMAT.format(guiProfileViewer.getPercentage(entry.getKey().toLowerCase(), level)) +
							"% to " + level.maxLevel + ")");
						if (entry.getKey().equals("farming")) {
							// double drops + pelts
							int doubleDrops = Utils.getElementAsInt(Utils.getElement(profileInfo, "jacob2.perks.double_drops"), 0);
							int peltCount = Utils.getElementAsInt(Utils.getElement(profileInfo, "trapper_quest.pelt_count"), 0);

							if (doubleDrops == 15) {
								tooltipToDisplay.add("§7Double Drops: §6" + (doubleDrops * 2) + "%");
							} else tooltipToDisplay.add("§7Double Drops: §5" + (doubleDrops * 2) + "%");

							tooltipToDisplay.add("§7Pelts: §e" + peltCount);

							// medals
							JsonObject medals_inv = Utils.getElement(profileInfo, "jacob2.medals_inv").getAsJsonObject();
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
						if (ExtraPage.slayers.containsKey(slayerNameLower)) {
							int maxLevel = ExtraPage.slayers.get(slayerNameLower);
							for (int i = 0; i < 5; i++) {
								if (i >= maxLevel) break;
								float tier = Utils.getElementAsFloat(
									Utils.getElement(profileInfo, "slayer_bosses." + slayerNameLower + ".boss_kills_tier_" + i),
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
				Minecraft.getMinecraft().fontRendererObj,
				guiLeft + 322,
				guiTop + 101,
				true,
				0
			);
		}

		renderWeight(mouseX, mouseY, skyblockInfo, profileInfo);
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
		Map<String, ProfileViewer.Level> skyblockInfo,
		JsonObject profileInfo
	) {
		if (skyblockInfo == null) {
			return;
		}

		if (Constants.WEIGHT == null || Utils.getElement(Constants.WEIGHT, "lily.skills.overall") == null ||
			!Utils.getElement(Constants.WEIGHT, "lily.skills.overall").isJsonPrimitive()) {
			Utils.showOutdatedRepoNotification();
			return;
		}

		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		SenitherWeight senitherWeight = new SenitherWeight(skyblockInfo);
		LilyWeight lilyWeight = new LilyWeight(skyblockInfo, profileInfo);

		Utils.drawStringCentered(
			EnumChatFormatting.GREEN +
				"Senither Weight: " +
				EnumChatFormatting.GOLD +
				GuiProfileViewer.numberFormat.format(roundToNearestInt(senitherWeight.getTotalWeight().getRaw())),
			fr,
			guiLeft + 63,
			guiTop + 18,
			true,
			0
		);

		int textWidth = fr.getStringWidth(
			"Senither Weight: " +
				GuiProfileViewer.numberFormat.format(roundToNearestInt(senitherWeight.getTotalWeight().getRaw()))
		);
		if (mouseX > guiLeft + 63 - textWidth / 2 && mouseX < guiLeft + 63 + textWidth / 2) {
			if (mouseY > guiTop + 12 && mouseY < guiTop + 12 + fr.FONT_HEIGHT) {
				getInstance().tooltipToDisplay = new ArrayList<>();
				getInstance()
					.tooltipToDisplay.add(
						EnumChatFormatting.GREEN +
							"Skills: " +
							EnumChatFormatting.GOLD +
							GuiProfileViewer.numberFormat.format(roundToNearestInt(senitherWeight
								.getSkillsWeight()
								.getWeightStruct()
								.getRaw()))
					);
				getInstance()
					.tooltipToDisplay.add(
						EnumChatFormatting.GREEN +
							"Slayer: " +
							EnumChatFormatting.GOLD +
							GuiProfileViewer.numberFormat.format(roundToNearestInt(senitherWeight
								.getSlayerWeight()
								.getWeightStruct()
								.getRaw()))
					);
				getInstance()
					.tooltipToDisplay.add(
						EnumChatFormatting.GREEN +
							"Dungeons: " +
							EnumChatFormatting.GOLD +
							GuiProfileViewer.numberFormat.format(
								roundToNearestInt(senitherWeight.getDungeonsWeight().getWeightStruct().getRaw())
							)
					);
			}
		}

		Utils.drawStringCentered(
			EnumChatFormatting.GREEN +
				"Lily Weight: " +
				EnumChatFormatting.GOLD +
				GuiProfileViewer.numberFormat.format(roundToNearestInt(lilyWeight.getTotalWeight().getRaw())),
			fr,
			guiLeft + 63,
			guiTop + 28,
			true,
			0
		);

		int fontWidth = fr.getStringWidth(
			"Lily Weight: " + GuiProfileViewer.numberFormat.format(roundToNearestInt(lilyWeight.getTotalWeight().getRaw()))
		);
		if (mouseX > guiLeft + 63 - fontWidth / 2 && mouseX < guiLeft + 63 + fontWidth / 2) {
			if (mouseY > guiTop + 22 && mouseY < guiTop + 22 + fr.FONT_HEIGHT) {
				getInstance().tooltipToDisplay = new ArrayList<>();
				getInstance()
					.tooltipToDisplay.add(
						EnumChatFormatting.GREEN +
							"Skills: " +
							EnumChatFormatting.GOLD +
							GuiProfileViewer.numberFormat.format(roundToNearestInt(lilyWeight
								.getSkillsWeight()
								.getWeightStruct()
								.getRaw()))
					);
				getInstance()
					.tooltipToDisplay.add(
						EnumChatFormatting.GREEN +
							"Slayer: " +
							EnumChatFormatting.GOLD +
							GuiProfileViewer.numberFormat.format(roundToNearestInt(lilyWeight
								.getSlayerWeight()
								.getWeightStruct()
								.getRaw()))
					);
				getInstance()
					.tooltipToDisplay.add(
						EnumChatFormatting.GREEN +
							"Dungeons: " +
							EnumChatFormatting.GOLD +
							GuiProfileViewer.numberFormat.format(roundToNearestInt(lilyWeight
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

	public String getGameModeType(JsonObject profileInfo) {
		if (profileInfo != null && profileInfo.has("game_mode")) {
			return profileInfo.get("game_mode").getAsString();
		}
		return "";
	}
}
