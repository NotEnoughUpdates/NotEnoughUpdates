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

package io.github.moulberry.notenoughupdates.miscgui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpingInteger;
import io.github.moulberry.notenoughupdates.itemeditor.GuiElementTextField;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.moulberry.notenoughupdates.util.GuiTextures.help;

public class GuiEnchantColour extends GuiScreen {
	public static final ResourceLocation custom_ench_colour = new ResourceLocation(
		"notenoughupdates:custom_ench_colour.png");

	private static final String sharePrefix = "NEUEC/";

	private int guiLeft;
	private int guiTop;
	private final int xSize = 217;
	private int ySize = 0;
	private int ySizeSidebar = 0;
	private int guiTopSidebar;

	public static final Splitter splitter = Splitter.on(":").limit(5);

	private final HashMap<Integer, String> comparators = new HashMap<>();
	private final HashMap<Integer, String> modifiers = new HashMap<>();
	private final List<GuiElementTextField[]> guiElementTextFields = new ArrayList<>();

	private final LerpingInteger scroll = new LerpingInteger(0, 100);
	private final LerpingInteger scrollSideBar = new LerpingInteger(0, 100);

	public static int BOLD_MODIFIER = 0b1;
	public static int ITALIC_MODIFIER = 0b10;
	public static int OBFUSCATED_MODIFIER = 0b100;
	public static int UNDERLINE_MODIFIER = 0b1000;
	public static int STRIKETHROUGH_MODIFIER = 0b10000;
	private static final Pattern settingPattern = Pattern.compile(".*:[>=<]:[0-9]+:[a-zA-Z0-9]+(:[a-zA-Z0-9]+)?");

	private ItemStack maxedBook;
	private ItemStack maxedAttBook;
	private int maxedBookFound = 0;
	private int maxedAttBookFound = 0;

