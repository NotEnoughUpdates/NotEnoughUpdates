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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.util.ItemResolutionQuery;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.moulberry.notenoughupdates.util.GuiTextures.dungeon_chest_worth;

public class DungeonNpcProfitOverlay {

	public static final ResourceLocation dungeonProfitResource =
		new ResourceLocation("notenoughupdates:dungeon_chest_worth.png");

	enum DungeonChest {
		WOOD, GOLD, DIAMOND, EMERALD, OBSIDIAN, BEDROCK;
	}

	static class ChestProfit {
		private DungeonChest dungeonChest;
		private boolean profit = false;
		private final List<String> items = new ArrayList<>();
	}

	private final Pattern pattern = Pattern.compile(".+ Catacombs - Floor .+");

	private final List<ChestProfit> chestProfits = new ArrayList<>();
	private ItemStack last;

	@SubscribeEvent
	public void onDrawBackground(GuiScreenEvent.BackgroundDrawnEvent event) {
		if (!(event.gui instanceof GuiChest)) return;
		String lastOpenChestName = SBInfo.getInstance().lastOpenChestName;
		Matcher matcher = pattern.matcher(lastOpenChestName);
		if (!matcher.matches()) return;

		GuiChest guiChest = (GuiChest) event.gui;
		if (guiChest.inventorySlots.getSlot(10).getStack() == last) {
			render(guiChest);
			return;
		}
		System.out.println("further");

		Map<DungeonChest, ChestProfit> chests = new HashMap<>();
		Map<String, DungeonChest> itemsInChest = new HashMap<>();
		for (Slot inventorySlot : guiChest.inventorySlots.inventorySlots) {
			ItemStack stack = inventorySlot.getStack();
			if (stack != null && stack.getItem() != null && stack.getItem() == Items.skull) {
				List<String> lore = ItemUtils.getLore(stack);
				boolean shouldAdd = false;
				for (String s : lore) {
					s = EnumChatFormatting.getTextWithoutFormattingCodes(s);
					if (shouldAdd) {
						DungeonChest dungeonChest = DungeonChest.valueOf(EnumChatFormatting.getTextWithoutFormattingCodes(
							stack.getDisplayName().replace(" Chest", "")).toUpperCase());
						System.out.println(s);
						itemsInChest.put(s, dungeonChest);
					}
					String textWithoutFormattingCodes = EnumChatFormatting.getTextWithoutFormattingCodes(s);
					if (textWithoutFormattingCodes.equals("Contents")) {
						shouldAdd = true;
					}
					if (textWithoutFormattingCodes.isEmpty()) {
						shouldAdd = false;
					}
				}

			}
		}
		for (Map.Entry<String, JsonObject> stringJsonObjectEntry : NotEnoughUpdates.INSTANCE.manager
			.getItemInformation()
			.entrySet()) {
			JsonObject value = stringJsonObjectEntry.getValue();
			if (value.has("displayname")) {
				String displayname = EnumChatFormatting.getTextWithoutFormattingCodes(value.get("displayname").getAsString());
				itemsInChest.forEach((name, dungeonChest) -> {
					if (name.startsWith("Enchanted Book ")) {
						JsonArray lore = value.get("lore").getAsJsonArray();
						String enchantedBookName = EnumChatFormatting.getTextWithoutFormattingCodes(lore.get(0).getAsString());
						if (name.contains(enchantedBookName)) {
							ChestProfit chestProfitCached = chests.get(dungeonChest);
							chestProfitCached.items.add(enchantedBookName);
						} else {
							ChestProfit chestProfit = new ChestProfit();
							chestProfit.dungeonChest = dungeonChest;
							chestProfit.items.add(enchantedBookName);
							chests.put(dungeonChest, chestProfit);
						}
					}
					if (displayname.contains(name)) {
						if (chests.containsKey(dungeonChest)) {
							ChestProfit chestProfit = chests.get(dungeonChest);
							chestProfit.items.add(name);
						} else {
							ChestProfit chestProfit = new ChestProfit();
							chestProfit.dungeonChest = dungeonChest;
							chestProfit.items.add(name);
							chests.put(dungeonChest, chestProfit);
						}
					}
				});

				chestProfits.addAll(chests.values());
				last = guiChest.inventorySlots.getSlot(10).getStack();
			}
		}
	}

	public void render(GuiChest guiChest) {
		int xSize = ((AccessorGuiContainer) guiChest).getXSize();
		int guiLeft = ((AccessorGuiContainer) guiChest).getGuiLeft();
		int guiTop = ((AccessorGuiContainer) guiChest).getGuiTop();
		Minecraft.getMinecraft().getTextureManager().bindTexture(dungeon_chest_worth);
		GL11.glColor4f(1, 1, 1, 1);
		GlStateManager.disableLighting();
		Utils.drawTexturedRect(guiLeft + xSize + 4, guiTop, 180, 101, 0, 180 / 256f, 0, 101 / 256f, GL11.GL_NEAREST);

		for (int i = 0; i < chestProfits.size(); i++) {
			ChestProfit chestProfit = chestProfits.get(i);
			Utils.renderAlignedString(
				chestProfit.dungeonChest + ": ",
				chestProfit.profit + "",
				guiLeft + xSize + 4 + 10,
				guiTop + 2 + (i * 3),
				160
			);
		}
	}
}
