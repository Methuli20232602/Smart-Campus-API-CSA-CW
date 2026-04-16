package com.smartcampus.resources;

import com.smartcampus.exceptions.EntityConflictException;
import com.smartcampus.exceptions.UnprocessableEntityException;
import com.smartcampus.models.Sensor;
import com.smartcampus.repository.DataStore;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            throw new UnprocessableEntityException("Validation Failed: The Room with ID " + roomId + " does not exist.");
        }

        // Ensure we do not overwrite an existing sensor maliciously or accidentally
        if (dataStore.getSensors().containsKey(newSensor.getId())) {
            throw new EntityConflictException("Sensor with ID " + newSensor.getId() + " already exists");
        }
        
        // Save the new sensor in the DataStore
        dataStore.getSensors().put(newSensor.getId(), newSensor);
        
        // Maintain logical dependency: add this sensor ID to the parent Room's sensor list so it 
        // properly triggers the "Cannot delete occupied room" safety checks from Day 8.
        dataStore.getRooms().get(roomId).getSensorIds().add(newSensor.getId());

        return Response.status(Response.Status.CREATED).entity(newSensor).build();
    }

    /**
     * GET /api/v1/sensors
     * Retrieves sensors, supporting optional filtering by type constraint.
     * Example: /api/v1/sensors?type=CO2
     * 
     * @param type Optional query parameter to filter the result set
     * @return 200 OK with a JSON array of matching Sensor objects
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSensors(@QueryParam("type") String type) {
        // Retrieve all existing sensors to a distinct array
        List<Sensor> allSensors = new ArrayList<>(dataStore.getSensors().values());
        
        // If type is defined in the URL, filter the list down
        if (type != null && !type.trim().isEmpty()) {
            List<Sensor> filteredSensors = allSensors.stream()
                .filter(sensor -> type.equalsIgnoreCase(sensor.getType()))
                .collect(Collectors.toList());
            return Response.ok(filteredSensors).build();
        }
        
        // Return full collection if no filter parameter was requested
        return Response.ok(allSensors).build();
    }

    /**
     * Sub-Resource Locator Pattern for Sensor Readings.
     * Delegates requests from /api/v1/sensors/{sensorId}/readings directly to the SensorReadingResource.
     * Note the omission of any HTTP method annotations (like @GET).
     * 
     * @param sensorId The parent's target ID
     * @return A scoped instance of SensorReadingResource to handle the sub-paths
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
