# Drone Service

This is the Drone service (Spring Boot) implementing a simple REST API to register drones, load medications,
query drones and their battery/medication information. The project is buildable and runnable with the included
Maven wrapper and uses an in-memory H2 database with preloaded data.

Quick facts
- Java: 21 (project configured for Java 21)
- Spring Boot: 3.5.x
- Database: H2 (in-memory)
- APIs: JSON input/output (snake_case)

What is included
- Complete source implementation (src/)
- Preloaded sample data: `src/main/resources/data.sql` (several drones + medications)
- Postman collection: `Hitachi Drone REST API - Complete.postman_collection.json`
- Unit tests for controller, service and scheduler (src/test/)

Build

On Windows (cmd.exe) using the included Maven wrapper:

```cmd
mvnw.cmd -v
mvnw.cmd clean package
```

Run

Run with the Maven Spring Boot plugin:

```cmd
mvnw.cmd spring-boot:run
```

Or run the packaged JAR:

```cmd
java -jar target\drone-0.0.1-SNAPSHOT.jar
```

By default, the server starts on port 8080. Application settings are in `src/main/resources/application.yaml`.

Tests

Run unit tests with:

```cmd
mvnw.cmd test
```

Database / Preloaded data
- The application uses an in-memory H2 database (jdbc:h2:mem:drone-db).
- Sample data is inserted from `src/main/resources/data.sql` at startup. It contains 5 drones and multiple
  medications distributed across them. Model weight limits used in the sample data are:
  - LIGHTWEIGHT = 250 g
  - MIDDLEWEIGHT = 500 g
  - CRUISERWEIGHT = 750 g
  - HEAVYWEIGHT = 1000 g

H2 Console
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:drone-db
- User: sa
- Password: (empty)

Postman / API testing
- A Postman collection is included at the repository root: `Hitachi Drone REST API - Complete.postman_collection.json`.
  Import it into Postman to try the example requests.

API Endpoints (short reference)

Base path: /api/drones

- Register a drone
  - POST /api/drones
  - Body (JSON, snake_case):

```json
{
  "serial_number": "DRONE-100",
  "model": "HEAVYWEIGHT",
  "battery_capacity": 100.0
}
```

- Load medications to a drone
  - POST /api/drones/{serialNumber}/medications
  - Body (JSON) example:

```json
{
  "medications": [
    {
      "name": "Medication-A",
      "weight": 100.0,
      "code": "MED_001",
      "image": "https://example.com/med.jpg"
    }
  ]
}
```

- Get drone by serial number
  - GET /api/drones/{serialNumber}

- List drones (optionally filter by state)
  - GET /api/drones
  - Example: /api/drones?state=IDLE

Behavior and important notes
- Maximum drone counts and thresholds are configurable via `app.drone` properties in `application.yaml`.
- Batteries below 25% cannot be loaded (configurable `min-battery-for-loading`).
- Weight limits depend on the drone model (see sample limits above). The service prevents loading above capacity.
- A scheduler advances drone states (LOADING → LOADED → DELIVERING → DELIVERED → RETURNING → IDLE) and
  reduces battery capacity after deliveries. See unit tests for exact behavior and examples.

Quick curl examples (Windows cmd.exe)

Register a drone:

```cmd
curl -X POST http://localhost:8080/api/drones -H "Content-Type: application/json" -d "{
  \"serial_number\": \"DRONE-100\",
  \"model\": \"HEAVYWEIGHT\",
  \"battery_capacity\": 100.0
}"
```

Load medication:

```cmd
curl -X POST http://localhost:8080/api/drones/DRONE-100/medications -H "Content-Type: application/json" -d "{
  \"medications\": [
    {\"name\": \"Med-1\", \"weight\": 100.0, \"code\": \"MED_001\", \"image\": \"https://example.com/med1.jpg\"}
  ]
}"
```

Get drone:

```cmd
curl http://localhost:8080/api/drones/DRONE-100
```
