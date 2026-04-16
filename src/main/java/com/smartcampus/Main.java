package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

/**
 * Main entry point for the Smart Campus API.
 * Bootstraps the embedded Grizzly HTTP server with Jersey JAX-RS.
 *
 * This implementation strictly adheres to the coursework requirements:
 * 1. Pure JAX-RS (Jakarta REST) using Jersey/Grizzly.
 * 2. No Spring Boot or external framework bloat.
 * 3. In-memory data management (No external database).
 */
public class Main {

    // The base URI where the server will listen
    public static final String BASE_URI = "http://localhost:8080/";

    public static void main(String[] args) throws IOException {
        // ResourceConfig is the Jersey-specific implementation of the JAX-RS Application class.
        // We tell it to scan our packages for @Path (Resources) and @Provider (Mappers/Filters).
        final ResourceConfig rc = new ResourceConfig()
                .packages("com.smartcampus.resources",
                          "com.smartcampus.exceptions",
                          "com.smartcampus.filters");

        // Create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);

        System.out.println("=================================================");
        System.out.println("   SMART CAMPUS JAX-RS API (Coursework v1.0)");
        System.out.println("=================================================");
        System.out.println("Server successfully started.");
        System.out.println("Base URI: " + BASE_URI);
        System.out.println("Endpoints: " + BASE_URI + " (API v1 context)");
        System.out.println("-------------------------------------------------");
        System.out.println("Press ENTER to stop the server...");
        
        System.in.read();

        // Graceful shutdown
        server.shutdownNow();
    }
}
