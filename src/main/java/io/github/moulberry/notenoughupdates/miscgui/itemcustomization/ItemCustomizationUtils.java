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

package io.github.moulberry.notenoughupdates.miscgui.itemcustomization;

import com.google.common.collect.Lists;
import io.github.moulberry.notenoughupdates.core.ChromaColour;
import io.github.moulberry.notenoughupdates.core.GuiElementTextField;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static io.github.moulberry.notenoughupdates.miscgui.GuiEnchantColour.custom_ench_colour;

public class ItemCustomizationUtils {

	public static List<String> customizeColourGuide = Lists.newArrayList(
		EnumChatFormatting.AQUA + "Set a custom name for the item",
		EnumChatFormatting.GREEN + "",
		EnumChatFormatting.GREEN + "Type \"&&\" for ¶",
		EnumChatFormatting.GREEN + "Type \"**\" for ✪",
		EnumChatFormatting.GREEN + "Type \"*1-9\" for ➊-➒",
		EnumChatFormatting.GREEN + "",
		EnumChatFormatting.GREEN + "Available colour codes:",
		Utils.chromaString("¶z = Chroma"),
		EnumChatFormatting.DARK_BLUE + "¶1 = Dark Blue",
		EnumChatFormatting.DARK_GREEN + "¶2 = Dark Green",
		EnumChatFormatting.DARK_AQUA + "¶3 = Dark Aqua",
		EnumChatFormatting.DARK_RED + "¶4 = Dark Red",
		EnumChatFormatting.DARK_PURPLE + "¶5 = Dark Purple",
		EnumChatFormatting.GOLD + "¶6 = Gold",
		EnumChatFormatting.GRAY + "¶7 = Gray",
		EnumChatFormatting.DARK_GRAY + "¶8 = Dark Gray",
		EnumChatFormatting.BLUE + "¶9 = Blue",
		EnumChatFormatting.GREEN + "¶a = Green",
		EnumChatFormatting.AQUA + "¶b = Aqua",
		EnumChatFormatting.RED + "¶c = Red",
		EnumChatFormatting.LIGHT_PURPLE + "¶d = Purple",
		EnumChatFormatting.YELLOW + "¶e = Yellow",
		EnumChatFormatting.WHITE + "¶f = White",
		"§Z¶Z = SBA Chroma" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY + " (Requires SBA)",
		"",
		EnumChatFormatting.GREEN + "Available formatting codes:",
		EnumChatFormatting.GRAY + "¶k = " + EnumChatFormatting.OBFUSCATED + "Obfuscated",
		EnumChatFormatting.GRAY + "¶l = " + EnumChatFormatting.BOLD + "Bold",
		EnumChatFormatting.GRAY + "¶m = " + EnumChatFormatting.STRIKETHROUGH + "Strikethrough",
		EnumChatFormatting.GRAY + "¶n = " + EnumChatFormatting.UNDERLINE + "Underline",
		EnumChatFormatting.GRAY + "¶o = " + EnumChatFormatting.ITALIC + "Italic"
	);

	public static List<String> resetGuide = Lists.newArrayList(
		EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + "This will reset all customisations!!",
		EnumChatFormatting.GREEN + "",
		EnumChatFormatting.RED + "Only click if you are sure you want to reset everything for this item"
	);

	public static ItemStack copy(ItemStack stack, GuiItemCustomize instance) {
		ItemStack customStack = stack.copy();
		if (!instance.textFieldCustomItem.getText().isEmpty()) {
			customStack.setItem(ItemCustomizeManager.getCustomItem(stack, instance.textFieldCustomItem.getText().trim()));
			customStack.setItemDamage(ItemCustomizeManager.getCustomItemDamage(stack));
			NBTTagCompound tagCompound = customStack.getTagCompound();
			if (tagCompound != null) {
				NBTTagCompound customSkull = ItemCustomizeManager.getCustomSkull(customStack);
				if (customSkull != null) {
					tagCompound.removeTag("SkullOwner");
					tagCompound.setTag("SkullOwner", customSkull);
				}
			}
		}
		return customStack;
	}

