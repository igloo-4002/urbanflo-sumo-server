FROM gradle:8-jdk17-jammy AS gradle-builder
WORKDIR /urbanflo-sumo-server
COPY . .
RUN gradle build

FROM eclipse-temurin:17-jammy
WORKDIR /urbanflo-sumo-server
ARG VERSION="0.0.1-SNAPSHOT"
# install sumo
RUN apt-get update && apt-get install -y software-properties-common
RUN add-apt-repository ppa:sumo/stable && apt-get update
RUN apt-get install -y sumo sumo-tools
# copy server jar
COPY --from=gradle-builder /urbanflo-sumo-server/build/libs/urbanflo-sumo-server-$VERSION.jar urbanflo-sumo-server.jar