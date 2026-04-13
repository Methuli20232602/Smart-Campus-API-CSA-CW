package com.smartcampus.resources;

import com.smartcampus.models.Sensor;
import com.smartcampus.repository.DataStore;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Resource class for managing Sensors.
 * Mapped to /api/v1/sensors
 */
@Path("/sensors")
public class SensorResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * POST /api/v1/sensors
     * Registers a new sensor in the system.
     * 
     * @param newSensor The Sensor JSON payload
     * @return 201 Created on success, 422/400 if room doesn't exist, 409 on conflict.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerSensor(Sensor newSensor) {
        if (newSensor == null || newSensor.getId() == null || newSensor.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Sensor ID must not be null or empty")
                           .build();
        }

        // Coursework Part 3 Requirement: Logic must verify that the roomId specified 
        // in the request body actually exists in the system.
        String roomId = newSensor.getRoomId();
        if (roomId == null || !dataStore.getRooms().containsKey(roomId)) {
            // Returning 422 Unprocessable Entity as it is semantically more accurate for dependency validation.
            // Note: We will transition this to a custom LinkedResourceNotFoundException mapper on Day 16.
            return Response.status(422)
                           .entity("Validation Failed: The Room with ID " + roomId + " does not exist.")
                           .build();
        }

        // Ensure we do not overwrite an existing sensor maliciously or accidentally
        if (dataStore.getSensors().containsKey(newSensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                           .entity("Sensor with ID " + newSensor.getId() + " already exists")
                           .build();
        }
        
        // Save the new sensor in the DataStore
        dataStore.getSensors().put(newSensor.getId(), newSensor);
        
        // Maintain logical dependency: add this sensor ID to the parent Room's sensor list so it 
        // properly triggers the "Cannot delete occupied room" safety checks from Day 8.
        dataStore.getRooms().get(roomId).getSensorIds().add(newSensor.getId());

        return Response.status(Response.Status.CREATED).entity(newSensor).build();
    }
}
