package com.smartcampus.repository;

import com.smartcampus.model.Sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory data store for Sensor entities.
 * Uses the Singleton pattern to ensure a single shared instance across all
 * resource classes.
 */
public class SensorRepository {

    // Singleton instance
    private static final SensorRepository INSTANCE = new SensorRepository();

    // Thread-safe storage
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    private SensorRepository() {
        // Private constructor to enforce singleton
    }

    public static SensorRepository getInstance() {
        return INSTANCE;
    }

    // --- CRUD Operations ---

    public List<Sensor> getAll() {
        return new ArrayList<>(sensors.values());
    }

    public Sensor getById(String id) {
        return sensors.get(id);
    }

    public void add(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
    }

    public Sensor remove(String id) {
        return sensors.remove(id);
    }

    public boolean exists(String id) {
        return sensors.containsKey(id);
    }

    /**
     * Retrieve all sensors filtered by type.
     */
    public List<Sensor> getByType(String type) {
        return sensors.values().stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all sensors assigned to a specific room.
     */
    public List<Sensor> getByRoomId(String roomId) {
        return sensors.values().stream()
                .filter(s -> roomId.equals(s.getRoomId()))
                .collect(Collectors.toList());
    }
}
