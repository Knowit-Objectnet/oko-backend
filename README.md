# Prosjekt Ombruk Backend

[![](https://img.shields.io/badge/Kotlin-a?style=flat&logo=Kotlin&label=Code&color=0095D5&logoColor=ffffff)](https://kotlinlang.org/) [![](https://img.shields.io/badge/Intellij%20IDEA-a?style=flat&logo=intellijidea&label=IDE&color=000000&logoColor=ffffff)](https://www.jetbrains.com/idea/) [![](https://img.shields.io/badge/Gradle-a?style=flat&logo=gradle&label=Build%20tool&color=02303A&logoColor=ffffff)](https://gradle.org/)  
[![](https://img.shields.io/badge/PostgreSQL-a?style=flat&logo=postgresql&label=Database&color=4169E1&logoColor=ffffff)](https://gradle.org/) [![](https://img.shields.io/badge/Docker-a?style=flat&logo=docker&label=Tool&color=2496ED&logoColor=ffffff)](https://gradle.org/)

## Table of Contents
- [Intro](#intro)
- [Built with](#built-with)
    - [Libraries](#libraries)
- [Getting started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Download project](#download-project)
    - [Running locally](#running-locally)
    - [Environment variables](#environment-variables)
- [Usage](#usage)
    - [Table of endpoints](#table-of-endpoints)
- [File Structure](#file-structure)
    - [Resources](#resources)
    - [Src](#src)
- [Docker](#docker)
- [Testing](#testing)
    - [Unit tests](#unit-tests)
    - [Integration tests](#integration-tests)

## Intro
This is the backend of the reuse station project, 
belonging to [Renovasjon og Gjenvinningsetaten (*REG*)](https://www.oslo.kommune.no/etater-foretak-og-ombud/renovasjons-og-gjenvinningsetaten/) in Oslo kommune.

## Built with
It's entirely written in [Kotlin](https://kotlinlang.org/) with [Exposed](https://github.com/JetBrains/Exposed) as its ORM and [Ktor](https://ktor.io/) as its web framework.
The code is mostly written in a functional style with the help of [Arrow library](https://arrow-kt.io/).

Although Kotlin is Java-friendly, it's highly recommended reading up on Kotlin's documentation at [kotlinlang.org](https://kotlinlang.org/docs/home.html)

Most of the developers that contributed to the project used [Intellij IDEA](https://www.jetbrains.com/idea/). 
We would recommend it as it has excellent Kotlin tooling. The next section has a list of some libraries you may need to look at for documentation. 
Note: the 4 first listed are the ones you will see most around the code base.

### Libraries:
- [Ktor](https://ktor.io/)
- [Exposed](https://github.com/JetBrains/Exposed)
- [Arrow](https://arrow-kt.io/)
- [Valiktor](https://github.com/valiktor/valiktor)
- [Koin](https://insert-koin.io/)
- [Flyway](https://flywaydb.org/)
- [AWS Lambda](https://docs.aws.amazon.com/lambda/?id=docs_gateway)
- [PostgreSQL JDBC](https://jdbc.postgresql.org/documentation/head/index.html)

## Getting started
### Prerequisites
To compile and run this project, you would need a Java Development Kit. We have been using [JDK 8](https://www.oracle.com/java/technologies/javase-downloads.html). 
As mentioned earlier, we highly recommend using [Intellij IDEA](https://www.jetbrains.com/idea/) as most of the plugins and libraries needed will be prompted and installed. 
If you want to run without it, you will need to install [Gradle](https://gradle.org/).

### Download project
This section will guide you to clone this repository. To follow this part of the guide, 
we expect you to have [Git](https://git-scm.com/) installed. Type the following lines in the *Terminal* (for ***unix*** users), 
or *Command Prompt* (for ***windows*** users):

```sh
cd /to-your-desired-directory
git clone https://kode.knowit.no/scm/oko/backend.git
cd backend
```

You are now inside the project folder.
Type `ls` in the *terminal*, or `dir` in *Command Prompt* to see the root folder structure.

### Running locally

To run this project locally you would need to install [Docker](https://www.docker.com/). This is for running a database locally.
After installing Docker, type in the commands bellow in a *terminal* to start a *PostgreSQL*-database:

```sh
docker run -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=db -p 5432:5432 postgres:13.2
```
Some explenation of the arguments in this initialization line:
`POSTGRES_USER`: Username to connect to database.
`POSTGRES_PASSWORD`: Password to connect to database.
`POSTGRES_DB`: Name of database.
`-p 5432:5432`: Port to connect to server.
`postgres:13.3`: PostgreSQL version.

You can install dependencies and run the application with:
```sh
gradle run
```
If you received some errors, you may need to set some of the [Environment Variables](#environment-variables) that are noted in the next section.

### Environment variables
You may need to set two environment variables in order for the application to function as intended:

`OKO_KEYCLOAK_CLIENT_SECRET`

`OKO_DEBUG`

To authorize users we use an open source solution, [Keycloak](https://www.keycloak.org/).
The keycloak client secret can be found in both Keycloak and AWS, and is usually fetched from AWS when running there.

OKO_DEBUG allows for bypassing Keycloak entirely. Under normal operations, 
the application makes requests to keycloak when creating partners and stations, 
as well as for authenticating/authorizing.

By setting OKO_DEBUG to true, the application stops making calls to Keycloak, and starts allowing pre-created access tokens. 
These will be listed when running the application, and are also available in `src/shared/api/JwtMockConfig.kt`.

## Usage
To communicate with the API we recommend you use some kind of API Client. E.g. [Postman](https://www.postman.com/) or [Insomnia](https://insomnia.rest/). 
It is important to remember that you need a *bearer token* to communicate with the server with or without *Keycloak*.

### Table of endpoints
Entry when local: `localhost:#PORTNUMBER#` or `0.0.0.0:#PORTNUMBER#`
where `#PORTNUMBER#` is set to `8080` by default in [application.conf](#applicationconf).

Most but not all the endpoints have a `get`, `post`, `patch` and `delete` method. 
Look up the corresponding HTTP Controller file for more details of the *nested endpoints* and *authentication*.

| **Endpoint** | **Description** | **More details at** |
|---|---|---|
| /aarsak   | Reason for cancellation  | src/aarsak/application/api/AarsakHttpController.kt  | 
| /aktor  | A general endpoint for looking up different participants  | src/aktor/application/api/AktorHttpController.kt |
| /kontakter  | Contact persons  | src/aktor/application/api/KontaktHttpController.kt  | 
| /partnere  | Partners that pick up from stations  | src/aktor/application/api/PartnerHttpController.kt  | 
| /stasjoner  | Stations  | src/aktor/application/api/StasjonHttpController.kt  | 
| /avtaler  | Agreements  | src/aktor/application/api/AvtaleHttpController.kt  | 
| /hentinger  | Pickup wrapper. A general way to get a *planlagt henting* or *ekstrahenting*  | src/henting/application/api/HentingHttpController.kt  | 
| /ekstra-hentinger  | Extra pickup  | src/henting/application/api/EkstraHentingHttpController.kt | 
| /planlagte-hentinger | Planned pickup  | src/henting/application/api/PlanlagtHentingHttpController.kt |
| /henteplaner  | Pickup plans  | src/henting/application/api/HenteplanHttpController.kt | 
| /kategorier | Categories | src/kategori/application/api/KategoriHttpController.kt|
| /statistikk | Weight statistics | src/statistikk/application/api/StatistikkHttpController.kt|
| /utlysninger | Announcements/Events | src/utlysning/application/api/UtlysningHttpController.kt |
| /vektregistrering | Weight registrations | src/vektregistrering/application/api/VektregistreringHttpController.kt |

## File Structure
### Resources
The resource folder contains files that are needed one place or another in the application. The file, _messages_en.properties_, 
is used for creating custom messages for the validation library the application uses, [Valiktor](https://github.com/valiktor/valiktor).
#### Application.conf
The `application.conf` file can be considered the entry point of the application, 
and contains important application values. The _.conf_ file usually defines each variable twice, 
where the first occurrence is a default value and the second being one that can override the origin value by being passed in through environment variables.

#### Table of variables in application.conf
| **Block**  | **Name**  | **Description** |
|---|---|---|
| deployment   | port  | The port to deploy the application to  | 
| deployment  | watch  | Currently not working. Used for hot reloading  |
| application  | modules  | Entrypoint for application  | 
| db  | jdbcUrl  | The URL that jdbc should connect to  | 
| db  | password  | Password for the database  | 
| db  | user  | Username for the database  | 
| db  | migrationsLocation  | Location of db migration  | 
| keycloak  | clientSecret  | Used for sending requests to keycloak  | 
| keycloak  | keycloakUrl  | The URL of the keycloak instance to connect to  |
| keycloak  | keycloakRealm  | The keycloak realm that should be used  | 
| oko | debug | Whether the application should run in debug mode or not. Should be false when deploying |

#### OpenAPI
**NOTE: This is outdated.**  
The OpenAPI folder describes the available API calls within the application. Each path has its own folder. These folders
contain descriptions of the different endpoints belonging to a path. The schema folder contains component schemas that
"belongs" to that path.

Furthermore, the _openapi_ folder contains the two files _api.yaml_ and _openapi_yaml_.
_api.yaml_ specifies the different paths and components in the different sub-modules. One can then use
[Swagger-cli]() or something akin to it to merge all the _yaml_ files into one big file, _openapi.yaml_.
This file is then usually uploaded to our [SwaggerHub](https://app.swaggerhub.com/apis/oko8/OKO/1.0.1).

#### Migrations
Migrations are done through the use of [Flyway](https://flywaydb.org).
The different database migrations are located in the db.migrations folder. 
If the postgres database requires fields to be updated, a new migration has to be created in order to alter the running db instance. 
Each new migration must follow this naming schema:
`V[1-9]+__*.sql`

### Src
Each endpoint has been placed in its own folder within __src__, 
with some exception as can be seen in the [table of endpoints](#table-of-endpoints).

Each folder has the following structure:

| **Name** | **Description** |
|---|---|
| dto | Serializable data classes that are created for a specific REST operation on a specific endpoint. Contains validation logic. |
| services | Business logic that does not belong in api. Communicates with the repository. |
| entity | An object that represent an entity of the endpoint. Serializable data class. |
| model | Representations of objects "belonging" to a specific endpoint. |
| infrastructure | Data access layer. Contains ORM logic and code representations of database tables. |

Most of the HTTP-requests follow this routine:
1. `HTTPController`: Authenticate and correct HTTP-method
2. `Services`: Access the correct repository and method to retrieve all the data that is requested.
3. `Repository`: Prepares query for the _PostgreSQL_-database and creates an `entity`.


The exception to this rule are the packages ***not*** listed in [table of endpoints](#table-of-endpoints). 
These packages, e.g. the __shared__ and __core__ folders, which contains common logic used in the other folders. 
__notification__ is used to communicate with *AWS Lambda*.

## Docker
We have used two different ways of running Docker throughout the project duration; 
Running locally has been done through the use of Docker-compose and Dockerfile.dev, whilst the deployed application simply uses __Dockerfile__. 
The deployment __Dockerfile__ is not magic.

Two things to look out for is to ensure that access rights are set correctly and that db migrations are placed where they're expected. 
All files should work as-is.

## Testing

### Unit tests

We are doing unit tests with JUnit5, Ktors test server, and MockK. All the tests can be found in the test directory.
All services have their own corresponding package. When writing new test you should try to make a as comprehensible as possible.
We have tried to follow AAA(Assemble, Act, Analyze), which means that the test should have three distinct parts. One for setup,
one for executing the feature you are testing, and one for checking that the results are what you expected.
The variables name ***expected*** and ***actual*** are use throughout our tests to make understanding other tests easier.

### Integration tests

For our integration tests we use ktor test framework. We lose a bit of in-depth http testing by doing it this way. 
The ease of use makes up for this as tests are much faster to write. Doing more in-depth integration testing is something which needs a closer look in the future. 
It is important to set the environment variable ***OKO_DEBUG*** to true before running integration tests. We are not testing against a real Keycloak instance. 
Debug mode makes sure this is the case. 