	public static int getGlintColour(GuiItemCustomize instance) {
		int col = instance.customGlintColour == null
			? ChromaColour.specialToChromaRGB(ItemCustomizeManager.DEFAULT_GLINT_COLOR)
			: ChromaColour.specialToChromaRGB(instance.customGlintColour);
		return 0xff000000 | col;
	}

	public static int getLeatherColour(GuiItemCustomize instance) {
		if (!instance.supportCustomLeatherColour) return 0xff000000;

		String customLeatherColour = instance.customLeatherColour;
		int col = customLeatherColour == null
			? ((ItemArmor) instance.customItemStack.getItem()).getColor(instance.customItemStack)
			: ChromaColour.specialToChromaRGB(customLeatherColour);
		return 0xff000000 | col;
	}

	public static int getLeatherColour(String colourString) {
		return 0xff000000 | ChromaColour.specialToChromaRGB(colourString);
	}

	public static String getChromaStrFromLeatherColour(GuiItemCustomize instance) {
		ItemStack customItemStack = instance.customItemStack;
		return ChromaColour.special(0, 0xff, ((ItemArmor) customItemStack.getItem()).getColor(customItemStack));
	}

	public static void renderFooter(int xCenter, int yTop, GuiType guiType) {
		int xCentreLeft = xCenter - 90;
		int xCentreRight = xCenter;

		Gui.drawRect(xCentreLeft, yTop, xCenter + 1, yTop + 17, 0xff101016);
		Gui.drawRect(xCentreLeft, yTop, xCenter - 1, yTop + 15, 0xff101016);
		Gui.drawRect(xCentreLeft - 1, yTop + 1, xCenter - 2, yTop + 14, 0xff000000 | 0xff00ffc4);

		Utils.renderShadowedString(getButtons(guiType, 0).getDisplay(),
			xCentreLeft + 44,
			yTop + 4,
			xCenter * 2 - xCentreRight
		);

		xCentreLeft += 90;
		xCentreRight += 90;

		Gui.drawRect(xCentreLeft, yTop, xCentreRight, yTop + 17, 0x70000000);
		Gui.drawRect(xCentreLeft, yTop, xCentreRight, yTop + 15, 0xff101016);
		Gui.drawRect(xCentreLeft - 1, yTop + 1, xCentreRight, yTop + 14, 0xff000000 | 0xff00ffc4 * 2);

		Utils.renderShadowedString(getButtons(guiType, 1).getDisplay(),
			xCentreLeft + 45,
			yTop + 4,
			xCenter * 2 - xCentreRight
		);
	}

	public static GuiType getButtons(GuiType guiType, int button) {
		if (button == 0) {
			if (guiType == GuiType.DEFAULT) {
				return GuiType.ANIMATED;
			} else {
				return GuiType.DEFAULT;
			}
		}
		if (button == 1) {
			if (guiType == GuiType.HYPIXEL) {
				return GuiType.ANIMATED;
			} else {
				return GuiType.HYPIXEL;
			}
		}

		return GuiType.DEFAULT;
	}

	public static GuiType getButtonClicked(int mouseX, int mouseY, GuiType guiType, float offset) {
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int xCenter = scaledResolution.getScaledWidth() / 2;
		int xCentreLeft = xCenter - 90;
		int xCentreRight = xCenter;

		for (int i = 0; i < 2; i++) {
			if (mouseX >= xCentreLeft && mouseX <= xCentreRight &&
				mouseY >= offset - 7 && mouseY <= offset + 12) {
				return getButtons(guiType, i);
			}
			xCentreLeft += 90;
			xCentreRight += 90;
		}

		return null;
	}

	public static int getAnimatedDyeColour(String[] dyeColours, int ticks) {
		return ChromaColour.specialToChromaRGB(
			dyeColours[(Minecraft.getMinecraft().thePlayer.ticksExisted / ticks) % dyeColours.length]);
	}

	static final ResourceLocation RESET = new ResourceLocation("notenoughupdates:itemcustomize/reset.png");

