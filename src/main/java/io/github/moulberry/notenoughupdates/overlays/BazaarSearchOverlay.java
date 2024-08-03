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

package io.github.moulberry.notenoughupdates.overlays;

import com.google.common.base.Splitter;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.commands.help.SettingsCommand;
import io.github.moulberry.notenoughupdates.core.GuiElementTextField;
import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@NEUAutoSubscribe
public class BazaarSearchOverlay extends GuiScreen {
	private static final ResourceLocation SEARCH_OVERLAY_TEXTURE = new ResourceLocation(
		"notenoughupdates:auc_search/ah_search_overlay.png");
	private static final ResourceLocation SEARCH_OVERLAY_TEXTURE_TAB_COMPLETED = new ResourceLocation(
		"notenoughupdates:auc_search/ah_search_overlay_tab_completed.png");

	private static final GuiElementTextField textField = new GuiElementTextField("", 200, 20, 0);
	private static boolean searchFieldClicked = false;
	private static String searchString = "";
	private static String searchStringExtra = "";
	private static final Splitter SPACE_SPLITTER = Splitter.on(" ").omitEmptyStrings().trimResults();
	private static boolean tabCompleted = false;
	private static int tabCompletionIndex = -1;

	private static final int AUTOCOMPLETE_HEIGHT = 118;

	private static final Set<String> autocompletedItems = new LinkedHashSet<>();

	private static final Comparator<String> salesComparator = (o1, o2) -> {
		JsonObject bazaarInfo1 = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo(o1);
		JsonObject bazaarInfo2 = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo(o2);

		boolean auc1Invalid = bazaarInfo1 == null || !bazaarInfo1.has("curr_sell");
		boolean auc2Invalid = bazaarInfo2 == null || !bazaarInfo2.has("curr_sell");

		if (auc1Invalid && auc2Invalid) return o1.compareTo(o2);
		if (auc1Invalid) return 1;
		if (auc2Invalid) return -1;

		int sales1 = bazaarInfo1.get("curr_sell").getAsInt();
		int sales2 = bazaarInfo2.get("curr_sell").getAsInt();

		if (sales1 == sales2) return o1.compareTo(o2);
		if (sales1 > sales2) return -1;
		return 1;
	};

	public BazaarSearchOverlay() {
		super();
	}

