package com.smartcampus.repository;

import com.smartcampus.model.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory data store for Room entities.
 * Uses the Singleton pattern to ensure a single shared instance across all
 * resource classes.
 */
public class RoomRepository {

    // Singleton instance
    private static final RoomRepository INSTANCE = new RoomRepository();

    // Thread-safe storage
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    private RoomRepository() {
        // Private constructor to enforce singleton
    }

    public static RoomRepository getInstance() {
        return INSTANCE;
    }

    // --- CRUD Operations ---

    public List<Room> getAll() {
        return new ArrayList<>(rooms.values());
    }

    public Room getById(String id) {
        return rooms.get(id);
    }

    public void add(Room room) {
        rooms.put(room.getId(), room);
    }

    public Room remove(String id) {
        return rooms.remove(id);
    }

    public boolean exists(String id) {
        return rooms.containsKey(id);
    }
}
