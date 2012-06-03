package org.i5y.browserid.verifier;

import java.io.IOException;
import java.net.URI;

import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.stream.JsonGenerator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class VerifierServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final Verifier verifier = new Verifier(Clock.current(),
			Certificates.newCache());

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		URI requestURI = URI.create(request.getRequestURL().toString());

		int requestPort = requestURI.getPort();

		String expectedAudience = requestURI.getScheme()
				+ "://"
				+ requestURI.getHost()
				+ (requestPort == 80 || requestPort <= 0 ? "" : ":"
						+ requestURI.getPort());

		String bundle = ((JsonObject) new JsonReader(request.getReader())
				.readObject()).getValue("assertion", JsonString.class)
				.getValue();

		String results = verifier.verify(bundle, expectedAudience);
		response.setContentType("application/json");
		new JsonGenerator(response.getWriter()).beginObject()
				.add("verified", results.length() > 0).add("identity", results)
				.endObject().close();
	}

	public static void main(String[] args) throws Exception {
		int port = 5000;
		Server server = new Server(port);

		ServletContextHandler context = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		context.setContextPath("/");

		context.addServlet(new ServletHolder(new VerifierServlet()),
				"/verifier/verifier");
		ServletHolder defaultServletHolder = new ServletHolder(
				new DefaultServlet());
		defaultServletHolder
				.setInitParameter("resourceBase", "src/main/webapp");
		context.addServlet(defaultServletHolder, "/");

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { context });
		server.setHandler(handlers);

		server.start();
		server.join();
	}
}
