package io.github.moulberry.notenoughupdates.dungeons;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.dungeons.map.DungeonMap;
import io.github.moulberry.notenoughupdates.dungeons.map.DungeonMapPlayers;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.options.seperateSections.DungeonMapConfig;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SpiritLeap {

	DungeonMap dungeonMap = new DungeonMap();
	DungeonMapPlayers savedPlayers = null;

	@SubscribeEvent
	public void onGuiRender(GuiScreenEvent.DrawScreenEvent.Post event) {
		if (!(event.gui instanceof GuiChest)) return;
		if (!SBInfo.getInstance().isInDungeon) return;
		GuiChest gui = (GuiChest) event.gui;
		ContainerChest chest = (ContainerChest) gui.inventorySlots;
		if (!chest.getLowerChestInventory().getDisplayName().getUnformattedText().equals("Spirit Leap")) return;
		DungeonMapConfig config = NotEnoughUpdates.INSTANCE.config.dungeonMap;
		this.dungeonMap.renderMap(
			config,
			this.dungeonMap.parsedDungeon,
			savedPlayers != null ? savedPlayers : this.dungeonMap.dungeonMapPlayers,
			((AccessorGuiContainer)gui).getGuiLeft() - Math.round(20 * config.dmBorderSize),
			((AccessorGuiContainer)gui).getGuiTop() + gui.height / 2
		);
	}

	@SubscribeEvent
	public void onGuiClick(GuiScreenEvent.MouseInputEvent.Post event) {

	}

}
