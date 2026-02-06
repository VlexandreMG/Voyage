package test;

import java.sql.*;
import base.ConnexionPostgres;

public class TestConnexion {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            System.out.println("Connexion à la base de données...");
            conn = ConnexionPostgres.getConnexion();
            
            if (conn == null) {
                System.err.println("Impossible de se connecter");
                return;
            }
            
            System.out.println("✅ Connexion réussie!");
            
            // Lister les tables
            String query = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            System.out.println("\n📋 Tables disponibles:");
            while (rs.next()) {
                System.out.println("  - " + rs.getString("table_name"));
            }
            
            rs.close();
            stmt.close();
            
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnexionPostgres.fermerConnexion(conn);
        }
    }
}
