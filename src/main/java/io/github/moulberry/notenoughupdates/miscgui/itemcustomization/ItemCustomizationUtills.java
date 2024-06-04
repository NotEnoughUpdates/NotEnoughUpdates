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
import java.util.List;

public class ItemCustomizationUtills {

	public static List<String> customizeColourGuide = Lists.newArrayList(
		EnumChatFormatting.AQUA + "Set a custom name for the item",
		EnumChatFormatting.GREEN + "",
		EnumChatFormatting.GREEN + "Type \"&&\" for \u00B6",
		EnumChatFormatting.GREEN + "Type \"**\" for \u272A",
		EnumChatFormatting.GREEN + "Type \"*1-9\" for \u278A-\u2792",
		EnumChatFormatting.GREEN + "",
		EnumChatFormatting.GREEN + "Available colour codes:",
		Utils.chromaString("\u00B6z = Chroma"),
		EnumChatFormatting.DARK_BLUE + "\u00B61 = Dark Blue",
		EnumChatFormatting.DARK_GREEN + "\u00B62 = Dark Green",
		EnumChatFormatting.DARK_AQUA + "\u00B63 = Dark Aqua",
		EnumChatFormatting.DARK_RED + "\u00B64 = Dark Red",
		EnumChatFormatting.DARK_PURPLE + "\u00B65 = Dark Purple",
		EnumChatFormatting.GOLD + "\u00B66 = Gold",
		EnumChatFormatting.GRAY + "\u00B67 = Gray",
		EnumChatFormatting.DARK_GRAY + "\u00B68 = Dark Gray",
		EnumChatFormatting.BLUE + "\u00B69 = Blue",
		EnumChatFormatting.GREEN + "\u00B6a = Green",
		EnumChatFormatting.AQUA + "\u00B6b = Aqua",
		EnumChatFormatting.RED + "\u00B6c = Red",
		EnumChatFormatting.LIGHT_PURPLE + "\u00B6d = Purple",
		EnumChatFormatting.YELLOW + "\u00B6e = Yellow",
		EnumChatFormatting.WHITE + "\u00B6f = White",
		"\u00A7Z\u00B6Z = SBA Chroma" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY + " (Requires SBA)",
		"",
		EnumChatFormatting.GREEN + "Available formatting codes:",
		EnumChatFormatting.GRAY + "\u00B6k = " + EnumChatFormatting.OBFUSCATED + "Obfuscated",
		EnumChatFormatting.GRAY + "\u00B6l = " + EnumChatFormatting.BOLD + "Bold",
		EnumChatFormatting.GRAY + "\u00B6m = " + EnumChatFormatting.STRIKETHROUGH + "Strikethrough",
		EnumChatFormatting.GRAY + "\u00B6n = " + EnumChatFormatting.UNDERLINE + "Underline",
		EnumChatFormatting.GRAY + "\u00B6o = " + EnumChatFormatting.ITALIC + "Italic"
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

		Gui.drawRect(xCentreLeft - 0, yTop, xCenter + 3, yTop + 17, 0xff101016);
		Gui.drawRect(xCentreLeft - 0, yTop, xCenter + 1, yTop + 15, 0xff101016);
		Gui.drawRect(xCentreLeft - 1, yTop + 1, xCenter, yTop + 14, 0xff000000 | 0xff00ffc4);

		Utils.renderShadowedString(getButtons(guiType, 0).getDisplay(), xCentreLeft + 45, yTop + 4, xCenter*2 - xCentreRight);

		xCentreLeft += 90;
		xCentreRight += 90;

		Gui.drawRect(xCentreLeft - 0, yTop, xCentreRight, yTop + 17, 0x70000000);
		Gui.drawRect(xCentreLeft - 0, yTop, xCentreRight, yTop + 15, 0xff101016);
		Gui.drawRect(xCentreLeft - 1, yTop + 1, xCentreRight, yTop + 14, 0xff000000 | 0xff00ffc4 * 2);

		Utils.renderShadowedString(getButtons(guiType, 1).getDisplay(), xCentreLeft + 45, yTop + 4, xCenter*2 - xCentreRight);
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
			return ChromaColour.specialToChromaRGB(dyeColours[(Minecraft.getMinecraft().thePlayer.ticksExisted / ticks) % dyeColours.length]);
	}

	private static final ResourceLocation RESET = new ResourceLocation("notenoughupdates:itemcustomize/reset.png");

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

	public static void renderTextBox(GuiElementTextField textField, String text, int xOffset, int yOffset, int maxTextSize) {
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
}
