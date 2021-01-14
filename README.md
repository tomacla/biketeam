# Biketeam

Easily create a website for bike groups.

## Demo

See live demo [here](https://biketeam.tomacla.info)

## Use sources

Biketeam is a standard spring boot application so use spring boot maven plugin 

`mvn spring-boot:run`

Or to clean and install

`mvn clean install`

## Run or deploy

### Prerequisites

#### Create an application on your strava account

You'll need to declare an app in strava for authentication.

Go to [this page](https://www.strava.com/settings/api) and create you app.

Write down client-id and client-secret.

#### Create a custom configuration file

Create file for custom configuration for example `application-custom.properties`.

Copy following lines and set your values.

```
file.repository=/path/to/data/folder
spring.datasource.url=jdbc:hsqldb:file:/path/to/data/folder/db/biketeam
spring.security.oauth2.client.registration.strava.client-id=xxx
spring.security.oauth2.client.registration.strava.client-secret=xxx
spring.security.oauth2.client.registration.strava.redirect-uri=http://callback.oauth2.url
spring.security.oauth2.client.provider.strava.token-uri=https://www.strava.com/oauth/token?client_id=xxx&client_secret=xxx
admin.strava-id=xxx
admin.first-name=admin
admin.last-name=admin
```

* file.repository : define a local path to a writable repository
* spring.datasource.url : change the path to a writable repository
* spring.security.oauth2.client.registration.strava.client-id : strava app client id
* spring.security.oauth2.client.registration.strava.client-secret : strava pp client secret
* spring.security.oauth2.client.registration.strava.redirect-uri : callback URI after authentication (should be http[s]://your-domain/login/oauth2/code/strava)
* spring.security.oauth2.client.provider.strava.token-uri : just change client_id and client_secret parameters to the right values
* admin.strava-id : this is your strava user id
* admin.first-name : your first name
* admin.last-name : your last name

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