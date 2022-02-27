package io.github.moulberry.notenoughupdates.miscgui.tutorials;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class NeuTutorial extends TutorialBase {
	static {
		title = "NEU Tutorial";
	}

	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		screenshots = new ResourceLocation[18];
		for (int i = 0; i <= 17; i++) {
			screenshots[i] = new ResourceLocation("notenoughupdates:ss_small/ss" + (i + 1) + "-0.jpg");
		}
	}

	//static {
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
	//
	//static {
	//	buttons = new ArrayList<JsonObject>() {{
	//		add(createNewButton(0.27f, 0.40f, new int[]{1, 2}, "TESTSHIT", "neu"));
	//	}};
	//}
}
