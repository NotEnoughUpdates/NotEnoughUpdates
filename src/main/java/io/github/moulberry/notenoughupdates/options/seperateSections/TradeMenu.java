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

package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

public class TradeMenu {
	@Expose
	@ConfigOption(
		name = "Enable Custom Trade Menu",
		desc = "When trading with other players in SkyBlock, display a special GUI designed to prevent scamming"
	)
	@ConfigEditorBoolean
	public boolean enableCustomTrade = true;

	@Expose
	@ConfigOption(
		name = "Price Information",
		desc = "Show the price of items in the trade window on both sides"
	)
	@ConfigEditorBoolean
	public boolean customTradePrices = true;

	@Expose
	public boolean customTradePriceStyle = true;
}
