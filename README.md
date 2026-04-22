# Smart Campus Sensor & Room Management API

A robust, scalable, and highly available RESTful API to manage rooms and sensors for a university campus. Built using Java 11, JAX-RS (Jersey 2.41), Grizzly, and Jackson, adhering strictly to RESTful architectural patterns without the use of external databases or Spring Boot.

## Overview of API Design

The API is designed following resource-oriented RESTful principles. It offers normal Create, Read and Delete operations which are logically mapped to HTTP operations (GET, POST, and DELETE).

### Core Features:
- **Models**: 'Room', 'Sensor', and 'SensorReading' standard POJOs. Data is securely stored in thread-safe, in-memory 'ConcurrentHashMap's with 'Collections.synchronizedList()'.
- **HATEOAS**: A root Discovery endpoint (`/api/v1`) providing hypermedia links to core resources.
- **Sub-resources**: Advanced resource nesting allowing 'SensorReadings' to be logically mapped strictly under their parent 'Sensor' context (`/sensors/{sensorId}/readings`).
- **Global Error Handling**: Comprehensive 'ExceptionMappers' mapping specific exceptions to strict HTTP status codes (`403 Forbidden`, `409 Conflict`, `422 Unprocessable Entity`), including a robust 'GenericExceptionMapper' blocking the leakage of Java stack traces using a `500` catch-all.
- **Traffic Logging**: Request and Response JAX-RS filters providing clean API observability.

---

## Setup & Launch Instructions

### Prerequisites
- JDK 11 or higher installed (`java -version`)
- Apache Maven installed (`mvn -version`)

### Step 1: Clone the Repository
```bash
git clone https://github.com/HansanaWeerakkody/Smart-Campus-Sensor-and-Room-Management-API-w2120338-20232409.git
cd Smart-Campus-Sensor-and-Room-Management-API-w2120338-20232409
```

### Step 2: Build the Project
Open your terminal in the project root directory containing the `pom.xml` file. Clean and compile the project using Maven:
```bash
mvn clean compile
```

### Step 3: Run the Server
Launch the embedded Grizzly HTTP server using the Exec Maven Plugin:
```bash
mvn exec:java "-Dexec.mainClass=com.smartcampus.Main"
```
The server will start listening at `http://localhost:8080/api/v1/`.

*Note: Press `Enter` in the server terminal at any time to gracefully shut it down.*

---

## Sample API Interactions (Curl Commands)

Once the server is running, execute these commands in a separate terminal to test the system:

**1. Discovery Endpoint (HATEOAS)**
```bash
curl -s http://localhost:8080/api/v1/
```

**2. Create a Room (201 Created)**
```bash
curl -i -X POST -H "Content-Type: application/json" -d "{\"id\":\"LIB-301\",\"name\":\"Library Quiet Study\",\"capacity\":50}" http://localhost:8080/api/v1/rooms
```

**3. Register a Sensor to a Room (201 Created)**
```bash
curl -i -X POST -H "Content-Type: application/json" -d "{\"id\":\"TEMP-001\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":22.5,\"roomId\":\"LIB-301\"}" http://localhost:8080/api/v1/sensors
```

**4. Attempt to Delete an Occupied Room (409 Conflict)**
```bash
curl -i -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

**5. Post a New Reading (Sub-Resource, 201 Created)**
```bash
curl -i -X POST -H "Content-Type: application/json" -d "{\"value\":23.1}" http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

**6. Create a Bad Sensor with a Non-Existent Room (422 Unprocessable Entity)**
```bash
curl -i -X POST -H "Content-Type: application/json" -d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"roomId\":\"FAKE-ROOM\"}" http://localhost:8080/api/v1/sensors
```

**7. Filter Sensors by Type (Query Parameter)**
```bash
curl -s "http://localhost:8080/api/v1/sensors?type=Temperature"
```

**8. Post a Reading to a Sensor in MAINTENANCE (403 Forbidden)**
```bash
curl -i -X POST -H "Content-Type: application/json" -d "{\"id\":\"MAINT-001\",\"type\":\"CO2\",\"status\":\"MAINTENANCE\",\"currentValue\":0,\"roomId\":\"LIB-301\"}" http://localhost:8080/api/v1/sensors
curl -i -X POST -H "Content-Type: application/json" -d "{\"value\":10.5}" http://localhost:8080/api/v1/sensors/MAINT-001/readings
```

---

## Conceptual Report (Theory Questions)

### Part 1: Service Architecture & Setup

**Q: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.**.

*Answer:* JAX-RS Resource classes are by default configured as Request-Scoped, i.e. a new instance of the resource class is created and destroyed each time an incoming HTTP request comes by. This imposes a logical statelessness, but provides a significant challenge to shared persistence. When we put data into ordinary non-static lists or maps, directly within the resource class (e.g. private List rooms = new ArrayList<>();), the lists will be re-created each time we make a request and the data will be immediately lost.

To avoid this, our data layer uses the Singleton Pattern, in particular, it uses the Concurrent HashMap within separate static Repository classes (e.g., RoomRepository). This guarantees that every short-lived Resource instance fetches references to a well-known common memory structure that is highly thread-safe and can safely serve concurrent multi-client requests and never lose its data nor have any race-condition. Further, Collections such as the Sensor ID mapping array also make explicit use of Collections.synchronizedList(new ArrayList<>()) to strengthen internal thread guarantees even further.

**Q: Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?**

