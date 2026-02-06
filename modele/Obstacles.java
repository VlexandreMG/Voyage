package modele;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import base.ConnexionPostgres;

public class Obstacles {
    private String id;
    private String idRoute;
    private Route route;
    private double distanceDebut;
    private double distanceFin;
    private Ville villeDebut;
    private Ville villeFin;

    public Obstacles(String id, String idRoute, double distanceDebut, double distanceFin){
        this.id = id;
        this.idRoute = idRoute;
        this.distanceDebut = distanceDebut;
        this.distanceFin = distanceFin;
    }
    
    public Obstacles(String id, Route route, double distanceDebut, double distanceFin){
        this.id = id;
        this.route = route;
        this.idRoute = route != null ? String.valueOf(route.getOgcFid()) : null;
        this.distanceDebut = distanceDebut;
        this.distanceFin = distanceFin;
    }
    
    public Obstacles(Route route, double distanceDebut, double distanceFin){
        this.route = route;
        this.idRoute = route != null ? String.valueOf(route.getOgcFid()) : null;
        this.distanceDebut = distanceDebut;
        this.distanceFin = distanceFin;
    }
    
    // Méthode statique pour charger tous les obstacles depuis la base
    public static List<Obstacles> chargerObstacles(List<Route> routes) {
        List<Obstacles> obstacles = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = ConnexionPostgres.getConnexion();
            if (conn == null) {
                System.err.println("Impossible de se connecter à la base de données");
                return obstacles;
            }
            
            String query = "SELECT id, id_route, distance_debut, distance_fin FROM obstacles";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                String id = rs.getString("id");
                String idRoute = rs.getString("id_route");
                double distDebut = rs.getDouble("distance_debut");
                double distFin = rs.getDouble("distance_fin");
                
                // Trouver la route correspondante
                Route route = trouverRouteParId(routes, idRoute);
                if (route != null) {
                    Obstacles obstacle = new Obstacles(id, route, distDebut, distFin);
                    obstacles.add(obstacle);
                }
            }
            
            rs.close();
            stmt.close();
            
            System.out.println("🚧 " + obstacles.size() + " obstacles chargés");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des obstacles : " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnexionPostgres.fermerConnexion(conn);
        }
        
        return obstacles;
    }
    
    // Méthode pour insérer un obstacle dans la base
    public boolean inserer() {
        Connection conn = null;
        try {
            conn = ConnexionPostgres.getConnexion();
            if (conn == null) {
                System.err.println("Impossible de se connecter à la base de données");
                return false;
            }
            
            String query = "INSERT INTO obstacles (id, id_route, id_ville_debut, id_ville_fin, distance_debut, distance_fin) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, this.id);
            pstmt.setString(2, this.idRoute);
            pstmt.setString(3, this.villeDebut != null ? this.villeDebut.getName() : null);
            pstmt.setString(4, this.villeFin != null ? this.villeFin.getName() : null);
            pstmt.setDouble(5, this.distanceDebut);
            pstmt.setDouble(6, this.distanceFin);
            
            int rows = pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("✅ Obstacle inséré dans la base de données: " + this);
            return rows > 0;
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'insertion de l'obstacle : " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            ConnexionPostgres.fermerConnexion(conn);
        }
    }
    
    private static Route trouverRouteParId(List<Route> routes, String idRoute) {
        if (idRoute == null || routes == null) return null;
        try {
            int ogcFid = Integer.parseInt(idRoute);
            for (Route r : routes) {
                if (r.getOgcFid() == ogcFid) {
                    return r;
                }
            }
        } catch (NumberFormatException e) {
            // Ignorer
        }
        return null;
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getIdRoute() {
        return idRoute;
    }
    
    public Route getRoute() {
        return route;
    }
    
    public double getDistanceDebut() {
        return distanceDebut;
    }
    
    public double getDistanceFin() {
        return distanceFin;
    }
    
    // Setters
    public void setId(String id) {
        this.id = id;
    }
    
    public void setIdRoute(String idRoute) {
        this.idRoute = idRoute;
    }
    
    public void setRoute(Route route) {
        this.route = route;
        this.idRoute = route != null ? String.valueOf(route.getOgcFid()) : null;
    }
    
    public void setDistanceDebut(double distanceDebut) {
        this.distanceDebut = distanceDebut;
    }
    
    public void setDistanceFin(double distanceFin) {
        this.distanceFin = distanceFin;
    }
    
    public Ville getVilleDebut() {
        return villeDebut;
    }
    
    public void setVilleDebut(Ville villeDebut) {
        this.villeDebut = villeDebut;
    }
    
    public Ville getVilleFin() {
        return villeFin;
    }
    
    public void setVilleFin(Ville villeFin) {
        this.villeFin = villeFin;
    }
    
    /**
     * Extrait les coordonnées du segment de route correspondant à cet obstacle
     * @return Liste de coordonnées [longitude, latitude] du segment
     */
    public List<double[]> extraireSegmentObstacle() {
        List<double[]> coordonnees = new ArrayList<>();
        
        if (route == null) return coordonnees;
        
        Connection conn = null;
        try {
            conn = ConnexionPostgres.getConnexion();
            if (conn == null) {
                return coordonnees;
            }
            
            double distanceTotale = route.getDistanceTotale();
            if (distanceTotale <= 0) return coordonnees;
            
            // Calculer les fractions (0 à 1) pour ST_LineSubstring
            double fractionDebut = distanceDebut / distanceTotale;
            double fractionFin = distanceFin / distanceTotale;
            
            // Limiter les fractions entre 0 et 1
            fractionDebut = Math.max(0, Math.min(1, fractionDebut));
            fractionFin = Math.max(0, Math.min(1, fractionFin));
            
            // Requête pour extraire le segment de la route
            String query = "WITH line AS (" +
                          "  SELECT CASE " +
                          "    WHEN GeometryType(wkb_geometry) = 'MULTILINESTRING' " +
                          "    THEN ST_GeometryN(wkb_geometry, 1) " +
                          "    ELSE wkb_geometry " +
                          "  END as geom " +
                          "  FROM routes_mada WHERE ogc_fid = ?" +
                          ") " +
                          "SELECT ST_AsText(ST_LineSubstring(geom, ?, ?)) as segment " +
                          "FROM line";
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, route.getOgcFid());
            pstmt.setDouble(2, fractionDebut);
            pstmt.setDouble(3, fractionFin);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String wkt = rs.getString("segment");
                if (wkt != null) {
                    // Parser le WKT LINESTRING
                    Route.extraireCoordonnees(wkt, coordonnees);
                }
            }
            
            rs.close();
            pstmt.close();
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'extraction du segment d'obstacle : " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnexionPostgres.fermerConnexion(conn);
        }
        
        return coordonnees;
    }
    
    @Override
    public String toString() {
        return "Obstacle{" + id + " sur route " + (route != null ? route.getName() : idRoute) + 
               " de " + distanceDebut + "km à " + distanceFin + "km}";
    }
}
