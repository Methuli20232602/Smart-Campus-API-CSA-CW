package com.smartcampus.resources;

import com.smartcampus.models.Room;
import com.smartcampus.repository.DataStore;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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

    /**
     * POST /api/v1/rooms
     * Enables the creation of new rooms.
     * 
     * @param newRoom The Room JSON payload sent by the client.
     * @return 201 Created if successful, 400 or 409 on error.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room newRoom) {
        if (newRoom == null || newRoom.getId() == null || newRoom.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Room ID must not be null or empty")
                           .build();
        }
        
        // Ensure room doesn't already exist to prevent overwrites
        if (dataStore.getRooms().containsKey(newRoom.getId())) {
            return Response.status(Response.Status.CONFLICT)
                           .entity("Room with ID " + newRoom.getId() + " already exists")
                           .build();
        }
        
        dataStore.getRooms().put(newRoom.getId(), newRoom);
        
        return Response.status(Response.Status.CREATED).entity(newRoom).build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Fetches detailed metadata for a specific room.
     * 
     * @param roomId The target room ID passed in the URL path.
     * @return 200 OK with the Room object, or 404 Not Found.
     */
    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRooms().get(roomId);
        
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Room with ID " + roomId + " not found")
                           .build();
        }
        
        return Response.ok(room).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Deletes a specific room if and only if it has no active sensors assigned to it.
     * 
     * @param roomId The room ID to delete
     * @return 204 No Content on success, 404 Not Found, or 409 Conflict if occupied
     */
    @DELETE
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRooms().get(roomId);
        
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Room with ID " + roomId + " not found")
                           .build();
        }
        
        // Business Logic Constraint: Cannot delete if it still has sensors assigned to it.
        // Returning HTTP 409 Conflict prevents data orphans.
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            return Response.status(Response.Status.CONFLICT)
                           .entity("Cannot delete room " + roomId + " because it is currently occupied by active hardware.")
                           .build();
        }
        
        dataStore.getRooms().remove(roomId);
        return Response.noContent().build();
    }
}
