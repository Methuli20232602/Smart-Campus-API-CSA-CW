package com.smartcampus.repository;

import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory Data Store implemented as a Singleton.
 * We use ConcurrentHashMap to guarantee thread-safety since the JAX-RS API 
 * will handle multiple incoming HTTP requests concurrently across different threads.
 */
public class DataStore {

    // The single static instance of the DataStore
    private static DataStore instance = null;

    // Core data structures replacing a traditional database
    private final Map<String, Room> rooms;
    private final Map<String, Sensor> sensors;
    // Maps a sensorId to a list of its historical readings
    private final Map<String, List<SensorReading>> sensorReadings;

    // Private constructor to prevent instantiation from outside
    private DataStore() {
        this.rooms = new ConcurrentHashMap<>();
        this.sensors = new ConcurrentHashMap<>();
        this.sensorReadings = new ConcurrentHashMap<>();
    }

    // Thread-safe singleton access
    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    // Getters for the data structures
    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Map<String, List<SensorReading>> getSensorReadings() {
        return sensorReadings;
    }
}
