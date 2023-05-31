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

import com.google.common.collect.Lists;
import io.github.moulberry.notenoughupdates.core.ChromaColour;
import io.github.moulberry.notenoughupdates.core.GlScissorStack;
import io.github.moulberry.notenoughupdates.core.GuiElement;
import io.github.moulberry.notenoughupdates.core.GuiElementBoolean;
import io.github.moulberry.notenoughupdates.core.GuiElementColour;
import io.github.moulberry.notenoughupdates.core.GuiElementTextField;
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpingFloat;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.miscfeatures.ItemCustomizeManager;
import io.github.moulberry.notenoughupdates.util.GuiTextures;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

public class GuiItemCustomize extends GuiScreen {
	private static final ResourceLocation RESET = new ResourceLocation("notenoughupdates:itemcustomize/reset.png");

	private final ItemStack stack;
	private ItemStack customItemStack;
	private final String itemUUID;
	private final GuiElementTextField textFieldRename = new GuiElementTextField("", 158, 20, GuiElementTextField.COLOUR);
	private final GuiElementTextField textFieldCustomItem = new GuiElementTextField("", 180, 20, GuiElementTextField.COLOUR);
	private final GuiElementBoolean enchantGlintButton;

	private int renderHeight = 0;

	private final LerpingFloat enchantGlintCustomColourAnimation = new LerpingFloat(0, 200);

	private boolean enchantGlint;
	private String customGlintColour = null;

	private String customLeatherColour = null;
	private boolean supportCustomLeatherColour;
	private String lastCustomItem = "";

	private GuiElement editor = null;

