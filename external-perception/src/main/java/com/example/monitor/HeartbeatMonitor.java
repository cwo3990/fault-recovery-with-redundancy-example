package com.example.monitor;

import com.example.config.AppConfig;
import com.example.process.ProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class HeartbeatMonitor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatMonitor.class);
    
    private final int timeoutThreshold;
    private LocalDateTime lastHeartbeat;
    private final DatagramSocket heartbeatSocket;
    private ProcessManager processManager;
    private final int duration;
    private final long startTime;
    private final List<String> detectorCmd;

    public HeartbeatMonitor(List<String> detectorCmd, int duration) throws IOException {
        this.timeoutThreshold = AppConfig.TIMEOUT_THRESHOLD;
        this.heartbeatSocket = new DatagramSocket(AppConfig.HEARTBEAT_PORT);
        this.duration = duration > 0 ? duration : AppConfig.DEFAULT_DURATION;
        this.startTime = System.currentTimeMillis();
        this.detectorCmd = detectorCmd;
    }

    public void setProcessManager(ProcessManager processManager) {
        this.processManager = processManager;
    }

    @Override
    public void run() {
        logger.info("Starting HeartbeatMonitor...");
        logger.info("Monitoring duration: " + duration + " seconds");

        try {
            processManager.startProcess(detectorCmd);
            lastHeartbeat = LocalDateTime.now();
            heartbeatSocket.setSoTimeout(100); // 100ms socket timeout

            while (!Thread.currentThread().isInterrupted()) {
                if ((System.currentTimeMillis() - startTime) / 1000 >= duration) {
                    logger.info("Monitoring duration reached. Shutting down.");
                    processManager.shutdownSystem();
                    break;
                }

                receiveHeartbeat();
                if (checkTimeout()) {
                    logger.warn("Heartbeat timeout detected. Restarting process...");
                    restartProcess();
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            logger.warn("Monitor interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Monitor error", e);
        } finally {
            heartbeatSocket.close();
            logger.info("HeartbeatMonitor stopped");
        }
    }

    private void receiveHeartbeat() {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        try {
            heartbeatSocket.receive(packet);
            lastHeartbeat = LocalDateTime.now();
            logger.info("Heartbeat received at " + lastHeartbeat);
        } catch (IOException e) {
            // Expected for non-blocking operation
        }
    }

    private boolean checkTimeout() {
        if (lastHeartbeat != null) {
            Duration delta = Duration.between(lastHeartbeat, LocalDateTime.now());
            return delta.toMillis() > timeoutThreshold;
        }
        return false;
    }

    private void restartProcess() throws IOException {
        if (processManager != null) {
            processManager.restartProcess();
            lastHeartbeat = LocalDateTime.now();
            logger.info("Process restarted and heartbeat tracking reset");
        } else {
            logger.error("Error: ProcessManager not available for restart");
        }
    }
}
