package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for the Smart Campus API.
 * Starts an embedded Grizzly HTTP server with Jersey.
 */
public class Main {

    // Base URI the Grizzly HTTP server will listen on
    // The /api/v1 path prefix is included here to match the @ApplicationPath
    // convention
    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * Creates and configures the Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // Scan the com.smartcampus package for JAX-RS resources, exception mappers, and
        // filters
        final ResourceConfig config = new ResourceConfig()
                .packages("com.smartcampus");

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();

        LOGGER.log(Level.INFO, "====================================================");
        LOGGER.log(Level.INFO, "Smart Campus API is running!");
        LOGGER.log(Level.INFO, "API Base URL: {0}", BASE_URI);
        LOGGER.log(Level.INFO, "Discovery:    {0}", BASE_URI);
        LOGGER.log(Level.INFO, "Rooms:        {0}rooms", BASE_URI);
        LOGGER.log(Level.INFO, "Sensors:      {0}sensors", BASE_URI);
        LOGGER.log(Level.INFO, "====================================================");
        LOGGER.log(Level.INFO, "Press Enter to stop the server...");

        // Wait for user input to stop the server
        System.in.read();
        server.shutdownNow();
        LOGGER.log(Level.INFO, "Server stopped.");
    }
}