	public GuiItemCustomize(ItemStack stack, String itemUUID) {
		this.stack = stack;
		this.itemUUID = itemUUID;
		this.customItemStack = copy(stack);

		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
		boolean stackHasEffect = stack.hasEffect() && !model.isBuiltInRenderer();

		ItemCustomizeManager.ItemData data = ItemCustomizeManager.getDataForItem(stack);
		if (data != null) {
			this.enchantGlint = data.overrideEnchantGlint ? data.enchantGlintValue : stackHasEffect;
			if (data.customName != null) {
				textFieldRename.setText(data.customName);
			}
			if (data.customItem != null && data.customItem.length() > 0) {
				textFieldCustomItem.setText(data.customItem);
			} else {
				textFieldCustomItem.setText(stack.getItem().getRegistryName().replace("minecraft:", ""));
			}
			this.customGlintColour = data.customGlintColour;
			this.customLeatherColour = data.customLeatherColour;
		} else {
			this.enchantGlint = stackHasEffect;
			textFieldCustomItem.setText(stack.getItem().getRegistryName().replace("minecraft:", ""));
		}

		supportCustomLeatherColour = customItemStack.getItem() instanceof ItemArmor &&
			((ItemArmor) customItemStack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER;

		enchantGlintCustomColourAnimation.setValue(enchantGlint ? 17 : 0);
		this.enchantGlintButton = new GuiElementBoolean(0, 0, () -> enchantGlint, (bool) -> {
			enchantGlint = bool;
			updateData();
		});

	}

	@Override
	public void onGuiClosed() {
		updateData();
	}

	public String getChromaStrFromLeatherColour() {
		return ChromaColour.special(0, 0xff, ((ItemArmor) customItemStack.getItem()).getColor(customItemStack));
	}

	public void updateData() {
		ItemCustomizeManager.ItemData data = new ItemCustomizeManager.ItemData();

		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
		boolean stackHasEffect = stack.hasEffect() && !model.isBuiltInRenderer();

		this.customItemStack = copy(stack);
		data.defaultItem = stack.getItem().getRegistryName();

		if (this.enchantGlint != stackHasEffect) {
			data.overrideEnchantGlint = true;
			data.enchantGlintValue = this.enchantGlint;
		}

		if (this.customGlintColour != null && !this.customGlintColour.equals(ItemCustomizeManager.DEFAULT_GLINT_COLOR)) {
			data.customGlintColour = this.customGlintColour;
		} else if (model.isBuiltInRenderer() && data.overrideEnchantGlint && data.enchantGlintValue) {
			data.customGlintColour = ItemCustomizeManager.DEFAULT_GLINT_COLOR;
		} else {
			data.customGlintColour = null;
		}

		if (this.customLeatherColour != null && (!(customItemStack.getItem() instanceof ItemArmor) || !this.customLeatherColour.equals(
			getChromaStrFromLeatherColour()))) {
			data.customLeatherColour = this.customLeatherColour;
		} else {
			data.customLeatherColour = null;
		}

		if (!this.textFieldRename.getText().isEmpty()) {
			data.customName = this.textFieldRename.getText();

			NBTTagCompound stackTagCompound = stack.getTagCompound();
			if (stackTagCompound != null && stackTagCompound.hasKey("display", 10)) {
				NBTTagCompound nbttagcompound = stackTagCompound.getCompoundTag("display");

				if (nbttagcompound.hasKey("Name", 8)) {
					String name = nbttagcompound.getString("Name");
					char[] chars = name.toCharArray();

					int i;
					for (i = 0; i < chars.length; i += 2) {
						if (chars[i] != '\u00a7') {
							break;
						}
					}

					data.customNamePrefix = name.substring(0, i);
				}
			}
		}

		if (!this.textFieldCustomItem.getText().isEmpty()) {
			data.customItem = this.textFieldCustomItem.getText();
		}

		ItemCustomizeManager.putItemData(itemUUID, data);
	}

	private ItemStack copy(ItemStack stack) {
		ItemStack customStack = stack.copy();
		if (!this.textFieldCustomItem.getText().isEmpty()) {
			customStack.setItem(ItemCustomizeManager.getCustomItem(stack, this.textFieldCustomItem.getText().trim()));
			customStack.setItemDamage(ItemCustomizeManager.getCustomItemDamage(stack));
		}
		return customStack;
	}

	private int getGlintColour() {
		int col = customGlintColour == null
			? ChromaColour.specialToChromaRGB(ItemCustomizeManager.DEFAULT_GLINT_COLOR)
			: ChromaColour.specialToChromaRGB(customGlintColour);
		return 0xff000000 | col;
	}

	private int getLeatherColour() {
		if (!supportCustomLeatherColour) return 0xff000000;

		int col =
			customLeatherColour == null ? ((ItemArmor) customItemStack.getItem()).getColor(customItemStack) : ChromaColour.specialToChromaRGB(
				customLeatherColour);
		return 0xff000000 | col;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();

		List<String> tooltipToDisplay = null;

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

		int xCenter = scaledResolution.getScaledWidth() / 2;
		int yTopStart = (scaledResolution.getScaledHeight() - renderHeight) / 2;
		int yTop = yTopStart;

		RenderUtils.drawFloatingRectDark(xCenter - 100, yTop - 9, 200, renderHeight + 33);

		RenderUtils.drawFloatingRectDark(xCenter - 90, yTop - 5, 180, 14);
		Utils.renderShadowedString("\u00a75\u00a7lNEU Item Customizer", xCenter, yTop - 1, 180);

		yTop += 14;

		if (!textFieldRename.getFocus() && textFieldRename.getText().isEmpty()) {
			textFieldRename.setOptions(GuiElementTextField.SCISSOR_TEXT);
			textFieldRename.setPrependText("\u00a77Enter Custom Name...");
		} else {
			textFieldRename.setOptions(GuiElementTextField.COLOUR | GuiElementTextField.SCISSOR_TEXT);
			textFieldRename.setPrependText("");
		}

		if (!textFieldRename.getFocus()) {
			textFieldRename.setSize(158, 20);
		} else {
			int textSize = fontRendererObj.getStringWidth(textFieldRename.getTextDisplay()) + 10;
			textFieldRename.setSize(Math.max(textSize, 158), 20);
		}

		textFieldRename.render(xCenter - textFieldRename.getWidth() / 2 - 10, yTop);
		int yTopText = yTop;

		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.help);
		GlStateManager.color(1, 1, 1, 1);
		int helpX = xCenter + textFieldRename.getWidth() / 2 - 5;
		Utils.drawTexturedRect(helpX, yTop, 20, 20, GL11.GL_LINEAR);

		if (mouseX >= helpX && mouseX <= helpX + 20 && mouseY >= yTop && mouseY <= yTop + 20) {
			tooltipToDisplay = Lists.newArrayList(
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
		}

		yTop += 25;

		RenderUtils.drawFloatingRectDark(xCenter - 90, yTop, 180, 110);
		GlStateManager.enableDepth();
		GlStateManager.pushMatrix();
		GlStateManager.translate(xCenter - 48, yTop + 7, 0);
		GlStateManager.scale(6, 6, 1);
		this.customItemStack = copy(stack);
		Utils.drawItemStack(customItemStack, 0, 0);
		GlStateManager.popMatrix();

		yTop += 115;

		RenderUtils.drawFloatingRectDark(xCenter - 90, yTop, 180, 20);

		Minecraft.getMinecraft().fontRendererObj.drawString("Enchant Glint",
			xCenter - 85, yTop + 6, 0xff8040cc
		);

		enchantGlintButton.x = xCenter + 90 - 5 - 48;
		enchantGlintButton.y = yTop + 3;
		enchantGlintButton.render();

		yTop += 25;

		enchantGlintCustomColourAnimation.tick();
		if (enchantGlintCustomColourAnimation.getValue() > 0) {
			yTop -= 5;

			int glintColour = getGlintColour();

			GlScissorStack.push(
				0,
				yTop,
				scaledResolution.getScaledWidth(),
				scaledResolution.getScaledHeight(),
				scaledResolution
			);
			GlStateManager.translate(0, enchantGlintCustomColourAnimation.getValue() - 17, 0);

			Gui.drawRect(xCenter - 90, yTop, xCenter + 92, yTop + 17, 0x70000000);
			Gui.drawRect(xCenter - 90, yTop, xCenter + 90, yTop + 15, 0xff101016);
			Gui.drawRect(xCenter - 89, yTop + 1, xCenter + 89, yTop + 14, 0xff000000 | glintColour);

			Utils.renderShadowedString("\u00a7a\u00a7lCustom Glint Colour", xCenter, yTop + 4, 180);

			Minecraft.getMinecraft().getTextureManager().bindTexture(RESET);
			GlStateManager.color(1, 1, 1, 1);
			RenderUtils.drawTexturedRect(xCenter + 90 - 12, yTop + 2, 10, 11, GL11.GL_NEAREST);

			GlStateManager.translate(0, -enchantGlintCustomColourAnimation.getValue() + 17, 0);
			GlScissorStack.pop(scaledResolution);

			yTop += enchantGlintCustomColourAnimation.getValue() + 3;
		}

		supportCustomLeatherColour = customItemStack.getItem() instanceof ItemArmor &&
			((ItemArmor) customItemStack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER;

		if (supportCustomLeatherColour) {
			int leatherColour = getLeatherColour();

			Gui.drawRect(xCenter - 90, yTop, xCenter + 92, yTop + 17, 0x70000000);
			Gui.drawRect(xCenter - 90, yTop, xCenter + 90, yTop + 15, 0xff101016);
			Gui.drawRect(xCenter - 89, yTop + 1, xCenter + 89, yTop + 14, 0xff000000 | leatherColour);

			Utils.renderShadowedString("\u00a7b\u00a7lCustom Leather Colour", xCenter, yTop + 4, 180);

			Minecraft.getMinecraft().getTextureManager().bindTexture(RESET);
			GlStateManager.color(1, 1, 1, 1);
			RenderUtils.drawTexturedRect(xCenter + 90 - 12, yTop + 2, 10, 11, GL11.GL_NEAREST);

			yTop += 20;
		}

        /*if(true) {
            yTop += 20;

            String titleStr = "\u00a76\u00a7lWant other players to see your customized item?";
            String buttonStr = "\u00a76Purchase Item Customize Tag";
            if(true) {
                buttonStr = "\u00a76Use item customize tag (3 remaining)";
            }

            int w = Minecraft.getMinecraft().fontRendererObj.getStringWidth(titleStr)+8;
            if(w > scaledResolution.getScaledWidth()/2) w= scaledResolution.getScaledWidth()/2;

            RenderUtils.drawFloatingRectDark(xCenter-w/2, yTop, w, 50);
            Utils.renderShadowedString(titleStr,  xCenter, yTop+8, scaledResolution.getScaledWidth()/2);

            int ctw = Minecraft.getMinecraft().fontRendererObj.getStringWidth(buttonStr)+8;

            RenderUtils.drawFloatingRectDark(xCenter-ctw/2, yTop+25, ctw, 15);
            Utils.renderShadowedString(buttonStr, xCenter, yTop+28, w);



        }*/

		if (!lastCustomItem.equals(textFieldCustomItem.getText())) {
			updateData();
		}
		lastCustomItem = textFieldCustomItem.getText();

		if (!textFieldCustomItem.getFocus() && textFieldCustomItem.getText().isEmpty()) {
			textFieldCustomItem.setOptions(GuiElementTextField.SCISSOR_TEXT);
			textFieldCustomItem.setPrependText("\u00a77Enter Custom Item ID...");
		} else {
			textFieldCustomItem.setOptions(GuiElementTextField.COLOUR | GuiElementTextField.SCISSOR_TEXT);
			textFieldCustomItem.setPrependText("");
		}

		if (!textFieldCustomItem.getFocus()) {
			textFieldCustomItem.setSize(180, 20);
		} else {
			int textSize = fontRendererObj.getStringWidth(textFieldCustomItem.getTextDisplay()) + 10;
			textFieldCustomItem.setSize(Math.max(textSize, 180), 20);
		}

		int offset = 200;
		if (!supportCustomLeatherColour) offset -= 20;
		if (!enchantGlint) offset -= 16;

		textFieldCustomItem.render(xCenter - textFieldCustomItem.getWidth() / 2 - 10 + 11, yTopText + offset);

		renderHeight = yTop - yTopStart;

		if (editor != null) {
			editor.render();
		}

		if (tooltipToDisplay != null) {
			Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, width, height, -1);
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void updateScreen() {
		if (enchantGlint) {
			if (enchantGlintCustomColourAnimation.getTarget() != 17) {
				enchantGlintCustomColourAnimation.setTarget(17);
				enchantGlintCustomColourAnimation.resetTimer();
			}
		} else {
			if (enchantGlintCustomColourAnimation.getTarget() != 0) {
				enchantGlintCustomColourAnimation.setTarget(0);
				enchantGlintCustomColourAnimation.resetTimer();
			}
		}

		super.updateScreen();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (textFieldRename.getFocus()) {
			if (keyCode == Keyboard.KEY_ESCAPE) {
				textFieldRename.setFocus(false);
				return;
			} else {
				textFieldRename.keyTyped(typedChar, keyCode);
			}
		}

		if (textFieldCustomItem.getFocus()) {
			updateData();
			if (keyCode == Keyboard.KEY_ESCAPE) {
				textFieldCustomItem.setFocus(false);
				return;
			} else {
				textFieldCustomItem.keyTyped(typedChar, keyCode);
			}
		}

		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void handleKeyboardInput() throws IOException {
		if (editor == null || !editor.keyboardInput()) {
			if (editor != null && Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
				editor = null;
			} else {
				super.handleKeyboardInput();
			}
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

		if (editor == null || !editor.mouseInput(mouseX, mouseY)) {
			super.handleMouseInput();
			enchantGlintButton.mouseInput(mouseX, mouseY);
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		textFieldRename.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		textFieldCustomItem.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int xCenter = scaledResolution.getScaledWidth() / 2;
		int yTop = (scaledResolution.getScaledHeight() - renderHeight) / 2;

		if (mouseX >= xCenter - textFieldRename.getWidth() / 2 - 10 &&
			mouseX <= xCenter + textFieldRename.getWidth() / 2 - 10 &&
			mouseY >= yTop + 14 && mouseY <= yTop + 14 + textFieldRename.getHeight()) {
			textFieldRename.mouseClicked(mouseX, mouseY, mouseButton);
		} else {
			textFieldRename.unfocus();
		}

		int offset = 200;
		if (!supportCustomLeatherColour) offset -= 20;
		if (!enchantGlint) offset -= 18;

		if (mouseX >= xCenter - textFieldCustomItem.getWidth() / 2 - 10 + 11 &&
			mouseX <= xCenter + textFieldCustomItem.getWidth() / 2 - 10 + 11 &&
			mouseY >= yTop + offset + 14 && mouseY <= yTop + offset + 14 + textFieldCustomItem.getHeight()) {
			textFieldCustomItem.mouseClicked(mouseX, mouseY, mouseButton);
		} else {
			textFieldCustomItem.unfocus();
		}

		if (enchantGlint && mouseX >= xCenter - 90 && mouseX <= xCenter + 90 &&
			mouseY >= yTop + 174 && mouseY <= yTop + 174 + enchantGlintCustomColourAnimation.getValue()) {
			if (mouseX >= xCenter + 90 - 12) {
				editor = null;
				customGlintColour = ItemCustomizeManager.DEFAULT_GLINT_COLOR;
				updateData();
			} else {
				editor = new GuiElementColour(
					mouseX,
					mouseY,
					() -> customGlintColour == null ? ItemCustomizeManager.DEFAULT_GLINT_COLOR : customGlintColour,
					(colour) -> {
						customGlintColour = colour;
						updateData();
					},
					() -> editor = null
				);
			}
		}

		float belowEnchGlint = yTop + 174 + enchantGlintCustomColourAnimation.getValue() + 5;

		if (supportCustomLeatherColour && mouseX >= xCenter - 90 && mouseX <= xCenter + 90 &&
			mouseY >= belowEnchGlint &&
			mouseY <= belowEnchGlint + 15) {
			if (mouseX >= xCenter + 90 - 12) {
				editor = null;
				customLeatherColour = null;
				updateData();
			} else {
				editor = new GuiElementColour(mouseX, mouseY,
					() -> customLeatherColour == null ? getChromaStrFromLeatherColour() : customLeatherColour,
					(colour) -> {
						customLeatherColour = colour;
						updateData();
					}, () -> editor = null, false, true
				);
			}
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
}
