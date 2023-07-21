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

package io.github.moulberry.notenoughupdates.listener;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUApi;
import io.github.moulberry.notenoughupdates.NEUOverlay;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.GuiScreenElementWrapper;
import io.github.moulberry.notenoughupdates.dungeons.DungeonWin;
import io.github.moulberry.notenoughupdates.events.ButtonExclusionZoneEvent;
import io.github.moulberry.notenoughupdates.miscfeatures.AbiphoneWarning;
import io.github.moulberry.notenoughupdates.miscfeatures.AuctionBINWarning;
import io.github.moulberry.notenoughupdates.miscfeatures.BetterContainers;
import io.github.moulberry.notenoughupdates.miscfeatures.CrystalMetalDetectorSolver;
import io.github.moulberry.notenoughupdates.miscfeatures.EnchantingSolvers;
import io.github.moulberry.notenoughupdates.miscfeatures.PresetWarning;
import io.github.moulberry.notenoughupdates.miscfeatures.StorageManager;
import io.github.moulberry.notenoughupdates.miscfeatures.dev.RepoExporters;
import io.github.moulberry.notenoughupdates.miscgui.AccessoryBagOverlay;
import io.github.moulberry.notenoughupdates.miscgui.CalendarOverlay;
import io.github.moulberry.notenoughupdates.miscgui.GuiCustomEnchant;
import io.github.moulberry.notenoughupdates.miscgui.GuiInvButtonEditor;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import io.github.moulberry.notenoughupdates.miscgui.StorageOverlay;
import io.github.moulberry.notenoughupdates.miscgui.TradeWindow;
import io.github.moulberry.notenoughupdates.miscgui.hex.GuiCustomHex;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.overlays.AuctionSearchOverlay;
import io.github.moulberry.notenoughupdates.overlays.BazaarSearchOverlay;
import io.github.moulberry.notenoughupdates.overlays.OverlayManager;
import io.github.moulberry.notenoughupdates.overlays.RancherBootOverlay;
import io.github.moulberry.notenoughupdates.overlays.TextOverlay;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewerUtils;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.NotificationHandler;
import io.github.moulberry.notenoughupdates.util.Rectangle;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.moulberry.notenoughupdates.util.GuiTextures.dungeon_chest_worth;

public class RenderListener {
	private static final ResourceLocation EDITOR = new ResourceLocation("notenoughupdates:invbuttons/editor.png");
	public static boolean disableCraftingText = false;
	public static boolean drawingGuiScreen = false;
	public static long lastGuiClosed = 0;
	public static boolean inventoryLoaded = false;
	private final NotEnoughUpdates neu;
	private final NumberFormat format = new DecimalFormat("#,##0.#", new DecimalFormatSymbols(Locale.US));
	private final Pattern ESSENCE_PATTERN = Pattern.compile("§d(.+) Essence §8x([\\d,]+)");
	ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
	JsonObject essenceJson = new JsonObject();
	private boolean hoverInv = false;
	private boolean focusInv = false;
	private boolean doInventoryButtons = false;
	private NEUConfig.InventoryButton buttonHovered = null;
	private long buttonHoveredMillis = 0;
	private int inventoryLoadedTicks = 0;
	private String loadedInvName = "";
	//NPC parsing

	private boolean inDungeonPage = false;

