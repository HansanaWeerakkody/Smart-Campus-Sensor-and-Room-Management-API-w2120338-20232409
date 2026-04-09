package com.smartcampus.repository;

import com.smartcampus.model.Room;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoomRepository {
    private static final RoomRepository instance = new RoomRepository();
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    private RoomRepository() {
    }

    public static RoomRepository getInstance() {
        return instance;
    }

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    public void deleteRoom(String id) {
        rooms.remove(id);
    }

    public boolean exists(String id) {
        return rooms.containsKey(id);
    }
}
