FROM openjdk:17.0.1-oraclelinux7
ARG JAR_FILE=build/libs/inventory.jar
COPY ${JAR_FILE} inventory.jar
ENTRYPOINT ["java","-jar","/inventory.jar"]