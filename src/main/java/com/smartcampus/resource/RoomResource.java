package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.repository.RoomRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

/**
 * JAX-RS Resource class for managing Rooms.
 * Handles CRUD operations on the /api/v1/rooms path.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final RoomRepository roomRepo = RoomRepository.getInstance();

    /**
     * GET /api/v1/rooms
     * Returns a comprehensive list of all rooms.
     */
    @GET
    public Response getAllRooms() {
        List<Room> rooms = roomRepo.getAll();
        return Response.ok(rooms).build();
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room. Returns 201 Created with Location header.
     */
    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Bad Request\", \"message\": \"Room ID is required.\"}")
                    .build();
        }

        if (roomRepo.exists(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Conflict\", \"message\": \"A room with ID '" + room.getId()
                            + "' already exists.\"}")
                    .build();
        }

        roomRepo.add(room);
        return Response.created(URI.create("/api/v1/rooms/" + room.getId()))
                .entity(room)
                .build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Fetches detailed metadata for a specific room.
     */
    @GET
    @Path("{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = roomRepo.getById(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Not Found\", \"message\": \"Room with ID '" + roomId + "' was not found.\"}")
                    .build();
        }
        return Response.ok(room).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Deletes a room. Blocks deletion if the room still has sensors assigned.
     * This operation is idempotent: deleting a non-existent room returns 204.
     */
    @DELETE
    @Path("{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = roomRepo.getById(roomId);

        // Idempotent: if room doesn't exist, return 204 (already deleted / never
        // existed)
        if (room == null) {
            return Response.noContent().build();
        }

        // Business Logic Constraint: cannot delete a room with active sensors
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Room '" + roomId + "' cannot be deleted because it still has " +
                            room.getSensorIds().size() + " sensor(s) assigned to it. " +
                            "Please reassign or remove all sensors before decommissioning this room.");
        }

        roomRepo.remove(roomId);
        return Response.noContent().build();
    }
}
