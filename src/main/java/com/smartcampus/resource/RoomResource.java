package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.repository.RoomRepository;
import com.smartcampus.repository.SensorRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final RoomRepository roomRepo = RoomRepository.getInstance();
    private final SensorRepository sensorRepo = SensorRepository.getInstance();

    @GET
    public List<Room> getAllRooms() {
        return roomRepo.getAllRooms();
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        roomRepo.addRoom(room);
        java.net.URI uri = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(uri).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = roomRepo.getRoom(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found with ID: " + roomId);
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = roomRepo.getRoom(roomId);
        if (room == null) {
            // Idempotent: if it doesn't exist, return 204
            return Response.noContent().build();
        }

        // Check for active sensors
        List<Sensor> sensors = sensorRepo.getSensorsByRoom(roomId);
        if (!sensors.isEmpty()) {
            throw new RoomNotEmptyException(
                    "Cannot delete room " + roomId + ". It is currently occupied by active hardware.");
        }

        roomRepo.deleteRoom(roomId);
        return Response.noContent().build();
    }
}
