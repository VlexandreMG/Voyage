package repo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import modele.Commune;
import base.ConnexionPostgres;

public class repoCommune {
   
    public static List<Commune> getAll() {
        List<Commune> communes = new ArrayList<>();
        
        String sql = """
            SELECT 
                id,
                name,
                longitude,
                latitude,
                population
            FROM communes
            ORDER BY name ASC
            """;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnexionPostgres.getConnexion();
            if (conn == null) {
                System.err.println("Échec de la connexion à la base de données");
                return communes; // retourne liste vide
            }

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Commune commune = new Commune();
                
                commune.setId(rs.getString("id"));
                commune.setName(rs.getString("name"));
                commune.setLongitude(rs.getDouble("longitude"));
                commune.setLatitude(rs.getDouble("latitude"));
                commune.setPopulation(rs.getInt("population"));

                communes.add(commune);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des communes : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Fermeture des ressources dans l'ordre inverse d'ouverture
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            ConnexionPostgres.fermerConnexion(conn);
        }

        return communes;
    }
}