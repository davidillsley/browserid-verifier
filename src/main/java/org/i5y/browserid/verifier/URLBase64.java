package org.i5y.browserid.verifier;

import org.bouncycastle.util.encoders.Base64;

public class URLBase64 {
	public static byte[] decodeURLBase64(String input) {
		String transformed = input.replace('-', '+').replace('_', '/');
		final String padding;
		switch (transformed.length() % 4) {
		case 0:
			padding = "";
			break;
		case 2:
			padding = "==";
			break;
		case 3:
			padding = "=";
			break;
		default:
			throw new IllegalArgumentException();
		}
		return Base64.decode(transformed + padding);
	}
}
