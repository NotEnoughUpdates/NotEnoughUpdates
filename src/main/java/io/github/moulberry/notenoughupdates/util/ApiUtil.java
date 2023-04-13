/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
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
import io.github.moulberry.notenoughupdates.events.ProfileDataLoadedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

public class ApiUtil {
	private static final Gson gson = new Gson();

	private static final Comparator<NameValuePair> nameValuePairComparator = Comparator
		.comparing(NameValuePair::getName)
		.thenComparing(NameValuePair::getValue);

	private static final ExecutorService executorService = Executors.newFixedThreadPool(3);
	private static String getUserAgent() {
		if (NotEnoughUpdates.INSTANCE.config.hidden.customUserAgent != null) {
			return NotEnoughUpdates.INSTANCE.config.hidden.customUserAgent;
		}
		return "NotEnoughUpdates/" + NotEnoughUpdates.VERSION;
	}
	private static SSLContext ctx;
	private final Map<String, CompletableFuture<Void>> updateTasks = new HashMap<>();
	private static boolean notifiedOfInvalidApiKey;

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

		String uuid = Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", "");
		updateTasks.put(playerUuid, newHypixelApiRequest("skyblock/profiles")
			.queryArgument("uuid", uuid)
			.requestJson()
			.handle((jsonObject, throwable) -> {
				new ProfileDataLoadedEvent(uuid, jsonObject).post();
				return null;
			}));

	}

	public static class Request {

		private final List<NameValuePair> queryArguments = new ArrayList<>();
		private String baseUrl = null;
		private boolean shouldGunzip = false;
		private Duration maxCacheAge = Duration.ofSeconds(500);
		private String method = "GET";
		private String postData = null;
		private String postContentType = null;

		public Request method(String method) {
			this.method = method;
			return this;
		}

		/**
		 * Specify a cache timeout of {@code null} to signify an uncacheable request.
		 * Non {@code GET} requests are always uncacheable.
		 */
		public Request maxCacheAge(Duration maxCacheAge) {
			this.maxCacheAge = maxCacheAge;
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

		private CompletableFuture<URL> buildUrl() {
			CompletableFuture<URL> fut = new CompletableFuture<>();
			try {
				fut.complete(new URIBuilder(baseUrl)
					.addParameters(queryArguments)
					.build()
					.toURL());
			} catch (URISyntaxException |
							 MalformedURLException |
							 NullPointerException e) { // Using CompletableFuture as an exception monad, isn't that exiting?
				fut.completeExceptionally(e);
			}
			return fut;
		}

		public String getBaseUrl() {
			return baseUrl;
		}

		private ApiCache.CacheKey getCacheKey() {
			if (!"GET".equals(method)) return null;
			queryArguments.sort(nameValuePairComparator);
			return new ApiCache.CacheKey(baseUrl, queryArguments, shouldGunzip);
		}

		private CompletableFuture<String> requestString0() {
			return buildUrl().thenApplyAsync(url -> {
				InputStream inputStream = null;
				URLConnection conn = null;
				try {
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
						conn.setRequestProperty("User-Agent", getUserAgent());
						if (this.postContentType != null) {
							conn.setRequestProperty("Content-Type", this.postContentType);
						}
						if (this.postData != null) {
							conn.setDoOutput(true);
							OutputStream os = conn.getOutputStream();
							try {
								os.write(this.postData.getBytes(StandardCharsets.UTF_8));
							} finally {
								os.close();
							}
						}

						inputStream = conn.getInputStream();

						if (shouldGunzip) {
							inputStream = new GZIPInputStream(inputStream);
						}

						// While the assumption of UTF8 isn't always true; it *should* always be true.
						// Not in the sense that this will hold in most cases (although that as well),
						// but in the sense that any violation of this better have a good reason.
						return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
					} finally {
						try {
							if (inputStream != null) {
								inputStream.close();
							}
						} finally {
							if (conn instanceof HttpURLConnection) {
								((HttpURLConnection) conn).disconnect();
							}
						}
					}
				} catch (IOException e) {
					if (conn instanceof HttpURLConnection) {
						try {
							int code = ((HttpURLConnection) conn).getResponseCode();
							if (code == 403 && baseUrl.startsWith("https://api.hypixel.net/")) {
								if (!notifiedOfInvalidApiKey) {
									NotificationHandler.displayNotification(Arrays.asList(
										"API request failed",
										"A Request failed because your API key is invalid/not present",
										"Please run " + EnumChatFormatting.BOLD + EnumChatFormatting.GOLD + "/api new " +
											EnumChatFormatting.RESET + "to fix this.",
										"If you don't do this, several API related features, such as the profile viewer, will not work.",
										"Press X on your keyboard to close this notification."
									), true, false);
									notifiedOfInvalidApiKey = true;
								}
								return "";
							}
						} catch (IOException ex) {
							throw new RuntimeException(ex);
						}
					}
					throw new RuntimeException(e); // We can rethrow, since supplyAsync catches exceptions.
				}
			}, executorService).handle((obj, t) -> {
				if (t != null) {
					System.err.println(ErrorUtil.printStackTraceWithoutApiKey(t));
				}
				return obj;
			});
		}

		public CompletableFuture<String> requestString() {
			return ApiCache.INSTANCE.cacheRequest(this, getCacheKey(), this::requestString0, maxCacheAge);
		}

		public CompletableFuture<JsonObject> requestJson() {
			return requestJson(JsonObject.class);
		}

		public <T> CompletableFuture<T> requestJson(Class<? extends T> clazz) {
			return requestString().thenApplyAsync(str -> gson.fromJson(str, clazz));
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
