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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.api.HypixelAPICache;
import io.github.moulberry.notenoughupdates.events.ProfileDataLoadedEvent;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

public class ApiUtil {
	private static final Gson gson = new Gson();
	private static final ExecutorService executorService = Executors.newFixedThreadPool(3);
	private static final String USER_AGENT = "NotEnoughUpdates/" + NotEnoughUpdates.VERSION;
	private static SSLContext ctx;
	private final Map<String, CompletableFuture<Void>> updateTasks = new HashMap<>();

	static {
		try {
			KeyStore letsEncryptStore = KeyStore.getInstance("JKS");
			letsEncryptStore.load(ApiUtil.class.getResourceAsStream("/neukeystore.jks"), "neuneu".toCharArray());
			ctx = SSLContext.getInstance("TLS");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			kmf.init(letsEncryptStore, null);
			tmf.init(letsEncryptStore);
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		} catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException |
						 IOException | CertificateException e) {
			System.out.println("Failed to load NEU keystore. A lot of API requests won't work");
			e.printStackTrace();
		}
	}

	public void updateProfileData() {
		updateProfileData(Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", ""));
	}

	public void updateProfileData(String playerUuid) {
		if (!updateTasks.getOrDefault(playerUuid, CompletableFuture.completedFuture(null)).isDone()) return;

		updateTasks.put(playerUuid, newHypixelApiRequest("skyblock/profiles")
			.queryArgument("uuid", Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", ""))
			.requestJson()
			.handle((jsonObject, throwable) -> {
				new ProfileDataLoadedEvent(jsonObject).post();
				return null;
			}));

	}

	public static class Request {

		private final List<NameValuePair> queryArguments = new ArrayList<>();
		private String baseUrl = null;
		private boolean shouldGunzip = false;
		private String method = "GET";
		private String postData = null;
		private String postContentType = null;

		public Request method(String method) {
			this.method = method;
			return this;
		}

		public Request url(String baseUrl) {
			this.baseUrl = baseUrl;
			return this;
		}

		public Request queryArgument(String key, String value) {
			queryArguments.add(new BasicNameValuePair(key, value));
			return this;
		}

		public Request queryArguments(Collection<NameValuePair> queryArguments) {
			this.queryArguments.addAll(queryArguments);
			return this;
		}

		public Request gunzip() {
			shouldGunzip = true;
			return this;
		}

		public Request postData(String contentType, String data) {
			this.postContentType = contentType;
			this.postData = data;
			return this;
		}

		public CompletableFuture<String> requestString() {
			CompletableFuture<String> result = new CompletableFuture<>();
			URL url;
			try {
				url = new URIBuilder(baseUrl).addParameters(queryArguments).build().toURL();
			} catch (URISyntaxException | MalformedURLException | NullPointerException e) {
				String message = "Error while building url! baseUrl: '" + this.baseUrl + "',  queryArguments: " + queryArguments;
				result.completeExceptionally(new RuntimeException(message, e));
				return result;
			}
			String link = url.toString();

			CompletableFuture<String> cachedResult = HypixelAPICache.INSTANCE.getCacheFor(link);
			if (cachedResult != null) return cachedResult;

			HypixelAPICache.INSTANCE.addToCache(link, result);

			executorService.execute(() -> {
				try {
					InputStream inputStream = null;
					URLConnection conn = null;
					try {
						conn = url.openConnection();
						if (conn instanceof HttpsURLConnection && ctx != null) {
							patchHttpsRequest((HttpsURLConnection) conn);
						}
						if (conn instanceof HttpURLConnection) {
							((HttpURLConnection) conn).setRequestMethod(method);
						}
						conn.setConnectTimeout(10000);
						conn.setReadTimeout(10000);
						conn.setRequestProperty("User-Agent", USER_AGENT);
						if (this.postContentType != null) {
							conn.setRequestProperty("Content-Type", this.postContentType);
						}
						if (this.postData != null) {
							conn.setDoOutput(true);
							try (OutputStream stream = conn.getOutputStream()) {
								stream.write(this.postData.getBytes(StandardCharsets.UTF_8));
							}
						}

						inputStream = conn.getInputStream();

						if (shouldGunzip) {
							inputStream = new GZIPInputStream(inputStream);
						}

						// While the assumption of UTF8 isn't always true; it *should* always be true.
						// Not in the sense that this will hold in most cases (although that as well),
						// but in the sense that any violation of this better have a good reason.
						result.complete(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
					} finally {
						try {
							if (inputStream != null) {
								inputStream.close();
							}
						} catch (Throwable t) {
							result.completeExceptionally(t);
						} finally {
							if (conn instanceof HttpURLConnection) {
								((HttpURLConnection) conn).disconnect();
							}
						}
					}
				} catch (IOException e) {
					result.completeExceptionally(e);
				}
			});

			return result;
		}

		public CompletableFuture<JsonObject> requestJson() {
			return requestJson(JsonObject.class);
		}

		public <T> CompletableFuture<T> requestJson(Class<? extends T> clazz) {
			return requestString().thenApply(str -> gson.fromJson(str, clazz));
		}
	}

	public static void patchHttpsRequest(HttpsURLConnection connection) {
		connection.setSSLSocketFactory(ctx.getSocketFactory());
	}

	public Request request() {
		return new Request();
	}

	public Request newHypixelApiRequest(String apiPath) {
		return newAnonymousHypixelApiRequest(apiPath)
			.queryArgument("key", NotEnoughUpdates.INSTANCE.config.apiData.apiKey);
	}

	public Request newAnonymousHypixelApiRequest(String apiPath) {
		return new Request()
			.url("https://api.hypixel.net/" + apiPath);
	}

	public Request newMoulberryRequest(String path) {
		return new Request()
			.url(getMyApiURL() + path);
	}

	private String getMyApiURL() {
		return String.format("https://%s/", NotEnoughUpdates.INSTANCE.config.apiData.moulberryCodesApi);
	}
}