*Answer:* HATEOAS (Hypermedia As The Engine Of Application State) is a design that enables the API to dynamically direct the client by embedding pertinent URIs of secondary resources directly within API responses. This is REST Maturity Level 3. It has the advantage of saving client developers a lot of hard-coding of URLs. Assuming the backend subsequently changes its scaling paths or routes, the client simply performs a dynamic adaptation since they systematically visit the URLs, which are dynamically defined by the server JSON response, and do not cause architectural breakage as it traditionally has done through fixed documentation dependencies.

### Part 2: Room Management

**Q: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.**

*Answer:* The consumption of Network Bandwidth is greatly reduced with the return of only IDs. The payload is also universal making it lighter and therefore giving ultra-low latency when on mobile or highly congested networks. The negative connotation however, strikes on the Client-Side Processing which directly results in the "(N+1) Problem". The client must reiteratively loop through each ID, and repeatedly make calls to GET /rooms/roomId to retrieve complete metadata, killing server stability and radically complicating original client screen rendering logic. On the other hand, the full object is pushed back, yields larger data sizes up-front, but gives the frontend all it requires in a single pass.

**Q: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.**

*Answer:* Yes, The endpoint, delete /api/v1/rooms/roomId is indeed designed to be strictly idempotent. When a client sends the first request of a delete on an empty room correctly, the application will find the record and delete it forever, responding with an HTTP code of '204 no content'.

If the exact same `DELETE` request is errantly relayed across the network a second time, the resource correctly processes this as perfectly safe. It looks into the memory store and identifies the room has already been deleted/gives a null response, avoids exceptions, and gracefully produces '204 No Content' again. At the end of 'N' successive calls the system state is exactly the same.

### Part 3: Sensor Operations & Linking

**Q: We explicitly use the `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?**

*Answer:* JAX-RS also follows the standards of HTTP Content Negotiation. With the annotation of Content-Type: application/json, the endpoint can only accept payloads of the specified type as a result of the annotation of the endpoint: `@Consumes(MediaType.APPLICATION_JSON)`.
In case a client sends payloads with the tag text/plain or application/xml the internal JAX-RS routing provider identifies the incompatibility at the middleware layer. Before it even tries to access the Java method variables this causes a short-circuit, and immediately bounces the request dynamically with an HTTP error status of '415 Unsupported Media Type' going straight to the user.

**Q: You implemented this filtering using `@QueryParam`. Contrast this with an alternative design where the type is part of the URL path (e.g., `/api/v1/sensors/type/CO2`). Why is the query parameter approach generally considered superior for filtering and searching collections?**

*Answer:* The first fundamental principle of REST is that different URIs identify different physical entities or resources (such as /api/v1/sensors/123) in a unique way. Alternatively, parameters dictate temporary 'actions' or non-resource 'states'.
The very nature of encoding of `/type/CO2` implies that Type is a hard coded hierarchical structure with child elements. Filtering using the query param (?type=CO2) directly indicates that we are dealing with the same root (sensors) collection resource but dynamically limiting the characteristics that can be seen. This guarantees standard pagination, filtering and mass-sorting metrics stack together in queries (`?type=CO2&status=ACTIVE`) with a natural stack without ad infinitum growing unmaintainable URL path structures.

### Part 4: Deep Nesting with Sub-Resources

**Q: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., `sensors/{id}/readings/{rid}`) in one massive controller class?**

*Answer:* The Sub-Resource Locator entirely separates modular concerns. As APIs scale dramatically, cramming all nested methods inside `SensorResource.java` guarantees a "God Class" anti-pattern forming thousands of redundant lines of code blurring distinct logic.
Through delegation, our primary `SensorResource` solely determines if a sensor functionally exists, and upon true, completely hands operation control off to the subsequent `SensorReadingResource`. This splits distinct dependency injected classes precisely matching real-world context boundaries (Readings are explicitly separated from Sensors), improving localized unit testing, enabling future route extensions rapidly, and naturally segregating unique security authorization roles logic uniquely.

### Part 5: Advanced Error Handling

**Q: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?**

*Answer:* HTTP `404 Not Found` is natively hard-wired to declare the URI itself is functionally dead or unreachable natively (`/api/v1/invalid_path`).
When a client sends a structurally perfect, validly typed JSON Payload mapping successfully to the matching Java POJO (`{ "roomId" : "non-existent-1" }`) to a valid active endpoint, utilizing 404 is technically inaccurate. HTTP `422 Unprocessable Entity` correctly dictates that the server successfully parsed the valid JSON structure natively, but uniquely determined the internal 'Business Data Logic' elements or relationships (such as referencing a missing foreign key `roomId`) inside the body itself restricted processing completion natively.

**Q: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?**

*Answer:* Uncaught Java stack traces represent a catastrophic security leakage directly feeding black-box threat intelligence. Attackers natively gain detailed infrastructure intel including exact library dependencies, direct operating server directories, explicit package layouts, database connection methods, and underlying algorithmic frameworks running the application directly.
This intel facilitates precise zero-day exploiting frameworks. Our application prevents this strictly by enacting the `GenericExceptionMapper` mapping `Throwable.class`, completely masking internal leakage and gracefully returning only an opaque HTTP `500 Internal Server error`.

**Q: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting `Logger.info()` statements inside every single resource method?**

*Answer:* Centralized filtering champions the precise "Don't Repeat Yourself (DRY)" engineering constraint natively. Inserting logic systematically into 50+ individual controllers ensures rapid maintenance decay; if the logging architecture requirement shifts slightly, engineers must redundantly adjust thousands of duplicated lines. By deploying `ContainerRequestFilter` and `ContainerResponseFilter`, logging is injected at the JAX-RS global runtime lifecycle boundary. It guarantees absolute holistic observability seamlessly over entire unmapped domains while preserving resource controllers entirely single-focused solely on unique business requirement operations without visual pollution.
