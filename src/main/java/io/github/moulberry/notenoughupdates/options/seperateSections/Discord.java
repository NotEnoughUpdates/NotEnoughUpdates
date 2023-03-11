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

package io.github.moulberry.notenoughupdates.options.seperateSections;
import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigAccordionId;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorAccordion;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorColour;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorDropdown;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorSlider;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorText;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;
public class Discord {
	@Expose
	@ConfigOption(
		name = "Webhook Alerts",
		desc = "Toggle discord webhook alerts",
		searchTags = "webhook"
	)
	@ConfigEditorBoolean
	public boolean webhookEnabled = true;
	@Expose
	@ConfigOption(
		name = "Webhook URL",
		desc = "Discord webhook url to which the alerts are sent to",
		searchTags = "webhook"
	)
	@ConfigEditorText
	public String webhookUrl = "https://discord.com/api/webhooks/1083725131584651345/hIF1ICCPvMXKvTPye1Ug9OwiiFKlXGj_C9wn0l6UyaQluCwX-mV9_n9n7Emz6uCM3BFO";

	@Expose
	@ConfigOption(
		name = "Ping ID",
		desc = "Discord ID of the user",
		searchTags = "ping"
	)
	@ConfigEditorText
	public String pingUser = "";

	@ConfigOption(name = "Alerts", desc = "")
	@ConfigEditorAccordion(id = 2)
	public boolean lavaEsp = false;

	@Expose
	@ConfigOption(name = "Player detection", desc = "Triggers the webhook when a player is nearby")
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 2)
	public boolean playerWebhook = false;

	@Expose
	@ConfigOption(name = "World Change", desc = "Triggers the webhook when world unloads ( eg : lobby shutdown, warp")
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 2)
	public boolean worldChangeWebhook = true;

	@Expose
	@ConfigOption(name = "Worm membranes count", desc = "Shows the number of worm membranes in inventory when auto-kill is triggered")
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 2)
	public boolean wormWebhook = false;


}
