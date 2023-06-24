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

package io.github.moulberry.notenoughupdates.cosmetics;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.GuiElementTextField;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.GuiTextures;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Matrix4f;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer.pv_bg;
import static io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer.pv_dropdown;
import static io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer.pv_elements;

public class GuiCosmetics extends GuiScreen {
	public static final ResourceLocation cosmetics_fg = new ResourceLocation("notenoughupdates:cosmetics_fg.png");

	private final GuiElementTextField unlockTextField = new GuiElementTextField("", GuiElementTextField.SCALE_TEXT);

	private CosmeticsPage currentPage = CosmeticsPage.CAPES;
	private int sizeX;
	private int sizeY;
	private int guiLeft;
	private int guiTop;

	private String wantToEquipCape = null;
	private long lastCapeEquip = 0;

	private List<String> cosmeticsInfoTooltip = null;

	public GuiCosmetics() {
		Gson gson = new Gson();

		JsonElement cosmeticHelpTextElement = Utils.getElement(Constants.MISC, "cosmeticsinfo.lore");
		if (cosmeticHelpTextElement.isJsonArray()) {
			cosmeticsInfoTooltip = gson.fromJson(cosmeticHelpTextElement, new TypeToken<List<String>>() {}.getType());
		}

	}

	public enum CosmeticsPage {
		CAPES(new ItemStack(Items.chainmail_chestplate));

		public final ItemStack stack;

		CosmeticsPage(ItemStack stack) {
			this.stack = stack;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.sizeX = 431;
		this.sizeY = 202;
		this.guiLeft = (this.width - this.sizeX) / 2;
		this.guiTop = (this.height - this.sizeY) / 2;

		super.drawScreen(mouseX, mouseY, partialTicks);
		drawDefaultBackground();

		blurBackground();
		renderBlurredBackground(width, height, guiLeft + 2, guiTop + 2, sizeX - 4, sizeY - 4);

		GlStateManager.enableDepth();
		GlStateManager.translate(0, 0, 5);
		renderTabs(true);
		GlStateManager.translate(0, 0, -3);

		GlStateManager.disableDepth();
		GlStateManager.translate(0, 0, -2);
		renderTabs(false);
		GlStateManager.translate(0, 0, 2);

		GlStateManager.disableLighting();
		GlStateManager.enableDepth();
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_bg);
		Utils.drawTexturedRect(guiLeft, guiTop, sizeX, sizeY, GL11.GL_NEAREST);

		GlStateManager.color(1, 1, 1, 1);
		switch (currentPage) {
			case CAPES:
				drawCapesPage(mouseX, mouseY, partialTicks);
				break;
		}
		int helpX = guiLeft + sizeX - 20;
		if (mouseX >= helpX && mouseX <= helpX + 20 && mouseY >= guiTop - 20 && mouseY <= guiTop) {
			if (cosmeticsInfoTooltip != null) {
				List<String> grayTooltip = new ArrayList<>(cosmeticsInfoTooltip.size());
				for (String line : cosmeticsInfoTooltip) {
					grayTooltip.add(EnumChatFormatting.GRAY + line);
				}
				Utils.drawHoveringText(grayTooltip, mouseX, mouseY, width, height, -1);}
		}

		StringBuilder statusMsg = new StringBuilder("Last Sync: ");
		if (CapeManager.INSTANCE.lastCapeSynced == 0) {
			statusMsg.append("Not Synced");
		} else {
			statusMsg.append((System.currentTimeMillis() - CapeManager.INSTANCE.lastCapeSynced) / 1000).append("s ago");
		}
		statusMsg.append(" - Next Sync: ");
		if (CapeManager.INSTANCE.lastCapeUpdate == 0) {
			statusMsg.append("ASAP");
		} else {
			statusMsg.append(60 - (System.currentTimeMillis() - CapeManager.INSTANCE.lastCapeUpdate) / 1000).append("s");
		}

		Minecraft.getMinecraft().fontRendererObj.drawString(
			EnumChatFormatting.AQUA + statusMsg.toString(),
			guiLeft + sizeX - Minecraft.getMinecraft().fontRendererObj.getStringWidth(statusMsg.toString()) - 20,
			guiTop - 12,
			0,
			true
		);

