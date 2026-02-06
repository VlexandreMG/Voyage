package affichage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import base.ConnexionPostgres;
import modele.Route;
import modele.Obstacles;
import modele.Ville;

public class FormulaireObstacle extends JPanel {
    private JComboBox<RouteItem> comboRoutes;
    private JComboBox<VilleItem> comboVilleDebut;
    private JComboBox<VilleItem> comboVilleFin;
    private JTextField txtDistanceDebut;
    private JTextField txtDistanceFin;
    private JButton btnAjouter;
    private JButton btnAnnuler;
    private Map map;
    private List<Route> routes;
    private JLabel lblInfoDistances;
    private JLabel lblDistanceVilleDebut;
    private JLabel lblDistanceVilleFin;
    
    public FormulaireObstacle(Map map) {
        this.map = map;
        this.routes = new ArrayList<>();
        
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Ajouter un Obstacle"));
        setPreferredSize(new Dimension(300, 500));
        
        initComponents();
        chargerRoutes();
    }
    
    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Label et combo pour la route
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(new JLabel("Sélectionner une route:"), gbc);
        
        gbc.gridy = 1;
        comboRoutes = new JComboBox<>();
        comboRoutes.setPreferredSize(new Dimension(250, 25));
        comboRoutes.addActionListener(e -> chargerVilles());
        add(comboRoutes, gbc);
        
        // Ville début
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        add(new JLabel("Ville début:"), gbc);
        
        gbc.gridx = 1;
        comboVilleDebut = new JComboBox<>();
        comboVilleDebut.addActionListener(e -> mettreAJourDistances());
        add(comboVilleDebut, gbc);
        
        // Distance de la ville début par rapport au début de la route
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        lblDistanceVilleDebut = new JLabel(" ");
        lblDistanceVilleDebut.setFont(new Font("Arial", Font.ITALIC, 10));
        lblDistanceVilleDebut.setForeground(Color.BLUE);
        add(lblDistanceVilleDebut, gbc);
        
        // Ville fin
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        add(new JLabel("Ville fin:"), gbc);
        
        gbc.gridx = 1;
        comboVilleFin = new JComboBox<>();
        comboVilleFin.addActionListener(e -> mettreAJourDistances());
        add(comboVilleFin, gbc);
        
        // Distance de la ville fin par rapport au début de la route
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        lblDistanceVilleFin = new JLabel(" ");
        lblDistanceVilleFin.setFont(new Font("Arial", Font.ITALIC, 10));
        lblDistanceVilleFin.setForeground(Color.BLUE);
        add(lblDistanceVilleFin, gbc);
        
        // Info sur les distances
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        lblInfoDistances = new JLabel("<html><i>Les distances sont calculées depuis la ville début</i></html>");
        lblInfoDistances.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfoDistances.setForeground(new Color(100, 100, 100));
        add(lblInfoDistances, gbc);
        
        // Distance début
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        add(new JLabel("<html>Distance début (km)<br/><i>depuis ville début:</i></html>"), gbc);
        
        gbc.gridx = 1;
        txtDistanceDebut = new JTextField(10);
        add(txtDistanceDebut, gbc);
        
        // Distance fin
        gbc.gridx = 0;
        gbc.gridy = 8;
        add(new JLabel("<html>Distance fin (km)<br/><i>depuis ville début:</i></html>"), gbc);
        
        gbc.gridx = 1;
        txtDistanceFin = new JTextField(10);
        add(txtDistanceFin, gbc);
        
        // Boutons
        JPanel panelBoutons = new JPanel(new FlowLayout());
        btnAjouter = new JButton("Ajouter");
        btnAnnuler = new JButton("Annuler");
        
