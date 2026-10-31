# Le Conseil des Sages : API

API Spring Boot (Java 17, PostgreSQL 16) : comptes, sages, et moteur de débat qui interroge Mistral, Gemini et Ollama Cloud.

## Démarrage

Copie d'abord `.env.sample` en `.env`, puis remplis-le : base de données, `JWT_SECRET` (`openssl rand -base64 48`) et clés d'API.

### Tout lancer avec Docker

Les dépôts `be-cds` et `fe-cds` doivent être côte à côte. Depuis `be-cds` :

```bash
docker compose up -d --build
```

| Service | Adresse |
| --- | --- |
| Application | `http://localhost:4200` |
| API et Swagger | `http://localhost:8080/swagger-ui/index.html` |
| PostgreSQL | `localhost:5432` |

`docker compose down` arrête tout en gardant les données. Après une modification du code, relance la commande de démarrage.

### Développer sans reconstruire les images

```bash
docker compose up -d database
mvn spring-boot:run
```

Lance ensuite le front avec `npm start` (voir le README de `fe-cds`).

Compte de démo : `demo@conseil.fr` / `demo1234`.

Le schéma est géré par Flyway (`src/main/resources/db/migration`) : les migrations s'appliquent au démarrage. Pour faire évoluer la base, ajoute un fichier `V6__...sql`, sans jamais modifier un fichier déjà appliqué.

## Fournisseurs de modèles

Une clé vide désactive son fournisseur. Le catalogue se règle dans `application.properties` (`app.llm.models`).

| Fournisseur | Clé | Protocole |
| --- | --- | --- |
| Mistral | `MISTRAL_API_KEY` | `/v1/chat/completions` |
| Gemini | `GEMINI_API_KEY` | API de compatibilité OpenAI |
| Ollama Cloud | `OLLAMA_API_KEY` | `/api/chat` (une ligne JSON par morceau) |

Si un modèle échoue, le routeur réessaie une fois quand l'erreur est temporaire, puis passe au modèle par défaut et aux autres fournisseurs : le message enregistre le modèle réellement utilisé. Les `*_BASE_URL` permettent de pointer vers un autre serveur, par exemple pour les tests.

## Déroulé d'un débat

`POST /api/debates` crée la session, puis `GET /api/debates/{id}/events` (flux SSE) lance ou reprend le débat :

1. Trois tours de parole : Ouverture, Confrontation, Position finale.
2. La synthèse JSON du Greffier.

Le débat tourne en arrière-plan et chaque message est enregistré dès qu'il est complet. Si le navigateur se ferme, le débat continue. Après une panne, il reprend au premier sage qui n'a pas parlé.

Le Greffier vit à côté de ce déroulé (`ScribeWatcher`) : sur son propre fil, il fait une remarque environ toutes les demi-tablées. Les sages ne l'attendent pas et ne lisent pas ses remarques, rangées dans la table `scribe_note`. Une remarque ratée est ignorée.

Événements du flux : `step`, `turn`, `delta`, `reset`, `message`, `note`, `synthesis`, `status`, `failure`.

## Routes

| Route | Rôle |
| --- | --- |
| `/api/auth/*` | Inscription, connexion, renouvellement, déconnexion |
| `/api/me`, `/api/me/council` | Profil et conseil du joueur |
| `/api/sages` | Sages visibles, création, modification, suppression |
| `/api/debates` | Débats du joueur, flux, précisions, archivage |
| `/api/llm/models`, `/api/stats` | Catalogue des modèles et tendances |
| `/user`, `/sessions`, `/api/steps` | Administration (rôle `ADMIN`) |

## Tests

```bash
mvn test
```

`BackSkeletonApplicationTests` démarre l'application : il faut que la base tourne et que `.env` soit rempli.

Limitation : l'état « débat en cours » est gardé en mémoire. Faire tourner plusieurs instances de l'API demanderait un bus d'événements partagé.
