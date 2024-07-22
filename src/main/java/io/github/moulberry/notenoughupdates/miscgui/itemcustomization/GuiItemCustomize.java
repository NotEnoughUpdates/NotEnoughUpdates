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

import com.google.gson.Gson;
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
import net.minecraft.client.gui.Gui;
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
import java.util.Objects;

public class GuiItemCustomize extends GuiScreen {
	private static final ResourceLocation PLUS = new ResourceLocation("notenoughupdates:itemcustomize/plus.png");

	private final ItemStack stack;
	ItemStack customItemStack;
	private final String itemUUID;
	private final GuiElementTextField textFieldRename = new GuiElementTextField("", 138, 20, GuiElementTextField.COLOUR);
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
	DyeMode dyeMode = DyeMode.ANIMATED;
	private int lastTicks = 2;
	boolean supportCustomLeatherColour;
	private String lastCustomItem = "";

	JsonObject animatedDyes = null;
	JsonObject staticDyes = null;
	JsonObject vanillaDyes = null;
	ArrayList<DyeType> dyes = new ArrayList<>();
	boolean repoError = false;

	private GuiElement editor = null;

	private GuiType guiType = GuiType.DEFAULT;

	public GuiItemCustomize(ItemStack stack, String itemUUID) {
		this.stack = stack;
		this.itemUUID = itemUUID;
		this.customItemStack = ItemCustomizationUtils.copy(stack, this);

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
				} else {
					this.animatedDyeTicks = data.animatedDyeTicks;
				}
				this.textFieldTickSpeed.setText("" + this.animatedDyeTicks);
				this.dyeMode = data.dyeMode;
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
			DyeType animatedHeader = new DyeType("Animated Dyes");
			dyes.add(animatedHeader);

