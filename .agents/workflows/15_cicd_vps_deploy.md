---
description: Deploy Spring Boot (Gradle/Maven) to VPS via GHCR & SSH.
---

# CI/CD VPS Deploy Workflow

## 1. PRE-FLIGHT (ASK USER FIRST)
Ask user 3 questions before generating:
1. Build Tool: Gradle or Maven?
2. Service Name: (e.g., `media-service`)
3. Host Port: (e.g., `8080`)

---

## 2. STRICT RULES
- Single `deploy` job ONLY. Trigger on `push` to `main` ONLY.
- NO `pull_request`, `workflow_dispatch`, `paths-ignore`, or extra test/docs jobs.
- `Dockerfile` & `docker-compose.prod.yml` MUST be at project root.
- IMAGE MUST be `ghcr.io/${{ github.repository_owner }}/<service-name>`.

### Deviation Handling
If project structure deviates: STOP & require:
1. `Dockerfile` at root project
2. `docker-compose.prod.yml` at root project
3. 3 Secrets configured in GitHub: `VPS_HOST`, `VPS_USER`, `VPS_PASSWORD`

---

## 3. TEMPLATES

### Dockerfile (Root)
```dockerfile
# Builder (Gradle: COPY gradlew build.gradle settings.gradle ./ && ./gradlew bootJar -x test)
# Builder (Maven: COPY .mvn/ mvnw pom.xml ./ && ./mvnw package -DskipTests)
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar -x test # OR: ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar # Maven: /app/target/*.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### `docker-compose.prod.yml` (Root)
```yaml
services:
  <service-name>:
    image: ghcr.io/<github-owner>/<service-name>:latest
    container_name: <service-name>
    restart: unless-stopped
    ports: ["<host-port>:8080"]
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://dts-postgres:5432/<db-name>
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_KAFKA_BOOTSTRAP-SERVERS: dts-kafka:9092
    networks: [dts-network]

networks:
  dts-network: { external: true }
```

### `.github/workflows/deploy.yml`
```yaml
name: Build & Deploy

on:
  push:
    branches: [main]

permissions:
  contents: read
  packages: write

env:
  IMAGE: ghcr.io/${{ github.repository_owner }}/<service-name>
  DEPLOY_DIR: /dts/<service-name>

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 21 }

      # Gradle:
      - uses: gradle/actions/setup-gradle@v4
      - run: chmod +x gradlew && ./gradlew clean bootJar

      # Maven (use if Maven):
      # - uses: actions/cache@v4
      #   with: { path: ~/.m2/repository, key: '${{ runner.os }}-maven-${{ hashFiles("**/pom.xml") }}' }
      # - run: mvn clean package -B -DskipTests

      - uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - run: |
          docker build -t $IMAGE:latest -t $IMAGE:${{ github.sha }} .
          docker push $IMAGE:latest
          docker push $IMAGE:${{ github.sha }}

      - uses: appleboy/scp-action@v0.1.7
        with:
          host: ${{ secrets.VPS_HOST }}
          username: ${{ secrets.VPS_USER }}
          password: ${{ secrets.VPS_PASSWORD }}
          source: "docker-compose.prod.yml"
          target: ${{ env.DEPLOY_DIR }}

      - uses: appleboy/ssh-action@v1.2.2
        with:
          host: ${{ secrets.VPS_HOST }}
          username: ${{ secrets.VPS_USER }}
          password: ${{ secrets.VPS_PASSWORD }}
          script: |
            cd ${{ env.DEPLOY_DIR }}
            mv -f docker-compose.prod.yml docker-compose.yml
            docker compose pull
            docker compose up -d
```

---

## 4. CHECKLIST
- Set GHCR Package access to **Write/Admin** (`https://github.com/users/<owner>/packages/container/<service-name>/settings`).
- Configure `VPS_HOST`, `VPS_USER`, `VPS_PASSWORD` in Repo Secrets.
- VPS network `dts-network` created (`docker network create dts-network`).
