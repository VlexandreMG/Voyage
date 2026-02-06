package test;

import java.sql.*;
import base.ConnexionPostgres;

public class VerifierStructure {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = ConnexionPostgres.getConnexion();
            Statement stmt = conn.createStatement();
            
            // Colonnes de villes_mada
            String query = "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'villes_mada' ORDER BY ordinal_position";
            ResultSet rs = stmt.executeQuery(query);
            
            System.out.println("📊 Colonnes de villes_mada:");
            while (rs.next()) {
                System.out.println("  - " + rs.getString("column_name") + " (" + rs.getString("data_type") + ")");
            }
            rs.close();
            
            // Compter les villes
            rs = stmt.executeQuery("SELECT COUNT(*) as nb FROM villes_mada");
            if (rs.next()) {
                System.out.println("\n🏙️  Nombre de villes: " + rs.getInt("nb"));
            }
            rs.close();
            
            // Colonnes de routes_mada
            query = "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'routes_mada' ORDER BY ordinal_position";
            rs = stmt.executeQuery(query);
            
            System.out.println("\n📊 Colonnes de routes_mada:");
            while (rs.next()) {
                System.out.println("  - " + rs.getString("column_name") + " (" + rs.getString("data_type") + ")");
            }
            rs.close();
            
            // Compter les routes
            rs = stmt.executeQuery("SELECT COUNT(*) as nb FROM routes_mada");
            if (rs.next()) {
                System.out.println("\n🛣️  Nombre de routes: " + rs.getInt("nb"));
            }
            rs.close();
            
            // Exemple de ville
            rs = stmt.executeQuery("SELECT name, place, ST_AsText(wkb_geometry) as geom FROM villes_mada WHERE name IS NOT NULL LIMIT 1");
            if (rs.next()) {
                System.out.println("\n📍 Exemple de ville:");
                System.out.println("  Nom: " + rs.getString("name"));
                System.out.println("  Type: " + rs.getString("place"));
                System.out.println("  Géométrie: " + rs.getString("geom"));
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
