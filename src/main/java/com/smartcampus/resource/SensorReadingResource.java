package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.repository.SensorReadingRepository;
import com.smartcampus.repository.SensorRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Sub-resource class for managing Sensor Readings.
 * Instantiated by the sub-resource locator in SensorResource.
 * Handles GET and POST on /api/v1/sensors/{sensorId}/readings.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final SensorReadingRepository readingRepo = SensorReadingRepository.getInstance();
    private final SensorRepository sensorRepo = SensorRepository.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns the full history of readings for this sensor.
     */
    @GET
    public Response getReadings() {
        List<SensorReading> readings = readingRepo.getReadings(sensorId);
        return Response.ok(readings).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Appends a new reading for this sensor.
     * Auto-generates a UUID and timestamp.
     * Side effect: updates the parent sensor's currentValue field.
     * Throws SensorUnavailableException (403) if sensor status is "MAINTENANCE".
     */
    @POST
    public Response addReading(SensorReading reading) {
        // Check if the sensor is in MAINTENANCE status
        Sensor sensor = sensorRepo.getById(sensorId);
        if (sensor != null && "MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is currently in MAINTENANCE status and cannot accept new readings. " +
                            "Please wait until the sensor is back online.");
        }

        // Auto-generate ID and timestamp
        reading.setId(UUID.randomUUID().toString());
        reading.setTimestamp(System.currentTimeMillis());

        // Store the reading
        readingRepo.addReading(sensorId, reading);

        // Side Effect: update the parent sensor's currentValue
        if (sensor != null) {
            sensor.setCurrentValue(reading.getValue());
        }

        return Response.created(URI.create("/api/v1/sensors/" + sensorId + "/readings/" + reading.getId()))
                .entity(reading)
                .build();
    }
}
