package com.smartcampus.resources;

import com.smartcampus.repository.DataStore;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Sub-resource target for managing Sensor Readings.
 * Notice: This class does NOT have a class-level @Path annotation. 
 * It is dynamically instantiated and routed here by the parent SensorResource.
 */
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore dataStore = DataStore.getInstance();

    /**
     * Constructor injection provided by the parent Sub-Resource Locator.
     * @param sensorId The scoped sensor context
     */
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET and POST endpoint implementations will be added in Day 14.
}
