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
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Minion extends OverviewLine {
	@Getter
	private final String internalName;
	@Getter
	private final int tier;
	@Getter
	@Setter
	private String displayName;
	@Setter
	@Getter
	private MinionSource minionSource;
	@Setter
	@Getter
	private CustomSource customSource;
	@Setter
	@Getter
	private Minion parent;
	@Getter
	private final List<MinionRequirement> requirements = new ArrayList<>();

	@Setter
	@Getter
	private boolean crafted = false;

	@Getter
	private final int xpGain;

	@Setter
	private boolean meetRequirements = false;

	public Minion(String internalName, int tier, int xpGain) {
		this.internalName = internalName;
		this.tier = tier;
		this.xpGain = xpGain;
	}

	public boolean doesMeetRequirements() {
		return meetRequirements;
	}

	@Override
	public void onClick() {
		NotEnoughUpdates.INSTANCE.manager.displayGuiItemRecipe(internalName);
	}

}
