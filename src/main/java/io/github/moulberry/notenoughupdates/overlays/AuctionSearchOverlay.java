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

package io.github.moulberry.notenoughupdates.overlays;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
import io.github.moulberry.notenoughupdates.miscfeatures.CookieWarning;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiEditSign;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Comparator;

@NEUAutoSubscribe
public class AuctionSearchOverlay extends SearchOverlayScreen {

	private static final Comparator<String> salesComparator = (o1, o2) -> {
		JsonObject auctionInfo1 = NotEnoughUpdates.INSTANCE.manager.auctionManager.getItemAuctionInfo(o1);
		JsonObject auctionInfo2 = NotEnoughUpdates.INSTANCE.manager.auctionManager.getItemAuctionInfo(o2);

		boolean auc1Invalid = auctionInfo1 == null || !auctionInfo1.has("sales");
		boolean auc2Invalid = auctionInfo2 == null || !auctionInfo2.has("sales");

		if (auc1Invalid && auc2Invalid) return o1.compareTo(o2);
		if (auc1Invalid) return 1;
		if (auc2Invalid) return -1;

		int sales1 = auctionInfo1.get("sales").getAsInt();
		int sales2 = auctionInfo2.get("sales").getAsInt();

		if (sales1 == sales2) return o1.compareTo(o2);
		if (sales1 > sales2) return -1;
		return 1;
	};

	public AuctionSearchOverlay() {
		super(new TileEntitySign());
	}

	public AuctionSearchOverlay(TileEntitySign sign) {
		super(sign);
		this.tileSign = sign;
		this.guiType = GuiType.AUCTION_HOUSE;
	}

	public static boolean shouldReplace() {
		return Minecraft.getMinecraft().currentScreen instanceof AuctionSearchOverlay;
	}

	public static boolean isinAhSign() {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return false;
		if (!NotEnoughUpdates.INSTANCE.config.ahTweaks.enableSearchOverlay) return false;

		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiEditSign)) {
			if (!NotEnoughUpdates.INSTANCE.config.ahTweaks.keepPreviousSearch) searchString = "";
			return false;
		}

		String lastContainer = Utils.getLastOpenChestName();
		if (!lastContainer.equals("Auctions Browser") && !lastContainer.startsWith("Auctions: ")) return false;

		TileEntitySign tes = ((AccessorGuiEditSign) Minecraft.getMinecraft().currentScreen).getTileSign();

		if (tes == null) return false;
		if (tes.getPos().getY() != 0) return false;
		if (!tes.signText[2].getUnformattedText().equals("^^^^^^^^^^^^^^^")) return false;
		return tes.signText[3].getUnformattedText().equals("Enter query");
	}


	@SubscribeEvent
	public void onSlotClick(SlotClickEvent event) {
		if (!enableSearchOverlay()) return;
		if (disableClientSideGUI()) return;
		if (event.clickedButton == 1 && event.clickType == 0) return;
		if (!CookieWarning.hasActiveBoosterCookie()) return;
		if (!Utils.getOpenChestName().startsWith("Auctions")) return;
		ItemStack stack = event.slot.getStack();
		if (event.slot.slotNumber == 48 && stack != null && stack.hasDisplayName() && stack.getItem() == Items.sign && stack.getDisplayName().equals("§aSearch")) {
			event.setCanceled(true);
			Minecraft.getMinecraft().currentScreen = null;
			NotEnoughUpdates.INSTANCE.openGui = new AuctionSearchOverlay();
		}
	}

	@SubscribeEvent
	public void onSignDrawn(GuiScreenEvent.DrawScreenEvent.Pre event) {
		if (!isinAhSign() || !(event.gui instanceof GuiEditSign) || event.gui instanceof SearchOverlayScreen)
			return;
		GuiEditSign guiEditSign = (GuiEditSign) event.gui;
		TileEntitySign tileSign = ((AccessorGuiEditSign) guiEditSign).getTileSign();
		if (tileSign != null) {
			event.setCanceled(true);
			Minecraft.getMinecraft().displayGuiScreen(new AuctionSearchOverlay(tileSign));
		}
	}

	@Override
	public Comparator<String> getSearchComparator() {
		return salesComparator;
	}

	@Override
	public boolean enableSearchOverlay() {
		return NotEnoughUpdates.INSTANCE.config.ahTweaks.enableSearchOverlay;
	}

	@Override
	public ArrayList<String> previousSearches() {
		return NotEnoughUpdates.INSTANCE.config.hidden.previousAuctionSearches;
	}

	@Override
	public int searchHistorySize() {
		return NotEnoughUpdates.INSTANCE.config.ahTweaks.ahSearchHistorySize;
	}

	@Override
	public boolean showPastSearches() {
		return NotEnoughUpdates.INSTANCE.config.ahTweaks.showPastSearches;
	}

	@Override
	public boolean escFullClose() {
		return NotEnoughUpdates.INSTANCE.config.ahTweaks.escFullClose;
	}

	@Override
	public boolean keepPreviousSearch() {
		return NotEnoughUpdates.INSTANCE.config.ahTweaks.keepPreviousSearch;
	}

	@Override
	public GuiType currentGuiType() {
		return GuiType.AUCTION_HOUSE;
	}
}
