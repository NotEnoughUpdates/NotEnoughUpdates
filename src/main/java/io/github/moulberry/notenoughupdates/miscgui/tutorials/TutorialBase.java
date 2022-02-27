package io.github.moulberry.notenoughupdates.miscgui.tutorials;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.github.moulberry.notenoughupdates.miscgui.GuiEnchantColour.custom_ench_colour;

public class TutorialBase extends GuiScreen {
	private int guiLeft = 0;
	private int guiTop = 0;
	private int sizeX = 0;
	private int sizeY = 0;

	protected static String title;

	private int page = 0;
	private final ResourceLocation screenshotBorder = new ResourceLocation("notenoughupdates:ss_border.jpg");

	protected ResourceLocation[] screenshots = null;

	int scaleFactor = 0;

	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		Keyboard.enableRepeatEvents(true);
		super.keyTyped(typedChar, keyCode);
		if (keyCode == Keyboard.KEY_LEFT) {
			page--;
		} else if (keyCode == Keyboard.KEY_RIGHT) {
			page++;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

		drawDefaultBackground();

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		scaleFactor = scaledResolution.getScaleFactor();

		sizeX = width / 2 + 40 / scaleFactor;
		sizeY = height / 2 + 40 / scaleFactor;
		guiLeft = width / 4 - 20 / scaleFactor;
		guiTop = height / 4 - 20 / scaleFactor;

		Minecraft.getMinecraft().getTextureManager().bindTexture(screenshotBorder);
		Utils.drawTexturedRect(guiLeft, guiTop, sizeX, sizeY);

		page = Math.max(0, Math.min(17, page));

		Minecraft.getMinecraft().getTextureManager().bindTexture(screenshots[page]);
		Utils.drawTexturedRect(
			guiLeft + 20f / scaleFactor,
			guiTop + 20f / scaleFactor,
			sizeX - 40f / scaleFactor,
			sizeY - 40f / scaleFactor
		);

		Utils.drawStringCentered(
			EnumChatFormatting.GOLD + title + " - Page " + (page + 1) + "/" + (texts.size()) + " - Use arrow keys",
			Minecraft.getMinecraft().fontRendererObj,
			width / 2,
			guiTop + 8,
			true,
			0
		);
		if (scaleFactor != 2)
			Utils.drawStringCentered(
				EnumChatFormatting.GOLD + "Use GUI Scale normal for better reading experience",
				Minecraft.getMinecraft().fontRendererObj,
				width / 2,
				guiTop + 18,
				true,
				0
			);
		JsonArray pageTexts = texts.get(page);
		for (int i = 0; i < pageTexts.size(); i++) {
			JsonObject textElement = pageTexts.get(i).getAsJsonObject();
			float oldX = textElement.get("x").getAsFloat();
			float oldY = textElement.get("y").getAsFloat();

			// List<String> text = entry.getValue();
			JsonArray textArray = textElement.getAsJsonArray("lines");
			List<String> text = new ArrayList<>();
			for (int j = 0; j < textArray.size(); j++) {
				text.add(textArray.get(j).getAsString());
			}

			float x = guiLeft + 20f / scaleFactor + (sizeX - 40f / scaleFactor) * oldX;
			float y = guiTop + 20f / scaleFactor + (sizeY - 40f / scaleFactor) * oldY;

			Utils.drawHoveringText(
				text,
				(int) x,
				(int) y + 12,
				100000,
				100000,
				200,
				Minecraft.getMinecraft().fontRendererObj
			);
		}

		drawButtons();
	}

