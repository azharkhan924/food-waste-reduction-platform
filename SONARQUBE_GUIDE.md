# Local SonarQube & Code Coverage Guide

This guide walks you through the local setup and workflow for **SonarQube 10.7 (Active Community Edition)** and **JaCoCo** code coverage for the **Food Waste Reduction Platform**.

---

## 1. Quick Overview

- **SonarQube Version**: `10.7.0` (Active Community Edition)
- **SonarQube Server URL**: [http://localhost:9000](http://localhost:9000)
- **Project Dashboard**: [http://localhost:9000/dashboard?id=food-waste-reduction-platform](http://localhost:9000/dashboard?id=food-waste-reduction-platform)
- **Local SonarQube Installation Path**: `/Users/azharkhan/sonarqube-10.7.0.96327`
- **Configured Token**: `squ_1111222233334444555566667777888899990000`

---

## 2. Managing the Local SonarQube Server

### Start Server
```bash
PATH=/opt/homebrew/Cellar/openjdk@17/17.0.19/libexec/openjdk.jdk/Contents/Home/bin:$PATH JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.19/libexec/openjdk.jdk/Contents/Home /Users/azharkhan/sonarqube-10.7.0.96327/bin/macosx-universal-64/sonar.sh start
```

### Check Status
```bash
/Users/azharkhan/sonarqube-10.7.0.96327/bin/macosx-universal-64/sonar.sh status
```

### Stop Server
```bash
/Users/azharkhan/sonarqube-10.7.0.96327/bin/macosx-universal-64/sonar.sh stop
```

---

## 3. Running Analysis & Generating Coverage

### Run Build, Tests, and Sonar Scan in One Command:
```bash
mvn clean verify sonar:sonar -Dsonar.token=squ_1111222233334444555566667777888899990000
```

---

## 4. Local Coverage Report (HTML)

JaCoCo automatically generates a detailed browser-viewable HTML test coverage report:
- Location: `target/site/jacoco/index.html`
- To open in browser:
  ```bash
  open target/site/jacoco/index.html
  ```

---

## 5. Summary of Key Maven Commands

| Command | Purpose |
| :--- | :--- |
| `mvn clean test jacoco:report` | Run tests and generate local JaCoCo HTML report |
| `mvn clean verify` | Full compile, run tests, and generate coverage XML/HTML |
| `mvn sonar:sonar -Dsonar.token=squ_1111222233334444555566667777888899990000` | Run Sonar scan against running local SonarQube |
| `mvn clean verify sonar:sonar -Dsonar.token=squ_1111222233334444555566667777888899990000` | Single step to test, calculate coverage, and push to SonarQube |
