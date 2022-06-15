# Biketeam

## Live

See [here](https://www.biketeam.info)

[![ko-fi](https://www.ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/S6S6CLH20)

## Use projet

### Prerequisites

#### Install Java

Version 17 or above

#### Create a Datasource

Install a DBMS and create a database.
Source code has only been tested with PostgreSQL.

#### Configure OAuth2

To handle authentication, you'll need : 
* [Strava](https://www.strava.com/settings/api) (mandatory)
* [Google](https://developers.google.com/identity/sign-in/web/sign-in) (optional)
* [Facebook](https://developers.facebook.com/docs/facebook-login/web) (optional)

Declare and configure your app in these providers to get Oauth2 Client ID and Client Secret

#### Get a SMTP provider

You'll need to configure a SMTP for biketeam to send mail (check out [Mailjet](https://www.mailjet.com/) if you don't have one). 

#### Get a Mapbox Key

Go to [Mapbox](https://www.mapbox.com/) and get a developer key.

### Configuration

Create file for custom configuration for example `application-custom.properties`.

Copy following lines and set your values.

```
site.url=http[s]://your-biketeam-host
contact.email=from@domain.com

file.repository=/path/to/biketeam/data
archive.directory=/path/to/biketeam/archives

mapbox.api-key=your-mapbox-key

rememberme.key=a-random-string-to-secure-spring-security

spring.datasource.url=jdbc:postgresql://[host]:[port]/[database]
spring.datasource.username=[user]
spring.datasource.password=[password]
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQL10Dialect

spring.security.oauth2.client.registration.strava.client-id=xxxx
spring.security.oauth2.client.registration.strava.client-secret=xxxx
spring.security.oauth2.client.registration.strava.redirect-uri=http[s]://your-biketeam-host/login/oauth2/code/strava
spring.security.oauth2.client.provider.strava.token-uri=https://www.strava.com/oauth/token?client_id=xxx&client_secret=xxxxx

# optional facebook connect
spring.security.oauth2.client.registration.facebook.client-id=xxx
spring.security.oauth2.client.registration.facebook.client-secret=xxxx
spring.security.oauth2.client.registration.facebook.redirect-uri=http[s]://your-biketeam-host/login/oauth2/code/facebook

# optional sign in with google
spring.security.oauth2.client.registration.google.client-id=xxx
spring.security.oauth2.client.registration.google.client-secret=xxx
spring.security.oauth2.client.registration.google.redirect-uri=http[s]://your-biketeam-host/login/oauth2/code/google

spring.mail.host=[smtp-host]
spring.mail.port=[smtp-port]
spring.mail.protocol=[smtp-protocol]
spring.mail.username=[smtp-username]
spring.mail.password=[smtp-password]
spring.mail.properties.mail.transport.protocol=smtp
spring.mail.properties.mail.smtps.auth=true
spring.mail.properties.mail.smtps.ssl.protocols=TLSv1.2
spring.mail.properties.mail.smtps.starttls.enable=true
spring.mail.properties.mail.smtps.ssl.trust=[smtp-host]
spring.mail.properties.mail.smtps.timeout=8000

# this is the user that will be created at startup with admin permissions
admin.strava-id=xxx
admin.first-name=name
admin.last-name=last name
```

### Run

Always add your configuration file to your classpath

#### With Maven

Biketeam is a standard spring boot application so use spring boot maven plugin.

`mvn spring-boot:run`

#### In IDE 

Run BiketeamApplication class with main method.

#### Executable jar

Run `mvn clean package` then execute the biketeam.jar

`biketeam.jar --spring.config.location=classpath:/application.properties,/path/to/application-custom.properties`