FROM arm64v8/openjdk:17.0-jdk-oraclelinux7
ARG JAR_FILE=build/libs/inventory.jar
COPY ${JAR_FILE} inventory.jar
ENTRYPOINT ["java","-jar","/inventory.jar"]