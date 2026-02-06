#!/bin/bash

rm -rf out
mkdir out

# Compilation de tous les fichiers Java
echo "=== Compilation des fichiers ==="
javac -cp ".:lib/*" -d out affichage/*.java base/*.java modele/*.java

# Vérification du contenu compilé
echo -e "\n=== Fichiers compilés dans 'out/' ==="
find out -type f -name "*.class" | head -20
echo "... (total: $(find out -type f -name "*.class" | wc -l) fichiers)"

# Vérification des classes principales
echo -e "\n=== Vérification des classes principales ==="
if [ -f "out/affichage/Main.class" ]; then
    echo "✅ Main.class trouvé"
else
    echo "❌ Main.class NON trouvé"
    exit 1
fi

if [ -f "out/affichage/Fenetre.class" ]; then
    echo "✅ Fenetre.class trouvé"
else
    echo "❌ Fenetre.class NON trouvé"
    exit 1
fi

# Exécution de la classe principale
echo -e "\n=== Lancement du jeu ==="
java -cp "out:lib/*" affichage.Main