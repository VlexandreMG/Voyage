package test;

import java.sql.*;
import base.ConnexionPostgres;

public class VerifierDonnees {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = ConnexionPostgres.getConnexion();
            
            // Vérifier les colonnes de routes_nationales
            String query1 = "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'routes_nationales'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query1);
            
            System.out.println("📊 Colonnes de routes_nationales:");
            while (rs.next()) {
                System.out.println("  - " + rs.getString("column_name") + " (" + rs.getString("data_type") + ")");
            }
            rs.close();
            
            // Compter les routes
            String query2 = "SELECT COUNT(*) as nb FROM routes_nationales";
            rs = stmt.executeQuery(query2);
            if (rs.next()) {
                System.out.println("\n🛣️  Nombre de routes: " + rs.getInt("nb"));
            }
            rs.close();
            
            // Chercher des tables avec "ville" ou "city"
            String query3 = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND (table_name LIKE '%ville%' OR table_name LIKE '%city%' OR table_name LIKE '%town%' OR table_name LIKE '%place%')";
            rs = stmt.executeQuery(query3);
            System.out.println("\n🏙️  Tables de villes:");
            while (rs.next()) {
                System.out.println("  - " + rs.getString("table_name"));
            }
            rs.close();
            
            stmt.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ConnexionPostgres.fermerConnexion(conn);
        }
    }
}
