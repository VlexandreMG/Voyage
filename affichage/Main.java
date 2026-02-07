package affichage;

import javax.swing.SwingUtilities;
import modele.Commune;
import service.SerCommune;

public class Main {

    public static void main(String[] args) {
        System.out.println("🗺️  Démarrage de l'application Carte de Madagascar...");

        //Test du nouveau sujet 
        Commune nouvelle = new Commune();
        nouvelle.setName("Ambohimalaza");
        nouvelle.setLongitude(47.5234);
        nouvelle.setLatitude(-18.9123);
        nouvelle.setPopulation(4500);

        long idCree = SerCommune.ajouterCommuneAvecAssociations(nouvelle);

        if (idCree > 0) {
            System.out.println("Succès ! ID = " + idCree);
        } else {
            System.out.println("Échec de l'ajout");
        }

        // Lancer l'interface graphique dans le thread Swing
        SwingUtilities.invokeLater(() -> {
            new Fenetre();
        });
    }
}