	static boolean isGuiOpen = false;
	public static boolean shouldReplace() {
		return isGuiOpen; //this whole method is just so skyhanni doesnt crash 💀
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		isGuiOpen = true;
		super.drawScreen(mouseX, mouseY, partialTicks);
		super.drawDefaultBackground();

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();

		int h = NotEnoughUpdates.INSTANCE.config.bazaarTweaks.showPastSearches ? 219 : 145;

		int topY = height / 4;
		if (scaledResolution.getScaleFactor() >= 4) {
			topY = height / 2 - h / 2 + 5;
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(SEARCH_OVERLAY_TEXTURE);
		GlStateManager.color(1, 1, 1, 1);
		Utils.drawTexturedRect(width / 2 - 100, topY - 1, 203, 145, 0, 203 / 512f, 0, 145 / 256f, GL11.GL_NEAREST);

		Minecraft.getMinecraft().fontRendererObj.drawString("Enter Query:", width / 2 - 100, topY - 10, 0xdddddd, true);

		textField.setFocus(true);
		textField.setText(searchString);
		textField.setSize(149, 20);
		textField.setCustomBorderColour(0xffffff);
		textField.render(width / 2 - 100 + 1, topY + 1);

		if (textField.getText().trim().isEmpty()) autocompletedItems.clear();

		List<String> tooltipToDisplay = null;

		int num = 0;
		synchronized (autocompletedItems) {
			String[] autoCompletedItemsArray = autocompletedItems.toArray(new String[0]);
			for (int i = 0; i < autoCompletedItemsArray.length; i++) {
				String str = autoCompletedItemsArray[i];
				JsonObject obj = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(str);
				if (obj != null) {
					ItemStack stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(obj);
					if (i == tabCompletionIndex) {
						Minecraft.getMinecraft().getTextureManager().bindTexture(SEARCH_OVERLAY_TEXTURE_TAB_COMPLETED);
						GlStateManager.color(1, 1, 1, 1);
						Utils.drawTexturedRect(
							width / 2 - 96 + 1,
							topY + 30 + num * 22 + 1,
							193,
							21,
							0 / 512f,
							193 / 512f,
							0,
							21 / 256f,
							GL11.GL_NEAREST
						);
					} else {
						Minecraft.getMinecraft().getTextureManager().bindTexture(SEARCH_OVERLAY_TEXTURE);
						GlStateManager.color(1, 1, 1, 1);
						Utils.drawTexturedRect(
							width / 2 - 96 + 1,
							topY + 30 + num * 22 + 1,
							193,
							21,
							214 / 512f,
							407 / 512f,
							0,
							21 / 256f,
							GL11.GL_NEAREST
						);

					}
					String itemName = Utils.trimIgnoreColour(stack.getDisplayName().replaceAll("\\[.+]", ""));
					if (itemName.contains("Enchanted Book") && str.contains(";")) {
						String[] lore = NotEnoughUpdates.INSTANCE.manager.getLoreFromNBT(stack.getTagCompound());
						itemName = lore[0].trim();
					}

					Minecraft.getMinecraft().fontRendererObj.drawString(Minecraft.getMinecraft().fontRendererObj.trimStringToWidth(
							itemName,
							165
						),
						width / 2 - 74, topY + 35 + num * 22 + 1, 0xdddddd, true
					);

					GlStateManager.enableDepth();
					Utils.drawItemStack(stack, width / 2 - 94 + 2, topY + 32 + num * 22 + 1);

					if (mouseX > width / 2 - 96 && mouseX < width / 2 + 96 && mouseY > topY + 30 + num * 22 &&
						mouseY < topY + 30 + num * 22 + 20) {
						tooltipToDisplay = stack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
					}

					if (++num >= 5) break;
				}
			}
		}

		if (NotEnoughUpdates.INSTANCE.config.bazaarTweaks.showPastSearches) {
			Minecraft.getMinecraft().fontRendererObj.drawString(
				"Past Searches:",
				width / 2 - 100,
				topY + 25 + AUTOCOMPLETE_HEIGHT + 5,
				0xdddddd,
				true
			);
			Minecraft.getMinecraft().getTextureManager().bindTexture(SEARCH_OVERLAY_TEXTURE);
			Utils.drawTexturedRect(width / 2 - 100, topY - 1 + 160, 203, 4, 0, 203 / 512f, 160 / 256f, 163 / 256f, GL11.GL_NEAREST);

			for (int i = 0; i < NotEnoughUpdates.INSTANCE.config.bazaarTweaks.bzSearchHistorySize; i++) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(SEARCH_OVERLAY_TEXTURE);
				Utils.drawTexturedRect(width / 2 - 100, topY - 1 + 160 + 4 + i * 10, 203, 10, 0, 203 / 512f, 164 / 256f, 174 / 256f, GL11.GL_NEAREST);
				if (i >= NotEnoughUpdates.INSTANCE.config.hidden.previousBazaarSearches.size()) continue;

				String s = NotEnoughUpdates.INSTANCE.config.hidden.previousBazaarSearches.get(i);
				Minecraft.getMinecraft().fontRendererObj.drawString(
					s,
					width / 2 - 95 + 1,
					topY + 45 + AUTOCOMPLETE_HEIGHT + i * 10 + 2,
					0xdddddd,
					true
				);
			}

			int size = NotEnoughUpdates.INSTANCE.config.bazaarTweaks.bzSearchHistorySize;
			Minecraft.getMinecraft().getTextureManager().bindTexture(SEARCH_OVERLAY_TEXTURE);
			Utils.drawTexturedRect(width / 2 - 100, topY - 1 + 160 + 4 + size * 10, 203, 4, 0, 203 / 512f, 215 / 256f, 219 / 256f, GL11.GL_NEAREST);

			if (tooltipToDisplay != null) {
				Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, width, height, -1);
			}
		}
	}

	private static final ExecutorService searchES = Executors.newSingleThreadExecutor();
	private static final AtomicInteger searchId = new AtomicInteger(0);

	private static String getItemIdAtIndex(int i) {
		if (!autocompletedItems.isEmpty()) {
			if ((i > autocompletedItems.size() - 1) || i < 0 || i > 4) {
				return "";
			}
			String searchString = autocompletedItems.toArray()[i].toString();
			JsonObject repoObject = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(searchString);
			if (repoObject != null) {
				ItemStack stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(repoObject);
				return Utils.cleanColour(stack.getDisplayName().replaceAll("\\[.+]", ""));
			}

		}
		return null;
	}

	public static void close() {
		isGuiOpen = false;
		if (tabCompleted) {
			tabCompletionIndex = -1;
			tabCompleted = false;
		}
		if (NotEnoughUpdates.INSTANCE.config.bazaarTweaks.keepPreviousSearch) {
			search();
		} else {
			synchronized (autocompletedItems) {
				autocompletedItems.clear();
			}
		}

		StringBuilder stringBuilder = new StringBuilder(searchString.trim());
		if (!searchStringExtra.isEmpty()) {
			stringBuilder.append(searchStringExtra);
		}

		String search = stringBuilder.toString();

		if (!searchString.trim().isEmpty()) {
			List<String> previousBazaarSearches = NotEnoughUpdates.INSTANCE.config.hidden.previousBazaarSearches;
			previousBazaarSearches.remove(searchString);
			previousBazaarSearches.remove(searchString);
			previousBazaarSearches.add(0, searchString);
			while (previousBazaarSearches.size() > NotEnoughUpdates.INSTANCE.config.bazaarTweaks.bzSearchHistorySize) {
				previousBazaarSearches.remove(previousBazaarSearches.size() - 1);
			}
		}

		if (!search.isEmpty()) NotEnoughUpdates.INSTANCE.sendChatMessage("/bz " + search);
		if (!NotEnoughUpdates.INSTANCE.config.bazaarTweaks.keepPreviousSearch) searchString = "";
	}

	private static boolean updateTabCompletedSearch(int key) {
		String id;
		if (key == Keyboard.KEY_DOWN || key == Keyboard.KEY_TAB) {
			id = getItemIdAtIndex(tabCompletionIndex + 1);
			if (id == null) {
				textField.setFocus(true);
				textField.setText(searchString);
				tabCompleted = false;
				tabCompletionIndex = -1;
				return true;
			} else if (id.equals("")) {
				tabCompletionIndex = 0;
				return true;
			} else {
				searchString = id;
				tabCompletionIndex += 1;
				return true;
			}
		} else if (key == Keyboard.KEY_UP) {
			id = getItemIdAtIndex(tabCompletionIndex - 1);
			if (id == null) {
				textField.setFocus(true);
				textField.setText(searchString);
				tabCompleted = false;
				tabCompletionIndex = -1;
				return true;
			} else if (id.equals("")) {
				if (autocompletedItems.size() > 4) tabCompletionIndex = 4;
				else tabCompletionIndex = autocompletedItems.size() - 1;
				tabCompletionIndex = autocompletedItems.size() - 1;
				return true;
			} else {
				searchString = id;
				tabCompletionIndex -= 1;
				return true;
			}
		}
		return false;
	}

	public static void search() {
		final int thisSearchId = searchId.incrementAndGet();

		searchES.submit(() -> {
			if (thisSearchId != searchId.get()) return;

			List<String> title = new ArrayList<>(NotEnoughUpdates.INSTANCE.manager.search("title:" + searchString.trim()));

			if (thisSearchId != searchId.get()) return;

			if (!searchString.trim().contains(" ")) {
				StringBuilder sb = new StringBuilder();
				for (char c : searchString.toCharArray()) {
					sb.append(c).append(" ");
				}
				title.addAll(NotEnoughUpdates.INSTANCE.manager.search("title:" + sb.toString().trim()));
			}

			if (thisSearchId != searchId.get()) return;

			List<String> desc = new ArrayList<>(NotEnoughUpdates.INSTANCE.manager.search("desc:" + searchString.trim()));
			desc.removeAll(title);

			if (thisSearchId != searchId.get()) return;

			Set<String> bazaarItems = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarKeySet();
			// Amalgamated Crimsonite (Old) // TODO remove from repo
			bazaarItems.remove("AMALGAMATED_CRIMSONITE");

				title.retainAll(bazaarItems);
				desc.retainAll(bazaarItems);

				title.sort(salesComparator);
				desc.sort(salesComparator);

			if (thisSearchId != searchId.get()) return;

			synchronized (autocompletedItems) {
				autocompletedItems.clear();
				autocompletedItems.addAll(title);
				autocompletedItems.addAll(desc);
			}
		});
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		boolean ignoreKey = false;

		if (keyCode == Keyboard.KEY_ESCAPE) {
			searchStringExtra = "";
			if (NotEnoughUpdates.INSTANCE.config.bazaarTweaks.escFullClose) {
				Minecraft.getMinecraft().displayGuiScreen(null);
			} else {
				close();
			}
			return;
		} else if (keyCode == Keyboard.KEY_RETURN) {
			searchStringExtra = "";
			close();
			return;
		} else if (keyCode == Keyboard.KEY_TAB) {
			//autocomplete to first item in the list
			if (!tabCompleted) {
				tabCompleted = true;
				ignoreKey = true;
				String id = getItemIdAtIndex(0);
				if (id == null) {
					tabCompleted = false;
					textField.setFocus(true);
					textField.setText(searchString);
				} else {
					tabCompletionIndex = 0;
					searchString = id;
				}
			}
		}

		if (Keyboard.getEventKeyState()) {
			if (tabCompleted) {
				if (!ignoreKey) {
					boolean success = updateTabCompletedSearch(keyCode);
					if (success) return;
					textField.setFocus(true);
					textField.setText(searchString);
					tabCompleted = false;
					tabCompletionIndex = -1;
				} else return;

			}
			textField.setFocus(true);
			textField.setText(searchString);
			textField.keyTyped(Keyboard.getEventCharacter(), keyCode);
			searchString = textField.getText();

			search();
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();
		int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

		int h = NotEnoughUpdates.INSTANCE.config.bazaarTweaks.showPastSearches ? 219 : 145;

		int topY = height / 4;
		if (scaledResolution.getScaleFactor() >= 4) {
			topY = height / 2 - h / 2 + 5;
		}

		if (!Mouse.getEventButtonState() && Mouse.getEventButton() == -1 && searchFieldClicked) {
			textField.mouseClickMove(mouseX - 2, topY + 10, 0, 0);
		}

		if (Mouse.getEventButton() != -1) {
			searchFieldClicked = false;
		}

		if (Mouse.getEventButtonState()) {
			if (mouseY > topY && mouseY < topY + 20) {
				if (mouseX > width / 2 - 100) {
					if (mouseX < width / 2 + 49) {
						searchFieldClicked = true;
						textField.mouseClicked(mouseX - 2, mouseY, Mouse.getEventButton());

						if (Mouse.getEventButton() == 1) {
							searchString = "";
							synchronized (autocompletedItems) {
								autocompletedItems.clear();
							}
						}
					} else if (mouseX < width / 2 + 75) {
						searchStringExtra = "";
						close();
					} else if (mouseX < width / 2 + 100) {
						searchString = "";
						searchStringExtra = "";
						close();
						NotEnoughUpdates.INSTANCE.openGui = SettingsCommand.INSTANCE.createConfigScreen("Bazaar Tweaks");
					}
				}
			} else if (Mouse.getEventButton() == 0) {
				int num = 0;
				synchronized (autocompletedItems) {
					for (String str : autocompletedItems) {
						JsonObject obj = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(str);
						if (obj != null) {
							ItemStack stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(obj);
							if (mouseX >= width / 2 - 96 && mouseX <= width / 2 + 96 && mouseY >= topY + 30 + num * 22 &&
								mouseY <= topY + 30 + num * 22 + 20) {
								searchString = Utils.cleanColour(stack.getDisplayName().replaceAll("\\[.+]", "")).trim();
								if (searchString.contains("Enchanted Book") && str.contains(";")) {
									String[] lore = NotEnoughUpdates.INSTANCE.manager.getLoreFromNBT(stack.getTagCompound());
									if (lore != null) {
										searchString = Utils.cleanColour(lore[0]);
									}
								}

								searchStringExtra = " ";

								close();
								return;
							}

							if (++num >= 5) break;
						}
					}
				}

				if (NotEnoughUpdates.INSTANCE.config.bazaarTweaks.showPastSearches) {
					for (int i = 0; i < NotEnoughUpdates.INSTANCE.config.bazaarTweaks.bzSearchHistorySize; i++) {
						if (i >= NotEnoughUpdates.INSTANCE.config.hidden.previousBazaarSearches.size()) break;

						String s = NotEnoughUpdates.INSTANCE.config.hidden.previousBazaarSearches.get(i);
						if (mouseX >= width / 2 - 95 && mouseX <= width / 2 + 95 &&
							mouseY >= topY + 45 + AUTOCOMPLETE_HEIGHT + i * 10 &&
							mouseY <= topY + 45 + AUTOCOMPLETE_HEIGHT + i * 10 + 10) {
							searchString = s;
							searchStringExtra = "";
							close();
							return;
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onSlotClick(SlotClickEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.bazaarTweaks.enableSearchOverlay) return;
		if (!Utils.getOpenChestName().startsWith("Bazaar ➜")) return;
		ItemStack stack = event.slot.getStack();
		if (event.slot.slotNumber == 45 && stack.hasDisplayName() && stack.getItem() == Items.sign && stack.getDisplayName().equals("§aSearch")) {
			event.setCanceled(true);
			NotEnoughUpdates.INSTANCE.openGui = new BazaarSearchOverlay();
		}
	}
}
