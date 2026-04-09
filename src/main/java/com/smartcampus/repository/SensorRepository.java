package com.smartcampus.repository;

import com.smartcampus.model.Sensor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SensorRepository {
    private static final SensorRepository instance = new SensorRepository();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    private SensorRepository() {
    }

    public static SensorRepository getInstance() {
        return instance;
    }

    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
    }

    public Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public List<Sensor> getAllSensors() {
        return new ArrayList<>(sensors.values());
    }

    public List<Sensor> getSensorsByRoom(String roomId) {
        return sensors.values().stream()
                .filter(s -> s.getRoomId().equals(roomId))
                .collect(Collectors.toList());
    }

    public List<Sensor> getSensorsByType(String type) {
        return sensors.values().stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    public void deleteSensor(String id) {
        sensors.remove(id);
    }

    public boolean exists(String id) {
        return sensors.containsKey(id);
    }
}
