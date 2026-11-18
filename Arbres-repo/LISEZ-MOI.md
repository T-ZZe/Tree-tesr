# Arbres 1.3

## Compiler
GitHub Actions (`.github/workflows/build.yml`) ou `mvn package` -> `target/Arbres-1.3.jar` (Java 25, Paper 26.3).
Supprime l'ancien `config.yml` du plugin avant de relancer.

## Changements 1.3
- Nether : les arbres ne sont plus remplacés (le remplacement coupait les arbres aux bords des chunks et fusionnait les couronnes). On ajoute seulement des lianes pleureuses, shroomlights et gouttes de verrues, sans rien supprimer.
- Plus aucun changement de biome : le biome mort de l'End et la Forêt de cristaux sont retirés. L'End garde les arbres d'os et le sol mort.

Les chunks du Nether déjà générés avec la 1.2 gardent leurs arbres abîmés : supprimer le dossier `DIM-1/region` du monde (après sauvegarde) pour les régénérer.
