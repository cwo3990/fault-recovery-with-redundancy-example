# 🚗💓 Heartbeat Obstacle Detector (Java)

A Java implementation of the **Heartbeat** architectural tactic for fault detection and recovery, applied to an obstacle detection module in a self-driving car case study.

---

## 📋 Table of Contents

- [📖 Overview](#-overview)
- [🏗️ Architecture](#️-architecture)
- [💪 Architecture Strengths](#-architecture-strengths)
- [📡 UDP Communication](#-why-udp-for-heartbeats)
- [🔧 Components](#-components)
- [🚀 Installation](#-installation)
- [⚙️ Configuration](#️-configuration)
- [💻 Usage](#-usage)
- [📁 Project Structure](#-project-structure)

---

## 📖 Overview

This Java implementation demonstrates the **Heartbeat** architectural pattern for fault detection and recovery in distributed systems. The system simulates a critical obstacle detection module in a self-driving car, using UDP-based heartbeats with a 50ms interval to provide real-time fault detection and automatic recovery within 500ms.

Key features:
- Automatic process monitoring and recovery
- Configurable heartbeat intervals and timeout thresholds
- Graceful shutdown handling
- Comprehensive logging system

---

## 🏗️ Architecture

The system implements a **hierarchical orchestration pattern** with three main components:

1. **⚙️ ProcessManager**: Main orchestrator that manages system lifecycle
2. **👁️ HeartbeatMonitor**: Dedicated monitoring service for heartbeat detection
3. **🔍 ObstacleDetector**: Worker process performing obstacle detection

---

## 💪 Architecture Strengths

**🎯 Single Entry Point**: ProcessManager serves as the main orchestrator  
**🔄 Automatic Recovery**: Detects and recovers from failures within 500ms  
**🏗️ Modular Design**: Clean separation between components  
**📡 Lightweight UDP**: Minimizes network overhead  
**🛡️ Fault Isolation**: Process crashes don't affect monitoring  
**⚡ Real-time Response**: 50ms heartbeat interval  
**📝 Comprehensive Logging**: Built-in log rotation and formatting  

---

## 📡 Why UDP for Heartbeats

UDP provides ideal characteristics for heartbeat communication:

- **🚀 Ultra-Low Latency**: No connection setup
- **📉 Minimal Overhead**: Lightweight datagrams
- **🔁 Stateless**: Missed packets indicate failure
- **⚙️ Simple Implementation**: Basic socket operations

---

## 🔧 Components

- `ProcessManager.java` - Main orchestrator
- `HeartbeatMonitor.java` - Heartbeat monitoring service  
- `ObstacleDetector.java` - Detection worker process
- `AppConfig.java` - Centralized configuration
- `logback.xml` - Logging configuration
- `pom.xml` - Build configuration

---

## 🚀 Installation

1. Ensure you have Java 17+ and Maven installed
2. Clone the repository
3. Build the project:

```bash
mvn clean package
```

---

## ⚙️ Configuration

Configure via environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `HEARTBEAT_INTERVAL` | Heartbeat interval (ms) | 50 |
| `TIMEOUT_THRESHOLD` | Timeout threshold (ms) | 500 |
| `HEARTBEAT_HOST` | Host for communication | localhost |
| `HEARTBEAT_PORT` | Port for communication | 9999 |
| `DEFAULT_DURATION` | System duration (s) | 60 |

---

## 💻 Usage

Run the system:

```bash
java -jar target/external-perception-1.0.jar [duration_seconds]
```

Example with custom duration:

```bash
java -jar target/external-perception-1.0.jar 120
```

System behavior:
- ProcessManager starts the detector and monitor
- Heartbeats sent every 50ms
- Timeouts detected within 500ms
- Automatic restarts on failure
- Graceful shutdown after duration

---

## 📁 Project Structure

```text
external-perception/
├── src/main/java/com/example/
│   ├── process/ProcessManager.java
│   ├── monitor/HeartbeatMonitor.java
│   ├── detector/ObstacleDetector.java
│   └── config/AppConfig.java
├── src/main/resources/
│   └── logback.xml
├── target/
│   └── external-perception-1.0.jar
├── pom.xml
└── README.md
```

Logs are written to `logs/system.log` with rotation and compression.
