package affichage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import modele.Route;
import modele.Ville;
import modele.Obstacles;

public class FormulaireRechercheObstacles extends JDialog {
    private Map map;
    private List<Route> routes;
    private List<Ville> villes;
    
    private JComboBox<RouteItem> comboRoutes;
    private JComboBox<VilleItem> comboVilleDepart;
    private JComboBox<VilleItem> comboVilleArrivee;
    private JLabel lblDistance;
    private JButton btnRechercher;
    private JButton btnAnnuler;
    
    public FormulaireRechercheObstacles(JFrame parent, Map map, List<Route> routes, List<Ville> villes) {
        super(parent, "Recherche d'obstacles entre deux villes", true);
        this.map = map;
        this.routes = routes;
        this.villes = villes;
        
        initComponents();
        setSize(500, 300);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Panel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Titre
        JLabel titre = new JLabel("🔍 Rechercher les obstacles entre deux villes");
        titre.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titre, gbc);
        
        gbc.gridwidth = 1;
        
        // Route
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblRoute = new JLabel("Route nationale:");
        lblRoute.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(lblRoute, gbc);
        
        gbc.gridx = 1;
        comboRoutes = new JComboBox<>();
        for (Route route : routes) {
            comboRoutes.addItem(new RouteItem(route));
        }
        comboRoutes.addActionListener(e -> chargerVillesDeLaRoute());
        mainPanel.add(comboRoutes, gbc);
        
        // Ville de départ
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblDepart = new JLabel("Ville de départ:");
        lblDepart.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(lblDepart, gbc);
        
        gbc.gridx = 1;
        comboVilleDepart = new JComboBox<>();
        comboVilleDepart.addActionListener(e -> calculerDistance());
        mainPanel.add(comboVilleDepart, gbc);
        
        // Ville d'arrivée
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblArrivee = new JLabel("Ville d'arrivée:");
        lblArrivee.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(lblArrivee, gbc);
        
        gbc.gridx = 1;
        comboVilleArrivee = new JComboBox<>();
        comboVilleArrivee.addActionListener(e -> calculerDistance());
        mainPanel.add(comboVilleArrivee, gbc);
        
        // Distance entre les villes
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        lblDistance = new JLabel("Sélectionnez deux villes pour voir la distance");
        lblDistance.setFont(new Font("Arial", Font.ITALIC, 11));
        lblDistance.setForeground(new Color(100, 100, 100));
        mainPanel.add(lblDistance, gbc);
        
        gbc.gridwidth = 1;
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Panel boutons
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        btnRechercher = new JButton("🔍 Rechercher les obstacles");
        btnRechercher.setBackground(new Color(0, 123, 255));
        btnRechercher.setForeground(Color.WHITE);
        btnRechercher.setFocusPainted(false);
        btnRechercher.addActionListener(e -> rechercherObstacles());
        panelBoutons.add(btnRechercher);
        
        btnAnnuler = new JButton("Annuler");
        btnAnnuler.addActionListener(e -> dispose());
        panelBoutons.add(btnAnnuler);
        
        add(panelBoutons, BorderLayout.SOUTH);
        
