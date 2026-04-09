# Smart Campus API - 20-Day Implementation Plan

This is a step-by-step 20-day plan to complete the "Smart Campus" Client-Server Architectures coursework. The plan is designed to spread out the work, allowing for daily commits to your GitHub repository and ensuring all strict requirements (e.g., pure JAX-RS, no Spring Boot, no databases) are met.

## 20-Day Coursework Breakdown

### Week 1: Project Setup, POJOs, & Part 1

*   **Day 1:** 
    *   Initialize the Maven project with Jersey (JAX-RS implementation) and an embedded server plug-in (like Tomcat or Jetty).
    *   Verify the build runs successfully.
    *   *Commit: "Initial project setup with Maven and Jersey"*
*   **Day 2:** 
    *   Create the core Object Models (POJOs): `Room`, `Sensor`, and `SensorReading`.
    *   Add getters, setters, and standard constructors.
    *   *Commit: "Add core data POJOs"*
*   **Day 3:** 
    *   Design and implement an In-Memory Data Store (using singletons with `HashMap` and `ArrayList`) to safely manage the state, adhering to the "no external database" constraint.
    *   *Commit: "Implement in-memory data storage singleton"*
*   **Day 4:** 
    *   Implement the `Application` configuration with `@ApplicationPath("/api/v1")`.
    *   Create the root "Discovery" resource endpoint (`GET /api/v1`).
    *   *Commit: "Add application config and discovery endpoint"*
*   **Day 5:** 
    *   Start the `README.md` and answer the **Part 1 questions** (Application lifecycle and HATEOAS).
    *   *Commit: "Draft Part 1 answers in README"*

---

### Week 2: Room Management & Sensor Foundations

*   **Day 6:** 
    *   (Part 2) Implement `SensorRoom` Resource.
    *   Build `GET /api/v1/rooms` to retrieve all rooms.
    *   *Commit: "Implement GET for all rooms"*
*   **Day 7:** 
    *   (Part 2) Build `POST /api/v1/rooms` to create new rooms.
    *   Build `GET /api/v1/rooms/{roomId}` to fetch metadata for a specific room.
    *   *Commit: "Implement POST and specific GET for rooms"*
*   **Day 8:** 
    *   (Part 2) Implement `DELETE /api/v1/rooms/{roomId}` with the business logic constraint (cannot delete if it has active sensors).
    *   *Commit: "Implement room deletion with safety checks"*
*   **Day 9:** 
    *   Answer **Part 2 questions** (returning IDs vs full objects, and DELETE idempotency) in `README.md`.
    *   *Commit: "Draft Part 2 answers in README"*
*   **Day 10:** 
    *   (Part 3) Implement `SensorResource` for path `/api/v1/sensors`.
    *   Implement `POST` with logic that verifies the `roomId` inside the request body actually exists in the data store.
    *   *Commit: "Implement POST for sensor registration with validation"*

---

### Week 3: Advanced Sensor Logic & Sub-Resources

*   **Day 11:** 
    *   (Part 3) Enhance `GET /api/v1/sensors` to support filtering by `type` via `@QueryParam`.
    *   *Commit: "Add filtered retrieval for sensors"*
*   **Day 12:** 
    *   Answer **Part 3 questions** (`@Consumes` mismatch behavior and QueryParam vs PathParam) in `README.md`.
    *   *Commit: "Draft Part 3 answers in README"*
*   **Day 13:** 
    *   (Part 4) Implement the **Sub-Resource Locator Pattern** in `SensorResource` for `{sensorId}/readings`.
    *   Create the `SensorReadingResource` class framework.
    *   *Commit: "Implement Sub-Resource Locator pattern for readings"*
*   **Day 14:** 
    *   (Part 4) Implement `GET` (fetch history) and `POST` (append new reading) within `SensorReadingResource`.
    *   *Commit: "Implement fetching and posting sensor historical readings"*
*   **Day 15:** 
    *   (Part 4) Implement the programmatic side effect: Update parent Sensor's `currentValue` when a new reading is POSTed.
    *   Answer **Part 4 questions** (Sub-Resource architectural benefits) in `README.md`.
    *   *Commit: "Handle parent sensor updating and draft Part 4 answers"*

---

### Week 4: Error Handling, Logging, & Final Polish

*   **Day 16:** 
    *   (Part 5) Create Custom Exceptions and Mappers for HTTP 409 (Conflict on Room deletion) and HTTP 422 (Unprocessable Entity on bad room foreign key).
    *   *Commit: "Add 409 and 422 exception mappers"*
*   **Day 17:** 
    *   (Part 5) Create Exception Mapper for HTTP 403 (Forbidden on MAINTENANCE sensor).
    *   Implement Global Catch-All (`Throwable`) Mapper for HTTP 500.
    *   *Commit: "Add 403 exception mapper and global 500 error handler"*
*   **Day 18:** 
    *   (Part 5) Implement JAX-RS API Observability filters (`ContainerRequestFilter`, `ContainerResponseFilter`) manually using `java.util.logging.Logger`.
    *   Answer **Part 5 questions** (422 semantics vs 404, security risks of stack traces, and Filter advantages) in `README.md`.
    *   *Commit: "Implement request/response logging filters and draft Part 5 answers"*
*   **Day 19:** 
    *   Finalize `README.md`.
    *   Add build/run instructions and at least 5 sample `curl` test commands demonstrating the API.
    *   *Commit: "Finalize README with build instructions and cURL commands"*
*   **Day 20:** 
    *   Comprehensive end-to-end testing using Postman (to prepare for the video demonstration).
    *   Code clean-up, review against coursework constraints (making sure nothing resembles Spring Boot or external databases).
    *   *Commit: "Final code polish before submission"*
