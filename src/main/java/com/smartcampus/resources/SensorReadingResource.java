package com.smartcampus.resources;

import com.smartcampus.repository.DataStore;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.smartcampus.models.SensorReading;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
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

    /**
     * GET /
     * Fetches the historical log of readings for this specific sensor.
     * 
     * @return 200 OK with a JSON array of SensorReading objects
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensorReadings() {
        // Ensure the parent sensor actually exists
        if (!dataStore.getSensors().containsKey(sensorId)) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Sensor with ID " + sensorId + " not found.")
                           .build();
        }

        // Return the list of readings or an empty list if none exist
        List<SensorReading> history = dataStore.getSensorReadings().getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(history).build();
    }

    /**
     * POST /
     * Appends a new reading for this specific sensor context.
     * 
     * @param reading The new SensorReading payload
     * @return 201 Created with the processed reading object
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSensorReading(SensorReading reading) {
        // Ensure the sensor actually exists to enforce logical integrity
        if (!dataStore.getSensors().containsKey(sensorId)) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Sensor with ID " + sensorId + " not found.")
                           .build();
        }

        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Reading payload cannot be empty.")
                           .build();
        }

        // Auto-generate ID or Timestamp if omitted by client to guarantee consistency
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() <= 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Add to history using a thread-safe list to prevent race conditions across concurrent JAX-RS threads
        dataStore.getSensorReadings()
                 .computeIfAbsent(sensorId, k -> new CopyOnWriteArrayList<>())
                 .add(reading);

        // Programmatic Side Effect: Dynamically update the parent sensor to reflect this newest reading value.
        // This ensures data consistency without needing database triggers.
        com.smartcampus.models.Sensor parentSensor = dataStore.getSensors().get(sensorId);
        if (parentSensor != null) {
            parentSensor.setCurrentValue(reading.getValue());
        }
        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