        // Charger les villes de la première route
        if (comboRoutes.getItemCount() > 0) {
            chargerVillesDeLaRoute();
        }
    }
    
    private void chargerVillesDeLaRoute() {
        comboVilleDepart.removeAllItems();
        comboVilleArrivee.removeAllItems();
        
        RouteItem selectedRoute = (RouteItem) comboRoutes.getSelectedItem();
        if (selectedRoute == null) return;
        
        Route route = selectedRoute.route;
        List<Ville> villesSurRoute = trouverVillesSurRoute(route);
        
        for (Ville ville : villesSurRoute) {
            VilleItem item = new VilleItem(ville);
            comboVilleDepart.addItem(item);
            comboVilleArrivee.addItem(item);
        }
        
        System.out.println("📍 " + villesSurRoute.size() + " villes trouvées sur " + route.getName());
    }
    
    private void calculerDistance() {
        VilleItem villeDepart = (VilleItem) comboVilleDepart.getSelectedItem();
        VilleItem villeArrivee = (VilleItem) comboVilleArrivee.getSelectedItem();
        RouteItem routeItem = (RouteItem) comboRoutes.getSelectedItem();
        
        if (villeDepart == null || villeArrivee == null || routeItem == null) {
            lblDistance.setText("Sélectionnez deux villes pour voir la distance");
            lblDistance.setForeground(new Color(100, 100, 100));
            return;
        }
        
        if (villeDepart.ville.equals(villeArrivee.ville)) {
            lblDistance.setText("⚠️ Veuillez sélectionner deux villes différentes");
            lblDistance.setForeground(new Color(220, 53, 69));
            return;
        }
        
        double distanceDepart = calculerDistanceSurRoute(routeItem.route, villeDepart.ville);
        double distanceArrivee = calculerDistanceSurRoute(routeItem.route, villeArrivee.ville);
        double distanceEntre = Math.abs(distanceArrivee - distanceDepart);
        
        lblDistance.setText("📏 Distance entre les deux villes: " + String.format("%.1f km", distanceEntre));
        lblDistance.setForeground(new Color(0, 123, 255));
    }
    
    private List<Ville> trouverVillesSurRoute(Route route) {
        List<Ville> villesSurRoute = new ArrayList<>();
        double seuil = 0.1; // Seuil de proximité en degrés (environ 11 km)
        
        for (Ville ville : villes) {
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
        
        return villesSurRoute;
    }
    
    private void rechercherObstacles() {
        VilleItem villeDepart = (VilleItem) comboVilleDepart.getSelectedItem();
        VilleItem villeArrivee = (VilleItem) comboVilleArrivee.getSelectedItem();
        RouteItem routeItem = (RouteItem) comboRoutes.getSelectedItem();
        
        if (villeDepart == null || villeArrivee == null || routeItem == null) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner une route et deux villes.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (villeDepart.ville.equals(villeArrivee.ville)) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner deux villes différentes.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Calculer les distances sur la route
        double distanceDepart = calculerDistanceSurRoute(routeItem.route, villeDepart.ville);
        double distanceArrivee = calculerDistanceSurRoute(routeItem.route, villeArrivee.ville);
        
        // S'assurer que départ < arrivée
        double distMin = Math.min(distanceDepart, distanceArrivee);
        double distMax = Math.max(distanceDepart, distanceArrivee);
        
        // Filtrer la route et afficher les villes
        map.filtrerParRouteAvecVilles(routeItem.route, villeDepart.ville, villeArrivee.ville, distMin, distMax);
        
        dispose();
    }
    
    private double calculerDistanceSurRoute(Route route, Ville ville) {
        List<double[]> coords = route.getCoordinates();
        double distanceTotale = 0.0;
        double distanceMin = Double.MAX_VALUE;
        double distanceResultat = 0.0;
        
        for (int i = 0; i < coords.size() - 1; i++) {
            double[] p1 = coords.get(i);
            double[] p2 = coords.get(i + 1);
            
            // Distance du point actuel à la ville
            double dist = Math.sqrt(
                Math.pow(ville.getLongitude() - p1[0], 2) +
                Math.pow(ville.getLatitude() - p1[1], 2)
            );
            
            if (dist < distanceMin) {
                distanceMin = dist;
                distanceResultat = distanceTotale;
            }
            
            // Calculer la longueur du segment
            double longueurSegment = Math.sqrt(
                Math.pow(p2[0] - p1[0], 2) +
                Math.pow(p2[1] - p1[1], 2)
            ) * 111.0; // Approximation: 1 degré ≈ 111 km
            
            distanceTotale += longueurSegment;
        }
        
        return distanceResultat;
    }
    
    // Classes internes pour les items des ComboBox
    private static class RouteItem {
        Route route;
        
        RouteItem(Route route) {
            this.route = route;
        }
        
        @Override
        public String toString() {
            double distance = route.getDistanceTotale();
            return route.getName() + " (" + route.getRef() + ") - " + 
                   String.format("%.1f km", distance);
        }
    }
    
    private static class VilleItem {
        Ville ville;
        
        VilleItem(Ville ville) {
            this.ville = ville;
        }
        
        @Override
        public String toString() {
            return ville.getName();
        }
    }
}
