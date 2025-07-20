package com.example.detector;

import com.example.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class ObstacleDetector implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ObstacleDetector.class);
    private static final Random random = new Random();
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final int heartbeatInterval;
    private final DatagramSocket heartbeatSocket;
    private final InetAddress monitorAddress;
    private final int monitorPort;
    private volatile boolean running;

    public ObstacleDetector() throws IOException {
        this.heartbeatInterval = AppConfig.HEARTBEAT_INTERVAL;
        this.heartbeatSocket = new DatagramSocket();
        this.monitorAddress = InetAddress.getByName(AppConfig.HEARTBEAT_HOST);
        this.monitorPort = AppConfig.HEARTBEAT_PORT;
        this.running = false;
    }

    @Override
    public void run() {
        running = true;
        logger.info("Starting ObstacleDetector...");
        while (running) {
            try {
                sendHeartbeat();
                detectObstacles();
                simulateFailure();
                Thread.sleep(heartbeatInterval);
            } catch (InterruptedException e) {
                logger.warn("Detector interrupted", e);
                Thread.currentThread().interrupt();
                stop();
            } catch (Exception e) {
                logger.error("Detector error", e);
                stop();
            }
        }
    }

    public void stop() {
        running = false;
        heartbeatSocket.close();
        logger.info("ObstacleDetector stopped");
    }

    private void sendHeartbeat() throws IOException {
        String message = LocalDateTime.now().format(formatter);
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(
            buffer, buffer.length, monitorAddress, monitorPort);
        heartbeatSocket.send(packet);
        logger.info("Heartbeat sent at " + message);
    }

    private void simulateFailure() {
        if (random.nextDouble() < 0.01) {
            logger.warn("Simulating a crash...");
            System.exit(1);
        }
    }

    private void detectObstacles() throws InterruptedException {
        Thread.sleep(random.nextInt(20) + 10); // 10-30ms delay
        double distance = 1 + random.nextDouble() * 99; // 1-100 meters
        logger.info(String.format("Detected obstacle at %.2f meters", distance));
    }

    public static void main(String[] args) {
        try {
            ObstacleDetector detector = new ObstacleDetector();
            Runtime.getRuntime().addShutdownHook(new Thread(detector::stop));
            detector.run();
        } catch (Exception e) {
            logger.error("Failed to start ObstacleDetector", e);
        }
    }
}
