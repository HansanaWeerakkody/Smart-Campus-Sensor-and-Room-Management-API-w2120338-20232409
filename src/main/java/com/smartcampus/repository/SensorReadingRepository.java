package com.smartcampus.repository;

import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory data store for SensorReading entities.
 * Readings are grouped by sensorId. Uses the Singleton pattern.
 */
public class SensorReadingRepository {

    // Singleton instance
    private static final SensorReadingRepository INSTANCE = new SensorReadingRepository();

    // Thread-safe storage: sensorId -> list of readings
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private SensorReadingRepository() {
        // Private constructor to enforce singleton
    }

    public static SensorReadingRepository getInstance() {
        return INSTANCE;
    }

    /**
     * Get all readings for a specific sensor.
     * Returns an empty list if no readings exist yet.
     */
    public List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, Collections.emptyList());
    }

    /**
     * Add a new reading for a specific sensor.
     * Thread-safe: uses computeIfAbsent with a synchronized list.
     */
    public void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(reading);
    }
}
