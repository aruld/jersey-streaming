package com.aruld.jersey.streaming;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Media server
 *
 * @author Arul Dhesiaseelan (arul@httpmine.org)
 */
public class App {

    private static int getPort(int defaultPort) {
        String port = System.getProperty("jersey.test.port");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
            }
        }
        return defaultPort;
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/").port(getPort(9998)).build();
    }

    public static final URI BASE_URI = getBaseURI();

    protected static Server startServer() throws IOException {
        System.out.println("Starting jetty...");
        ResourceConfig config = new ResourceConfig(MediaResource.class);
        config.register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), Level.INFO, LoggingFeature.Verbosity.PAYLOAD_TEXT, Integer.MAX_VALUE));
        return JettyHttpContainerFactory.createServer(BASE_URI, config);
    }

    public static void main(String[] args) throws Exception {
        Server httpServer = startServer();
        System.out.println(String.format("Jersey app started with WADL available at %sapplication.wadl\nHit enter to stop it...", BASE_URI, BASE_URI));
        System.in.read();
        httpServer.stop();
    }
}
