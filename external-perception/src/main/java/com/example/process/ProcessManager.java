package com.example.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.config.AppConfig;
import com.example.monitor.HeartbeatMonitor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProcessManager {
    private static final Logger logger = LoggerFactory.getLogger(ProcessManager.class);
    
    private Process workerProcess;
    private List<String> workerCmd;
    private final int duration;
    private HeartbeatMonitor monitor;

    public ProcessManager(int duration) {
        this.duration = duration > 0 ? duration : AppConfig.DEFAULT_DURATION;
    }

    public void startProcess(List<String> cmd) throws IOException {
        // In real life, this would be more complex but for this example, 
        // we just start a process with the given command.
        this.workerCmd = new ArrayList<>(cmd);
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        this.workerProcess = pb.start();
        logger.info("Started worker process: " + String.join(" ", cmd));
    }

    public void restartProcess() throws IOException {
        if (this.workerCmd == null) {
            throw new IllegalStateException("No command stored. Call startProcess() first.");
        }

        if (workerProcess != null && isProcessRunning()) {
            logger.info("Terminating existing worker process...");
            terminateProcess(workerProcess);
        }

        startProcess(workerCmd);
    }

    public void terminateProcess(Process process) {
        if (process.isAlive()) {
            process.destroy();
            try {
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    process.waitFor(2, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            }
        }
    }

    public boolean isProcessRunning() {
        return workerProcess != null && workerProcess.isAlive();
    }

    public void startSystem(List<String> detectorCmd) throws IOException {
        logger.info("Starting heartbeat monitoring system...");
        logger.info("System duration: " + duration + " seconds");

        this.monitor = new HeartbeatMonitor(detectorCmd, duration);
        this.monitor.setProcessManager(this);
        
        Thread monitorThread = new Thread(monitor);
        monitorThread.start();

        try {
            monitorThread.join(duration * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        shutdownSystem();
    }

    public void shutdownSystem() {
        logger.info("Shutting down system...");

        if (workerProcess != null && isProcessRunning()) {
            logger.info("Terminating detector process...");
            terminateProcess(workerProcess);
        }

        logger.info("System shutdown completed");
    }

    public static void main( String[] args )
    {
        int duration = AppConfig.DEFAULT_DURATION;

        // command to run: java -jar target/external-perception-1.0.jar
        // This must be run from JAR file using this command or else the object detector will not be found.
        Path jarPath = null;
        try {
            jarPath = Paths.get(ProcessManager.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
        } catch (Exception e) {
            logger.warn("Error determining JAR path", e);
        }

        if (jarPath == null) {
            logger.error("Unable to determine JAR path. Ensure this is run from a JAR file.");
            System.exit(1);
        }

        List<String> detectorCmd = Arrays.asList("java", "-cp", jarPath.toString(), "com.example.detector.ObstacleDetector");

        if (args.length > 0) {
            try {
                duration = Integer.parseInt(args[0]);
                logger.info("Using custom duration: " + duration + " seconds");
            } catch (NumberFormatException e) {
                logger.warn("Invalid duration '" + args[0] + "', using default: " + duration);
            }
        }

        ProcessManager manager = new ProcessManager(duration);
        try {
            manager.startSystem(detectorCmd);
        } catch (Exception e) {
            logger.error("System error", e);
            manager.shutdownSystem();
            System.exit(1);
        }
    }
}
