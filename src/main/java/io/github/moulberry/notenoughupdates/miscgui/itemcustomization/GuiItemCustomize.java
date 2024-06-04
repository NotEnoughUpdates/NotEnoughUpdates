/*
 * Copyright (C) 2022-2024 NotEnoughUpdates contributors
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.GlScissorStack;
import io.github.moulberry.notenoughupdates.core.GuiElement;
import io.github.moulberry.notenoughupdates.core.GuiElementBoolean;
import io.github.moulberry.notenoughupdates.core.GuiElementColour;
import io.github.moulberry.notenoughupdates.core.GuiElementTextField;
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpingFloat;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.GuiTextures;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GuiItemCustomize extends GuiScreen {
	private static final ResourceLocation PLUS = new ResourceLocation("notenoughupdates:itemcustomize/plus.png");

	private final ItemStack stack;
	ItemStack customItemStack;
	private final String itemUUID;
	private final GuiElementTextField textFieldRename = new GuiElementTextField("", 158, 20, GuiElementTextField.COLOUR);
	final GuiElementTextField textFieldCustomItem = new GuiElementTextField("", 180, 20, GuiElementTextField.COLOUR);
	final GuiElementTextField textFieldTickSpeed = new GuiElementTextField("", 180, 45, GuiElementTextField.COLOUR | GuiElementTextField.NUM_ONLY);
	private final GuiElementBoolean enchantGlintButton;

	private int renderHeight = 0;

	private final LerpingFloat enchantGlintCustomColourAnimation = new LerpingFloat(0, 200);

	private boolean enchantGlint;
	String customGlintColour = null;

	String customLeatherColour = null;
	ArrayList<String> animatedLeatherColours = new ArrayList<>();
	int animatedDyeTicks = 2;
	private int lastTicks = 2;
	boolean supportCustomLeatherColour;
	private String lastCustomItem = "";

	JsonObject animatedDyes = null;
	JsonObject staticDyes = null;
	ArrayList<DyeType> dyes = new ArrayList<>();
	boolean repoError = false;

	private GuiElement editor = null;

	private GuiType guiType = GuiType.DEFAULT;

	public GuiItemCustomize(ItemStack stack, String itemUUID) {
		this.stack = stack;
		this.itemUUID = itemUUID;
		this.customItemStack = ItemCustomizationUtills.copy(stack, this);

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
			if (data.animatedLeatherColours != null) {
				this.animatedLeatherColours = new ArrayList<>(Arrays.asList(data.animatedLeatherColours));
				if (data.animatedDyeTicks < 1) {
					this.animatedDyeTicks = 1;
					data.animatedDyeTicks = 1;
				}
				else {
					this.animatedDyeTicks = data.animatedDyeTicks;
				}
				this.textFieldTickSpeed.setText("" + this.animatedDyeTicks);
			} else {
				this.animatedLeatherColours = new ArrayList<>();
				this.textFieldTickSpeed.setText("2");

			}
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

		JsonObject dyesConst = Constants.DYES;

		if (dyesConst == null) {
			Utils.showOutdatedRepoNotification("dyes.json");
			repoError = true;
			return;
		} else {
			repoError = false;
		}

		if (dyesConst.has("animated")) {
			animatedDyes = dyesConst.get("animated").getAsJsonObject();
		}
		if (dyesConst.has("static")) {
			staticDyes = dyesConst.get("static").getAsJsonObject();
		}

		DyeType animatedHeader = new DyeType("Animated Dyes");
		dyes.add(animatedHeader);

		animatedDyes.entrySet().forEach(entry -> {
			String key = entry.getKey();
			JsonArray value = entry.getValue().getAsJsonArray();
			DyeType dyeType = new DyeType(key, value);
			dyes.add(dyeType);
		});

		DyeType staticHeader = new DyeType("Static Dyes");
		dyes.add(staticHeader);

		staticDyes.entrySet().forEach(entry -> {
			String key = entry.getKey();
			String value = entry.getValue().getAsString();
			DyeType dyeType = new DyeType(key, value);
			dyes.add(dyeType);
		});


	}

	@Override
	public void onGuiClosed() {
		updateData();
	}

	public void updateData() {
		ItemCustomizeManager.ItemData data = new ItemCustomizeManager.ItemData();

		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
		boolean stackHasEffect = stack.hasEffect() && !model.isBuiltInRenderer();

		this.customItemStack = ItemCustomizationUtills.copy(stack, this);
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
			ItemCustomizationUtills.getChromaStrFromLeatherColour(this)))) {
			data.customLeatherColour = this.customLeatherColour;
		} else {
			data.customLeatherColour = null;
		}

		if (!this.animatedLeatherColours.isEmpty()) {
			data.animatedLeatherColours = new String[animatedLeatherColours.size()];
			data.animatedLeatherColours = animatedLeatherColours.toArray(data.animatedLeatherColours);
			if (textFieldTickSpeed.getText().isEmpty()) {
				data.animatedDyeTicks = 2;
			} else {
				try {
					int dyeTicks = Integer.parseInt(textFieldTickSpeed.getText());
					data.animatedDyeTicks = Math.max(dyeTicks, 1);
				} catch (NumberFormatException e) {
					data.animatedDyeTicks = 2;
				}
			}
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

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (!supportCustomLeatherColour) guiType = GuiType.DEFAULT;
		drawScreenType(mouseX, mouseY, partialTicks, guiType);
	}

	private void drawScreenType(int mouseX, int mouseY, float partialTicks, GuiType type) {
		if (type == GuiType.DEFAULT) {
			drawScreenDefault(mouseX, mouseY, partialTicks);
		} else if (type == GuiType.ANIMATED) {
			drawScreenAnimatedDyes(mouseX, mouseY, partialTicks);
		} else if (type == GuiType.HYPIXEL) {
			drawScreenHypixel(mouseX, mouseY, partialTicks);
		}
	}

	private void drawScreenDefault(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();

		List<String> tooltipToDisplay = null;

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

		int xCenter = scaledResolution.getScaledWidth() / 2;
		int yTopStart = (scaledResolution.getScaledHeight() - renderHeight) / 2;
		int yTop = yTopStart;

		renderHeader(xCenter, yTop);

		yTop += 14;

		ItemCustomizationUtills.renderTextBox(textFieldRename, "§7Enter Custom Name...", xCenter - textFieldRename.getWidth() / 2 - 10, yTop, 158);


		int yTopText = yTop;

		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.help);
		GlStateManager.color(1, 1, 1, 1);
		int helpX = xCenter + textFieldRename.getWidth() / 2 - 5;
		Utils.drawTexturedRect(helpX, yTop, 20, 20, GL11.GL_LINEAR);

		if (mouseX >= helpX && mouseX <= helpX + 20 && mouseY >= yTop && mouseY <= yTop + 20) {
			tooltipToDisplay = ItemCustomizationUtills.customizeColourGuide;
		}

		yTop += 25;

		renderBigStack(xCenter, yTop);

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

			int glintColour = ItemCustomizationUtills.getGlintColour(this);

			GlScissorStack.push(
				0,
				yTop,
				scaledResolution.getScaledWidth(),
				scaledResolution.getScaledHeight(),
				scaledResolution
			);
			GlStateManager.translate(0, enchantGlintCustomColourAnimation.getValue() - 17, 0);

		 ItemCustomizationUtills.renderColourBlob(xCenter, yTop, glintColour, "§a§lCustom Glint Colour", true);

			GlStateManager.translate(0, -enchantGlintCustomColourAnimation.getValue() + 17, 0);
			GlScissorStack.pop(scaledResolution);

			yTop += enchantGlintCustomColourAnimation.getValue() + 3;
		}

		supportCustomLeatherColour = customItemStack.getItem() instanceof ItemArmor &&
			((ItemArmor) customItemStack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER;

		if (supportCustomLeatherColour) {
			int leatherColour = ItemCustomizationUtills.getLeatherColour(this);

			ItemCustomizationUtills.renderColourBlob(xCenter, yTop, leatherColour, "§b§lCustom Leather Colour", true);

			yTop += 20;
		}

		if (!lastCustomItem.equals(textFieldCustomItem.getText())) {
			updateData();
		}
		lastCustomItem = textFieldCustomItem.getText();


		int offset = 200;
		if (!supportCustomLeatherColour) offset -= 20;
		if (!enchantGlint) offset -= 16;

		ItemCustomizationUtills.renderTextBox(textFieldCustomItem, "§7Enter Custom Item ID...", xCenter - textFieldCustomItem.getWidth() / 2 - 10 + 11, yTopText + offset, 180);

		if (supportCustomLeatherColour) {
			yTop += 25;
			ItemCustomizationUtills.renderFooter(xCenter, yTop, guiType);
		}


		renderHeight = yTop - yTopStart;

		if (editor != null) {
			editor.render();
		}

		if (tooltipToDisplay != null) {
			Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, width, height, -1);
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void drawScreenAnimatedDyes(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();

		List<String> tooltipToDisplay = null;

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

		int xCenter = scaledResolution.getScaledWidth() / 2;
		int yTopStart = (scaledResolution.getScaledHeight() - renderHeight) / 2;
		int yTop = yTopStart;

		renderHeader(xCenter, yTop);

		yTop += 14;

		renderBigStack(xCenter, yTop);

		yTop += 115;

		if (animatedLeatherColours.isEmpty()) {
		}

		int adjustedY = yTop + pageScroll + 20;

		for (int i = 0; i < animatedLeatherColours.size(); i++) {
			//todo gui auto is + 230
			if (adjustedY + 20 * i < yTopStart + 130 || adjustedY + 20 * i >= yTopStart + 330) {
				continue;
			}

			int leatherColour = ItemCustomizationUtills.getLeatherColour(animatedLeatherColours.get(i));
			ItemCustomizationUtills.renderColourBlob(xCenter, yTop, leatherColour, "§b§lDye Colour " + (i + 1), true);

			yTop += 20;
		}


		Minecraft.getMinecraft().getTextureManager().bindTexture(PLUS);
		GlStateManager.color(1, 1, 1, 1);
		RenderUtils.drawTexturedRect(xCenter + 90 - 12, yTop + 4, 10, 10, GL11.GL_NEAREST);

		ItemCustomizationUtills.renderTextBox(textFieldTickSpeed, "§7Speed...", xCenter - textFieldCustomItem.getWidth() / 2 - 10 + 11, yTop, 45);

		Utils.renderShadowedString("§c§lClear", xCenter - 20, yTop + 6, xCenter*2);

		yTop += 25;

		enchantGlintCustomColourAnimation.tick();

		ItemCustomizationUtills.renderFooter(xCenter, yTop, guiType);

		try {
			if (lastTicks != (Integer.parseInt(textFieldTickSpeed.getText()))) {
				updateData();
			}
			lastTicks = Integer.parseInt(textFieldTickSpeed.getText());
		} catch (NumberFormatException ignored) {
		}

		renderHeight = yTop - yTopStart;

		if (editor != null) {
			editor.render();
		}

		if (tooltipToDisplay != null) {
			Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, width, height, -1);
		}

		scrollScreen(animatedLeatherColours.size());

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void drawScreenHypixel(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();

		List<String> tooltipToDisplay = null;

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

		int xCenter = scaledResolution.getScaledWidth() / 2;
		int yTopStart = (scaledResolution.getScaledHeight() - renderHeight) / 2;
		int yTop = yTopStart;

		renderHeader(xCenter, yTop);

		yTop += 14;

		if (repoError) {
			Utils.renderShadowedString("Repo Error", xCenter, yTop + 4, 180);

			yTop += 15;

			ItemCustomizationUtills.renderFooter(xCenter, yTop, guiType);

			renderHeight = yTop - yTopStart;

			super.drawScreen(mouseX, mouseY, partialTicks);
			return;
		}

		renderBigStack(xCenter, yTop);

		yTop += 115;

		int adjustedY = yTop + pageScroll + 20;

		for (int i = 0; i < dyes.size(); i++) {
			//todo gui auto is + 230
			if (adjustedY + 20 * i < yTopStart + 130 || adjustedY + 20 * i >= yTopStart + 330) {
				continue;
			}

			Color color = ItemCustomizationUtills.getColourFromHex(dyes.get(i).colour);

			JsonArray colours = dyes.get(i).colours;
			String itemId = dyes.get(i).itemId;
			String displayName = null;
			ItemStack itemStack = NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery()
																														 .withKnownInternalName(itemId)
																														 .resolveToItemStack();
			if (itemStack == null && (colours != null || dyes.get(i).colour != null)) {
				itemStack = NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery()
																														 .withKnownInternalName("DYE_PURE_YELLOW")
																														 .resolveToItemStack();
				displayName = itemId;
			}
			if (itemStack != null) {
				if (displayName == null) displayName = itemStack.getDisplayName();
				//Utils.drawItemStack(itemStack, xCenter - 90, yTop);
				GlStateManager.enableDepth();
				GlStateManager.pushMatrix();
				GlStateManager.translate(xCenter - 90, yTop, 0);
				GlStateManager.scale(.9, .9, 1);
				Utils.drawItemStack(itemStack, 0, 0);
				GlStateManager.popMatrix();
			}
			if (color == null && colours == null) {
				if (displayName == null) displayName = itemId;
				Utils.renderShadowedString(displayName, xCenter, yTop + 4, 180);
			} else if (color == null && colours != null) {
				String colourHex = colours.get(
					(Minecraft.getMinecraft().thePlayer.ticksExisted / this.animatedDyeTicks) % colours.size()).getAsString();
				int colourFromHex = ItemCustomizationUtills.rgbToInt(ItemCustomizationUtills.getColourFromHex(colourHex));
				ItemCustomizationUtills.renderColourBlob(xCenter, yTop, colourFromHex, displayName, false);
			} else {
				int colour = ItemCustomizationUtills.rgbToInt(color);
				ItemCustomizationUtills.renderColourBlob(xCenter, yTop, colour, displayName, false);
			}

			yTop += 20;
		}

		ItemCustomizationUtills.renderTextBox(textFieldTickSpeed, "§7Speed...", xCenter - textFieldCustomItem.getWidth() / 2 - 10 + 11, yTop, 45);

		yTop += 25;

		enchantGlintCustomColourAnimation.tick();

		ItemCustomizationUtills.renderFooter(xCenter, yTop, guiType);

		try {
			if (lastTicks != (Integer.parseInt(textFieldTickSpeed.getText()))) {
				updateData();
			}
			lastTicks = Integer.parseInt(textFieldTickSpeed.getText());
		} catch (NumberFormatException ignored) {
		}

		renderHeight = yTop - yTopStart;

		if (editor != null) {
			editor.render();
		}

		if (tooltipToDisplay != null) {
			Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, width, height, -1);
		}

		scrollScreen(dyes.size());

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	int lastMouseScroll = 0;
	int noMouseScrollFrames;
	double scrollVelocity = 0;
	int pageScroll = 0;

	private void scrollScreen(int size) {
		scrollVelocity += lastMouseScroll / 48.0;
		scrollVelocity *= 0.95;
		pageScroll += (int) scrollVelocity + lastMouseScroll / 24;

		noMouseScrollFrames++;

		if (noMouseScrollFrames >= 100) {
			scrollVelocity *= 0.75;
		}

		if (pageScroll > 0) {
			pageScroll = 0;
		}

		//todo gui auto is - 80
		pageScroll = MathHelper.clamp_int(pageScroll, -((size * 20 - 20) - 180), 0);
		lastMouseScroll = 0;
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

		if (textFieldTickSpeed.getFocus()) {
			updateData();
			if (keyCode == Keyboard.KEY_ESCAPE) {
				textFieldTickSpeed.setFocus(false);
				return;
			} else {
				textFieldTickSpeed.keyTyped(typedChar, keyCode);
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
			if (guiType == GuiType.DEFAULT) enchantGlintButton.mouseInput(mouseX, mouseY);
		}

		if (guiType != GuiType.DEFAULT) {
			if (!Mouse.getEventButtonState() && Mouse.getEventDWheel() != 0) {
				lastMouseScroll = Mouse.getEventDWheel();
				noMouseScrollFrames = 0;
			}
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		textFieldRename.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		textFieldCustomItem.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		textFieldTickSpeed.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}


	private void mouseClickedType(int mouseX, int mouseY, int mouseButton, GuiType type) throws IOException {
		if (type == GuiType.DEFAULT) {
			mouseClickedDefault(mouseX, mouseY, mouseButton);
		} else if (type == GuiType.ANIMATED) {
			mouseClickedAnimatedDyes(mouseX, mouseY, mouseButton);
		} else if (type == GuiType.HYPIXEL) {
			mouseClickedHypixel(mouseX, mouseY, mouseButton);
		}
	}

	private void mouseClickedDefault(int mouseX, int mouseY, int mouseButton) throws IOException {
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
					() -> customLeatherColour == null ? ItemCustomizationUtills.getChromaStrFromLeatherColour(this) : customLeatherColour,
					(colour) -> {
						customLeatherColour = colour;
						updateData();
					}, () -> editor = null, false, true
				);
			}
		}

		if (supportCustomLeatherColour) {
			/*if (mouseX >= xCenter + 105 && mouseY >= belowEnchGlint - 7) {
				if (mouseX <= xCenter + 125 && mouseY <= belowEnchGlint + 15) {
					guiType = GuiType.DYES;
				}
			}*/

				float buttonOffset = yTop + 174 + enchantGlintCustomColourAnimation.getValue() + 5 + 45;
				System.out.println(mouseY - buttonOffset);

				GuiType buttonClicked = ItemCustomizationUtills.getButtonClicked(mouseX, mouseY, guiType, buttonOffset);
				if (buttonClicked != null) guiType = buttonClicked;

		/*	System.out.println(mouseX + " " + mouseY);
			System.out.println(xCenter + " " + belowEnchGlint);
			System.out.println((mouseX >= xCenter + 105) + " " + (mouseY >= belowEnchGlint - 7));
			System.out.println((mouseX <= xCenter + 125) + " " + (mouseY <= belowEnchGlint + 15));*/

		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private void mouseClickedAnimatedDyes(int mouseX, int mouseY, int mouseButton) throws IOException {
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int xCenter = scaledResolution.getScaledWidth() / 2;
		int yTop = (scaledResolution.getScaledHeight() - renderHeight) / 2;
		int topOffset = yTop + 129;
		float bottomOffset = yTop + renderHeight + 3;

		ArrayList<Integer> indexToRemove = new ArrayList<>();

		int adjustedY = topOffset + pageScroll + 20;

		for (int i = 0; i < animatedLeatherColours.size(); i++) {
			if (adjustedY + 20 * i < yTop + 130 || adjustedY + 20 * i >= yTop + 330) {
				continue;
			}
			if (supportCustomLeatherColour && mouseX >= xCenter - 90 && mouseX <= xCenter + 90 &&
				mouseY >= topOffset &&
				mouseY <= topOffset + 15) {
				int finalI = i;
				if (mouseX >= xCenter + 90 - 12) {
					editor = null;
					indexToRemove.add(i);
					updateData();
				} else {

					editor = new GuiElementColour(mouseX, mouseY,
						() -> {
							String animatedColour = animatedLeatherColours.get(finalI);
							return animatedColour == null
								? ItemCustomizationUtills.getChromaStrFromLeatherColour(this)
								: animatedColour;
						},
						(colour) -> {
							animatedLeatherColours.set(finalI, colour);
							updateData();
						}, () -> editor = null, false, true
					);
				}
			}
			topOffset += 20;
		}

		if (supportCustomLeatherColour && mouseX >= xCenter - 90 && mouseX <= xCenter + 90 &&
			mouseY >= topOffset &&
			mouseY <= topOffset + 15) {
			if (mouseX >= xCenter + 90 - 12) {
				editor = null;
				if (!animatedLeatherColours.isEmpty()) animatedLeatherColours.add(animatedLeatherColours.get(animatedLeatherColours.size() - 1));
				else if (customLeatherColour != null) animatedLeatherColours.add(customLeatherColour);
				else animatedLeatherColours.add(ItemCustomizationUtills.getChromaStrFromLeatherColour(this));
				updateData();
				//todo gui scales
				pageScroll = -((animatedLeatherColours.size() * 20 - 20) - 180);
			}
		}

		if (mouseX >= xCenter - textFieldTickSpeed.getWidth() / 2 - 70 &&
			mouseX <= xCenter + textFieldTickSpeed.getWidth() / 2 - 70 &&
			mouseY >= topOffset && mouseY <= topOffset + textFieldTickSpeed.getHeight()) {
			textFieldTickSpeed.mouseClicked(mouseX, mouseY, mouseButton);
		} else {
			textFieldTickSpeed.unfocus();
		}


		for (Integer i : indexToRemove) {
			animatedLeatherColours.set(i, null);
		}
		animatedLeatherColours.removeAll(Collections.singleton(null));

		if (mouseX >= xCenter - 23 - 15 &&
			mouseX <= xCenter + 23 / 2 - 15 &&
			mouseY >= topOffset && mouseY <= topOffset + 20) {
			animatedLeatherColours.clear();
		}

		GuiType buttonClicked = ItemCustomizationUtills.getButtonClicked(mouseX, mouseY, guiType, bottomOffset);
		if (buttonClicked != null) guiType = buttonClicked;

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private void mouseClickedHypixel(int mouseX, int mouseY, int mouseButton) throws IOException {
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int xCenter = scaledResolution.getScaledWidth() / 2;
		int yTop = (scaledResolution.getScaledHeight() - renderHeight) / 2;
		int topOffset = yTop + 129;
		float bottomOffset = yTop + renderHeight + 3;

		int adjustedY = topOffset + pageScroll + 20;

		for (int i = 0; i < dyes.size(); i++) {
			if (adjustedY + 20 * i < yTop + 130 || adjustedY + 20 * i >= yTop + 330) {
				continue;
			}
			if (supportCustomLeatherColour && mouseX >= xCenter - 90 && mouseX <= xCenter + 90 && mouseY >= topOffset &&
				mouseY <= topOffset + 15) {
				if (dyes.get(i).hasAnimatedColour()) {
					animatedLeatherColours.clear();
					for (JsonElement colour : dyes.get(i).colours) {
						String string = colour.getAsString();
						Color colourFromHex = ItemCustomizationUtills.getColourFromHex(string);
						String special = SpecialColour.special(0, 0, colourFromHex.getRGB());
						animatedLeatherColours.add(special);
					}
				} else if ((dyes.get(i).hasStaticColour())) {
					animatedLeatherColours.clear();
					Color colourFromHex = ItemCustomizationUtills.getColourFromHex(dyes.get(i).colour);
					String special = SpecialColour.special(0, 0, colourFromHex.getRGB());
					customLeatherColour = special;
				}
				updateData();
			} topOffset += 20;
		}

		if (mouseX >= xCenter - textFieldTickSpeed.getWidth() / 2 - 70 &&
			mouseX <= xCenter + textFieldTickSpeed.getWidth() / 2 - 70 &&
			mouseY >= topOffset && mouseY <= topOffset + textFieldTickSpeed.getHeight()) {
			textFieldTickSpeed.mouseClicked(mouseX, mouseY, mouseButton);
		} else {
			textFieldTickSpeed.unfocus();
		}

		GuiType buttonClicked = ItemCustomizationUtills.getButtonClicked(mouseX, mouseY, guiType, bottomOffset);
		if (buttonClicked != null) guiType = buttonClicked;

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}


	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		mouseClickedType(mouseX, mouseY, mouseButton, guiType);
	}

	public void renderHeader(int xCenter, int yTop) {
		RenderUtils.drawFloatingRectDark(xCenter - 100, yTop - 9, 200, renderHeight + 33);

		RenderUtils.drawFloatingRectDark(xCenter - 90, yTop - 5, 180, 14);
		Utils.renderShadowedString("\u00a75\u00a7lNEU Item Customizer", xCenter, yTop - 1, 180);

	}

	private void renderBigStack(int xCenter, int yTop) {
		RenderUtils.drawFloatingRectDark(xCenter - 90, yTop, 180, 110);
		GlStateManager.enableDepth();
		GlStateManager.pushMatrix();
		GlStateManager.translate(xCenter - 48, yTop + 7, 0);
		GlStateManager.scale(6, 6, 1);
		this.customItemStack = ItemCustomizationUtills.copy(stack, this);
		Utils.drawItemStack(customItemStack, 0, 0);
		GlStateManager.popMatrix();
	}


}
