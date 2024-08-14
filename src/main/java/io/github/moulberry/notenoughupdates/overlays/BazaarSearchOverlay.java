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
public class BazaarSearchOverlay extends SearchOverlayScreen {

	private static final Comparator<String> salesComparator = (o1, o2) -> {
		JsonObject bazaarInfo1 = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo(o1);
		JsonObject bazaarInfo2 = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo(o2);

		boolean auc1Invalid = bazaarInfo1 == null || !bazaarInfo1.has("curr_sell");
		boolean auc2Invalid = bazaarInfo2 == null || !bazaarInfo2.has("curr_sell");

		if (auc1Invalid && auc2Invalid) return o1.compareTo(o2);
		if (auc1Invalid) return 1;
		if (auc2Invalid) return -1;

		int sales1 = bazaarInfo1.get("curr_sell").getAsInt();
		int sales2 = bazaarInfo2.get("curr_sell").getAsInt();

		if (sales1 == sales2) return o1.compareTo(o2);
		if (sales1 > sales2) return -1;
		return 1;
	};

	public BazaarSearchOverlay() {
		super(new TileEntitySign());
	}

	public BazaarSearchOverlay(TileEntitySign tes) {
		super(tes);
		this.tileSign = tes;
		this.guiType = GuiType.BAZAAR;
	}

	public static boolean shouldReplace() {
		return Minecraft.getMinecraft().currentScreen instanceof BazaarSearchOverlay;
	}

	public static boolean isinBzSign() {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return false;
		if (!NotEnoughUpdates.INSTANCE.config.bazaarTweaks.enableSearchOverlay) return false;

		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiEditSign)) {
			if (!NotEnoughUpdates.INSTANCE.config.bazaarTweaks.keepPreviousSearch) searchString = "";
			return false;
		}

		String lastContainer = Utils.getLastOpenChestName();
		if (!lastContainer.startsWith("Bazaar ➜ ")) return false;

		TileEntitySign tes = ((AccessorGuiEditSign) Minecraft.getMinecraft().currentScreen).getTileSign();

		if (tes == null) return false;
		if (tes.getPos().getY() != 0) return false;
		if (!tes.signText[2].getUnformattedText().equals("^^^^^^^^^^^^^^^")) return false;
		return tes.signText[3].getUnformattedText().equals("Enter query");
	}

	@SubscribeEvent
	public void onSlotClick(SlotClickEvent event) {
		if (!enableSearchOverlay()) return;
		if (NotEnoughUpdates.INSTANCE.config.hidden.disableClientSideSearch) return;
		if (event.clickedButton == 1 && event.clickType == 0) return;
		if (!CookieWarning.hasActiveBoosterCookie()) return;
		if (!Utils.getOpenChestName().startsWith("Bazaar ➜")) return;
		ItemStack stack = event.slot.getStack();
		if (event.slot.slotNumber == 45 && stack != null && stack.hasDisplayName() && stack.getItem() == Items.sign && stack.getDisplayName().equals("§aSearch")) {
			event.setCanceled(true);
			NotEnoughUpdates.INSTANCE.openGui = new BazaarSearchOverlay();
		}
	}

	@SubscribeEvent
	public void onSignDrawn(GuiScreenEvent.DrawScreenEvent.Post event) {
		if (!isinBzSign() || !(event.gui instanceof GuiEditSign) || event.gui instanceof SearchOverlayScreen)
			return;
		GuiEditSign guiEditSign = (GuiEditSign) event.gui;
		TileEntitySign tileSign = ((AccessorGuiEditSign) guiEditSign).getTileSign();
		if (tileSign != null) {
			Minecraft.getMinecraft().displayGuiScreen(new BazaarSearchOverlay(tileSign));
		}
	}

	@Override
	public Comparator<String> getSearchComparator() {
		return salesComparator;
	}

	@Override
	public boolean enableSearchOverlay() {
		return NotEnoughUpdates.INSTANCE.config.bazaarTweaks.enableSearchOverlay;
	}

	@Override
	public ArrayList<String> previousSearches() {
		return NotEnoughUpdates.INSTANCE.config.hidden.previousBazaarSearches;
	}

	@Override
	public int searchHistorySize() {
		return NotEnoughUpdates.INSTANCE.config.bazaarTweaks.bzSearchHistorySize;
	}

	@Override
	public boolean showPastSearches() {
		return NotEnoughUpdates.INSTANCE.config.bazaarTweaks.showPastSearches;
	}

	@Override
	public boolean escFullClose() {
		return NotEnoughUpdates.INSTANCE.config.bazaarTweaks.escFullClose;
	}

	@Override
	public boolean keepPreviousSearch() {
		return NotEnoughUpdates.INSTANCE.config.bazaarTweaks.keepPreviousSearch;
	}

	@Override
	public GuiType currentGuiType() {
		return GuiType.BAZAAR;
	}
}