	private List<String> getEnchantColours() {
		return NotEnoughUpdates.INSTANCE.config.hidden.enchantColours;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();

		List<String> enchantColours = getEnchantColours();

		ySize = 53 + 25 * enchantColours.size();
		guiLeft = (width - xSize) / 2;

		if (ySize > height) {

			if (scroll.getTarget() > 0) {
				scroll.setTarget(0);
			} else if (scroll.getTarget() < height - ySize) {
				scroll.setTarget(height - ySize);
			}
			scroll.tick();
			guiTop = scroll.getValue();

		} else {
			guiTop = (height - ySize) / 2;
			scroll.setValue(0);
			scroll.resetTimer();
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(custom_ench_colour);
		Utils.drawTexturedRect(guiLeft, guiTop, xSize, 21, 0, 1, 0, 21 / 78f, GL11.GL_NEAREST);
		Utils.drawTexturedRect(guiLeft, guiTop + ySize - 32, xSize, 32, 0, 1, 46 / 78f, 1, GL11.GL_NEAREST);

		fontRendererObj.drawString("Ench Name", guiLeft + 10, guiTop + 7, 4210752);
		fontRendererObj.drawString("CMP", guiLeft + 86, guiTop + 7, 4210752);
		fontRendererObj.drawString("LVL", guiLeft + 111, guiTop + 7, 4210752);
		fontRendererObj.drawString("COL", guiLeft + 136, guiTop + 7, 4210752);
		fontRendererObj.drawString("DEL", guiLeft + 161, guiTop + 7, 4210752);

		Utils.drawStringCentered(
			"Add Ench Colour",
			guiLeft + xSize / 2 + 1,
			guiTop + ySize - 20,
			false,
			4210752
		);

		int yIndex = 0;
		for (String str : enchantColours) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(custom_ench_colour);
			GlStateManager.color(1, 1, 1, 1);
			Utils.drawTexturedRect(guiLeft, guiTop + 21 + yIndex * 25, xSize, 25, 0, 1, 21 / 78f, 46 / 78f, GL11.GL_NEAREST);

			List<String> colourOps = splitter.splitToList(str);
			String enchantName = getColourOpIndex(colourOps, 0);
			String comparator = getColourOpIndex(colourOps, 1);
			String comparison = getColourOpIndex(colourOps, 2);
			String colourCode = getColourOpIndex(colourOps, 3);
			String modifier = getColourOpIndex(colourOps, 4);
			modifiers.put(yIndex, modifier);

			if (colourCode.length() > 1) colourCode = String.valueOf(colourCode.toLowerCase().charAt(0));
			if (comparator.length() > 1) comparator = String.valueOf(comparator.toLowerCase().charAt(0));

			Utils.drawStringCentered(comparator, guiLeft + 96, guiTop + 33 + 25 * yIndex, false, 4210752);

			if (guiElementTextFields.size() <= yIndex) {
				guiElementTextFields.add(new GuiElementTextField[3]);
			}
			if (guiElementTextFields.get(yIndex)[0] == null) {
				guiElementTextFields.get(yIndex)[0] = new GuiElementTextField(enchantName, GuiElementTextField.SCALE_TEXT);
				guiElementTextFields.get(yIndex)[0].setSize(75, 20);
			}
			if (guiElementTextFields.get(yIndex)[1] == null) {
				guiElementTextFields.get(yIndex)[1] = new GuiElementTextField(
					comparison,
					GuiElementTextField.SCALE_TEXT | GuiElementTextField.NUM_ONLY | GuiElementTextField.NO_SPACE
				);
				guiElementTextFields.get(yIndex)[1].setSize(20, 20);
			}
			if (guiElementTextFields.get(yIndex)[2] == null) {
				guiElementTextFields.get(yIndex)[2] = new GuiElementTextField(colourCode, GuiElementTextField.SCALE_TEXT);
				guiElementTextFields.get(yIndex)[2].setSize(20, 20);
			}
			guiElementTextFields.get(yIndex)[0].setText(enchantName);
			guiElementTextFields.get(yIndex)[1].setText(comparison);
			comparators.put(yIndex, comparator);
			guiElementTextFields.get(yIndex)[2].setText(colourCode);

			guiElementTextFields.get(yIndex)[0].render(guiLeft + 7, guiTop + 23 + 25 * yIndex);
			guiElementTextFields.get(yIndex)[1].render(guiLeft + 110, guiTop + 23 + 25 * yIndex);
			guiElementTextFields.get(yIndex)[2].render(guiLeft + 135, guiTop + 23 + 25 * yIndex);

			int modifierI = getIntModifier(modifier);
			if ((modifierI & GuiEnchantColour.BOLD_MODIFIER) != 0) {
				Minecraft.getMinecraft().fontRendererObj.drawString(
					"\u00a7l\u2713",
					guiLeft + 181,
					guiTop + 23 + 25 * yIndex - 2,
					0xff202020,
					true
				);
			}
			if ((modifierI & GuiEnchantColour.ITALIC_MODIFIER) != 0) {
				Minecraft.getMinecraft().fontRendererObj.drawString(
					"\u00a7l\u2713",
					guiLeft + 181,
					guiTop + 23 + 25 * yIndex + 10,
					0xff202020,
					true
				);
			}
			if ((modifierI & GuiEnchantColour.UNDERLINE_MODIFIER) != 0) {
				Minecraft.getMinecraft().fontRendererObj.drawString(
					"\u00a7l\u2713",
					guiLeft + 196,
					guiTop + 23 + 25 * yIndex - 2,
					0xff202020,
					true
				);
			}
			if ((modifierI & GuiEnchantColour.STRIKETHROUGH_MODIFIER) != 0) {
				Minecraft.getMinecraft().fontRendererObj.drawString(
					"\u00a7l\u2713",
					guiLeft + 196,
					guiTop + 23 + 25 * yIndex + 10,
					0xff202020,
					true
				);
			}

			yIndex++;
		}
		renderSideBar(mouseX, mouseY, partialTicks);
		List<String> tooltipToDisplay = null;
		GlStateManager.color(1, 1, 1, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(help);
		Utils.drawTexturedRect(guiLeft + xSize + 3, guiTopSidebar - 18, 16, 16, GL11.GL_NEAREST);
		if (maxedBookFound == 0) {
			try {
				if (NotEnoughUpdates.INSTANCE.manager.jsonToStack(
					NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("MAXED_ENCHANT_BOOK")).hasDisplayName()) {
					maxedBook = NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
						.getItemInformation()
						.get("MAXED_ENCHANT_BOOK"));
					maxedBookFound = 1;
				} else {
					maxedBookFound = 2;
				}

			} catch (Exception ignored) {
				maxedBookFound = 2;
			}
		}
		if (maxedBookFound == 1) {
			Utils.drawItemStack(maxedBook, guiLeft + xSize + 3, guiTopSidebar - 34);
		}
		if (maxedAttBookFound == 0) {
			try {
				if (NotEnoughUpdates.INSTANCE.manager.jsonToStack(
					NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("MAXED_ATTRIBUTE_SHARD")).hasDisplayName()) {
					maxedAttBook = NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
						.getItemInformation()
						.get("MAXED_ATTRIBUTE_SHARD"));
					maxedAttBookFound = 1;
				} else {
					maxedAttBookFound = 2;
				}

			} catch (Exception ignored) {
				maxedAttBookFound = 2;
			}
		}
		if (maxedAttBookFound == 1) {
			Utils.drawItemStack(maxedAttBook, guiLeft + xSize + 3, guiTopSidebar - 52);
		}

