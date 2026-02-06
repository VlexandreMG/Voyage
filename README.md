# Application de Visualisation Cartographique de Madagascar

Cette application Java affiche les routes et les villes de Madagascar à partir d'une base de données PostgreSQL/PostGIS, comme dans QGIS.

## 📋 Fonctionnalités

- ✅ Affichage des villes de Madagascar avec classification par type
- ✅ Affichage des routes nationales
- ✅ Légende interactive
- ✅ Connexion à PostgreSQL/PostGIS
- ✅ Interface graphique Swing

## 🗺️ Données affichées

### Villes (`villes_mada`)
- **Villes principales** (city) : Points rouges avec nom
- **Villes secondaires** (town) : Points orange
- **Autres localités** : Points bleus

### Routes (`routes_mada`)
- Routes nationales de Madagascar
- Affichées en marron/orange

## 🚀 Utilisation

### Compilation et exécution
```bash
./compile_and_run.sh
```

### Configuration de la base de données

La connexion PostgreSQL est configurée dans `base/ConnexionPostgres.java` :
- **Base de données** : `sig_madagascar`
- **Utilisateur** : `postgres`
- **Mot de passe** : `md5`
- **Port** : `5432`

## 📦 Structure du projet

```
Voyage/
├── affichage/          # Interface graphique
│   ├── Main.java       # Point d'entrée
│   ├── Fenetre.java    # Fenêtre principale
│   └── Map.java        # Composant carte
├── base/               # Connexions base de données
│   ├── ConnexionPostgres.java
│   └── ConnexionOracle.java
├── modele/             # Modèles de données
│   ├── Ville.java      # Modèle de ville
│   └── Route.java      # Modèle de route
├── lib/                # Bibliothèques
│   ├── postgresql-42.7.1.jar
│   └── postgis-jdbc-2.5.1.jar
└── compile_and_run.sh  # Script de compilation
```

## 🔧 Dépendances

- **Java** : JDK 11 ou supérieur
- **PostgreSQL** : 12 ou supérieur
- **PostGIS** : Extension PostgreSQL pour données géospatiales
- **JDBC PostgreSQL** : Driver de connexion
- **PostGIS JDBC** : Support des types géométriques

## 📊 Schéma de base de données

### Table `villes_mada`
```sql
- ogc_fid : ID unique
- name : Nom de la ville
- place : Type de localité (city, town, village, etc.)
- population : Population
- wkb_geometry : Géométrie MULTIPOINT
```

### Table `routes_mada`
```sql
- ogc_fid : ID unique
- name : Nom de la route
- ref : Référence (ex: RN1, RN2)
- network : Réseau routier
- wkb_geometry : Géométrie LINESTRING/MULTILINESTRING
```

## 🎨 Carte affichée

- **Fond** : Bleu clair (océan)
- **Projection** : Coordonnées géographiques (lat/lon)
- **Limites** : Madagascar (43°E - 51°E, 26°S - 11°S)

## 📝 Notes techniques

- Les géométries PostGIS sont converties en WKT (Well-Known Text) pour le parsing
- La projection utilise une transformation linéaire simple
- L'antialiasing est activé pour un meilleur rendu

## 🔍 Tests

Le dossier `test/` contient des utilitaires :
- `TestConnexion.java` : Test de connexion à la base
- `VerifierStructure.java` : Vérification des tables et données
- `VerifierDonnees.java` : Inspection des données

## 📖 Exemples de requêtes

```java
// Charger les villes
SELECT ogc_fid, name, 
       ST_X(ST_Centroid(wkb_geometry)) as lon,
       ST_Y(ST_Centroid(wkb_geometry)) as lat,
       place, population
FROM public.villes_mada 
WHERE wkb_geometry IS NOT NULL;

// Charger les routes
SELECT ogc_fid, name, ref, network,
       ST_AsText(wkb_geometry) as geom_text
FROM public.routes_mada 
WHERE wkb_geometry IS NOT NULL;
```

## 🎯 Améliorations possibles

- [ ] Zoom et déplacement de la carte
- [ ] Filtrage par type de ville/route
- [ ] Affichage d'informations au survol
- [ ] Export de la carte en image
- [ ] Calcul d'itinéraires entre villes
- [ ] Affichage de statistiques

## 👥 Auteur

Projet développé pour l'examen de Mr. Tahina - S3
