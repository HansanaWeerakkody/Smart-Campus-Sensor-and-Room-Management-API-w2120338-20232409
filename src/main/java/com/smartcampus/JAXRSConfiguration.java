package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Configures JAX-RS for the application.
 * Points to the versioned root /api/v1 as per spec.
 */
@ApplicationPath("/api/v1")
public class JAXRSConfiguration extends Application {
}
