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

package io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements;

import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.util.Utils;

public class ReputationRequirement extends MinionRequirement {

	private final String reputationType;
	private final int reputation;
	private final String description;

	public ReputationRequirement(String reputationType, int reputation) {
		this.reputationType = reputationType;
		this.reputation = reputation;

		String reputationName = StringUtils.firstUpperLetter(reputationType.toLowerCase());
		description = Utils.formatNumberWithDots(reputation) + " §7" + reputationName + " Reputation";
	}

	public int getReputation() {
		return reputation;
	}

	public String getReputationType() {
		return reputationType;
	}

	@Override
	public String printDescription(String color) {
		return "Reputation: " + color + description;
	}
}
