# Biketeam

## Production

See live [here](https://www.biketeam.info)

## Use sources

Biketeam is a standard spring boot application so use spring boot maven plugin. You'll need a postgresql database.

`mvn spring-boot:run`

Or to clean and install

`mvn clean install`

## Run or deploy

### Prerequisites

#### Create an application on your strava account

You'll need to declare an app in strava for authentication.

Go to [this page](https://www.strava.com/settings/api) and create you app.

Write down client-id and client-secret.

#### Install PostgreSQL and create a database

Follow instructions to install PostgreSQL and create a database and a user.

#### Create a custom configuration file

Create file for custom configuration for example `application-custom.properties`.

Copy following lines and set your values.

```
site.url=http[s]://your-domain
## Spring Datasource and JPA config : postgresql
## database and username must be created before startup
spring.datasource.url=jdbc:postgresql://localhost:5432/biketeam
spring.datasource.username=xxx
spring.datasource.password=xxx
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQL10Dialect
## Spring security with Strava
spring.security.oauth2.client.registration.strava.client-id=xxx
spring.security.oauth2.client.registration.strava.client-secret=xxx
spring.security.oauth2.client.registration.strava.redirect-uri=http[s]://your-domain/login/oauth2/code/strava
spring.security.oauth2.client.provider.strava.token-uri=https://www.strava.com/oauth/token?client_id=xxx&client_secret=xxx
## User used as primary admin
admin.strava-id=xxx
admin.first-name=admin
admin.last-name=admin
## Directories (archive to import, file to store)
archive.directory=/path/to/archives
file.repository=./data/repository
## Mapbox integration
mapbox.api-key=xx
## Facebook integration (optional)
facebook.app-id=xx
facebook.app-secret=xx
## Contact email
contact.email=foo@bar.com
## SMTP integration (optional)
## See Spring Mail configuration
```

### With docker

#### Build image

`docker build -t biketeam:0.0.1 .`

#### Run image

`docker run -d -p 8080:8080 -v /path/to/application-custom.properties:/opt/biketeam/application-custom.properties -v /path/to/data:/opt/biketeam-data -t biketeam:0.0.1`

### With java

#### Install java

You need Java 13 or above.

#### Get the jar

Download the biketeam jar on github or clone the source and build source with `mvn clean install`.

#### Start

Start with command line (change path to application-custom.properties if needed)

`/usr/bin/java -jar biketeam.jar --spring.config.location=classpath:/application.properties,./application-custom.properties`
