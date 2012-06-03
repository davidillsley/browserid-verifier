package org.i5y.browserid.verifier;

public interface Certificate {
	public boolean verify(String alg, String message, String signature);
}
