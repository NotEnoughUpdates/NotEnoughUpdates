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

package io.github.moulberry.notenoughupdates.dungeons;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.GuiElementColour;
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigOption;
import io.github.moulberry.notenoughupdates.core.config.GuiPositionEditorButForTheDungeonMap;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.core.util.render.TextRenderUtils;
import io.github.moulberry.notenoughupdates.itemeditor.GuiElementTextField;
import io.github.moulberry.notenoughupdates.options.seperateSections.DungeonMapConfig;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Matrix4f;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec4b;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static io.github.moulberry.notenoughupdates.util.GuiTextures.button_tex;
import static io.github.moulberry.notenoughupdates.util.GuiTextures.slider_button;
import static io.github.moulberry.notenoughupdates.util.GuiTextures.slider_off_large;
import static io.github.moulberry.notenoughupdates.util.GuiTextures.slider_on_large;

public class GuiDungeonMapEditor extends GuiScreen {
	public static final ResourceLocation BACKGROUND = new ResourceLocation(
		"notenoughupdates:dungeon_map/editor/background.png");
	public static final ResourceLocation BUTTON = new ResourceLocation("notenoughupdates:dungeon_map/editor/button.png");
	private static final DungeonMap demoMap = new DungeonMap();

	private int sizeX;
	private int sizeY;
	private int guiLeft;
	private int guiTop;

	private final List<Button> buttons = new ArrayList<>();

	private final GuiElementTextField blurField = new GuiElementTextField(
		"",
		GuiElementTextField.NUM_ONLY | GuiElementTextField.NO_SPACE
	);
	private GuiElementColour activeColourEditor = null;

	private Field clickedSlider;

	private Runnable closedCallback;

	class Button {
		private final int id;
		private final int x;
		private final int y;
		private String text;
		private Color colour = new Color(-1, true);
		private final Field option;
		private String displayName;
		private String desc;

		public Button(int id, int x, int y, String text) {
			this(id, x, y, text, null);
		}

		public Button(int id, int x, int y, String text, Field option) {
			this.id = id;
			this.x = x;
			this.y = y;
			this.text = text;
			this.option = option;

			if (option != null) {
				ConfigOption optionAnnotation = option.getAnnotation(ConfigOption.class);
				displayName = optionAnnotation.name();
				desc = optionAnnotation.desc();
			}
		}

		public List<String> getTooltip() {
			if (option == null) {
				return null;
			}

			List<String> tooltip = new ArrayList<>();
			tooltip.add(EnumChatFormatting.YELLOW + displayName);
			for (String line : desc.split("\n")) {
				tooltip.add(EnumChatFormatting.AQUA + line);
			}
			return tooltip;
		}

		public void render() {
			if (text == null) return;

			Minecraft.getMinecraft().getTextureManager().bindTexture(BUTTON);
			if (isButtonPressed(id)) {
				GlStateManager.color(colour.getRed() * 0.85f / 255f, colour.getGreen() * 0.85f / 255f,
					colour.getBlue() * 0.85f / 255f, 1
				);
				Utils.drawTexturedRect(guiLeft + x, guiTop + y, 48, 16, 1, 0, 1, 0, GL11.GL_NEAREST);
			} else {
				GlStateManager.color(colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue() / 255f, 1);
				Utils.drawTexturedRect(guiLeft + x, guiTop + y, 48, 16, GL11.GL_NEAREST);
			}

			if (text.length() > 0) {
				Utils.drawStringCenteredScaledMaxWidth(
					text,
					guiLeft + x + 24,
					guiTop + y + 8,
					false,
					39,
					0xFF000000
				);
			}
		}

	}

