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
