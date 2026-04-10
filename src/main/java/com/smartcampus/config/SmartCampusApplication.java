package com.smartcampus.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Bootstraps the JAX-RS application and sets the versioned API base path.
 * In JAX-RS 3.x (Jersey 3.x), the standard package is jakarta.ws.rs.* 
 * instead of the older javax.ws.rs.* package.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    // By default, JAX-RS will automatically scan the project packages for @Path and @Provider annotations.
    // So we don't necessarily have to register the classes manually here.
}
