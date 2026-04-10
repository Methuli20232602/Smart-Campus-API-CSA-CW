package com.smartcampus.resources;

import com.smartcampus.models.Room;
import com.smartcampus.repository.DataStore;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Resource class for managing Rooms.
 * Mapped to /api/v1/rooms (ApplicationPath + @Path)
 */
@Path("/rooms")
public class SensorRoomResource {

    // Retrieve the centralized, thread-safe in-memory data store singleton
    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/rooms
     * Retrieves a comprehensive list of all rooms currently registered in the system.
     * 
     * @return 200 OK with a JSON array of Room objects
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRooms() {
        // Convert the values of the ConcurrentHashMap into a List for JAX-RS JSON serialization
        List<Room> roomsList = new ArrayList<>(dataStore.getRooms().values());
        
        return Response.ok(roomsList).build();
    }
}
