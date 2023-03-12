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

package io.github.moulberry.notenoughupdates.itemeditor;

import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuiElementTextField extends GuiElement {
	public static final int SCALE_TEXT = 0b100000;
	public static final int NUM_ONLY = 0b10000;
	public static final int NO_SPACE = 0b01000;
	public static final int FORCE_CAPS = 0b00100;
	public static final int COLOUR = 0b00010;
	public static final int MULTILINE = 0b00001;

	private int searchBarYSize = 20;
	private int searchBarXSize = 350;
	private static final int searchBarPadding = 2;

	private int options = 0;

	private boolean focus = false;

	private int x;
	private int y;

	private String prependText = "";

	private final GuiTextField textField = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj,
		0, 0, 0, 0
	);

	private int customBorderColour = -1;

	public GuiElementTextField(String initialText, int options) {
		textField.setFocused(true);
		textField.setCanLoseFocus(false);
		textField.setMaxStringLength(9999);
		textField.setText(initialText);
		this.options = options;
	}

	public void setMaxStringLength(int len) {
		textField.setMaxStringLength(len);
	}

	public void setCustomBorderColour(int colour) {
		this.customBorderColour = colour;
	}

	public String getText() {
		return textField.getText();
	}

	public void setPrependText(String text) {
		this.prependText = text;
	}

	public void setText(String text) {
		if (textField.getText() == null || !textField.getText().equals(text)) {
			textField.setText(text);
		}
	}

	public void setSize(int searchBarXSize, int searchBarYSize) {
		this.searchBarXSize = searchBarXSize;
		this.searchBarYSize = searchBarYSize;
	}

	@Override
	public String toString() {
		return textField.getText();
	}

	public boolean getFocus() {
		return focus;
	}

	@Override
	public int getHeight() {
		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		int paddingUnscaled = searchBarPadding / scaledresolution.getScaleFactor();

		int numLines = StringUtils.countMatches(textField.getText(), "\n") + 1;
		int extraSize = (searchBarYSize - 8) / 2 + 8;
		int bottomTextBox = searchBarYSize + extraSize * (numLines - 1);

		return bottomTextBox + paddingUnscaled * 2;
	}

	@Override
	public int getWidth() {
		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		int paddingUnscaled = searchBarPadding / scaledresolution.getScaleFactor();

		return searchBarXSize + paddingUnscaled * 2;
	}

	private float getScaleFactor(String str) {
		return Math.min(1, (searchBarXSize - 2) / (float) getStringWidth0(str));
	}

	private boolean isScaling() {
		return (options & SCALE_TEXT) != 0;
	}

	private float getStringWidth(String str) {
		if (isScaling()) {
			return getStringWidth0(str) * getScaleFactor(str);
		} else {
			return getStringWidth0(str);
		}
	}

	private static int getStringWidth0(String str) {
		return Minecraft.getMinecraft().fontRendererObj.getStringWidth(str);
	}

	public int getCursorPos(int mouseX, int mouseY) {
		int xComp = mouseX - x;
		int yComp = mouseY - y;

		int extraSize = (searchBarYSize - 8) / 2 + 8;

		String renderText = prependText + textField.getText();

		int lineNum = Math.round(((yComp - (searchBarYSize - 8) / 2)) / extraSize);

		Pattern patternControlCode = Pattern.compile("(?i)\\u00A7([^\\u00B6])(?!\\u00B6)");
		String text = renderText;
		String textNoColour = renderText;
		if ((options & COLOUR) != 0) {
			while (true) {
				Matcher matcher = patternControlCode.matcher(text);
				if (!matcher.find() || matcher.groupCount() < 1) break;
				String code = matcher.group(1);
				text = matcher.replaceFirst("\u00A7" + code + "\u00B6" + code);
			}
		}
		while (true) {
			Matcher matcher = patternControlCode.matcher(textNoColour);
			if (!matcher.find() || matcher.groupCount() < 1) break;
			String code = matcher.group(1);
			textNoColour = matcher.replaceFirst("\u00B6" + code);
		}

		int currentLine = 0;
		int cursorIndex = 0;
		for (; cursorIndex < textNoColour.length(); cursorIndex++) {
			if (currentLine == lineNum) break;
			if (textNoColour.charAt(cursorIndex) == '\n') {
				currentLine++;
			}
		}

		String textNC = textNoColour.substring(0, cursorIndex);
		int colorCodes = StringUtils.countMatches(textNC, "\u00B6");
		String[] lines = text.substring(cursorIndex + (((options & COLOUR) != 0) ? colorCodes * 2 : 0)).split("\n");
		if (lines.length < 1) {
			return 0;
		}
		String line = lines[0];

		int padding = Math.min(5, searchBarXSize - strLenNoColor(line)) / 2;
		String trimmed = Minecraft.getMinecraft().fontRendererObj.trimStringToWidth(line, xComp - padding);
		int linePos = strLenNoColor(trimmed);
		if (linePos != strLenNoColor(line)) {
			char after = line.charAt(linePos);
			int trimmedWidth = getStringWidth0(trimmed);
			int charWidth = Minecraft.getMinecraft().fontRendererObj.getCharWidth(after);
			if (trimmedWidth + charWidth / 2 < xComp - padding) {
				linePos++;
			}
		}
		cursorIndex += linePos;

		int pre = Utils.cleanColour(prependText).length();
		if (cursorIndex < pre) {
			cursorIndex = 0;
		} else {
			cursorIndex -= pre;
		}

		return cursorIndex;
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 1) {
			textField.setText("");
		} else {
			textField.setCursorPosition(getCursorPos(mouseX, mouseY));
		}
		focus = true;
	}

	public void otherComponentClick() {
		focus = false;
		textField.setSelectionPos(textField.getCursorPosition());
	}

	public int strLenNoColor(String str) {
		return str.replaceAll("(?i)\\u00A7.", "").length();
	}

	public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (focus) {
			textField.setSelectionPos(getCursorPos(mouseX, mouseY));
		}
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if (focus) {
			//allows for pasting formatted text that includes "§"
			if (GuiScreen.isKeyComboCtrlV(keyCode)) {
				textField.setEnabled(false);

				int selectionEnd = textField.getSelectionEnd();
				int cursorPosition = textField.getCursorPosition();

				if (cursorPosition < selectionEnd) {
					//swap selectionEnd and cursorPosition
					selectionEnd = selectionEnd ^ cursorPosition;
					cursorPosition = selectionEnd ^ cursorPosition;
					selectionEnd = selectionEnd ^ cursorPosition;
				}

				String clipboardContent = GuiScreen.getClipboardString();

				StringBuilder stringBuilder = new StringBuilder(getText())
					.replace(selectionEnd, cursorPosition, "")
					.insert(selectionEnd, clipboardContent);

				//writeText removes unwanted chars from the String which includes "§"
				textField.setText(stringBuilder.toString());
				textField.setCursorPosition(selectionEnd + clipboardContent.length());
			} else {
				textField.setEnabled(true);
			}

			if ((options & MULTILINE) != 0) { //Carriage return
				Pattern patternControlCode = Pattern.compile("(?i)\\u00A7([^\\u00B6\n])(?!\\u00B6)");

				String text = textField.getText();
				String textNoColour = textField.getText();
				while (true) {
					Matcher matcher = patternControlCode.matcher(text);
					if (!matcher.find() || matcher.groupCount() < 1) break;
					String code = matcher.group(1);
					text = matcher.replaceFirst("\u00A7" + code + "\u00B6" + code);
				}
				while (true) {
					Matcher matcher = patternControlCode.matcher(textNoColour);
					if (!matcher.find() || matcher.groupCount() < 1) break;
					String code = matcher.group(1);
					textNoColour = matcher.replaceFirst("\u00B6" + code);
				}

				if (keyCode == 28) {
					String before = textField.getText().substring(0, textField.getCursorPosition());
					String after = textField.getText().substring(textField.getCursorPosition());
					int pos = textField.getCursorPosition();
					textField.setText(before + "\n" + after);
					textField.setCursorPosition(pos + 1);
					return;
				} else if (keyCode == 200) { //Up
					String textNCBeforeCursor = textNoColour.substring(0, textField.getSelectionEnd());
					int colorCodes = StringUtils.countMatches(textNCBeforeCursor, "\u00B6");
					String textBeforeCursor = text.substring(0, textField.getSelectionEnd() + colorCodes * 2);

					int numLinesBeforeCursor = StringUtils.countMatches(textBeforeCursor, "\n");

					String[] split = textBeforeCursor.split("\n");
					int textBeforeCursorWidth;
					String lineBefore;
					String thisLineBeforeCursor;
					if (split.length == numLinesBeforeCursor && split.length > 0) {
						textBeforeCursorWidth = 0;
						lineBefore = split[split.length - 1];
						thisLineBeforeCursor = "";
					} else if (split.length > 1) {
						thisLineBeforeCursor = split[split.length - 1];
						lineBefore = split[split.length - 2];
						textBeforeCursorWidth = getStringWidth0(thisLineBeforeCursor);
					} else {
						return;
					}
					String trimmed = Minecraft.getMinecraft().fontRendererObj
						.trimStringToWidth(lineBefore, textBeforeCursorWidth);
					int linePos = strLenNoColor(trimmed);
					if (linePos != strLenNoColor(lineBefore)) {
						char after = lineBefore.charAt(linePos);
						int trimmedWidth = getStringWidth0(trimmed);
						int charWidth = Minecraft.getMinecraft().fontRendererObj.getCharWidth(after);
						if (trimmedWidth + charWidth / 2 < textBeforeCursorWidth) {
							linePos++;
						}
					}
					int newPos = textField.getSelectionEnd() - strLenNoColor(thisLineBeforeCursor)
						- strLenNoColor(lineBefore) - 1 + linePos;

					if (GuiScreen.isShiftKeyDown()) {
						textField.setSelectionPos(newPos);
					} else {
						textField.setCursorPosition(newPos);
					}
				} else if (keyCode == 208) { //Down
					String textNCBeforeCursor = textNoColour.substring(0, textField.getSelectionEnd());
					int colorCodes = StringUtils.countMatches(textNCBeforeCursor, "\u00B6");
					String textBeforeCursor = text.substring(0, textField.getSelectionEnd() + colorCodes * 2);

					int numLinesBeforeCursor = StringUtils.countMatches(textBeforeCursor, "\n");

					String[] split = textBeforeCursor.split("\n");
					String thisLineBeforeCursor;
					int textBeforeCursorWidth;
					if (split.length == numLinesBeforeCursor) {
						thisLineBeforeCursor = "";
						textBeforeCursorWidth = 0;
					} else if (split.length > 0) {
						thisLineBeforeCursor = split[split.length - 1];
						textBeforeCursorWidth = getStringWidth0(thisLineBeforeCursor);
					} else {
						return;
					}

					String[] split2 = textNoColour.split("\n");
					if (split2.length > numLinesBeforeCursor + 1) {
						String lineAfter = split2[numLinesBeforeCursor + 1];
						String trimmed = Minecraft.getMinecraft().fontRendererObj
							.trimStringToWidth(lineAfter, textBeforeCursorWidth);
						int linePos = strLenNoColor(trimmed);
						if (linePos != strLenNoColor(lineAfter)) {
							char after = lineAfter.charAt(linePos);
							int trimmedWidth = getStringWidth0(trimmed);
							int charWidth = Minecraft.getMinecraft().fontRendererObj.getCharWidth(after);
							if (trimmedWidth + charWidth / 2 < textBeforeCursorWidth) {
								linePos++;
							}
						}
						int newPos = textField.getSelectionEnd() - strLenNoColor(thisLineBeforeCursor)
							+ strLenNoColor(split2[numLinesBeforeCursor]) + 1 + linePos;

						if (GuiScreen.isShiftKeyDown()) {
							textField.setSelectionPos(newPos);
						} else {
							textField.setCursorPosition(newPos);
						}
					}
				}
			}

			String old = textField.getText();
			if ((options & FORCE_CAPS) != 0) typedChar = Character.toUpperCase(typedChar);
			if ((options & NO_SPACE) != 0 && typedChar == ' ') return;

			textField.setFocused(true);
			textField.textboxKeyTyped(typedChar, keyCode);

			if ((options & COLOUR) != 0) {
				if (typedChar == '&') {
					int pos = textField.getCursorPosition() - 2;
					if (pos >= 0 && pos < textField.getText().length()) {
						if (textField.getText().charAt(pos) == '&') {
							String before = textField.getText().substring(0, pos);
							String after = "";
							if (pos + 2 < textField.getText().length()) {
								after = textField.getText().substring(pos + 2);
							}
							textField.setText(before + "\u00A7" + after);
							textField.setCursorPosition(pos + 1);
						}
					}
				}
			}

			if ((options & NUM_ONLY) != 0 && textField.getText().matches("[^0-9.]")) textField.setText(old);
		}
	}

	public void render(int x, int y) {
		this.x = x;
		this.y = y;
		drawTextbox(x, y, searchBarXSize, searchBarYSize, searchBarPadding, textField, focus);
	}

	private void drawTextbox(
		int x, int y, int searchBarXSize, int searchBarYSize, int searchBarPadding,
		GuiTextField textField, boolean focus
	) {
		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		String renderText = prependText + textField.getText();

		GlStateManager.disableLighting();

		/*
		 * Search bar
		 */
		int paddingUnscaled = searchBarPadding / scaledresolution.getScaleFactor();
		if (paddingUnscaled < 1) paddingUnscaled = 1;

		int numLines = StringUtils.countMatches(renderText, "\n") + 1;
		int extraSize = (searchBarYSize - 8) / 2 + 8;
		int bottomTextBox = y + searchBarYSize + extraSize * (numLines - 1);

		int borderColour = focus ? Color.GREEN.getRGB() : Color.WHITE.getRGB();
		if (customBorderColour != -1) {
			borderColour = customBorderColour;
		}
		//bar background
		drawRect(x - paddingUnscaled,
			y - paddingUnscaled,
			x + searchBarXSize + paddingUnscaled,
			bottomTextBox + paddingUnscaled, borderColour
		);
		drawRect(x,
			y,
			x + searchBarXSize,
			bottomTextBox, Color.BLACK.getRGB()
		);

		//bar text
		Pattern patternControlCode = Pattern.compile("(?i)\\u00A7([^\\u00B6\n])(?!\\u00B6)");

		String text = renderText;
		String textNoColor = renderText;
		if ((options & COLOUR) != 0) {
			while (true) {
				Matcher matcher = patternControlCode.matcher(text);
				if (!matcher.find() || matcher.groupCount() < 1) break;
				String code = matcher.group(1);
				text = matcher.replaceFirst("\u00A7" + code + "\u00B6" + code);
			}
		}
		while (true) {
			Matcher matcher = patternControlCode.matcher(textNoColor);
			if (!matcher.find() || matcher.groupCount() < 1) break;
			String code = matcher.group(1);
			textNoColor = matcher.replaceFirst("\u00B6" + code);
		}

		int xStartOffset = 5;
		float scale = 1;
		String[] texts = text.split("\n");
		for (int yOffI = 0; yOffI < texts.length; yOffI++) {
			int yOff = yOffI * extraSize;

			if (isScaling() && getStringWidth0(texts[yOffI]) > searchBarXSize - 10) {
				scale = (searchBarXSize - 2) / (float) getStringWidth0(texts[yOffI]);
				if (scale > 1) scale = 1;
				float newLen = getStringWidth0(texts[yOffI]) * scale;
				xStartOffset = (int) ((searchBarXSize - newLen) / 2f);

				Utils.drawStringCenteredScaledMaxWidth(
					texts[yOffI],
					x + searchBarXSize / 2f,
					y + searchBarYSize / 2f + yOff,
					false,
					searchBarXSize - 2,
					Color.WHITE.getRGB()
				);
			} else {
				Minecraft.getMinecraft().fontRendererObj.drawString(Utils.trimToWidth(texts[yOffI], searchBarXSize - 10), x + 5,
					y + (searchBarYSize - 8) / 2 + yOff, Color.WHITE.getRGB()
				);
			}
		}

		if (focus && System.currentTimeMillis() % 1000 > 500) {
			String textNCBeforeCursor = textNoColor.substring(0, textField.getCursorPosition() + prependText.length());
			int colorCodes = StringUtils.countMatches(textNCBeforeCursor, "\u00B6");
			String textBeforeCursor = text.substring(
				0,
				textField.getCursorPosition() + prependText.length() + (((options & COLOUR) != 0) ? colorCodes * 2 : 0)
			);

			int numLinesBeforeCursor = StringUtils.countMatches(textBeforeCursor, "\n");
			int yOff = numLinesBeforeCursor * extraSize;

			String[] split = textBeforeCursor.split("\n");
			int textBeforeCursorWidth;
			if (split.length <= numLinesBeforeCursor || split.length == 0) {
				textBeforeCursorWidth = 0;
			} else {
				textBeforeCursorWidth = (int) (getStringWidth0(split[split.length -
					1]) * scale);
			}
			drawRect(x + xStartOffset + textBeforeCursorWidth,
				y + (searchBarYSize - 8) / 2 - 1 + yOff,
				x + xStartOffset + textBeforeCursorWidth + 1,
				y + (searchBarYSize - 8) / 2 + 9 + yOff, Color.WHITE.getRGB()
			);
		}

		String selectedText = textField.getSelectedText();
		if (!selectedText.isEmpty()) {
			int leftIndex = Math.min(
				textField.getCursorPosition() + prependText.length(),
				textField.getSelectionEnd() + prependText.length()
			);
			int rightIndex = Math.max(
				textField.getCursorPosition() + prependText.length(),
				textField.getSelectionEnd() + prependText.length()
			);

			float texX = 0;
			int texY = 0;
			boolean sectionSignPrev = false;
			boolean bold = false;
			for (int i = 0; i < textNoColor.length(); i++) {
				char c = textNoColor.charAt(i);
				if (sectionSignPrev) {
					if (c != 'k' && c != 'K'
						&& c != 'm' && c != 'M'
						&& c != 'n' && c != 'N'
						&& c != 'o' && c != 'O') {
						bold = c == 'l' || c == 'L';
					}
				}
				sectionSignPrev = c == '\u00B6';

				if (c == '\n') {
					if (i >= leftIndex && i < rightIndex) {
						drawRect(x + xStartOffset + (int) texX,
							y + (searchBarYSize - 8) / 2 - 1 + texY,
							x + xStartOffset + (int) texX + 3,
							y + (searchBarYSize - 8) / 2 + 9 + texY, Color.LIGHT_GRAY.getRGB()
						);
					}

					texX = 0;
					texY += extraSize;
					continue;
				}

				int len = getStringWidth0(String.valueOf(c));
				if (bold) len++;
				if (i >= leftIndex && i < rightIndex) {
					drawRect(x + xStartOffset + (int) texX,
						y + (searchBarYSize - 8) / 2 - 1 + texY,
						x + xStartOffset + (int) (texX + len * scale),
						y + (searchBarYSize - 8) / 2 + 9 + texY, Color.LIGHT_GRAY.getRGB()
					);

					Utils.drawStringScaled(String.valueOf(c),
						x + xStartOffset + texX,
						y + searchBarYSize / 2f - scale * 8 / 2f + texY, false, Color.BLACK.getRGB(), scale
					);
					if (bold) {
						Utils.drawStringScaled(String.valueOf(c),
							x + xStartOffset + texX + 1,
							y + searchBarYSize / 2f - scale * 8 / 2f + texY, false, Color.BLACK.getRGB(), scale
						);
					}
				}

				texX += len * scale;
			}
		}
	}
}
