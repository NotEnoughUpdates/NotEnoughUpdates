package io.github.moulberry.notenoughupdates.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
	private static final int FAILS_BEFORE_SWITCH = 3;
	private final Gson gson = new Gson();
	private final ExecutorService es = Executors.newFixedThreadPool(3);
	private final String[] myApiURLs = {"https://moulberry.codes/"};
	//, "http://moulberry.codes/", "http://51.79.51.21/"};//, "http://51.75.78.252/" };
	private final Integer[] myApiSuccesses = {0, 0, 0, 0};
	private int currentUrl = 0;
	private long lastPrimaryUrl = 0;

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
		if (currentUrl == 0) {
			lastPrimaryUrl = System.currentTimeMillis();
		} else if (System.currentTimeMillis() - lastPrimaryUrl > 1000 * 60 * 30) { //Try switch back to main url after 30m
			currentUrl = 0;
		}

		myApiSuccesses[currentUrl] = Math.min(FAILS_BEFORE_SWITCH, myApiSuccesses[currentUrl] + 1);
		return myApiURLs[currentUrl];
	}

	private void myApiError(int index) {
		myApiSuccesses[index] = myApiSuccesses[index] - 2;

		if (myApiSuccesses[index] < 0) {
			myApiSuccesses[index] = 0;

			if (index == currentUrl) {
				currentUrl++;
				if (currentUrl >= myApiURLs.length) {
					currentUrl = 0;
				}
			}
		}
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
			int current = currentUrl;
			try {
				consumer.accept(getApiSync(getMyApiURL() + urlS));
			} catch (Exception e) {
				if (NotEnoughUpdates.INSTANCE.config.hidden.dev) {
					e.printStackTrace();
				}
				myApiError(current);
				error.run();
			}
		});
	}

	public void getMyApiGZIPAsync(String urlS, Consumer<JsonObject> consumer, Runnable error) {
		es.submit(() -> {
			int current = currentUrl;
			try {
				consumer.accept(getApiGZIPSync(getMyApiURL() + urlS));
			} catch (Exception e) {
				myApiError(current);
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
		makeCompatible(connection);
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
		makeCompatible(connection);
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


	public void makeCompatible(URLConnection connection) {
		if (!(connection instanceof HttpsURLConnection)) {
			// we don't need to do anything to make non-https requests compatible
			return;
		}

		if (isJavaValid()) {
			// we don't need to do anything to make requests compatible if we have a recent enough java version
			return;
		}

		// try to initialise an SSLSocketFactory that doesn't validate certificates
		SSLSocketFactory trustingFactory = getNoCertificateVerificationFactory();
		if (trustingFactory == null) {
			return;
		}

		// set the SSLSocketFactory on the connection
		((HttpsURLConnection) connection).setSSLSocketFactory(trustingFactory);
	}

	/**
	 * @return whether the current Java version is higher than 8u141, the version that supports Let's Encrypt certificates.
	 */
	public boolean isJavaValid() {
		String[] splitJavaVersion;
		try {
			splitJavaVersion = System.getProperty("java.version").split("_");
		} catch (SecurityException | IllegalArgumentException | NullPointerException e) {
			// either we aren't allowed to access the version, or it doesn't exist.
			return true; // either way, we can't check it, so assume it's new enough
		}
		if (splitJavaVersion.length < 2 || !splitJavaVersion[0].contains("1.8")) {
			// some versions of java don't have a minor version in this property, also this bug only affects Java 8 (1.8)
			return true;
		}
		try {
			int minorVersion = Integer.parseInt(splitJavaVersion[1]);
			return minorVersion >= 141;
		} catch (NumberFormatException e) {
			// couldn't parse the minor version, so to be on the safe side, we'll assume it's new enough
			return true;
		}
	}

	public SSLSocketFactory getNoCertificateVerificationFactory() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[]{
			new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
				}
				public void checkServerTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
				}
			}
		};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			return sc.getSocketFactory();
		} catch (Exception ignored) {
		}
		return null;
	}
}
