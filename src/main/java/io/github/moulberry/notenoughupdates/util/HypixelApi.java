package io.github.moulberry.notenoughupdates.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

public class HypixelApi {
	private final Gson gson = new Gson();
	private final ExecutorService es = Executors.newFixedThreadPool(3);

	public CompletableFuture<JsonObject> getHypixelApiAsync(String apiKey, String method, HashMap<String, String> args) {
		return getApiAsync(generateApiUrl(apiKey, method, args));
	}

	public void getHypixelApiAsync(
		String apiKey,
		String method,
		HashMap<String, String> args,
		Consumer<JsonObject> consumer
	) {
		getHypixelApiAsync(apiKey, method, args, consumer, () -> {
		});
	}

	public void getHypixelApiAsync(
		String apiKey,
		String method,
		HashMap<String, String> args,
		Consumer<JsonObject> consumer,
		Runnable error
	) {
		getApiAsync(generateApiUrl(apiKey, method, args), consumer, error);
	}

	private String getMyApiURL() {
		return String.format("https://%s/", NotEnoughUpdates.INSTANCE.config.apiKey.moulberryCodesApi);
	}

	public CompletableFuture<JsonObject> getApiAsync(String urlS) {
		CompletableFuture<JsonObject> result = new CompletableFuture<>();
		es.submit(() -> {
			try {
				result.complete(getApiSync(urlS));
			} catch (Exception e) {
				result.completeExceptionally(e);
			}
		});
		return result;
	}

	public void getApiAsync(String urlS, Consumer<JsonObject> consumer, Runnable error) {
		es.submit(() -> {
			try {
				consumer.accept(getApiSync(urlS));
			} catch (Exception e) {
				error.run();
			}
		});
	}

	public void getMyApiAsync(String urlS, Consumer<JsonObject> consumer, Runnable error) {
		es.submit(() -> {
			try {
				consumer.accept(getApiSync(getMyApiURL() + urlS));
			} catch (Exception e) {
				if (NotEnoughUpdates.INSTANCE.config.hidden.dev) {
					e.printStackTrace();
				}
				error.run();
			}
		});
	}

	public void getMyApiGZIPAsync(String urlS, Consumer<JsonObject> consumer, Runnable error) {
		es.submit(() -> {
			try {
				consumer.accept(getApiGZIPSync(getMyApiURL() + urlS));
			} catch (Exception e) {
				error.run();
			}
		});
	}

	public void getApiGZIPAsync(String urlS, Consumer<JsonObject> consumer, Runnable error) {
		es.submit(() -> {
			try {
				consumer.accept(getApiGZIPSync(urlS));
			} catch (Exception e) {
				error.run();
			}
		});
	}

	public JsonObject getApiSync(String urlS) throws IOException {
		URL url = new URL(urlS);
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);

		String response = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);

		JsonObject json = gson.fromJson(response, JsonObject.class);
		if (json == null) throw new ConnectException("Invalid JSON");
		return json;
	}

	public JsonObject getApiGZIPSync(String urlS) throws IOException {
		URL url = new URL(urlS);
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);

		String response = IOUtils.toString(new GZIPInputStream(connection.getInputStream()), StandardCharsets.UTF_8);

		JsonObject json = gson.fromJson(response, JsonObject.class);
		return json;
	}

	public String generateApiUrl(String apiKey, String method, HashMap<String, String> args) {
		if (apiKey != null)
			args.put("key", apiKey.trim());
		StringBuilder url = new StringBuilder("https://api.hypixel.net/" + method);
		boolean first = true;
		for (Map.Entry<String, String> entry : args.entrySet()) {
			if (first) {
				url.append("?");
				first = false;
			} else {
				url.append("&");
			}
			try {
				url.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name())).append("=")
					 .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()));
			} catch (UnsupportedEncodingException e) {
			}
		}
		return url.toString();
	}
}
