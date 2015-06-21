package com.aruld.jersey.streaming;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

/**
 * Media server
 *
 * @author Arul Dhesiaseelan (aruld@acm.org)
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
        ResourceConfig rc = new ResourceConfig();
        rc.registerInstances(new MediaResource(), new LoggingFilter());
        return JettyHttpContainerFactory.createServer(BASE_URI, rc);
    }

    public static void main(String[] args) throws Exception {
        Server httpServer = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                        + "%sapplication.wadl\nHit enter to stop it...",
                BASE_URI, BASE_URI));
        System.in.read();
        httpServer.stop();
    }
}
