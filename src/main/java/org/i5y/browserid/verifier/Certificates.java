package org.i5y.browserid.verifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERSequence;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

public class Certificates {
	public static Certificate parse(JsonObject object) {
		if ("DS".equals(object.getValue("algorithm", JsonString.class)
				.getValue())) {
			return new DSACertificate(object.getValue("y", JsonString.class)
					.getValue(), object.getValue("p", JsonString.class)
					.getValue(), object.getValue("q", JsonString.class)
					.getValue(), object.getValue("g", JsonString.class)
					.getValue());
		} else {
			return new RSACertificate(object.getValue("n", JsonString.class)
					.getValue(), object.getValue("e", JsonString.class)
					.getValue());
		}
	}

	public static LoadingCache<String, Certificate> newCache() {
		return CacheBuilder.newBuilder().expireAfterWrite(24, TimeUnit.HOURS)
				.build(new CacheLoader<String, Certificate>() {
					@Override
					public Certificate load(String issuer) throws Exception {
						try {
							URL issuerCertURL = new URL("https://" + issuer
									+ "/.well-known/browserid");
							HttpURLConnection httpUrlConnection = (HttpURLConnection) issuerCertURL
									.openConnection();

							InputStream is = httpUrlConnection.getInputStream();

							JsonObject certificate = (JsonObject) new JsonReader(
									new InputStreamReader(is)).readObject();
							return Certificates.parse(certificate.getValue(
									"public-key", JsonObject.class));
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				});
	}

	private static class RSACertificate implements Certificate {
		private final BigInteger n;
		private final BigInteger e;

		RSACertificate(String nString, String eString) {
			n = new BigInteger(nString, 10);
			e = new BigInteger(eString, 10);
		}

		@Override
		public boolean verify(String alg, String message, String signature) {
			try {
				RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
				Signature sig = Signature.getInstance("SHA256withRSA");

				PublicKey pk = KeyFactory.getInstance("RSA").generatePublic(
						publicKeySpec);

				sig.initVerify(pk);
				sig.update(message.getBytes());

				byte[] sigBytes = URLBase64.decodeURLBase64(signature);
				return sig.verify(sigBytes);
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
	}

	private static class DSACertificate implements Certificate {
		private static final Map<String, String> algs = ImmutableMap
				.<String, String> builder().put("DS128", "SHA1withDSA")
				.put("DS256", "SHA256withDSA").build();

		final BigInteger y;
		final BigInteger p;
		final BigInteger q;
		final BigInteger g;

		DSACertificate(String yString, String pString, String qString,
				String gString) {
			y = new BigInteger(yString, 16);
			p = new BigInteger(pString, 16);
			q = new BigInteger(qString, 16);
			g = new BigInteger(gString, 16);
		}

		@Override
		public boolean verify(String alg, String message, String signature) {
			String fullSignatureHex = new BigInteger(
					URLBase64.decodeURLBase64("AAAA" + signature)).toString(16);

			String rStringHex = fullSignatureHex.substring(0,
					fullSignatureHex.length() / 2);
			String sStringHex = fullSignatureHex.substring(
					fullSignatureHex.length() / 2, fullSignatureHex.length());

			BigInteger r = new BigInteger(rStringHex, 16);
			BigInteger s = new BigInteger(sStringHex, 16);

			DERSequence derSignature = new DERSequence(new DERInteger[] {
					new DERInteger(r), new DERInteger(s) });
			try {
				Signature sig = Signature.getInstance(algs.get(alg), "BC");

				DSAPublicKeySpec spec = new DSAPublicKeySpec(y, p, q, g);

				PublicKey pk = KeyFactory.getInstance("DSA").generatePublic(
						spec);

				sig.initVerify(pk);
				sig.update(message.getBytes());
				return sig.verify(derSignature.getDEREncoded());
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
	}
}
