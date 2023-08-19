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

### Docker development

```shell
docker compose up --force-recreate --build
```

## Deploy

### Docker

```shell
docker compose up --force-recreate
```

Note: ARM64/M1 users will need to use `docker compose up --force-recreate --build` for now, as the pre-built image is
only for x86-64. This is because the ARM version of SUMO package does not include `libtracijni`, requiring a build from
scratch, and the CI job keeps timing out for ARM.

See [this issue](https://github.com/eclipse/sumo/issues/13702) to learn more

### Manual

```shell
java -jar build/libs/urbanflo-sumo-server-0.0.1-SNAPSHOT.jar
```