	public GuiDungeonMapEditor(Runnable closedCallback) {

		if (NotEnoughUpdates.INSTANCE.colourMap == null) {
			try (
				BufferedReader reader = new BufferedReader(new InputStreamReader(Minecraft
					.getMinecraft()
					.getResourceManager()
					.getResource(
						new ResourceLocation("notenoughupdates:maps/F1Full.json"))
					.getInputStream(), StandardCharsets.UTF_8))
			) {
				JsonObject json = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(reader, JsonObject.class);

				NotEnoughUpdates.INSTANCE.colourMap = new Color[128][128];
				for (int x = 0; x < 128; x++) {
					for (int y = 0; y < 128; y++) {
						NotEnoughUpdates.INSTANCE.colourMap[x][y] = new Color(0, 0, 0, 0);
					}
				}
				for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
					int x = Integer.parseInt(entry.getKey().split(":")[0]);
					int y = Integer.parseInt(entry.getKey().split(":")[1]);

					NotEnoughUpdates.INSTANCE.colourMap[x][y] = new Color(entry.getValue().getAsInt(), true);
				}
			} catch (Exception ignored) {
			}
		}

		//Map Border Styles
		buttons.add(new Button(6, 6, 97 + 30, "None"));
		buttons.add(new Button(7, 52, 97 + 30, "Custom"));
		buttons.add(new Button(8, 98, 97 + 30, "Stone"));
		buttons.add(new Button(9, 6, 116 + 30, "Wood"));
		buttons.add(new Button(10, 52, 116 + 30, "Rustic(S)"));
		buttons.add(new Button(11, 98, 116 + 30, "Rustic(C)"));
		buttons.add(new Button(12, 6, 135 + 30, "Fade"));
		buttons.add(new Button(13, 52, 135 + 30, "Ribbons"));
		buttons.add(new Button(14, 98, 135 + 30, "Paper"));
		buttons.add(new Button(15, 6, 154 + 30, "Crimson"));
		buttons.add(new Button(16, 52, 154 + 30, "Ornate"));
		buttons.add(new Button(17, 98, 154 + 30, "Dragon"));

		try {
			//Dungeon Map
			buttons.add(new Button(18, 20 + 139, 36, "Yes/No", DungeonMapConfig.class.getDeclaredField("dmEnable")));
			//Center
			buttons.add(new Button(
				19,
				84 + 139,
				36,
				"Player/Map",
				DungeonMapConfig.class.getDeclaredField("dmCenterPlayer")
			));
			//Rotate
			buttons.add(new Button(
				20,
				20 + 139,
				65,
				"Player/No Rotate",
				DungeonMapConfig.class.getDeclaredField("dmRotatePlayer")
			));
			//Icon Style
			buttons.add(new Button(
				21,
				84 + 139,
				65,
				"Default/Heads",
				DungeonMapConfig.class.getDeclaredField("dmPlayerHeads")
			));
			//Check Orient
			buttons.add(new Button(
				22,
				20 + 139,
				94,
				"Normal/Reorient",
				DungeonMapConfig.class.getDeclaredField("dmOrientCheck")
			));
			//Check Center
			buttons.add(new Button(23, 84 + 139, 94, "Yes/No", DungeonMapConfig.class.getDeclaredField("dmCenterCheck")));
			//Interpolation
			buttons.add(new Button(24, 20 + 139, 123, "Yes/No", DungeonMapConfig.class.getDeclaredField("dmPlayerInterp")));
			//Compatibility
			buttons.add(new Button(
				25,
				84 + 139,
				123,
				"Normal/No SHD/No FB/SHD",
				DungeonMapConfig.class.getDeclaredField("dmCompat")
			));

			//Background
			buttons.add(new Button(26, 20 + 139, 152, "", DungeonMapConfig.class.getDeclaredField("dmBackgroundColour")));
			//Border
			buttons.add(new Button(27, 84 + 139, 152, "", DungeonMapConfig.class.getDeclaredField("dmBorderColour")));

			//Chroma Mode
			buttons.add(new Button(
				28,
				84 + 139,
				181,
				"Normal/Scroll",
				DungeonMapConfig.class.getDeclaredField("dmChromaBorder")
			));
		} catch (Exception e) {
			e.printStackTrace();
		}

		{
			double val = NotEnoughUpdates.INSTANCE.config.dungeonMap.dmBackgroundBlur;
			String strVal;
			if (val % 1 == 0) {
				strVal = Integer.toString((int) val);
			} else {
				strVal = Double.toString(val);
				strVal = strVal.replaceAll("(\\.\\d\\d\\d)(?:\\d)+", "$1");
				strVal = strVal.replaceAll("0+$", "");
			}
			blurField.setText(strVal);
		}
		this.closedCallback = closedCallback;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		ScaledResolution scaledResolution = Utils.pushGuiScale(2);
		this.width = scaledResolution.getScaledWidth();
		this.height = scaledResolution.getScaledHeight();

		mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
		mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

		List<String> tooltipToDisplay = null;
		for (Button button : buttons) {
			if (mouseX >= guiLeft + button.x && mouseX <= guiLeft + button.x + 48 &&
				mouseY >= guiTop + button.y - 13 && mouseY <= guiTop + button.y + 16) {
				if (button.id >= 6 && button.id <= 17) {
					String mapDesc = null;
					String mapCredit = null;
					int id = button.id;
					switch (id) {
						case 6:
							mapDesc = "No Border";
							break;
						case 7:
							mapDesc = "Used by custom Resource Packs";
							break;
						case 8:
							mapDesc = "Simple gray border";
							mapCredit = "Lucy";
							break;
						case 9:
							mapDesc = "Viney wood border";
							mapCredit = "iDevil4Hell";
							break;
						case 10:
							mapDesc = "Steampunk-inspired square border";
							mapCredit = "ThatGravyBoat";
							break;
						case 11:
							mapDesc = "Steampunk-inspired circular border";
							mapCredit = "ThatGravyBoat";
							break;
						case 12:
							mapDesc = "Light fade border";
							mapCredit = "Qwiken";
							break;
						case 13:
							mapDesc = "Simple gray border with red ribbons";
							mapCredit = "Sai";
							break;
						case 14:
							mapDesc = "Paper border";
							mapCredit = "KingJames02st";
							break;
						case 15:
							mapDesc = "Nether-inspired border";
							mapCredit = "DTRW191";
							break;
						case 16:
							mapDesc = "Golden ornate border";
							mapCredit = "iDevil4Hell";
							break;
						case 17:
							mapDesc = "Stone dragon border";
							mapCredit = "ImperiaL";
							break;
					}

					ArrayList<String> tooltip = new ArrayList<>();
					tooltip.add(EnumChatFormatting.YELLOW + "Border Style");
					tooltip.add(EnumChatFormatting.AQUA + "Customize the look of the dungeon border");
					tooltip.add("");
					if (mapDesc != null)
						tooltip.add(EnumChatFormatting.YELLOW + "Set to: " + EnumChatFormatting.AQUA + mapDesc);
					if (mapCredit != null)
						tooltip.add(EnumChatFormatting.YELLOW + "Artist: " + EnumChatFormatting.GOLD + mapCredit);
					tooltipToDisplay = tooltip;
				} else {
					tooltipToDisplay = button.getTooltip();
				}
				break;
			}
		}

		this.sizeX = 431;
		this.sizeY = 237;
		this.guiLeft = (this.width - this.sizeX) / 2;
		this.guiTop = (this.height - this.sizeY) / 2;

		super.drawScreen(mouseX, mouseY, partialTicks);
		drawDefaultBackground();

