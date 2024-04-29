/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.options.separatesections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.Accordion;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigEditorButton;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDropdown;
import io.github.moulberry.moulconfig.annotations.ConfigOption;
import io.github.moulberry.moulconfig.observer.Property;
import io.github.moulberry.notenoughupdates.options.customtypes.ConfigVersionDisplay;
import io.github.moulberry.notenoughupdates.util.Utils;

public class About {
	@ConfigOption(name = "Current Version", desc = "This is the NEU version you are running currently")
	@ConfigVersionDisplay
	public transient Void currentVersion = null;

	@ConfigOption(name = "Check for Updates", desc = "Automatically check for updates on each startup")
	@Expose
	@ConfigEditorBoolean
	public boolean autoUpdates = true;

	@ConfigOption(name = "Update Stream", desc = "How often do you want to get updates")
	@Expose
	@ConfigEditorDropdown
	public Property<UpdateStream> updateStream = Property.of(UpdateStream.FULL);

	@ConfigOption(name = "Used Software", desc = "Information about used software and licenses")
	@Accordion
	@Expose
	public Licenses licenses = new Licenses();

	public enum UpdateStream {
		PRE("Full Releases and Beta", "pre"), FULL("Full Releases", "full"), NONE("None", "none");
		public final String stream;
		public final String label;

		UpdateStream(String name, String stream) {
			this.label = name;
			this.stream = stream;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	public static class Licenses {

		@ConfigOption(name = "Forge", desc = "Forge is available under the LGPL 3.0 license")
		@ConfigEditorButton(buttonText = "Source")
		public Runnable forge = () -> Utils.openUrl("https://github.com/MinecraftForge/MinecraftForge");

		@ConfigOption(name = "Mixin", desc = "Mixin is available under the MIT license")
		@ConfigEditorButton(buttonText = "Source")
		public Runnable mixin = () -> Utils.openUrl("https://github.com/SpongePowered/Mixin/");

		@ConfigOption(name = "LibAutoUpdate", desc = "LibAutoUpdate is available under the BSD 2 Clause license")
		@ConfigEditorButton(buttonText = "Source")
		public Runnable libAutoUpdate = () -> Utils.openUrl("https://git.nea.moe/nea/libautoupdate/");

		@ConfigOption(name = "Kotlin", desc = "Kotlin is available under the Apache 2 license")
		@ConfigEditorButton(buttonText = "Source")
		public Runnable kotlin = () -> Utils.openUrl("https://github.com/jetbrains/kotlin/");

		@ConfigOption(name = "AutoService", desc = "auto-service-ksp is available under the Apache 2 license")
		@ConfigEditorButton(buttonText = "Source")
		public Runnable autoService = () -> Utils.openUrl("https://github.com/ZacSweers/auto-service-ksp/");

		@ConfigOption(name = "Brigadier", desc = "Brigadier is available under the MIT license")
		@ConfigEditorButton(buttonText = "Source")
		public Runnable brigadier = () -> Utils.openUrl("https://github.com/Mojang/brigadier/");

		@ConfigOption(name = "JB Annotations", desc = "Jetbrains annotations is available under the Apache 2 license")
		@ConfigEditorButton(buttonText = "Source")
		public Runnable annotations = () -> Utils.openUrl("https://github.com/JetBrains/java-annotations");

		@ConfigOption(name = "MoulConfig", desc = "MoulConfig is available under the LGPL 3.0 license")
		@ConfigEditorButton(buttonText = "Source")
		public Runnable moulConfig = () -> Utils.openUrl("https://github.com/NotEnoughUpdates/MoulConfig");

		@ConfigOption(name = "Bliki", desc = "Bliki Core is available under the Eclipse Public License 1.0 license")
		@ConfigEditorButton(buttonText = "Source")
		public Runnable blikiCore = () -> Utils.openUrl("https://github.com/AaronZhangL/blikiCore/");

		@ConfigOption(name = "Lombok", desc = "Lombok is available under the MIT license")
		@ConfigEditorButton(buttonText = "Website")
		public Runnable lombok = () -> Utils.openUrl("https://projectlombok.org/");

	}
}
