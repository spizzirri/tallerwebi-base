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
      "DELETE FROM plan_join_requests",
      "DELETE FROM plan_invitations",
      "DELETE FROM plan_editors",
      "DELETE FROM plan_participants",
      "DELETE FROM plan_ratings",
      // [CAL] Las calificaciones referencian plan y user: las borro primero para no dejar la
      // limpieza atrapada por la FK que introdujo 16-CAL.md (mismo cuidado que plan_places).
      "DELETE FROM plan_places",
      // [12-DATA] places no se borra: PlaceDataSeeder solo corre una vez por JVM, y el server
      // arranca antes que cualquier test, asi que un DELETE aca dejaria la base de E2E sin
      // dataset para siempre. El ABM E2E limpia el lugar que crea (E-06); el seed queda intacto.
      "DELETE FROM plans",
      // [FOLLOW] La relacion follower/followed apunta a users: sin este DELETE, borrar users
      // falla por FK en cuanto un E2E dejo una relacion (el test siguiente moriria en el reset,
      // no en el flujo).
      "DELETE FROM follows",
      "DELETE FROM users",
      // [12-DATA] ResetDatabase habla SQL directo: los nombres mapean a firstname/lastname,
      // tal como Hibernate creó la tabla. [PROF] El perfil público muestra el nombre del
      // dueño: sin nombres, la vista cae al círculo de iniciales y los E2E de perfil no
      // tienen "Admin Test" qué mirar. El id es un UUIDv7 fijo: desde [UUID] la PK ya no la
      // genera la secuencia y el seed necesita su propio valor.
      "INSERT INTO users(id, email, password, role, active, firstname, lastname) " +
      "VALUES('0192f0a1-0000-7000-8000-0000000000aa', 'test@unlam.edu.ar', '" +
      bcryptHash +
      "', 'ADMIN', TRUE, 'Admin', 'Test') ON CONFLICT (email) DO NOTHING",
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
