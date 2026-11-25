# Varotra

Application Android (en malgache) pour gérer les ventes de chaque jour : achats d'ingrédients (masonkarena),
ventes, dettes (trosa), remises et bénéfice (tombombarotra), avec rapports par jour / mois / année.

## Compilation
Push sur `main` → GitHub Actions → **Build APK** → Artifacts → `Varotra-APK`.
`app/debug.keystore` est une clé fixe : chaque nouvelle version s'installe par-dessus l'ancienne sans perdre les données.

## Modifier les textes
Tous les mots et phrases sont dans **`app/src/main/assets/teny.txt`** (format `cle = texte`).
Seul le nom sous l'icône est dans `app/src/main/res/values/strings.xml` (`app_name`).

## Données
Stockées localement sur le téléphone (aucun compte requis). Onglet **Hafa** : sauvegarde / restauration en fichier JSON.

## Fiarovana (PIN)
Onglet **Hafa** → kaody PIN 4 isa + rantsan-tanana (tsy voatery). Mijanona voakatona rehefa mivoaka ny app mihoatra ny 30 segondra.
Raha adino ny PIN: « Adino ny kaody PIN ? » mamafa ny angona rehetra (ka manaova backup tsara).

## Tantaran'ny fandoavana
Isaky ny vola voaray (indrindra ny trosa) dia voasoratra miaraka amin'ny daty; bokotra « Tantara » eo amin'ny varotra, na tsindrio ny trosa ao amin'ny tab Trosa.
