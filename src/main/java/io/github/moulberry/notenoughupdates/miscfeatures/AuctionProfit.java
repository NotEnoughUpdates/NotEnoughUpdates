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

package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.events.ButtonExclusionZoneEvent;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.util.Rectangle;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

@NEUAutoSubscribe
public class AuctionProfit {

	public static final ResourceLocation auctionProfitImage =
		new ResourceLocation("notenoughupdates:auction_profit.png");

	@SubscribeEvent
	public void onButtonExclusionZones(ButtonExclusionZoneEvent event) {
		if (inAuctionPage()) {
			event.blockArea(
				new Rectangle(
					event.getGuiBaseRect().getRight(),
					event.getGuiBaseRect().getTop(),
					128 /*width*/ + 4 /*space*/, 56
				),
				ButtonExclusionZoneEvent.PushDirection.TOWARDS_RIGHT
			);
		}
	}

	@SubscribeEvent
	public void onDrawBackground(GuiScreenEvent.BackgroundDrawnEvent event) {
		if (!inAuctionPage()) return;

		Minecraft minecraft = Minecraft.getMinecraft();
		Container inventoryContainer = minecraft.thePlayer.openContainer;

		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;
		Gui gui = event.gui;
		int xSize = ((AccessorGuiContainer) gui).getXSize();
		int guiLeft = ((AccessorGuiContainer) gui).getGuiLeft();
		int guiTop = ((AccessorGuiContainer) gui).getGuiTop();
		minecraft.getTextureManager().bindTexture(auctionProfitImage);
		GL11.glColor4f(1, 1, 1, 1);
		GlStateManager.disableLighting();
		Utils.drawTexturedRect(guiLeft + xSize + 4, guiTop, 180, 101, 0, 180 / 256f, 0, 101 / 256f, GL11.GL_NEAREST);

		double coinsToCollect = 0;
		double coinsIfAllSold = 0;
		int expiredAuctions = 0;
		int unclaimedAuctions = 0;
		for (ItemStack itemStack : inventoryContainer.getInventory()) {
			boolean isBin = false;
			if (itemStack == null || !itemStack.hasTagCompound()) continue;

			NBTTagCompound tag = itemStack.getTagCompound();
			if (tag == null) continue;
			NBTTagCompound display = tag.getCompoundTag("display");
			if (!display.hasKey("Lore", 9)) continue;
			NBTTagList lore = itemStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);

			double coinsToCheck = 0;
			for (int i = 0; i < lore.tagCount(); i++) {
				String line = lore.getStringTagAt(i);
				if (line.contains("§7Buy it now")) {
					isBin = true;
					String s = line.split("§7Buy it now: ")[1];
					String coinsString = s.split("coins")[0];
					double coins = tryParse(EnumChatFormatting.getTextWithoutFormattingCodes(coinsString.trim()));
					if (coins != 0) {
						coinsToCheck += coins;
					}
				}

				if (line.contains("§7Top bid: ")) {
					String s = line.split("§7Top bid: ")[1];
					String coinsString = s.split("coins")[0];
					String textWithoutFormattingCodes = EnumChatFormatting.getTextWithoutFormattingCodes(coinsString.trim());
					double coins = tryParse(textWithoutFormattingCodes);
					if (coins != 0) {
						coinsToCheck += coins;
					}
				}

				if (line.contains("§7Sold for: ")) {
					String s = line.split("§7Sold for: ")[1];
					String coinsString = s.split("coins")[0];
					double coins = tryParse(EnumChatFormatting.getTextWithoutFormattingCodes(coinsString.trim()));
					if (coins != 0) {
						coins = removeTax(coins);
						coinsToCollect += coins;
					}
				}

				if (line.contains("§7Status: §aSold!") || line.contains("§7Status: §aEnded!")) {
					if (coinsToCheck != 0) {
						coinsToCheck = removeTax(coinsToCheck);
						coinsToCollect += coinsToCheck;
						coinsToCheck = 0;
					}
					unclaimedAuctions++;
				} else if (line.contains("§7Status: §cExpired!")) {
					expiredAuctions++;
				}

				if (isBin && line.contains("§7Ends in") && coinsToCheck != 0) {
					coinsIfAllSold += coinsToCheck;
					coinsToCheck = 0;
				}

			}

		}
		int a = guiLeft + xSize + 4;
		String unclaimedAuctionsStr = EnumChatFormatting.DARK_GREEN.toString()
			+ unclaimedAuctions + EnumChatFormatting.BOLD + EnumChatFormatting.DARK_GRAY + " Unclaimed auctions";
		String expiredAuctionsStr =
			EnumChatFormatting.RED.toString() + expiredAuctions + EnumChatFormatting.BOLD + EnumChatFormatting.DARK_GRAY +
				" Expired auctions";

		FontRenderer fontRendererObj = minecraft.fontRendererObj;
		fontRendererObj.drawString(unclaimedAuctionsStr, a + 6, guiTop + 6, -1, false);
		fontRendererObj.drawString(expiredAuctionsStr, a + 6, guiTop + 16, -1, false);

		String coinsToCollectStr =
			EnumChatFormatting.BOLD + EnumChatFormatting.DARK_GRAY.toString() + "Coins to collect: " +
				EnumChatFormatting.RESET + EnumChatFormatting.DARK_GREEN + "" +
				StringUtils.shortNumberFormat(coinsToCollect);
		String valueIfSoldStr = EnumChatFormatting.BOLD + EnumChatFormatting.DARK_GRAY.toString() + "Value if all sold: " +
			EnumChatFormatting.RESET + EnumChatFormatting.DARK_GREEN + "" +
			StringUtils.shortNumberFormat(coinsIfAllSold);

		fontRendererObj.drawString(coinsToCollectStr, a + 6, guiTop + 32, -1, false);
		fontRendererObj.drawString(valueIfSoldStr, a + 6, guiTop + 42, -1, false);
	}

	private double removeTax(double coins) {
		if (coins < 10_000_000) {
			return coins / 1.01;
		}
		if (coins < 100_000_000) {
			return coins / 1.02;
		}
		return coins / 1.025;
	}

	public static Double tryParse(String s) {
		try {
			return Double.parseDouble(s.replace(",", ""));
		} catch (NumberFormatException exception) {
			return 0.0;
		}
	}

	public static boolean inAuctionPage() {
		if (!NotEnoughUpdates.INSTANCE.config.ahTweaks.enableAhSellValue
			|| !NotEnoughUpdates.INSTANCE.isOnSkyblock()) return false;

		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft == null || minecraft.thePlayer == null) return false;

		Container inventoryContainer = minecraft.thePlayer.openContainer;
		if (!(inventoryContainer instanceof ContainerChest)) return false;
		ContainerChest containerChest = (ContainerChest) inventoryContainer;
		return containerChest.getLowerChestInventory().getDisplayName()
												 .getUnformattedText().equalsIgnoreCase("Manage Auctions");
	}
}
