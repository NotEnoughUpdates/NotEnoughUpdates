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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NEUOverlay;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.GlScissorStack;
import io.github.moulberry.notenoughupdates.core.GuiElementTextField;
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpingInteger;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.moulberry.notenoughupdates.miscgui.GuiEnchantColour.custom_ench_colour;

public class GuiInvButtonEditor extends GuiScreen {
	private static final ResourceLocation INVENTORY = new ResourceLocation(
		"minecraft:textures/gui/container/inventory.png");
	private static final ResourceLocation EDITOR = new ResourceLocation("notenoughupdates:invbuttons/editor.png");
	private static final ResourceLocation EXTRA_ICONS_JSON = new ResourceLocation(
		"notenoughupdates:invbuttons/extraicons.json");
	private static final ResourceLocation PRESETS_JSON = new ResourceLocation("notenoughupdates:invbuttons/presets.json");

	private static final String sharePrefix = "NEUBUTTONS/";

	private final int xSize = 176;
	private final int ySize = 166;

	private int guiLeft;
	private int guiTop;

	private static final int BACKGROUND_TYPES = 5;
	private static final int ICON_TYPES = 3;
	private int iconTypeIndex = 0;

	private final int editorXSize = 150;
	private final int editorYSize = 204;
	private int editorLeft;
	private int editorTop;

	private final GuiElementTextField commandTextField = new GuiElementTextField(
		"",
		editorXSize - 14,
		16,
		GuiElementTextField.SCALE_TEXT
	);
	private final GuiElementTextField iconTextField = new GuiElementTextField(
		"",
		editorXSize - 14,
		16,
		GuiElementTextField.SCALE_TEXT
	);

	private static final HashSet<String> prioritisedIcons = new HashSet<String>() {{
		add("WORKBENCH");
		add("LEATHER_CHESTPLATE");
		add("CHEST");
		add("BONE");
		add("ENDER_CHEST");
		add("GOLD_BARDING");
		add("COMPASS");
		add("GOLD_BLOCK");
		add("EMPTY_MAP");
		add("RAW_FISH");
		add("FISHING_ROD");
		add("EMERALD");
		add("IRON_SWORD");
		add("POTION");
		add("NETHER_STAR");
		add("PAINTING");
		add("COMMAND");
		add("BOOK");
	}};

	private static HashMap<String, String> extraIcons = null;