		blurBackground();
		renderBlurredBackground(width, height, guiLeft + 2, guiTop + 2, sizeX - 4, sizeY - 4);

		Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND);
		GlStateManager.color(1, 1, 1, 1);
		Utils.drawTexturedRect(guiLeft, guiTop, sizeX, sizeY, GL11.GL_NEAREST);

		Minecraft.getMinecraft().fontRendererObj.drawString("NEU Dungeon Map Editor", guiLeft + 8, guiTop + 6, 0xFFB4B4B4);

		Utils.drawStringCenteredScaledMaxWidth("Border Size", guiLeft + 76, guiTop + 30, false, 137, 0xFFB4B4B4);
		Utils.drawStringCenteredScaledMaxWidth("Rooms Size", guiLeft + 76, guiTop + 60, false, 137, 0xFFB4B4B4);
		Utils.drawStringCenteredScaledMaxWidth("Icon Scale", guiLeft + 76, guiTop + 90, false, 137, 0xFFB4B4B4);
		Utils.drawStringCenteredScaledMaxWidth("Border Style", guiLeft + 76, guiTop + 120, false, 137, 0xFFB4B4B4);

		Utils.drawStringCenteredScaledMaxWidth("Dungeon Map", guiLeft + 44 + 139, guiTop + 30, false, 60, 0xFFB4B4B4);
		Utils.drawStringCenteredScaledMaxWidth("Center", guiLeft + 108 + 139, guiTop + 30, false, 60, 0xFFB4B4B4);

		Utils.drawStringCenteredScaledMaxWidth("Rotate", guiLeft + 44 + 139, guiTop + 59, false, 60, 0xFFB4B4B4);
		Utils.drawStringCenteredScaledMaxWidth("Icon Style", guiLeft + 108 + 139, guiTop + 59, false, 60, 0xFFB4B4B4);

		Utils.drawStringCenteredScaledMaxWidth("Check Orient", guiLeft + 44 + 139, guiTop + 88, false, 60, 0xFFB4B4B4);
		Utils.drawStringCenteredScaledMaxWidth("Check Center", guiLeft + 108 + 139, guiTop + 88, false, 60, 0xFFB4B4B4);

		Utils.drawStringCenteredScaledMaxWidth("Interpolation", guiLeft + 44 + 139, guiTop + 117, false, 60, 0xFFB4B4B4);
		Utils.drawStringCenteredScaledMaxWidth("Compatibility", guiLeft + 108 + 139, guiTop + 117, false, 60, 0xFFB4B4B4);

		Utils.drawStringCenteredScaledMaxWidth("Background", guiLeft + 44 + 139, guiTop + 146, false, 60, 0xFFB4B4B4);
		Utils.drawStringCenteredScaledMaxWidth("Border", guiLeft + 108 + 139, guiTop + 146, false, 60, 0xFFB4B4B4);

		Utils.drawStringCenteredScaledMaxWidth("BG Blur", guiLeft + 44 + 139, guiTop + 175, false, 60, 0xFFB4B4B4);
		Utils.drawStringCenteredScaledMaxWidth("Chroma Type", guiLeft + 108 + 139, guiTop + 175, false, 60, 0xFFB4B4B4);

		Utils.drawStringCenteredScaledMaxWidth("Edit Map Position", guiLeft + 76, guiTop + 209, false, 200, 0xFFB4B4B4);

		try {
			drawSlider(DungeonMapConfig.class.getDeclaredField("dmBorderSize"), guiLeft + 76, guiTop + 45);
			drawSlider(DungeonMapConfig.class.getDeclaredField("dmRoomSize"), guiLeft + 76, guiTop + 75);
			drawSlider(DungeonMapConfig.class.getDeclaredField("dmIconScale"), guiLeft + 76, guiTop + 105);
		} catch (Exception e) {
			e.printStackTrace();
		}

		DungeonMapConfig options = NotEnoughUpdates.INSTANCE.config.dungeonMap;
		buttons.get(18 - 6).text = options.dmEnable ? "Enabled" : "Disabled";
		buttons.get(19 - 6).text = options.dmCenterPlayer ? "Player" : "Map";
		buttons.get(20 - 6).text = options.dmRotatePlayer ? "Player" : "Vertical";
		buttons.get(21 - 6).text =
			options.dmPlayerHeads <= 0 ? "Default" : options.dmPlayerHeads == 1 ? "Heads" : "Heads w/ Border";
		buttons.get(22 - 6).text = options.dmOrientCheck ? "Orient" : "Off";
		buttons.get(23 - 6).text = options.dmCenterCheck ? "Center" : "Off";
		buttons.get(24 - 6).text = options.dmPlayerInterp ? "Interp" : "No Interp";
		buttons.get(25 - 6).text = options.dmCompat <= 0 ? "Normal" : options.dmCompat >= 2 ? "No FB/SHD" : "No SHD";

		buttons.get(26 - 6).colour = new Color(SpecialColour.specialToChromaRGB(options.dmBackgroundColour));
		buttons.get(27 - 6).colour = new Color(SpecialColour.specialToChromaRGB(options.dmBorderColour));

		buttons.get(28 - 6).text = options.dmChromaBorder ? "Scroll" : "Normal";

		blurField.setSize(48, 16);
		blurField.render(guiLeft + 20 + 139, guiTop + 181);

		GlStateManager.color(1, 1, 1, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(button_tex);
		RenderUtils.drawTexturedRect(guiLeft + 52, guiTop + 215, 48, 16);
		TextRenderUtils.drawStringCenteredScaledMaxWidth("Edit", guiLeft + 76, guiTop + 223, false, 48, 0xFF303030);

		Map<String, Vec4b> decorations = new HashMap<>();
		Vec4b vec4b = new Vec4b((byte) 3, (byte) (((50) - 64) * 2), (byte) (((40) - 64) * 2), (byte) ((60) * 16 / 360));
		decorations.put(Minecraft.getMinecraft().thePlayer.getName(), vec4b);

		HashSet<String> players = new HashSet<>();
		players.add(Minecraft.getMinecraft().thePlayer.getName());
		GlStateManager.color(1, 1, 1, 1);

		demoMap.renderMap(guiLeft + 357, guiTop + 125, NotEnoughUpdates.INSTANCE.colourMap, decorations, 0,
			players, false, partialTicks
		);

		for (Button button : buttons) {
			button.render();
		}

		if (tooltipToDisplay != null) {
			Utils.drawHoveringText(
				tooltipToDisplay,
				mouseX,
				mouseY,
				width,
				height,
				200
			);
		}

		Utils.pushGuiScale(-1);

		if (activeColourEditor != null) {
			activeColourEditor.render();
		}
	}

	public void drawSlider(Field option, int centerX, int centerY) {
		float value;
		float minValue;
		float maxValue;
		try {
			value = ((Number) option.get(NotEnoughUpdates.INSTANCE.config.dungeonMap)).floatValue();

			ConfigEditorSlider sliderAnnotation = option.getAnnotation(ConfigEditorSlider.class);
			minValue = sliderAnnotation.minValue();
			maxValue = sliderAnnotation.maxValue();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		float sliderAmount = Math.max(0, Math.min(1, (value - minValue) / (maxValue - minValue)));
		int sliderAmountI = (int) (96 * sliderAmount);

		GlStateManager.color(1f, 1f, 1f, 1f);
		Minecraft.getMinecraft().getTextureManager().bindTexture(slider_on_large);
		Utils.drawTexturedRect(centerX - 48, centerY - 8, sliderAmountI, 16,
			0, sliderAmount, 0, 1, GL11.GL_NEAREST
		);

		Minecraft.getMinecraft().getTextureManager().bindTexture(slider_off_large);
		Utils.drawTexturedRect(centerX - 48 + sliderAmountI, centerY - 8, 96 - sliderAmountI, 16,
			sliderAmount, 1, 0, 1, GL11.GL_NEAREST
		);

		Minecraft.getMinecraft().getTextureManager().bindTexture(slider_button);
		Utils.drawTexturedRect(centerX - 48 + sliderAmountI - 4, centerY - 8, 8, 16,
			0, 1, 0, 1, GL11.GL_NEAREST
		);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

		if (clickedSlider != null) {
			float minValue;
			float maxValue;
			try {
				ConfigEditorSlider sliderAnnotation = clickedSlider.getAnnotation(ConfigEditorSlider.class);
				minValue = sliderAnnotation.minValue();
				maxValue = sliderAnnotation.maxValue();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			float sliderAmount = (mouseX - (guiLeft + 76 - 48)) / 96f;
			double val = minValue + (maxValue - minValue) * sliderAmount;
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
				val = Math.round(val);
			}
			float value = (float) Math.max(minValue, Math.min(maxValue, val));
			try {
				if (clickedSlider.getType() == int.class) {
					clickedSlider.set(NotEnoughUpdates.INSTANCE.config.dungeonMap, Math.round(value));
				} else {
					clickedSlider.set(NotEnoughUpdates.INSTANCE.config.dungeonMap, value);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		for (Button button : buttons) {
			if (mouseX >= guiLeft + button.x && mouseX <= guiLeft + button.x + 48 &&
				mouseY >= guiTop + button.y && mouseY <= guiTop + button.y + 16) {
				buttonClicked(mouseX, mouseY, button.id);

				blurField.otherComponentClick();
				return;
			}
		}

		clickedSlider = null;
		if (mouseX >= guiLeft + 76 - 48 && mouseX <= guiLeft + 76 + 48) {
			try {
				if (mouseY > guiTop + 45 - 8 && mouseY < guiTop + 45 + 8) {
					clickedSlider = DungeonMapConfig.class.getDeclaredField("dmBorderSize");
					return;
				} else if (mouseY > guiTop + 75 - 8 && mouseY < guiTop + 75 + 8) {
					clickedSlider = DungeonMapConfig.class.getDeclaredField("dmRoomSize");
					return;
				} else if (mouseY > guiTop + 105 - 8 && mouseY < guiTop + 105 + 8) {
					clickedSlider = DungeonMapConfig.class.getDeclaredField("dmIconScale");
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (mouseY > guiTop + 181 && mouseY < guiTop + 181 + 16) {
			if (mouseX > guiLeft + 20 + 139 && mouseX < guiLeft + 20 + 139 + 48) {
				blurField.mouseClicked(mouseX, mouseY, mouseButton);
				return;
			}
		} else if (mouseY > guiTop + 215 && mouseY < guiTop + 215 + 16) {
			if (mouseX > guiLeft + 52 && mouseX < guiLeft + 100) {
				int size = 80 + Math.round(40 * NotEnoughUpdates.INSTANCE.config.dungeonMap.dmBorderSize);

				Map<String, Vec4b> decorations = new HashMap<>();
				Vec4b vec4b = new Vec4b((byte) 3, (byte) (((50) - 64) * 2), (byte) (((40) - 64) * 2), (byte) ((60) * 16 / 360));
				decorations.put(Minecraft.getMinecraft().thePlayer.getName(), vec4b);

				HashSet<String> players = new HashSet<>();
				players.add(Minecraft.getMinecraft().thePlayer.getName());
				GlStateManager.color(1, 1, 1, 1);
				Runnable runnable = this.closedCallback;
				this.closedCallback = null;
				Minecraft.getMinecraft().displayGuiScreen(new GuiPositionEditorButForTheDungeonMap(
					NotEnoughUpdates.INSTANCE.config.dungeonMap.dmPosition,
					size, size, () -> {
					ScaledResolution scaledResolution = Utils.pushGuiScale(2);
					demoMap.renderMap(
						NotEnoughUpdates.INSTANCE.config.dungeonMap.dmPosition.getAbsX(scaledResolution, size) + size / 2,
						NotEnoughUpdates.INSTANCE.config.dungeonMap.dmPosition.getAbsY(scaledResolution, size) + size / 2,
						NotEnoughUpdates.INSTANCE.colourMap,
						decorations,
						0,
						players,
						false,
						0
					);
					Utils.pushGuiScale(-1);
				}, () -> {}, () -> NotEnoughUpdates.INSTANCE.openGui = new GuiDungeonMapEditor(runnable)
				).withScale(2));
				return;
			}
		}

		blurField.otherComponentClick();
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();

		if (activeColourEditor != null) {
			ScaledResolution realRes = new ScaledResolution(Minecraft.getMinecraft());
			int mouseX = Mouse.getEventX() * realRes.getScaledWidth() / this.mc.displayWidth;
			int mouseY =
				realRes.getScaledHeight() - Mouse.getEventY() * realRes.getScaledHeight() / this.mc.displayHeight - 1;
			activeColourEditor.mouseInput(mouseX, mouseY);
		}
	}

	@Override
	public void handleKeyboardInput() throws IOException {
		super.handleKeyboardInput();

		if (activeColourEditor != null) {
			activeColourEditor.keyboardInput();
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);

		if (blurField.getFocus()) {
			blurField.keyTyped(typedChar, keyCode);

			try {
				blurField.setCustomBorderColour(-1);
				NotEnoughUpdates.INSTANCE.config.dungeonMap.dmBackgroundBlur = Float.parseFloat(blurField.getText());
			} catch (Exception e) {
				blurField.setCustomBorderColour(Color.RED.getRGB());
			}
		}
	}

	private void buttonClicked(int mouseX, int mouseY, int id) {
		DungeonMapConfig options = NotEnoughUpdates.INSTANCE.config.dungeonMap;
		switch (id) {
			case 0:
				options.dmBorderSize = 0;
				break;
			case 1:
				options.dmBorderSize = 1;
				break;
			case 2:
				options.dmBorderSize = 2;
				break;
			case 30:
				options.dmBorderSize = 3;
				break;
			case 3:
				options.dmRoomSize = 0;
				break;
			case 4:
				options.dmRoomSize = 1;
				break;
			case 5:
				options.dmRoomSize = 2;
				break;
			case 29:
				options.dmRoomSize = 3;
				break;
			case 18:
				options.dmEnable = !options.dmEnable;
				break;
			case 19:
				options.dmCenterPlayer = !options.dmCenterPlayer;
				break;
			case 20:
				options.dmRotatePlayer = !options.dmRotatePlayer;
				break;
			case 21:
				options.dmPlayerHeads++;
				if (options.dmPlayerHeads > 2) options.dmPlayerHeads = 0;
				break;
			case 22:
				options.dmOrientCheck = !options.dmOrientCheck;
				break;
			case 23:
				options.dmCenterCheck = !options.dmCenterCheck;
				break;
			case 24:
				options.dmPlayerInterp = !options.dmPlayerInterp;
				break;
			case 25:
				options.dmCompat++;
				if (options.dmCompat > 2) options.dmCompat = 0;
				break;
			case 26: {
				ScaledResolution realRes = new ScaledResolution(Minecraft.getMinecraft());
				mouseX = Mouse.getEventX() * realRes.getScaledWidth() / this.mc.displayWidth;
				mouseY = realRes.getScaledHeight() - Mouse.getEventY() * realRes.getScaledHeight() / this.mc.displayHeight - 1;
				activeColourEditor = new GuiElementColour(mouseX, mouseY, () -> options.dmBackgroundColour,
					(col) -> options.dmBackgroundColour = col, () -> activeColourEditor = null
				);
			}
			break;
			case 27: {
				ScaledResolution realRes = new ScaledResolution(Minecraft.getMinecraft());
				mouseX = Mouse.getEventX() * realRes.getScaledWidth() / this.mc.displayWidth;
				mouseY = realRes.getScaledHeight() - Mouse.getEventY() * realRes.getScaledHeight() / this.mc.displayHeight - 1;
				activeColourEditor = new GuiElementColour(mouseX, mouseY, () -> options.dmBorderColour,
					(col) -> options.dmBorderColour = col, () -> activeColourEditor = null
				);
			}
			break;
			case 28:
				options.dmChromaBorder = !options.dmChromaBorder;
				break;
			default:
				if (id >= 6 && id <= 17) {
					options.dmBorderStyle = id - 6;
					break;
				}
		}
	}

	private boolean isButtonPressed(int id) {
		DungeonMapConfig options = NotEnoughUpdates.INSTANCE.config.dungeonMap;

		if (id >= 0 && id <= 2) {
			return options.dmBorderSize == id;
		} else if (id >= 3 && id <= 5) {
			return options.dmRoomSize == id - 3;
		} else if (id >= 6 && id <= 17) {
			return options.dmBorderStyle == id - 6;
		} else if (id == 29) {
			return options.dmRoomSize == 3;
		} else if (id == 30) {
			return options.dmBorderSize == 3;
		}
		return false;
	}

	Shader blurShaderHorz = null;
	Framebuffer blurOutputHorz = null;
	Shader blurShaderVert = null;
	Framebuffer blurOutputVert = null;

	/**
	 * Creates a projection matrix that projects from our coordinate space [0->width; 0->height] to OpenGL coordinate
	 * space [-1 -> 1; 1 -> -1] (Note: flipped y-axis).
	 * <p>
	 * This is so that we can render to and from the framebuffer in a way that is familiar to us, instead of needing to
	 * apply scales and translations manually.
	 */
	private Matrix4f createProjectionMatrix(int width, int height) {
		Matrix4f projMatrix = new Matrix4f();
		projMatrix.setIdentity();
		projMatrix.m00 = 2.0F / (float) width;
		projMatrix.m11 = 2.0F / (float) (-height);
		projMatrix.m22 = -0.0020001999F;
		projMatrix.m33 = 1.0F;
		projMatrix.m03 = -1.0F;
		projMatrix.m13 = 1.0F;
		projMatrix.m23 = -1.0001999F;
		return projMatrix;
	}

	private double lastBgBlurFactor = -1;

	private void blurBackground() {
		if (!OpenGlHelper.isFramebufferEnabled()) return;

		int width = Minecraft.getMinecraft().displayWidth;
		int height = Minecraft.getMinecraft().displayHeight;

		if (blurOutputHorz == null) {
			blurOutputHorz = new Framebuffer(width, height, false);
			blurOutputHorz.setFramebufferFilter(GL11.GL_NEAREST);
		}
		if (blurOutputVert == null) {
			blurOutputVert = new Framebuffer(width, height, false);
			blurOutputVert.setFramebufferFilter(GL11.GL_NEAREST);
		}
		if (blurOutputHorz.framebufferWidth != width || blurOutputHorz.framebufferHeight != height) {
			blurOutputHorz.createBindFramebuffer(width, height);
			blurShaderHorz.setProjectionMatrix(createProjectionMatrix(width, height));
			Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
		}
		if (blurOutputVert.framebufferWidth != width || blurOutputVert.framebufferHeight != height) {
			blurOutputVert.createBindFramebuffer(width, height);
			blurShaderVert.setProjectionMatrix(createProjectionMatrix(width, height));
			Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
		}

		if (blurShaderHorz == null) {
			try {
				blurShaderHorz = new Shader(Minecraft.getMinecraft().getResourceManager(), "blur",
					Minecraft.getMinecraft().getFramebuffer(), blurOutputHorz
				);
				blurShaderHorz.getShaderManager().getShaderUniform("BlurDir").set(1, 0);
				blurShaderHorz.setProjectionMatrix(createProjectionMatrix(width, height));
			} catch (Exception ignored) {
			}
		}
		if (blurShaderVert == null) {
			try {
				blurShaderVert = new Shader(Minecraft.getMinecraft().getResourceManager(), "blur",
					blurOutputHorz, blurOutputVert
				);
				blurShaderVert.getShaderManager().getShaderUniform("BlurDir").set(0, 1);
				blurShaderVert.setProjectionMatrix(createProjectionMatrix(width, height));
			} catch (Exception ignored) {
			}
		}
		if (blurShaderHorz != null && blurShaderVert != null) {
			if (15 != lastBgBlurFactor) {
				blurShaderHorz.getShaderManager().getShaderUniform("Radius").set((float) 15);
				blurShaderVert.getShaderManager().getShaderUniform("Radius").set((float) 15);
				lastBgBlurFactor = 15;
			}
			GL11.glPushMatrix();
			blurShaderHorz.loadShader(0);
			blurShaderVert.loadShader(0);
			GlStateManager.enableDepth();
			GL11.glPopMatrix();

			Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
		}
	}

	/**
	 * Renders a subsection of the blurred framebuffer on to the corresponding section of the screen.
	 * Essentially, this method will "blur" the background inside the bounds specified by [x->x+blurWidth, y->y+blurHeight]
	 */
	public void renderBlurredBackground(int width, int height, int x, int y, int blurWidth, int blurHeight) {
		if (!OpenGlHelper.isFramebufferEnabled()) return;

		float uMin = x / (float) width;
		float uMax = (x + blurWidth) / (float) width;
		float vMin = (height - y) / (float) height;
		float vMax = (height - y - blurHeight) / (float) height;

		blurOutputVert.bindFramebufferTexture();
		GlStateManager.color(1f, 1f, 1f, 1f);
		Utils.drawTexturedRect(x, y, blurWidth, blurHeight, uMin, uMax, vMin, vMax);
		blurOutputVert.unbindFramebufferTexture();
	}

	@Override
	public void onGuiClosed() {
		if (this.closedCallback != null) {
			this.closedCallback.run();
		}
	}
}
