package modele;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import base.ConnexionPostgres;

public class Route {
    private int ogcFid;
    private String name;
    private String ref;
    private String network;
    private List<double[]> coordinates; // Liste de [longitude, latitude]
    private double distanceTotale; // Distance en km calculée par PostGIS
    
    public Route(int ogcFid, String name, String ref, String network) {
        this.ogcFid = ogcFid;
        this.name = name;
        this.ref = ref;
        this.network = network;
        this.coordinates = new ArrayList<>();
        this.distanceTotale = 0.0;
    }
    
    // Méthode statique pour charger toutes les routes depuis la base
    public static List<Route> chargerRoutes() {
        List<Route> routes = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = ConnexionPostgres.getConnexion();
            if (conn == null) {
                System.err.println("Impossible de se connecter à la base de données");
                return routes;
            }
            
            String query = "SELECT ogc_fid, name, ref, network, " +
                          "ST_AsText(wkb_geometry) as geom_text, " +
                          "ST_Length(wkb_geometry::geography) / 1000.0 as distance_km " +
                          "FROM public.routes_mada WHERE wkb_geometry IS NOT NULL LIMIT 200";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                int ogcFid = rs.getInt("ogc_fid");
                String name = rs.getString("name");
                String ref = rs.getString("ref");
                String network = rs.getString("network");
                String geomText = rs.getString("geom_text");
                double distanceKm = rs.getDouble("distance_km");
                
                Route route = new Route(ogcFid, name, ref, network);
                route.setDistanceTotale(distanceKm);
                
                // Parser le WKT pour extraire les coordonnées
                if (geomText != null) {
                    extraireCoordonnees(geomText, route);
                }
                
                if (route.getCoordinates().size() > 0) {
                    routes.add(route);
                }
            }
            
            rs.close();
            stmt.close();
            
            System.out.println("🛣️  " + routes.size() + " routes chargées");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des routes : " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnexionPostgres.fermerConnexion(conn);
        }
        
        return routes;
    }
    
    public static void extraireCoordonnees(String wkt, Route route) {
        try {
            // Nettoyer le WKT
            wkt = wkt.replaceAll("MULTILINESTRING\\s*\\(", "")
                     .replaceAll("LINESTRING\\s*\\(", "")
                     .replaceAll("\\)", "")
                     .trim();
            
            // Parser les points
            String[] segments = wkt.split("\\),\\(");
            for (String segment : segments) {
                String[] points = segment.split(",");
                for (String point : points) {
                    String[] coords = point.trim().split("\\s+");
                    if (coords.length >= 2) {
                        try {
                            double lon = Double.parseDouble(coords[0]);
                            double lat = Double.parseDouble(coords[1]);
                            route.addCoordinate(lon, lat);
                        } catch (NumberFormatException e) {
                            // Ignorer les points invalides
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur parsing WKT: " + e.getMessage());
        }
    }
    
    // Surcharge pour extraire dans une liste
    public static void extraireCoordonnees(String wkt, List<double[]> coordonnees) {
        try {
            // Nettoyer le WKT
            wkt = wkt.replaceAll("MULTILINESTRING\\s*\\(", "")
                     .replaceAll("LINESTRING\\s*\\(", "")
                     .replaceAll("\\)", "")
                     .trim();
            
            // Parser les points
            String[] segments = wkt.split("\\),\\(");
            for (String segment : segments) {
                String[] points = segment.split(",");
                for (String point : points) {
                    String[] coords = point.trim().split("\\s+");
                    if (coords.length >= 2) {
                        try {
                            double lon = Double.parseDouble(coords[0]);
                            double lat = Double.parseDouble(coords[1]);
                            coordonnees.add(new double[]{lon, lat});
                        } catch (NumberFormatException e) {
                            // Ignorer les points invalides
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur parsing WKT: " + e.getMessage());
        }
    }
    
    public void addCoordinate(double longitude, double latitude) {
        coordinates.add(new double[]{longitude, latitude});
    }
    
    public void setCoordinates(List<double[]> coords) {
        this.coordinates = coords;
    }
    
    // Getters
    public int getOgcFid() {
        return ogcFid;
    }
    
    public String getName() {
        return name != null ? name : (ref != null ? "Route " + ref : "Route sans nom");
    }
    
    public String getRef() {
        return ref;
    }
    
    public String getNetwork() {
        return network;
    }
    
    public List<double[]> getCoordinates() {
        return coordinates;
    }
    
    public void setDistanceTotale(double distance) {
        this.distanceTotale = distance;
    }
    
    public double getDistanceTotale() {
        return distanceTotale;
    }
    
    /**
     * Calcule la distance depuis le début de la route jusqu'à la ville la plus proche
     * @param ville La ville de référence
     * @return La distance en km depuis le début de la route
     */
    public double calculerDistanceDepuisDebut(Ville ville) {
        Connection conn = null;
        try {
            conn = ConnexionPostgres.getConnexion();
            if (conn == null) {
                return 0.0;
            }
            
            // Requête pour calculer la distance le long de la route jusqu'au point le plus proche de la ville
            // Utiliser ST_GeometryN pour extraire la première ligne si c'est un MULTILINESTRING
            // ST_LineLocatePoint retourne une fraction (0 à 1) de la position sur la ligne
            String query = "WITH line AS (" +
                          "  SELECT CASE " +
                          "    WHEN GeometryType(wkb_geometry) = 'MULTILINESTRING' " +
                          "    THEN ST_GeometryN(wkb_geometry, 1) " +
                          "    ELSE wkb_geometry " +
                          "  END as geom " +
                          "  FROM routes_mada WHERE ogc_fid = ?" +
                          ") " +
                          "SELECT ST_Length(" +
                          "  ST_LineSubstring(" +
                          "    geom, " +
                          "    0, " +
                          "    ST_LineLocatePoint(geom, ST_SetSRID(ST_MakePoint(?, ?), 4326))" +
                          "  )::geography" +
                          ") / 1000.0 as distance_km " +
                          "FROM line";
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, this.ogcFid);
            pstmt.setDouble(2, ville.getLongitude());
            pstmt.setDouble(3, ville.getLatitude());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double distance = rs.getDouble("distance_km");
                rs.close();
                pstmt.close();
                return distance;
            }
            
            rs.close();
            pstmt.close();
            
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul de la distance : " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnexionPostgres.fermerConnexion(conn);
        }
        
        return 0.0;
    }
    
    @Override
    public String toString() {
        return "Route{" + getName() + " (" + coordinates.size() + " points)}";
    }
}