# Stoker-Web v2

A full rewrite of Stoker-Web using a modern stack: Spring Boot 3.x + Kotlin backend, React + TypeScript + Vite frontend. The original v1 application (Java 1.6 / GWT) is in the parent directory.

## Requirements

- JDK 21+
- Node.js 18+ and npm (for the frontend build)
- A Stoker power draft device accessible on the network

## Configuration

Edit `src/main/resources/application.yml` before running:

```yaml
stoker:
  device:
    ip: 192.168.10.139   # IP address of your Stoker device
    port: 23
    login: root
    password: tini
  data-dir: ./data        # directory for cook logs, config, and user data

  email:
    enabled: false
    smtp-host: smtp.gmail.com
    smtp-port: 465
    username: ""
    password: ""
    send-to: ""
    use-ssl: true

  weather:
    enabled: false
    api-key: ""           # OpenWeatherMap API key (free tier)
    zip-code: ""
    units: imperial       # "imperial" for °F, "metric" for °C

server:
  port: 8080
```

## Building

The Gradle build compiles the Kotlin backend and the React frontend together into a single executable JAR. npm dependencies are installed automatically.

```bash
./gradlew bootJar
```

The JAR is written to `build/libs/stoker-web-1.0.0-SNAPSHOT.jar`.

### Building the frontend separately (for development)

```bash
cd frontend
npm install
npm run build       # production build → ../src/main/resources/static
npm run dev         # dev server with hot reload on http://localhost:5173
```

When using `npm run dev`, the Vite dev server proxies `/api` and `/ws` requests to the Spring Boot backend at `localhost:8080`.

## Running

### From the JAR (production)

```bash
java -jar build/libs/stoker-web-1.0.0-SNAPSHOT.jar
```

Open `http://localhost:8080` in a browser.

### From Gradle (development — backend only)

> **Note:** Do not use `bootRun` for automated builds or scripts. Use `bootJar` to verify the build.

```bash
./gradlew bootRun
```

For active frontend development, run the Vite dev server alongside:

```bash
cd frontend && npm run dev
```

Then open `http://localhost:5173`.

## First-time setup

1. Start the application and open the browser.
2. The first page load shows **"Device configuration required"** if no `CookerConfig.json` exists yet.
3. Click **Sign In** — the default credentials are set in the `data/` directory. To create a user:
   ```bash
   # The data directory must exist first (created on first run)
   # Add a user via the API or manually create data/users.json
   ```
4. Click **Configure** to open the device configuration dialog. Wait for the Stoker to connect — available sensors populate automatically after discovery.
5. Assign each sensor to a cooker, set alarm types and target temperatures, then save.

## Data directory layout

```
data/
  CookerConfig.json      # cooker/probe/fan assignments and alarm settings
  AlertsConfig.json      # alert rules
  users.json             # hashed user credentials
  cookLogs/              # cook session logs (JSON Lines format, .jsonl)
```

The `data-dir` path in `application.yml` is relative to the working directory when the app is launched.

## Project structure

```
stoker-web-v2/
  src/main/kotlin/com/gbak/stoker/
    model/          # data classes (devices, alerts, logs)
    telnet/         # Stoker telnet connection and stream parser
    stoker/         # device discovery (HTTP /stoker.json), pit monitor
    log/            # cook log manager and JSON Lines writer
    rest/           # Spring MVC REST controllers
    alerts/         # alert evaluation and email delivery
    config/         # Spring Security, WebSocket, configuration properties
  frontend/
    src/
      components/   # React UI components
      hooks/        # WebSocket hook (STOMP)
      api/          # fetch wrappers for REST API
      types/        # TypeScript interfaces matching backend models
  build.gradle.kts
  src/main/resources/application.yml
```