	public RenderListener(NotEnoughUpdates neu) {
		this.neu = neu;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onRenderEntitySpecials(RenderLivingEvent.Specials.Pre<EntityPlayer> event) {
		if (Minecraft.getMinecraft().currentScreen instanceof GuiProfileViewer) {
			if (((GuiProfileViewer) Minecraft.getMinecraft().currentScreen).getEntityPlayer() == event.entity) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
		if (event.type != null && event.type.equals(RenderGameOverlayEvent.ElementType.BOSSHEALTH) &&
			Minecraft.getMinecraft().currentScreen instanceof GuiContainer && neu.overlay.isUsingMobsFilter()) {
			event.setCanceled(true);
		}
		if (event.type != null && event.type.equals(RenderGameOverlayEvent.ElementType.PLAYER_LIST)) {
			GlStateManager.enableDepth();
		}
	}

	@SubscribeEvent
	public void onRenderGameOverlayPost(RenderGameOverlayEvent.Post event) {
		if (neu.hasSkyblockScoreboard() && event.type.equals(RenderGameOverlayEvent.ElementType.ALL)) {
			DungeonWin.render(event.partialTicks);
			GlStateManager.pushMatrix();
			Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.locationedit.guiScale);
			GlStateManager.translate(0, 0, -200);
			label:
			for (TextOverlay overlay : OverlayManager.textOverlays) {
				for (Class<? extends TextOverlay> dontRender : OverlayManager.dontRenderOverlay) {
					if (dontRender != null &&
						dontRender.isAssignableFrom(overlay.getClass())) {
						continue label;
					}
				}

				GlStateManager.translate(0, 0, -1);
				GlStateManager.enableDepth();
				overlay.render();
			}
			Utils.pushGuiScale(0);
			GlStateManager.popMatrix();
			OverlayManager.dontRenderOverlay = new ArrayList<>();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_X)) {
			NotificationHandler.notificationDisplayMillis = 0;
		}

		if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
			NotificationHandler.renderNotification();
		}

	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.START) return;
		if (Minecraft.getMinecraft().theWorld == null) return;
		if (Minecraft.getMinecraft().thePlayer == null) return;

		if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
			GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest cc = (ContainerChest) chest.inventorySlots;

			if (!loadedInvName.equals(cc.getLowerChestInventory().getDisplayName().getUnformattedText())) {
				loadedInvName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();
				inventoryLoaded = false;
				inventoryLoadedTicks = 3;
			}

			if (!inventoryLoaded) {
				if (cc.getLowerChestInventory().getStackInSlot(cc.getLowerChestInventory().getSizeInventory() - 1) != null) {
					inventoryLoaded = true;
				} else {
					for (ItemStack stack : chest.inventorySlots.getInventory()) {
						if (stack != null) {
							if (--inventoryLoadedTicks <= 0) {
								inventoryLoaded = true;
							}
							break;
						}
					}
				}
			}
		} else {
			inventoryLoaded = false;
			inventoryLoadedTicks = 3;
		}

	}

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		NEUApi.disableInventoryButtons = false;

		if ((Minecraft.getMinecraft().currentScreen instanceof GuiScreenElementWrapper ||
			Minecraft.getMinecraft().currentScreen instanceof GuiItemRecipe) && event.gui == null &&
			!(Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) &&
			System.currentTimeMillis() - NotEnoughUpdates.INSTANCE.lastOpenedGui < 500) {
			NotEnoughUpdates.INSTANCE.lastOpenedGui = 0;
			event.setCanceled(true);
			return;
		}

		if (!(event.gui instanceof GuiContainer) && Minecraft.getMinecraft().currentScreen != null) {
			CalendarOverlay.setEnabled(false);
		}

		if (Minecraft.getMinecraft().currentScreen != null) {
			lastGuiClosed = System.currentTimeMillis();
		}

		BetterContainers.reset();
		inventoryLoaded = false;
		inventoryLoadedTicks = 3;

		//OPEN
		if (Minecraft.getMinecraft().currentScreen == null && event.gui instanceof GuiContainer) {
			neu.overlay.reset();
		}
		if (event.gui != null && NotEnoughUpdates.INSTANCE.config.hidden.dev) {
			if (event.gui instanceof GuiChest) {
				GuiChest eventGui = (GuiChest) event.gui;
				ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
				IInventory lower = cc.getLowerChestInventory();
				ses.schedule(() -> {
					if (Minecraft.getMinecraft().currentScreen != event.gui) {
						return;
					}
					if (lower.getStackInSlot(23).getDisplayName().endsWith("Crafting Table")) {
						try {
							ItemStack res = lower.getStackInSlot(25);
							String resInternalname = neu.manager.getInternalNameForItem(res);

							if (lower.getStackInSlot(48) != null) {
								String backName = null;
								NBTTagCompound tag = lower.getStackInSlot(48).getTagCompound();
								if (tag.hasKey("display", 10)) {
									NBTTagCompound nbttagcompound = tag.getCompoundTag("display");
									if (nbttagcompound.getTagId("Lore") == 9) {
										NBTTagList nbttaglist1 = nbttagcompound.getTagList("Lore", 8);
										backName = nbttaglist1.getStringTagAt(0);
									}
								}

								if (backName != null) {
									String[] split = backName.split(" ");
									if (split[split.length - 1].contains("Rewards")) {
										String col = backName.substring(
											split[0].length() + 1,
											backName.length() - split[split.length - 1].length() - 1
										);

										JsonObject json = neu.manager.getItemInformation().get(resInternalname);
										json.addProperty("crafttext", "Requires: " + col);

										Utils.addChatMessage("Added: " + resInternalname);
										neu.manager.writeJsonDefaultDir(json, resInternalname + ".json");
										neu.manager.loadItem(resInternalname);
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, 200, TimeUnit.MILLISECONDS);
			}
		}
	}

	/**
	 * Sets hoverInv and focusInv variables, representing whether the NEUOverlay should render behind the inventory when
	 * (hoverInv == true) and whether mouse/kbd inputs shouldn't be sent to NEUOverlay (focusInv == true).
	 * <p>
	 * If hoverInv is true, will render the overlay immediately (resulting in the inventory being drawn over the GUI)
	 * If hoverInv is false, the overlay will render in #onGuiScreenDraw (resulting in the GUI being drawn over the inv)
	 * <p>
	 * All of this only matters if players are using gui scale auto which may result in the inventory being drawn
	 * over the various panes.
	 */
	@SubscribeEvent
	public void onGuiBackgroundDraw(GuiScreenEvent.BackgroundDrawnEvent event) {
		if (NotificationHandler.showNotificationOverInv) {

			NotificationHandler.renderNotification();

		}
		inDungeonPage = false;
		if (NotificationHandler.shouldRenderOverlay(event.gui) && neu.isOnSkyblock()) {
			ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
			int width = scaledresolution.getScaledWidth();

			boolean hoverPane = event.getMouseX() < width * neu.overlay.getInfoPaneOffsetFactor() ||
				event.getMouseX() > width * neu.overlay.getItemPaneOffsetFactor();

			if (event.gui instanceof GuiContainer) {
				try {
					int xSize = ((AccessorGuiContainer) event.gui).getXSize();
					int ySize = ((AccessorGuiContainer) event.gui).getYSize();
					int guiLeft = ((AccessorGuiContainer) event.gui).getGuiLeft();
					int guiTop = ((AccessorGuiContainer) event.gui).getGuiTop();

					hoverInv = event.getMouseX() > guiLeft && event.getMouseX() < guiLeft + xSize && event.getMouseY() > guiTop &&
						event.getMouseY() < guiTop + ySize;

					if (hoverPane) {
						if (!hoverInv) focusInv = false;
					} else {
						focusInv = true;
					}
				} catch (NullPointerException npe) {
					focusInv = !hoverPane;
				}
			}
			if (event.gui instanceof GuiItemRecipe) {
				GuiItemRecipe guiItemRecipe = ((GuiItemRecipe) event.gui);
				hoverInv = event.getMouseX() > guiItemRecipe.guiLeft &&
					event.getMouseX() < guiItemRecipe.guiLeft + guiItemRecipe.xSize && event.getMouseY() > guiItemRecipe.guiTop &&
					event.getMouseY() < guiItemRecipe.guiTop + guiItemRecipe.ySize;

				if (hoverPane) {
					if (!hoverInv) focusInv = false;
				} else {
					focusInv = true;
				}
			}
			if (focusInv) {
				try {
					neu.overlay.render(hoverInv);
				} catch (ConcurrentModificationException e) {
					e.printStackTrace();
				}
				GL11.glTranslatef(0, 0, 10);
			}
			if (hoverInv) {
				renderDungKuudraChestOverlay(event.gui);
				if (NotEnoughUpdates.INSTANCE.config.accessoryBag.enableOverlay) {
					AccessoryBagOverlay.renderOverlay();
				}
			}
		}

		drawingGuiScreen = true;
	}

	@SubscribeEvent
	public void onGuiScreenDrawPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
		doInventoryButtons = false;

		if (AuctionSearchOverlay.shouldReplace()) {
			AuctionSearchOverlay.render();
			event.setCanceled(true);
			return;
		}
		if (BazaarSearchOverlay.shouldReplace()) {
			BazaarSearchOverlay.render();
			event.setCanceled(true);
			return;
		}
		if (RancherBootOverlay.shouldReplace()) {
			RancherBootOverlay.render();
			event.setCanceled(true);
			return;
		}

		String containerName = null;
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
		if (guiScreen instanceof GuiChest) {
			GuiChest eventGui = (GuiChest) guiScreen;
			ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
			containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();
		}

		if (GuiCustomHex.getInstance().shouldOverride(containerName)) {
			GuiCustomHex.getInstance().render(event.renderPartialTicks, containerName);
			event.setCanceled(true);
			return;
		}

		if (GuiCustomEnchant.getInstance().shouldOverride(containerName)) {
			GuiCustomEnchant.getInstance().render(event.renderPartialTicks);
			event.setCanceled(true);
			return;
		}

		boolean tradeWindowActive = TradeWindow.tradeWindowActive(containerName);
		boolean storageOverlayActive = StorageManager.getInstance().shouldRenderStorageOverlay(containerName);

		if (storageOverlayActive) {
			StorageOverlay.getInstance().render();
			event.setCanceled(true);
			return;
		}

		if (tradeWindowActive) {
			event.setCanceled(true);

			ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
			int width = scaledResolution.getScaledWidth();
			int height = scaledResolution.getScaledHeight();

			//Dark background
			Utils.drawGradientRect(0, 0, width, height, -1072689136, -804253680);

			if (event.mouseX < width * neu.overlay.getWidthMult() / 3 ||
				event.mouseX > width - width * neu.overlay.getWidthMult() / 3) {
				TradeWindow.render(event.mouseX, event.mouseY);
				neu.overlay.render(false);
			} else {
				neu.overlay.render(false);
				TradeWindow.render(event.mouseX, event.mouseY);
			}
		}

		if (CalendarOverlay.isEnabled() || event.isCanceled()) return;
		if (NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() && NotificationHandler.shouldRenderOverlay(event.gui) &&
			event.gui instanceof GuiContainer) {
			renderButtons((GuiContainer) event.gui);
		}
	}

	public void iterateButtons(GuiContainer gui, BiConsumer<NEUConfig.InventoryButton, Rectangle> acceptButton) {
		if (NEUApi.disableInventoryButtons || EnchantingSolvers.disableButtons() || gui == null) {
			return;
		}

		AccessorGuiContainer accessor = (AccessorGuiContainer) gui;
		Rectangle guiRectangle = new Rectangle(
			accessor.getGuiLeft(),
			accessor.getGuiTop(),
			accessor.getXSize(),
			accessor.getYSize()
		);

		ButtonExclusionZoneEvent buttonExclusionZoneEvent = new ButtonExclusionZoneEvent(gui, guiRectangle);
		buttonExclusionZoneEvent.post();
		for (NEUConfig.InventoryButton button : NotEnoughUpdates.INSTANCE.config.hidden.inventoryButtons) {
			if (!button.isActive()) continue;
			if (button.playerInvOnly && !(gui instanceof GuiInventory)) continue;

			Rectangle buttonPosition = buttonExclusionZoneEvent.findButtonPosition(new Rectangle(
					accessor.getGuiLeft() + button.x + (button.anchorRight ? accessor.getXSize() : 0),
					accessor.getGuiTop() + button.y + (button.anchorBottom ? accessor.getYSize() : 0),
					18, 18
				)
			);
			acceptButton.accept(button, buttonPosition);
		}
	}

	public void renderButtons(GuiContainer gui) {
		doInventoryButtons = true;

		int zOffset = 50;
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, zOffset);
		iterateButtons(gui, (button, buttonPosition) -> {
			GlStateManager.color(1, 1, 1, 1f);
			GlStateManager.enableDepth();
			GlStateManager.enableAlpha();

			Minecraft.getMinecraft().getTextureManager().bindTexture(EDITOR);
			Utils.drawTexturedRect(
				buttonPosition.getX(),
				buttonPosition.getY(),
				18,
				18,
				button.backgroundIndex * 18 / 256f,
				(button.backgroundIndex * 18 + 18) / 256f,
				18 / 256f,
				36 / 256f,
				GL11.GL_NEAREST
			);

			if (button.icon != null && !button.icon.trim().isEmpty()) {
				GuiInvButtonEditor.renderIcon(button.icon, buttonPosition.getX() + 1, buttonPosition.getY() + 1);
			}
		});
		GlStateManager.popMatrix();
	}

	/**
	 * Will draw the NEUOverlay over the inventory if focusInv == false. (z-translation of 300 is so that NEUOverlay
	 * will draw over Items in the inventory (which render at a z value of about 250))
	 */
	@SubscribeEvent
	public void onGuiScreenDrawPost(GuiScreenEvent.DrawScreenEvent.Post event) {
		drawingGuiScreen = false;
		disableCraftingText = false;

		String containerName = null;
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
		if (guiScreen instanceof GuiChest) {
			GuiChest eventGui = (GuiChest) guiScreen;
			ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
			containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();

			if (GuiCustomHex.getInstance().shouldOverride(containerName)) return;
			if (GuiCustomEnchant.getInstance().shouldOverride(containerName)) return;
		}

		boolean tradeWindowActive = TradeWindow.tradeWindowActive(containerName);
		boolean storageOverlayActive = StorageManager.getInstance().shouldRenderStorageOverlay(containerName);
		if (!(tradeWindowActive || storageOverlayActive)) {
			if (NotificationHandler.shouldRenderOverlay(event.gui) && neu.isOnSkyblock()) {
				GlStateManager.pushMatrix();
				if (!focusInv) {
					GL11.glTranslatef(0, 0, 300);
					neu.overlay.render(hoverInv && focusInv);
					GL11.glTranslatef(0, 0, -300);
				}
				GlStateManager.popMatrix();
			}
		}

		if (NotificationHandler.shouldRenderOverlay(event.gui) && neu.isOnSkyblock() && !hoverInv) {
			renderDungKuudraChestOverlay(event.gui);
			if (NotEnoughUpdates.INSTANCE.config.accessoryBag.enableOverlay) {
				AccessoryBagOverlay.renderOverlay();
			}
		}

		final boolean[] hoveringButton = {false};
		if (!doInventoryButtons) return;
		if (NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() && NotificationHandler.shouldRenderOverlay(event.gui) &&
			event.gui instanceof GuiContainer) {
			AccessorGuiContainer acc = (AccessorGuiContainer) event.gui;
			Rectangle mousePosition = new Rectangle(event.mouseX, event.mouseY, 0, 0);
			Rectangle craftingTextRectangle = new Rectangle(acc.getGuiLeft() + 85, acc.getGuiTop() + 4, 30, 21);
			iterateButtons((GuiContainer) guiScreen, (button, buttonPosition) -> {

				if (buttonPosition.intersects(craftingTextRectangle)) {
					disableCraftingText = true;
				}

				if (!buttonPosition.intersects(mousePosition)) {
					return;
				}
				hoveringButton[0] = true;
				long currentTime = System.currentTimeMillis();

				if (buttonHovered != button) {
					buttonHoveredMillis = currentTime;
					buttonHovered = button;
				}

				if (currentTime - buttonHoveredMillis <= NotEnoughUpdates.INSTANCE.config.inventoryButtons.tooltipDelay) {
					return;
				}
				String command = button.command.trim();
				if (!command.startsWith("/")) {
					command = "/" + command;
				}

				Utils.drawHoveringText(
					Lists.newArrayList("\u00a77" + command),
					event.mouseX,
					event.mouseY,
					event.gui.width,
					event.gui.height,
					-1
				);

			});
		}
		if (!hoveringButton[0]) buttonHovered = null;

		if (AuctionBINWarning.getInstance().shouldShow()) {
			AuctionBINWarning.getInstance().render();
		}

		if (AbiphoneWarning.getInstance().shouldShow()) {
			AbiphoneWarning.getInstance().render();
		}

		if (PresetWarning.getInstance().shouldShow()) {
			PresetWarning.getInstance().render();
		}
	}

	private void renderDungKuudraChestOverlay(GuiScreen gui) {
		if (NotEnoughUpdates.INSTANCE.config.dungeons.profitDisplayLoc == 3) return;
		if (gui instanceof GuiChest && NotEnoughUpdates.INSTANCE.config.dungeons.profitDisplayLoc != 2) {
			try {
				int xSize = ((AccessorGuiContainer) gui).getXSize();
				int guiLeft = ((AccessorGuiContainer) gui).getGuiLeft();
				int guiTop = ((AccessorGuiContainer) gui).getGuiTop();

				GuiChest eventGui = (GuiChest) gui;
				ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
				IInventory lower = cc.getLowerChestInventory();

				ItemStack rewardChest = lower.getStackInSlot(31);
				this.inDungeonPage = rewardChest != null && rewardChest.getDisplayName().endsWith(
					EnumChatFormatting.GREEN + "Open Reward Chest");
				if (inDungeonPage) {
					int chestCost = 0;
					try {
						String line6 = Utils.cleanColour(neu.manager.getLoreFromNBT(rewardChest.getTagCompound())[6]);
						StringBuilder cost = new StringBuilder();
						for (int i = 0; i < line6.length(); i++) {
							char c = line6.charAt(i);
							if (Character.isDigit(c)) {
								cost.append(c);
							}
						}
						if (cost.length() > 0) {
							chestCost = Integer.parseInt(cost.toString());
						}
					} catch (Exception ignored) {
					}

					String missingItem = null;
					double totalValue = 0;
					HashMap<String, Double> itemValues = new HashMap<>();
					for (int i = 0; i < 5; i++) {
						ItemStack item = lower.getStackInSlot(11 + i);
						if (ItemUtils.isSoulbound(item)) continue;

						String internal = neu.manager.createItemResolutionQuery().withItemStack(item).resolveInternalName();
						String displayName = item.getDisplayName();
						Matcher matcher = ESSENCE_PATTERN.matcher(displayName);
						if (neu.config.dungeons.useEssenceCostFromBazaar && matcher.matches()) {
							String type = matcher.group(1).toUpperCase();
							JsonObject bazaarInfo = neu.manager.auctionManager.getBazaarInfo("ESSENCE_" + type);
							if (bazaarInfo != null && bazaarInfo.has("curr_sell")) {
								float bazaarPrice = bazaarInfo.get("curr_sell").getAsFloat();
								int amount = Integer.parseInt(matcher.group(2));
								double price = bazaarPrice * amount;
								itemValues.put(displayName, price);
								totalValue += price;
							}
							continue;
						}
						if (internal != null) {
							internal = internal.replace("\u00CD", "I").replace("\u0130", "I");
							float bazaarPrice = -1;
							JsonObject bazaarInfo = neu.manager.auctionManager.getBazaarInfo(internal);
							if (bazaarInfo != null && bazaarInfo.has("curr_sell")) {
								bazaarPrice = bazaarInfo.get("curr_sell").getAsFloat();
							} else if (bazaarInfo != null) {
								bazaarPrice = 0;
							}
							if (bazaarPrice < 5000000 && internal.equals("RECOMBOBULATOR_3000")) bazaarPrice = 5000000;

							double worth = -1;
							boolean isOnBz = false;
							if (bazaarPrice >= 0) {
								worth = bazaarPrice;
								isOnBz = true;
							} else {
								switch (NotEnoughUpdates.INSTANCE.config.dungeons.profitType) {
									case 1:
										worth = neu.manager.auctionManager.getItemAvgBin(internal);
										break;
									case 2:
										JsonObject auctionInfo = neu.manager.auctionManager.getItemAuctionInfo(internal);
										if (auctionInfo != null) {
											if (auctionInfo.has("clean_price")) {
												worth = (long) auctionInfo.get("clean_price").getAsDouble();
											} else {
												worth =
													(long) (auctionInfo.get("price").getAsDouble() / auctionInfo.get("count").getAsDouble());
											}
										}
										break;
									default:
										worth = neu.manager.auctionManager.getLowestBin(internal);
								}
								if (worth <= 0) {
									worth = neu.manager.auctionManager.getLowestBin(internal);
									if (worth <= 0) {
										worth = neu.manager.auctionManager.getItemAvgBin(internal);
										if (worth <= 0) {
											JsonObject auctionInfo = neu.manager.auctionManager.getItemAuctionInfo(internal);
											if (auctionInfo != null) {
												if (auctionInfo.has("clean_price")) {
													worth = auctionInfo.get("clean_price").getAsFloat();
												} else {
													worth = (auctionInfo.get("price").getAsFloat() / auctionInfo.get("count").getAsFloat());
												}
											}
										}
									}
								}
							}

							if ((worth >= 0 || isOnBz) && totalValue >= 0) {
								totalValue += worth;
								String display = item.getDisplayName();

								if (display.contains("Enchanted Book")) {
									NBTTagCompound tag = item.getTagCompound();
									if (tag != null && tag.hasKey("ExtraAttributes", 10)) {
										NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
										NBTTagCompound enchants = ea.getCompoundTag("enchantments");

										int highestLevel = -1;
										for (String enchname : enchants.getKeySet()) {
											int level = enchants.getInteger(enchname);
											if (level > highestLevel) {
												display = EnumChatFormatting.BLUE + WordUtils.capitalizeFully(enchname
													.replace("_", " ")
													.replace("Ultimate", "")
													.trim()) + " " + level;
											}
										}
									}
								}

								itemValues.put(display, worth);
							} else {
								if (totalValue != -1) {
									missingItem = internal;
								}
								totalValue = -1;
							}
						}
					}

					String valueStringBIN1;
					String valueStringBIN2;
					if (totalValue >= 0) {
						valueStringBIN1 = EnumChatFormatting.YELLOW + "Value (BIN): ";
						valueStringBIN2 = EnumChatFormatting.GOLD + formatCoins(totalValue) + " coins";
					} else {
						valueStringBIN1 = EnumChatFormatting.YELLOW + "Can't find BIN: ";
						valueStringBIN2 = missingItem;
					}

					double profitLossBIN = totalValue - chestCost;

					boolean kismetUsed = false;
					// checking for kismet
					Slot slot = (eventGui.inventorySlots.getSlot(50));
					if (slot.getHasStack()) {
						String[] lore = NotEnoughUpdates.INSTANCE.manager.getLoreFromNBT(slot.getStack().getTagCompound());
						for (String line : lore) {
							if (line.contains("You already rerolled a chest!")) {
								kismetUsed = true;
								break;
							}
						}
					}
					JsonObject kismetBazaar = neu.manager.auctionManager.getBazaarInfo("KISMET_FEATHER");
					double kismetPrice = (kismetBazaar != null && kismetBazaar.has("curr_buy")) ? kismetBazaar.get("curr_buy").getAsFloat() : 0;
					String kismetStr = EnumChatFormatting.RED + formatCoins(kismetPrice) + " coins";
					if (neu.config.dungeons.useKismetOnDungeonProfit)
						profitLossBIN = kismetUsed ? profitLossBIN - kismetPrice : profitLossBIN;

					String profitPrefix = EnumChatFormatting.DARK_GREEN.toString();
					String lossPrefix = EnumChatFormatting.RED.toString();
					String prefix = profitLossBIN >= 0 ? profitPrefix : lossPrefix;

					String plStringBIN;
					if (profitLossBIN >= 0) {
						plStringBIN = prefix + "+" + formatCoins(profitLossBIN) + " coins";
					} else {
						plStringBIN = prefix + "-" + formatCoins(-profitLossBIN) + " coins";
					}

					if (NotEnoughUpdates.INSTANCE.config.dungeons.profitDisplayLoc == 1 && !valueStringBIN2.equals(missingItem)) {
						int w = Minecraft.getMinecraft().fontRendererObj.getStringWidth(plStringBIN);
						GlStateManager.disableLighting();
						GlStateManager.translate(0, 0, 200);
						Minecraft.getMinecraft().fontRendererObj.drawString(
							plStringBIN,
							guiLeft + xSize - 5 - w,
							guiTop + 5,
							0xffffffff,
							true
						);
						GlStateManager.translate(0, 0, -200);
						return;
					}

					Minecraft.getMinecraft().getTextureManager().bindTexture(dungeon_chest_worth);
					GL11.glColor4f(1, 1, 1, 1);
					GlStateManager.disableLighting();
					Utils.drawTexturedRect(guiLeft + xSize + 4, guiTop, 180, 101, 0, 180 / 256f, 0, 101 / 256f, GL11.GL_NEAREST);

					Utils.renderAlignedString(valueStringBIN1, valueStringBIN2, guiLeft + xSize + 4 + 10, guiTop + 14, 160);
					if (neu.config.dungeons.useKismetOnDungeonProfit && kismetUsed) {
						Utils.renderAlignedString(
							EnumChatFormatting.YELLOW + "Kismet Feather: ",
							kismetStr,
							guiLeft + xSize + 4 + 10,
							guiTop + 24,
							160
						);
					}
					if (totalValue >= 0) {
						Utils.renderAlignedString(
							EnumChatFormatting.YELLOW + "Profit/Loss: ",
							plStringBIN,
							guiLeft + xSize + 4 + 10,
							guiTop + (neu.config.dungeons.useKismetOnDungeonProfit ? (kismetUsed ? 34 : 24) : 24),
							160
						);
					}

					int index = 0;
					for (Map.Entry<String, Double> entry : itemValues.entrySet()) {
						Utils.renderAlignedString(
							entry.getKey(),
							prefix + formatCoins(entry.getValue().doubleValue()),
							guiLeft + xSize + 4 + 10,
							guiTop + (neu.config.dungeons.useKismetOnDungeonProfit ? (kismetUsed ? 39 : 29) : 29) + (++index) * 10,
							160
						);
					}
					JsonObject mayorJson = SBInfo.getInstance().getMayorJson();
					JsonElement mayor = mayorJson.get("mayor");
					if (mayorJson.has("mayor") && mayor != null && mayor.getAsJsonObject().has("name") &&
						mayor.getAsJsonObject().get("name").getAsString().equals("Derpy")
						&& NotEnoughUpdates.INSTANCE.config.dungeons.shouldWarningDerpy) {
						Utils.drawStringScaled(
							EnumChatFormatting.RED + EnumChatFormatting.BOLD.toString() + "Mayor Derpy active!",
							guiLeft + xSize + 4 + 10,
							guiTop + 85,
							true,
							0,
							1.3f
						);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String formatCoins(double price) {
		return format.format(price < 5 ? price : (long) price);
	}

	/**
	 * Sends a mouse event to NEUOverlay if the inventory isn't hovered AND focused.
	 * Will also cancel the event if if NEUOverlay#mouseInput returns true.
	 */
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onGuiScreenMouse(GuiScreenEvent.MouseInputEvent.Pre event) {
		final ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		final int scaledWidth = scaledresolution.getScaledWidth();
		final int scaledHeight = scaledresolution.getScaledHeight();
		int mouseX = Mouse.getX() * scaledWidth / Minecraft.getMinecraft().displayWidth;
		int mouseY = scaledHeight - Mouse.getY() * scaledHeight / Minecraft.getMinecraft().displayHeight - 1;

		if (AuctionBINWarning.getInstance().shouldShow()) {
			AuctionBINWarning.getInstance().mouseInput(mouseX, mouseY);
			event.setCanceled(true);
			return;
		}
		if (AbiphoneWarning.getInstance().shouldShow()) {
			AbiphoneWarning.getInstance().mouseInput(mouseX, mouseY);
			event.setCanceled(true);
			return;
		}
		if (PresetWarning.getInstance().shouldShow()) {
			PresetWarning.getInstance().mouseInput(mouseX, mouseY);
			event.setCanceled(true);
			return;
		}

		if (!event.isCanceled()) {
			Utils.scrollTooltip(Mouse.getEventDWheel());
		}
		if (AuctionSearchOverlay.shouldReplace()) {
			AuctionSearchOverlay.mouseEvent();
			event.setCanceled(true);
			return;
		}
		if (BazaarSearchOverlay.shouldReplace()) {
			BazaarSearchOverlay.mouseEvent();
			event.setCanceled(true);
			return;
		}
		if (RancherBootOverlay.shouldReplace()) {
			RancherBootOverlay.mouseEvent();
			event.setCanceled(true);
			return;
		}

		String containerName = null;
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
		if (guiScreen instanceof GuiChest) {
			GuiChest eventGui = (GuiChest) guiScreen;
			ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
			containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();
			if (containerName.contains(" Profile") && BetterContainers.profileViewerStackIndex != -1 &&
				((AccessorGuiContainer) eventGui).doIsMouseOverSlot(
					cc.inventorySlots.get(BetterContainers.profileViewerStackIndex),
					mouseX,
					mouseY
				) &&
				Mouse.getEventButton() >= 0) {
				event.setCanceled(true);
				if (Mouse.getEventButtonState() && eventGui.inventorySlots.inventorySlots.get(22).getStack() != null &&
					eventGui.inventorySlots.inventorySlots.get(22).getStack().getTagCompound() != null) {
					NBTTagCompound tag = eventGui.inventorySlots.inventorySlots.get(22).getStack().getTagCompound();
					if (tag.hasKey("SkullOwner") && tag.getCompoundTag("SkullOwner").hasKey("Name")) {
						String username = tag.getCompoundTag("SkullOwner").getString("Name");
						Utils.playPressSound();
						NotEnoughUpdates.profileViewer.loadPlayerByName(username, profile -> {
							if (profile == null) {
								Utils.addChatMessage("${RED}Invalid player name. Maybe the API is down?");
							} else {
								profile.resetCache();
								ProfileViewerUtils.saveSearch(username);
								NotEnoughUpdates.INSTANCE.openGui = new GuiProfileViewer(profile);
							}
						});
					}
				}
			}
		}

		if (GuiCustomHex.getInstance().shouldOverride(containerName) &&
			GuiCustomHex.getInstance().mouseInput(mouseX, mouseY)) {
			event.setCanceled(true);
			return;
		}
		if (GuiCustomEnchant.getInstance().shouldOverride(containerName) &&
			GuiCustomEnchant.getInstance().mouseInput(mouseX, mouseY)) {
			event.setCanceled(true);
			return;
		}

		boolean tradeWindowActive = TradeWindow.tradeWindowActive(containerName);
		boolean storageOverlayActive = StorageManager.getInstance().shouldRenderStorageOverlay(containerName);

		if (storageOverlayActive) {
			if (StorageOverlay.getInstance().mouseInput(mouseX, mouseY)) {
				event.setCanceled(true);
			}
			return;
		}

		if (tradeWindowActive) {
			event.setCanceled(true);
			TradeWindow.handleMouseInput();
			neu.overlay.mouseInput();
			return;
		}

		if (NotificationHandler.shouldRenderOverlay(event.gui) && neu.isOnSkyblock()) {
			if (!NotEnoughUpdates.INSTANCE.config.accessoryBag.enableOverlay || !AccessoryBagOverlay.mouseClick()) {
				if (!(hoverInv && focusInv)) {
					if (neu.overlay.mouseInput()) {
						event.setCanceled(true);
					}
				} else {
					neu.overlay.mouseInputInv();
				}
			}
		}
		if (event.isCanceled()) return;
		if (!doInventoryButtons) return;
		if (NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() && NotificationHandler.shouldRenderOverlay(event.gui) &&
			Mouse.getEventButton() >= 0 && event.gui instanceof GuiContainer) {
			Rectangle mouseRect = new Rectangle(mouseX, mouseY, 0, 0);
			iterateButtons((GuiContainer) event.gui, (button, buttonPositon) -> {
				if (!buttonPositon.intersects(mouseRect)) {
					return;
				}
				if (Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null) {
					int clickType = NotEnoughUpdates.INSTANCE.config.inventoryButtons.clickType;
					if ((clickType == 0 && Mouse.getEventButtonState()) ||
						(clickType == 1 && !Mouse.getEventButtonState())) {
						String command = button.command.trim();
						if (!command.startsWith("/")) {
							command = "/" + command;
						}
						if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, command) == 0) {
							NotEnoughUpdates.INSTANCE.sendChatMessage(command);
						}
					}
				} else {
					event.setCanceled(true);
				}

			});
		}
	}

	/**
	 * Sends a kbd event to NEUOverlay, cancelling if NEUOverlay#keyboardInput returns true.
	 * Also includes a dev function used for creating custom named json files with recipes.
	 */
	@SubscribeEvent
	public void onGuiScreenKeyboard(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		Keyboard.enableRepeatEvents(true);
		if (Minecraft.getMinecraft().currentScreen instanceof GuiInventory &&
			!NEUOverlay.searchBarHasFocus &&
			Keyboard.isRepeatEvent()) {
			event.setCanceled(true);
			return;
		}
		if (NotEnoughUpdates.INSTANCE.config.hidden.dev && Keyboard.isKeyDown(Keyboard.KEY_B) &&
			Minecraft.getMinecraft().currentScreen instanceof GuiChest
		) {
			GuiChest eventGui = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
			IInventory lower = cc.getLowerChestInventory();

			ItemStack backArrow = lower.getStackInSlot(48);
			List<String> tooltip = backArrow != null ? backArrow.getTooltip(Minecraft.getMinecraft().thePlayer, false) : null;
			if (tooltip != null && tooltip.size() >= 2 && tooltip.get(1).endsWith("Essence")) {
				RepoExporters.getInstance().essenceExporter();
			} else if (lower.getName().contains("Draconic Altar Guide")) {
				RepoExporters.getInstance().draconicAlterExporter();
			}
		} else if (NotEnoughUpdates.INSTANCE.config.hidden.dev && Keyboard.isKeyDown(Keyboard.KEY_B) &&
			Minecraft.getMinecraft().currentScreen instanceof GuiChest &&
			((((ContainerChest) ((GuiChest) Minecraft.getMinecraft().currentScreen).inventorySlots)
				.getLowerChestInventory()
				.getDisplayName()
				.getUnformattedText()
				.endsWith("Essence")))) {
			RepoExporters.getInstance().essenceExporter2();
			event.setCanceled(true);
			return;
		}

		if (AuctionBINWarning.getInstance().shouldShow()) {
			AuctionBINWarning.getInstance().keyboardInput();
			event.setCanceled(true);
			return;
		}
		if (AbiphoneWarning.getInstance().shouldShow()) {
			AbiphoneWarning.getInstance().keyboardInput();
			event.setCanceled(true);
			return;
		}
		if (PresetWarning.getInstance().shouldShow()) {
			PresetWarning.getInstance().keyboardInput();
			event.setCanceled(true);
			return;
		}

		if (AuctionSearchOverlay.shouldReplace()) {
			AuctionSearchOverlay.keyEvent();
			event.setCanceled(true);
			return;
		}
		if (BazaarSearchOverlay.shouldReplace()) {
			BazaarSearchOverlay.keyEvent();
			event.setCanceled(true);
			return;
		}
		if (RancherBootOverlay.shouldReplace()) {
			RancherBootOverlay.keyEvent();
			event.setCanceled(true);
			return;
		}

		String containerName = null;
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;

		if (guiScreen instanceof GuiChest) {
			containerName = ((ContainerChest) ((GuiChest) guiScreen).inventorySlots)
				.getLowerChestInventory()
				.getDisplayName()
				.getUnformattedText();
		}

		if (GuiCustomHex.getInstance().shouldOverride(containerName) &&
			GuiCustomHex.getInstance().keyboardInput()) {
			event.setCanceled(true);
			return;
		}

		if (GuiCustomEnchant.getInstance().shouldOverride(containerName) &&
			GuiCustomEnchant.getInstance().keyboardInput()) {
			event.setCanceled(true);
			return;
		}

		boolean tradeWindowActive = TradeWindow.tradeWindowActive(containerName);
		boolean storageOverlayActive = StorageManager.getInstance().shouldRenderStorageOverlay(containerName);

		if (storageOverlayActive) {
			if (StorageOverlay.getInstance().keyboardInput()) {
				event.setCanceled(true);
				return;
			}
		}

		if (tradeWindowActive) {
				TradeWindow.keyboardInput();
				if (Keyboard.getEventKey() != Keyboard.KEY_ESCAPE) {
					event.setCanceled(true);
					Minecraft.getMinecraft().dispatchKeypresses();
					neu.overlay.keyboardInput(focusInv);
				}
			return;
		}

		if (NotificationHandler.shouldRenderOverlay(event.gui) && neu.isOnSkyblock()) {
			if (neu.overlay.keyboardInput(focusInv)) {
				event.setCanceled(true);
			}
		}
		if (NotEnoughUpdates.INSTANCE.config.apiData.repositoryEditing &&
			Minecraft.getMinecraft().theWorld != null && Keyboard.getEventKey() == Keyboard.KEY_N &&
			Keyboard.getEventKeyState()) {
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof GuiChest) {
				GuiChest eventGui = (GuiChest) event.gui;
				ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
				IInventory lower = cc.getLowerChestInventory();

				if (!lower.getDisplayName().getUnformattedText().endsWith("Essence")) return;

				for (int i = 0; i < lower.getSizeInventory(); i++) {
					ItemStack stack = lower.getStackInSlot(i);

					String internalname = neu.manager.getInternalNameForItem(stack);
					if (internalname != null) {
						String[] lore = neu.manager.getLoreFromNBT(stack.getTagCompound());

						for (String line : lore) {
							if (line.contains(":") && (line.startsWith("\u00A77Upgrade to") || line.startsWith(
								"\u00A77Convert to Dungeon Item"))) {
								String[] split = line.split(":");
								String after = Utils.cleanColour(split[1]);
								StringBuilder costS = new StringBuilder();
								for (char c : after.toCharArray()) {
									if (c >= '0' && c <= '9') {
										costS.append(c);
									}
								}
								int cost = Integer.parseInt(costS.toString());
								String[] afterSplit = after.split(" ");
								String type = afterSplit[afterSplit.length - 2];

								if (!essenceJson.has(internalname)) {
									essenceJson.add(internalname, new JsonObject());
								}
								JsonObject obj = essenceJson.get(internalname).getAsJsonObject();
								obj.addProperty("type", type);

								if (line.startsWith("\u00A77Convert to Dungeon Item")) {
									obj.addProperty("dungeonize", cost);
								} else if (line.startsWith("\u00A77Upgrade to")) {
									int stars = 0;
									for (char c : line.toCharArray()) {
										if (c == '\u272A') stars++;
									}
									if (stars > 0) {
										obj.addProperty(stars + "", cost);
									}
								}
							}
						}
					}
				}
				System.out.println(essenceJson);
			}
		}
		if (NotEnoughUpdates.INSTANCE.config.apiData.repositoryEditing &&
			Minecraft.getMinecraft().theWorld != null && Keyboard.getEventKey() == Keyboard.KEY_O &&
			Keyboard.getEventKeyState()) {
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof GuiChest) {
				GuiChest eventGui = (GuiChest) event.gui;
				ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
				IInventory lower = cc.getLowerChestInventory();

				if (lower.getStackInSlot(23) != null && lower.getStackInSlot(23).getDisplayName().endsWith("Crafting Table")) {
					ItemStack res = lower.getStackInSlot(25);
					String resInternalname = neu.manager.getInternalNameForItem(res);

					JsonObject recipe = new JsonObject();

					String[] x = {"1", "2", "3"};
					String[] y = {"A", "B", "C"};

					for (int i = 0; i <= 18; i += 9) {
						for (int j = 0; j < 3; j++) {
							ItemStack stack = lower.getStackInSlot(10 + i + j);
							String internalname = "";
							if (stack != null) {
								internalname = neu.manager.getInternalNameForItem(stack);
								if (!neu.manager.getItemInformation().containsKey(internalname)) {
									neu.manager.writeItemToFile(stack);
								}
								internalname += ":" + stack.stackSize;
							}
							recipe.addProperty(y[i / 9] + x[j], internalname);
						}
					}

					JsonObject json = neu.manager.getJsonForItem(res);
					json.add("recipe", recipe);
					json.addProperty("internalname", resInternalname);
					json.addProperty("clickcommand", "viewrecipe");
					json.addProperty("modver", NotEnoughUpdates.VERSION);
					try {
						Utils.addChatMessage("Added: " + resInternalname);
						neu.manager.writeJsonDefaultDir(json, resInternalname + ".json");
						neu.manager.loadItem(resInternalname);
					} catch (IOException ignored) {
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onRenderLast(RenderWorldLastEvent event) {
		CrystalMetalDetectorSolver.render(event.partialTicks);
	}

	/**
	 * Support for switching between different pages in the RecipeView gui via right and left arrow key
	 * @param event
	 */
	//Because GuiScreen.keyTyped does not fire the KEY_LEFT and KEY_RIGHT keys. Maybe some event cancelled it?
	@SubscribeEvent
	public void onMouseClick(GuiScreenEvent.KeyboardInputEvent.Post event) {

		if (!NotEnoughUpdates.INSTANCE.isOnSkyblock()) return;

		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft == null || minecraft.thePlayer == null) return;

		GuiScreen screen = minecraft.currentScreen;
		if (screen instanceof GuiItemRecipe) {
			GuiItemRecipe itemRecipe = (GuiItemRecipe) screen;
			itemRecipe.arrowKeyboardInput();
		}
	}
}
