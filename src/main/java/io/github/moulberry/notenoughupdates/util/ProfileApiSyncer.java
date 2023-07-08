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

package io.github.moulberry.notenoughupdates.util;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.profileviewer.SkyblockProfiles;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.function.Consumer;

public class ProfileApiSyncer {
	private static final ProfileApiSyncer INSTANCE = new ProfileApiSyncer();

	private final HashMap<String, Long> resyncTimes = new HashMap<>();
	private final HashMap<String, Runnable> syncingCallbacks = new HashMap<>();
	private final HashMap<String, Consumer<SkyblockProfiles>> finishSyncCallbacks = new HashMap<>();
	private long lastResync;

	public static ProfileApiSyncer getInstance() {
		return INSTANCE;
	}

	public void requestResync(String id, long timeBetween) {
		requestResync(id, timeBetween, null);
	}

	public void requestResync(String id, long timeBetween, Runnable syncingCallback) {
		requestResync(id, timeBetween, null, null);
	}

	public void requestResync(
		String id,
		long timeBetween,
		Runnable syncingCallback,
		Consumer<SkyblockProfiles> finishSyncCallback
	) {
		resyncTimes.put(id, timeBetween);
		syncingCallbacks.put(id, syncingCallback);
		finishSyncCallbacks.put(id, finishSyncCallback);
	}

	public long getCurrentResyncTime() {
		long time = -1;
		for (long l : resyncTimes.values()) {
			if (l > 0 && (l < time || time == -1)) time = l;
		}
		return time;
	}

	public void tick() {
		if (Minecraft.getMinecraft().thePlayer == null) return;

		long resyncTime = getCurrentResyncTime();

		if (resyncTime < 0) return;

		long currentTime = System.currentTimeMillis();

		if (currentTime - lastResync > resyncTime) {
			lastResync = currentTime;
			resyncTimes.clear();

			for (Runnable r : syncingCallbacks.values()) r.run();
			syncingCallbacks.clear();

			forceResync();
		}
	}

	private void forceResync() {
		if (Minecraft.getMinecraft().thePlayer == null) return;

		String uuid = Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", "");
		NotEnoughUpdates.INSTANCE.manager.apiUtils
			.newHypixelApiRequest("/skyblock/profiles")
			.queryArgument("uuid", uuid)
			.requestJson()
			.thenAcceptAsync((profile) -> {
				SkyblockProfiles skyblockProfiles = new SkyblockProfiles(NotEnoughUpdates.profileViewer, uuid);
				for (Consumer<SkyblockProfiles> c : finishSyncCallbacks.values())
					c.accept((skyblockProfiles));
				finishSyncCallbacks.clear();
			}, MinecraftExecutor.OnThread);
	}
}
