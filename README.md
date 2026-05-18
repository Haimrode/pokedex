# PokéDex App — Atelier Développement Mobile (EPSI)

Application Android native en Kotlin consommant l'API publique [PokéAPI](https://pokeapi.co) pour afficher un Pokédex interactif avec gestion de favoris persistés localement.

> **Projet réalisé dans le cadre du cours de Développement Mobile à l'EPSI.**

---

## Sommaire

- [Fonctionnalités](#fonctionnalités)
- [Stack technique](#stack-technique)
- [Architecture](#architecture)
- [Structure du projet](#structure-du-projet)
- [Installation et lancement](#installation-et-lancement)
- [Captures d'écran](#captures-décran)
- [Difficultés rencontrées](#difficultés-rencontrées)
- [Auteur](#auteur)

---

## Fonctionnalités

- [ ] Liste des 100 premiers Pokémons (sprite, numéro Pokédex, nom, types)
- [ ] Filtrage local par nom (insensible à la casse) et par type
- [ ] Fiche détaillée par Pokémon (taille, poids, stats de base)
- [ ] Ajout / retrait des favoris depuis la fiche détail
- [ ] Onglet Favoris persisté localement avec mise à jour réactive
- [ ] Navigation par Bottom Navigation Bar (2 onglets)

> Le statut des fonctionnalités sera mis à jour au fur et à mesure de l'avancement.

---

## Stack technique

| Catégorie | Librairie | Rôle |
|---|---|---|
| **Langage** | Kotlin | Langage principal |
| **UI** | Jetpack Compose + Material 3 | Construction des écrans |
| **Architecture** | MVVM + Clean Architecture | Séparation des responsabilités |
| **DI** | Hilt | Injection de dépendances |
| **Réseau** | Retrofit + OkHttp + Logging Interceptor | Consommation de l'API REST |
| **Sérialisation** | Gson (ou Moshi) | Parsing JSON ↔ objets Kotlin |
| **Images** | Coil | Chargement asynchrone des sprites |
| **Base de données** | Room | Persistance des favoris |
| **Asynchrone** | Coroutines + Flow / StateFlow | Programmation réactive |
| **Navigation** | Jetpack Navigation Compose | Navigation entre écrans |

### Versions et environnement

- **Android Studio** : 2025.3.4 (Narwhal)
- **Compile SDK** : 36
- **Min SDK** : 24 (Android 7.0 Nougat)
- **Target SDK** : 36
- **Java** : 11

---

## Architecture

Le projet suit le pattern **MVVM** combiné à une **Clean Architecture** en 3 couches, comme imposé par le cahier des charges.

```
┌─────────────────────────────────────────┐
│           PRESENTATION (UI)             │
│   Composables + ViewModels + StateFlow  │
└──────────────────┬──────────────────────┘
                   │ observe
┌──────────────────▼──────────────────────┐
│              DOMAIN                     │
│   Models + Repository interfaces +      │
│              UseCases                   │
└──────────────────┬──────────────────────┘
                   │ implémenté par
┌──────────────────▼──────────────────────┐
│               DATA                      │
│   Retrofit API + Room DB + Repository   │
│             implementations             │
└─────────────────────────────────────────┘
```

### Choix architecturaux justifiés

- **MVVM** : séparation entre l'UI (Compose) et la logique de présentation (ViewModels). Permet aux ViewModels de survivre aux rotations d'écran et d'être testables sans le framework Android.
- **Clean Architecture** : la couche `domain` ne dépend d'aucun framework, ce qui la rend portable et facilement testable. Les `UseCases` encapsulent les règles métier réutilisables.
- **Repository pattern** : abstraction des sources de données (réseau + BDD locale). L'interface vit en `domain/`, l'implémentation en `data/`, permettant un swap facile (mock pour les tests, par exemple).
- **Hilt** : génère le code d'injection de dépendances à la compilation, plus performant que Dagger pur et bien intégré à Android.
- **StateFlow** : exposition d'états UI immuables et observables côté Compose, parfait pour le pattern unidirectionnel data flow.
- **Sealed class `UiState<T>`** : modélise explicitement les trois états possibles d'une opération asynchrone (Loading / Success / Error) et force l'UI à les gérer tous.

---

## Structure du projet

```
com.example.pokedex/
├── data/
│   ├── remote/        ← Retrofit API + DTOs
│   ├── local/         ← Room Entities, DAOs, Database
│   └── repository/    ← Implémentations des repositories
├── domain/
│   ├── model/         ← Modèles métier (Pokemon, PokemonDetail)
│   ├── repository/    ← Interfaces des repositories
│   └── usecase/       ← Use cases (GetPokemonListUseCase, ToggleFavoriteUseCase…)
├── presentation/
│   ├── list/          ← Écran liste Pokédex + ViewModel
│   ├── detail/        ← Fiche détail Pokémon + ViewModel
│   ├── favorites/     ← Liste favoris + ViewModel
│   └── navigation/    ← Configuration de la navigation
├── di/                ← Modules Hilt
└── ui/theme/          ← Thème Material 3
```

---

## Installation et lancement

### Prérequis
- Android Studio Narwhal (2025.3+) ou plus récent
- JDK 11 ou supérieur
- Un émulateur Android API 26+ ou un appareil physique en mode développeur

### Étapes

1. Cloner le dépôt :
   ```bash
   git clone <url-du-repo>
   cd "developpement mobile"
   ```
2. Ouvrir le dossier dans Android Studio.
3. Laisser Gradle synchroniser les dépendances (peut prendre quelques minutes au premier build).
4. Sélectionner un device/émulateur dans la barre du haut.
5. Cliquer sur **Run** (▶) ou `Shift + F10`.

### Build de l'APK debug

```bash
./gradlew assembleDebug
```

L'APK sera généré dans `app/build/outputs/apk/debug/app-debug.apk`.

---

## Captures d'écran

_À ajouter en fin de projet._

| Liste Pokédex | Fiche détail | Favoris |
|---|---|---|
| _screenshot_ | _screenshot_ | _screenshot_ |

---

## Difficultés rencontrées

_Section à remplir au fur et à mesure du développement._

---

## Auteurs

Projet réalisé en binôme :
- **Alexandre S.**
- **Pierre**

## Répartition des tâches

| Membre | Responsabilités |
|---|---|
| **Alexandre** | Couche réseau (Retrofit + PokéAPI), `data/remote`, `PokemonRepositoryImpl`, `NetworkModule` (Hilt), use cases liste/détail, écran liste Pokédex + filtres, écran détail Pokémon |
| **Pierre** | Base de données locale (Room), `data/local`, `FavoriteRepositoryImpl`, `DatabaseModule` + `RepositoryModule` (Hilt), use cases favoris, écran Favoris, navigation (Bottom Navigation Bar + NavGraphs), intégration `MainActivity` |

Les modèles métier (`domain/model/`), les interfaces de repository (`domain/repository/`) et la sealed class `UiState` ont été définis ensemble en amont pour garantir des contrats stables entre les deux côtés.

---

## Ressources utilisées

- [Documentation Android Developers](https://developer.android.com)
- [Guide d'architecture Android](https://developer.android.com/topic/architecture)
- [Documentation PokéAPI](https://pokeapi.co/docs/v2)
- [Documentation Retrofit](https://square.github.io/retrofit/)
- [Documentation Hilt](https://dagger.dev/hilt/)
- [Documentation Room](https://developer.android.com/training/data-storage/room)
- [Documentation Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Codelabs Android officiels](https://developer.android.com/codelabs)
