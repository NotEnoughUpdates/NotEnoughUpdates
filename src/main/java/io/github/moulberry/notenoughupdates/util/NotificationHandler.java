package io.github.moulberry.notenoughupdates.util;

import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ContainerChest;

import java.util.List;

public class NotificationHandler {
	public static List<String> notificationLines = null;
	public static boolean showNotificationOverInv = false;
	public static long notificationDisplayMillis = 0;

	public static void displayNotification(List<String> lines, boolean showForever) {
		displayNotification(lines, showForever, false);
	}

	public static void displayNotification(List<String> lines, boolean showForever, boolean overInventory) {
		if (showForever) {
			notificationDisplayMillis = -420;
		} else {
			notificationDisplayMillis = System.currentTimeMillis();
		}
		notificationLines = lines;
		showNotificationOverInv = overInventory;
	}

	public static void renderNotification() {
		long timeRemaining = 15000 - (System.currentTimeMillis() - notificationDisplayMillis);
		boolean display = timeRemaining > 0 || notificationDisplayMillis == -420;
		if (display && notificationLines != null && notificationLines.size() > 0) {
			int width = 0;
			int height = notificationLines.size() * 10 + 10;

			for (String line : notificationLines) {
				int len = Minecraft.getMinecraft().fontRendererObj.getStringWidth(line) + 8;
				if (len > width) {
					width = len;
				}
			}

			ScaledResolution sr = Utils.pushGuiScale(2);

			int midX = sr.getScaledWidth() / 2;
			int topY = sr.getScaledHeight() * 3 / 4 - height / 2;
			RenderUtils.drawFloatingRectDark(midX - width / 2, sr.getScaledHeight() * 3 / 4 - height / 2, width, height);
            /*Gui.drawRect(midX-width/2, sr.getScaledHeight()*3/4-height/2,
                    midX+width/2, sr.getScaledHeight()*3/4+height/2, 0xFF3C3C3C);
            Gui.drawRect(midX-width/2+2, sr.getScaledHeight()*3/4-height/2+2,
                    midX+width/2-2, sr.getScaledHeight()*3/4+height/2-2, 0xFFC8C8C8);*/

			int xLen = Minecraft.getMinecraft().fontRendererObj.getStringWidth("[X] Close");
			Minecraft.getMinecraft().fontRendererObj.drawString(
				"[X] Close",
				midX + width / 2f - 3 - xLen,
				topY + 3,
				0xFFFF5555,
				false
			);

			if (notificationDisplayMillis > 0) {
				Minecraft.getMinecraft().fontRendererObj.drawString(
					(timeRemaining / 1000) + "s",
					midX - width / 2f + 3,
					topY + 3,
					0xFFaaaaaa,
					false
				);
			}

			Utils.drawStringCentered(
				notificationLines.get(0),
				Minecraft.getMinecraft().fontRendererObj,
				midX,
				topY + 4 + 5,
				false,
				-1
			);
			for (int i = 1; i < notificationLines.size(); i++) {
				String line = notificationLines.get(i);
				Utils.drawStringCentered(
					line,
					Minecraft.getMinecraft().fontRendererObj,
					midX,
					topY + 4 + 5 + 2 + i * 10,
					false,
					-1
				);
			}

			Utils.pushGuiScale(-1);
		}
	}

	public static boolean shouldRenderOverlay(Gui gui) {
		boolean validGui = gui instanceof GuiContainer || gui instanceof GuiItemRecipe;
		if (gui instanceof GuiChest) {
			GuiChest eventGui = (GuiChest) gui;
			ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
			String containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();
			if (containerName.trim().equals("Fast Travel")) {
				validGui = false;
			}
		}
		return validGui;
	}
}
