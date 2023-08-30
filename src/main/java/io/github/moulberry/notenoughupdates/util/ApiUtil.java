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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.events.ProfileDataLoadedEvent;
import io.github.moulberry.notenoughupdates.util.kotlin.KotlinTypeAdapterFactory;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class ApiUtil {
	private static final Gson gson = new GsonBuilder()
		.registerTypeAdapterFactory(KotlinTypeAdapterFactory.INSTANCE)
		.create();

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
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			kmf.init(letsEncryptStore, null);
			tmf.init(letsEncryptStore);
			ctx = SSLContext.getInstance("TLS");
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		} catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException |
						 IOException | CertificateException e) {
			System.out.println("Failed to load NEU keystore. A lot of API requests won't work");
			e.printStackTrace();
			ctx = null;
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
		private final Map<String, String> headers = new HashMap<>();
		private Map<String, List<String>> responseHeaders = new HashMap<>();
		private String postContentType = null;

		public Request method(String method) {
			this.method = method;
			return this;
		}

		public Request header(String key, String value) {
			this.headers.put(key, value);
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

		/**
		 * Note: This map may be empty on cache hits.
		 */
		public Map<String, List<String>> getResponseHeaders() {
			return responseHeaders;
		}

		private CompletableFuture<String> requestString0() {
			return buildUrl().thenApplyAsync(url -> {
				InputStream inputStream = null;
				int httpStatusCode = 200;
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
						for (Map.Entry<String, String> header : headers.entrySet()) {
							conn.setRequestProperty(header.getKey(), header.getValue());
						}
						if (this.postContentType != null) {
							conn.setRequestProperty("Content-Type", this.postContentType);
						}
						if (!shouldGunzip) {
							conn.setRequestProperty("Accept-Encoding", "gzip"); // Tell the server we can accept gzip
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

						if (conn instanceof HttpURLConnection) {
							HttpURLConnection httpConn = (HttpURLConnection) conn;
							httpStatusCode = httpConn.getResponseCode();
							if (httpStatusCode >= 400) {
								inputStream = httpConn.getErrorStream();
							}
						}
						if (inputStream == null)
							inputStream = conn.getInputStream();

						if (shouldGunzip || "gzip".equals(conn.getContentEncoding())) {
							inputStream = new GZIPInputStream(inputStream);
						}
						responseHeaders = conn.getHeaderFields().entrySet().stream()
																	.collect(Collectors.toMap(
																		it -> it.getKey() == null ? null : it.getKey().toLowerCase(Locale.ROOT),
																		it -> new ArrayList<>(it.getValue())
																	));

						// While the assumption of UTF8 isn't always true; it *should* always be true.
						// Not in the sense that this will hold in most cases (although that as well),
						// but in the sense that any violation of this better have a good reason.
						String response = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
						if (httpStatusCode >= 400) {
							throw new HttpStatusCodeException(url, httpStatusCode, response);
						}
						return response;
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
		if (ctx != null && connection != null)
			connection.setSSLSocketFactory(ctx.getSocketFactory());
	}

	public Request request() {
		return new Request();
	}

	@Deprecated
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
