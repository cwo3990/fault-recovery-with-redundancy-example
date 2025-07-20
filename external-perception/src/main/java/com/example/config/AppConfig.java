package com.example.config;

public class AppConfig {
    // Heartbeat interval in milliseconds
    public static final int HEARTBEAT_INTERVAL = 
        Integer.parseInt(System.getenv().getOrDefault("HEARTBEAT_INTERVAL", "50"));
    
    // Timeout threshold for missing heartbeat in milliseconds
    public static final int TIMEOUT_THRESHOLD = 
        Integer.parseInt(System.getenv().getOrDefault("TIMEOUT_THRESHOLD", "500"));
    
    // Host and port for heartbeat communication
    public static final String HEARTBEAT_HOST = 
        System.getenv().getOrDefault("HEARTBEAT_HOST", "localhost");
    public static final int HEARTBEAT_PORT = 
        Integer.parseInt(System.getenv().getOrDefault("HEARTBEAT_PORT", "9999"));
    
    // Default duration for monitoring and process manager in seconds
    public static final int DEFAULT_DURATION = 
        Integer.parseInt(System.getenv().getOrDefault("DEFAULT_DURATION", "60"));
}