/*
 * Copyright (C) 2022-2023 Linnea Gräf
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

package io.github.moulberry.notenoughupdates.hooks;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.ApiUtil;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;

public class ThreadDownloadImageHook {
	public static String hookThreadImageLink(String originalLink) {
		if (!NotEnoughUpdates.INSTANCE.config.misc.fixSteveSkulls || originalLink == null || !originalLink.startsWith(
			"http://textures.minecraft.net"))
			return originalLink;
		return originalLink.replace("http://", "https://");
	}

	public static void hookThreadImageConnection(HttpURLConnection connection) {
		if ((connection instanceof HttpsURLConnection) && NotEnoughUpdates.INSTANCE.config.misc.fixSteveSkulls) {
			ApiUtil.patchHttpsRequest((HttpsURLConnection) connection);
		}
	}

	public interface AccessorThreadDownloadImageData {
		String getOriginalUrl();

		String getPatchedUrl();
	}
}
