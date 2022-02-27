package io.github.moulberry.notenoughupdates.gamemodes;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.github.moulberry.notenoughupdates.util.GuiTextures.*;

public class GuiGamemodes extends GuiScreen {
	private final String currentProfile;
	private SBGamemodes.Gamemode currentGamemode = null;
	private final boolean upgradeOverride;

	private int guiLeft = 100;
	private int guiTop = 100;
	private final int xSize = 200;
	private final int ySize = 232;

	public GuiGamemodes(boolean upgradeOverride) {
		this.currentProfile = NotEnoughUpdates.INSTANCE.manager.getCurrentProfile();
		this.upgradeOverride = upgradeOverride;
	}

	private boolean canChange(int from, int to) {
		if (from >= to) {
			return true;
		} else {
			return !currentGamemode.locked || upgradeOverride;
		}
	}

	@Override
	public void updateScreen() {
		if (this.currentProfile == null) {
			Minecraft.getMinecraft().displayGuiScreen(null);
			Minecraft.getMinecraft().thePlayer.addChatMessage(
				new ChatComponentText(EnumChatFormatting.RED + "Couldn't detect current profile. Maybe try later?"));
		}

		if (currentGamemode == null) {
			currentGamemode = SBGamemodes.getGamemode();
			if (currentGamemode == null) {
				Minecraft.getMinecraft().displayGuiScreen(null);
				Minecraft.getMinecraft().thePlayer.addChatMessage(
					new ChatComponentText(EnumChatFormatting.RED + "Couldn't automatically detect current profile." +
						"If you have only 1 profile, try using /api new so that NEU can detect your profile."));
			}
		}

		String currentProfile = NotEnoughUpdates.INSTANCE.manager.getCurrentProfile();
		if (!this.currentProfile.equals(currentProfile)) {
			Minecraft.getMinecraft().displayGuiScreen(null);
			Minecraft.getMinecraft().thePlayer.addChatMessage(
				new ChatComponentText(EnumChatFormatting.RED + "Profile change detected. Closing gamemodes menu."));
		}
	}

	@Override
	public void handleKeyboardInput() throws IOException {
		if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
			SBGamemodes.saveToFile();
		}

