package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.repository.RoomRepository;
import com.smartcampus.repository.SensorRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

/**
 * JAX-RS Resource class for managing Sensors.
 * Handles CRUD operations on the /api/v1/sensors path.
 * Also acts as a sub-resource locator for sensor readings.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final SensorRepository sensorRepo = SensorRepository.getInstance();
    private final RoomRepository roomRepo = RoomRepository.getInstance();

    /**
     * GET /api/v1/sensors
     * Returns all sensors. Supports optional filtering by type via query parameter.
     * Example: GET /api/v1/sensors?type=CO2
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors;

        if (type != null && !type.trim().isEmpty()) {
            // Filtered retrieval by sensor type
            sensors = sensorRepo.getByType(type);
        } else {
            // Return all sensors
            sensors = sensorRepo.getAll();
        }

        return Response.ok(sensors).build();
    }

    /**
     * POST /api/v1/sensors
     * Registers a new sensor. Validates that the roomId exists in the system.
     * Also links the sensor to the room by adding its ID to the room's sensorIds
     * list.
     */
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Bad Request\", \"message\": \"Sensor ID is required.\"}")
                    .build();
        }

        if (sensorRepo.exists(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Conflict\", \"message\": \"A sensor with ID '" + sensor.getId()
                            + "' already exists.\"}")
                    .build();
        }

        // Validate that the referenced room exists
        if (sensor.getRoomId() == null || !roomRepo.exists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "The room with ID '" + sensor.getRoomId() + "' does not exist. " +
                            "Cannot register a sensor for a non-existent room.");
        }

        // Register the sensor
        sensorRepo.add(sensor);

        // Link the sensor to the room (update room's sensorIds list)
        Room room = roomRepo.getById(sensor.getRoomId());
        room.getSensorIds().add(sensor.getId());

        return Response.created(URI.create("/api/v1/sensors/" + sensor.getId()))
                .entity(sensor)
                .build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Fetches details for a specific sensor.
     */
    @GET
    @Path("{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensorRepo.getById(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Not Found\", \"message\": \"Sensor with ID '" + sensorId
                            + "' was not found.\"}")
                    .build();
        }
        return Response.ok(sensor).build();
    }

    /**
     * Sub-Resource Locator for Sensor Readings.
     * Delegates handling of /api/v1/sensors/{sensorId}/readings to
     * SensorReadingResource.
     */
    @Path("{sensorId}/readings")
    public SensorReadingResource getReadingsSubResource(@PathParam("sensorId") String sensorId) {
        // Verify the sensor exists before delegating
        Sensor sensor = sensorRepo.getById(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }
        return new SensorReadingResource(sensorId);
    }
}
