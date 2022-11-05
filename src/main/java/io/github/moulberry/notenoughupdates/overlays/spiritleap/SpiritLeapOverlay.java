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

package io.github.moulberry.notenoughupdates.overlays.spiritleap;

import com.google.common.collect.Ordering;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.overlays.MiningOverlay;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpiritLeapOverlay {

	private static final Minecraft minecraft = Minecraft.getMinecraft();
	private final Map<String, DungeonClass> ignToClass = new HashMap<>();
	private static final Map<String, LeapButtonData> leapButtons = new HashMap<>();
	private static Map<Integer, Position> numberToPos = new HashMap<>();

	// head (skin)
	private final Map<String, ResourceLocation> skinHeads = new HashMap<>();

	public SpiritLeapOverlay() {
		numberToPos.put(1, NotEnoughUpdates.INSTANCE.config.dungeons.playerOne);
		numberToPos.put(2, NotEnoughUpdates.INSTANCE.config.dungeons.playerTwo);
		numberToPos.put(3, NotEnoughUpdates.INSTANCE.config.dungeons.playerThree);
		numberToPos.put(4, NotEnoughUpdates.INSTANCE.config.dungeons.playerFour);
		numberToPos.put(5, NotEnoughUpdates.INSTANCE.config.dungeons.playerFive);
	}

	private static final Ordering<NetworkPlayerInfo> playerOrdering = Ordering.from(new MiningOverlay.PlayerComparator());

	@SubscribeEvent
	public void onRender(GuiScreenEvent.BackgroundDrawnEvent event) {
		if (!shouldRender()) return;
		GuiChest currentScreen = (GuiChest) Minecraft.getMinecraft().currentScreen;
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();
		GL11.glColor4f(1, 1, 1, 1);
		GlStateManager.disableLighting();
		Utils.drawGradientRect(0, 0, width, height, -1072689136, -804253680);

		initClasses(currentScreen);

		leapButtons.forEach((name, button) -> {
			Position position = button.position;
			int elementHeight = 55;
			int elementWidth = 123;
			int x = position.getAbsX(scaledResolution, elementWidth);
			int y = position.getAbsY(scaledResolution, elementHeight);

			if (position.isCenterX()) x -= elementWidth / 2;
			if (position.isCenterY()) y -= elementHeight / 2;
			button.button.render(x, y, button.location, false);

		});

	}

	private final Pattern pattern = Pattern.compile("\\[.+\\] (.+) \\((.+)\\)");

	private void initClasses(GuiChest currentScreen) {
		List<NetworkPlayerInfo> players =
			playerOrdering.sortedCopy(Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap());
		for (NetworkPlayerInfo info : players) {
			String s = EnumChatFormatting.getTextWithoutFormattingCodes(Minecraft.getMinecraft().ingameGUI
				.getTabList()
				.getPlayerName(info));
			Matcher matcher = pattern.matcher(s);
			if (matcher.matches()) {
				String name = matcher.group(1);
				String dungeonClass = matcher.group(2);
				if (dungeonClass.contains("Mage")) {
					ignToClass.put(name, DungeonClass.MAGE);
				}
				if (dungeonClass.contains("Berserk")) {
					ignToClass.put(name, DungeonClass.BERSERK);
				}
				if (dungeonClass.contains("Tank")) {
					ignToClass.put(name, DungeonClass.TANK);
				}
				if (dungeonClass.contains("Archer")) {
					ignToClass.put(name, DungeonClass.ARCHER);
				}
				if (dungeonClass.contains("Healer")) {
					ignToClass.put(name, DungeonClass.HEALER);
				}
			}
		}

		if (skinHeads.size() != ignToClass.size()) {
			for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
				if (ignToClass.containsKey(player.getName())) {
					AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer) player;
					ResourceLocation skin = abstractClientPlayer.getLocationSkin();
					skinHeads.put(player.getName(), skin);
				}
			}
		}

		AtomicInteger atomicInteger = new AtomicInteger();
		for (Slot inventorySlot : currentScreen.inventorySlots.inventorySlots) {
			ItemStack stack = inventorySlot.getStack();
			if (!inventorySlot.getHasStack() || stack.getItem() != Items.skull) continue;
			String displayName = EnumChatFormatting.getTextWithoutFormattingCodes(stack.getDisplayName()).trim();
			if (ignToClass.containsKey(displayName) &&
				!leapButtons.containsKey(displayName)) {
				int slotIndex = inventorySlot.getSlotIndex();
				System.out.println(leapButtons.size() + 1 + " for " + displayName);
				DungeonClass dungeonClass = ignToClass.get(displayName);
				LeapButtonData leapButton = new LeapButtonData(
					new SpiritLeapButton(dungeonClass.prefix, displayName),
					slotIndex, skinHeads.get(displayName),
					leapButtons.size() + 1, displayName,
					dungeonClass, numberToPos.get(atomicInteger.incrementAndGet())
				);
				leapButtons.put(leapButton.player, leapButton.clone());
			}
		}
	}

	private static void handleMouseClick(int slotIndex) {
		GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
		if (!(currentScreen instanceof GuiChest)) return;
		GuiChest chest = (GuiChest) currentScreen;
		System.out.println(Minecraft.getMinecraft().playerController.windowClick(
			chest.inventorySlots.windowId,
			slotIndex, 2, 3, Minecraft.getMinecraft().thePlayer
		));
	}

	public static void mouseEvent() {
		if (!shouldRender()) return;
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();
		int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

		leapButtons.forEach((name, button) -> {
			Position position = button.position;
			int x = position.getRawX();
			int y = position.getRawY();

			if (mouseX >= x && mouseX <= x + button.button.getWidth()
				&& mouseY >= y && mouseY <= y + button.button.getHeight()) {
				System.out.println("in");
				handleMouseClick(button.slotToClick);
			}
		});
	}

	@SubscribeEvent
	public void onWorldChange(WorldEvent.Load event) {
		leapButtons.clear();
		ignToClass.clear();
		skinHeads.clear();
	}

	public static class LeapButtonData {

		public final SpiritLeapButton button;
		public final int slotToClick;

		public final ResourceLocation location;

		public final int buttonIndex;
		public final String player;
		public final DungeonClass dungeonClass;

		public final Position position;

		private LeapButtonData(
			SpiritLeapButton button,
			int slotToClick,
			ResourceLocation location, int buttonIndex,
			String player,
			DungeonClass dungeonClass,
			Position position
		) {
			this.button = button;
			this.slotToClick = slotToClick;
			this.location = location;
			this.buttonIndex = buttonIndex;
			this.player = player;
			this.dungeonClass = dungeonClass;
			this.position = position;
		}

		@Override
		protected LeapButtonData clone()  {
			return new LeapButtonData(button,slotToClick,location,buttonIndex,player,dungeonClass,position);
		}
	}

	public enum DungeonClass {
		HEALER("H", Color.GREEN),
		TANK("T", Color.gray),
		ARCHER("A", Color.black),
		BERSERK("B", Color.RED),
		MAGE("M", Color.CYAN);

		final String prefix;
		final Color color;

		DungeonClass(String prefix, Color color) {
			this.prefix = prefix;
			this.color = color;
		}

		public Color getColor() {
			return color;
		}
	}

	public static boolean shouldRender() {
		if (!SBInfo.getInstance().isInDungeon) return false;
		if (minecraft == null || minecraft.thePlayer == null || minecraft.currentScreen == null) return false;
		if (minecraft.currentScreen instanceof GuiChest) {
			if (SBInfo.getInstance().lastOpenChestName.equals("Spirit Leap")) {
				return true;
			}
		}
		return false;
	}

	public static void renderDummy(SpiritLeapMapDummy pos) {

		int overlayHeight = 123;
		int overlayWidth = 20;

		int x = pos.position.getRawX();
		int y = pos.position.getRawY();
		Gui.drawRect(x, y, x + overlayWidth, y + overlayHeight, 0x80000000);

		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.tryBlendFuncSeparate(
			GL11.GL_SRC_ALPHA,
			GL11.GL_ONE_MINUS_SRC_ALPHA,
			GL11.GL_ONE,
			GL11.GL_ONE_MINUS_SRC_ALPHA
		);

		Minecraft.getMinecraft().fontRendererObj.drawString("Teleport to §fMinikloon",
			x + 5, y, 0xffffff, true
		);
	}

	public static class SpiritLeapMapDummy {

		public final SpiritLeapButton button;
		public final Position position;

		public SpiritLeapMapDummy(SpiritLeapButton button, Position position) {
			this.button = button;
			this.position = position;
		}

		@Override
		public SpiritLeapMapDummy clone() {
			return new SpiritLeapMapDummy(new SpiritLeapButton(button.getDungeonClass(), button.getIgn()), position);
		}

		public static Vector2f getDummySize() {
			return new Vector2f(123, 17);
		}
	}
}