		super.handleKeyboardInput();
	}

	public void drawStringShadow(String str, float x, float y, int len) {
		for (int xOff = -2; xOff <= 2; xOff++) {
			for (int yOff = -2; yOff <= 2; yOff++) {
				if (Math.abs(xOff) != Math.abs(yOff)) {
					Utils.drawStringScaledMaxWidth(Utils.cleanColourNotModifiers(str),
						Minecraft.getMinecraft().fontRendererObj,
						x + xOff / 2f, y + yOff / 2f, false, len,
						new Color(20, 20, 20, 100 / Math.max(Math.abs(xOff), Math.abs(yOff))).getRGB()
					);
				}
			}
		}

		Utils.drawStringScaledMaxWidth(str,
			Minecraft.getMinecraft().fontRendererObj,
			x, y, false, len,
			new Color(64, 64, 64, 255).getRGB()
		);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mouseButton == 0) {
			SBGamemodes.HardcoreMode setHC = SBGamemodes.HardcoreMode.NORMAL;
			SBGamemodes.IronmanMode setIM = SBGamemodes.IronmanMode.NORMAL;
			int setMod = 0;

			if (mouseX > guiLeft + xSize - 27 && mouseX < guiLeft + xSize - 9) {
				if (mouseY > guiTop + 30 && mouseY < guiTop + 30 + 16) {
					setHC = SBGamemodes.HardcoreMode.SOFTCORE;
				} else if (mouseY > guiTop + 50 && mouseY < guiTop + 50 + 16) {
					setHC = SBGamemodes.HardcoreMode.HARDCORE;
				} else if (mouseY > guiTop + 80 && mouseY < guiTop + 80 + 16) {
					setIM = SBGamemodes.IronmanMode.IRONMAN;
				} else if (mouseY > guiTop + 100 && mouseY < guiTop + 100 + 16) {
					setIM = SBGamemodes.IronmanMode.IRONMANPLUS;
				} else if (mouseY > guiTop + 120 && mouseY < guiTop + 120 + 16) {
					setIM = SBGamemodes.IronmanMode.ULTIMATE_IRONMAN;
				} else if (mouseY > guiTop + 140 && mouseY < guiTop + 140 + 16) {
					setIM = SBGamemodes.IronmanMode.ULTIMATE_IRONMANPLUS;
				} else if (mouseY > guiTop + 170 && mouseY < guiTop + 170 + 16) {
					setMod = SBGamemodes.MODIFIER_DEVILISH;
				} else if (mouseY > guiTop + 190 && mouseY < guiTop + 190 + 16) {
					setMod = SBGamemodes.MODIFIER_NOBANK;
				} else if (mouseY > guiTop + 210 && mouseY < guiTop + 210 + 16) {
					setMod = SBGamemodes.MODIFIER_SMALLISLAND;
				}
			}

			if (setHC != SBGamemodes.HardcoreMode.NORMAL) {
				if (currentGamemode.hardcoreMode == setHC) {
					currentGamemode.hardcoreMode = SBGamemodes.HardcoreMode.NORMAL;
				} else {
					if (canChange(currentGamemode.hardcoreMode.ordinal(), setHC.ordinal())) {
						currentGamemode.hardcoreMode = setHC;
					}
				}
			} else if (setIM != SBGamemodes.IronmanMode.NORMAL) {
				if (currentGamemode.ironmanMode == setIM) {
					currentGamemode.ironmanMode = SBGamemodes.IronmanMode.NORMAL;
				} else {
					if (canChange(currentGamemode.ironmanMode.ordinal(), setIM.ordinal())) {
						currentGamemode.ironmanMode = setIM;
					}
				}
			} else if (setMod != 0) {
				if (canChange(currentGamemode.gamemodeModifiers, currentGamemode.gamemodeModifiers ^ setMod)) {
					currentGamemode.gamemodeModifiers ^= setMod;
				}
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawDefaultBackground();

		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;

		GlStateManager.color(1, 1, 1, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(gamemodes);
		Utils.drawTexturedRect(guiLeft, guiTop, xSize, ySize, GL11.GL_NEAREST);

		if (currentGamemode == null) return;

		Utils.drawStringCentered("NEU Skyblock Gamemodes", Minecraft.getMinecraft().fontRendererObj,
			guiLeft + xSize / 2f, guiTop + 14, false, new Color(64, 64, 64).getRGB()
		);

		drawStringShadow(SBGamemodes.HardcoreMode.SOFTCORE.display, guiLeft + 10, guiTop + 30, xSize - 47);
		drawStringShadow(SBGamemodes.HardcoreMode.HARDCORE.display, guiLeft + 10, guiTop + 50, xSize - 47);

		drawStringShadow(SBGamemodes.IronmanMode.IRONMAN.display, guiLeft + 10, guiTop + 80, xSize - 47);
		drawStringShadow(SBGamemodes.IronmanMode.IRONMANPLUS.display, guiLeft + 10, guiTop + 100, xSize - 47);
		drawStringShadow(SBGamemodes.IronmanMode.ULTIMATE_IRONMAN.display, guiLeft + 10, guiTop + 120, xSize - 47);
		drawStringShadow(SBGamemodes.IronmanMode.ULTIMATE_IRONMANPLUS.display, guiLeft + 10, guiTop + 140, xSize - 47);

		drawStringShadow(SBGamemodes.MODIFIER_DEVILISH_DISPLAY, guiLeft + 10, guiTop + 170, xSize - 47);
		drawStringShadow(SBGamemodes.MODIFIER_NOBANK_DISPLAY, guiLeft + 10, guiTop + 190, xSize - 47);
		drawStringShadow(SBGamemodes.MODIFIER_SMALLISLAND_DISPLAY, guiLeft + 10, guiTop + 210, xSize - 47);

		String tooltipToDisplay = null;

		GlStateManager.color(1, 1, 1, 1);
		if (canChange(currentGamemode.hardcoreMode.ordinal(), SBGamemodes.HardcoreMode.SOFTCORE.ordinal())) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(
				currentGamemode.hardcoreMode == SBGamemodes.HardcoreMode.SOFTCORE ? radial_circle_on : radial_circle_off);
			Utils.drawTexturedRect(guiLeft + xSize - 26, guiTop + 30 - 4, 16, 16, GL11.GL_NEAREST);

			Minecraft.getMinecraft().getTextureManager().bindTexture(help);
			Utils.drawTexturedRect(guiLeft + xSize - 47, guiTop + 30 - 4, 16, 16, GL11.GL_NEAREST);
			if (mouseX > guiLeft + xSize - 47 && mouseX < guiLeft + xSize - 31) {
				if (mouseY > guiTop + 30 - 4 && mouseY < guiTop + 30 + 12) {
					tooltipToDisplay = SBGamemodes.HardcoreMode.SOFTCORE.desc;
				}
			}
		}
		if (canChange(currentGamemode.hardcoreMode.ordinal(), SBGamemodes.HardcoreMode.HARDCORE.ordinal())) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(
				currentGamemode.hardcoreMode == SBGamemodes.HardcoreMode.HARDCORE ? radial_circle_on : radial_circle_off);
			Utils.drawTexturedRect(guiLeft + xSize - 26, guiTop + 50 - 4, 16, 16, GL11.GL_NEAREST);

			Minecraft.getMinecraft().getTextureManager().bindTexture(help);
			Utils.drawTexturedRect(guiLeft + xSize - 47, guiTop + 50 - 4, 16, 16, GL11.GL_NEAREST);
			if (mouseX > guiLeft + xSize - 47 && mouseX < guiLeft + xSize - 31) {
				if (mouseY > guiTop + 50 - 4 && mouseY < guiTop + 50 + 12) {
					tooltipToDisplay = SBGamemodes.HardcoreMode.HARDCORE.desc;
				}
			}
		}

		if (canChange(currentGamemode.ironmanMode.ordinal(), SBGamemodes.IronmanMode.IRONMAN.ordinal())) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(
				currentGamemode.ironmanMode == SBGamemodes.IronmanMode.IRONMAN ? radial_circle_on : radial_circle_off);
			Utils.drawTexturedRect(guiLeft + xSize - 26, guiTop + 80 - 4, 16, 16, GL11.GL_NEAREST);

			Minecraft.getMinecraft().getTextureManager().bindTexture(help);
			Utils.drawTexturedRect(guiLeft + xSize - 47, guiTop + 80 - 4, 16, 16, GL11.GL_NEAREST);
			if (mouseX > guiLeft + xSize - 47 && mouseX < guiLeft + xSize - 31) {
				if (mouseY > guiTop + 80 - 4 && mouseY < guiTop + 80 + 12) {
					tooltipToDisplay = SBGamemodes.IronmanMode.IRONMAN.desc;
				}
			}
		}
		if (canChange(currentGamemode.ironmanMode.ordinal(), SBGamemodes.IronmanMode.IRONMANPLUS.ordinal())) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(
				currentGamemode.ironmanMode == SBGamemodes.IronmanMode.IRONMANPLUS ? radial_circle_on : radial_circle_off);
			Utils.drawTexturedRect(guiLeft + xSize - 26, guiTop + 100 - 4, 16, 16, GL11.GL_NEAREST);

			Minecraft.getMinecraft().getTextureManager().bindTexture(help);
			Utils.drawTexturedRect(guiLeft + xSize - 47, guiTop + 100 - 4, 16, 16, GL11.GL_NEAREST);
			if (mouseX > guiLeft + xSize - 47 && mouseX < guiLeft + xSize - 31) {
				if (mouseY > guiTop + 100 - 4 && mouseY < guiTop + 100 + 12) {
					tooltipToDisplay = SBGamemodes.IronmanMode.IRONMANPLUS.desc;
				}
			}
		}
		if (canChange(currentGamemode.ironmanMode.ordinal(), SBGamemodes.IronmanMode.ULTIMATE_IRONMAN.ordinal())) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(
				currentGamemode.ironmanMode == SBGamemodes.IronmanMode.ULTIMATE_IRONMAN ? radial_circle_on : radial_circle_off);
			Utils.drawTexturedRect(guiLeft + xSize - 26, guiTop + 120 - 4, 16, 16, GL11.GL_NEAREST);

			Minecraft.getMinecraft().getTextureManager().bindTexture(help);
			Utils.drawTexturedRect(guiLeft + xSize - 47, guiTop + 120 - 4, 16, 16, GL11.GL_NEAREST);
			if (mouseX > guiLeft + xSize - 47 && mouseX < guiLeft + xSize - 31) {
				if (mouseY > guiTop + 120 - 4 && mouseY < guiTop + 120 + 12) {
					tooltipToDisplay = SBGamemodes.IronmanMode.ULTIMATE_IRONMAN.desc;
				}
			}
		}
		if (canChange(currentGamemode.ironmanMode.ordinal(), SBGamemodes.IronmanMode.ULTIMATE_IRONMANPLUS.ordinal())) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(
				currentGamemode.ironmanMode == SBGamemodes.IronmanMode.ULTIMATE_IRONMANPLUS
					? radial_circle_on
					: radial_circle_off);
			Utils.drawTexturedRect(guiLeft + xSize - 26, guiTop + 140 - 4, 16, 16, GL11.GL_NEAREST);

			Minecraft.getMinecraft().getTextureManager().bindTexture(help);
			Utils.drawTexturedRect(guiLeft + xSize - 47, guiTop + 140 - 4, 16, 16, GL11.GL_NEAREST);
			if (mouseX > guiLeft + xSize - 47 && mouseX < guiLeft + xSize - 31) {
				if (mouseY > guiTop + 140 - 4 && mouseY < guiTop + 140 + 12) {
					tooltipToDisplay = SBGamemodes.IronmanMode.ULTIMATE_IRONMANPLUS.desc;
				}
			}
		}

		if (canChange(
			currentGamemode.gamemodeModifiers,
			currentGamemode.gamemodeModifiers ^ SBGamemodes.MODIFIER_DEVILISH
		)) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(
				(currentGamemode.gamemodeModifiers & SBGamemodes.MODIFIER_DEVILISH) != 0
					? radial_square_on
					: radial_square_off);
			Utils.drawTexturedRect(guiLeft + xSize - 26, guiTop + 170 - 4, 16, 16, GL11.GL_NEAREST);

			Minecraft.getMinecraft().getTextureManager().bindTexture(help);
			Utils.drawTexturedRect(guiLeft + xSize - 47, guiTop + 170 - 4, 16, 16, GL11.GL_NEAREST);
			if (mouseX > guiLeft + xSize - 47 && mouseX < guiLeft + xSize - 31) {
				if (mouseY > guiTop + 170 - 4 && mouseY < guiTop + 170 + 12) {
					tooltipToDisplay = SBGamemodes.MODIFIER_DEVILISH_DESC;
				}
			}
		}
		if (canChange(currentGamemode.gamemodeModifiers, currentGamemode.gamemodeModifiers ^ SBGamemodes.MODIFIER_NOBANK)) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(
				(currentGamemode.gamemodeModifiers & SBGamemodes.MODIFIER_NOBANK) != 0 ? radial_square_on : radial_square_off);
			Utils.drawTexturedRect(guiLeft + xSize - 26, guiTop + 190 - 4, 16, 16, GL11.GL_NEAREST);

			Minecraft.getMinecraft().getTextureManager().bindTexture(help);
			Utils.drawTexturedRect(guiLeft + xSize - 47, guiTop + 190 - 4, 16, 16, GL11.GL_NEAREST);
			if (mouseX > guiLeft + xSize - 47 && mouseX < guiLeft + xSize - 31) {
				if (mouseY > guiTop + 190 - 4 && mouseY < guiTop + 190 + 12) {
					tooltipToDisplay = SBGamemodes.MODIFIER_NOBANK_DESC;
				}
			}
		}
		if (canChange(
			currentGamemode.gamemodeModifiers,
			currentGamemode.gamemodeModifiers ^ SBGamemodes.MODIFIER_SMALLISLAND
		)) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(
				(currentGamemode.gamemodeModifiers & SBGamemodes.MODIFIER_SMALLISLAND) != 0
					? radial_square_on
					: radial_square_off);
			Utils.drawTexturedRect(guiLeft + xSize - 26, guiTop + 210 - 4, 16, 16, GL11.GL_NEAREST);

			Minecraft.getMinecraft().getTextureManager().bindTexture(help);
			Utils.drawTexturedRect(guiLeft + xSize - 47, guiTop + 210 - 4, 16, 16, GL11.GL_NEAREST);
			if (mouseX > guiLeft + xSize - 47 && mouseX < guiLeft + xSize - 31) {
				if (mouseY > guiTop + 210 - 4 && mouseY < guiTop + 210 + 12) {
					tooltipToDisplay = SBGamemodes.MODIFIER_SMALLISLAND_DESC;
				}
			}
		}

		if (tooltipToDisplay != null) {
			List<String> lines = new ArrayList<>();
			for (String line : tooltipToDisplay.split("\n")) {
				lines.add(EnumChatFormatting.GRAY + line);
			}
			Utils.drawHoveringText(lines, mouseX, mouseY, width, height, -1, Minecraft.getMinecraft().fontRendererObj);
		}
	}
}