        btnAjouter.addActionListener(e -> ajouterObstacle());
        btnAnnuler.addActionListener(e -> annulerFormulaire());
        
        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnAnnuler);
        
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        add(panelBoutons, gbc);
    }
    
    private void mettreAJourDistances() {
        RouteItem selectedRoute = (RouteItem) comboRoutes.getSelectedItem();
        VilleItem villeDebutItem = (VilleItem) comboVilleDebut.getSelectedItem();
        VilleItem villeFinItem = (VilleItem) comboVilleFin.getSelectedItem();
        
        if (selectedRoute == null || villeDebutItem == null || villeFinItem == null) {
            lblDistanceVilleDebut.setText(" ");
            lblDistanceVilleFin.setText(" ");
            return;
        }
        
        Route route = selectedRoute.getRoute();
        Ville villeDebut = villeDebutItem.getVille();
        Ville villeFin = villeFinItem.getVille();
        
        // Calculer les distances depuis le début de la route
        double distVilleDebut = route.calculerDistanceDepuisDebut(villeDebut);
        double distVilleFin = route.calculerDistanceDepuisDebut(villeFin);
        
        lblDistanceVilleDebut.setText(String.format("📍 %s est à %.2f km du début de la route", 
            villeDebut.getName(), distVilleDebut));
        lblDistanceVilleFin.setText(String.format("📍 %s est à %.2f km du début de la route", 
            villeFin.getName(), distVilleFin));
    }
    
    private void chargerVilles() {
        comboVilleDebut.removeAllItems();
        comboVilleFin.removeAllItems();
        
        RouteItem selectedRoute = (RouteItem) comboRoutes.getSelectedItem();
        if (selectedRoute == null) return;
        
        Route route = selectedRoute.getRoute();
        
        // Trouver les villes sur cette route
        double seuil = 0.1; // Seuil de proximité en degrés
        List<Ville> villesSurRoute = new ArrayList<>();
        
        for (Ville ville : map.getVilles()) {
            for (double[] coord : route.getCoordinates()) {
                double distance = Math.sqrt(
                    Math.pow(ville.getLongitude() - coord[0], 2) +
                    Math.pow(ville.getLatitude() - coord[1], 2)
                );
                
                if (distance < seuil) {
                    villesSurRoute.add(ville);
                    break;
                }
            }
        }
        
        // Ajouter les villes au ComboBox
        for (Ville ville : villesSurRoute) {
            VilleItem item = new VilleItem(ville);
            comboVilleDebut.addItem(item);
            comboVilleFin.addItem(item);
        }
    }
    
    private void chargerRoutes() {
        Connection conn = null;
        try {
            conn = ConnexionPostgres.getConnexion();
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Impossible de se connecter à la base de données", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String query = "SELECT ogc_fid, name, ref, network, " +
                          "ST_AsText(wkb_geometry) as geom_text, " +
                          "ST_Length(wkb_geometry::geography) / 1000.0 as distance_km " +
                          "FROM public.routes_mada ORDER BY name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            comboRoutes.removeAllItems();
            routes.clear();
            
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
                    Route.extraireCoordonnees(geomText, route);
                }
                
                routes.add(route);
                
                double distance = route.getDistanceTotale();
                String displayName = route.getName() + 
                    (ref != null ? " (" + ref + ")" : "") + 
                    " - " + String.format("%.1f km", distance);
                comboRoutes.addItem(new RouteItem(route, displayName));
            }
            
            rs.close();
            stmt.close();
            
            System.out.println("✅ " + routes.size() + " routes chargées dans le formulaire");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des routes: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            ConnexionPostgres.fermerConnexion(conn);
        }
    }
    
    private void ajouterObstacle() {
        // Validation
        if (comboRoutes.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une route", 
                "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (comboVilleDebut.getSelectedItem() == null || comboVilleFin.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner les villes de début et fin", 
                "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        VilleItem villeDebutItem = (VilleItem) comboVilleDebut.getSelectedItem();
        VilleItem villeFinItem = (VilleItem) comboVilleFin.getSelectedItem();
        Ville villeDebut = villeDebutItem.getVille();
        Ville villeFin = villeFinItem.getVille();
        
        if (villeDebut.equals(villeFin)) {
            JOptionPane.showMessageDialog(this, "Les villes de début et fin doivent être différentes", 
                "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Récupérer la route sélectionnée
        RouteItem selectedItem = (RouteItem) comboRoutes.getSelectedItem();
        Route route = selectedItem.getRoute();
        
        // Calculer les distances des villes par rapport au début de la route
        double distanceVilleDebut = route.calculerDistanceDepuisDebut(villeDebut);
        double distanceVilleFin = route.calculerDistanceDepuisDebut(villeFin);
        
        // Lire les distances relatives entrées par l'utilisateur
        double distDebutRelative, distFinRelative;
        try {
            distDebutRelative = Double.parseDouble(txtDistanceDebut.getText().trim());
            distFinRelative = Double.parseDouble(txtDistanceFin.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Les distances doivent être des nombres valides", 
                "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (distDebutRelative < 0 || distFinRelative < 0) {
            JOptionPane.showMessageDialog(this, "Les distances doivent être positives", 
                "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (distDebutRelative >= distFinRelative) {
            JOptionPane.showMessageDialog(this, "La distance de début doit être inférieure à la distance de fin", 
                "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Calculer les distances absolues par rapport au début de la route
        // distDebut = distance de la ville début + distance relative depuis cette ville
        // distFin = distance de la ville début + distance relative depuis cette ville
        double distDebutAbsolue = distanceVilleDebut + distDebutRelative;
        double distFinAbsolue = distanceVilleDebut + distFinRelative;
        
        // Vérifier que l'obstacle ne dépasse pas la distance totale de la route
        if (distFinAbsolue > route.getDistanceTotale()) {
            JOptionPane.showMessageDialog(this, 
                String.format("L'obstacle dépasse la longueur de la route (%.2f km). Distance fin: %.2f km", 
                    route.getDistanceTotale(), distFinAbsolue), 
                "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Générer un ID unique
        String id = UUID.randomUUID().toString();
        
        // Créer l'obstacle avec les distances absolues
        Obstacles obstacle = new Obstacles(id, route, distDebutAbsolue, distFinAbsolue);
        obstacle.setVilleDebut(villeDebut);
        obstacle.setVilleFin(villeFin);
        
        // Insérer dans la base de données
        if (obstacle.inserer()) {
            // Ajouter à la carte
            map.ajouterObstacle(obstacle);
            
            JOptionPane.showMessageDialog(this, "Obstacle ajouté avec succès!", 
                "Succès", JOptionPane.INFORMATION_MESSAGE);
            
            // Réinitialiser le formulaire
            annulerFormulaire();
        }
    }
    
    private void annulerFormulaire() {
        txtDistanceDebut.setText("");
        txtDistanceFin.setText("");
        if (comboRoutes.getItemCount() > 0) {
            comboRoutes.setSelectedIndex(0);
        }
    }
    
    // Classe interne pour afficher les routes dans le combo
    private class RouteItem {
        private Route route;
        private String display;
        
        public RouteItem(Route route, String display) {
            this.route = route;
            this.display = display;
        }
        
        public Route getRoute() {
            return route;
        }
        
        @Override
        public String toString() {
            return display;
        }
    }
    
    // Classe interne pour afficher les villes dans le combo
    private class VilleItem {
        private Ville ville;
        
        public VilleItem(Ville ville) {
            this.ville = ville;
        }
        
        public Ville getVille() {
            return ville;
        }
        
        @Override
        public String toString() {
            return ville.getName();
        }
    }
}
