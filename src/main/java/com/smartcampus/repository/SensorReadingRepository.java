package com.smartcampus.repository;

import com.smartcampus.model.SensorReading;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SensorReadingRepository {
    private static final SensorReadingRepository instance = new SensorReadingRepository();
    // Map sensorId -> List of Readings
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private SensorReadingRepository() {
    }

    public static SensorReadingRepository getInstance() {
        return instance;
    }

    public void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(reading);
    }

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        return readings.getOrDefault(sensorId, Collections.emptyList());
    }
}
