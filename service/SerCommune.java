package service;

import modele.Commune;
import repo.RepoCommune;
import java.util.List;

public class SerCommune {

    /**
     * Logique métier : ajoute une commune + associe les RN à ≤ 5 km si existantes
     * @param nouvelleCommune Objet Commune rempli (sans id)
     * @return l'ID généré de la commune, ou -1 en cas d'erreur
     */
    public static long ajouterCommuneAvecAssociations(Commune nouvelleCommune) {
        // Validation basique (optionnel, mais recommandé)
        if (nouvelleCommune.getName() == null || nouvelleCommune.getName().trim().isEmpty()) {
            System.err.println("Nom de commune requis");
            return -1;
        }
        if (nouvelleCommune.getLongitude() < -180 || nouvelleCommune.getLongitude() > 180 ||
            nouvelleCommune.getLatitude() < -90 || nouvelleCommune.getLatitude() > 90) {
            System.err.println("Coordonnées invalides");
            return -1;
        }

        // Étape 1 : insertion
        long communeId = RepoCommune.insertCommune(nouvelleCommune);
        if (communeId == -1) {
            System.err.println("Échec insertion commune");
            return -1;
        }

        // Étape 2 : recherche RN proches
        List<Object[]> rnProches = RepoCommune.findRNProches(
            nouvelleCommune.getLongitude(),
            nouvelleCommune.getLatitude()
        );

        // Étape 3 : association si pertinent
        if (!rnProches.isEmpty()) {
            RepoCommune.associerRNsALaCommune(communeId, rnProches);
            System.out.println("Commune insérée (ID " + communeId + ") - " + rnProches.size() + " RN associées");
        } else {
            System.out.println("Commune insérée (ID " + communeId + ") - aucune RN dans les 5 km");
        }

        return communeId;
    }

    // Exemple d'utilisation future :
    // SerCommune.ajouterCommuneAvecAssociations(maCommune);
}