	protected void drawButtons() {

		for (JsonObject button : buttons) {
			JsonArray pages = button.get("pages").getAsJsonArray();
			boolean drawButton = false;
			for (int i1 = 0; i1 < pages.size(); i1++) {
				if (pages.get(i1).getAsInt() == page) {
					drawButton = true;
					break;
				}
			}
			if (!drawButton) {
				continue;
			}
			float x = button.get("x").getAsFloat();
			float y = button.get("y").getAsFloat();
			String text = button.get("text").getAsString();
			// String command = button.get("command").getAsString();
			Minecraft.getMinecraft().getTextureManager().bindTexture(custom_ench_colour);
			GlStateManager.color(1, 1, 1, 1);
			Utils.drawTexturedRect(
				guiLeft + 20f / scaleFactor + (sizeX - 40f / scaleFactor) * x,
				guiTop + 20f / scaleFactor + (sizeY - 40f / scaleFactor) * y,
				88,
				20,
				64 / 217f,
				152 / 217f,
				48 / 78f,
				68 / 78f,
				GL11.GL_NEAREST
			);
			Utils.drawStringCenteredScaledMaxWidth(
				text,
				fontRendererObj,
				(guiLeft + 20f / scaleFactor + (sizeX - 40f / scaleFactor) * x) + 44,
				(guiTop + 20f / scaleFactor + (sizeY - 40f / scaleFactor) * y) + 10,
				false,
				86,
				4210752
			);

		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		int width = 88;
		int height = 20;

		for (JsonObject button : buttons) {
			JsonArray pages = button.get("pages").getAsJsonArray();
			boolean drawButton = false;
			for (int i1 = 0; i1 < pages.size(); i1++) {
				if (pages.get(i1).getAsInt() == page) {
					drawButton = true;
					break;
				}
			}
			if (!drawButton) {
				continue;
			}
			float x = button.get("x").getAsFloat();
			float y = button.get("y").getAsFloat();
			// String text = button.get("text").getAsString();
			float realX = guiLeft + 20f / scaleFactor + (sizeX - 40f / scaleFactor) * x;
			float realY = guiTop + 20f / scaleFactor + (sizeY - 40f / scaleFactor) * y;
			if (mouseX > realX && mouseX < realX + width && mouseY > realY && mouseY < realY + height) {
				String command = button.get("command").getAsString();
				NotEnoughUpdates.INSTANCE.openGui = null;
				ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, "/" + command);
				return;
			}
		}
	}

	protected static List<JsonArray> texts = new ArrayList<>();

	protected static List<JsonObject> buttons = new ArrayList<>();

	protected static JsonObject createNewButton(float x, float y, int[] pages, String text, String command) {
		JsonObject button = new JsonObject();
		JsonArray pagesArray = new JsonArray();
		for (int j : pages) {
			pagesArray.add(new JsonPrimitive(j));
		}
		button.add("pages", pagesArray);
		button.add("x", new JsonPrimitive(x));
		button.add("y", new JsonPrimitive(y));
		button.add("text", new JsonPrimitive(text));
		button.add("command", new JsonPrimitive(command));
		return button;
	}

	protected static JsonArray createNewTexts(JsonObject... texts) {
		JsonArray textArray = new JsonArray();
		for (JsonObject text : texts) {
			textArray.add(text);
		}
		return textArray;
	}

	protected static JsonObject createNewText(float x, float y, String... texts) {
		JsonObject tooltip = new JsonObject();
		tooltip.add("x", new JsonPrimitive(x));
		tooltip.add("y", new JsonPrimitive(y));
		JsonArray lines = new JsonArray();
		for (String text : texts) {
			lines.add(new JsonPrimitive(text));
		}
		tooltip.add("lines", lines);
		return tooltip;
	}

	protected static JsonObject createNewText(float x, float y, List<String> texts) {
		JsonObject tooltip = new JsonObject();
		tooltip.add("x", new JsonPrimitive(x));
		tooltip.add("y", new JsonPrimitive(y));
		JsonArray lines = new JsonArray();
		for (String text : texts) {
			lines.add(new JsonPrimitive(text));
		}
		tooltip.add("lines", lines);
		return tooltip;
	}

