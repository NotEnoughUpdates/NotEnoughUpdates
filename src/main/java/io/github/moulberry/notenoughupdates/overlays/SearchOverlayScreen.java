/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
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
import io.github.moulberry.notenoughupdates.commands.help.SettingsCommand;
import io.github.moulberry.notenoughupdates.core.GuiElementTextField;
import io.github.moulberry.notenoughupdates.recipes.CraftingRecipe;
import io.github.moulberry.notenoughupdates.recipes.NeuRecipe;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchOverlayScreen extends GuiEditSign {

	static final ResourceLocation SEARCH_OVERLAY_TEXTURE = new ResourceLocation(
		"notenoughupdates:auc_search/ah_search_overlay.png");
	static final ResourceLocation SEARCH_OVERLAY_TEXTURE_TAB_COMPLETED = new ResourceLocation(
		"notenoughupdates:auc_search/ah_search_overlay_tab_completed.png");
	static final ResourceLocation STAR = new ResourceLocation("notenoughupdates:auc_search/star.png");
	static final ResourceLocation MASTER_STAR =
		new ResourceLocation("notenoughupdates:auc_search/master_star.png");
	static final ResourceLocation STAR_BOARD = new ResourceLocation("notenoughupdates:auc_search/star_board.png");

	static final GuiElementTextField textField = new GuiElementTextField("", 200, 20, 0);
	static boolean searchFieldClicked = false;
	static String searchString = "";
	static String searchStringExtra = "";
	static final Splitter SPACE_SPLITTER = Splitter.on(" ").omitEmptyStrings().trimResults();
	static boolean tabCompleted = false;
	static int tabCompletionIndex = -1;
	TileEntitySign tileSign;
	private static final Pattern ENCHANTED_BOOK_PATTERN = Pattern.compile("(.*)( [IVX]+)");

	static int selectedStars = 0;
	static boolean atLeast = true;
	static boolean onlyLevel100 = false;

	final int AUTOCOMPLETE_HEIGHT = 118;
	GuiType guiType;

	static final Set<String> autocompletedItems = new LinkedHashSet<>();

	public SearchOverlayScreen(TileEntitySign tileEntitySign) {
		super(tileEntitySign);
	}

	static final ExecutorService searchES = Executors.newSingleThreadExecutor();
	static final AtomicInteger searchId = new AtomicInteger(0);

	static String getItemIdAtIndex(int i) {
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

	static boolean updateTabCompletedSearch(int key) {
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

	@Override
	public void initGui() {

	}

	@Override
	public void onGuiClosed() {
		if (this.tileSign == null) return;
		if (this.tileSign.signText[0].getUnformattedText().isEmpty()) return;
		NetHandlerPlayClient netHandlerPlayClient = this.mc.getNetHandler();
		if (netHandlerPlayClient != null) {
			netHandlerPlayClient.addToSendQueue(new C12PacketUpdateSign(this.tileSign.getPos(), this.tileSign.signText));
		}

		this.tileSign.setEditable(true);
	}

	public void close(TileEntitySign tes) {
		if (tabCompleted) {
			tabCompletionIndex = -1;
			tabCompleted = false;
		}
		if (keepPreviousSearch()) {
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
		if (currentGuiType() == GuiType.AUCTION_HOUSE && onlyLevel100) {
			stringBuilder.insert(0, "[Lvl 100] ");
		}

		String search = stringBuilder.toString();

		if (!searchString.trim().isEmpty()) {
			List<String> previousSearches = previousSearches();
			previousSearches.remove(searchString);
			previousSearches.remove(searchString);
			previousSearches.add(0, searchString);
			while (previousSearches.size() > searchHistorySize()) {
				previousSearches.remove(previousSearches.size() - 1);
			}
		}

		if (tes != null) {
			if (search.length() <= 15) {
				tes.signText[0] = new ChatComponentText(search.substring(0, Math.min(search.length(), 15)));
			} else {
				ListIterator<String> words = SPACE_SPLITTER.splitToList(search).listIterator();
				StringBuilder line0 = new StringBuilder();

				while (words.hasNext()) {
					String word = words.next();
					if (line0.length() + word.length() > 15) {
						words.previous();
						break;
					}
					line0.append(word).append(' ');
				}
				StringBuilder line1 = new StringBuilder();
				while (words.hasNext()) {
					String word = words.next();
					if (line1.length() + word.length() > 15) {
						break;
					}
					line1.append(word).append(' ');
				}

				tes.signText[0] = new ChatComponentText(line0.toString().trim());
				tes.signText[1] = new ChatComponentText(line1.toString().trim());
			}
		} else {
			if (!search.isEmpty()) {
				if (currentGuiType() == GuiType.AUCTION_HOUSE) NotEnoughUpdates.INSTANCE.sendChatMessage("/ahs " + search);
				else if (currentGuiType() == GuiType.BAZAAR) NotEnoughUpdates.INSTANCE.sendChatMessage("/bz " + search);
				else if (currentGuiType() == GuiType.RECIPE) NotEnoughUpdates.INSTANCE.sendChatMessage("/recipe " + search);
			}
		}
		if (!keepPreviousSearch()) searchString = "";
		Minecraft.getMinecraft().displayGuiScreen(null);
	}

	public void search() {
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

			if (currentGuiType() == GuiType.AUCTION_HOUSE) {
				Set<String> auctionableItems = NotEnoughUpdates.INSTANCE.manager.auctionManager.getLowestBinKeySet();
				auctionableItems.addAll(NotEnoughUpdates.INSTANCE.manager.auctionManager.getItemAuctionInfoKeySet());

				if (!auctionableItems.isEmpty()) {
					title.retainAll(auctionableItems);
					desc.retainAll(auctionableItems);

					title.sort(getSearchComparator());
					desc.sort(getSearchComparator());
				} else {
					Set<String> bazaarItems = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarKeySet();

					title.removeAll(bazaarItems);
					desc.removeAll(bazaarItems);
				}
			} else if (currentGuiType() == GuiType.BAZAAR) {
				Set<String> bazaarItems = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarKeySet();

				title.retainAll(bazaarItems);
				desc.retainAll(bazaarItems);

				title.sort(getSearchComparator());
				desc.sort(getSearchComparator());
			} else if (currentGuiType() == GuiType.RECIPE) {
				HashMap<String, Set<NeuRecipe>> items = NotEnoughUpdates.INSTANCE.manager.getAllRecipes();

				List<String> keys = new ArrayList<>();

				for (Map.Entry<String, Set<NeuRecipe>> entry : items.entrySet()) {
					for (NeuRecipe recipe : entry.getValue()) {
						if (recipe instanceof CraftingRecipe && recipe.isAvailable()) keys.add(entry.getKey());
					}
				}
				title.retainAll(keys);
				desc.retainAll(keys);
			}

			if (thisSearchId != searchId.get()) return;

			synchronized (autocompletedItems) {
				autocompletedItems.clear();
				autocompletedItems.addAll(title);
				autocompletedItems.addAll(desc);
			}
		});
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawDefaultBackground();

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();

		int h = showPastSearches() ? 219 : 145;

		int topY = height / 4;
		if (scaledResolution.getScaleFactor() >= 4) {
			topY = height / 2 - h / 2 + 5;
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(SEARCH_OVERLAY_TEXTURE);
		GlStateManager.color(1, 1, 1, 1);
		Utils.drawTexturedRect(width / 2 - 100, topY - 1, 203, 145, 0, 203 / 512f, 0, 145 / 256f, GL11.GL_NEAREST);

		if (currentGuiType() == GuiType.AUCTION_HOUSE) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(STAR_BOARD);
			Utils.drawTexturedRect(width / 2 + 105, topY + 27, 105, 13, GL11.GL_NEAREST);

			Minecraft.getMinecraft().getTextureManager().bindTexture(STAR);
			GlStateManager.color(1, 1, 1, 1);
			int stars = atLeast && selectedStars > 0 ? 10 : selectedStars;
			for (int i = 0; i < stars; i++) {
				if (i >= 5) {
					Minecraft.getMinecraft().getTextureManager().bindTexture(MASTER_STAR);
					GlStateManager.color(1, 1, 1, 1);
				}
				if (i >= selectedStars) {
					GlStateManager.color(1, 1, 1, 0.3f);
				}
				Utils.drawTexturedRect(width / 2 + 108 + 10 * i, topY + 29, 9, 10, GL11.GL_NEAREST);
			}

			if (selectedStars < 6) {
				Gui.drawRect(width / 2 + 106, topY + 42, width / 2 + 115, topY + 51, 0xffffffff);
				Gui.drawRect(width / 2 + 107, topY + 43, width / 2 + 114, topY + 50, 0xff000000);
				Minecraft.getMinecraft().fontRendererObj.drawString("At Least?", width / 2 + 117, topY + 43, 0xffffff);

				if (atLeast) {
					Gui.drawRect(width / 2 + 108, topY + 44, width / 2 + 113, topY + 49, 0xffffffff);
				}
			}

			Gui.drawRect(width / 2 + 106, topY + 53, width / 2 + 115, topY + 62, 0xffffffff);
			Gui.drawRect(width / 2 + 107, topY + 54, width / 2 + 114, topY + 61, 0xff000000);
			if (onlyLevel100) {
				Gui.drawRect(width / 2 + 108, topY + 55, width / 2 + 113, topY + 60, 0xffffffff);
			}
			Minecraft.getMinecraft().fontRendererObj.drawString("Level 100 pets only?", width / 2 + 117, topY + 54, 0xffffff);
		}

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
					ItemStack stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(obj, false, true);
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

		if (showPastSearches()) {
			Minecraft.getMinecraft().fontRendererObj.drawString(
				"Past Searches:",
				width / 2 - 100,
				topY + 25 + AUTOCOMPLETE_HEIGHT + 5,
				0xdddddd,
				true
			);
			Minecraft.getMinecraft().getTextureManager().bindTexture(SEARCH_OVERLAY_TEXTURE);
			Utils.drawTexturedRect(width / 2 - 100, topY - 1 + 160, 203, 4, 0, 203 / 512f, 160 / 256f, 163 / 256f, GL11.GL_NEAREST);

			for (int i = 0; i < searchHistorySize(); i++) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(SEARCH_OVERLAY_TEXTURE);
				Utils.drawTexturedRect(width / 2 - 100, topY - 1 + 160 + 4 + i * 10, 203, 10, 0, 203 / 512f, 164 / 256f, 174 / 256f, GL11.GL_NEAREST);
				if (i >= previousSearches().size()) continue;

				String s = previousSearches().get(i);
				Minecraft.getMinecraft().fontRendererObj.drawString(
					s,
					width / 2 - 95 + 1,
					topY + 45 + AUTOCOMPLETE_HEIGHT + i * 10 + 2,
					0xdddddd,
					true
				);
			}

			int size = searchHistorySize();
			Minecraft.getMinecraft().getTextureManager().bindTexture(SEARCH_OVERLAY_TEXTURE);
			Utils.drawTexturedRect(width / 2 - 100, topY - 1 + 160 + 4 + size * 10, 203, 4, 0, 203 / 512f, 215 / 256f, 219 / 256f, GL11.GL_NEAREST);


			if (tooltipToDisplay != null) {
				Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, width, height, -1);
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		boolean ignoreKey = false;

		if (keyCode == Keyboard.KEY_ESCAPE) {
			searchStringExtra = "";
			if (escFullClose()) {
				Minecraft.getMinecraft().displayGuiScreen(null);
			} else {
				close(this.tileSign);
			}
			return;
		} else if (keyCode == Keyboard.KEY_RETURN) {
			searchStringExtra = "";
			close(this.tileSign);
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

		int h = showPastSearches() ? 219 : 145;

		int topY = height / 4;
		if (scaledResolution.getScaleFactor() >= 4) {
			topY = height / 2 - h / 2 + 5;
		}

		if (currentGuiType() == GuiType.AUCTION_HOUSE) {

			if (Mouse.getEventButtonState() && mouseX > width / 2 + 105 && mouseX < width / 2 + 105 + 105 &&
				mouseY > topY + 27 && mouseY < topY + 40) {
				int starClicked = 10;
				for (int i = 1; i <= 10; i++) {
					if (mouseX < width / 2 + 108 + 10 * i) {
						starClicked = i;
						break;
					}
				}
				if (selectedStars == starClicked) {
					selectedStars = 0;
				} else {
					selectedStars = starClicked;
				}
				return;
			}

			if (Mouse.getEventButtonState() && mouseX >= width / 2 + 106 && mouseX <= width / 2 + 116 &&
				mouseY >= topY + 42 && mouseY <= topY + 50) {
				atLeast = !atLeast;
				return;
			}

			if (Mouse.getEventButtonState() && mouseX >= width / 2 + 106 && mouseX <= width / 2 + 116 &&
				mouseY >= topY + 53 && mouseY <= topY + 62) {
				onlyLevel100 = !onlyLevel100;
				return;
			}
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
						close(this.tileSign);
					} else if (mouseX < width / 2 + 100) {
						searchString = "";
						searchStringExtra = "";
						close(this.tileSign);
						if (currentGuiType() == GuiType.AUCTION_HOUSE) NotEnoughUpdates.INSTANCE.openGui = SettingsCommand.INSTANCE.createConfigScreen("AH Tweaks");
						else if (currentGuiType() == GuiType.BAZAAR) NotEnoughUpdates.INSTANCE.openGui = SettingsCommand.INSTANCE.createConfigScreen("Bazaar Tweaks");
						else if (currentGuiType() == GuiType.RECIPE) NotEnoughUpdates.INSTANCE.openGui = SettingsCommand.INSTANCE.createConfigScreen("Recipe Tweaks");
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
										if (currentGuiType() == GuiType.AUCTION_HOUSE) {
											String[] split = Utils.cleanColour(lore[0]).trim().split(" ");
											split[split.length - 1] = "";
											searchString = StringUtils.join(split, " ").trim();
										} else if (currentGuiType() == GuiType.BAZAAR) {
											searchString = Utils.cleanColour(lore[0]);
										} else if (currentGuiType() == GuiType.RECIPE) {
											String bookName = Utils.cleanColour(lore[0]);
											Matcher matcher = ENCHANTED_BOOK_PATTERN.matcher(bookName);
											if (matcher.matches()) {
												searchString = matcher.group(1);
											} else {
												searchString = bookName;
											}
										}
									}
								}

								searchStringExtra = " ";
								if (currentGuiType() == GuiType.AUCTION_HOUSE) {
									JsonObject essenceCosts = Constants.ESSENCECOSTS;
									if (essenceCosts != null && essenceCosts.has(str) && selectedStars > 0) {
										for (int i = 0; i < selectedStars; i++) {
											if (i > 4) break;
											searchStringExtra += "\u272A";
										}
										switch (selectedStars) {
											case 6:
												searchStringExtra += "\u278A";
												break;
											case 7:
												searchStringExtra += "\u278B";
												break;
											case 8:
												searchStringExtra += "\u278C";
												break;
											case 9:
												searchStringExtra += "\u278D";
												break;
											case 10:
												searchStringExtra += "\u278E";
												break;
										}
										if (selectedStars < 6 && !atLeast) {
											searchStringExtra += " ";
											searchStringExtra += stack.getItem().getItemStackDisplayName(stack).substring(0, 1);
										}
									}
								}

								close(this.tileSign);
								return;
							}

							if (++num >= 5) break;
						}
					}
				}

				if (showPastSearches()) {
					for (int i = 0; i < searchHistorySize(); i++) {
						if (i >= previousSearches().size()) break;

						String s = previousSearches().get(i);
						if (mouseX >= width / 2 - 95 && mouseX <= width / 2 + 95 &&
							mouseY >= topY + 45 + AUTOCOMPLETE_HEIGHT + i * 10 &&
							mouseY <= topY + 45 + AUTOCOMPLETE_HEIGHT + i * 10 + 10) {
							searchString = s;
							searchStringExtra = "";
							close(this.tileSign);
							return;
						}
					}
				}
			}
		}
	}

	public Comparator<String> getSearchComparator() {
		return new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return 0;
			}
		};
	}

	public boolean enableSearchOverlay() {
		return false;
	}

	public ArrayList<String> previousSearches() {
		return new ArrayList<>();
	}

	public int searchHistorySize() {
		return 0;
	}

	public boolean showPastSearches() {
		return false;
	}

	public boolean escFullClose() {
		return false;
	}

	public boolean keepPreviousSearch() {
		return false;
	}

	public boolean disableClientSideGUI() {
		return Loader.isModLoaded("skyblockcatia") || NotEnoughUpdates.INSTANCE.config.hidden.disableClientSideSearch;
	}

	public GuiType currentGuiType() {
		return GuiType.UNKNOWN;
	}

	public enum GuiType {
		AUCTION_HOUSE,
		BAZAAR,
		RECIPE,
		UNKNOWN,
	}

}
