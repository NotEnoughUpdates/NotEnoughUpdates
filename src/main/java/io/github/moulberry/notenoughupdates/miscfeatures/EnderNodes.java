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

package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import io.github.moulberry.notenoughupdates.util.TitleUtil;
import net.minecraft.client.Minecraft;

public class EnderNodes {
	public static void displayEndermiteNotif() {
		if (NotEnoughUpdates.INSTANCE.config.notifications.endermiteAlert && SBInfo.getInstance().getLocation() != null &&
			SBInfo.getInstance().getLocation().equals("combat_3")) {
			TitleUtil.getInstance().createTitle("Nested Endermite",
				NotEnoughUpdates.INSTANCE.config.notifications.endermiteAlertTicks,
				SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.notifications.endermiteAlertColor));
			Minecraft.getMinecraft().thePlayer.playSound("random.orb", 1, 1);
		}
	}
}
