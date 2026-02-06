package affichage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.UUID;
import modele.Route;
import modele.Obstacles;
import base.ConnexionPostgres;

public class FormulaireMultiObstacles extends JDialog {
    private final List<Route> routes;
    private JComboBox<RouteItem> comboRoutes;
    private JLabel lblLongueurRoute;
    private JPanel panelObstacles;
    private JButton btnAjouterLigne;
    private JButton btnValider;

    public FormulaireMultiObstacles(JFrame parent, List<Route> routes) {
        super(parent, "Ajouter plusieurs obstacles à une route", true);
        this.routes = routes;

        setSize(600, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        if (comboRoutes.getItemCount() > 0) {
            comboRoutes.setSelectedIndex(0);
        }
    }

    private void initComponents() {
        // Panel supérieur : sélection route + info longueur
        JPanel panelHaut = new JPanel(new GridBagLayout());
        panelHaut.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panelHaut.add(new JLabel("Route nationale :"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        comboRoutes = new JComboBox<>();
        for (Route r : routes) {
            comboRoutes.addItem(new RouteItem(r));
        }
        comboRoutes.addActionListener(e -> updateInfoRoute());
        panelHaut.add(comboRoutes, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        lblLongueurRoute = new JLabel("Longueur de la route : N/A km");
        lblLongueurRoute.setFont(new Font("Arial", Font.ITALIC, 12));
        panelHaut.add(lblLongueurRoute, gbc);

        add(panelHaut, BorderLayout.NORTH);

        // Panel central : liste dynamique des obstacles
        panelObstacles = new JPanel();
        panelObstacles.setLayout(new BoxLayout(panelObstacles, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(panelObstacles);
        scroll.setBorder(BorderFactory.createTitledBorder("Obstacles à ajouter"));
        add(scroll, BorderLayout.CENTER);

        // Bouton + Ajouter
        btnAjouterLigne = new JButton("+ Ajouter un obstacle");
        btnAjouterLigne.addActionListener(e -> ajouterLigneObstacle());
        JPanel panelAjouter = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelAjouter.add(btnAjouterLigne);
        add(panelAjouter, BorderLayout.EAST);

        // Bouton Valider en bas
        JPanel panelBas = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnValider = new JButton("Valider et insérer dans la base");
        btnValider.addActionListener(e -> validerEtInserer());
        panelBas.add(btnValider);
        add(panelBas, BorderLayout.SOUTH);

        // Ajouter une première ligne par défaut
        ajouterLigneObstacle();
    }

    private void updateInfoRoute() {
        RouteItem item = (RouteItem) comboRoutes.getSelectedItem();
        if (item != null) {
            lblLongueurRoute.setText(String.format("Longueur de la route : %.1f km", item.route.getDistanceTotale()));
        } else {
            lblLongueurRoute.setText("Longueur de la route : N/A km");
        }

        // Vérifier si des lignes non validées existent avant de changer
        if (panelObstacles.getComponentCount() > 0) {
            int choix = JOptionPane.showConfirmDialog(this, 
                    "Changer de route va effacer les obstacles non validés. Voulez-vous valider d'abord ?", 
                    "Confirmation", JOptionPane.YES_NO_CANCEL_OPTION);
            
            if (choix == JOptionPane.YES_OPTION) {
                validerEtInserer();  // Valide et insère avant de vider
            } else if (choix == JOptionPane.CANCEL_OPTION) {
                return;  // Annule le changement de route
            }
            // Si NO ou ferme : on continue et vide
        }

        // Remettre à zéro le formulaire d'obstacles pour la nouvelle route
        panelObstacles.removeAll();
        ajouterLigneObstacle();  // Ajoute une ligne vide par défaut
        panelObstacles.revalidate();
        panelObstacles.repaint();
    }

    private void ajouterLigneObstacle() {
        JPanel ligne = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ligne.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JLabel lblDebut = new JLabel("Début (km) :");
        JTextField txtDebut = new JTextField(8);
        JLabel lblFin = new JLabel("Fin (km) :");
        JTextField txtFin = new JTextField(8);
        JButton btnSupprimer = new JButton("X");
        btnSupprimer.setForeground(Color.RED);
        btnSupprimer.addActionListener(e -> {
            panelObstacles.remove(ligne);
            panelObstacles.revalidate();
            panelObstacles.repaint();
        });

        ligne.add(lblDebut);
        ligne.add(txtDebut);
        ligne.add(lblFin);
        ligne.add(txtFin);
        ligne.add(btnSupprimer);

        panelObstacles.add(ligne);
        panelObstacles.revalidate();
        panelObstacles.repaint();
    }

    private void validerEtInserer() {
        RouteItem item = (RouteItem) comboRoutes.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une route.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Route route = item.route;
        double longueurRoute = route.getDistanceTotale();

        List<Obstacles> obstaclesAInserer = new ArrayList<>();

        Component[] lignes = panelObstacles.getComponents();
        for (Component comp : lignes) {
            if (comp instanceof JPanel) {
                JPanel ligne = (JPanel) comp;
                Component[] champs = ligne.getComponents();

                JTextField txtDebut = (JTextField) champs[1];
                JTextField txtFin = (JTextField) champs[3];

                try {
                    double debut = Double.parseDouble(txtDebut.getText().trim());
                    double fin = Double.parseDouble(txtFin.getText().trim());

                    if (debut < 0 || fin < 0 || debut >= fin || fin > longueurRoute) {
                        JOptionPane.showMessageDialog(this, "Distances invalides : positives, début < fin, fin <= " + longueurRoute + " km.",
                                "Erreur", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Obstacles obs = new Obstacles(UUID.randomUUID().toString(), route, debut, fin);
                    obs.setVilleDebut(null);  // NULL comme demandé
                    obs.setVilleFin(null);    // NULL comme demandé
                    obstaclesAInserer.add(obs);

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Distances doivent être des nombres valides.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        if (obstaclesAInserer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucun obstacle à insérer.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Insertion en base
        boolean succes = true;
        for (Obstacles obs : obstaclesAInserer) {
            if (!obs.inserer()) {
                succes = false;
                break;
            }
        }

        if (succes) {
            JOptionPane.showMessageDialog(this, obstaclesAInserer.size() + " obstacles insérés avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
            // Remettre à zéro après insertion (optionnel, mais utile)
            panelObstacles.removeAll();
            ajouterLigneObstacle();
            panelObstacles.revalidate();
            panelObstacles.repaint();
        } else {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'insertion.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class RouteItem {
        Route route;

        RouteItem(Route r) {
            this.route = r;
        }

        public String toString() {
            return route.getName() + " (" + route.getRef() + ") – " + String.format("%.1f km", route.getDistanceTotale());
        }
    }
}