	private static final HashMap<String, String> skullIcons = new HashMap<String, String>() {{
		put("personal bank", "skull:e36e94f6c34a35465fce4a90f2e25976389eb9709a12273574ff70fd4daa6852");
		put("skyblock hub", "skull:d7cc6687423d0570d556ac53e0676cb563bbdd9717cd8269bdebed6f6d4e7bf8");
		put("private island", "skull:c9c8881e42915a9d29bb61a16fb26d059913204d265df5b439b3d792acd56");
		put("castle", "skull:f4559d75464b2e40a518e4de8e6cf3085f0a3ca0b1b7012614c4cd96fed60378");
		put("sirius shack", "skull:7ab83858ebc8ee85c3e54ab13aabfcc1ef2ad446d6a900e471c3f33b78906a5b");
		put("crypts", "skull:25d2f31ba162fe6272e831aed17f53213db6fa1c4cbe4fc827f3963cc98b9");
		put("spiders den", "skull:c754318a3376f470e481dfcd6c83a59aa690ad4b4dd7577fdad1c2ef08d8aee6");
		put("top of the nest", "skull:9d7e3b19ac4f3dee9c5677c135333b9d35a7f568b63d1ef4ada4b068b5a25");
		put("the end", "skull:7840b87d52271d2a755dedc82877e0ed3df67dcc42ea479ec146176b02779a5");
		put("the end dragons nest", "skull:a1cd6d2d03f135e7c6b5d6cdae1b3a68743db4eb749faf7341e9fb347aa283b");
		put("the park", "skull:a221f813dacee0fef8c59f76894dbb26415478d9ddfc44c2e708a6d3b7549b");
		put("the park jungle", "skull:79ca3540621c1c79c32bf42438708ff1f5f7d0af9b14a074731107edfeb691c");
		put("the park howling cave", "skull:1832d53997b451635c9cf9004b0f22bb3d99ab5a093942b5b5f6bb4e4de47065");
		put("gold mines", "skull:73bc965d579c3c6039f0a17eb7c2e6faf538c7a5de8e60ec7a719360d0a857a9");
		put("deep caverns", "skull:569a1f114151b4521373f34bc14c2963a5011cdc25a6554c48c708cd96ebfc");
		put("the barn", "skull:4d3a6bd98ac1833c664c4909ff8d2dc62ce887bdcf3cc5b3848651ae5af6b");
		put("mushroom desert", "skull:6b20b23c1aa2be0270f016b4c90d6ee6b8330a17cfef87869d6ad60b2ffbf3b5");
		put("dungeon hub", "skull:9b56895b9659896ad647f58599238af532d46db9c1b0389b8bbeb70999dab33d");
		put("dwarven mines", "skull:51539dddf9ed255ece6348193cd75012c82c93aec381f05572cecf7379711b3b");
		put("hotm heart of the mountain", "skull:86f06eaa3004aeed09b3d5b45d976de584e691c0e9cade133635de93d23b9edb");
		put("bazaar dude", "skull:c232e3820897429157619b0ee099fec0628f602fff12b695de54aef11d923ad7");
		put("museum", "skull:438cf3f8e54afc3b3f91d20a49f324dca1486007fe545399055524c17941f4dc");
		put("crystal hollows", "skull:21dbe30b027acbceb612563bd877cd7ebb719ea6ed1399027dcee58bb9049d4a");
		put("dwarven forge", "skull:5cbd9f5ec1ed007259996491e69ff649a3106cf920227b1bb3a71ee7a89863f");
		put("forgotton skull", "skull:6becc645f129c8bc2faa4d8145481fab11ad2ee75749d628dcd999aa94e7");
		put("crystal nucleus", "skull:34d42f9c461cee1997b67bf3610c6411bf852b9e5db607bbf626527cfb42912c");
		put("void sepulture", "skull:eb07594e2df273921a77c101d0bfdfa1115abed5b9b2029eb496ceba9bdbb4b3");
		put("crimson isle", "skull:c3687e25c632bce8aa61e0d64c24e694c3eea629ea944f4cf30dcfb4fbce071");
	}};

	private static LinkedHashMap<String, List<NEUConfig.InventoryButton>> presets = null;

	public GuiInvButtonEditor() {
		super();
		reloadExtraIcons();
		reloadPresets();
	}

