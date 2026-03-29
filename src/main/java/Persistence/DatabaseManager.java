package Persistence;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.RunScript;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private static DatabaseManager instance;
    private JdbcConnectionPool connectionPool;

    // URL with AUTO_SERVER=TRUE for simultaneous access
    private static final String DB_URL = "jdbc:h2:file:./data/eventify_db;AUTO_SERVER=TRUE";

    /*private DatabaseManager() {
        try {
            connectionPool = JdbcConnectionPool.create(DB_URL, "sa", "");
            try (Connection conn = connectionPool.getConnection()) {
                RunScript.execute(conn, new FileReader("src/main/resources/schema.sql"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Fatal DB error: " + e.getMessage(), e);
        }
    }*/

    private void initSchema() {
        try (Connection conn = connectionPool.getConnection()) {

            java.net.URL scriptUrl = getClass().getResource("/schema.sql");
            if (scriptUrl == null) {
                throw new RuntimeException("FATAL: schema.sql não encontrado nos resources!");
            }

            RunScript.execute(conn, new java.io.InputStreamReader(scriptUrl.openStream()));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao inicializar/limpar a BD: " + e.getMessage());
        }
    }

    private DatabaseManager(String url) {
        try {
            connectionPool = JdbcConnectionPool.create(url, "sa", "");
            initSchema();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing the database", e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager(DB_URL);
        return instance;
    }

    public void close() {
        connectionPool.dispose();
        instance = null;
    }

    public Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    public static synchronized void initTestDatabase() {
        if (instance != null) {
            instance.close();
        }
        instance = new DatabaseManager("jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1");
    }
}
