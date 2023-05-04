/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

@NEUAutoSubscribe
public class MutingComposter {

	private static final MutingComposter INSTANCE = new MutingComposter();
	private final List<String> mutableSounds = Arrays.asList(
		"mob.wolf.growl",
		"tile.piston.out",
		"liquid.water",
		"mob.chicken.plop"
	);

	public static MutingComposter getInstance() {
		return INSTANCE;
	}

	protected boolean isEnabled() {
		return "garden".equals(SBInfo.getInstance().getLocation())
			&& NotEnoughUpdates.INSTANCE.config.garden.muteComposterSounds;
	}

	@SubscribeEvent
	public void onSoundPlay(PlaySoundEvent event) {
		if (mutableSounds.contains(event.name) && isEnabled()) {
			event.result = null;
		}
	}
}
