package affichage;

import java.awt.*;
import javax.swing.*;

public class Fenetre extends JFrame {

    private Map map;
    private FormulaireObstacle formObstacle;
    private JPanel content;

    public Fenetre() {
        setTitle("Carte de Madagascar - Routes et Villes");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Créer la carte et le formulaire d'obstacles
        map = new Map();
        formObstacle = new FormulaireObstacle(map);
        formObstacle.setVisible(false); // Caché par défaut

        // Panel principal avec bordure
        content = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(map);
        content.add(scrollPane, BorderLayout.CENTER);

        // Barre d'outils en haut
        JToolBar toolbar = new JToolBar("Outils");
        toolbar.setFloatable(false);

        // Bouton pour sélectionner une route
        JButton btnSelectionRoute = new JButton("📍 Afficher une route spécifique");
        btnSelectionRoute.setFont(new Font("Arial", Font.BOLD, 12));
        btnSelectionRoute.setBackground(new Color(0, 123, 255));
        btnSelectionRoute.setForeground(Color.WHITE);
        btnSelectionRoute.setFocusPainted(false);
        btnSelectionRoute.setBorderPainted(false);
        btnSelectionRoute.setPreferredSize(new Dimension(230, 35));
        btnSelectionRoute.addActionListener(e -> ouvrirSelectionRoute());

        // Bouton pour rechercher entre deux villes
        JButton btnRechercheVilles = new JButton("🔍 Rechercher entre deux villes");
        btnRechercheVilles.setFont(new Font("Arial", Font.BOLD, 12));
        btnRechercheVilles.setBackground(new Color(220, 53, 69));
        btnRechercheVilles.setForeground(Color.WHITE);
        btnRechercheVilles.setFocusPainted(false);
        btnRechercheVilles.setBorderPainted(false);
        btnRechercheVilles.setPreferredSize(new Dimension(250, 35));
        btnRechercheVilles.addActionListener(e -> ouvrirRechercheObstacles());

        // Bouton pour afficher tout
        JButton btnToutAfficher = new JButton("🗺️  Afficher tout");
        btnToutAfficher.setFont(new Font("Arial", Font.BOLD, 12));
        btnToutAfficher.setBackground(new Color(40, 167, 69));
        btnToutAfficher.setForeground(Color.WHITE);
        btnToutAfficher.setFocusPainted(false);
        btnToutAfficher.setBorderPainted(false);
        btnToutAfficher.setPreferredSize(new Dimension(150, 35));
        btnToutAfficher.addActionListener(e -> map.afficherTout());

        // Bouton pour afficher les infos obstacles
        JButton btnInfoObstacles = new JButton("ℹ️  Infos obstacles");
        btnInfoObstacles.setFont(new Font("Arial", Font.BOLD, 12));
        btnInfoObstacles.setBackground(new Color(255, 193, 7));
        btnInfoObstacles.setForeground(Color.BLACK);
        btnInfoObstacles.setFocusPainted(false);
        btnInfoObstacles.setBorderPainted(false);
        btnInfoObstacles.setPreferredSize(new Dimension(160, 35));
        btnInfoObstacles.addActionListener(e -> map.toggleInfoObstacles());

        // Bouton pour afficher le formulaire d'insertion
        JButton btnFormObstacle = new JButton("➕ Ajouter obstacle");
        btnFormObstacle.setFont(new Font("Arial", Font.BOLD, 12));
        btnFormObstacle.setBackground(new Color(108, 117, 125));
        btnFormObstacle.setForeground(Color.WHITE);
        btnFormObstacle.setFocusPainted(false);
        btnFormObstacle.setBorderPainted(false);
        btnFormObstacle.setPreferredSize(new Dimension(180, 35));
        btnFormObstacle.addActionListener(e -> toggleFormulaireObstacle());

        // Nouveau bouton pour ouvrir le formulaire multi-obstacles
        JButton btnMultiObstacles = new JButton("➕ Plusieurs obstacles");
        btnMultiObstacles.setFont(new Font("Arial", Font.BOLD, 12));
        btnMultiObstacles.setBackground(new Color(153, 102, 255));     // violet clair par exemple
        btnMultiObstacles.setForeground(Color.WHITE);
        btnMultiObstacles.setFocusPainted(false);
        btnMultiObstacles.setBorderPainted(false);
        btnMultiObstacles.setPreferredSize(new Dimension(200, 35));
        btnMultiObstacles.addActionListener(e -> {
            FormulaireMultiObstacles dialog =  new FormulaireMultiObstacles(this, map.getRoutes());
        dialog.setVisible(true);
    });

        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnMultiObstacles);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnSelectionRoute);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnRechercheVilles);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnToutAfficher);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnInfoObstacles);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnFormObstacle);
        toolbar.add(Box.createHorizontalGlue());

        // Ajouter la toolbar en haut
        add(toolbar, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        setVisible(true);
    }

    private void ouvrirSelectionRoute() {
        FormulaireSelectionRoute dialog = new FormulaireSelectionRoute(this, map, map.getRoutes());
        dialog.setVisible(true);
    }

    private void ouvrirRechercheObstacles() {
        FormulaireRechercheObstacles dialog = new FormulaireRechercheObstacles(this, map, map.getRoutes(), map.getVilles());
        dialog.setVisible(true);
    }

    private void toggleFormulaireObstacle() {
        if (formObstacle.isVisible()) {
            content.remove(formObstacle);
            formObstacle.setVisible(false);
        } else {
            content.add(formObstacle, BorderLayout.EAST);
            formObstacle.setVisible(true);
        }
        content.revalidate();
        content.repaint();
    }
}
