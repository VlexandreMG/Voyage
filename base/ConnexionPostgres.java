package base;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnexionPostgres {

    private static final String URL = "jdbc:postgresql://172.17.0.1:5432/roads_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    // Retourne une nouvelle Connection propre à chaque appel
    public static Connection getConnexion() {
        try {
            // Charger le driver PostgreSQL
            Class.forName("org.postgresql.Driver");

            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
            con.setAutoCommit(true);
            return con;

        } catch (ClassNotFoundException e) {
            System.err.println("⚠️ Pilote PostgreSQL introuvable. Vérifie postgresql-*.jar");
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(
                "Erreur lors de la connexion à la base PostgreSQL : " + e.getMessage(), e
            );
        }
    }

    // Utilitaire pour fermer une connexion
    public static void fermerConnexion(Connection c) {
        if (c != null) {
            try {
                if (!c.isClosed()) c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
