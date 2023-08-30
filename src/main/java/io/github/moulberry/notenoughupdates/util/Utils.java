/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.TooltipTextScrolling;
import io.github.moulberry.notenoughupdates.miscfeatures.SlotLocking;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Matrix4f;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	private static final LinkedList<Integer> guiScales = new LinkedList<>();
	//Labymod compatibility
	private static final FloatBuffer projectionMatrixOld = BufferUtils.createFloatBuffer(16);
	private static final FloatBuffer modelviewMatrixOld = BufferUtils.createFloatBuffer(16);
	private static final EnumChatFormatting[] rainbow = new EnumChatFormatting[]{
		EnumChatFormatting.RED,
		EnumChatFormatting.GOLD,
		EnumChatFormatting.YELLOW,
		EnumChatFormatting.GREEN,
		EnumChatFormatting.AQUA,
		EnumChatFormatting.LIGHT_PURPLE,
		EnumChatFormatting.DARK_PURPLE
	};
	private static final Pattern CHROMA_REPLACE_PATTERN = Pattern.compile("\u00a7z(.+?)(?=\u00a7|$)");
	private static final char[] c = new char[]{'k', 'm', 'b', 't'};
	private static final LerpingFloat scrollY = new LerpingFloat(0, 100);
	public static boolean hasEffectOverride = false;
	public static boolean disableCustomDungColours = false;
	public static String[] rarityArr = new String[]{
		"COMMON",
		"UNCOMMON",
		"RARE",
		"EPIC",
		"LEGENDARY",
		"MYTHIC",
		"SPECIAL",
		"VERY SPECIAL",
		"SUPREME",
		"^^ THAT ONE IS DIVINE ^^"
	};
	public static String[] rarityArrC = new String[]{
		EnumChatFormatting.WHITE + EnumChatFormatting.BOLD.toString() + "COMMON",
		EnumChatFormatting.GREEN + EnumChatFormatting.BOLD.toString() + "UNCOMMON",
		EnumChatFormatting.BLUE + EnumChatFormatting.BOLD.toString() + "RARE",
		EnumChatFormatting.DARK_PURPLE + EnumChatFormatting.BOLD.toString() + "EPIC",
		EnumChatFormatting.GOLD + EnumChatFormatting.BOLD.toString() + "LEGENDARY",
		EnumChatFormatting.LIGHT_PURPLE + EnumChatFormatting.BOLD.toString() + "MYTHIC",
		EnumChatFormatting.RED + EnumChatFormatting.BOLD.toString() + "SPECIAL",
		EnumChatFormatting.RED + EnumChatFormatting.BOLD.toString() + "VERY SPECIAL",
		EnumChatFormatting.AQUA + EnumChatFormatting.BOLD.toString() + "DIVINE",
	};
	public static final HashMap<String, String> rarityArrMap = new HashMap<String, String>() {{
		put("COMMON", rarityArrC[0]);
		put("UNCOMMON", rarityArrC[1]);
		put("RARE", rarityArrC[2]);
		put("EPIC", rarityArrC[3]);
		put("LEGENDARY", rarityArrC[4]);
		put("MYTHIC", rarityArrC[5]);
		put("SPECIAL", rarityArrC[6]);
		put("VERY SPECIAL", rarityArrC[7]);
		put("DIVINE", rarityArrC[8]);
	}};
	public static Splitter PATH_SPLITTER = Splitter.on(".").omitEmptyStrings().limit(2);
	private static ScaledResolution lastScale = new ScaledResolution(Minecraft.getMinecraft());
	private static long startTime = 0;
	private static DecimalFormat simpleDoubleFormat = new DecimalFormat("0.0");

	public static <T> ArrayList<T> createList(T... values) {
		ArrayList<T> list = new ArrayList<>();
		Collections.addAll(list, values);
		return list;
	}

	public static void resetGuiScale() {
		guiScales.clear();
	}

	public static ScaledResolution peekGuiScale() {
		return lastScale;
	}

	public static ScaledResolution pushGuiScale(int scale) {
		if (guiScales.size() == 0) {
			if (Loader.isModLoaded("labymod")) {
				GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrixOld);
				GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelviewMatrixOld);
			}
		}

		if (scale < 0) {
			if (guiScales.size() > 0) {
				guiScales.pop();
			}
		} else {
			if (scale == 0) {
				guiScales.push(Minecraft.getMinecraft().gameSettings.guiScale);
			} else {
				guiScales.push(scale);
			}
		}

		int newScale = guiScales.size() > 0
			? Math.max(0, guiScales.peek())
			: Minecraft.getMinecraft().gameSettings.guiScale;
		if (newScale == 0) newScale = Minecraft.getMinecraft().gameSettings.guiScale;

		int oldScale = Minecraft.getMinecraft().gameSettings.guiScale;
		Minecraft.getMinecraft().gameSettings.guiScale = newScale;
		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		Minecraft.getMinecraft().gameSettings.guiScale = oldScale;

		if (guiScales.size() > 0) {
			GlStateManager.viewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
			GlStateManager.matrixMode(GL11.GL_PROJECTION);
			GlStateManager.loadIdentity();
			GlStateManager.ortho(0.0D,
				scaledresolution.getScaledWidth_double(),
				scaledresolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D
			);
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
			GlStateManager.loadIdentity();
			GlStateManager.translate(0.0F, 0.0F, -2000.0F);
		} else {
			if (Loader.isModLoaded("labymod") && projectionMatrixOld.limit() > 0 && modelviewMatrixOld.limit() > 0) {
				GlStateManager.matrixMode(GL11.GL_PROJECTION);
				GL11.glLoadMatrix(projectionMatrixOld);
				GlStateManager.matrixMode(GL11.GL_MODELVIEW);
				GL11.glLoadMatrix(modelviewMatrixOld);
			} else {
				GlStateManager.matrixMode(GL11.GL_PROJECTION);
				GlStateManager.loadIdentity();
				GlStateManager.ortho(0.0D,
					scaledresolution.getScaledWidth_double(),
					scaledresolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D
				);
				GlStateManager.matrixMode(GL11.GL_MODELVIEW);
				GlStateManager.loadIdentity();
				GlStateManager.translate(0.0F, 0.0F, -2000.0F);
			}
		}

		lastScale = scaledresolution;
		return scaledresolution;
	}

	public static boolean getHasEffectOverride() {
		return hasEffectOverride;
	}

	public static void drawItemStackWithoutGlint(ItemStack stack, int x, int y) {
		RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();

		disableCustomDungColours = true;
		RenderHelper.enableGUIStandardItemLighting();
		itemRender.zLevel = -145; //Negates the z-offset of the below method.
		hasEffectOverride = true;
		try {
			itemRender.renderItemAndEffectIntoGUI(stack, x, y);
		} catch (Exception e) {
			e.printStackTrace();
		} //Catch exceptions to ensure that hasEffectOverride is set back to false.
		itemRender.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRendererObj, stack, x, y, null);
		hasEffectOverride = false;
		itemRender.zLevel = 0;
		RenderHelper.disableStandardItemLighting();
		disableCustomDungColours = false;
	}

	public static void drawItemStackWithText(ItemStack stack, int x, int y, String text) {
		drawItemStackWithText(stack, x, y, text, false);
	}

	public static void drawItemStackWithText(ItemStack stack, int x, int y, String text, boolean skytilsRarity) {
		if (stack == null) return;
		if (skytilsRarity)
			SkytilsCompat.renderSkytilsRarity(stack, x, y);
		RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();

		disableCustomDungColours = true;
		RenderHelper.enableGUIStandardItemLighting();
		itemRender.zLevel = -145; //Negates the z-offset of the below method.
		itemRender.renderItemAndEffectIntoGUI(stack, x, y);
		itemRender.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRendererObj, stack, x, y, text);
		itemRender.zLevel = 0;
		RenderHelper.disableStandardItemLighting();
		disableCustomDungColours = false;
	}

	public static void drawItemStack(ItemStack stack, int x, int y) {
		drawItemStackWithText(stack, x, y, null);
	}

	public static void drawItemStack(ItemStack stack, int x, int y, boolean skytilsRarity) {
		drawItemStackWithText(stack, x, y, null, skytilsRarity);
	}

	public static String chromaString(String str) {
		return chromaString(str, 0, false);
	}

	public static String chromaStringByColourCode(String str) {
		if (str.contains("\u00a7z")) {
			Matcher matcher = CHROMA_REPLACE_PATTERN.matcher(str);

			StringBuffer sb = new StringBuffer();

			while (matcher.find()) {
				matcher.appendReplacement(
					sb,
					chromaString(matcher.group(1))
						.replace("\\", "\\\\")
						.replace("$", "\\$")
				);
			}
			matcher.appendTail(sb);

			str = sb.toString();
		}
		return str;
	}

	public static String chromaString(String str, float offset, boolean bold) {
		return chromaString(str, offset, bold ? "§l" : "");
	}

	public static String chromaString(String str, float offset, String extraFormatting) {
		str = cleanColour(str);
		boolean bold = extraFormatting.contains("§l");

		long currentTimeMillis = System.currentTimeMillis();
		if (startTime == 0) startTime = currentTimeMillis;

		int chromaSpeed = NotEnoughUpdates.INSTANCE.config.misc.chromaSpeed;
		if (chromaSpeed < 10) chromaSpeed = 10;
		if (chromaSpeed > 5000) chromaSpeed = 5000;

		StringBuilder rainbowText = new StringBuilder();
		int len = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			int index = ((int) (offset + len / 12f - (currentTimeMillis - startTime) / chromaSpeed)) % rainbow.length;
			len += Minecraft.getMinecraft().fontRendererObj.getCharWidth(c);
			if (bold) len++;

			if (index < 0) index += rainbow.length;
			rainbowText.append(rainbow[index]);
			rainbowText.append(extraFormatting);
			rainbowText.append(c);
		}
		return rainbowText.toString();
	}

	public static String shortNumberFormat(double n, int iteration) {
		if (n < 3 && n > 0) {
			return simpleDoubleFormat.format(n);
		}

		if (n < 1000 && iteration == 0) return "" + (int) n;
		double d = ((long) n / 100) / 10.0;
		boolean isRound = (d * 10) % 10 == 0;
		return (d < 1000 ?
			((d > 99.9 || isRound || (!isRound && d > 9.99) ?
				(int) d * 10 / 10 : d + ""
			) + "" + c[iteration])
			: shortNumberFormat(d, iteration + 1));
	}

	public static String trimIgnoreColour(String str) {
		return trimIgnoreColourStart(trimIgnoreColourEnd(str));
	}

	public static String trimIgnoreColourStart(String str) {
		str = str.trim();
		boolean colourCodeLast = false;
		StringBuilder colours = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (colourCodeLast) {
				colours.append('\u00a7').append(c);
				colourCodeLast = false;
				continue;
			}
			if (c == '\u00A7') {
				colourCodeLast = true;
			} else if (c != ' ') {
				return colours.append(str.substring(i)).toString();
			}
		}

		return "";
	}

	public static String trimIgnoreColourEnd(String str) {
		str = str.trim();
		for (int i = str.length() - 1; i >= 0; i--) {
			char c = str.charAt(i);
			if (c == ' ') {
				continue;
			} else if (i > 0 && str.charAt(i - 1) == '\u00a7') {
				i--;
				continue;
			}

			return str.substring(0, i + 1);
		}

		return "";
	}

	public static String trimWhitespaceAndFormatCodes(String str) {
		int startIndex = indexOfFirstNonWhitespaceNonFormatCode(str);
		int endIndex = lastIndexOfNonWhitespaceNonFormatCode(str);
		if (startIndex == -1 || endIndex == -1) return "";
		return str.substring(startIndex, endIndex + 1);
	}

	private static int indexOfFirstNonWhitespaceNonFormatCode(String str) {
		int len = str.length();
		for (int i = 0; i < len; i++) {
			char ch = str.charAt(i);
			if (Character.isWhitespace(ch)) {
				continue;
			} else if (ch == '\u00a7') {
				i++;
				continue;
			}
			return i;
		}
		return -1;
	}

	private static int lastIndexOfNonWhitespaceNonFormatCode(String str) {
		for (int i = str.length() - 1; i >= 0; i--) {
			char ch = str.charAt(i);
			if (Character.isWhitespace(ch) || ch == '\u00a7' || (i > 0 && str.charAt(i - 1) == '\u00a7')) {
				continue;
			}
			return i;
		}

		return -1;
	}

	public static List<String> getRawTooltip(ItemStack stack) {
		List<String> list = Lists.newArrayList();
		String s = stack.getDisplayName();

		if (stack.hasDisplayName()) {
			s = EnumChatFormatting.ITALIC + s;
		}

		s = s + EnumChatFormatting.RESET;

		if (!stack.hasDisplayName() && stack.getItem() == Items.filled_map) {
			s = s + " #" + stack.getItemDamage();
		}

		list.add(s);

		if (stack.hasTagCompound()) {
			if (stack.getTagCompound().hasKey("display", 10)) {
				NBTTagCompound nbttagcompound = stack.getTagCompound().getCompoundTag("display");

				if (nbttagcompound.hasKey("color", 3)) {
					list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("item.dyed"));
				}

				if (nbttagcompound.getTagId("Lore") == 9) {
					NBTTagList nbttaglist1 = nbttagcompound.getTagList("Lore", 8);

					if (nbttaglist1.tagCount() > 0) {
						for (int j1 = 0; j1 < nbttaglist1.tagCount(); ++j1) {
							list.add(
								EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.ITALIC + nbttaglist1.getStringTagAt(j1));
						}
					}
				}
			}
		}

		return list;
	}

	public static String floatToString(float f, int decimals) {
		if (decimals <= 0) {
			return String.valueOf(Math.round(f));
		} else {
			return String.format("%." + decimals + "f", f + 0.00001f);
		}
	}

	public static void drawItemStackLinear(ItemStack stack, int x, int y) {
		if (stack == null) return;

		RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();

		RenderHelper.enableGUIStandardItemLighting();
		itemRender.zLevel = -145; //Negates the z-offset of the below method.

		IBakedModel ibakedmodel = itemRender.getItemModelMesher().getItemModel(stack);
		GlStateManager.pushMatrix();
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
		Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(true, true);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		setupGuiTransform(x, y, ibakedmodel.isGui3d());
		ibakedmodel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(
			ibakedmodel,
			ItemCameraTransforms.TransformType.GUI
		);
		itemRender.renderItem(stack, ibakedmodel);
		GlStateManager.disableAlpha();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
		Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();

		itemRender.renderItemOverlays(Minecraft.getMinecraft().fontRendererObj, stack, x, y);
		itemRender.zLevel = 0;
		RenderHelper.disableStandardItemLighting();
	}

	private static void setupGuiTransform(int xPosition, int yPosition, boolean isGui3d) {
		GlStateManager.translate((float) xPosition, (float) yPosition, 5);
		GlStateManager.translate(8.0F, 8.0F, 0.0F);
		GlStateManager.scale(1.0F, 1.0F, -1.0F);
		GlStateManager.scale(0.5F, 0.5F, 0.5F);

		if (isGui3d) {
			GlStateManager.scale(40.0F, 40.0F, 40.0F);
			GlStateManager.rotate(210.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.enableLighting();
		} else {
			GlStateManager.scale(64.0F, 64.0F, 64.0F);
			GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.disableLighting();
		}
	}

	public static Method getMethod(Class<?> clazz, Class<?>[] params, String... methodNames) {
		for (String methodName : methodNames) {
			try {
				return clazz.getDeclaredMethod(methodName, params);
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	public static Object getField(Class<?> clazz, Object o, String... fieldNames) {
		Field field = null;
		for (String fieldName : fieldNames) {
			try {
				field = clazz.getDeclaredField(fieldName);
				break;
			} catch (Exception ignored) {
			}
		}
		if (field != null) {
			field.setAccessible(true);
			try {
				return field.get(o);
			} catch (IllegalAccessException ignored) {
			}
		}
		return null;
	}

	public static Slot getSlotUnderMouse(GuiContainer container) {
		Slot slot = (Slot) getField(GuiContainer.class, container, "theSlot", "field_147006_u");
		if (slot == null) {
			slot = SlotLocking.getInstance().getRealSlot();
		}
		return slot;
	}

	public static void drawTexturedRect(float x, float y, float width, float height) {
		drawTexturedRect(x, y, width, height, 0, 1, 0, 1);
	}

	public static void drawPvSideButton(
		int yIndex,
		ItemStack itemStack,
		boolean pressed,
		GuiProfileViewer guiProfileViewer
	) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);

		int x = guiLeft - 28;
		int y = guiTop + yIndex * 28;

		float uMin = 193 / 256f;
		float uMax = 223 / 256f;
		float vMin = 200 / 256f;
		float vMax = 228 / 256f;
		if (pressed) {
			uMin = 224 / 256f;
			uMax = 1f;

			if (yIndex != 0) {
				vMin = 228 / 256f;
				vMax = 1f;
			}

			guiProfileViewer.renderBlurredBackground(
				guiProfileViewer.width,
				guiProfileViewer.height,
				x + 2,
				y + 2,
				30,
				28 - 4
			);
		} else {
			guiProfileViewer.renderBlurredBackground(
				guiProfileViewer.width,
				guiProfileViewer.height,
				x + 2,
				y + 2,
				28 - 2,
				28 - 4
			);
		}

		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);

		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiProfileViewer.pv_elements);

		drawTexturedRect(x, y, pressed ? 32 : 28, 28, uMin, uMax, vMin, vMax, GL11.GL_NEAREST);

		GlStateManager.enableDepth();
		drawItemStack(itemStack, x + 8, y + 7);
	}

	public static void drawTexturedRect(float x, float y, float width, float height, int filter) {
		drawTexturedRect(x, y, width, height, 0, 1, 0, 1, filter);
	}

	public static void drawTexturedRect(
		float x,
		float y,
		float width,
		float height,
		float uMin,
		float uMax,
		float vMin,
		float vMax
	) {
		drawTexturedRect(x, y, width, height, uMin, uMax, vMin, vMax, GL11.GL_LINEAR);
	}

	public static String cleanColour(String in) {
		return in.replaceAll("(?i)\\u00A7.", "");
	}

	public static String cleanColourNotModifiers(String in) {
		return in.replaceAll("(?i)\\u00A7[0-9a-f]", "");
	}

	public static String fixBrokenAPIColour(String in) {
		return in.replaceAll("(?i)\\u00C2(\\u00A7.)", "$1");
	}

	public static String prettyCase(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
	}

	public static String getRarityFromInt(int rarity) {
		if (rarity < 0 || rarity >= rarityArr.length) {
			return rarityArr[0];
		}
		return rarityArr[rarity];
	}

	public static int checkItemTypePet(List<String> lore) {
		for (int i = lore.size() - 1; i >= 0; i--) {
			String line = cleanColour(lore.get(i));
			for (int i1 = 0; i1 < rarityArr.length; i1++) {
				if (line.equals(rarityArr[i1])) {
					return i1;
				}
			}
		}
		return -1;
	}

	public static int checkItemType(JsonArray lore, boolean contains, String... typeMatches) {
		for (int i = lore.size() - 1; i >= 0; i--) {
			String line = lore.get(i).getAsString();

			int returnType = checkItemType(line, contains, typeMatches);
			if (returnType != -1) {
				return returnType;
			}
		}
		return -1;
	}

	public static int checkItemType(String[] lore, boolean contains, String... typeMatches) {
		for (int i = lore.length - 1; i >= 0; i--) {
			String line = lore[i];

			int returnType = checkItemType(line, contains, typeMatches);
			if (returnType != -1) {
				return returnType;
			}
		}
		return -1;
	}

	public static int checkItemType(List<String> lore, boolean contains, String... typeMatches) {
		for (int i = lore.size() - 1; i >= 0; i--) {
			String line = lore.get(i);

			int returnType = checkItemType(line, contains, typeMatches);
			if (returnType != -1) {
				return returnType;
			}
		}
		return -1;
	}

	private static int checkItemType(String line, boolean contains, String... typeMatches) {
		for (String rarity : rarityArr) {
			for (int j = 0; j < typeMatches.length; j++) {
				if (contains) {
					if (line.trim().contains(rarity + " " + typeMatches[j])) {
						return j;
					} else if (line.trim().contains(rarity + " DUNGEON " + typeMatches[j])) {
						return j;
					}
				} else {
					if (line.trim().endsWith(rarity + " " + typeMatches[j])) {
						return j;
					} else if (line.trim().endsWith(rarity + " DUNGEON " + typeMatches[j])) {
						return j;
					}
				}
			}
		}
		return -1;
	}

	public static float round(float value, int precision) {
		int scale = (int) Math.pow(10, precision);
		return (float) Math.round(value * scale) / scale;
	}

	public static int roundToNearestInt(double value) {
		return (int) Math.round(value);
	}

	// Parses Roman numerals, allowing for single character irregular subtractive notation (e.g. IL is 49, IIL is invalid)
	public static int parseRomanNumeral(String input) {
		int prevVal = 0;
		int total = 0;
		for (int i = input.length() - 1; i >= 0; i--) {
			int val;
			char ch = input.charAt(i);
			switch (ch) {
				case 'I':
					val = 1;
					break;
				case 'V':
					val = 5;
					break;
				case 'X':
					val = 10;
					break;
				case 'L':
					val = 50;
					break;
				case 'C':
					val = 100;
					break;
				case 'D':
					val = 500;
					break;
				case 'M':
					val = 1000;
					break;
				default:
					throw new IllegalArgumentException("Invalid Roman Numeral Character: " + ch);
			}
			if (val < prevVal) val = -val;
			total += val;
			prevVal = val;
		}

		return total;
	}

	public static int parseIntOrRomanNumeral(String input) {
		// 0 through 9, '-', and '+' come before 'A' in ANSI, UTF8, and UTF16 character sets
		//
		if (input.charAt(0) < 'A') {
			return Integer.parseInt(input);
		}

		return parseRomanNumeral(input);
	}

	public static void playPressSound() {
		playSound(new ResourceLocation("gui.button.press"), true);
	}

	public static void playSound(ResourceLocation sound, boolean gui) {
		if (NotEnoughUpdates.INSTANCE.config.misc.guiButtonClicks || !gui) {
			Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(sound, 1.0F));
		}
	}

	public static String cleanDuplicateColourCodes(String line) {
		StringBuilder sb = new StringBuilder();
		char currentColourCode = 'r';
		boolean sectionSymbolLast = false;
		for (char c : line.toCharArray()) {
			if ((int) c > 50000) continue;

			if (c == '\u00a7') {
				sectionSymbolLast = true;
			} else {
				if (sectionSymbolLast) {
					if (currentColourCode != c) {
						sb.append('\u00a7');
						sb.append(c);
						currentColourCode = c;
					}
					sectionSymbolLast = false;
				} else {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}

	public static void drawTexturedRect(
		float x,
		float y,
		float width,
		float height,
		float uMin,
		float uMax,
		float vMin,
		float vMax,
		int filter
	) {
		GlStateManager.enableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(
			GL11.GL_SRC_ALPHA,
			GL11.GL_ONE_MINUS_SRC_ALPHA,
			GL11.GL_ONE,
			GL11.GL_ONE_MINUS_SRC_ALPHA
		);
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer
			.pos(x, y + height, 0.0D)
			.tex(uMin, vMax).endVertex();
		worldrenderer
			.pos(x + width, y + height, 0.0D)
			.tex(uMax, vMax).endVertex();
		worldrenderer
			.pos(x + width, y, 0.0D)
			.tex(uMax, vMin).endVertex();
		worldrenderer
			.pos(x, y, 0.0D)
			.tex(uMin, vMin).endVertex();
		tessellator.draw();

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

		GlStateManager.disableBlend();
	}

	public static void drawTexturedRectNoBlend(
		float x,
		float y,
		float width,
		float height,
		float uMin,
		float uMax,
		float vMin,
		float vMax,
		int filter
	) {
		GlStateManager.enableTexture2D();

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer
			.pos(x, y + height, 0.0D)
			.tex(uMin, vMax).endVertex();
		worldrenderer
			.pos(x + width, y + height, 0.0D)
			.tex(uMax, vMax).endVertex();
		worldrenderer
			.pos(x + width, y, 0.0D)
			.tex(uMax, vMin).endVertex();
		worldrenderer
			.pos(x, y, 0.0D)
			.tex(uMin, vMin).endVertex();
		tessellator.draw();

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
	}

	public static ItemStack createItemStack(Item item, String displayName, String... lore) {
		return createItemStack(item, displayName, 0, lore);
	}

	public static ItemStack createItemStackArray(Item item, String displayName, String[] lore) {
		return createItemStack(item, displayName, 0, lore);
	}

	public static ItemStack createItemStack(Block item, String displayName, String... lore) {
		return createItemStack(Item.getItemFromBlock(item), displayName, lore);
	}

	public static ItemStack createItemStack(Item item, String displayName, int damage, String... lore) {
		return createItemStack(item, displayName, damage, 1, lore);
	}

	public static ItemStack createItemStack(Item item, String displayName, int damage, int amount, String... lore) {
		ItemStack stack = new ItemStack(item, amount, damage);
		NBTTagCompound tag = new NBTTagCompound();
		addNameAndLore(tag, displayName, lore);
		tag.setInteger("HideFlags", 254);

		stack.setTagCompound(tag);

		return stack;
	}

	private static void addNameAndLore(NBTTagCompound tag, String displayName, String[] lore) {
		NBTTagCompound display = new NBTTagCompound();

		display.setString("Name", displayName);

		if (lore != null) {
			NBTTagList tagLore = new NBTTagList();
			for (String line : lore) {
				tagLore.appendTag(new NBTTagString(line));
			}
			display.setTag("Lore", tagLore);
		}

		tag.setTag("display", display);
	}

	public static ItemStack editItemStackInfo(
		ItemStack itemStack,
		String displayName,
		boolean disableNeuToolTips,
		String... lore
	) {
		NBTTagCompound tag = itemStack.getTagCompound();
		if (tag == null)
			tag = new NBTTagCompound();
		NBTTagCompound display = tag.getCompoundTag("display");
		NBTTagList Lore = new NBTTagList();

		for (String line : lore) {
			Lore.appendTag(new NBTTagString(line));
		}

		display.setString("Name", displayName);
		display.setTag("Lore", Lore);

		tag.setTag("display", display);
		tag.setInteger("HideFlags", 254);
		if (disableNeuToolTips) {
			tag.setBoolean("disableNeuTooltip", true);
		}

		itemStack.setTagCompound(tag);

		return itemStack;
	}

	public static ItemStack createSkull(String displayName, String uuid, String value) {
		return createSkull(displayName, uuid, value, null);
	}

	public static ItemStack createSkull(String displayName, String uuid, String value, String[] lore) {
		ItemStack render = new ItemStack(Items.skull, 1, 3);
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagCompound skullOwner = new NBTTagCompound();
		NBTTagCompound properties = new NBTTagCompound();
		NBTTagList textures = new NBTTagList();
		NBTTagCompound textures_0 = new NBTTagCompound();

		skullOwner.setString("Id", uuid);
		skullOwner.setString("Name", uuid);

		textures_0.setString("Value", value);
		textures.appendTag(textures_0);

		addNameAndLore(tag, displayName, lore);

		properties.setTag("textures", textures);
		skullOwner.setTag("Properties", properties);
		tag.setTag("SkullOwner", skullOwner);
		render.setTagCompound(tag);
		return render;
	}

	public static void drawStringF(String str, float x, float y, boolean shadow, int colour) {
		drawStringF(str, Minecraft.getMinecraft().fontRendererObj, x, y, shadow, colour);
	}

	@Deprecated
	public static void drawStringF(String str, FontRenderer fr, float x, float y, boolean shadow, int colour) {
		fr.drawString(str, x, y, colour, shadow);
	}

	public static int getCharVertLen(char c) {
		if ("acegmnopqrsuvwxyz".indexOf(c) >= 0) {
			return 5;
		} else {
			return 7;
		}
	}

	public static float getVerticalHeight(String str) {
		str = cleanColour(str);
		float height = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			int charHeight = getCharVertLen(c);
			height += charHeight + 1.5f;
		}
		return height;
	}

	public static void drawStringVertical(String str, float x, float y, boolean shadow, int colour) {
		drawStringVertical(str, Minecraft.getMinecraft().fontRendererObj, x, y, shadow, colour);
	}

	@Deprecated
	public static void drawStringVertical(String str, FontRenderer fr, float x, float y, boolean shadow, int colour) {
		String format = FontRenderer.getFormatFromString(str);
		str = cleanColour(str);
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);

			int charHeight = getCharVertLen(c);
			int charWidth = fr.getCharWidth(c);
			fr.drawString(format + c, x + (5 - charWidth) / 2f, y - 7 + charHeight, colour, shadow);

			y += charHeight + 1.5f;
		}
	}

	public static void renderShadowedString(String str, float x, float y, int maxLength) {
		int strLen = Minecraft.getMinecraft().fontRendererObj.getStringWidth(str);
		float factor;
		if (maxLength < 0) {
			factor = 1;
		} else {
			factor = maxLength / (float) strLen;
			factor = Math.min(1, factor);
		}

		for (int xOff = -2; xOff <= 2; xOff++) {
			for (int yOff = -2; yOff <= 2; yOff++) {
				if (Math.abs(xOff) != Math.abs(yOff)) {
					drawStringCenteredScaledMaxWidth(
						cleanColourNotModifiers(str),
						x + xOff / 2f * factor,
						y + 4 + yOff / 2f * factor,
						false,
						maxLength,
						new Color(0, 0, 0, 200 / Math.max(Math.abs(xOff), Math.abs(yOff))).getRGB()
					);
				}
			}
		}

		GlStateManager.color(1, 1, 1, 1);
		drawStringCenteredScaledMaxWidth(str, x, y + 4, false, maxLength, 421075);
	}

	public static void renderAlignedString(String first, String second, float x, float y, int length) {
		FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRendererObj;
		if (fontRendererObj.getStringWidth(first + " " + second) >= length) {
			renderShadowedString(first + " " + second, x + length / 2f, y, length);
		} else {
			for (int xOff = -2; xOff <= 2; xOff++) {
				for (int yOff = -2; yOff <= 2; yOff++) {
					if (Math.abs(xOff) != Math.abs(yOff)) {
						fontRendererObj.drawString(cleanColourNotModifiers(first),
							x + xOff / 2f, y + yOff / 2f,
							new Color(0, 0, 0, 200 / Math.max(Math.abs(xOff), Math.abs(yOff))).getRGB(), false
						);
					}
				}
			}

			int secondLen = fontRendererObj.getStringWidth(second);
			GlStateManager.color(1, 1, 1, 1);
			fontRendererObj.drawString(first, x, y, 4210752, false);
			for (int xOff = -2; xOff <= 2; xOff++) {
				for (int yOff = -2; yOff <= 2; yOff++) {
					if (Math.abs(xOff) != Math.abs(yOff)) {
						fontRendererObj.drawString(cleanColourNotModifiers(second),
							x + length - secondLen + xOff / 2f, y + yOff / 2f,
							new Color(0, 0, 0, 200 / Math.max(Math.abs(xOff), Math.abs(yOff))).getRGB(), false
						);
					}
				}
			}

			GlStateManager.color(1, 1, 1, 1);
			fontRendererObj.drawString(second, x + length - secondLen, y, 4210752, false);
		}
	}

	public static void drawStringScaledMaxWidth(
		String str,
		float x,
		float y,
		boolean shadow,
		int len,
		int colour
	) {
		drawStringScaledMaxWidth(str, Minecraft.getMinecraft().fontRendererObj, x, y, shadow, len, colour);
	}

	@Deprecated
	public static void drawStringScaledMaxWidth(
		String str,
		FontRenderer fr,
		float x,
		float y,
		boolean shadow,
		int len,
		int colour
	) {
		int strLen = fr.getStringWidth(str);
		float factor = len / (float) strLen;
		factor = Math.min(1, factor);

		drawStringScaled(str, x, y, shadow, colour, factor);
	}

	public static void drawStringCentered(String str, float x, float y, boolean shadow, int colour) {
		drawStringCentered(str, Minecraft.getMinecraft().fontRendererObj, x, y, shadow, colour);
	}

	@Deprecated
	public static void drawStringCentered(String str, FontRenderer fr, float x, float y, boolean shadow, int colour) {
		int strLen = fr.getStringWidth(str);

		float x2 = x - strLen / 2f;
		float y2 = y - fr.FONT_HEIGHT / 2f;

		GL11.glTranslatef(x2, y2, 0);
		fr.drawString(str, 0, 0, colour, shadow);
		GL11.glTranslatef(-x2, -y2, 0);
	}

	public static void drawStringScaled(
		String str,
		float x,
		float y,
		boolean shadow,
		int colour,
		float factor
	) {
		drawStringScaled(str, Minecraft.getMinecraft().fontRendererObj, x, y, shadow, colour, factor);
	}

	@Deprecated
	public static void drawStringScaled(
		String str,
		FontRenderer fr,
		float x,
		float y,
		boolean shadow,
		int colour,
		float factor
	) {
		GlStateManager.scale(factor, factor, 1);
		fr.drawString(str, x / factor, y / factor, colour, shadow);
		GlStateManager.scale(1 / factor, 1 / factor, 1);
	}

	public static void drawStringRightAligned(
		String str,
		FontRenderer fr,
		float x,
		float y,
		boolean shadow,
		int colour,
		float factor
	) {
		drawStringScaled(str, x - fr.getStringWidth(str) * factor, y, shadow, colour, factor);
	}

	public static void drawStringScaledMax(
		String str,
		float x,
		float y,
		boolean shadow,
		int colour,
		float factor,
		int len
	) {
		drawStringScaledMax(str, Minecraft.getMinecraft().fontRendererObj, x, y, shadow, colour, factor, len);
	}

	@Deprecated
	public static void drawStringScaledMax(
		String str,
		FontRenderer fr,
		float x,
		float y,
		boolean shadow,
		int colour,
		float factor,
		int len
	) {
		int strLen = fr.getStringWidth(str);
		float f = len / (float) strLen;
		factor = Math.min(factor, f);

		GlStateManager.scale(factor, factor, 1);
		fr.drawString(str, x / factor, y / factor, colour, shadow);
		GlStateManager.scale(1 / factor, 1 / factor, 1);
	}

	public static void drawStringCenteredScaledMaxWidth(
		String str,
		float x,
		float y,
		boolean shadow,
		int len,
		int colour
	) {
		drawStringCenteredScaledMaxWidth(str, Minecraft.getMinecraft().fontRendererObj, x, y, shadow, len, colour);
	}

	@Deprecated
	public static void drawStringCenteredScaledMaxWidth(
		String str,
		FontRenderer fr,
		float x,
		float y,
		boolean shadow,
		int len,
		int colour
	) {
		int strLen = fr.getStringWidth(str);
		float factor = len / (float) strLen;
		factor = Math.min(1, factor);
		int newLen = Math.min(strLen, len);

		float fontHeight = 8 * factor;

		drawStringScaled(str, x - newLen / 2, y - fontHeight / 2, shadow, colour, factor);
	}

	public static Matrix4f createProjectionMatrix(int width, int height) {
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

	public static void drawStringCenteredScaled(
		String str,
		float x, float y,
		boolean shadow,
		int len,
		int colour
	) {
		int strLen = Minecraft.getMinecraft().fontRendererObj.getStringWidth(str);
		float factor = len / (float) strLen;
		float fontHeight = 8 * factor;

		drawStringScaled(
			str,
			x - len / 2, y - fontHeight / 2,
			shadow,
			colour,
			factor
		);
	}

	public static void drawStringCenteredScaled(
		String str,
		float x, float y,
		boolean shadow,
		float factor
	) {
		drawStringCenteredScaled(str, Minecraft.getMinecraft().fontRendererObj, x, y, shadow, factor);
	}

	@Deprecated
	public static void drawStringCenteredScaled(
		String str,
		FontRenderer fr,
		float x,
		float y,
		boolean shadow,
		float factor
	) {
		int strLen = fr.getStringWidth(str);

		float x2 = x - strLen / 2f;
		float y2 = y - fr.FONT_HEIGHT / 2f;

		drawStringScaled(str, x2, y2, shadow, 0, factor);
	}

	public static void drawStringCenteredYScaled(
		String str,
		FontRenderer fr,
		float x,
		float y,
		boolean shadow,
		int len,
		int colour
	) {
		int strLen = fr.getStringWidth(str);
		float factor = len / (float) strLen;
		float fontHeight = 8 * factor;

		drawStringScaled(str, x, y - fontHeight / 2, shadow, colour, factor);
	}

	public static void drawStringCenteredYScaledMaxWidth(
		String str,
		FontRenderer fr,
		float x,
		float y,
		boolean shadow,
		int len,
		int colour
	) {
		int strLen = fr.getStringWidth(str);
		float factor = len / (float) strLen;
		factor = Math.min(1, factor);
		float fontHeight = 8 * factor;

		drawStringScaled(str, x, y - fontHeight / 2, shadow, colour, factor);
	}

	public static int renderStringTrimWidth(
		String str,
		boolean shadow,
		int x,
		int y,
		int len,
		int colour,
		int maxLines
	) {
		return renderStringTrimWidth(str, shadow, x, y, len, colour, maxLines, 1);
	}

	public static int renderStringTrimWidth(
		String str,
		boolean shadow,
		int x,
		int y,
		int len,
		int colour,
		int maxLines,
		float scale
	) {
		len = (int) (len / scale);

		int yOff = 0;
		String excess;
		String trimmed = trimToWidth(str, len);

		String colourCodes = "";
		Pattern pattern = Pattern.compile("\\u00A7.");
		Matcher matcher = pattern.matcher(trimmed);
		while (matcher.find()) {
			colourCodes += matcher.group();
		}

		boolean firstLine = true;
		int trimmedCharacters = trimmed.length();
		int lines = 0;
		while ((lines++ < maxLines) || maxLines < 0) {
			if (trimmed.length() == str.length()) {
				drawStringScaled(trimmed, x, y + yOff, shadow, colour, scale);
				break;
			} else if (trimmed.isEmpty()) {
				yOff -= 12 * scale;
				break;
			} else {
				if (firstLine) {
					drawStringScaled(trimmed, x, y + yOff, shadow, colour, scale);
					firstLine = false;
				} else {
					if (trimmed.startsWith(" ")) {
						trimmed = trimmed.substring(1);
					}
					drawStringScaled(colourCodes + trimmed, x, y + yOff, shadow, colour, scale);
				}

				excess = str.substring(trimmedCharacters);
				trimmed = trimToWidth(excess, len);
				trimmedCharacters += trimmed.length();
				yOff += 12 * scale;
			}
		}
		return yOff;
	}

	public static String trimToWidth(String str, int len) {
		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
		String trim = fr.trimStringToWidth(str, len);

		if (str.length() != trim.length() && !trim.endsWith(" ")) {
			char next = str.charAt(trim.length());
			if (next != ' ') {
				String[] split = trim.split(" ");
				String last = split[split.length - 1];
				if (last.length() < 8) {
					trim = trim.substring(0, trim.length() - last.length());
				}
			}
		}

		return trim;
	}

	public static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
		float f = (float) (startColor >> 24 & 255) / 255.0F;
		float f1 = (float) (startColor >> 16 & 255) / 255.0F;
		float f2 = (float) (startColor >> 8 & 255) / 255.0F;
		float f3 = (float) (startColor & 255) / 255.0F;
		float f4 = (float) (endColor >> 24 & 255) / 255.0F;
		float f5 = (float) (endColor >> 16 & 255) / 255.0F;
		float f6 = (float) (endColor >> 8 & 255) / 255.0F;
		float f7 = (float) (endColor & 255) / 255.0F;
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(7425);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		worldrenderer.pos(right, top, 0).color(f1, f2, f3, f).endVertex();
		worldrenderer.pos(left, top, 0).color(f1, f2, f3, f).endVertex();
		worldrenderer.pos(left, bottom, 0).color(f5, f6, f7, f4).endVertex();
		worldrenderer.pos(right, bottom, 0).color(f5, f6, f7, f4).endVertex();
		tessellator.draw();
		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	public static void drawGradientRectHorz(int left, int top, int right, int bottom, int startColor, int endColor) {
		float f = (float) (startColor >> 24 & 255) / 255.0F;
		float f1 = (float) (startColor >> 16 & 255) / 255.0F;
		float f2 = (float) (startColor >> 8 & 255) / 255.0F;
		float f3 = (float) (startColor & 255) / 255.0F;
		float f4 = (float) (endColor >> 24 & 255) / 255.0F;
		float f5 = (float) (endColor >> 16 & 255) / 255.0F;
		float f6 = (float) (endColor >> 8 & 255) / 255.0F;
		float f7 = (float) (endColor & 255) / 255.0F;
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(7425);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		worldrenderer.pos(right, top, 0).color(f5, f6, f7, f4).endVertex();
		worldrenderer.pos(left, top, 0).color(f1, f2, f3, f).endVertex();
		worldrenderer.pos(left, bottom, 0).color(f1, f2, f3, f).endVertex();
		worldrenderer.pos(right, bottom, 0).color(f5, f6, f7, f4).endVertex();
		tessellator.draw();
		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	public static void drawHoveringText(
		List<String> textLines,
		final int mouseX,
		final int mouseY,
		final int screenWidth,
		final int screenHeight,
		final int maxTextWidth
	) {
		drawHoveringText(
			textLines,
			mouseX,
			mouseY,
			screenWidth,
			screenHeight,
			maxTextWidth,
			Minecraft.getMinecraft().fontRendererObj
		);
	}

	public static JsonObject getConstant(String constant, Gson gson) {
		return getConstant(constant, gson, JsonObject.class);
	}

	public static <T> T getConstant(String constant, Gson gson, Class<T> clazz) {
		File repo = NotEnoughUpdates.INSTANCE.manager.repoLocation;
		if (repo.exists()) {
			File jsonFile = new File(repo, "constants/" + constant + ".json");
			try (
				BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(jsonFile),
					StandardCharsets.UTF_8
				))
			) {
				T obj = gson.fromJson(reader, clazz);
				return obj;
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	public static float getElementAsFloat(JsonElement element, float def) {
		if (element == null) return def;
		if (!element.isJsonPrimitive()) return def;
		JsonPrimitive prim = element.getAsJsonPrimitive();
		if (!prim.isNumber()) return def;
		return prim.getAsFloat();
	}

	public static int getElementAsInt(JsonElement element, int def) {
		if (element == null) return def;
		if (!element.isJsonPrimitive()) return def;
		JsonPrimitive prim = element.getAsJsonPrimitive();
		if (!prim.isNumber()) return def;
		return prim.getAsInt();
	}

	public static String getElementAsString(JsonElement element, String def) {
		if (element == null) return def;
		if (!element.isJsonPrimitive()) return def;
		JsonPrimitive prim = element.getAsJsonPrimitive();
		if (!prim.isString()) return def;
		return prim.getAsString();
	}

	public static JsonElement getElement(JsonElement element, String path) {
		List<String> path_split = PATH_SPLITTER.splitToList(path);
		if (element instanceof JsonObject) {
			JsonElement e = element.getAsJsonObject().get(path_split.get(0));
			if (path_split.size() > 1) {
				return getElement(e, path_split.get(1));
			} else {
				return e;
			}
		} else {
			return element;
		}
	}

	public static JsonElement getElementOrDefault(JsonElement element, String path, JsonElement def) {
		JsonElement result = getElement(element, path);
		return result != null ? result : def;
	}

	public static ChatStyle createClickStyle(ClickEvent.Action action, String value) {
		ChatStyle style = new ChatStyle();
		style.setChatClickEvent(new ClickEvent(action, value));
		style.setChatHoverEvent(new HoverEvent(
			HoverEvent.Action.SHOW_TEXT,
			new ChatComponentText(EnumChatFormatting.YELLOW + value)
		));
		return style;
	}

	public static ChatStyle createClickStyle(ClickEvent.Action action, String value, String message) {
		ChatStyle style = createClickStyle(action, value);
		style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(message)));
		return style;
	}

	public static void recursiveDelete(File file) {
		if (file.isDirectory() && !Files.isSymbolicLink(file.toPath())) {
			for (File child : file.listFiles()) {
				recursiveDelete(child);
			}
		}
		file.delete();
	}

	public static char getPrimaryColourCode(String displayName) {
		int lastColourCode = -99;
		int currentColour = 0;
		int[] mostCommon = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		for (int i = 0; i < displayName.length(); i++) {
			char c = displayName.charAt(i);
			if (c == '\u00A7') {
				lastColourCode = i;
			} else if (lastColourCode == i - 1) {
				int colIndex = "0123456789abcdef".indexOf(c);
				if (colIndex >= 0) {
					currentColour = colIndex;
				} else {
					currentColour = 0;
				}
			} else if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(c) >= 0) {
				if (currentColour > 0) {
					mostCommon[currentColour]++;
				}
			}
		}
		int mostCommonCount = 0;
		for (int index = 0; index < mostCommon.length; index++) {
			if (mostCommon[index] > mostCommonCount) {
				mostCommonCount = mostCommon[index];
				currentColour = index;
			}
		}

		return "0123456789abcdef".charAt(currentColour);
	}

	public static Color getPrimaryColour(String displayName) {
		int colourInt = Minecraft.getMinecraft().fontRendererObj.getColorCode(getPrimaryColourCode(displayName));
		return new Color(colourInt).darker();
	}

	public static void scrollTooltip(int dY) {
		scrollY.setTarget(scrollY.getTarget() + dY / 10f);
		scrollY.resetTimer();
	}

	@Deprecated
	public static void drawHoveringText(
		List<String> textLines,
		int mouseX,
		int mouseY,
		int screenWidth,
		int screenHeight,
		final int maxTextWidth,
		FontRenderer font
	) {
		if (!textLines.isEmpty()) {
			int borderColorStart = 0x505000FF;
			if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.tooltipBorderColours) {
				if (textLines.size() > 0) {
					String first = textLines.get(0);
					borderColorStart = getPrimaryColour(first).getRGB() & 0x00FFFFFF |
						((NotEnoughUpdates.INSTANCE.config.tooltipTweaks.tooltipBorderOpacity) << 24);
				}
			}
			textLines = TooltipTextScrolling.handleTextLineRendering(textLines);
			if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.guiScale != 0) {
				ScaledResolution scaledResolution = Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.tooltipTweaks.guiScale);
				mouseX = Mouse.getX() * scaledResolution.getScaledWidth() / Minecraft.getMinecraft().displayWidth;

				mouseY = scaledResolution.getScaledHeight() -
					Mouse.getY() * scaledResolution.getScaledHeight() / Minecraft.getMinecraft().displayHeight;

				screenWidth = scaledResolution.getScaledWidth();

				screenHeight = scaledResolution.getScaledHeight();
			}

			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.enableDepth();
			int tooltipTextWidth = 0;

			for (String textLine : textLines) {
				int textLineWidth = font.getStringWidth(textLine);

				if (textLineWidth > tooltipTextWidth) {
					tooltipTextWidth = textLineWidth;
				}
			}

			boolean needsWrap = false;

			int titleLinesCount = 1;
			int tooltipX = mouseX + 12;
			if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
				tooltipX = mouseX - 16 - tooltipTextWidth;
				if (tooltipX < 4) // if the tooltip doesn't fit on the screen
				{
					if (mouseX > screenWidth / 2) {
						tooltipTextWidth = mouseX - 12 - 8;
					} else {
						tooltipTextWidth = screenWidth - 16 - mouseX;
					}
					needsWrap = true;
				}
			}

			if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth) {
				tooltipTextWidth = maxTextWidth;
				needsWrap = true;
			}

			if (needsWrap) {
				int wrappedTooltipWidth = 0;
				List<String> wrappedTextLines = new ArrayList<>();
				for (int i = 0; i < textLines.size(); i++) {
					String textLine = textLines.get(i);
					List<String> wrappedLine = font.listFormattedStringToWidth(textLine, tooltipTextWidth);
					if (i == 0) {
						titleLinesCount = wrappedLine.size();
					}

					for (String line : wrappedLine) {
						int lineWidth = font.getStringWidth(line);
						if (lineWidth > wrappedTooltipWidth) {
							wrappedTooltipWidth = lineWidth;
						}
						wrappedTextLines.add(line);
					}
				}
				tooltipTextWidth = wrappedTooltipWidth;
				textLines = wrappedTextLines;

				if (mouseX > screenWidth / 2) {
					tooltipX = mouseX - 16 - tooltipTextWidth;
				} else {
					tooltipX = mouseX + 12;
				}
			}

			int tooltipY = mouseY - 12;
			int tooltipHeight = 8;

			if (textLines.size() > 1) {
				tooltipHeight += (textLines.size() - 1) * 10;
				if (textLines.size() > titleLinesCount) {
					tooltipHeight += 2; // gap between title lines and next lines
				}
			}

			//Scrollable tooltips
			if (!NotEnoughUpdates.INSTANCE.config.tooltipTweaks.scrollableTooltips) {
				if (tooltipHeight + 6 > screenHeight) {
					if (scrollY.getTarget() < 0) {
						scrollY.setTarget(0);
						scrollY.resetTimer();
					} else if (screenHeight - tooltipHeight - 12 + (int) scrollY.getTarget() > 0) {
						scrollY.setTarget(-screenHeight + tooltipHeight + 12);
						scrollY.resetTimer();
					}
				} else {
					scrollY.setValue(0);
					scrollY.resetTimer();
				}
				scrollY.tick();
			}

			if (tooltipY + tooltipHeight + 6 > screenHeight) {
				tooltipY = screenHeight - tooltipHeight - 6 + (int) scrollY.getValue();
			}

			final int zLevel = 300;
			final int backgroundColor = 0xF0100010;
			drawGradientRect(
				zLevel,
				tooltipX - 3,
				tooltipY - 4,
				tooltipX + tooltipTextWidth + 3,
				tooltipY - 3,
				backgroundColor,
				backgroundColor
			);
			drawGradientRect(
				zLevel,
				tooltipX - 3,
				tooltipY + tooltipHeight + 3,
				tooltipX + tooltipTextWidth + 3,
				tooltipY + tooltipHeight + 4,
				backgroundColor,
				backgroundColor
			);
			drawGradientRect(
				zLevel,
				tooltipX - 3,
				tooltipY - 3,
				tooltipX + tooltipTextWidth + 3,
				tooltipY + tooltipHeight + 3,
				backgroundColor,
				backgroundColor
			);
			drawGradientRect(
				zLevel,
				tooltipX - 4,
				tooltipY - 3,
				tooltipX - 3,
				tooltipY + tooltipHeight + 3,
				backgroundColor,
				backgroundColor
			);
			drawGradientRect(
				zLevel,
				tooltipX + tooltipTextWidth + 3,
				tooltipY - 3,
				tooltipX + tooltipTextWidth + 4,
				tooltipY + tooltipHeight + 3,
				backgroundColor,
				backgroundColor
			);
			final int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
			drawGradientRect(
				zLevel,
				tooltipX - 3,
				tooltipY - 3 + 1,
				tooltipX - 3 + 1,
				tooltipY + tooltipHeight + 3 - 1,
				borderColorStart,
				borderColorEnd
			);
			drawGradientRect(
				zLevel,
				tooltipX + tooltipTextWidth + 2,
				tooltipY - 3 + 1,
				tooltipX + tooltipTextWidth + 3,
				tooltipY + tooltipHeight + 3 - 1,
				borderColorStart,
				borderColorEnd
			);
			drawGradientRect(
				zLevel,
				tooltipX - 3,
				tooltipY - 3,
				tooltipX + tooltipTextWidth + 3,
				tooltipY - 3 + 1,
				borderColorStart,
				borderColorStart
			);
			drawGradientRect(
				zLevel,
				tooltipX - 3,
				tooltipY + tooltipHeight + 2,
				tooltipX + tooltipTextWidth + 3,
				tooltipY + tooltipHeight + 3,
				borderColorEnd,
				borderColorEnd
			);

			GlStateManager.disableDepth();
			for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
				String line = textLines.get(lineNumber);
				font.drawStringWithShadow(line, (float) tooltipX, (float) tooltipY, -1);

				if (lineNumber + 1 == titleLinesCount) {
					tooltipY += 2;
				}

				tooltipY += 10;
			}

			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
			RenderHelper.enableStandardItemLighting();
			GlStateManager.enableRescaleNormal();
			if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.guiScale != 0) Utils.pushGuiScale(0);
		}
		GlStateManager.disableLighting();
	}

	public static void drawGradientRect(
		int zLevel,
		int left,
		int top,
		int right,
		int bottom,
		int startColor,
		int endColor
	) {
		float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
		float startRed = (float) (startColor >> 16 & 255) / 255.0F;
		float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
		float startBlue = (float) (startColor & 255) / 255.0F;
		float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
		float endRed = (float) (endColor >> 16 & 255) / 255.0F;
		float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
		float endBlue = (float) (endColor & 255) / 255.0F;

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(7425);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		worldrenderer.pos(right, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
		worldrenderer.pos(left, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
		worldrenderer.pos(left, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
		worldrenderer.pos(right, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
		tessellator.draw();

		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	public static void drawRectNoBlend(int left, int top, int right, int bottom, int color) {
		if (left < right) {
			int i = left;
			left = right;
			right = i;
		}

		if (top < bottom) {
			int j = top;
			top = bottom;
			bottom = j;
		}

		float f3 = (float) (color >> 24 & 255) / 255.0F;
		float f = (float) (color >> 16 & 255) / 255.0F;
		float f1 = (float) (color >> 8 & 255) / 255.0F;
		float f2 = (float) (color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		GlStateManager.disableTexture2D();
		GlStateManager.color(f, f1, f2, f3);
		worldrenderer.begin(7, DefaultVertexFormats.POSITION);
		worldrenderer.pos(left, bottom, 0.0D).endVertex();
		worldrenderer.pos(right, bottom, 0.0D).endVertex();
		worldrenderer.pos(right, top, 0.0D).endVertex();
		worldrenderer.pos(left, top, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
	}

	public static String prettyTime(Duration time) {
		return prettyTime(time.toMillis());
	}

	public static String prettyTime(long millis) {
		long seconds = millis / 1000 % 60;
		long minutes = (millis / 1000 / 60) % 60;
		long hours = (millis / 1000 / 60 / 60) % 24;
		long days = (millis / 1000 / 60 / 60 / 24);

		String endsIn = "";
		if (millis < 0) {
			endsIn += "Ended!";
		} else if (minutes == 0 && hours == 0 && days == 0) {
			endsIn += seconds + "s";
		} else if (hours == 0 && days == 0) {
			endsIn += minutes + "m" + seconds + "s";
		} else if (days == 0) {
			if (hours <= 6) {
				endsIn += hours + "h" + minutes + "m" + seconds + "s";
			} else {
				endsIn += hours + "h";
			}
		} else {
			endsIn += days + "d" + hours + "h";
		}

		return endsIn;
	}

	public static void drawLine(float sx, float sy, float ex, float ey, int width, int color) {
		float f = (float) (color >> 24 & 255) / 255.0F;
		float f1 = (float) (color >> 16 & 255) / 255.0F;
		float f2 = (float) (color >> 8 & 255) / 255.0F;
		float f3 = (float) (color & 255) / 255.0F;
		GlStateManager.pushMatrix();
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(f1, f2, f3, f);
		GL11.glLineWidth(width);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2d(sx, sy);
		GL11.glVertex2d(ex, ey);
		GL11.glEnd();
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
	}

	public static void drawDottedLine(float sx, float sy, float ex, float ey, int width, int factor, int color) {
		GlStateManager.pushMatrix();
		GL11.glLineStipple(factor, (short) 0xAAAA);
		GL11.glEnable(GL11.GL_LINE_STIPPLE);
		drawLine(sx, sy, ex, ey, width, color);
		GL11.glDisable(GL11.GL_LINE_STIPPLE);
		GlStateManager.popMatrix();
	}

	public static void drawTexturedQuad(
		float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4,
		float uMin, float uMax, float vMin, float vMax, int filter
	) {
		GlStateManager.enableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(
			GL11.GL_SRC_ALPHA,
			GL11.GL_ONE_MINUS_SRC_ALPHA,
			GL11.GL_ONE,
			GL11.GL_ONE_MINUS_SRC_ALPHA
		);
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer
			.pos(x1, y1, 0.0D)
			.tex(uMin, vMax).endVertex();
		worldrenderer
			.pos(x2, y2, 0.0D)
			.tex(uMax, vMax).endVertex();
		worldrenderer
			.pos(x3, y3, 0.0D)
			.tex(uMax, vMin).endVertex();
		worldrenderer
			.pos(x4, y4, 0.0D)
			.tex(uMin, vMin).endVertex();
		tessellator.draw();

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

		GlStateManager.disableBlend();
	}

	public static boolean sendCloseScreenPacket() {
		EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
		if (thePlayer.openContainer == null) return false;
		thePlayer.sendQueue.addToSendQueue(new C0DPacketCloseWindow(
			thePlayer.openContainer.windowId));
		return true;
	}

	public static String formatNumberWithDots(long number) {
		if (number == 0)
			return "0";
		String work = "";
		boolean isNegative = false;
		if (number < 0) {
			isNegative = true;
			number = -number;
		}
		while (number != 0) {
			work = String.format("%03d.%s", number % 1000, work);
			number /= 1000;
		}
		work = work.substring(0, work.length() - 1);
		while (work.startsWith("0"))
			work = work.substring(1);
		if (isNegative)
			return "-" + work;
		return work;
	}

	public static int getMouseY() {
		int height = peekGuiScale().getScaledHeight();
		return height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;
	}

	public static int getMouseX() {
		return Mouse.getX() * peekGuiScale().getScaledWidth() / Minecraft.getMinecraft().displayWidth;
	}

	public static boolean isWithinRect(int x, int y, int left, int top, int width, int height) {
		return left <= x && x < left + width &&
			top <= y && y < top + height;
	}

	public static boolean isWithinRect(int x, int y, Rectangle rectangle) {
		return isWithinRect(x, y, rectangle.getLeft(), rectangle.getTop(), rectangle.getWidth(), rectangle.getHeight());
	}

	public static int getNumberOfStars(ItemStack stack) {
		if (stack != null && stack.hasTagCompound()) {
			NBTTagCompound tag = stack.getTagCompound();

			if (tag.hasKey("ExtraAttributes", 10)) {
				NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
				if (ea.hasKey("upgrade_level", 99)) {
					return ea.getInteger("upgrade_level");
				} else if (ea.hasKey("dungeon_item_level")) {
					return ea.getInteger("dungeon_item_level");
				}
			}
		}
		return -1;
	}

	public static String getStarsString(int stars) {
		EnumChatFormatting colorCode = null;
		EnumChatFormatting defaultColorCode = EnumChatFormatting.GOLD;
		int amount = 0;
		if (stars > 5 && stars < 11) {
			colorCode = EnumChatFormatting.LIGHT_PURPLE;
			amount = stars - 5;
			stars = 5;
		}
		if (stars > 10) {
			colorCode = EnumChatFormatting.AQUA;
			defaultColorCode = EnumChatFormatting.LIGHT_PURPLE;
			amount = stars - 10;
			stars = 5;
		}

		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < stars; i++) {
			if (i < amount) {
				stringBuilder.append(colorCode).append('\u272A');
			} else {
				stringBuilder.append(defaultColorCode).append('\u272A');
			}
		}
		return stringBuilder.toString();
	}

	public static void showOutdatedRepoNotification() {
		if (NotEnoughUpdates.INSTANCE.config.notifications.outdatedRepo) {
			NotificationHandler.displayNotification(Lists.newArrayList(
					EnumChatFormatting.RED + EnumChatFormatting.BOLD.toString() + "Missing repo data",
					EnumChatFormatting.RED +
						"Data used for many NEU features is not up to date, this should normally not be the case.",
					EnumChatFormatting.RED + "You can try " + EnumChatFormatting.BOLD + "/neuresetrepo" + EnumChatFormatting.RESET +
						EnumChatFormatting.RED + " and restart your game" +
						" to see if that fixes the issue.",
					EnumChatFormatting.RED + "If the problem persists please join " + EnumChatFormatting.BOLD +
						"discord.gg/moulberry" +
						EnumChatFormatting.RESET + EnumChatFormatting.RED + " and message in " + EnumChatFormatting.BOLD +
						"#neu-support" + EnumChatFormatting.RESET + EnumChatFormatting.RED + " to get support"
				),
				true, true
			);
		}
	}

	/**
	 * Finds the rarity from the lore of an item.
	 * -1 = UNKNOWN
	 * 0 = COMMON
	 * 1 = UNCOMMON
	 * 2 = RARE
	 * 3 = EPIC
	 * 4 = LEGENDARY
	 * 5 = MYTHIC
	 * 6 = SPECIAL
	 * 7 = VERY SPECIAL
	 */
	public static int getRarityFromLore(JsonArray lore) {
		for (int i = lore.size() - 1; i >= 0; i--) {
			String line = lore.get(i).getAsString();

			for (int j = 0; j < rarityArrC.length; j++) {
				if (line.startsWith(rarityArrC[j])) {
					return j;
				}
			}
		}
		return -1;
	}

	public static UUID parseDashlessUUID(String dashlessUuid) {
		// From: https://stackoverflow.com/a/30760478/
		BigInteger most = new BigInteger(dashlessUuid.substring(0, 16), 16);
		BigInteger least = new BigInteger(dashlessUuid.substring(16, 32), 16);
		return new UUID(most.longValue(), least.longValue());
	}

	public static String getOpenChestName() {
		return SBInfo.getInstance().currentlyOpenChestName;
	}

	public static String getLastOpenChestName() {
		return SBInfo.getInstance().lastOpenChestName;
	}

	public static String getNameFromChatComponent(IChatComponent chatComponent) {
		String unformattedText = cleanColour(chatComponent.getSiblings().get(0).getUnformattedText());
		String username = unformattedText.substring(unformattedText.indexOf(">") + 2, unformattedText.indexOf(":"));
		// If the first character is a square bracket the user has a rank
		// So we get the username from the space after the closing square bracket (end of their rank)
		if (username.charAt(0) == '[') {
			username = username.substring(username.indexOf(" ") + 1);
		}
		// If we still get any square brackets it means the user was talking in guild chat with a guild rank
		// So we get the username up to the space before the guild rank
		if (username.contains("[") || username.contains("]")) {
			username = username.substring(0, username.indexOf(" "));
		}
		return username;
	}

	public static void addChatMessage(String message) {
		EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
		if (thePlayer != null) {
			thePlayer.addChatMessage(new ChatComponentText(message));
		} else {
			System.out.println(message);
		}
	}

	public static boolean openUrl(String url) {
		try {
			Desktop desk = Desktop.getDesktop();
			desk.browse(new URI(url));
			return true;
		} catch (UnsupportedOperationException | IOException | URISyntaxException ignored) {
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec("xdg-open " + url);
				return true;
			} catch (IOException e) {
				playSound(new ResourceLocation("game.player.hurt"), true);
				return false;
			}
		}
	}

	public static void sendLeftMouseClick(int windowId, int slot) {
		Minecraft.getMinecraft().playerController.windowClick(
			windowId,
			slot, 0, 0, Minecraft.getMinecraft().thePlayer
		);
	}

	public static String timeSinceMillisecond(long time) {
		Instant lastSave = Instant.ofEpochMilli(time);
		LocalDateTime lastSaveTime = LocalDateTime.ofInstant(lastSave, TimeZone.getDefault().toZoneId());
		long timeDiff = System.currentTimeMillis() - lastSave.toEpochMilli();
		LocalDateTime sinceOnline = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeDiff), ZoneId.of("UTC"));
		String renderText;

		if (timeDiff < 60000L) {
			renderText = sinceOnline.getSecond() + " seconds ago";
		} else if (timeDiff < 3600000L) {
			renderText = sinceOnline.getMinute() + " minutes ago";
		} else if (timeDiff < 86400000L) {
			renderText = sinceOnline.getHour() + " hours ago";
		} else if (timeDiff < 31556952000L) {
			renderText = sinceOnline.getDayOfYear() + " days ago";
		} else {
			renderText = lastSaveTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
		}
		return renderText;
	}
}
