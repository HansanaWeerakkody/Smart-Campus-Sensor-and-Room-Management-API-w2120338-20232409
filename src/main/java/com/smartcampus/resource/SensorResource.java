package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.repository.RoomRepository;
import com.smartcampus.repository.SensorRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final SensorRepository sensorRepo = SensorRepository.getInstance();
    private final RoomRepository roomRepo = RoomRepository.getInstance();

    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        if (type != null && !type.isEmpty()) {
            return sensorRepo.getSensorsByType(type);
        }
        return sensorRepo.getAllSensors();
    }

    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        // Verify room exists
        if (!roomRepo.exists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "Cannot register sensor. Room ID " + sensor.getRoomId() + " does not exist.");
        }

        sensorRepo.addSensor(sensor);

        // Also update the room's sensor list
        roomRepo.getRoom(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        java.net.URI uri = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(uri).entity(sensor).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensorRepo.getSensor(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor not found with ID: " + sensorId);
        }
        return Response.ok(sensor).build();
    }

    /**
     * Sub-resource locator for readings.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensorRepo.getSensor(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor not found with ID: " + sensorId);
        }
        return new SensorReadingResource(sensorId);
    }
}
