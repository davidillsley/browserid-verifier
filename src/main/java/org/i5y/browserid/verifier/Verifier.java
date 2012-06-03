package org.i5y.browserid.verifier;

import java.io.StringReader;
import java.security.Security;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.google.common.cache.LoadingCache;

public class Verifier {
	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	private final Clock clock;
	private final LoadingCache<String, Certificate> certificateCache;

	public Verifier(Clock clock, LoadingCache<String, Certificate> certificateCache) {
		this.clock = clock;
		this.certificateCache = certificateCache;
	}

	public String verify(String bundle, String audience) {
		System.out.println("bundle: "+bundle);
		System.out.println("audience: "+audience);
		int delimitedAt = bundle.indexOf("~");
		String part1 = bundle.substring(0, delimitedAt);
		String part2 = bundle.substring(delimitedAt + 1, bundle.length());

		String[] part1sections = part1.split("\\.");

		String part1bodyDecoded = new String(URLBase64.decodeURLBase64(part1sections[1]));

		JsonObject part1bodyObj = (JsonObject) new JsonReader(new StringReader(
				part1bodyDecoded)).readObject();

		String iss = part1bodyObj.getValue("iss", JsonString.class).getValue();
		long exp = part1bodyObj.getValue("exp", JsonNumber.class)
				.getLongValue();
		long iat = part1bodyObj.getValue("iat", JsonNumber.class)
				.getLongValue();
		String principal = part1bodyObj.getValue("principal", JsonObject.class)
				.getValue("email", JsonString.class).getValue();
		Certificate identityCert = Certificates.parse(part1bodyObj.getValue(
				"public-key", JsonObject.class));

		Certificate issuerCertificate = certificateCache.getUnchecked(iss);

		boolean part1Verified = issuerCertificate.verify(null, part1sections[0]
				+ "." + part1sections[1], part1sections[2]);

		String[] part2sections = part2.split("\\.");

		String header2 = part2sections[0];
		String header2decoded = new String(URLBase64.decodeURLBase64(part2sections[0]));
		String body2 = part2sections[1];
		String part2bodyDecoded = new String(URLBase64.decodeURLBase64(part2sections[1]));

		JsonObject part2bodyObj = (JsonObject) new JsonReader(new StringReader(
				part2bodyDecoded)).readObject();

		long part2exp = part2bodyObj.getValue("exp", JsonNumber.class)
				.getLongValue();
		String aud = part2bodyObj.getValue("aud", JsonString.class).getValue();

		String header2alg = ((JsonObject) new JsonReader(new StringReader(
				header2decoded)).readObject())
				.getValue("alg", JsonString.class).getValue();

		boolean part2Verified = identityCert.verify(header2alg, header2 + "."
				+ body2, part2sections[2]);

		long part1expms = exp;
		long iatms = iat;
		long part2expms = part2exp;
		long currentms = clock.millis();

		System.out.println("Current Time: " + currentms);
		System.out.println("Issued At: " + iatms);
		System.out.println("Part 1 Expiry: " + part1expms);
		System.out.println("Part 2 Expiry: " + part2expms);
		boolean timingValid = (iatms < currentms) && (currentms < part1expms)
				&& (currentms < part2expms);
		System.out.println("Timing Valid: " + timingValid);

		System.out.println("Expected Audience: " + audience);
		System.out.println("Assertion Audience: " + aud);
		boolean audienceValid = audience.equalsIgnoreCase(aud);
		System.out.println("Audience Valid: " + audienceValid);

		String expectedIssuer = principal.split("@")[1];
		System.out.println("Expected Issuer: " + expectedIssuer
				+ " (or browserid.org)");
		System.out.println("Actual Issuer: " + iss);
		boolean validIssuer = iss.equalsIgnoreCase(expectedIssuer)
				|| "browserid.org".equalsIgnoreCase(iss);
		System.out.println("Valid Issuer: " + validIssuer);

		System.out.println("Part 1 verified: " + part1Verified);
		System.out.println("Part 2 verified: " + part2Verified);

		boolean fullyVerified = timingValid && audienceValid && validIssuer
				&& part1Verified && part2Verified;

		System.out.println("Fully Verified: " + fullyVerified);

		return (fullyVerified ? principal : "");
	}
}