			animatedDyes.entrySet().forEach(entry -> {
				String key = entry.getKey();
				JsonArray value = entry.getValue().getAsJsonArray();
				DyeType dyeType = new DyeType(key, value);
				dyes.add(dyeType);
			});
		}

		if (dyesConst.has("static")) {
			staticDyes = dyesConst.get("static").getAsJsonObject();
			DyeType staticHeader = new DyeType("Static Dyes");
			dyes.add(staticHeader);

			staticDyes.entrySet().forEach(entry -> {
				String key = entry.getKey();
				String value = entry.getValue().getAsString();
				DyeType dyeType = new DyeType(key, value);
				dyes.add(dyeType);
			});
		}

		if (dyesConst.has("vanilla")) {
			vanillaDyes = dyesConst.get("vanilla").getAsJsonObject();
			DyeType staticHeader = new DyeType("Vanilla Dyes");
			dyes.add(staticHeader);

			vanillaDyes.entrySet().forEach(entry -> {
				String key = entry.getKey();
				String value = entry.getValue().getAsString();
				DyeType dyeType = new DyeType(key, value);
				dyes.add(dyeType);
			});
		}
	}

	@Override
	public void onGuiClosed() {
		updateData();
	}

	public void updateData() {
		ItemCustomizeManager.ItemData data = new ItemCustomizeManager.ItemData();

		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
		boolean stackHasEffect = stack.hasEffect() && !model.isBuiltInRenderer();

		this.customItemStack = ItemCustomizationUtils.copy(stack, this);
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

		if (this.customLeatherColour != null && (!(customItemStack.getItem() instanceof ItemArmor) ||
			!this.customLeatherColour.equals(ItemCustomizationUtils.getChromaStrFromLeatherColour(this)))) {
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

		data.dyeMode = dyeMode;

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

		ItemCustomizationUtils.renderTextBox(textFieldRename, "§7Enter Custom Name...", xCenter - textFieldRename.getWidth() / 2 - 20, yTop, 138);

		int yTopText = yTop;

		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.help);
		GlStateManager.color(1, 1, 1, 1);
		int helpX = xCenter + textFieldRename.getWidth() / 2 - 5 + 10;
		Utils.drawTexturedRect(helpX, yTop, 20, 20, GL11.GL_LINEAR);

		if (mouseX >= helpX && mouseX <= helpX + 20 && mouseY >= yTop && mouseY <= yTop + 20) {
			ItemCustomizationUtils.customizeColourGuide.set(7, Utils.chromaString("¶z = Chroma"));
			tooltipToDisplay = ItemCustomizationUtils.customizeColourGuide;
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(ItemCustomizationUtils.RESET);
		GlStateManager.color(1, 1, 1, 1);
		int resetX = xCenter + textFieldRename.getWidth() / 2 - 15;
		Utils.drawTexturedRect(resetX, yTop + 5, 10, 11, GL11.GL_LINEAR);

		if (mouseX >= resetX && mouseX <= resetX + 10 && mouseY >= yTop && mouseY <= yTop + 20) {
			tooltipToDisplay = ItemCustomizationUtils.resetGuide;
		}

		ItemCustomizationUtils.renderPresetButtons(xCenter,
			yTop,
			ItemCustomizationUtils.validShareContents("NEUCUSTOMIZE"),
			true,
			"preset"
		);

		yTop += 25;

		renderBigStack(xCenter, yTop);

		yTop += 115;

		RenderUtils.drawFloatingRectDark(xCenter - 90, yTop, 180, 20);

		Minecraft.getMinecraft().fontRendererObj.drawString("Enchant Glint", xCenter - 85, yTop + 6, 0xff8040cc);

		enchantGlintButton.x = xCenter + 90 - 5 - 48;
		enchantGlintButton.y = yTop + 3;
		enchantGlintButton.render();

		yTop += 25;

		enchantGlintCustomColourAnimation.tick();
		if (enchantGlintCustomColourAnimation.getValue() > 0) {
			yTop -= 5;

			int glintColour = ItemCustomizationUtils.getGlintColour(this);

			GlScissorStack.push(0,
				yTop,
				scaledResolution.getScaledWidth(),
				scaledResolution.getScaledHeight(),
				scaledResolution
			);
			GlStateManager.translate(0, enchantGlintCustomColourAnimation.getValue() - 17, 0);

			ItemCustomizationUtils.renderColourBlob(xCenter, yTop, glintColour, "§a§lCustom Glint Colour", true, false);

			GlStateManager.translate(0, -enchantGlintCustomColourAnimation.getValue() + 17, 0);
			GlScissorStack.pop(scaledResolution);

			yTop += enchantGlintCustomColourAnimation.getValue() + 3;
		}

		supportCustomLeatherColour = customItemStack.getItem() instanceof ItemArmor &&
			((ItemArmor) customItemStack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER;

		if (supportCustomLeatherColour) {
			int leatherColour = ItemCustomizationUtils.getLeatherColour(this);

			String text = "§b§lCustom Leather Colour";
			boolean reset = true;

			if (!animatedLeatherColours.isEmpty()) {
				text = "§b§lOverridden by Animated";
				reset = false;
			}

			ItemCustomizationUtils.renderColourBlob(xCenter, yTop, leatherColour, text, reset, false);

			yTop += 20;
		}

		if (!lastCustomItem.equals(textFieldCustomItem.getText())) {
			updateData();
		}
		lastCustomItem = textFieldCustomItem.getText();

		int offset = 200;
		if (!supportCustomLeatherColour) offset -= 20;
		if (!enchantGlint) offset -= 16;

		ItemCustomizationUtils.renderTextBox(textFieldCustomItem, "§7Enter Custom Item ID...", xCenter - textFieldCustomItem.getWidth() / 2 - 10 + 11, yTopText + offset, 180);

		if (supportCustomLeatherColour) {
			yTop += 25;
			ItemCustomizationUtils.renderFooter(xCenter, yTop, guiType);
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

		ItemCustomizationUtils.renderPresetButtons(xCenter,
			yTop,
			ItemCustomizationUtils.validShareContents("NEUCUSTOMIZE"),
			true,
			"preset"
		);
		ItemCustomizationUtils.renderPresetButtons(xCenter,
			yTop + 50,
			ItemCustomizationUtils.validShareContents("NEUANIMATED"),
			!this.animatedLeatherColours.isEmpty(),
			"animated"
		);

		yTop += 14;

		renderBigStack(xCenter, yTop);

		yTop += 115;

		int adjustedY = yTop + pageScroll + 20;

		for (int i = 0; i < animatedLeatherColours.size(); i++) {
			if (adjustedY + 20 * i < yTopStart + 130 || adjustedY + 20 * i >= yTopStart + guiScaleOffset() + 150) {
				continue;
			}

			int leatherColour = ItemCustomizationUtils.getLeatherColour(animatedLeatherColours.get(i));
			ItemCustomizationUtils.renderColourBlob(xCenter, yTop, leatherColour, "§b§lDye Colour " + (i + 1), false, true);

			yTop += 20;
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(PLUS);
		GlStateManager.color(1, 1, 1, 1);
		RenderUtils.drawTexturedRect(xCenter + 90 - 12, yTop + 4, 10, 10, GL11.GL_NEAREST);

		int xOffset = xCenter - textFieldCustomItem.getWidth() / 2 - 10 + 11;
		ItemCustomizationUtils.renderTextBox(textFieldTickSpeed,
			"§7Speed...",
			xOffset,
			yTop,
			45
		);

		if (mouseX >= xOffset && mouseX <= xOffset + textFieldTickSpeed.getWidth() && mouseY >= yTop && mouseY <= yTop + 20) {
			tooltipToDisplay = ItemCustomizationUtils.speedGuide;
		}

		// Button background
		Gui.drawRect(xCenter - 40, yTop + 2, xCenter - 2, yTop + 19, 0x70000000);
		Gui.drawRect(xCenter - 40, yTop + 2, xCenter - 2, yTop + 16, 0xff101016);
		Gui.drawRect(xCenter - 39, yTop + 3, xCenter - 3, yTop + 16, 0xff000000 | 0xff6955);
		Utils.renderShadowedString("§c§lClear", xCenter - 20, yTop + 6, xCenter * 2);

		String dyeModeText = dyeMode == DyeMode.ANIMATED ? "§a§lAnimated" : "§d§lGradient";
		int backgroundColour = dyeMode == DyeMode.ANIMATED ? 0x0aff00 : 0xff00ef;
		Gui.drawRect(xCenter + 10, yTop + 2, xCenter + 68, yTop + 19, 0x70000000);
		Gui.drawRect(xCenter + 10, yTop + 2, xCenter + 68, yTop + 16, 0xff101016);
		Gui.drawRect(xCenter + 11, yTop + 3, xCenter + 67, yTop + 16, 0xff000000 | backgroundColour);
		Utils.renderShadowedString(dyeModeText, xCenter + 39, yTop + 6, xCenter * 2);

		yTop += 25;

		enchantGlintCustomColourAnimation.tick();

		ItemCustomizationUtils.renderFooter(xCenter, yTop, guiType);

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

			ItemCustomizationUtils.renderFooter(xCenter, yTop, guiType);

			renderHeight = yTop - yTopStart;

			super.drawScreen(mouseX, mouseY, partialTicks);
			return;
		}

		renderBigStack(xCenter, yTop);

		yTop += 115;

		int adjustedY = yTop + pageScroll + 20;

		for (int i = 0; i < dyes.size(); i++) {
			if (adjustedY + 20 * i < yTopStart + 130 || adjustedY + 20 * i >= yTopStart + guiScaleOffset() + 150) {
				continue;
			}

			Color color = ItemCustomizationUtils.getColourFromHex(dyes.get(i).colour);

			JsonArray colours = dyes.get(i).colours;
			String itemId = dyes.get(i).itemId;
			String displayName = null;
			ItemStack itemStack = NotEnoughUpdates.INSTANCE.manager
				.createItemResolutionQuery()
				.withKnownInternalName(itemId)
				.resolveToItemStack();
			if (itemStack == null && (colours != null || dyes.get(i).colour != null)) {
				itemStack = NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery().withKnownInternalName(
					"DYE_PURE_YELLOW").resolveToItemStack();
				displayName = itemId;
			}
			if (itemStack != null) {
				if (displayName == null) displayName = itemStack.getDisplayName();
				//Utils.drawItemStack(itemStack, xCenter - 90, yTop);
				GlStateManager.enableDepth();
				GlStateManager.pushMatrix();
				GlStateManager.translate(xCenter - 89, yTop, 0);
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
				int colourFromHex = ItemCustomizationUtils.rgbToInt(ItemCustomizationUtils.getColourFromHex(colourHex));
				ItemCustomizationUtils.renderColourBlob(xCenter, yTop, colourFromHex, displayName, false, false);
			} else {
				int colour = ItemCustomizationUtils.rgbToInt(color);
				ItemCustomizationUtils.renderColourBlob(xCenter, yTop, colour, displayName, false, false);
			}

			yTop += 20;
		}

		int xOffset = xCenter - textFieldCustomItem.getWidth() / 2 - 10 + 11;
		ItemCustomizationUtils.renderTextBox(textFieldTickSpeed,
			"§7Speed...",
			xOffset,
			yTop,
			45
		);

		if (mouseX >= xOffset && mouseX <= xOffset + textFieldTickSpeed.getWidth() && mouseY >= yTop && mouseY <= yTop + 20) {
			tooltipToDisplay = ItemCustomizationUtils.speedGuide;
		}

		yTop += 25;

		enchantGlintCustomColourAnimation.tick();

		ItemCustomizationUtils.renderFooter(xCenter, yTop, guiType);

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

		pageScroll = MathHelper.clamp_int(pageScroll, -((size * 20 - 20) - guiScaleOffset()), 0);
		lastMouseScroll = 0;
	}

	private int guiScaleOffset() {
		//auto 0
		//large 3
		//medium 2
		//small 1
		int scale = Minecraft.getMinecraft().gameSettings.guiScale;
		if (scale == 0) return 80;
		if (scale == 1) return 680;
		if (scale == 2) return 280;
		if (scale == 3) return 220;
		return 80;
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

		if (mouseX >= xCenter - 88 + 198 && mouseX <= xCenter + 88 + 105) {
			Gson gson = new Gson();
			ItemCustomizeManager.ItemData dataForItem = ItemCustomizeManager.getDataForItem(stack);
			if (mouseY >= yTop + 40 && mouseY <= yTop + 40 + 20) {
				if (dataForItem.customItem != null &&
					dataForItem.customItem.equals(dataForItem.defaultItem.replace("minecraft:", ""))) {
					dataForItem.customItem = null;
				}
				ItemCustomizationUtils.shareContents("NEUCUSTOMIZE", gson.toJson(dataForItem));

			} else if (mouseY >= yTop + 10 && mouseY <= yTop + 10 + 20) {
				if (ItemCustomizationUtils.validShareContents("NEUCUSTOMIZE")) {
					String shareJson = ItemCustomizationUtils.getShareFromClipboard("NEUCUSTOMIZE");
					ItemCustomizeManager.ItemData itemData = gson.fromJson(shareJson, ItemCustomizeManager.ItemData.class);
					itemData.defaultItem = dataForItem.defaultItem;
					ItemCustomizeManager.putItemData(itemUUID, itemData);
					NotEnoughUpdates.INSTANCE.openGui = new GuiItemCustomize(stack, itemUUID);
				}
			}
		}

		int resetX = xCenter + textFieldRename.getWidth() / 2 - 15;

		if (mouseX >= resetX && mouseX <= resetX + 10 && mouseY >= yTop + 15 && mouseY <= yTop + 25) {
			ItemCustomizeManager.putItemData(itemUUID, new ItemCustomizeManager.ItemData());
			NotEnoughUpdates.INSTANCE.openGui = new GuiItemCustomize(stack, itemUUID);
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
				editor = new GuiElementColour(mouseX,
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
			mouseY >= belowEnchGlint && mouseY <= belowEnchGlint + 15) {
			if (mouseX >= xCenter + 90 - 12) {
				editor = null;
				customLeatherColour = null;
				updateData();
			} else if (animatedLeatherColours.isEmpty()) {
				editor = new GuiElementColour(mouseX,
					mouseY,
					() -> customLeatherColour == null
						? ItemCustomizationUtils.getChromaStrFromLeatherColour(this)
						: customLeatherColour,
					(colour) -> {
						customLeatherColour = colour;
						updateData();
					},
					() -> editor = null,
					false,
					true
				);
			} else {
				guiType = GuiType.ANIMATED;
			}
		}

		if (supportCustomLeatherColour) {
			float buttonOffset = yTop + 174 + enchantGlintCustomColourAnimation.getValue() + 5 + 45;

			GuiType buttonClicked = ItemCustomizationUtils.getButtonClicked(mouseX, mouseY, guiType, buttonOffset);
			if (buttonClicked != null) {
				guiType = buttonClicked;
				pageScroll = 0;
			}
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
			if (adjustedY + 20 * i < yTop + 130 || adjustedY + 20 * i >= yTop + guiScaleOffset() + 150) {
				continue;
			}
			if (supportCustomLeatherColour && mouseX >= xCenter - 90 && mouseX <= xCenter + 90 &&
				mouseY >= topOffset && mouseY <= topOffset + 15) {
				int finalI = i;
				if (mouseX >= xCenter + 90 - 12) {
					editor = null;
					indexToRemove.add(i);
					updateData();
				} else {

					editor = new GuiElementColour(mouseX, mouseY, () -> {
						String animatedColour = animatedLeatherColours.get(finalI);
						return animatedColour == null
							? ItemCustomizationUtils.getChromaStrFromLeatherColour(this)
							: animatedColour;
					}, (colour) -> {
						animatedLeatherColours.set(finalI, colour);
						updateData();
					}, () -> editor = null, false, true);
				}
			}
			topOffset += 20;
		}

		if (mouseX >= xCenter - 88 + 198 && mouseX <= xCenter + 88 + 105) {
			Gson gson = new Gson();
			ItemCustomizeManager.ItemData dataForItem = ItemCustomizeManager.getDataForItem(stack);
			if (mouseY >= yTop + 0 && mouseY <= yTop + 0 + 20) {
				if (ItemCustomizationUtils.validShareContents("NEUCUSTOMIZE")) {
					String shareJson = ItemCustomizationUtils.getShareFromClipboard("NEUCUSTOMIZE");
					ItemCustomizeManager.ItemData itemData = gson.fromJson(shareJson, ItemCustomizeManager.ItemData.class);
					itemData.defaultItem = dataForItem.defaultItem;
					ItemCustomizeManager.putItemData(itemUUID, itemData);
					NotEnoughUpdates.INSTANCE.openGui = new GuiItemCustomize(stack, itemUUID);
				}
			} else if (mouseY >= yTop + 20 && mouseY <= yTop + 20 + 20) {
				if (dataForItem.customItem != null &&
					dataForItem.customItem.equals(dataForItem.defaultItem.replace("minecraft:", ""))) {
					dataForItem.customItem = null;
				}
				ItemCustomizationUtils.shareContents("NEUCUSTOMIZE", gson.toJson(dataForItem));

			} else if (mouseY >= yTop + 45 && mouseY <= yTop + 45 + 20) {

				if (ItemCustomizationUtils.validShareContents("NEUANIMATED") && this.animatedLeatherColours != null) {
					String shareJson = ItemCustomizationUtils.getShareFromClipboard("NEUANIMATED");
					DyeType dyeType = gson.fromJson(shareJson, DyeType.class);
					if (dyeType.coloursArray != null) {
						this.animatedDyeTicks = dyeType.ticks;
						dataForItem.animatedDyeTicks = dyeType.ticks;
						dataForItem.animatedLeatherColours = Arrays.copyOf(dyeType.coloursArray, dyeType.coloursArray.length);
						this.animatedLeatherColours.clear();
						this.animatedLeatherColours = new ArrayList<>(Arrays.asList(dyeType.coloursArray));
						dataForItem.animatedLeatherColours =
							Arrays.stream(dataForItem.animatedLeatherColours).filter(Objects::nonNull).toArray(String[]::new);
						if (dyeType.dyeMode != null) {
							this.dyeMode = dyeType.dyeMode;
							dataForItem.dyeMode = dyeType.dyeMode;
						} else {
							this.dyeMode = DyeMode.ANIMATED;
							dataForItem.dyeMode = DyeMode.ANIMATED;
						}
						ItemCustomizeManager.putItemData(itemUUID, dataForItem);
						NotEnoughUpdates.INSTANCE.openGui = new GuiItemCustomize(stack, itemUUID);
					}
				}
			} else if (mouseY >= yTop + 72 && mouseY <= yTop + 72 + 20) {
				ItemCustomizationUtils.shareContents(
					"NEUANIMATED",
					gson.toJson(new DyeType(dataForItem.animatedLeatherColours, dataForItem.animatedDyeTicks, dataForItem.dyeMode))
				);
			}
		}

		if (supportCustomLeatherColour && mouseX >= xCenter - 90 && mouseX <= xCenter + 90 &&
			mouseY >= topOffset && mouseY <= topOffset + 15) {
			if (mouseX >= xCenter + 90 - 12) {
				editor = null;
				if (!animatedLeatherColours.isEmpty()) animatedLeatherColours.add(animatedLeatherColours.get(
					animatedLeatherColours.size() - 1));
				else if (customLeatherColour != null) animatedLeatherColours.add(customLeatherColour);
				else animatedLeatherColours.add(ItemCustomizationUtils.getChromaStrFromLeatherColour(this));
				updateData();
				pageScroll = -((animatedLeatherColours.size() * 20 - 20) - guiScaleOffset());
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

		if (mouseX >= xCenter - 23 - 15 && mouseX <= xCenter + 23 / 2 - 15 &&
			mouseY >= topOffset && mouseY <= topOffset + 20) {
			animatedLeatherColours.clear();
			updateData();
		}

		if (mouseX >= xCenter - 23 - 15 + 50 && mouseX <= xCenter + 23 / 2 - 15 + 70 &&
			mouseY >= topOffset && mouseY <= topOffset + 20) {
			dyeMode = dyeMode == DyeMode.ANIMATED ? DyeMode.GRADIENT : DyeMode.ANIMATED;
			updateData();
		}

		GuiType buttonClicked = ItemCustomizationUtils.getButtonClicked(mouseX, mouseY, guiType, bottomOffset);
		if (buttonClicked != null) {
			guiType = buttonClicked;
			pageScroll = 0;
		}

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
			if (adjustedY + 20 * i < yTop + 130 || adjustedY + 20 * i >= yTop + guiScaleOffset() + 150) {
				continue;
			}
			if (supportCustomLeatherColour && mouseX >= xCenter - 90 && mouseX <= xCenter + 90 && mouseY >= topOffset &&
				mouseY <= topOffset + 15) {
				if (dyes.get(i).hasAnimatedColour()) {
					animatedLeatherColours.clear();
					dyeMode = DyeMode.ANIMATED;
					for (JsonElement colour : dyes.get(i).colours) {
						String string = colour.getAsString();
						Color colourFromHex = ItemCustomizationUtils.getColourFromHex(string);
						String special = SpecialColour.special(0, 0, colourFromHex.getRGB());
						animatedLeatherColours.add(special);
					}
				} else if ((dyes.get(i).hasStaticColour())) {
					dyeMode = DyeMode.ANIMATED;
					animatedLeatherColours.clear();
					Color colourFromHex = ItemCustomizationUtils.getColourFromHex(dyes.get(i).colour);
					String special = SpecialColour.special(0, 0, colourFromHex.getRGB());
					customLeatherColour = special;
				}
				updateData();
			}
			topOffset += 20;
		}

		if (mouseX >= xCenter - textFieldTickSpeed.getWidth() / 2 - 70 &&
			mouseX <= xCenter + textFieldTickSpeed.getWidth() / 2 - 70 &&
			mouseY >= topOffset && mouseY <= topOffset + textFieldTickSpeed.getHeight()) {
			textFieldTickSpeed.mouseClicked(mouseX, mouseY, mouseButton);
		} else {
			textFieldTickSpeed.unfocus();
		}

		GuiType buttonClicked = ItemCustomizationUtils.getButtonClicked(mouseX, mouseY, guiType, bottomOffset);
		if (buttonClicked != null) {
			guiType = buttonClicked;
			pageScroll = 0;
		}

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
		this.customItemStack = ItemCustomizationUtils.copy(stack, this);
		Utils.drawItemStack(customItemStack, 0, 0);
		GlStateManager.popMatrix();
	}

}
