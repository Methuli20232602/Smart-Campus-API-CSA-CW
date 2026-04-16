# Smart Campus Sensor & Room Management API

A robust, scalable, and highly available RESTful API built to manage rooms and diverse arrays of sensors (e.g., CO2 monitors, occupancy trackers, smart lighting controllers) across the university campus.

**Coursework:** Client-Server Architectures (5COSC022W)  
**Technology Stack:** Pure Java, JAX-RS (Jakarta RESTful Web Services via Jersey), embedded Grizzly HTTP server. Employs Thread-safe in-memory operations across a Singleton data schema without Spring Boot or external databases.

---

## Part 1 Conceptual Report

### 1. Application Lifecycle and State Management
**Question:** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

**Answer:**
By default, the JAX-RS runtime manages Resource classes on a **"per-request"** lifecycle. This means that an entirely new, distinct instance of the Resource class is instantiated by the server for every single incoming HTTP request, and it is subsequently destroyed (garbage collected) once the HTTP response is dispatched over the network. 

This specific architectural decision strictly dictates how we handle application state. Because Resource objects are inherently ephemeral, we cannot use standard instance variables within the Resource classes to store lists of Rooms or Sensors (as the data would vanish immediately after the request finishes). Furthermore, because the server handles multiple requests concurrently using multi-threading, attempting to store data in simple static fields would lead to dangerous race conditions and severe data corruption. 

To solve this, state management must be completely decoupled. This is accomplished by implementing a centralized `DataStore` class using the **Singleton** design pattern. Every per-request Resource instance retrieves exactly the same underlying `DataStore` instance. Crucially, the `DataStore` uses `ConcurrentHashMap` instead of basic `HashMap` or `ArrayList`, which provides guaranteed thread-safety. This ensures that even when hundreds of ephemeral JAX-RS requests attempt to read and write data simultaneously, the state remains completely synchronized, and no data is lost to race conditions.

### 2. Discoverability and Hypermedia (HATEOAS)
**Question:** Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

**Answer:**
The provision of Hypermedia (often summarized as **HATEOAS** - *Hypermedia As The Engine Of Application State*) is fundamentally considered the highest level of advanced RESTful design (Levels 3 of the Richardson Maturity Model). It transitions an API from being a simple, rigid RPC (Remote Procedure Call) wrapper into a dynamic, self-describing system.

This approach offers profound benefits for client developers when compared to traditional static documentation. Without HATEOAS, client applications must have URL paths rigidly hardcoded into their source code based on a PDF or Swagger document. If the server decides to change its URI routing in the future, the client integration immediately breaks. 

With HATEOAS, the client developer accesses the API much like a human browsing a website. They hit a single root entry point (like our Discovery endpoint `/api/v1`), and the server responds with a JSON payload that contains the explicit, valid navigation links (URIs) to the next possible actions or data collections. The client application simply reads these dynamic links from the payload and follows them dynamically. This completely decouples the client from the server’s exact URI namespace routing. It allows the backend system to evolve, reorganize collections, and introduce new workflows, all without ever breaking the client-side implementation.

---

## Part 2 Conceptual Report

### 1. Data Granularity and Network Efficiency
**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

**Answer:**
Returning only standard IDs (e.g., `["LIB-301", "LAB-102"]`) drastically minimizes the size of the initial JSON payload, heavily conserving **network bandwidth**. This is highly beneficial in environments with slow networks or for mobile/IoT clients. However, it negatively shifts the processing burden onto the client side. If the client needs to display room details, it is forced to make multiple independent HTTP `GET /rooms/{id}` calls for every single ID—creating an infamous "N+1 query problem" that introduces compounding network latency overhead. 

Conversely, returning the **full room objects** upfront consumes more initial bandwidth. However, it is vastly superior for client-side processing, as the client immediately receives all necessary metadata to render the UI locally without making any secondary, latency-inducing HTTP requests to the server.

### 2. Idempotency in REST Operations
**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer:**
Yes, the `DELETE` operation in our REST implementation is mathematically **idempotent**. 

