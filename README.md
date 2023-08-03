# urbanflo-sumo-server

Interacts between [SUMO](https://www.eclipse.org/sumo/) and Urbanflo app.

## Development

### Dependencies

- [JDK 17](https://adoptium.net/temurin/releases/)
- [Kotlin](https://kotlinlang.org/docs/getting-started.html)
- [Gradle](https://gradle.org/install/) (optional - wrapper included in repository)

### Building

1. Clone the repository
2. Run `./gradlew build` to build

### Build docker container

```shell
docker compose up --force-recreate --build
```

## Deploy

```shell
java -jar build/libs/urbanflo-sumo-server-0.0.1-SNAPSHOT.jar
```
