package affichage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import modele.Route;

public class FormulaireSelectionRoute extends JDialog {
    private JComboBox<RouteItem> comboRoutes;
    private JButton btnAfficher;
    private JButton btnToutAfficher;
    private JButton btnAnnuler;
    private Map map;
    private List<Route> routes;
    
    public FormulaireSelectionRoute(JFrame parent, Map map, List<Route> routes) {
        super(parent, "Sélectionner une Route Nationale", true);
        this.map = map;
        this.routes = routes;
        
        setSize(400, 200);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        initComponents();
    }
    
    private void initComponents() {
        // Panel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel label = new JLabel("Choisir une route nationale :");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(label, gbc);
        
        // ComboBox
        gbc.gridy = 1;
        comboRoutes = new JComboBox<>();
        comboRoutes.setPreferredSize(new Dimension(300, 30));
        
        // Remplir avec les routes
        for (Route route : routes) {
            comboRoutes.addItem(new RouteItem(route));
        }
        
        mainPanel.add(comboRoutes, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Panel des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnAfficher = new JButton("Afficher cette route");
        btnAfficher.setBackground(new Color(0, 123, 255));
        btnAfficher.setForeground(Color.WHITE);
        btnAfficher.setFocusPainted(false);
        btnAfficher.addActionListener(e -> afficherRoute());
        
        btnToutAfficher = new JButton("Tout afficher");
        btnToutAfficher.setBackground(new Color(40, 167, 69));
        btnToutAfficher.setForeground(Color.WHITE);
        btnToutAfficher.setFocusPainted(false);
        btnToutAfficher.addActionListener(e -> toutAfficher());
        
        btnAnnuler = new JButton("Annuler");
        btnAnnuler.addActionListener(e -> dispose());
        
        buttonPanel.add(btnAfficher);
        buttonPanel.add(btnToutAfficher);
        buttonPanel.add(btnAnnuler);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void afficherRoute() {
        RouteItem selectedItem = (RouteItem) comboRoutes.getSelectedItem();
        if (selectedItem != null) {
            Route route = selectedItem.getRoute();
            map.filtrerParRoute(route);
            JOptionPane.showMessageDialog(this, 
                "Affichage de : " + route.getName(), 
                "Route sélectionnée", 
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }
    
    private void toutAfficher() {
        map.afficherTout();
        JOptionPane.showMessageDialog(this, 
            "Affichage de toutes les routes et obstacles", 
            "Vue complète", 
            JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
    
    // Classe interne pour afficher les routes dans le combo
    private class RouteItem {
        private Route route;
        
        public RouteItem(Route route) {
            this.route = route;
        }
        
        public Route getRoute() {
            return route;
        }
        
        @Override
        public String toString() {
            double distance = route.getDistanceTotale();
            return route.getName() + (route.getRef() != null ? " (" + route.getRef() + ")" : "") + 
                   " - " + String.format("%.1f km", distance);
        }
    }
}
