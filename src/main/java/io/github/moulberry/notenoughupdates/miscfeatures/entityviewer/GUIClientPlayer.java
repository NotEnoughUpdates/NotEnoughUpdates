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

package io.github.moulberry.notenoughupdates.miscfeatures.entityviewer;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class GUIClientPlayer extends AbstractClientPlayer {
	public GUIClientPlayer() {
		super(null, new GameProfile(UUID.randomUUID(), "GuiPlayer"));
	}

	public ResourceLocation overrideSkin = DefaultPlayerSkin.getDefaultSkinLegacy();
	public ResourceLocation overrideCape = null;
	public boolean overrideIsSlim = false;
	NetworkPlayerInfo playerInfo = new NetworkPlayerInfo(this.getGameProfile()) {
		@Override
		public String getSkinType() {
			return overrideIsSlim ? "slim" : "default";
		}

		@Override
		public ResourceLocation getLocationSkin() {
			return overrideSkin;
		}

		@Override
		public ResourceLocation getLocationCape() {
			return overrideCape;
		}
	};

	@Override
	public String getName() {
		return name;
	}

	public String name;

	public void setName(String name) {
		this.name = name;
	}

	@Override
	protected NetworkPlayerInfo getPlayerInfo() {
		return playerInfo;
	}
}
