package com.valhalla.e2e;

import com.valhalla.config.EnvironmentConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Resets the database to a known state using JDBC directly (no external tools). */
public class ResetDatabase {

  private ResetDatabase() {}

  /** Deletes all users and seeds the default admin user via JDBC. */
  public static void cleanDatabase() {
    requireE2eDatabase();

    String bcryptHash = new BCryptPasswordEncoder().encode("password");
    String[] statements = {
      "DELETE FROM plan_participants",
      "DELETE FROM plan_places",
      "ALTER SEQUENCE plan_places_id_seq RESTART WITH 1",
      "DELETE FROM plans",
      "ALTER SEQUENCE plans_id_seq RESTART WITH 1",
      "DELETE FROM users",
      "ALTER SEQUENCE users_id_seq RESTART WITH 1",
      "INSERT INTO users(email, password, role, active) " +
      "VALUES('test@unlam.edu.ar', '" +
      bcryptHash +
      "', 'ADMIN', TRUE) ON CONFLICT (email) DO NOTHING",
    };

    try (Connection connection = openConnection()) {
      for (String sql : statements) {
        try (Statement statement = connection.createStatement()) {
          statement.execute(sql);
        }
      }
      System.out.println("Database cleaned successfully");
    } catch (SQLException e) {
      // Swallowing this left the suite green against a dirty database: a foreign key added by a
      // later task makes DELETE FROM users throw, and every test then ran on stale rows.
      throw new IllegalStateException("Could not reset the E2E database: " + e.getMessage(), e);
    }
  }

  /**
   * Refuses to wipe anything that is not recognisably an E2E database. The E2E suite has no
   * database of its own yet, so it points at whatever DB_NAME says -- on a developer machine that
   * is usually the same database the app is being developed against, and DELETE FROM users there
   * destroys real work.
   */
  private static void requireE2eDatabase() {
    String database = EnvironmentConfig.dbName().toLowerCase();
    if (!database.contains("e2e")) {
      throw new IllegalStateException(
        "Refusing to reset database '" +
        EnvironmentConfig.dbName() +
        "': its name does not contain 'e2e'. Point DB_NAME at an E2E database, or rename" +
        " the one in use, before running the E2E suite."
      );
    }
  }

  private static Connection openConnection() throws SQLException {
    java.util.Properties props = new java.util.Properties();
    props.setProperty("user", EnvironmentConfig.dbUser());
    props.setProperty("password", EnvironmentConfig.dbPassword());
    EnvironmentConfig.pinTimeZoneToUtc();
    return DriverManager.getConnection(EnvironmentConfig.databaseUrl(), props);
  }
}
