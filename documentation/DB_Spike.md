# Spike Documentation: Persistence Architecture and State Migration for Eventify

This document constitutes the technical analysis and execution plan for the database integration "Spike". The objective is to migrate the **Eventify** system, currently based on volatile state management (in-memory lists within *Singleton* classes), to a robust file-based persistence infrastructure.

## 1. Introduction and Strategic Context

The current architecture, while functional for rapid prototyping, presents significant technical debt: data loss upon application restart and strong coupling due to the use of static Singletons. The proposed solution aims to introduce the **DAO (Data Access Object)** pattern and the **H2** database engine, ensuring data integrity between the User and Event entities without overly complicating the university development environment.

## 2. Comparative Analysis: Database Engine Selection

To support the technological decision, we analyzed three predominant candidates in the Java ecosystem, evaluating the balance between functionality and ease of configuration ("Zero-Config").

### 2.1 The Engine Landscape

| **Characteristic** | **MySQL (Client-Server)** | **SQLite (Embedded C)** | **H2 Database (Embedded Java)** |
| --- | --- | --- | --- |
| **Architecture** | Heavy external process | Native C library | **Native Java (JAR)** |
| --- | --- | --- | --- |
| **Installation** | Requires server installation | Requires JNI binaries | **Single Maven dependency** |
| --- | --- | --- | --- |
| **Portability** | Low (network configuration) | Medium (OS dependent) | **High (Platform Independent)** |
| --- | --- | --- | --- |
| **Typing** | Strict | Dynamic (Weak) | **Strict (SQL Standard Compliant)** |
| --- | --- | --- | --- |

### 2.2 Justification for Choice: H2 Database

**H2** was selected as the ideal solution for this project due to its hybrid nature and native integration with the JVM:

*   **File-Based Mode:** Allows for real persistence on disk, meeting the requirement of maintaining data between restarts.
*   **Zero Infrastructure:** Does not require each team member to install a server. Maven manages the engine.
*   **Auto Server Mode:** The `AUTO_SERVER=TRUE` functionality allows the Java application and an administration tool (DBeaver/H2 Console) to access the database file simultaneously.

## 3. Infrastructure and Database Configuration

This section details the technical configuration required to support persistence.

### 3.1 Maven Dependencies (pom.xml)

To enable H2 and unit tests, add to `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.2.224</version>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 3.2 Schema Definition (`schema.sql`)

Instead of scattering SQL strings throughout the Java code, we will centralize the database structure definition in a resource file (`src/main/resources/schema.sql`).

```sql
-- USERS Table
CREATE TABLE IF NOT EXISTS USERS (
    id INT PRIMARY KEY AUTO_INCREMENT,
    firstName VARCHAR(255) NOT NULL,
    lastName VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    bio CLOB,
    interests_csv VARCHAR(1000), -- Lists saved as CSV
    eventTypes_csv VARCHAR(1000)
);

-- EVENTS Table
CREATE TABLE IF NOT EXISTS EVENTS (
    id INT PRIMARY KEY AUTO_INCREMENT,
    creatorID INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description CLOB,
    date TIMESTAMP,
    location VARCHAR(255),
    eventType VARCHAR(100),
    maxAttendees INT,
    interests_csv VARCHAR(1000),
    FOREIGN KEY (creatorID) REFERENCES USERS(id) ON DELETE CASCADE
);
    -- ...
```

### 3.3 The Connection Manager (`DatabaseManager.java`)

We will use a **Singleton** that manages a `JdbcConnectionPool` and initializes the database by reading the `schema.sql` file on startup.

```java
public class DatabaseManager {
    private static DatabaseManager instance;
    private JdbcConnectionPool connectionPool;

    // URL with AUTO_SERVER=TRUE for simultaneous access
    private static final String DB_URL = "jdbc:h2:file:./data/eventify_db;AUTO_SERVER=TRUE";

    private DatabaseManager() {
        try {
            connectionPool = JdbcConnectionPool.create(DB_URL, "sa", "");
            try (Connection conn = connectionPool.getConnection()) {
                RunScript.execute(conn, new FileReader("src/main/resources/schema.sql"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Fatal DB error: " + e.getMessage(), e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }
}
```

## 4. Architectural Refactoring: From Singleton to DAO

This is the critical part of the migration. The `UserService` will no longer manage in-memory lists and will delegate persistence to a DAO.

### 4.1 The DAO Pattern: Interface and Implementation (User Example)

#### The Interface (Contract)

```java
public interface UserDAO {
    void save(User user);
    User findByEmail(String email);
    boolean validateLogin(String email, String password);
}
```

#### The JDBC Implementation (H2)

Uses the `DatabaseManager` to get connections and `PreparedStatement` for security.

```java
public class JdbcUserDAO implements UserDAO {
    @Override
    public void save(User user) {
        String sql = "INSERT INTO USERS (firstName, email, ...) VALUES (?, ?, ...)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // ... setStrings ...
            stmt.executeUpdate();
            // ... retrieve generated keys (ID) ...
        } catch (SQLException e) {
            throw new RuntimeException("SQL Error", e);
        }
    }
    // ...
}
```

### 4.2 Service Refactoring (UserService)

The `UserService` becomes a pure orchestrator that receives the DAO via dependency injection (constructor), eliminating the use of static lists.

```java
public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new JdbcUserDAO(); // Injection
    }

    public User registerUser(User u) {
        if (userDAO.findByEmail(u.getEmail()) != null) throw new IllegalArgumentException("Email exists");
        userDAO.save(u);
        return u;
    }
}
```

### 4.3 Extension to the Events Entity (`EventDAO`)

The same architecture was replicated for event management, ensuring code consistency:

1.  **`EventDAO` (Interface):** Defines operations like `save(Event e)`, `findAll()`, and `findByCreator(int userId)`.
2.  **`JdbcEventDAO` (Implementation):** Implements persistence with two additional technical considerations compared to the user:
    *   **Foreign Key Management:** Ensures the referential integrity of the `creatorID` when inserting events.
    *   **Temporal Conversion:** Handles the necessary bidirectional conversion between `java.time.LocalDateTime` (used in the Event class) and `java.sql.Timestamp` (used by H2).

In this way, the `EventService` also ceases to be an in-memory Singleton and now orchestrates business logic over the `JdbcEventDAO`.

## 5. Conclusion

The implementation of `DatabaseManager` with `JdbcConnectionPool` centralizes the complexity of resource management, allowing the DAOs (`UserDAO` and `EventDAO`) to focus solely on executing SQL. The use of `schema.sql` makes database versioning transparent and facilitates the installation of the project on new machines, eliminating the need for manual server configuration.