		if (currentPage == CosmeticsPage.CAPES) {
			GlStateManager.color(1, 1, 1, 1);
			Minecraft.getMinecraft().getTextureManager().bindTexture(pv_dropdown);
			Utils.drawTexturedRect(
				guiLeft + sizeX / 2f - 50,
				guiTop + sizeY + 5,
				100,
				20,
				0,
				100 / 200f,
				0,
				20 / 185f,
				GL11.GL_NEAREST
			);

			String equipMsg;
			if (wantToEquipCape != null) {
				equipMsg = EnumChatFormatting.GREEN + "Equip Cape";
			} else {
				equipMsg = EnumChatFormatting.GREEN + "Unequip";
			}
			if (System.currentTimeMillis() - lastCapeEquip < 20 * 1000) {
				equipMsg += " - " + (20 - (System.currentTimeMillis() - lastCapeEquip) / 1000) + "s";
			}

			Utils.drawStringCenteredScaledMaxWidth(equipMsg, guiLeft + sizeX / 2f, guiTop + sizeY + 5 + 10, false, 90, 0);
		}

		if (unlockTextField.getFocus() || !unlockTextField.getText().isEmpty()) {
			unlockTextField.setPrependText("");
		} else {
			unlockTextField.setPrependText("\u00a77Creator Code");
		}

