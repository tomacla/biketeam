# Biketeam

## Live

See [here](https://www.biketeam.info)

[![ko-fi](https://www.ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/S6S6CLH20)

## Use projet

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
