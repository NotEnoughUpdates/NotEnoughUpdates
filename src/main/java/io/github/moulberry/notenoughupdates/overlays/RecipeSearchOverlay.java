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
import io.github.moulberry.notenoughupdates.commands.help.SettingsCommand;
import io.github.moulberry.notenoughupdates.core.GuiElementTextField;
import io.github.moulberry.notenoughupdates.core.GuiScreenElementWrapper;
import io.github.moulberry.notenoughupdates.recipes.CraftingRecipe;
import io.github.moulberry.notenoughupdates.recipes.NeuRecipe;
import io.github.moulberry.notenoughupdates.util.StreamExtL;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RecipeSearchOverlay {
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

	public static boolean shouldReplace() {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return false;
		if (!NotEnoughUpdates.INSTANCE.config.recipeTweaks.enableSearchOverlay) return false;

		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiEditSign)) {
			if (!NotEnoughUpdates.INSTANCE.config.recipeTweaks.keepPreviousSearch) searchString = "";
			return false;
		}

		String lastContainer = Utils.getLastOpenChestName();
		return lastContainer.equals("Craft Item") || lastContainer.contains("Recipes (");
	}

	public static void render() {
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();
		int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

		Utils.drawGradientRect(0, 0, width, height, -1072689136, -804253680);

		int h = NotEnoughUpdates.INSTANCE.config.recipeTweaks.showPastSearches ? 219 : 145;

		int topY = height / 4;
		if (scaledResolution.getScaleFactor() >= 4) {
			topY = height / 2 - h / 2 + 5;
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(SEARCH_OVERLAY_TEXTURE);
		GlStateManager.color(1, 1, 1, 1);
		Utils.drawTexturedRect(width / 2 - 100, topY - 1, 203, h, 0, 203 / 512f, 0, h / 256f, GL11.GL_NEAREST);

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
					), width / 2 - 74, topY + 35 + num * 22 + 1, 0xdddddd, true);

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

		if (NotEnoughUpdates.INSTANCE.config.recipeTweaks.showPastSearches) {
			Minecraft.getMinecraft().fontRendererObj.drawString(
				"Past Searches:",
				width / 2 - 100,
				topY + 25 + AUTOCOMPLETE_HEIGHT + 5,
				0xdddddd,
				true
			);

			for (int i = 0; i < 5; i++) {
				if (i >= NotEnoughUpdates.INSTANCE.config.hidden.previousRecipeSearches.size()) break;

				String s = NotEnoughUpdates.INSTANCE.config.hidden.previousRecipeSearches.get(i);
				Minecraft.getMinecraft().fontRendererObj.drawString(
					s,
					width / 2 - 95 + 1,
					topY + 45 + AUTOCOMPLETE_HEIGHT + i * 10 + 2,
					0xdddddd,
					true
				);
			}

			if (tooltipToDisplay != null) {
				Utils.drawHoveringText(
					tooltipToDisplay,
					mouseX,
					mouseY,
					width,
					height,
					-1,
					Minecraft.getMinecraft().fontRendererObj
				);
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
		if (tabCompleted) {
			tabCompletionIndex = -1;
			tabCompleted = false;
		}
		if (NotEnoughUpdates.INSTANCE.config.recipeTweaks.keepPreviousSearch) {
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

		if (search.isEmpty()) return;

		Minecraft.getMinecraft().thePlayer.sendChatMessage("/recipe " + search);

		if (!searchString.trim().isEmpty()) {
			List<String> previousRecipeSearches = NotEnoughUpdates.INSTANCE.config.hidden.previousRecipeSearches;
			previousRecipeSearches.remove(searchString);
			previousRecipeSearches.remove(searchString);
			previousRecipeSearches.add(0, searchString);
			while (previousRecipeSearches.size() > 5) {
				previousRecipeSearches.remove(previousRecipeSearches.size() - 1);
			}
		}

		Minecraft.getMinecraft().displayGuiScreen(null);

		if (Minecraft.getMinecraft().currentScreen == null) {
			Minecraft.getMinecraft().setIngameFocus();
		}
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

			HashMap<String, Set<NeuRecipe>> items = NotEnoughUpdates.INSTANCE.manager.getAllRecipes();

			System.out.println(items.toString());

			List<String> keys = new ArrayList<>();

			for (Map.Entry<String, Set<NeuRecipe>> entry : items.entrySet()) {
				for (NeuRecipe recipe : entry.getValue()) {
					if(recipe.isAvailable()) keys.add(entry.getKey());
				}
			}
			System.out.println(keys);
			title.retainAll(keys);
			desc.retainAll(keys);

			if (thisSearchId != searchId.get()) return;

			synchronized (autocompletedItems) {
				autocompletedItems.clear();
				autocompletedItems.addAll(title);
				autocompletedItems.addAll(desc);
			}
		});
	}

	public static void keyEvent() {
		boolean ignoreKey = false;

		if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
			searchStringExtra = "";
			close();
			if (NotEnoughUpdates.INSTANCE.config.recipeTweaks.escFullClose) {
				Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(new C0DPacketCloseWindow(Minecraft.getMinecraft().thePlayer.openContainer.windowId));
			}
			return;
		} else if (Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
			searchStringExtra = "";
			close();
			return;
		} else if (Keyboard.getEventKey() == Keyboard.KEY_TAB) {
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
					boolean success = updateTabCompletedSearch(Keyboard.getEventKey());
					if (success) return;
					textField.setFocus(true);
					textField.setText(searchString);
					tabCompleted = false;
					tabCompletionIndex = -1;
				} else return;

			}
			textField.setFocus(true);
			textField.setText(searchString);
			textField.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
			searchString = textField.getText();

			search();
		}
	}

	public static void mouseEvent() {
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();
		int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

		int h = NotEnoughUpdates.INSTANCE.config.recipeTweaks.showPastSearches ? 219 : 145;

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
						searchStringExtra = "";
						close();
						Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(new C0DPacketCloseWindow(Minecraft.getMinecraft().thePlayer.openContainer.windowId));
						NotEnoughUpdates.INSTANCE.openGui = SettingsCommand.INSTANCE.createConfigScreen("Recipe Tweaks");
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

				if (NotEnoughUpdates.INSTANCE.config.recipeTweaks.showPastSearches) {
					for (int i = 0; i < 5; i++) {
						if (i >= NotEnoughUpdates.INSTANCE.config.hidden.previousRecipeSearches.size()) break;

						String s = NotEnoughUpdates.INSTANCE.config.hidden.previousRecipeSearches.get(i);
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
}
