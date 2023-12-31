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
import lombok.Getter;

public class ReputationRequirement extends MinionRequirement {

	@Getter
	private final String reputationType;
	@Getter
	private final int reputation;
	private final String description;

	public ReputationRequirement(String reputationType, int reputation) {
		this.reputationType = reputationType;
		this.reputation = reputation;

		String reputationName = StringUtils.firstUpperLetter(reputationType.toLowerCase());
		description = Utils.formatNumberWithDots(reputation) + " §7" + reputationName + " Reputation";
	}

	@Override
	public String printDescription(String color) {
		return "Reputation: " + color + description;
	}
}
