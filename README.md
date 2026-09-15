# Biketeam

[![ko-fi](https://www.ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/S6S6CLH20)

## Use project

### Prerequisites

#### Install Java

Version 17 or above

#### Start components

`docker compose up -d`

#### Configure OAuth2

To handle authentication, you'll need : 
* [Strava](https://www.strava.com/settings/api) (mandatory)
* [Google](https://developers.google.com/identity/sign-in/web/sign-in) (optional)
* [Facebook](https://developers.facebook.com/docs/facebook-login/web) (optional)

Declare and configure your app in these providers to get Oauth2 Client ID and Client Secret

#### Get a SMTP provider

You'll need to configure a SMTP for biketeam to send mail (check out [Mailjet](https://www.mailjet.com/) if you don't have one).

Docker Compose provides [MailHog](https://github.com/mailhog/MailHog) on [this URL](http://localhost:8025) for local tests.

#### Get a Mapbox Key

Go to [Mapbox](https://www.mapbox.com/) and get a developer key.

### Configuration

Copy .env.template to .env and replace first lines values, including REMEMBERME_KEY.

### Run

To enable logging in a file, use `--logging.file.name=/path/to/log/file` as a command line argument.

#### With Maven

Biketeam is a standard spring boot application so use spring boot maven plugin.

`mvn spring-boot:run`

#### In IDE 

Run BiketeamApplication class with main method.

#### Executable jar

Run `mvn clean package` then execute the biketeam.jar

`java -jar biketeam.jar`

.env file must be next to the jar file.

#### Docker Compose

Run `mvn clean package -Pdocker` then execute

`docker compose -f docker-compose.yml -f docker-compose.biketeam.yml up -d`

`/opt/biketeam` MUST exist.

## Authentification

### Modes de connexion disponibles

Biketeam propose plusieurs modes de connexion :
* **email / mot de passe** — inscription avec vérification de l'adresse par lien envoyé par mail, mot de passe oublié / réinitialisation par lien mail, changement de mot de passe depuis `/users/me` ;
* **Google** (OAuth2, optionnel) ;
* **Facebook** (OAuth2, optionnel) ;
* **Strava** (OAuth2) — voir ci-dessous, connexion en cours de transition.

### Statut de transition de la connexion Strava

La connexion Strava est **conservée temporairement** mais sera **supprimée** à terme. Le code est isolé derrière une stratégie de fournisseurs (`OAuth2ProviderHandler`) précisément pour que ce retrait ne nécessite aucune refonte. Le jour venu, il suffit de retirer :

1. `StravaProviderHandler` (`info.tomacla.biketeam.security.oauth2.provider`, annotée `@Deprecated`) ;
2. le bloc `spring.security.oauth2.client.{registration,provider}.strava.*` d'`application.properties` ;
3. le bouton de connexion Strava de `login.ftlh` ;
4. le bootstrap de l'administrateur par `admin.strava-id` dans `UserService.init()` ;
5. les entrées Strava de `.env.template`, de ce README et des fichiers `docker-compose*.yml`.

Les colonnes `strava_id` / `strava_user_name` de `user_account` ne sont pas concernées et peuvent rester en base.

### Comptes incomplets

Un compte est considéré **incomplet** s'il n'a ni email vérifié + mot de passe, ni identité Google ou Facebook liée — c'est typiquement le cas des comptes créés via Strava.

Le niveau d'insistance est piloté par `auth.completion.mode`, qui forme une escalade progressive :

| Mode | Effet pour un compte incomplet |
| --- | --- |
| `OFF` | Aucune sollicitation. |
| `SUGGESTED` *(défaut)* | Bandeau d'incitation en haut de toutes les pages + badge « Compte à compléter » dans la barre de navigation. La navigation reste libre. Le bandeau est masquable, mais pour la session seulement (mémorisé en `sessionStorage`) ; le badge reste toujours visible. |
| `ENFORCED` | En plus du bandeau, toute page hors whitelist redirige vers `/account/complete` (whitelist : login/logout, pages de complétion, ressources statiques, `/confirm-email`, `/users/me/delete` et l'API `/api/**`). Les requêtes JSON/XHR reçoivent un `403 {"error":"account_completion_required"}` plutôt qu'une page HTML. |

Une valeur inconnue ne bloque pas le démarrage : repli sur `SUGGESTED` avec un avertissement dans les logs. Le mode effectif est loggué au démarrage.

La trajectoire prévue est donc d'ouvrir en `SUGGESTED` le temps que les comptes Strava se complètent d'eux-mêmes, puis de passer en `ENFORCED` pour les retardataires, `OFF` servant de retour arrière.

### Prérequis SMTP

Sans configuration SMTP (`SMTP_*`), l'inscription par email, la réinitialisation de mot de passe **et le forçage de complétion de compte** sont désactivés : sans mail, personne ne peut vérifier une adresse, un utilisateur Strava incomplet serait donc enfermé dans la boucle de complétion. Le forçage se désactive automatiquement (`auth.completion.mode=ENFORCED` seul ne suffit pas, il faut aussi que le SMTP soit configuré) et un avertissement est loggué au démarrage si ce n'est pas le cas.

Le bandeau d'incitation, lui, reste affiché sans SMTP : la page de complétion permet aussi de lier un compte Google ou Facebook, ce qui ne demande aucun envoi de mail.

### Migration des emails

L'email devient une identité de connexion et doit être unique (insensible à la casse). La migration Liquibase gère les doublons existants selon la procédure suivante :

1. exécuter d'abord le `select` de détection de doublons (changeset `login-password-email-dedup`) seul sur un dump de la base ;
2. relire le contenu de la table d'audit `user_email_conflict` qui liste les comptes perdants et le compte conservé pour chaque doublon ;
3. appliquer ensuite la migration complète.

Règle de vérification appliquée aux emails existants : **aucune adresse existante n'est promue « vérifiée » par la migration**, y compris sur les comptes portant une identité Google ou Facebook. Porter un `google_id` / `facebook_id` ne prouve en effet pas que l'adresse vienne du fournisseur : l'ancien formulaire `/users/me` permettait de saisir n'importe quelle adresse sur son propre compte, y compris sur un compte déjà lié à Google. Promouvoir ces adresses en identités de connexion vérifiées rouvrirait une usurpation : la victime se connectant pour la première fois avec Google atterrirait dans le compte de celui qui a revendiqué son adresse (repli « recherche par email » des handlers OAuth2).

Conséquence : `email_verified` ne vaut `true` que par une preuve réelle de contrôle de la boîte — clic sur un lien de vérification, réinitialisation de mot de passe, ou connexion Google/Facebook qui repose l'adresse prouvée par le fournisseur. Les comptes Google/Facebook existants retrouvent donc le drapeau **automatiquement à leur prochaine connexion**, sans action manuelle et sans perte d'accès (ils restent « complets » par leur identité externe). Le flux "mot de passe oublié" reste le chemin de vérification des comptes sans identité externe.

Requête d'audit recommandée avant et après migration :

```sql
select id, email, google_id, facebook_id from user_account
 where email is not null and (google_id is not null or facebook_id is not null);
```

Note d'ordonnancement : le changeset `login-password-email-dedup` supprime l'ancienne contrainte `unique_email` **avant** de normaliser les adresses en minuscules. Cette contrainte historique est sensible à la casse et porte sur toutes les lignes, comptes supprimés compris : sans ce drop préalable, un compte supprimé `foo@bar.com` face à un compte actif `Foo@Bar.com` ferait échouer la migration, donc le démarrage.

### Variables d'administration

`ADMIN_EMAIL` et `ADMIN_PASSWORD` permettent d'amorcer un compte administrateur par email/mot de passe (optionnel), en complément de `ADMIN_STRAVA_ID`, en préparation du retrait de Strava. Un mot de passe existant n'est jamais réécrit au démarrage.

Ces deux variables sont lues directement de l'environnement par la liaison relâchée de Spring Boot, comme `ADMIN_STRAVA_ID` : elles ne doivent **pas** être déclarées dans `application.properties` sous la forme `admin.email=<placeholder admin.email>`, le placeholder porterait le nom de la propriété qu'il définit et Spring refuserait de démarrer sur une référence circulaire dès que la variable d'environnement est absente.

### Recette manuelle

Aucun test automatique ne couvre la chaîne complète de filtres de sécurité : la recette suivante doit être rejouée manuellement après tout changement dans cette zone.

1. Inscription → réception du mail → `/verify-email` → connexion par mot de passe.
2. Mot de passe oublié → réception du mail → réinitialisation → connexion ; vérifier que les autres sessions sont bien déconnectées (la réinitialisation fait tourner la graine remember-me **et** supprime les sessions HTTP persistées de l'utilisateur, les deux étant nécessaires : une session stockée en base n'est jamais reconfrontée à `user_account`).
   Vérifier également qu'un changement d'adresse email depuis `/users/me` exige le mot de passe actuel (ou, pour un compte sans mot de passe, une connexion fraîche : une session « se souvenir de moi » est refusée), l'adresse étant une identité de connexion.
3. Connexion Strava d'un compte incomplet → redirection vers `/account/complete` → email + mot de passe → accès normal.
4. Même scénario mais via « Lier mon compte Google », y compris le cas de conflit → `/account/link-conflict` → fusion.
5. **`GET /logout` depuis la navbar** : à vérifier impérativement après toute modification de la configuration CSRF. La réactivation ciblée de CSRF (`SecurityConfig`) exige un `logoutRequestMatcher` explicite acceptant GET et POST ; sans lui, plus personne ne peut se déconnecter. Vérifier aussi « Supprimer mon compte », désormais en `POST` avec jeton CSRF (une opération destructrice ne doit pas être déclenchable par une simple navigation provoquée depuis un site tiers).
6. **Cookie "se souvenir de moi" émis avant déploiement** : vérifier qu'il reste valide (compatibilité ascendante de `auth_token_seed`, initialisé à l'id utilisateur par la migration), puis qu'il est réémis avec une nouvelle graine aléatoire à la première connexion interactive (form login ou OAuth2 — jamais lors d'un auto-login remember-me).
7. `POST /api/auth/refresh` avec un cookie valide, puis appel à `/api/auth/me` avec l'en-tête `X-Auth-Token` retourné.
8. Vérifier qu'un nouvel identifiant de session est émis après `POST /login` (protection contre la fixation de session).
