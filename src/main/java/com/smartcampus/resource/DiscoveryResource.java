package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root "Discovery" endpoint for the Smart Campus API.
 * Provides API metadata, version info, and links to primary resource
 * collections (HATEOAS).
 */
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response getApiInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", "Smart Campus Sensor & Room Management API");
        info.put("version", "v1");
        info.put("description",
                "A RESTful API for managing rooms, sensors, and sensor readings across the university campus.");
        info.put("contact", "admin@smartcampus.university.edu");

        // Hypermedia links to primary resource collections
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        info.put("resources", resources);

        return Response.ok(info).build();
    }
}