	//static {
	//	for (int i = 0; i < 18; i++) {
	//		texts[i] = new HashMap<>();
	//	}
	//	texts[0].put(new Vector2f(0.73f, 0.60f), Utils.createList(
	//		EnumChatFormatting.GOLD + "Itemlist",
	//		EnumChatFormatting.GRAY + "Here you will find a list of (most) skyblock items",
	//		EnumChatFormatting.GRAY + "The itemlist can be accessed by opening your inventory or most menus while on skyblock"
	//	));
	//	texts[1].put(new Vector2f(0.73f, 0.16f), Utils.createList(
	//		EnumChatFormatting.GOLD + "Itemlist",
	//		EnumChatFormatting.GRAY + "These are the page controls for the itemlist",
	//		EnumChatFormatting.GRAY + "Clicking these controls will bring you to other pages of the itemlist"
	//	));
	//	texts[2].put(new Vector2f(0.73f, 1.05f), Utils.createList(
	//		EnumChatFormatting.GOLD + "Itemlist",
	//		EnumChatFormatting.GRAY + "These are the sorting controls for the itemlist",
	//		EnumChatFormatting.GRAY + "The buttons on the left control the ordering of the items",
	//		EnumChatFormatting.GRAY + "The buttons on the right can be used to filter a certain type of item"
	//	));
	//	texts[3].put(new Vector2f(0.39f, 1.04f), Utils.createList(
	//		EnumChatFormatting.GOLD + "Itemlist",
	//		EnumChatFormatting.GRAY + "This is the search bar for the itemlist",
	//		EnumChatFormatting.GRAY + "Double-click the bar to enable inventory search mode",
	//		EnumChatFormatting.GRAY + "The button on the left opens up the mod settings",
	//		EnumChatFormatting.GRAY + "The button on the right displays this tutorial"
	//	));
	//	texts[4].put(new Vector2f(0.39f, 0.99f), Utils.createList(
	//		EnumChatFormatting.GOLD + "QuickCommands",
	//		EnumChatFormatting.GRAY + "These are the QuickCommands",
	//		EnumChatFormatting.GRAY + "They let you warp around or access certain menus more easily"
	//	));
	//	texts[5].put(new Vector2f(0.7f, 0.71f), Utils.createList(
	//		EnumChatFormatting.GOLD + "Itemlist",
	//		EnumChatFormatting.GRAY + "Hover over an item in the list to display it's lore",
	//		EnumChatFormatting.GRAY + "Left clicking some items will display the recipe for that item",
	//		EnumChatFormatting.GRAY + "Right clicking some items will display a wiki page for that item",
	//		EnumChatFormatting.GRAY + "'F' will favourite an item, putting it to the top of the itemlist"
	//	));
	//	texts[6].put(new Vector2f(0.17f, 0.21f), Utils.createList(
	//		EnumChatFormatting.GOLD + "Collection Log",
	//		EnumChatFormatting.GRAY +
	//			"This is the collection log. It can be accessed using the /neucl command, or via the QuickCommand",
	//		EnumChatFormatting.GRAY +
	//			"The collection log keeps track of all items that enter your inventory while you are playing skyblock",
	//		EnumChatFormatting.GRAY + "If you are a completionist, this feature is for you"
	//	));
	//	texts[7].put(new Vector2f(0.05f, 0.13f), Utils.createList(
	//		EnumChatFormatting.GOLD + "Collection Log",
	//		EnumChatFormatting.GRAY + "Clicking on 'Filter' will change the items that",
	//		EnumChatFormatting.GRAY + "appear in the list"
	//	));
	//	texts[8].put(new Vector2f(0.35f, 0.74f), Utils.createList(
	//		EnumChatFormatting.GOLD + "NeuAH",
	//		EnumChatFormatting.GRAY + "This is the NEU Auction House (NeuAH)",
	//		EnumChatFormatting.GRAY +
	//			"This AH can be accessed from anywhere using the /neuah command, or via the QuickCommand",
	//		EnumChatFormatting.GRAY +
	//			"The items here refresh automatically, so there is no need to close the GUI to see the latest auctions",
	//		EnumChatFormatting.GRAY + "Sometimes, you might have to wait until the list is populated with items from the API"
	//	));
	//	texts[9].put(new Vector2f(0.41f, 0.40f), Utils.createList(
	//		EnumChatFormatting.GOLD + "NeuAH",
	//		EnumChatFormatting.GRAY + "These tabs control the items that appear in NeuAH",
	//		EnumChatFormatting.GRAY +
	//			"You can find the main categories on the top of the GUI and subcategories appear on the side of the GUI once a main category is selected"
	//	));
	//	texts[10].put(new Vector2f(0.57f, 0.38f), Utils.createList(
	//		EnumChatFormatting.GOLD + "NeuAH",
	//		EnumChatFormatting.GRAY + "Search for items using the search bar at the top",
	//		EnumChatFormatting.GRAY + "Boolean operators such as &, | or ! work here."
	//	));
	//	texts[10].put(new Vector2f(0.40f, 0.72f), Utils.createList(
	//		EnumChatFormatting.GOLD + "NeuAH",
	//		EnumChatFormatting.GRAY + "This toolbar contains many useful features",
	//		EnumChatFormatting.GRAY + "which control the sorting and ordering of",
	//		EnumChatFormatting.GRAY + "the auction house, similar to the normal AH"
	//	));
	//	texts[11].put(new Vector2f(0.55f, 0.72f), Utils.createList(
	//		EnumChatFormatting.GOLD + "NeuAH",
	//		EnumChatFormatting.GRAY + "Clicking on an item will bring up the auction view",
	//		EnumChatFormatting.GRAY + "Here you can viewer the buyer/seller and place bids or make purchases",
	//		EnumChatFormatting.GRAY + "Trying to purchase an item will result in a confirmation GUI similar to the normal AH"
	//	));
	//	texts[12].put(new Vector2f(0.28f, 0.82f), Utils.createList(
	//		EnumChatFormatting.GOLD + "Profile Viewer",
	//		EnumChatFormatting.GRAY + "Access the profile viewer using /neuprofile (ign) or /pv (ign)",
	//		EnumChatFormatting.GRAY + "This is the main page of the profile viewer",
	//		EnumChatFormatting.GRAY + "This page contains basic information like stats and skill levels"
	//	));
	//	texts[12].put(new Vector2f(0.72f, 0.55f), Utils.createList(
	//		EnumChatFormatting.GOLD + "Profile Viewer",
	//		EnumChatFormatting.GRAY +
	//			"Click the button on the left to switch profiles and use the bar on the right to switch players"
	//	));
	//	texts[13].put(new Vector2f(0.28f, 0.82f), Utils.createList(
	//		EnumChatFormatting.GOLD + "Profile Viewer",
	//		EnumChatFormatting.GRAY + "This is the extra info page of the profile viewer",
	//		EnumChatFormatting.GRAY +
	//			"This page contains all the small bits of information about a player that don't fit anywhere else"
	//	));
	//	texts[14].put(new Vector2f(0.28f, 0.82f), Utils.createList(
	//		EnumChatFormatting.GOLD + "Profile Viewer",
	//		EnumChatFormatting.GRAY + "This is the inventories page of the profile viewer",
	//		EnumChatFormatting.GRAY +
	//			"Click on the inventory icons in the top-left or use your keyboard to switch the inventory type",
	//		EnumChatFormatting.GRAY + "The bar on the bottom-left searches the current inventory for matching items"
	//	));
	//	texts[15].put(new Vector2f(0.28f, 0.82f), Utils.createList(
	//		EnumChatFormatting.GOLD + "Profile Viewer",
	//		EnumChatFormatting.GRAY + "This is the collections page of the profile viewer",
	//		EnumChatFormatting.GRAY + "Click on the icons on the left or use the keyboard shortcut to switch collection type"
	//	));
	//	texts[16].put(new Vector2f(0.28f, 0.82f), Utils.createList(
	//		EnumChatFormatting.GOLD + "Profile Viewer",
	//		EnumChatFormatting.GRAY + "This is the pets page of the profile viewer",
	//		EnumChatFormatting.GRAY + "Click to select the pet on the left",
	//		EnumChatFormatting.GRAY + "The selected pet's stats will display on the right"
	//	));
	//	texts[17].put(new Vector2f(0.27f, 0.40f), Utils.createList(
	//		EnumChatFormatting.GOLD + "Overlay",
	//		EnumChatFormatting.GRAY + "Rearrange certain GUI elements of the main overlay using /neuoverlay",
	//		EnumChatFormatting.GRAY +
	//			"If you accidentally move them off screen, use the button in the top left to reset the GUI"
	//	));
	//}
}
