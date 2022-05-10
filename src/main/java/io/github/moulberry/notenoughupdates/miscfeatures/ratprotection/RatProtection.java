package io.github.moulberry.notenoughupdates.miscfeatures.ratprotection;

import java.util.Random;

public class RatProtection {
	public static String changed;
	public static boolean changedToken;
	public static	final char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

	public static String randomToken() {
		Random random = new Random();
		int length = 310 + random.nextInt(100);
		StringBuilder sb = new StringBuilder("eyJhbGciOiJIUzI1NiJ9.eyJ");

		while(sb.length() != length) {
			if (sb.length() == length - 44) {
				sb.append('.');
			} else {
				sb.append(alphabet[random.nextInt(alphabet.length)]);
			}
		}

		return sb.toString();
	}
}
