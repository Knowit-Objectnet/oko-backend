# Prosjekt Ombruk Backend
This is the backend of the reuse station project, belonging to [Renovasjon og Gjenvinningsetaten (*REG*)](https://www.oslo.kommune.no/etater-foretak-og-ombud/renovasjons-og-gjenvinningsetaten/)
in Oslo kommune. It's entirely written in [Kotlin](https://kotlinlang.org/) with [Exposed]() as its ORM and [Ktor]() as its web framework.
The code is mostly written in a functional style. 

## Required Knowledge
Although Kotlin is Java-friendly, it's highly recommended to read up on Kotlin at [kotlinlang.org](https://kotlinlang.org/docs/reference/).
Furthermore, knowledge of the [Exposed ORM](), the [Ktor web framework]() and the [Arrow library]() is required.

## Getting Started

### Running Locally
Intellij IDEA has excellent Kotlin tooling, and it's highly recommended for everything.
If you want to run without it, you will need to install Gradle. Furthermore, you'll need to have JDK 8 installed.
We have been using 1.8.252.

You can install dependencies and run the application with `gradle run`.

### Environment Variables
You will need to set two environment variables in order for the application to function
as intended:

`OKO_KEYCLOAK_CLIENT_SECRET`

`OKO_DEBUG`

The keycloak client secret can be found in both Keycloak and AWS, and is usually fetched from AWS when running there.
OKO_DEBUG allows for bypassing keycloak entirely. Under normal operations, the application makes requests to keycloak 
when creating partners and stations, as well as for authenticating/authorizing. By setting OKO_DEBUG to true, the 
application stops making calls to Keycloak, and starts allowing pre-created access tokens. These will be listed when
running the application, and are also available in `JwtMockConfig.kt`.

## Resources
The resource folder contains files that are needed one place or another in the application.
_messages_en.properties_ is used for creating custom messages for the validation library the application uses, [Valiktor]().
### Application.conf
The _application.conf_ file can be considered the entry point of the application, and contains important application 
values. The _.conf_ file usually defines each variable twice, where the first occurence is a default value
and the second being one that can override the origin value by being passed in through environment variables.

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

### OpenAPI
The OpenAPI folder describes the available API calls within the application. Each path has its own folder. These folders
contain descriptions of the different endpoints belonging to a path. The schema folder contains component schemas that
"belongs" to that path.

Furthermore, the _openapi_ folder contains the two files _api.yaml_ and _openapi_yaml_. 
_api.yaml_ specifies the different paths and components in the different sub-modules. One can then use
[Swagger-cli]() or something akin to it to merge all the _yaml_ files into one big file, _openapi.yaml_.
This file is then usually uploaded to our [SwaggerHub](https://app.swaggerhub.com/apis/oko8/OKO/1.0.1).

### Migrations
Migrations are done through the use of [Flyway](https://flywaydb.org).
The different database migrations are located in the db.migrations folder. If the postgres database requires updated fields,
a new migration has to be created in order to alter the running db instance. Each new migration must follow this naming schema:
`V[1-9]+__*.sql`

## Docker
We have used two different ways of running Docker throughout the project duration; Running locally has been done through the use of
Docker-compose and Dockerfile.dev, whilst the deployed application simply uses __Dockerfile__. The deployment __Dockerfile__
is not magic; two things to look out for is to ensure that access rights are set correctly and that db migrations
are placed where they're expected. All files should work as-is.

## Structure
Each endpoint has been placed in its own folder within __src__, with the exception being __station__, which is placed
within __calendar_. This structure was created due to moving from microservices to a monolith; In case we want to move back
to microservices, the current structure would easily allow for this.

Each folder has the following structure:

| **Name** | **Description** |
|---|---|
| api | Contains endpoint definitions and logic |
| database | Data access layer. Contains ORM logic and code representations of db tables |
| form | Serializable data classes that are created for a specific REST operation on a specific endpoint. Contains validation logic. |
| model | Representations of objects "belonging" to a specific endpoint. |
| service | Business logic that does not belong in api or DAL. |

The exception to this rule is the __shared__ folder, which contains common logic used in the other folders.







