package repo;

import base.ConnexionPostgres;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import modele.Commune;

public class RepoCommune {

    /**
     * Récupère toutes les communes
     */
    public static List<Commune> getAll() {
        List<Commune> communes = new ArrayList<>();
        Connection conn = null;
        String sql = """
            SELECT id, name, longitude, latitude, population
            FROM communes
            ORDER BY name ASC
            """;

        try {
            conn = ConnexionPostgres.getConnexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Commune c = new Commune();
                c.setId(rs.getString("id"));
                c.setName(rs.getString("name"));
                c.setLongitude(rs.getDouble("longitude"));
                c.setLatitude(rs.getDouble("latitude"));
                c.setPopulation(rs.getInt("population"));
                communes.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAll communes : " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnexionPostgres.fermerConnexion(conn); // Juste pour respecter la méthode, pas de connexion à fermer ici
        }
        return communes;
    }

    /**
     * Insère une nouvelle commune et retourne l'ID généré
     * @param commune L'objet Commune à insérer (sans id)
     * @return l'ID généré ou -1 en cas d'erreur
     */
    public static long insertCommune(Commune commune) {
        long newId = -1;
        String sql = """
            INSERT INTO commune (name, longitude, latitude, population)
            VALUES (?, ?, ?, ?)
            RETURNING id
            """;

        Connection conn = null;
        try {
            conn = ConnexionPostgres.getConnexion();
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, commune.getName());
            stmt.setDouble(2, commune.getLongitude());
            stmt.setDouble(3, commune.getLatitude());
            stmt.setInt(4, commune.getPopulation());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    newId = rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur insertion commune : " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnexionPostgres.fermerConnexion(conn); // Juste pour respecter la méthode, pas de connexion à fermer ici
        }
        return newId;
    }

    /**
     * Trouve toutes les routes nationales à ≤ 5000m d'un point donné
     * @return liste de [rn_id (Long), distance_m (Double)]
     */
    public static List<Object[]> findRNProches(double longitude, double latitude) {
        List<Object[]> result = new ArrayList<>();
        String sql = """
            SELECT 
                r.ogc_fid AS rn_id,
                ROUND(ST_Distance(
                    r.wkb_geometry::geography,
                    ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography
                )::numeric, 2) AS distance_m
            FROM routes_mada r
            WHERE ST_DWithin(
                r.wkb_geometry::geography,
                ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                5000
            )
            ORDER BY distance_m ASC
            """;

        Connection conn = null;
        try {
            conn = ConnexionPostgres.getConnexion();
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setDouble(1, longitude);
            stmt.setDouble(2, latitude);
            stmt.setDouble(3, longitude);
            stmt.setDouble(4, latitude);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{
                        rs.getLong("rn_id"),
                        rs.getDouble("distance_m")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche RN proches : " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnexionPostgres.fermerConnexion(conn); // Juste pour respecter la méthode, pas de connexion à fermer ici
        }
        return result;
    }

    /**
     * Associe une commune à plusieurs RN dans la table de liaison
     */
    public static void associerRNsALaCommune(long communeId, List<Object[]> rnProches) {
        if (rnProches.isEmpty()) return;

        String sql = """
            INSERT INTO rn_commune_sert (rn_id, commune_id, distance_m)
            VALUES (?, ?, ?)
            ON CONFLICT DO NOTHING
            """;

            Connection conn = null;
        try {
            conn = ConnexionPostgres.getConnexion();
            PreparedStatement stmt = conn.prepareStatement(sql);

            for (Object[] assoc : rnProches) {
                stmt.setLong(1, (Long) assoc[0]);
                stmt.setLong(2, communeId);
                stmt.setDouble(3, (Double) assoc[1]);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            System.err.println("Erreur association RN-commune : " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnexionPostgres.fermerConnexion(conn); // Juste pour respecter la méthode, pas de connexion à fermer ici
        }
    }
}