		if (mouseX >= guiLeft + xSize + 3 && mouseX < guiLeft + xSize + 39) {
			boolean renderingTooltip = false;

			if (mouseY >= guiTopSidebar - 34 && mouseY <= guiTopSidebar - 18 && maxedBookFound == 1) {
				tooltipToDisplay = maxedBook.getTooltip(Minecraft.getMinecraft().thePlayer, false);
				Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, width, height, -1);
				tooltipToDisplay = null;
				renderingTooltip = true;
			}
			if (mouseY >= guiTopSidebar - 52 && mouseY <= guiTopSidebar - 34 && maxedAttBookFound == 1 && !renderingTooltip) {
				tooltipToDisplay = maxedAttBook.getTooltip(Minecraft.getMinecraft().thePlayer, false);
				Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, width, height, -1);
				tooltipToDisplay = null;
				renderingTooltip = true;
			}
			if (mouseY >= guiTopSidebar - 18 && mouseY <= guiTopSidebar - 2 && !renderingTooltip) {
				tooltipToDisplay = Lists.newArrayList(
					EnumChatFormatting.AQUA + "NEUEC Colouring Guide",
					EnumChatFormatting.GREEN + "",
					EnumChatFormatting.GREEN + "How to use the GUI",
					EnumChatFormatting.YELLOW + "Type the name of the enchant you want to colour in the left box",
					EnumChatFormatting.YELLOW + "Change the comparison: ",
					EnumChatFormatting.RED + "> " + EnumChatFormatting.YELLOW + "greater than a level ",
					EnumChatFormatting.RED + "= " + EnumChatFormatting.YELLOW + "equals the level ",
					EnumChatFormatting.RED + "< " + EnumChatFormatting.YELLOW + "less than a level",
					EnumChatFormatting.YELLOW + "Put the level of enchant you want to color in the middle box",
					EnumChatFormatting.YELLOW + "Put the color code you want to use here",
					EnumChatFormatting.GREEN + "",
					EnumChatFormatting.GREEN + "Available colour codes:",
					Utils.chromaString("z = Chroma"),
					EnumChatFormatting.DARK_BLUE + "1 = Dark Blue",
					EnumChatFormatting.DARK_GREEN + "2 = Dark Green",
					EnumChatFormatting.DARK_AQUA + "3 = Dark Aqua",
					EnumChatFormatting.DARK_RED + "4 = Dark Red",
					EnumChatFormatting.DARK_PURPLE + "5 = Dark Purple",
					EnumChatFormatting.GOLD + "6 = Gold",
					EnumChatFormatting.GRAY + "7 = Gray",
					EnumChatFormatting.DARK_GRAY + "8 = Dark Gray",
					EnumChatFormatting.BLUE + "9 = Blue",
					EnumChatFormatting.GREEN + "a = Green",
					EnumChatFormatting.AQUA + "b = Aqua",
					EnumChatFormatting.RED + "c = Red",
					EnumChatFormatting.LIGHT_PURPLE + "d = Purple",
					EnumChatFormatting.YELLOW + "e = Yellow",
					EnumChatFormatting.WHITE + "f = White",
					"\u00A7ZZ = SBA Chroma" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY + " (Requires SBA)",
					"",
					EnumChatFormatting.GREEN + "Available formatting:",
					EnumChatFormatting.GRAY + "" + EnumChatFormatting.BOLD + "B = " + EnumChatFormatting.BOLD + "Bold",
					EnumChatFormatting.GRAY + "" + EnumChatFormatting.STRIKETHROUGH + "S" + EnumChatFormatting.RESET +
						EnumChatFormatting.GRAY + " = " + EnumChatFormatting.STRIKETHROUGH + "Strikethrough",
					EnumChatFormatting.GRAY + "" + EnumChatFormatting.UNDERLINE + "U" + EnumChatFormatting.RESET +
						EnumChatFormatting.GRAY + " = " + EnumChatFormatting.UNDERLINE + "Underline",
					EnumChatFormatting.GRAY + "" + EnumChatFormatting.ITALIC + "I" + EnumChatFormatting.RESET +
						EnumChatFormatting.GRAY + " = " + EnumChatFormatting.ITALIC + "Italic"
				);
				Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, width, height, -1);
				tooltipToDisplay = null;
			}
		}

	}

	private void renderSideBar(int mouseX, int mouseY, float partialTicks) {
		ySizeSidebar = 24 * (2);

		if (ySizeSidebar > height) {

			if (scrollSideBar.getTarget() > 0) {
				scrollSideBar.setTarget(0);
			} else if (scrollSideBar.getTarget() < height - ySizeSidebar) {
				scrollSideBar.setTarget(height - ySizeSidebar);
			}

			scrollSideBar.tick();
			guiTopSidebar = scrollSideBar.getValue();

		} else {
			guiTopSidebar = (height - ySizeSidebar) / 2;
			scrollSideBar.setValue(0);
			scrollSideBar.resetTimer();
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(custom_ench_colour);
		GlStateManager.color(1, 1, 1, 1);
		Utils.drawTexturedRect(
			guiLeft + xSize + 3,
			guiTopSidebar + 2,
			88,
			20,
			64 / 217f,
			152 / 217f,
			48 / 78f,
			68 / 78f,
			GL11.GL_NEAREST
		);
		Utils.drawTexturedRect(
			guiLeft + xSize + 3,
			guiTopSidebar + 2 + 24,
			88,
			20,
			64 / 217f,
			152 / 217f,
			48 / 78f,
			68 / 78f,
			GL11.GL_NEAREST
		);
		Utils.drawTexturedRect(
			guiLeft + xSize + 3,
			guiTopSidebar + 2 + 24 * 2,
			88,
			20,
			64 / 217f,
			152 / 217f,
			48 / 78f,
			68 / 78f,
			GL11.GL_NEAREST
		);
		Utils.drawStringCenteredScaledMaxWidth(
			"Load preset",
			guiLeft + xSize + 4 + 44,
			guiTopSidebar + 8,
			false,
			86,
			4210752
		);
		Utils.drawStringCenteredScaledMaxWidth(
			"from Clipboard",
			guiLeft + xSize + 4 + 44,
			guiTopSidebar + 16,
			false,
			86,
			4210752
		);
		Utils.drawStringCenteredScaledMaxWidth(
			"Save preset",
			guiLeft + xSize + 4 + 44,
			guiTopSidebar + 8 + 24,
			false,
			86,
			4210752
		);
		Utils.drawStringCenteredScaledMaxWidth(
			"to Clipboard",
			guiLeft + xSize + 4 + 44,
			guiTopSidebar + 16 + 24,
			false,
			86,
			4210752
		);
		Utils.drawStringCenteredScaledMaxWidth(
			"Reset Config",
			guiLeft + xSize + 4 + 44,
			guiTopSidebar + 12 + 24 * 2,
			false,
			86,
			4210752
		);

		if (!validShareContents()) {
			Gui.drawRect(
				guiLeft + xSize + 3,
				guiTopSidebar + 2,
				guiLeft + xSize + 3 + 88,
				guiTopSidebar + 2 + 20,
				0x80000000
			);
		}
	}

	@Override
	public void onGuiClosed() {
		NotEnoughUpdates.INSTANCE.saveConfig();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		for (int yIndex = 0; yIndex < guiElementTextFields.size(); yIndex++) {
			for (int i = 0; i < 3; i++) {
				guiElementTextFields.get(yIndex)[i].keyTyped(typedChar, keyCode);
				if (guiElementTextFields.get(yIndex)[i].getFocus()) {
					int addOffset = 0;
					if (keyCode == Keyboard.KEY_UP) {
						addOffset -= 1;
					} else if (keyCode == Keyboard.KEY_DOWN) {
						addOffset += 1;
					}

					NotEnoughUpdates.INSTANCE.config.hidden.enchantColours.remove(yIndex);
					if (yIndex + addOffset < 0) {
						addOffset = -yIndex;
					} else if (yIndex + addOffset > NotEnoughUpdates.INSTANCE.config.hidden.enchantColours.size()) {
						addOffset = NotEnoughUpdates.INSTANCE.config.hidden.enchantColours.size() - yIndex;
					}
					NotEnoughUpdates.INSTANCE.config.hidden.enchantColours.add(
						yIndex + addOffset,
						getEnchantOpString(guiElementTextFields.get(yIndex), comparators.get(yIndex), modifiers.get(yIndex))
					);
					if (addOffset != 0) {
						GuiElementTextField[] guiElementTextFieldArray = guiElementTextFields.remove(yIndex);
						guiElementTextFields.add(yIndex + addOffset, guiElementTextFieldArray);
					}
					return;
				}
			}
		}
	}

	public String getEnchantOpString(GuiElementTextField[] tfs, String comparator, String modifiers) {
		StringBuilder enchantOp = new StringBuilder();
		enchantOp.append(tfs[0].getText());
		enchantOp.append(":");
		enchantOp.append(comparator);
		enchantOp.append(":");
		enchantOp.append(tfs[1].getText());
		enchantOp.append(":");
		enchantOp.append(tfs[2].getText());
		enchantOp.append(":");
		enchantOp.append(modifiers);
		return enchantOp.toString();
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();

		int dWheel = Mouse.getEventDWheel();
		int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
		if (mouseX > guiLeft && mouseX < guiLeft + xSize) {
			if (dWheel < 0) {
				scroll.setTarget(scroll.getTarget() - 50);
				scroll.resetTimer();
			} else if (dWheel > 0) {
				scroll.setTarget(scroll.getTarget() + 50);
				scroll.resetTimer();
			}
		} else if (mouseX > guiLeft + xSize && mouseX < guiLeft + xSize + 100) {
			if (dWheel < 0) {
				scrollSideBar.setTarget(scrollSideBar.getTarget() - 50);
				scrollSideBar.resetTimer();
			} else if (dWheel > 0) {
				scrollSideBar.setTarget(scrollSideBar.getTarget() + 50);
				scrollSideBar.resetTimer();
			}
		}

	}

	public static int getIntModifier(String modifier) {
		try {
			return Integer.parseInt(modifier);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private boolean validShareContents() {
		try {
			String base64 = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);

			if (base64.length() <= sharePrefix.length()) return false;

			base64 = base64.trim();

			try {
				return new String(Base64.getDecoder().decode(base64)).startsWith(sharePrefix);
			} catch (IllegalArgumentException e) {
				return false;
			}
		} catch (HeadlessException | IOException | UnsupportedFlavorException | IllegalStateException e) {
			return false;
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		for (int yIndex = 0; yIndex < guiElementTextFields.size(); yIndex++) {
			for (int i = 0; i < 3; i++) {
				int x = guiLeft + 7;
				if (i == 1) x += 103;
				else if (i == 2) x += 128;

				if (mouseX > x && mouseX < x + guiElementTextFields.get(yIndex)[i].getWidth()) {
					if (mouseY > guiTop + 23 + 25 * yIndex && mouseY < guiTop + 23 + 25 * yIndex + 20) {
						guiElementTextFields.get(yIndex)[i].mouseClicked(mouseX, mouseY, mouseButton);
						if (mouseButton == 1) {
							NotEnoughUpdates.INSTANCE.config.hidden.enchantColours.remove(yIndex);
							NotEnoughUpdates.INSTANCE.config.hidden.enchantColours.add(
								yIndex,
								getEnchantOpString(guiElementTextFields.get(yIndex), comparators.get(yIndex), modifiers.get(yIndex))
							);
						}
						continue;
					}
				}
				guiElementTextFields.get(yIndex)[i].otherComponentClick();
			}
			comparators.putIfAbsent(yIndex, ">");
			modifiers.putIfAbsent(yIndex, "0");
			if (mouseX >= guiLeft + 180 && mouseX <= guiLeft + 210 &&
				mouseY >= guiTop + 23 + 25 * yIndex && mouseY <= guiTop + 23 + 25 * yIndex + 20) {
				int modifierI = getIntModifier(modifiers.get(yIndex));
				int selectedModifier = -1;

				if (mouseX < guiLeft + 195) {
					if (mouseY < guiTop + 23 + 25 * yIndex + 10) {
						selectedModifier = BOLD_MODIFIER;
					} else {
						selectedModifier = ITALIC_MODIFIER;
					}
				} else {
					if (mouseY < guiTop + 23 + 25 * yIndex + 10) {
						selectedModifier = UNDERLINE_MODIFIER;
					} else {
						selectedModifier = STRIKETHROUGH_MODIFIER;
					}
				}

				if (selectedModifier != -1) {
					int modifierMasked = (modifierI & selectedModifier);
					int modifierMaskedInverted = selectedModifier - modifierMasked;

					int modifierInverted = (-1) - selectedModifier;

					int finalModifier = (modifierI & modifierInverted) | modifierMaskedInverted;

					modifiers.put(yIndex, "" + finalModifier);

					NotEnoughUpdates.INSTANCE.config.hidden.enchantColours.remove(yIndex);
					NotEnoughUpdates.INSTANCE.config.hidden.enchantColours.add(
						yIndex,
						getEnchantOpString(guiElementTextFields.get(yIndex), comparators.get(yIndex), modifiers.get(yIndex))
					);
				}
			}
			if (mouseY > guiTop + 23 + 25 * yIndex && mouseY < guiTop + 23 + 25 * yIndex + 20) {
				if (mouseX > guiLeft + 86 && mouseX < guiLeft + 86 + 20) {
					switch (comparators.get(yIndex)) {
						case ">":
							comparators.put(yIndex, "=");
							break;
						case "=":
							comparators.put(yIndex, "<");
							break;
						default:
							comparators.put(yIndex, ">");
							break;
					}
					NotEnoughUpdates.INSTANCE.config.hidden.enchantColours.remove(yIndex);
					NotEnoughUpdates.INSTANCE.config.hidden.enchantColours.add(
						yIndex,
						getEnchantOpString(guiElementTextFields.get(yIndex), comparators.get(yIndex), modifiers.get(yIndex))
					);
				} else if (mouseX > guiLeft + 160 && mouseX < guiLeft + 160 + 20) {
					if (NotEnoughUpdates.INSTANCE.config.hidden.enchantColours.size() > 0) {
						NotEnoughUpdates.INSTANCE.config.hidden.enchantColours.remove(yIndex);
						guiElementTextFields.remove(yIndex);
						comparators.remove(yIndex);
						modifiers.remove(yIndex);
					}
				}
			}
		}
		if (mouseX >= guiLeft + 57 && mouseX <= guiLeft + xSize - 57) {
			if (mouseY >= guiTop + ySize - 30 && mouseY <= guiTop + ySize - 10) {
				NotEnoughUpdates.INSTANCE.config.hidden.enchantColours.add("[a-zA-Z\\- ]+:>:5:9:0");
			}
		}
		if (mouseX > guiLeft + xSize + 3 && mouseX < guiLeft + xSize + 3 + 88) {
			if (mouseY > guiTopSidebar + 2 && mouseY < guiTopSidebar + 20 + 2) {

				String base64;

				try {
					base64 = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
				} catch (HeadlessException | IOException | UnsupportedFlavorException e) {
					return;
				}

				if (base64.length() <= sharePrefix.length()) return;

				base64 = base64.trim();

				String jsonString;
				try {
					jsonString = new String(Base64.getDecoder().decode(base64));
					if (!jsonString.startsWith(sharePrefix)) return;
					jsonString = jsonString.substring(sharePrefix.length());
				} catch (IllegalArgumentException e) {
					return;
				}

				JsonArray presetArray;
				try {
					presetArray = new JsonParser().parse(jsonString).getAsJsonArray();
				} catch (IllegalStateException | JsonParseException e) {
					return;
				}
				ArrayList<String> presetList = new ArrayList<>();

				for (int i = 0; i < presetArray.size(); i++) {
					if (presetArray.get(i).isJsonPrimitive()) {
						String test = presetArray.get(i).getAsString();
						Matcher matcher = settingPattern.matcher(test);
						if (matcher.matches()) {
							presetList.add(presetArray.get(i).getAsString());
						}
					}
				}
				if (presetList.size() != 0) {
					NotEnoughUpdates.INSTANCE.config.hidden.enchantColours = presetList;
				}

			} else if (mouseY > guiTopSidebar + 2 + 24 && mouseY < guiTopSidebar + 20 + 24 + 2) {

				ArrayList<String> result = NotEnoughUpdates.INSTANCE.config.hidden.enchantColours;
				JsonArray jsonArray = new JsonArray();

				for (String s : result) {
					jsonArray.add(new JsonPrimitive(s));
				}
				String base64String = Base64.getEncoder().encodeToString((sharePrefix +
					jsonArray).getBytes(StandardCharsets.UTF_8));
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(base64String), null);
			} else if (mouseY > guiTopSidebar + 2 + (24 * 2) && mouseY < guiTopSidebar + 20 + 2 + 24 * 2) {
				NotEnoughUpdates.INSTANCE.config.hidden.enchantColours = NEUConfig.createDefaultEnchantColours();
			}
		}
	}

	public static String getColourOpIndex(List<String> colourOps, int index) {
		if (colourOps.size() > index) {
			return colourOps.get(index);
		} else {
			switch (index) {
				case 0:
					return "[a-zA-Z\\- ]+";
				case 1:
					return ">";
				case 2:
					return "5";
				case 3:
					return "9";
				case 4:
					return "0";
			}
		}
		return null;
	}
}
