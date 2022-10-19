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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class MoulSigner {
	private MoulSigner() {}

	static PublicKey publicKey;

	static {
		try (InputStream is = MoulSigner.class.getResourceAsStream("/moulberry.key")) {
			byte[] publicKeyBytes = IOUtils.toByteArray(is);
			X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyBytes);
			publicKey = KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec);
		} catch (IOException | NullPointerException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			NotEnoughUpdates.LOGGER.error("Cannot initialize MoulSigner", e);
		}
	}

	public static boolean verifySignature(byte[] data, byte[] signatureBytes) {
		if (Boolean.getBoolean("neu.noverifysignature")) return true;
		if (publicKey == null) {
			NotEnoughUpdates.LOGGER.warn("MoulSigner could not be initialized, will fail this request");
			return false;
		}
		try {
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initVerify(publicKey);
			signature.update(data);
			return signature.verify(signatureBytes);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			NotEnoughUpdates.LOGGER.error("Error while verifying signature. Considering this as invalid signature", e);
			return false;
		}
	}

	public static boolean verifySignature(File file) {
		try {
			return verifySignature(
				IOUtils.toByteArray(file.toURI()),
				IOUtils.toByteArray(new File(file.getParentFile(), file.getName() + ".asc").toURI())
			);
		} catch (IOException e) {
			NotEnoughUpdates.LOGGER.error("Ran into an IOException while verifying a signature", e);
			return false;
		}
	}
}