	public static void renderColourBlob(int xCenter, int yTop, int colour, String text, boolean renderReset) {
		Gui.drawRect(xCenter - 90, yTop, xCenter + 92, yTop + 17, 0x70000000);
		Gui.drawRect(xCenter - 90, yTop, xCenter + 90, yTop + 15, 0xff101016);
		Gui.drawRect(xCenter - 89, yTop + 1, xCenter + 89, yTop + 14, 0xff000000 | colour);

		Utils.renderShadowedString(text, xCenter, yTop + 4, 180);

		if (renderReset) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(RESET);
			GlStateManager.color(1, 1, 1, 1);
			RenderUtils.drawTexturedRect(xCenter + 90 - 12, yTop + 2, 10, 11, GL11.GL_NEAREST);
		}
	}

	public static void renderTextBox(
		GuiElementTextField textField, String text, int xOffset, int yOffset, int maxTextSize
	) {
		if (!textField.getFocus() && textField.getText().isEmpty()) {
			textField.setOptions(GuiElementTextField.SCISSOR_TEXT);
			textField.setPrependText(text);
		} else {
			textField.setOptions(GuiElementTextField.COLOUR | GuiElementTextField.SCISSOR_TEXT);
			textField.setPrependText("");
		}

		if (!textField.getFocus()) {
			textField.setSize(maxTextSize, 20);
		} else {
			int textSize = Minecraft.getMinecraft().fontRendererObj.getStringWidth(textField.getTextDisplay()) + 10;
			textField.setSize(Math.max(textSize, maxTextSize), 20);
		}

		textField.render(xOffset, yOffset);
	}

	public static Color getColourFromHex(String hex) {
		Color color = null;
		try {
			int decode = Integer.decode(hex);
			color = new Color(decode);
		} catch (NumberFormatException | NullPointerException e) {
		}
		return color;
	}

	public static int rgbToInt(Color color) {
		return (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
	}

	public static void renderPresetButtons(int x, int y, boolean valid, boolean secondValid, String preset) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(custom_ench_colour);
		GlStateManager.color(1, 1, 1, 1);
		Utils.drawTexturedRect(
			x - 88 + 198,
			y + 2,
			88,
			20,
			64 / 217f,
			152 / 217f,
			48 / 78f,
			68 / 78f,
			GL11.GL_NEAREST
		);
		Utils.drawTexturedRect(
			x - 88 + 198,
			y + 2 + 24,
			88,
			20,
			64 / 217f,
			152 / 217f,
			48 / 78f,
			68 / 78f,
			GL11.GL_NEAREST
		);

		Utils.drawStringCenteredScaledMaxWidth("Load " + preset, x - 44 + 198, y + 8, false, 86, 4210752);
		Utils.drawStringCenteredScaledMaxWidth("from Clipboard", x - 44 + 198, y + 16, false, 86, 4210752);
		Utils.drawStringCenteredScaledMaxWidth("Save " + preset, x - 44 + 198, y + 8 + 24, false, 86, 4210752);
		Utils.drawStringCenteredScaledMaxWidth("to Clipboard", x - 44 + 198, y + 16 + 24, false, 86, 4210752);

		if (!valid) {
			Gui.drawRect(x - 88 + 198, y + 2, x + 198, y + 2 + 20, 0x80000000);
		}
		if (!secondValid) {
			Gui.drawRect(x - 88 + 198, y + 2 + 24, x + 198, y + 2 + 20 + 24, 0x80000000);
		}

		GlStateManager.color(1, 1, 1, 1);
	}

	public static boolean validShareContents(String sharePrefix) {
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

	public static void shareContents(String sharePrefix, String jsonObject) {
		String base64String = Base64.getEncoder().encodeToString((sharePrefix +
			jsonObject).getBytes(StandardCharsets.UTF_8));
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(base64String), null);
	}

	public static String getShareFromClipboard(String sharePrefix) {
		String base64;

		try {
			base64 = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (HeadlessException | IOException | UnsupportedFlavorException e) {
			return null;
		}

		if (base64.length() <= sharePrefix.length()) return null;

		base64 = base64.trim();

		String jsonString;
		try {
			jsonString = new String(Base64.getDecoder().decode(base64));
			if (!jsonString.startsWith(sharePrefix)) return null;
			jsonString = jsonString.substring(sharePrefix.length());
		} catch (IllegalArgumentException e) {
			return null;
		}

		return jsonString;
	}
}
