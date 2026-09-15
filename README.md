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
* **passkey** (WebAuthn) — s'active depuis `/users/me` sur un compte déjà connecté, voir ci-dessous ;
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

### Passkeys (WebAuthn)

Une passkey remplace le mot de passe par l'empreinte, le visage ou le code de l'appareil. Deux règles structurent toute l'implémentation :

* **une passkey ne crée jamais de compte.** Elle s'enregistre depuis `/users/me`, sur une session déjà ouverte. C'est ce qui dispense d'un parcours de récupération dédié : perdre toutes ses passkeys ramène simplement au mot de passe ou au compte Google/Facebook, qui restent en place.
* **une passkey ne rend pas un compte « complet »** au sens de la section précédente. Elle disparaît avec l'appareil, elle ne peut donc pas tenir lieu de moyen de récupération. Un compte Strava seul qui ajoute une passkey continue de voir le bandeau d'incitation.

La connexion, elle, ne demande aucun identifiant : la passkey est enregistrée comme *credential découvrable* (`residentKey: required`), l'authentificateur présente donc lui-même celle dont il dispose pour le site. Le champ email de `/login` porte `autocomplete="username webauthn"`, ce qui fait apparaître la passkey dans la liste de suggestions du navigateur sans clic préalable ; un bouton explicite reste disponible.

#### Contrainte de domaine

Une passkey est liée cryptographiquement à un **RP ID**, dérivé de `site.url`. L'authentificateur refuse de la présenter sur tout autre domaine : **les passkeys ne fonctionnent pas sur le domaine personnalisé d'une team** (`TeamConfiguration.domain`). Ce n'est pas une régression — la macro `common.teamUrl` sert déjà `/login`, `/register` et `/users/me` depuis `_siteUrl` — mais c'est une limite structurelle de WebAuthn, pas un choix d'implémentation.

Si une de ces pages est malgré tout atteinte depuis un domaine de team, `passkey.js` masque le bouton plutôt que d'en proposer un qui échouerait : la requête serait interdomaine, le cookie de session ne partirait pas et le challenge ne serait jamais retrouvé.

Le navigateur n'expose l'API WebAuthn qu'en **contexte sécurisé** : HTTPS, ou `localhost`. En développement sur `http://localhost:8080` tout fonctionne sans configuration. Sur un `site.url` en HTTP autre que `localhost`, un avertissement est loggué au démarrage et le bouton restera sans effet.

#### Configuration

| Propriété | Défaut | Rôle |
| --- | --- | --- |
| `auth.passkey.rp-id` | hôte de `site.url` | Domaine auquel les passkeys sont liées. À ne forcer que pour couvrir plusieurs sous-domaines, et toujours avec un suffixe enregistrable de l'origine — sinon le navigateur refuse l'enregistrement **sans message d'erreur**. |
| `auth.passkey.origins` | origine de `site.url` | Origines supplémentaires acceptées, séparées par des virgules (reverse proxy exposé sur plusieurs noms). |
| `auth.passkey.challenge-validity-seconds` | `300` | Validité d'un challenge, et timeout côté navigateur. |
| `auth.passkey.max-per-user` | `20` | Nombre maximum de passkeys par compte. |
| `auth.passkey.login-options.max-per-hour` | `60` | Débit sur `POST /login/webauthn/options`, par adresse IP. Cette route est publique et crée une session. |

Les deux premières sont surchargeables par `AUTH_PASSKEY_RP_ID` et `AUTH_PASSKEY_ORIGINS` (voir `.env.template`). Aucune n'est requise pour un déploiement mono-domaine.

#### Notes d'implémentation

La vérification est confiée à `webauthn4j`, et non au DSL WebAuthn natif de Spring Security : celui-ci impose sa page de connexion et résout le compte via `UserDetailsService.loadUserByUsername(userEntity.getName())`, ce qui obligerait à loger l'identifiant technique dans un champ destiné à l'affichage. Le projet avait déjà tranché dans ce sens pour le mot de passe.

Aucun comptage de tentatives, contrairement au mot de passe : une signature asymétrique ne se devine pas. Le débit est limité en amont, sur l'émission des options. Le message d'échec est uniformément le même que le credential soit inconnu ou la signature invalide — les distinguer ferait de l'endpoint un oracle d'énumération.

Seule la clé **publique** est stockée (`user_passkey.attested_credential_data`) : sa fuite ne permet pas de se connecter. La colonne est déclarée `BYTEA` et non `BLOB`, le type abstrait de Liquibase produisant un `oid` sur PostgreSQL alors qu'Hibernate écrit du `bytea` — le décalage ne se voit qu'au premier insert.

Côté cycle de vie du compte : la suppression (soft delete) détruit les passkeys, au même titre que le mot de passe et la graine remember-me ; la fusion de comptes les déplace vers le compte conservé.

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
9. **Passkeys** — la vérification de signature n'est couverte par aucun test automatique (elle appartient à `webauthn4j` et demanderait un authentificateur virtuel) : ce scénario doit être joué en navigateur réel.
   1. Depuis `/users/me`, « Ajouter une passkey », nommer l'appareil, vérifier qu'elle apparaît dans la liste avec sa date.
   2. Se déconnecter, puis se connecter par le bouton « Se connecter avec une passkey » — sans saisir d'adresse.
   3. Recharger `/login` et vérifier que la passkey est proposée directement dans la liste d'autocomplétion du champ email (remplissage conditionnel).
   4. Vérifier que « dernière utilisation » s'est mise à jour, puis supprimer la passkey et vérifier que la connexion par ce moyen n'est plus possible.
   5. Tenter un second enregistrement avec le même appareil : il doit être refusé (`excludeCredentials`, doublé d'un contrôle serveur).
   6. Sur un navigateur sans WebAuthn, vérifier que `/login` n'affiche pas le bouton et que `/users/me` affiche « Ce navigateur ne gère pas les passkeys ».
   7. Si une team dispose d'un domaine personnalisé, ouvrir `/login` sur ce domaine et vérifier que le bouton passkey est bien absent.
