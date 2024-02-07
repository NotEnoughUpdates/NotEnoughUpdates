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

package io.github.moulberry.notenoughupdates.miscgui.minionhelper;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.auction.APIManager;
import io.github.moulberry.notenoughupdates.core.util.MiscUtils;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.render.renderables.OverviewLine;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.MinionRequirement;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.CraftingSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.CustomSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.MinionSource;
import io.github.moulberry.notenoughupdates.util.ItemResolutionQuery;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Minion extends OverviewLine {
	private final String internalName;
	private final int tier;
	private String displayName;
	private MinionSource minionSource;
	private CustomSource customSource;
	private Minion parent;
	private final List<MinionRequirement> requirements = new ArrayList<>();

	private boolean crafted = false;

	private final int xpGain;

	private boolean meetRequirements = false;

	public Minion(String internalName, int tier, int xpGain) {
		this.internalName = internalName;
		this.tier = tier;
		this.xpGain = xpGain;
	}

	public MinionSource getMinionSource() {
		return minionSource;
	}

	public void setMinionSource(MinionSource minionSource) {
		this.minionSource = minionSource;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public boolean isCrafted() {
		return crafted;
	}

	public void setCrafted(boolean crafted) {
		this.crafted = crafted;
	}

	public String getInternalName() {
		return internalName;
	}

	public void setParent(Minion parent) {
		this.parent = parent;
	}

	public Minion getParent() {
		return parent;
	}

	public int getTier() {
		return tier;
	}

	public List<MinionRequirement> getRequirements() {
		return requirements;
	}

	public boolean doesMeetRequirements() {
		return meetRequirements;
	}

	public void setMeetRequirements(boolean meetRequirements) {
		this.meetRequirements = meetRequirements;
	}

	public int getXpGain() {
		return xpGain;
	}

	@Override
	public void onClick() {
		if (Mouse.getEventButton() != 0 || !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
			NotEnoughUpdates.INSTANCE.manager.displayGuiItemRecipe(internalName);
		} else {
			if (minionSource instanceof CraftingSource) {
				CraftingSource craftingSource = (CraftingSource) minionSource;
				Map<String, Integer> counts = new HashMap<>();
				for (Map.Entry<String, Integer> entry : craftingSource.getItems().entries()) {
					counts.compute(entry.getKey(), (k, v) -> (v == null ? 0 : v) + entry.getValue());
				}
				Optional<Map.Entry<String, Integer>> resource = counts
					.entrySet()
					.stream()
					.filter(it -> !APIManager.hardcodedVanillaItems.contains(it.getKey()))
					.max(Comparator.comparingInt(Map.Entry::getValue));
				if (!resource.isPresent()) return;

				String bazaarName = resource.get().getKey();
				int totalAmount = resource.get().getValue();

				MiscUtils.copyToClipboard(String.valueOf(totalAmount));
				ItemStack itemStack = new ItemResolutionQuery(NotEnoughUpdates.INSTANCE.manager).withKnownInternalName(
					bazaarName).resolveToItemStack();
				if (itemStack != null) {
					String displayName = Utils.cleanColour(itemStack.getDisplayName());
					NotEnoughUpdates.INSTANCE.trySendCommand("/bz " + displayName);
				}
			}
		}
	}

	public void setCustomSource(CustomSource customSource) {
		this.customSource = customSource;
	}

	public CustomSource getCustomSource() {
		return customSource;
	}
}
