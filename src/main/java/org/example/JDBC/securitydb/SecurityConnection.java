package org.example.JDBC.securitydb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The {@code SecurityConnection} class is a utility class responsible for creating JDBC connections
 * to the {@code securitydb} database.
 * This class is typically used by other classes such as {@link SecurityManager} to interact with the
 * security-related data (users and roles) stored in the database. The class defines:
 * <ul>
 *     <li> The JDBC URL pointing to the {@code securitydb} database</li>
 *     <li> Provides {@link #getConnection()} to obtain the active Connection instance</li>
 * </ul>
 *
 * @author MariaMM04
 * @author MamenCortes
 */
public class SecurityConnection {

    private static final String URL = "jdbc:sqlite:src/main/java/org/example/DataBases/Securitydb_try.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error loading SQLite driver for securitydb", e);
        }
    }

    /**
     * Obtains a new JDBC {@link Connection} to the security database. Each call returns a new connection
     * instance that uses the URL defines in the field values.
     *
     * @return      An active Connection to the database
     * @throws SQLException if a database access errors occurs or the URL is invalid
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
