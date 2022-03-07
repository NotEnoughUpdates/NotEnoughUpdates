package io.github.moulberry.notenoughupdates.profileviewer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BingoPage {
	private static final ResourceLocation BINGO_GUI_TEXTURE = new ResourceLocation("notenoughupdates:pv_bingo_tab.png");
	private static long lastResourceRequest;
	private static JsonObject bingoResourcesObject;
	private static List<JsonObject> bingoGoals;
	private static int currentEventId;

	public static void renderPage(int mouseX, int mouseY, float partialTicks) {
		getBingoResources();
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();
		JsonObject bingoInfo = GuiProfileViewer.getProfile().getBingoInformation();

		//check if the player has created a bingo profile for the current event
		if (bingoInfo == null) {
			showMissingDataMessage(guiLeft, guiTop);
			return;
		}

		JsonArray events = bingoInfo.get("events").getAsJsonArray();
		JsonObject lastEvent = events.get(events.size() - 1).getAsJsonObject();
		int lastParticipatedId = lastEvent.get("key").getAsInt();
		if (currentEventId != lastParticipatedId) {
			showMissingDataMessage(guiLeft, guiTop);
			return;
		}

		List<String> completedGoals = jsonArrayToStringList(lastEvent.get("completed_goals").getAsJsonArray());
		Minecraft.getMinecraft().getTextureManager().bindTexture(BINGO_GUI_TEXTURE);
		Utils.drawTexturedRect(guiLeft, guiTop, 431, 202, GL11.GL_NEAREST);

		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.disableLighting();

		int row = 0;
		int col = 0;
		int initalY = guiTop + 41; //jani
		int initalX = guiLeft + 234;
		for (JsonObject bingoGoal : bingoGoals) {
			ItemStack itemStack = new ItemStack(Items.paper);
			int x = col == 0 ? initalX : initalX + (18 * col);
			int y = row == 0 ? initalY : initalY + (18 * row);

			Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(itemStack, x, y);
			col++;
			if (col == 5) {
				col = 0;
				row++;
			}
		}

//		ItemStack itemStack = new ItemStack(Items.paper);
//		ItemStack itemStack2 = new ItemStack(Items.iron_ingot);
//		Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(itemStack, guiLeft + 50, guiTop + 50);
//		Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(itemStack2, guiLeft + 70, guiTop + 50);
		GlStateManager.enableLighting();
	}

	private static void showMissingDataMessage(int guiLeft, int guiTop) {
		String message = EnumChatFormatting.RED + "No Bingo data for current event!";
		Utils.drawStringCentered(message, Minecraft.getMinecraft().fontRendererObj,
			guiLeft + 431 / 2f, guiTop + 101, true, 0
		);
	}

	private static List<String> jsonArrayToStringList(JsonArray completedGoals) {
		List<String> list = new ArrayList<>();
		for (JsonElement completedGoal : completedGoals) {
			list.add(completedGoal.getAsString());
		}
		return list;
	}

	private static List<JsonObject> jsonArrayToJsonObjectList(JsonArray goals) {
		List<JsonObject> list = new ArrayList<>();
		for (JsonElement goal : goals) {
			System.out.println(goal);
			list.add(goal.getAsJsonObject());
		}

		return list;
	}

	private static void getBingoResources() {
		if (bingoGoals != null) return;

		long currentTime = System.currentTimeMillis();

		if (currentTime - lastResourceRequest < 15 * 1000) return;
		lastResourceRequest = currentTime;

		HashMap<String, String> args = new HashMap<>();
		System.out.println("iajudfiawdiawjdia");
		NotEnoughUpdates.INSTANCE.manager.hypixelApi.getHypixelApiAsync(
			NotEnoughUpdates.INSTANCE.config.apiKey.apiKey,
			"resources/skyblock/bingo",
			args,
			jsonObject -> {
				System.out.println("uweuhjhkacjnk");
				if (jsonObject.has("success") && jsonObject.get("success").getAsBoolean()) {
					bingoGoals = jsonArrayToJsonObjectList(jsonObject.get("goals").getAsJsonArray());
					currentEventId = jsonObject.get("id").getAsInt();
				}
			},
			() -> {}
		);
	}
}
