FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY tradesense-common-model ./tradesense-common-model
COPY tradesense-ingestion-svc ./tradesense-ingestion-svc
COPY tradesense-liquibase ./tradesense-liquibase
COPY tradesense-api-gateway ./tradesense-api-gateway
COPY tradesense-trade-processor ./tradesense-trade-processor
COPY tradesense-validator-svc ./tradesense-validator-svc
COPY tradesense-report-svc ./tradesense-report-svc
COPY tradesense-ui ./tradesense-ui

RUN chmod +x mvnw && ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /opt/tradesense

COPY --from=build /workspace/tradesense-api-gateway/target/tradesense-api-gateway-*.jar /opt/tradesense/tradesense-api-gateway.jar
COPY --from=build /workspace/tradesense-ingestion-svc/target/tradesense-ingestion-svc-*.jar /opt/tradesense/tradesense-ingestion-svc.jar
COPY --from=build /workspace/tradesense-liquibase/target/tradesense-liquibase-*.jar /opt/tradesense/tradesense-liquibase.jar
COPY --from=build /workspace/tradesense-trade-processor/target/tradesense-trade-processor-*.jar /opt/tradesense/tradesense-trade-processor.jar
COPY --from=build /workspace/tradesense-validator-svc/target/tradesense-validator-svc-*.jar /opt/tradesense/tradesense-validator-svc.jar
COPY --from=build /workspace/tradesense-report-svc/target/tradesense-report-svc-*.jar /opt/tradesense/tradesense-report-svc.jar

RUN mkdir -p /opt/tradesense/data/trade-files

CMD ["bash"]

