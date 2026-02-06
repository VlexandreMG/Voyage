package modele;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import base.ConnexionPostgres;

public class Ville {
    private int ogcFid;
    private String name;
    private double longitude;
    private double latitude;
    private String place;
    private String population;
    
    public Ville(int ogcFid, String name, double longitude, double latitude, String place, String population) {
        this.ogcFid = ogcFid;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.place = place;
        this.population = population;
    }
    
    // Méthode statique pour charger toutes les villes depuis la base
    public static List<Ville> chargerVilles() {
        List<Ville> villes = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = ConnexionPostgres.getConnexion();
            if (conn == null) {
                System.err.println("Impossible de se connecter à la base de données");
                return villes;
            }
            
            String query = "SELECT ogc_fid, name, ST_X(ST_Centroid(wkb_geometry)) as lon, " +
                          "ST_Y(ST_Centroid(wkb_geometry)) as lat, place, population " +
                          "FROM public.villes_mada WHERE wkb_geometry IS NOT NULL";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                int ogcFid = rs.getInt("ogc_fid");
                String name = rs.getString("name");
                double lon = rs.getDouble("lon");
                double lat = rs.getDouble("lat");
                String place = rs.getString("place");
                String population = rs.getString("population");
                
                villes.add(new Ville(ogcFid, name, lon, lat, place, population));
            }
            
            rs.close();
            stmt.close();
            
            System.out.println("🏙️  " + villes.size() + " villes chargées");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des villes : " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnexionPostgres.fermerConnexion(conn);
        }
        
        return villes;
    }
    
    // Getters
    public int getOgcFid() {
        return ogcFid;
    }
    
    public String getName() {
        return name != null ? name : "Ville sans nom";
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public String getPlace() {
        return place;
    }
    
    public String getPopulation() {
        return population;
    }
    
    @Override
    public String toString() {
        return "Ville{" + name + " (" + latitude + ", " + longitude + ")}";
    }
}
