package org.i5y.browserid.verifier;

import java.io.InputStreamReader;

import javax.json.JsonObject;
import javax.json.JsonReader;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class VerifierTest {
	@Test
	public void testSecondary() {
		Clock clock = Clock.fixed(1338742471370l);
		LoadingCache<String, Certificate> testCache = CacheBuilder.newBuilder()
				.build(new CacheLoader<String, Certificate>() {
					@Override
					public Certificate load(String arg0) throws Exception {
						if ("browserid.org".equals(arg0)) {
							JsonObject doc = (JsonObject) new JsonReader(
									new InputStreamReader(getClass()
											.getResourceAsStream(
													"browserid.org.json")))
									.readObject();
							return Certificates.parse(doc.getValue(
									"public-key", JsonObject.class));
						} else {
							throw new RuntimeException();
						}
					}
				});
		Verifier verifier = new Verifier(clock, testCache);
		Assert.assertEquals(
				"david@illsley.org",
				verifier.verify(
						"eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJicm93c2VyaWQub3JnIiwiZXhwIjoxMzM4ODI3NDI2MDAxLCJpYXQiOjEzMzg3NDEwMjYwMDEsInB1YmxpYy1rZXkiOnsiYWxnb3JpdGhtIjoiRFMiLCJ5IjoiYTNjYWU5OGVmM2EwYjhhYWZhNzM3YjQwYzRkNTQxOTQ3MDljOGMzMGYwMjczYWQ4ZmY1Y2IwYjFiYWVjMTMzNzBjMDY4NjBlNzY4NmMxZjRlNGRmNjhiZWZiMDdhY2U3ODE2MzUxZjc0NTA2ZTU2YWZmMzgxYWI3MDUxYzU5OTYxNTAxYWY2NWVjODUxMGExNGY5NTE4MjY3NjdjMjgwYWIzZmFlNTA5Zjg1NTFlNWYxMGI2NWI0M2U1NWJlMTM0NzU5ODI4MjQ5MTkyMjZjMTU5NTZiODcxNTJkZmYxZTAzMjMwZDExMjBhNjc5MTMxYmQzZmJiY2JjNjg1ZTE3ZiIsInAiOiJmZjYwMDQ4M2RiNmFiZmM1YjQ1ZWFiNzg1OTRiMzUzM2Q1NTBkOWYxYmYyYTk5MmE3YThkYWE2ZGMzNGY4MDQ1YWQ0ZTZlMGM0MjlkMzM0ZWVlYWFlZmQ3ZTIzZDQ4MTBiZTAwZTRjYzE0OTJjYmEzMjViYTgxZmYyZDVhNWIzMDVhOGQxN2ViM2JmNGEwNmEzNDlkMzkyZTAwZDMyOTc0NGE1MTc5MzgwMzQ0ZTgyYTE4YzQ3OTMzNDM4Zjg5MWUyMmFlZWY4MTJkNjljOGY3NWUzMjZjYjcwZWEwMDBjM2Y3NzZkZmRiZDYwNDYzOGMyZWY3MTdmYzI2ZDAyZTE3IiwicSI6ImUyMWUwNGY5MTFkMWVkNzk5MTAwOGVjYWFiM2JmNzc1OTg0MzA5YzMiLCJnIjoiYzUyYTRhMGZmM2I3ZTYxZmRmMTg2N2NlODQxMzgzNjlhNjE1NGY0YWZhOTI5NjZlM2M4MjdlMjVjZmE2Y2Y1MDhiOTBlNWRlNDE5ZTEzMzdlMDdhMmU5ZTJhM2NkNWRlYTcwNGQxNzVmOGViZjZhZjM5N2Q2OWUxMTBiOTZhZmIxN2M3YTAzMjU5MzI5ZTQ4MjliMGQwM2JiYzc4OTZiMTViNGFkZTUzZTEzMDg1OGNjMzRkOTYyNjlhYTg5MDQxZjQwOTEzNmM3MjQyYTM4ODk1YzlkNWJjY2FkNGYzODlhZjFkN2E0YmQxMzk4YmQwNzJkZmZhODk2MjMzMzk3YSJ9LCJwcmluY2lwYWwiOnsiZW1haWwiOiJkYXZpZEBpbGxzbGV5Lm9yZyJ9fQ.EaOZIuiXkF9_hAJ5hOD_v3gxSzy7aTenZpcAdVXZ-IwH3e9GzML0ZAljaD4K_yukJxtM-kN0GFo37DN1xprPiB3JHnQ5bi2Zv4TcE-KXiO7L_IGrf7R0OoMtd3E44oNOnx2icBDj4FAUUboytSJD2TA7R0EK_iE_zsaVmnk-qcK9lb95TFJXPBq10vPOgeP4OeqMvFZ5NFAlq2qCmLXGxrftzJXjMaHu_YmjkrdwFs7AF-B1B1Dw7YBYsvdLhUqz_R_Ua54a2ki1IvmCIYVSEIvGXK0QOGuqYYFPsKIUuVOf6r4_CLv1PdQr3s8Y5BoKmNbNW03rHVQlCcNAURCrIQ~eyJhbGciOiJEUzEyOCJ9.eyJleHAiOjEzMzg3NDI1ODQwMzQsImF1ZCI6Imh0dHA6Ly9sb2NhbGhvc3Q6NTAwMCJ9.I-xMWvUKaZ-Xa2oNanpMYnK343AlSfpjsXsq1iCh0LDkBxwOXvedeg",
						"http://localhost:5000"));
	}
}
