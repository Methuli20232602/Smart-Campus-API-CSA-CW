package com.smartcampus.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Root Discovery endpoint serving at GET /api/v1
 * Provides HATEOAS-style API metadata and resource navigation links.
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiMetadata() {
        // Construct the root API JSON metadata object
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("api_name", "Smart Campus Core API");
        metadata.put("version", "v1.0.0");
        metadata.put("contact_email", "admin@smartcampus.westminster.ac.uk");
        metadata.put("documentation", "See README.md for complete JAX-RS implementation documentation.");
        
        // Expose a map of primary resource collections to facilitate discovery
        Map<String, String> collections = new HashMap<>();
        collections.put("rooms", "/api/v1/rooms");
        collections.put("sensors", "/api/v1/sensors");
        metadata.put("resources", collections);
        
        return Response.ok(metadata).build();
    }
}