		unlockTextField.setSize(80, 20);
		unlockTextField.render(guiLeft + sizeX - 80, guiTop + sizeY + 2);

		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.help);
		GlStateManager.color(1, 1, 1, 1);
		Utils.drawTexturedRect(helpX, guiTop - 20, 20, 20, GL11.GL_LINEAR);

	}

	private void renderTabs(boolean renderPressed) {
		int ignoredTabs = 0;
		for (int i = 0; i < CosmeticsPage.values().length; i++) {
			CosmeticsPage page = CosmeticsPage.values()[i];
			if (page.stack == null) {
				ignoredTabs++;
				continue;
			}
			boolean pressed = page == currentPage;
			if (pressed == renderPressed) {
				renderTab(page.stack, i - ignoredTabs, pressed);
			}
		}
	}

	private void renderTab(ItemStack stack, int xIndex, boolean pressed) {
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);

		int x = guiLeft + xIndex * 28;
		int y = guiTop - 28;

		float uMin = 0;
		float uMax = 28 / 256f;
		float vMin = 20 / 256f;
		float vMax = 51 / 256f;
		if (pressed) {
			vMin = 52 / 256f;
			vMax = 84 / 256f;

			if (xIndex != 0) {
				uMin = 28 / 256f;
				uMax = 56 / 256f;
			}

			renderBlurredBackground(width, height, x + 2, y + 2, 28 - 4, 28 - 4);
		} else {
			renderBlurredBackground(width, height, x + 2, y + 4, 28 - 4, 28 - 4);
		}

		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_elements);
		Utils.drawTexturedRect(x, y, 28, pressed ? 32 : 31, uMin, uMax, vMin, vMax, GL11.GL_NEAREST);

		GlStateManager.enableDepth();
		Utils.drawItemStack(stack, x + 6, y + 9);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (unlockTextField.getFocus()) {
			if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_RETURN) {
				CapeManager.INSTANCE.tryUnlockCape(unlockTextField.getText().trim());
				unlockTextField.setText("");
				unlockTextField.setFocus(false);
			} else {
				unlockTextField.keyTyped(typedChar, keyCode);
			}
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		//guiLeft+sizeX-140, guiTop+sizeY+2

		if (mouseX > guiLeft + sizeX - 140 & mouseX < guiLeft + sizeX &&
			mouseY > guiTop + sizeY && mouseY < guiTop + sizeY + 22) {
			unlockTextField.mouseClicked(mouseX, mouseY, mouseButton);
		}

		for (int i = 0; i < CosmeticsPage.values().length; i++) {
			CosmeticsPage page = CosmeticsPage.values()[i];
			int x = guiLeft + i * 28;
			int y = guiTop - 28;

			if (mouseX > x && mouseX < x + 28) {
				if (mouseY > y && mouseY < y + 32) {
					if (currentPage != page) Utils.playPressSound();
					currentPage = page;
					return;
				}
			}
		}
		if (mouseY > guiTop + 177 && mouseY < guiTop + 177 + 12) {
			if (mouseX > guiLeft + 15 + 371 * scroll && mouseX < guiLeft + 15 + 371 * scroll + 32) {
				scrollClickedX = mouseX - (int) (guiLeft + 15 + 371 * scroll);
				return;
			}
		}

		int displayingCapes = 0;
		for (CapeManager.CapeData cape : CapeManager.INSTANCE.getCapes()) {
			boolean equipable = CapeManager.INSTANCE.getAvailableCapes() == null ||
				CapeManager.INSTANCE.getAvailableCapes().contains(cape.capeName);
			if (cape.canShow() || equipable) {
				displayingCapes++;
			}
		}

		float totalNeeded = 91 * displayingCapes;
		float totalAvail = sizeX - 20;
		float xOffset = scroll * (totalNeeded - totalAvail);

		int displayIndex = 0;
		for (CapeManager.CapeData cape : CapeManager.INSTANCE.getCapes()) {
			boolean equipable = CapeManager.INSTANCE.getAvailableCapes() == null ||
				CapeManager.INSTANCE.getAvailableCapes().contains(cape.capeName);
			if (!cape.canShow() && !equipable) continue;

			GlStateManager.color(1, 1, 1, 1);
			Utils.drawTexturedRect(guiLeft + 20 + 91 * displayIndex - xOffset, guiTop + 123, 81, 20,
				0, 81 / 256f, 216 / 256f, 236 / 256f, GL11.GL_NEAREST
			);

			if (mouseX > guiLeft + 20 + 91 * displayIndex - xOffset &&
				mouseX < guiLeft + 20 + 91 * displayIndex - xOffset + 81) {
				if (mouseY > guiTop + 123 && mouseY < guiTop + 123 + 20) {
					if (CapeManager.INSTANCE.localCape != null &&
						CapeManager.INSTANCE.localCape.getRight().equals(cape.capeName)) {
						CapeManager.INSTANCE.setCape(Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", ""),
							"null", true
						);
					} else {
						CapeManager.INSTANCE.setCape(Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", ""),
							cape.capeName, true
						);
					}

					return;
				} else if (equipable && mouseY > guiTop + 149 && mouseY < guiTop + 149 + 20) {
					if (cape.capeName.equals(wantToEquipCape)) {
						wantToEquipCape = null;
					} else {
						wantToEquipCape = cape.capeName;
					}
					return;
				}
			}

			displayIndex++;
		}

		if (currentPage == CosmeticsPage.CAPES) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(pv_dropdown);
			Utils.drawTexturedRect(
				guiLeft + sizeX / 2f - 50,
				guiTop + sizeY + 5,
				100,
				20,
				0,
				100 / 200f,
				0,
				20 / 185f,
				GL11.GL_NEAREST
			);

			if (mouseX > guiLeft + sizeX / 2f - 50 && mouseX < guiLeft + sizeX / 2f + 50) {
				if (mouseY > guiTop + sizeY + 5 && mouseY < guiTop + sizeY + 25) {
					if (System.currentTimeMillis() - lastCapeEquip > 20 * 1000) {
						CapeManager.INSTANCE.setCape(Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", ""),
							wantToEquipCape, true
						);

						lastCapeEquip = System.currentTimeMillis();

						try {
							String userName = Minecraft.getMinecraft().thePlayer.getName();
							String accessToken = Minecraft.getMinecraft().getSession().getToken();
							Random r1 = new Random();
							Random r2 = new Random(System.identityHashCode(new Object()));
							BigInteger random1Bi = new BigInteger(128, r1);
							BigInteger random2Bi = new BigInteger(128, r2);
							BigInteger serverBi = random1Bi.xor(random2Bi);
							String serverId = serverBi.toString(16);
							Minecraft.getMinecraft().getSessionService().joinServer(Minecraft
								.getMinecraft()
								.getSession()
								.getProfile(), accessToken, serverId);

							String toEquipName = wantToEquipCape == null ? "null" : wantToEquipCape;
							NotEnoughUpdates.INSTANCE.manager.apiUtils
								.newMoulberryRequest("cgi-bin/changecape.py")
								.queryArgument("capeType", toEquipName)
								.queryArgument("serverId", serverId)
								.queryArgument("username", userName)
								.requestString()
								.thenAccept(System.out::println);
						} catch (Exception e) {
							System.out.println("Exception while generating mojang shared secret");
							e.printStackTrace();
						}

					}
				}
			}
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);

		scrollClickedX = -1;
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

		if (scrollClickedX >= 0) {
			float scrollStartX = mouseX - scrollClickedX;
			scroll = (scrollStartX - (guiLeft + 15)) / 371f;
			scroll = Math.max(0, Math.min(1, scroll));
		}
	}

	private final HashMap<String, ResourceLocation> capesLocation = new HashMap<>();
	private float scroll = 0f;
	private int scrollClickedX = -1;

	private void drawCapesPage(int mouseX, int mouseY, float partialTicks) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(cosmetics_fg);
		Utils.drawTexturedRect(guiLeft, guiTop, sizeX, sizeY, GL11.GL_NEAREST);

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_elements);
		Utils.drawTexturedRect(guiLeft + 15 + 371 * scroll, guiTop + 177, 32, 12,
			0, 32 / 256f, 192 / 256f, 204 / 256f, GL11.GL_NEAREST
		);

		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(Minecraft.getMinecraft().displayWidth * (guiLeft + 3) / width, 0,
			Minecraft.getMinecraft().displayWidth * (sizeX - 6) / width, Minecraft.getMinecraft().displayHeight
		);

		int displayingCapes = 0;
		for (CapeManager.CapeData capeData : CapeManager.INSTANCE.getCapes()) {
			boolean equipable = CapeManager.INSTANCE.getAvailableCapes() == null ||
				CapeManager.INSTANCE.getAvailableCapes().contains(capeData.capeName);
			if (capeData.canShow() || equipable) {
				displayingCapes++;
			}
		}

		float totalNeeded = 91 * displayingCapes;
		float totalAvail = sizeX - 20;
		float xOffset = scroll * (totalNeeded - totalAvail);

		int displayIndex = 0;
		for (CapeManager.CapeData capeData : CapeManager.INSTANCE.getCapes()) {
			boolean equipable = CapeManager.INSTANCE.getAvailableCapes() == null ||
				CapeManager.INSTANCE.getAvailableCapes().contains(capeData.capeName);
			if (!capeData.canShow() && !equipable) continue;

			if (capeData.capeName.equals(CapeManager.INSTANCE.getCape(Minecraft.getMinecraft().thePlayer
				.getUniqueID()
				.toString()
				.replace("-", "")))) {
				GlStateManager.color(250 / 255f, 200 / 255f, 0 / 255f, 1);
				Utils.drawGradientRect(guiLeft + 20 + 91 * displayIndex - (int) xOffset, guiTop + 10,
					guiLeft + 20 + 91 * displayIndex - (int) xOffset + 81, guiTop + 10 + 108,
					new Color(150, 100, 0, 40).getRGB(), new Color(250, 200, 0, 40).getRGB()
				);
			} else if (capeData.capeName.equals(wantToEquipCape)) {
				GlStateManager.color(0, 200 / 255f, 250 / 255f, 1);
				Utils.drawGradientRect(guiLeft + 20 + 91 * displayIndex - (int) xOffset, guiTop + 10,
					guiLeft + 20 + 91 * displayIndex - (int) xOffset + 81, guiTop + 10 + 108,
					new Color(0, 100, 150, 40).getRGB(), new Color(0, 200, 250, 40).getRGB()
				);
			} else if (CapeManager.INSTANCE.localCape != null &&
				CapeManager.INSTANCE.localCape.getRight().equals(capeData.capeName)) {
				GlStateManager.color(100 / 255f, 250 / 255f, 150 / 255f, 1);
				Utils.drawGradientRect(guiLeft + 20 + 91 * displayIndex - (int) xOffset, guiTop + 10,
					guiLeft + 20 + 91 * displayIndex - (int) xOffset + 81, guiTop + 10 + 108,
					new Color(50, 100, 75, 40).getRGB(), new Color(100, 250, 150, 40).getRGB()
				);
			}
			Minecraft.getMinecraft().getTextureManager().bindTexture(pv_elements);
			Utils.drawTexturedRect(guiLeft + 20 + 91 * displayIndex - xOffset, guiTop + 10, 81, 108,
				0, 81 / 256f, 84 / 256f, 192 / 256f, GL11.GL_NEAREST
			);
			GlStateManager.color(1, 1, 1, 1);

			Utils.drawTexturedRect(guiLeft + 20 + 91 * displayIndex - xOffset, guiTop + 123, 81, 20,
				0, 81 / 256f, 216 / 256f, 236 / 256f, GL11.GL_NEAREST
			);

			boolean equipPressed = capeData.capeName.equals(wantToEquipCape);
			if (!equipable) GlStateManager.color(1, 1, 1, 0.5f);
			Utils.drawTexturedRect(
				guiLeft + 20 + 91 * displayIndex - xOffset,
				guiTop + 149,
				81,
				20,
				equipPressed ? 81 / 256f : 0,
				equipPressed ? 0 : 81 / 256f,
				equipPressed ? 236 / 256f : 216 / 256f,
				equipPressed ? 216 / 256f : 236 / 256f,
				GL11.GL_NEAREST
			);

			Utils.drawStringCenteredScaledMaxWidth(
				"Try it out",
				guiLeft + 20 + 91 * displayIndex + 81 / 2f - xOffset,
				guiTop + 123 + 10,
				false,
				75,
				new Color(100, 250, 150).getRGB()
			);
			if (equipable) {
				Utils.drawStringCenteredScaledMaxWidth(
					"Equip",
					guiLeft + 20 + 91 * displayIndex + 81 / 2f - xOffset,
					guiTop + 149 + 10,
					false,
					75,
					new Color(100, 250, 150).getRGB()
				);
			} else {
				Utils.drawStringCenteredScaledMaxWidth(
					"Not Unlocked",
					guiLeft + 20 + 91 * displayIndex + 81 / 2f - xOffset,
					guiTop + 149 + 10,
					false,
					75,
					new Color(200, 50, 50, 100).getRGB()
				);
			}
			GlStateManager.color(1, 1, 1, 1);

			ResourceLocation capeTexture = capesLocation.computeIfAbsent(
				capeData.capeName,
				k -> new ResourceLocation("notenoughupdates", "capes/" + capeData.capeName + "_preview.png")
			);
			Minecraft.getMinecraft().getTextureManager().bindTexture(capeTexture);
			Utils.drawTexturedRect(guiLeft + 31 + 91 * displayIndex - xOffset, guiTop + 24, 59, 84, GL11.GL_NEAREST);

			displayIndex++;
		}

		GL11.glScissor(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
		GL11.glDisable(GL11.GL_SCISSOR_TEST);

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_elements);
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

	/**
	 * Renders whatever is currently in the Minecraft framebuffer to our two framebuffers, applying a horizontal
	 * and vertical blur separately in order to significantly save computation time.
	 * This is only possible if framebuffers are supported by the system, so this method will exit prematurely
	 * if framebuffers are not available. (Apple machines, for example, have poor framebuffer support).
	 */
	private double lastBgBlurFactor = -1;

	private void blurBackground() {
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
		float uMin = x / (float) width;
		float uMax = (x + blurWidth) / (float) width;
		float vMin = (height - y) / (float) height;
		float vMax = (height - y - blurHeight) / (float) height;

		blurOutputVert.bindFramebufferTexture();
		GlStateManager.color(1f, 1f, 1f, 1f);
		//Utils.setScreen(width*f, height*f, f);
		Utils.drawTexturedRect(x, y, blurWidth, blurHeight, uMin, uMax, vMin, vMax);
		//Utils.setScreen(width, height, f);
		blurOutputVert.unbindFramebufferTexture();
	}
}
