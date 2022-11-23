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
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.render.renderables.OverviewLine;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.MinionRequirement;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.CustomSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.MinionSource;

import java.util.ArrayList;
import java.util.List;

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
		NotEnoughUpdates.INSTANCE.manager.displayGuiItemRecipe(internalName);
	}

	public void setCustomSource(CustomSource customSource) {
		this.customSource = customSource;
	}

	public CustomSource getCustomSource() {
		return customSource;
	}
}