In RESTful architecture, idempotency dictates that executing a request multiple identically-constructed times must leave the server's resource state exactly the same as executing it just once. 
If a client mistakenly sends the exact same `DELETE /api/v1/rooms/LIB-301` request multiple times:
1. **The first request** successfully locates the room, evaluates the empty sensor safety check, removes the room from the `DataStore`, and returns an HTTP `204 No Content` code.
2. **The second (and any subsequent) request** processes the exact same URL. Because the room was already deleted, the `dataStore.getRooms().get(roomId)` check evaluates to `null`. The server gracefully aborts and returns an HTTP `404 Not Found`.

Despite the HTTP Status code responding differently (204 vs 404), the absolute **server state** (the absence of the "LIB-301" room) remains identical after the first request and the hundredth request. Therefore, the `DELETE` operation perfectly preserves its idempotent nature.

---

## Part 3 Conceptual Report

### 1. Payload Formatting and `@Consumes` Mismatches
**Question:** We explicitly use the `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

**Answer:**
When a method is mapped with `@Consumes(MediaType.APPLICATION_JSON)`, it establishes a strict programmatic contract guaranteeing the endpoint will only process HTTP requests carrying the `Content-Type: application/json` header. 
If a client poorly forms their request and attempts to send data in an entirely different format (such as `text/plain` or `application/xml`), the JAX-RS framework intervenes before the Java method is ever executed. Because no matching endpoint exists that explicitly consumes that specific mismatched media type, JAX-RS immediately intercepts and rejects the request, automatically returning a standard **HTTP 415 Unsupported Media Type** response. This acts as a robust, built-in defensive mechanism shielding the backend parsing logic from processing fundamentally incompatible data payloads.

### 2. Filtering Architecture: `@QueryParam` vs `@PathParam`
**Question:** You implemented this filtering using `@QueryParam`. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Answer:**
Utilizing `@QueryParam` (e.g., `/api/v1/sensors?type=CO2`) is universally considered the superior architectural pattern for filtering REST collections compared to embedding the filter mathematically into the URI path itself (`/api/v1/sensors/type/CO2`).

In a pure RESTful design context, the URI Path should be rigidly reserved to identify the exact target **Resource** or hierarchical address (e.g., retrieving a specific room or sensor). Query parameters, by contrast, act as dynamic modifiers or filters applied *against* that targeted collection. 
Using query strings offers enormous flexibility. It empowers clients to intuitively stack multiple filters simultaneously (e.g., `?type=CO2&status=ACTIVE`) without forcing the server architect to artificially construct and map dozens of complex, deeply-nested URL pathways endpoint by endpoint. This elegantly minimizes backend controller bloat while maintaining a clean, deterministic endpoint URI namespace.

---

## Part 4 Conceptual Report

### 1. Architectural Benefits of Sub-Resource Locators
**Question:** What are the architectural benefits of using the Sub-Resource Locator pattern for the `{sensorId}/readings` path, as opposed to implementing all reading-related GET and POST methods directly inside the parent SensorResource class?

**Answer:**
Implementing the **Sub-Resource Locator** pattern fundamentally enforces the Single Responsibility Principle (SRP) and rigorously prevents the development of massive, unmaintainable monolithic classes. 

If all reading-related `GET` and `POST` endpoints were jammed directly inside the parent `SensorResource` class, that class's footprint would aggressively bloat. It would be forced to simultaneously juggle Sensor metadata operations AND the high-throughput, structurally different time-series operations for readings.

By delegating the `/readings` URI branch down to a completely dedicated `SensorReadingResource`, we achieving a distinct separation of concerns. `SensorResource` exclusively handles Sensor lifecycle management, while `SensorReadingResource` is strictly tailored to appending and querying historical data logs.


Furthermore, the sub-resource locator mechanism elegantly isolates the `{sensorId}` from the URI path just *once* and passes it dynamically into the sub-resource's constructor. This means the sub-resource's internal methods do not need to repetitively declare, extract, and trace `@PathParam("sensorId")` on every single endpoint they offer. This radically strips away boilerplate code, minimizes mapping vulnerabilities, and keeps the overall JAX-RS codebase cleanly modularized.

---

## Part 5 Conceptual Report

### 1. HTTP 422 (Unprocessable Entity) vs HTTP 404 (Not Found)
**Question:** Explain the semantic difference between returning HTTP 422 (Unprocessable Entity) and HTTP 404 (Not Found) when a client attempts to register a sensor with a `roomId` that does not exist. Why is 422 the more appropriate choice?

**Answer:**
HTTP 404 (Not Found) semantically communicates that the **target URI itself** does not map to any resource on the server. If we returned 404 when a sensor registration fails due to a non-existent `roomId`, we would be falsely telling the client that the endpoint `/api/v1/sensors` does not exist—which is incorrect. The endpoint is perfectly valid and reachable.

HTTP 422 (Unprocessable Entity) carries a fundamentally different meaning: the server successfully received and parsed the request body (the JSON was syntactically valid), but the **content within the payload failed semantic business validation**. In our case, the `roomId` field inside the JSON body references a room that does not exist in the `DataStore`. The request is structurally well-formed, but logically invalid.

Using 422 is therefore the architecturally correct choice because it accurately distinguishes between "I cannot find the URL you are requesting" (404) and "I understood your request, but the data you provided violates a business rule" (422). This distinction is critical for client developers to programmatically differentiate between routing errors and validation errors, enabling them to display appropriate user-facing feedback.

### 2. Security Risks of Exposing Raw Stack Traces
**Question:** What are the security risks of allowing raw Java stack traces to be returned in API error responses? How does the `GlobalExceptionMapper` mitigate this?

**Answer:**
Allowing raw Java stack traces to leak into HTTP responses represents a severe **information disclosure vulnerability**. Stack traces expose critically sensitive implementation details including: the exact Java class names and package hierarchy, the precise line numbers where failures occur, the names and versions of third-party libraries in use, internal method signatures, and potentially even database connection strings or file system paths embedded in error messages.

An attacker can exploit this information to map the internal architecture of the application, identify known vulnerabilities in specific library versions (CVE databases), and craft targeted injection or denial-of-service attacks with surgical precision.

The `GlobalExceptionMapper` class in our implementation acts as a security-hardened safety net. By implementing `ExceptionMapper<Throwable>`, it intercepts **every** unhandled exception before the JAX-RS runtime can generate a default response. Instead of forwarding the raw stack trace to the client, it returns a sanitized, generic error message ("An unexpected internal server error occurred") while logging the full technical details server-side using `System.err`. This ensures that developers retain full diagnostic capability for debugging while the external API surface reveals absolutely nothing about the internal implementation to potential attackers.

### 3. Architectural Advantages of JAX-RS Filters for Cross-Cutting Concerns
**Question:** What are the advantages of using JAX-RS `ContainerRequestFilter` and `ContainerResponseFilter` to implement logging, as opposed to manually adding logging statements inside every resource method?

**Answer:**
Using JAX-RS filters for logging and observability enforces the **Separation of Concerns** principle and the **DRY (Don't Repeat Yourself)** principle simultaneously.

If logging were implemented manually inside every resource method, we would face several severe problems:
1. **Code Duplication:** Every single `@GET`, `@POST`, `@DELETE` method across `SensorRoomResource`, `SensorResource`, and `SensorReadingResource` would need identical boilerplate logging code copied into it. This creates a maintenance nightmare where a format change requires editing dozens of methods.
2. **Fragility:** A developer adding a new endpoint might forget to include the logging code, creating blind spots in the observability pipeline.
3. **Polluted Business Logic:** Resource methods would become cluttered with infrastructure code (timestamps, latency calculations), making the actual business logic harder to read and review.

By contrast, implementing a single `LoggingFilter` class annotated with `@Provider` provides **automatic, global coverage** across every endpoint in the entire API. The filter intercepts requests and responses at the container level, completely outside the resource method lifecycle. This means:
- **Zero modification** to existing resource classes is required.
- **New endpoints** automatically inherit logging without any developer action.
- **Latency tracking** is computed once in a single location using `ContainerRequestContext.setProperty()` to pass state between the request and response filter phases.
- The filter can be **enabled or disabled** at the configuration level (in `Main.java` or `ResourceConfig`) without touching any business logic whatsoever.
