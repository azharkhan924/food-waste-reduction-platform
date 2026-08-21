# 📊 Loki + Grafana Logging Guide

A beginner-friendly guide to monitoring your Food Waste Reduction Platform logs with **Grafana Loki**.

## What is This?

- **Loki** = Log storage (like a database, but for logs)
- **Promtail** = Log shipper (reads your app's log file and sends it to Loki)
- **Grafana** = Dashboard UI (lets you search and visualize logs in your browser)

---

## 🔧 Prerequisites — One-Time Setup

### 1. Install Homebrew (if you don't have it)

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

### 2. Install the tools

```bash
brew install grafana
brew install loki
brew install promtail
```

### 3. Copy Grafana datasource config

This tells Grafana where to find Loki automatically:

```bash
# Find where Grafana stores its config
GRAFANA_HOME=$(brew --prefix grafana)/share/grafana

# Copy our datasource provisioning file
cp -r grafana/provisioning/datasources/ "$GRAFANA_HOME/conf/provisioning/datasources/"
```

> **Note**: You only need to do step 3 once. After that, Grafana will auto-connect to Loki every time it starts.

---

## 🚀 Quick Start (Every Time You Want to Use It)

### Option A: Use the start script (easiest)

```bash
# Start everything
./start-logging.sh start

# Check status
./start-logging.sh status

# Stop everything
./start-logging.sh stop
```

### Option B: Start manually (if you want to learn)

Open **3 separate terminal tabs**:

**Terminal 1 — Start Loki:**
```bash
loki -config.file=loki/loki-config.yml
```

**Terminal 2 — Start Promtail:**
```bash
promtail -config.file=promtail/promtail-config.yml
```

**Terminal 3 — Start Grafana:**
```bash
brew services start grafana
```

Then start your Spring Boot app as usual:
```bash
./mvnw spring-boot:run
```

---

## 🔍 Using Grafana to Search Logs

### 1. Open Grafana
Go to **http://localhost:3000** in your browser.

### 2. Login
- Username: `admin`
- Password: `admin`
- (It will ask you to change the password — you can skip for now)

### 3. Go to Explore
Click the **compass icon** (🧭) on the left sidebar → **Explore**

### 4. Select Loki
In the dropdown at the top, select **Loki** as the data source.

### 5. Write a Query
Type a LogQL query in the query box and click **Run query**.

---

## 📝 LogQL Cheat Sheet (Loki's Query Language)

### Basic Queries

| Query | What It Shows |
|-------|---------------|
| `{job="foodwaste"}` | All logs from your app |
| `{job="foodwaste"} \|= "ERROR"` | Only error logs |
| `{job="foodwaste"} \|= "WARN"` | Only warning logs |
| `{job="foodwaste"} \|= "DEBUG"` | Only debug logs |

### Search for Specific Things

| Query | What It Shows |
|-------|---------------|
| `{job="foodwaste"} \|= "login"` | Login-related logs |
| `{job="foodwaste"} \|= "donation"` | Donation-related logs |
| `{job="foodwaste"} \|= "Exception"` | Any exceptions/errors |
| `{job="foodwaste"} \|= "restaurant"` | Restaurant-related logs |

### Exclude Something

| Query | What It Shows |
|-------|---------------|
| `{job="foodwaste"} != "DEBUG"` | All logs EXCEPT debug |
| `{job="foodwaste"} \|= "ERROR" != "404"` | Errors, but not 404s |

### Case-Insensitive Search

```
{job="foodwaste"} |~ "(?i)error"
```

---

## 🛑 Stopping Everything

### With the script:
```bash
./start-logging.sh stop
```

### Manually:
```bash
# Stop Grafana
brew services stop grafana

# Stop Loki & Promtail — press Ctrl+C in their terminal tabs
```

---

## ❓ Troubleshooting

### "No data" in Grafana
1. Make sure your Spring Boot app is running and generating logs
2. Check that `logs/foodwaste.log` exists and has content
3. Make sure Loki is running: visit http://localhost:3100/ready (should say "ready")
4. Make sure Promtail is running: check its terminal for errors

### Grafana won't start
```bash
# Check if port 3000 is in use
lsof -i :3000

# Restart Grafana
brew services restart grafana
```

### Loki won't start
```bash
# Check if port 3100 is in use
lsof -i :3100

# Clear Loki's data and restart
rm -rf /tmp/loki
loki -config.file=loki/loki-config.yml
```

### Promtail shows "file not found"
Make sure your Spring Boot app has run at least once so that `logs/foodwaste.log` exists:
```bash
ls -la logs/foodwaste.log
```

---

## 📁 File Overview

| File | Purpose |
|------|---------|
| `src/main/resources/logback-spring.xml` | Configures Spring Boot to write logs to a file |
| `loki/loki-config.yml` | Loki server configuration |
| `promtail/promtail-config.yml` | Promtail — reads log file, sends to Loki |
| `grafana/provisioning/datasources/loki.yml` | Auto-configures Grafana → Loki connection |
| `start-logging.sh` | Start/stop script for all services |