	private static void reloadExtraIcons() {
		extraIcons = new HashMap<>();

		try (
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				Minecraft.getMinecraft().getResourceManager().getResource(EXTRA_ICONS_JSON).getInputStream(),
				StandardCharsets.UTF_8
			))
		) {
			JsonObject json = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(reader, JsonObject.class);

			for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
				if (entry.getValue().isJsonPrimitive()) {
					extraIcons.put(entry.getKey(), "extra:" + entry.getValue().getAsString());
				}
			}
		} catch (Exception ignored) {
		}
	}

	private static void reloadPresets() {
		presets = new LinkedHashMap<>();

		try (
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				Minecraft.getMinecraft().getResourceManager().getResource(PRESETS_JSON).getInputStream(),
				StandardCharsets.UTF_8
			))
		) {
			JsonObject json = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(reader, JsonObject.class);

			for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
				if (entry.getValue().isJsonArray()) {
					JsonArray arr = entry.getValue().getAsJsonArray();
					List<NEUConfig.InventoryButton> buttons = new ArrayList<>();
					for (int i = 0; i < arr.size(); i++) {
						JsonObject o = arr.get(i).getAsJsonObject();
						NEUConfig.InventoryButton button = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(
							o,
							NEUConfig.InventoryButton.class
						);
						buttons.add(button);
					}
					presets.put(entry.getKey(), buttons);
				}
			}
		} catch (Exception ignored) {
		}
	}

	private static final Comparator<String> prioritisingComparator = (o1, o2) -> {
		boolean c1 = prioritisedIcons.contains(o1);
		boolean c2 = prioritisedIcons.contains(o2);

		if (c1 && !c2) return -1;
		if (!c1 && c2) return 1;

		return o1.compareTo(o2);
	};

	private final List<String> searchedIcons = new ArrayList<>();

	private final LerpingInteger itemScroll = new LerpingInteger(0, 100);

	private NEUConfig.InventoryButton editingButton = null;

	private static final HashMap<String, ItemStack> skullMap = new HashMap<>();

	public static void renderIcon(String icon, int x, int y) {
		if (extraIcons == null) {
			reloadExtraIcons();
		}

		if (icon.startsWith("extra:")) {
			String name = icon.substring("extra:".length());
			ResourceLocation resourceLocation = new ResourceLocation(
				"notenoughupdates:invbuttons/extraicons/" + name + ".png");
			Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
			GlStateManager.color(1, 1, 1, 1);
			Utils.drawTexturedRect(x, y, 16, 16, GL11.GL_NEAREST);
		} else {
			ItemStack stack = getStack(icon);

			float scale = 1;
			if (icon.startsWith("skull:")) {
				scale = 1.2f;
			}
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 8, y + 8, 0);
			GlStateManager.scale(scale, scale, 1);
			GlStateManager.translate(-8, -8, 0);
			Utils.drawItemStack(stack, 0, 0);
			GlStateManager.popMatrix();
		}
	}

	public static ItemStack getStack(String icon) {
		if (icon.startsWith("extra:")) {
			return null;
		} else if (icon.startsWith("skull:")) {
			String link = icon.substring("skull:".length());
			if (skullMap.containsKey(link)) return skullMap.get(link);

			ItemStack render = new ItemStack(Items.skull, 1, 3);
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagCompound skullOwner = new NBTTagCompound();
			NBTTagCompound properties = new NBTTagCompound();
			NBTTagList textures = new NBTTagList();
			NBTTagCompound textures_0 = new NBTTagCompound();

			String uuid = UUID.nameUUIDFromBytes(link.getBytes()).toString();
			skullOwner.setString("Id", uuid);
			skullOwner.setString("Name", uuid);

			String display = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + link + "\"}}}";
			String displayB64 = Base64.getEncoder().encodeToString(display.getBytes());

			textures_0.setString("Value", displayB64);
			textures.appendTag(textures_0);

			properties.setTag("textures", textures);
			skullOwner.setTag("Properties", properties);
			nbt.setTag("SkullOwner", skullOwner);
			render.setTagCompound(nbt);

			skullMap.put(link, render);
			return render;
		} else {
			return NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
				.getItemInformation()
				.get(icon));
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

		super.drawDefaultBackground();

		guiLeft = width / 2 - xSize / 2;
		guiTop = height / 2 - ySize / 2;

		GlStateManager.enableDepth();

		Minecraft.getMinecraft().getTextureManager().bindTexture(INVENTORY);
		GlStateManager.color(1, 1, 1, 1);
		Utils.drawTexturedRect(guiLeft, guiTop, xSize, ySize, 0, xSize / 256f, 0, ySize / 256f, GL11.GL_NEAREST);

		if (NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud) {
			NotEnoughUpdates.INSTANCE.overlay.renderPreviewArmorHud();
		}
		if (NotEnoughUpdates.INSTANCE.config.petOverlay.petInvDisplay) {
			NotEnoughUpdates.INSTANCE.overlay.renderPreviewPetInvHud();
		}

		for (NEUConfig.InventoryButton button : NotEnoughUpdates.INSTANCE.config.hidden.inventoryButtons) {
			int x = guiLeft + button.x;
			int y = guiTop + button.y;
			if (button.anchorRight) {
				x += xSize;
			}
			if (button.anchorBottom) {
				y += ySize;
			}

			if (NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud) {
				if (x < guiLeft + xSize - 150 && x > guiLeft + xSize - 200 && y > guiTop && y < guiTop + 84) {
					x -= 25;
				}
			}
			if (NotEnoughUpdates.INSTANCE.config.petOverlay.petInvDisplay) {
				if (x < guiLeft + xSize - 150 && x > guiLeft + xSize - 200 && y > guiTop + 60 && y < guiTop + 120) {
					x -= 25;
				}
			}

			if (button.isActive()) {
				GlStateManager.color(1, 1, 1, 1f);
			} else {
				GlStateManager.color(1, 1, 1, 0.5f);
			}

			Minecraft.getMinecraft().getTextureManager().bindTexture(EDITOR);
			Utils.drawTexturedRect(
				x,
				y,
				18,
				18,
				button.backgroundIndex * 18 / 256f,
				(button.backgroundIndex * 18 + 18) / 256f,
				18 / 256f,
				36 / 256f,
				GL11.GL_NEAREST
			);

			if (button.isActive()) {
				if (button.icon != null && !button.icon.trim().isEmpty()) {
					GlStateManager.enableDepth();

					renderIcon(button.icon, x + 1, y + 1);
				}
			} else {
				fontRendererObj.drawString("+", x + 6, y + 5, 0xffcccccc);
			}
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(custom_ench_colour);
		GlStateManager.color(1, 1, 1, 1);
		Utils.drawTexturedRect(
			guiLeft - 88 - 2 - 22 - (NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud ? 25 : 0),
			guiTop + 2,
			88,
			20,
			64 / 217f,
			152 / 217f,
			48 / 78f,
			68 / 78f,
			GL11.GL_NEAREST
		);
		Utils.drawTexturedRect(
			guiLeft - 88 - 2 - 22 - (NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud ? 25 : 0),
			guiTop + 2 + 24,
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
			fontRendererObj,
			guiLeft - 44 - 2 - 22 - (NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud ? 25 : 0),
			guiTop + 8,
			false,
			86,
			4210752
		);
		Utils.drawStringCenteredScaledMaxWidth(
			"from Clipboard",
			fontRendererObj,
			guiLeft - 44 - 2 - 22 - (NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud ? 25 : 0),
			guiTop + 16,
			false,
			86,
			4210752
		);
		Utils.drawStringCenteredScaledMaxWidth(
			"Save preset",
			fontRendererObj,
			guiLeft - 44 - 2 - 22 - (NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud ? 25 : 0),
			guiTop + 8 + 24,
			false,
			86,
			4210752
		);
		Utils.drawStringCenteredScaledMaxWidth(
			"to Clipboard",
			fontRendererObj,
			guiLeft - 44 - 2 - 22 - (NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud ? 25 : 0),
			guiTop + 16 + 24,
			false,
			86,
			4210752
		);

		if (!validShareContents()) {
			Gui.drawRect(guiLeft - 88 - 2 - 22 - (NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud ? 25 : 0), guiTop + 2, guiLeft - 2 - 22 - (NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud ? 25 : 0), guiTop + 2 + 20, 0x80000000);
		}

		GlStateManager.color(1, 1, 1, 1);

		if (presets != null) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(EDITOR);
			Utils.drawTexturedRect(guiLeft + xSize + 22, guiTop, 80, ySize,
				editorXSize / 256f, (editorXSize + 80) / 256f, 41 / 256f, (41 + ySize) / 256f, GL11.GL_NEAREST
			);
			Utils.drawStringCenteredScaledMaxWidth(
				"\u00a7nPresets",
				fontRendererObj,
				guiLeft + xSize + 22 + 40,
				guiTop + 10,
				false,
				70,
				0xffa0a0a0
			);

			int index = 0;
			for (String presetName : presets.keySet()) {
				Utils.drawStringCenteredScaledMaxWidth(
					presetName,
					fontRendererObj,
					guiLeft + xSize + 22 + 40,
					guiTop + 25 + 10 * (index++),
					false,
					70,
					0xff909090
				);
			}
		}

		if (editingButton != null) {
			int x = guiLeft + editingButton.x;
			int y = guiTop + editingButton.y;
			if (editingButton.anchorRight) {
				x += xSize;
			}
			if (editingButton.anchorBottom) {
				y += ySize;
			}

			if (NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud) {
				if (x < guiLeft + xSize - 150 && x > guiLeft + xSize - 200 && y > guiTop && y < guiTop + 84) {
					x -= 25;
				}
			}
			if (NotEnoughUpdates.INSTANCE.config.petOverlay.petInvDisplay) {
				if (x < guiLeft + xSize - 150 && x > guiLeft + xSize - 200 && y > guiTop + 60 && y < guiTop + 120) {
					x -= 25;
				}
			}

			GlStateManager.translate(0, 0, 300);
			editorLeft = x + 8 - editorXSize / 2;
			editorTop = y + 18 + 2;

			boolean showArrow = true;
			if (editorTop + editorYSize + 5 > height) {
				editorTop = height - editorYSize - 5;
				showArrow = false;
			}
			if (editorLeft < 5) {
				editorLeft = 5;
				showArrow = false;
			}
			if (editorLeft + editorXSize + 5 > width) {
				editorLeft = width - editorXSize - 5;
				showArrow = false;
			}

			Minecraft.getMinecraft().getTextureManager().bindTexture(EDITOR);
			GlStateManager.color(1, 1, 1, 1f);
			Utils.drawTexturedRect(
				editorLeft,
				editorTop,
				editorXSize,
				editorYSize,
				0,
				editorXSize / 256f,
				41 / 256f,
				(41 + editorYSize) / 256f,
				GL11.GL_NEAREST
			);

			if (showArrow)
				Utils.drawTexturedRect(x + 8 - 3, y + 18, 10, 5, 0, 6 / 256f, 36 / 256f, 41 / 256f, GL11.GL_NEAREST);

			fontRendererObj.drawString("Command", editorLeft + 7, editorTop + 7, 0xffa0a0a0, false);

			commandTextField.setSize(editorXSize - 14, 16);
			commandTextField.setText(commandTextField.getText().replaceAll("^ +", ""));
			if (commandTextField.getText().startsWith("/")) {
				commandTextField.setPrependText("");
			} else {
				commandTextField.setPrependText("\u00a77/\u00a7r");
			}
			commandTextField.render(editorLeft + 7, editorTop + 19);

			fontRendererObj.drawString("Background", editorLeft + 7, editorTop + 40, 0xffa0a0a0, false);

			for (int i = 0; i < BACKGROUND_TYPES; i++) {
				if (i == editingButton.backgroundIndex) {
					Gui.drawRect(
						editorLeft + 7 + 20 * i - 1,
						editorTop + 50 - 1,
						editorLeft + 7 + 20 * i + 19,
						editorTop + 50 + 19,
						0xff0000ff
					);
				}
				Minecraft.getMinecraft().getTextureManager().bindTexture(EDITOR);
				GlStateManager.color(1, 1, 1, 1);
				Utils.drawTexturedRect(editorLeft + 7 + 20 * i, editorTop + 50, 18, 18,
					i * 18 / 256f, (i * 18 + 18) / 256f, 0 / 256f, 18 / 256f, GL11.GL_NEAREST
				);
			}

			fontRendererObj.drawString("Icon Type", editorLeft + 7, editorTop + 50 + 24, 0xffa0a0a0, false);

			Minecraft.getMinecraft().getTextureManager().bindTexture(EDITOR);
			GlStateManager.color(1, 1, 1, 1);
			float uMin = 18 / 256f;
			float uMax = 36 / 256f;
			float vMin = 0;
			float vMax = 18 / 256f;

			for (int i = 0; i < ICON_TYPES; i++) {
				boolean flip = iconTypeIndex == i;

				Minecraft.getMinecraft().getTextureManager().bindTexture(EDITOR);
				GlStateManager.color(1, 1, 1, 1);
				Utils.drawTexturedRect(editorLeft + 7 + 20 * i, editorTop + 50 + 34, 18, 18,
					flip ? uMax : uMin, flip ? uMin : uMax, flip ? vMax : vMin, flip ? vMin : vMax, GL11.GL_NEAREST
				);

				ItemStack stack = null;
				if (i == 0) {
					stack = new ItemStack(Items.diamond_sword);
				} else if (i == 1) {
					stack = getStack("skull:c9c8881e42915a9d29bb61a16fb26d059913204d265df5b439b3d792acd56");
				} else if (i == 2) {
					stack = new ItemStack(Items.lead);
				}
				if (stack != null) Utils.drawItemStack(stack, editorLeft + 8 + 20 * i, editorTop + 50 + 35);
			}

			fontRendererObj.drawString("Icon Selector", editorLeft + 7, editorTop + 50 + 55, 0xffa0a0a0, false);

			iconTextField.render(editorLeft + 7, editorTop + 50 + 65);

			GlStateManager.enableDepth();

			itemScroll.tick();

			ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
			GlScissorStack.push(0, editorTop + 136, width, editorTop + 196, scaledResolution);

			synchronized (searchedIcons) {
				if (iconTextField.getText().trim().isEmpty() && searchedIcons.isEmpty()) {
					searchedIcons.addAll(NotEnoughUpdates.INSTANCE.manager.getItemInformation().keySet());
					searchedIcons.sort(prioritisingComparator);
				}

				int max = (searchedIcons.size() - 1) / 6 * 20 - 40;
				int scroll = itemScroll.getValue();
				if (scroll > max) scroll = max;

				int scrollBarHeight = (int) Math.ceil(3f * 54f / (searchedIcons.size() - 18));
				if (scrollBarHeight < 0) scrollBarHeight = 54;
				if (scrollBarHeight < 2) scrollBarHeight = 2;
				int scrollY = (int) Math.floor(54f * ((scroll / 20f) / ((searchedIcons.size() - 18) / 6f)));
				if (scrollY + scrollBarHeight > 54) scrollY = 54 - scrollBarHeight;

				Gui.drawRect(
					editorLeft + 137,
					editorTop + 139 + scrollY,
					editorLeft + 139,
					editorTop + 139 + scrollY + scrollBarHeight,
					0xff202020
				);

				int endIndex = searchedIcons.size();
				int startIndex = scroll / 20 * 6;
				if (startIndex < 0) startIndex = 0;
				if (endIndex > startIndex + 24) endIndex = startIndex + 24;

				for (int i = startIndex; i < endIndex; i++) {
					String iconS = searchedIcons.get(i);

					int iconX = editorLeft + 12 + ((i - startIndex) % 6) * 20;
					int iconY = editorTop + 137 + ((i - startIndex) / 6) * 20 - (itemScroll.getValue() % 20);

					Minecraft.getMinecraft().getTextureManager().bindTexture(EDITOR);
					GlStateManager.color(1, 1, 1, 1);
					Utils.drawTexturedRect(iconX, iconY, 18, 18,
						18 / 256f, 36 / 256f, 0 / 256f, 18 / 256f, GL11.GL_NEAREST
					);

					renderIcon(iconS, iconX + 1, iconY + 1);
				}
			}

			GlScissorStack.pop(scaledResolution);

			GlStateManager.translate(0, 0, -300);
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		int scroll = Mouse.getEventDWheel();
		if (scroll != 0) {
			scroll = -scroll;
			if (scroll > 1) scroll = 8;
			if (scroll < -1) scroll = -8;

			int delta = Math.abs(itemScroll.getTarget() - itemScroll.getValue());
			float acc = delta / 20 + 1;
			scroll *= acc;

			int max = (searchedIcons.size() - 1) / 6 * 20 - 40;
			int newTarget = itemScroll.getTarget() + scroll;

			if (newTarget > max) newTarget = max;
			if (newTarget < 0) newTarget = 0;

			itemScroll.setTarget(newTarget);
			itemScroll.resetTimer();
		}

		super.handleMouseInput();
	}

	private boolean validShareContents() {
		try {
			String base64 = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);

			if (base64.length() <= sharePrefix.length()) return false;

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

		if (editingButton != null) {
			if (mouseX >= editorLeft && mouseX <= editorLeft + editorXSize &&
				mouseY >= editorTop & mouseY <= editorTop + editorYSize) {
				if (mouseX >= editorLeft + 7 && mouseX <= editorLeft + 7 + commandTextField.getWidth() &&
					mouseY >= editorTop + 12 && mouseY <= editorTop + 12 + commandTextField.getHeight()) {
					commandTextField.mouseClicked(mouseX, mouseY, mouseButton);
					iconTextField.unfocus();
					editingButton.command = commandTextField.getText();
					return;
				}
				if (mouseX >= editorLeft + 7 && mouseX <= editorLeft + 7 + iconTextField.getWidth() &&
					mouseY >= editorTop + 50 + 65 && mouseY <= editorTop + 50 + 65 + iconTextField.getHeight()) {
					iconTextField.mouseClicked(mouseX, mouseY, mouseButton);

					if (mouseButton == 1) {
						search();
					}

					commandTextField.unfocus();
					return;
				}
				if (mouseY >= editorTop + 50 && mouseY <= editorTop + 50 + 18) {
					for (int i = 0; i < BACKGROUND_TYPES; i++) {
						if (mouseX >= editorLeft + 7 + 20 * i && mouseX <= editorLeft + 7 + 20 * i + 18) {
							editingButton.backgroundIndex = i;
							return;
						}
					}
				}
				for (int i = 0; i < ICON_TYPES; i++) {
					if (mouseX >= editorLeft + 7 + 20 * i && mouseX <= editorLeft + 7 + 20 * i + 18 &&
						mouseY >= editorTop + 50 + 34 && mouseY <= editorTop + 50 + 34 + 18) {
						if (iconTypeIndex != i) {
							iconTypeIndex = i;
							search();
						}
						return;
					}
				}
				if (mouseX > editorLeft + 8 && mouseX < editorLeft + editorXSize - 16 && mouseY > editorTop + 136 &&
					mouseY < editorTop + 196) {
					synchronized (searchedIcons) {
						int max = (searchedIcons.size() - 1) / 6 * 20 - 40;
						int scroll = itemScroll.getValue();
						if (scroll > max) scroll = max;

						int endIndex = searchedIcons.size();
						int startIndex = scroll / 20 * 6;
						if (startIndex < 0) startIndex = 0;
						if (endIndex > startIndex + 24) endIndex = startIndex + 24;

						for (int i = startIndex; i < endIndex; i++) {
							String iconS = searchedIcons.get(i);

							int x = editorLeft + 12 + ((i - startIndex) % 6) * 20;
							int y = editorTop + 137 + ((i - startIndex) / 6) * 20 - (itemScroll.getValue() % 20);

							if (mouseX >= x && mouseX <= x + 18 &&
								mouseY >= y && mouseY <= y + 18) {
								editingButton.icon = iconS;
								return;
							}
						}
					}
				}
				return;
			}
		}

		for (NEUConfig.InventoryButton button : NotEnoughUpdates.INSTANCE.config.hidden.inventoryButtons) {
			int x = guiLeft + button.x;
			int y = guiTop + button.y;
			if (button.anchorRight) {
				x += xSize;
			}
			if (button.anchorBottom) {
				y += ySize;
			}

			if (NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud) {
				if (x < guiLeft + xSize - 150 && x > guiLeft + xSize - 200 && y > guiTop && y < guiTop + 84) {
					x -= 25;
				}
			}
			if (NotEnoughUpdates.INSTANCE.config.petOverlay.petInvDisplay) {
				if (x < guiLeft + xSize - 150 && x > guiLeft + xSize - 200 && y > guiTop + 60 && y < guiTop + 120) {
					x -= 25;
				}
			}

			if (mouseX >= x && mouseY >= y &&
				mouseX <= x + 18 && mouseY <= y + 18) {
				if (editingButton == button) {
					editingButton = null;
				} else {
					editingButton = button;
					commandTextField.setText(editingButton.command);
				}
				return;
			}
		}

		if (mouseX > guiLeft - 2 - 88 - 22 && mouseX < guiLeft - 2 - 22) {
			if (mouseY > guiTop + 2 && mouseY < guiTop + 22) {

				String base64;

				try {
					base64 = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
				} catch (HeadlessException | IOException | UnsupportedFlavorException e) {
					return;
				}

				if (base64.length() <= sharePrefix.length()) return;

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

				List<NEUConfig.InventoryButton> buttons = new ArrayList<>();
				System.out.println(presetArray.size());
				try {
					for (int i = 0; i < presetArray.size(); i++) {

						JsonElement shittyO = presetArray.get(i);
						JsonElement lessShittyO = new JsonParser().parse(shittyO.getAsString());
						if (lessShittyO.isJsonObject()) {
							JsonObject o = lessShittyO.getAsJsonObject();
							NEUConfig.InventoryButton button = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(
								o,
								NEUConfig.InventoryButton.class
							);
							buttons.add(button);
						}

					}

					NotEnoughUpdates.INSTANCE.config.hidden.inventoryButtons = buttons;
					return;
				} catch (JsonParseException | ClassCastException | IllegalStateException e) {
					return;
				}

			} else if (mouseY > guiTop + 26 && mouseY < guiTop + 26 + 20) {

				List<NEUConfig.InventoryButton> result = NotEnoughUpdates.INSTANCE.config.hidden.inventoryButtons;
				JsonArray jsonArray = new JsonArray();

				for (NEUConfig.InventoryButton inventoryButton : result) {
					jsonArray.add(new JsonPrimitive(NotEnoughUpdates.INSTANCE.manager.gson.toJson(
						inventoryButton,
						NEUConfig.InventoryButton.class
					)));
				}
				String base64String = Base64.getEncoder().encodeToString((sharePrefix +
					jsonArray).getBytes(StandardCharsets.UTF_8));
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(base64String), null);
				return;
			}
		}

		if (editingButton == null) {
			int index = 0;
			for (List<NEUConfig.InventoryButton> buttons : presets.values()) {
				if (mouseX >= guiLeft + xSize + 22 && mouseX <= guiLeft + xSize + 22 + 80 &&
					mouseY >= guiTop + 21 + 10 * index && mouseY <= guiTop + 21 + 10 * index + 10) {
					NotEnoughUpdates.INSTANCE.config.hidden.inventoryButtons = buttons;
					return;
				}
				index++;
			}
		}

		editingButton = null;
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);

		if (editingButton != null && commandTextField.getFocus()) {
			commandTextField.keyTyped(typedChar, keyCode);
			editingButton.command = commandTextField.getText();
		} else if (editingButton != null && iconTextField.getFocus()) {
			String old = iconTextField.getText().trim();
			iconTextField.keyTyped(typedChar, keyCode);
			String newText = iconTextField.getText().trim();

			if (!old.equalsIgnoreCase(newText)) {
				search();
			}
		}
	}

	private final ExecutorService searchES = Executors.newSingleThreadExecutor();
	private final AtomicInteger searchId = new AtomicInteger(0);

	@Override
	public void onGuiClosed() {
		NotEnoughUpdates.INSTANCE.saveConfig();
	}

	public void search() {
		final int thisSearchId = searchId.incrementAndGet();
		final String searchString = iconTextField.getText();

		if (iconTypeIndex == 0) {
			if (searchString.trim().isEmpty()) {
				synchronized (searchedIcons) {
					searchedIcons.clear();

					List<String> unsorted = new ArrayList<>(NotEnoughUpdates.INSTANCE.manager.getItemInformation().keySet());
					unsorted.sort(prioritisingComparator);
					searchedIcons.addAll(unsorted);
				}
				return;
			}

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

				title.sort(prioritisingComparator);
				desc.sort(prioritisingComparator);

				if (thisSearchId != searchId.get()) return;

				synchronized (searchedIcons) {
					searchedIcons.clear();
					searchedIcons.addAll(title);
					searchedIcons.addAll(desc);
				}
			});
		} else if (iconTypeIndex == 1) {
			if (searchString.trim().isEmpty()) {
				searchedIcons.clear();
				searchedIcons.addAll(skullIcons.values());
				return;
			}

			synchronized (searchedIcons) {
				searchedIcons.clear();
				for (Map.Entry<String, String> entry : skullIcons.entrySet()) {
					if (NotEnoughUpdates.INSTANCE.manager.searchString(entry.getKey(), searchString)) {
						searchedIcons.add(entry.getValue());
					}
				}
			}
		} else if (iconTypeIndex == 2) {
			if (searchString.trim().isEmpty()) {
				searchedIcons.clear();
				searchedIcons.addAll(extraIcons.values());
				return;
			}

			synchronized (searchedIcons) {
				searchedIcons.clear();
				for (Map.Entry<String, String> entry : extraIcons.entrySet()) {
					if (NotEnoughUpdates.INSTANCE.manager.searchString(entry.getKey(), searchString)) {
						searchedIcons.add(entry.getValue());
					}
				}
			}
		}
	}
}
