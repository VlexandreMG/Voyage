package affichage;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        System.out.println("🗺️  Démarrage de l'application Carte de Madagascar...");
        
        // Lancer l'interface graphique dans le thread Swing
        SwingUtilities.invokeLater(() -> {
            new Fenetre();
        });
    }
}
