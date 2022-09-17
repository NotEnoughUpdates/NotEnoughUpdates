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

package io.github.moulberry.notenoughupdates.miscgui.minionhelper.util;

import io.github.moulberry.notenoughupdates.miscgui.minionhelper.ApiData;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.Minion;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperManager;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.CollectionRequirement;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.CustomRequirement;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.MinionRequirement;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.ReputationRequirement;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.SlayerRequirement;
import io.github.moulberry.notenoughupdates.util.Utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MinionHelperRequirementsManager {

	private final MinionHelperManager manager;

	public MinionHelperRequirementsManager(MinionHelperManager manager) {
		this.manager = manager;
	}

	public List<MinionRequirement> getRequirements(Minion minion) {
		if (!minion.getRequirements().isEmpty()) {
			return minion.getRequirements();
		}

		Minion parent = minion.getParent();
		if (parent != null) {
			return getRequirements(parent);
		}

		return Collections.emptyList();
	}

	public boolean meetAllRequirements(Minion minion) {
		List<MinionRequirement> list = getRequirements(minion);
		for (MinionRequirement requirement : list) {
			if (!meetRequirement(minion, requirement)) {
				return false;
			}
		}

		Minion parent = minion.getParent();
		if (parent != null) {
			return meetAllRequirements(parent);
		}

		return true;
	}

	public boolean meetRequirement(Minion minion, MinionRequirement requirement) {
		ApiData apiData = manager.getApi().getApiData();
		if (apiData == null) return false;

		if (requirement instanceof CollectionRequirement) {
			if (apiData.isCollectionApiDisabled()) return true;

			CollectionRequirement collectionRequirement = (CollectionRequirement) requirement;
			String collection = collectionRequirement.getCollection();
			String internalName = manager.formatInternalName(collection);

			int need = collectionRequirement.getLevel();
			Map<String, Integer> highestCollectionTier = apiData.getHighestCollectionTier();
			if (highestCollectionTier.containsKey(internalName)) {
				int has = highestCollectionTier.get(internalName);

				return has >= need;
			}

		} else if (requirement instanceof SlayerRequirement) {
			SlayerRequirement slayerRequirement = (SlayerRequirement) requirement;
			String slayer = slayerRequirement.getSlayer();
			//Because the neu-repo uses 'eman' and the hypixel api is using 'enderman'
			if (slayer.equals("eman")) {
				slayer = "enderman";
			}
			int need = slayerRequirement.getLevel();
			Map<String, Integer> slayerTiers = apiData.getSlayerTiers();
			if (slayerTiers.containsKey(slayer)) {
				return slayerTiers.get(slayer) >= need;
			}

		} else if (requirement instanceof ReputationRequirement) {
			ReputationRequirement reputationRequirement = (ReputationRequirement) requirement;
			int need = reputationRequirement.getReputation();
			String reputationType = reputationRequirement.getReputationType();
			if (reputationType.equals("BARBARIAN")) {
				return apiData.getBarbariansReputation() >= need;
			} else if (reputationType.equals("MAGE")) {
				return apiData.getMagesReputation() >= need;
			} else {
				Utils.addChatMessage("§c[NEU] Minion Helper: Unknown reputation type: '" + reputationType + "'");
				return false;
			}
		} else if (requirement instanceof CustomRequirement) {
			return minion.doesMeetRequirements();
		}

		return false;
	}

	public void reloadRequirements() {
		for (Minion minion : manager.getAllMinions().values()) {
			minion.setMeetRequirements(meetAllRequirements(minion));
		}
	}
}
