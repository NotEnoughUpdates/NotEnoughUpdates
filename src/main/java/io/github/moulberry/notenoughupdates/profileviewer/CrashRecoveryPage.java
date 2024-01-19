/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.profileviewer;

import io.github.moulberry.moulconfig.internal.ClipboardUtils;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.util.Rectangle;
import io.github.moulberry.notenoughupdates.util.Utils;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.init.Bootstrap;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CrashRecoveryPage extends GuiProfileViewerPage {
	private final Exception exception;
	private final String timestamp;
	private final GuiProfileViewer.ProfileViewerPage lastViewedPage;
	private int offset = 0;
	private final CrashReport crashReport;

	public CrashRecoveryPage(
		GuiProfileViewer instance,
		Exception exception,
		GuiProfileViewer.ProfileViewerPage lastViewedPage
	) {
		super(instance);
		this.lastViewedPage = lastViewedPage;
		this.timestamp = DateTimeFormatter.ISO_ZONED_DATE_TIME.format(OffsetDateTime.ofInstant(
			Instant.now(),
			ZoneId.systemDefault()
		));
		this.exception = exception;
		val profile = GuiProfileViewer.getProfile();
		crashReport = new CrashReport("NEU Profile Viewer crashed", exception);
		val parameters = crashReport.makeCategory("Profile Viewer Parameters");

		parameters.addCrashSection("Viewed Player", (profile == null ? "null" : profile.getUuid()));
		parameters.addCrashSection("Viewed Profile", GuiProfileViewer.getProfileName());
		parameters.addCrashSection("Timestamp", timestamp);
		parameters.addCrashSection("Last Viewed Page", lastViewedPage);
		Bootstrap.printToSYSOUT(crashReport.getCompleteReport());
	}

	@Override
	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(
			GuiProfileViewer.getGuiLeft() + getInstance().sizeX / 2f,
			GuiProfileViewer.getGuiTop() + 20,
			0
		);
		offset = 20;

		drawTitle();

		drawString("§cLooked like your profile viewer crashed.");
		drawString("§cPlease immediately send a screenshot of this screen into #neu-support.");
		drawString("§cJoin our support server at §adiscord.gg/moulberry§c.");

		val profile = GuiProfileViewer.getProfile();
		drawString("Viewed Player: " + (profile == null ? "null" : profile.getUuid()));
		drawString("Viewed Profile: " + GuiProfileViewer.getProfileName());
		drawString("Timestamp: " + timestamp);

		drawString("");
		drawString(exception.toString());
		for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
			if (offset >= getInstance().sizeY - 50) break;
			drawString(stackTraceElement.toString());
		}

		GlStateManager.popMatrix();

		val buttonCoords = getButtonCoordinates();
		RenderUtils.drawFloatingRectWithAlpha(
			buttonCoords.getX(), buttonCoords.getY(),
			buttonCoords.getWidth(), buttonCoords.getHeight(),
			100, true
		);
		Utils.drawStringCenteredScaledMaxWidth(
			"Copy Report",
			buttonCoords.getCenterX(),
			buttonCoords.getCenterY(),
			false,
			buttonCoords.getWidth(),
			-1
		);

	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (getButtonCoordinates().contains(mouseX, mouseY) && mouseButton == 0) {
			ClipboardUtils.copyToClipboard(crashReport.getCompleteReport());
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private Rectangle getButtonCoordinates() {
		return new Rectangle(
			GuiProfileViewer.getGuiLeft() + getInstance().sizeX / 2 - 40,
			GuiProfileViewer.getGuiTop() + getInstance().sizeY - 30,
			80, 12
		);
	}

	private void drawString(String text) {
		Utils.drawStringCenteredScaledMaxWidth(text, 0, 0, false, getInstance().sizeX - 20, -1);
		val spacing = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 2;
		GlStateManager.translate(0, spacing, 0);
		offset += spacing;
	}

	private void drawTitle() {
		GlStateManager.pushMatrix();
		GlStateManager.scale(2, 2, 2);
		Utils.drawStringCenteredScaledMaxWidth("§cKA-BOOM!", 0, 0, false, getInstance().sizeX / 2, -1);
		GlStateManager.popMatrix();
		val spacing = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * 2 + 6;
		GlStateManager.translate(0, spacing, 0);
		offset += spacing;
	}
}
