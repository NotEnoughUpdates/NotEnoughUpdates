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

package io.github.moulberry.notenoughupdates.miscfeatures.customblockzones;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomBlockSounds {

	static Gson gson = new Gson();

	static List<CustomSoundEvent> allCustomSoundEvents = new ArrayList<>();

	public static final CustomSoundEvent mithrilBreak = newCustomSoundEvent("mithril");
	public static final CustomSoundEvent gemstoneBreakRuby = newCustomSoundEvent("gemstoneRuby");
	public static final CustomSoundEvent gemstoneBreakAmber = newCustomSoundEvent("gemstoneAmber");
	public static final CustomSoundEvent gemstoneBreakAmethyst = newCustomSoundEvent("gemstoneAmethyst");
	public static final CustomSoundEvent gemstoneBreakSapphire = newCustomSoundEvent("gemstoneSapphire");
	public static final CustomSoundEvent gemstoneBreakJade = newCustomSoundEvent("gemstoneJade");
	public static final CustomSoundEvent gemstoneBreakTopaz = newCustomSoundEvent("gemstoneTopaz");
	public static final CustomSoundEvent gemstoneBreakJasper = newCustomSoundEvent("gemstoneJasper");
	public static final CustomSoundEvent titaniumBreak = newCustomSoundEvent("titanium");

	public static class ReloaderListener implements IResourceManagerReloadListener {
		@Override
		public void onResourceManagerReload(IResourceManager iResourceManager) {
			allCustomSoundEvents.forEach(CustomSoundEvent::reload);
		}
	}

	public static class CustomSoundEvent {
		public ResourceLocation soundEvent;
		public ResourceLocation configFile;
		private boolean loaded = false;
		private int timer = 0;
		private long lastReplaced = 0L;

		public CustomSoundEvent() {
			allCustomSoundEvents.add(this);
		}

		public boolean shouldReplace() {
			if (!loaded) reload();
			if (timer < 0) return true;
			long now = System.currentTimeMillis();
			if (now - lastReplaced >= timer) {
				lastReplaced = now;
				return true;
			}
			return false;
		}

		public ISound replaceSoundEvent(ISound sound) {
			return new PositionedSoundRecord(
				this.soundEvent,
				sound.getPitch(), sound.getVolume(),
				sound.getXPosF(), sound.getYPosF(), sound.getZPosF()
			);
		}

		public void reload() {
			loaded = true;
			IResource resource;
			try {
				resource = Minecraft.getMinecraft().getResourceManager().getResource(configFile);
			} catch (IOException e) {
				timer = -1;
				return;
			}
			try (Reader r = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
				JsonObject jsonObject = gson.fromJson(r, JsonObject.class);
				timer = jsonObject.getAsJsonPrimitive("debouncer").getAsInt() * 1000 / 20;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private static CustomSoundEvent newCustomSoundEvent(String soundEvent) {
		CustomSoundEvent event = new CustomSoundEvent();
		event.soundEvent = new ResourceLocation("notenoughupdates", soundEvent + ".break");
		event.configFile = new ResourceLocation(
			"notenoughupdates",
			"sounds/" + soundEvent.toLowerCase(Locale.ROOT) + "break.json"
		);
		return event;
	}

}
