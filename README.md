# Simple Wallet - Digital Banking CLI

A miniature fintech backend simulation - a console-based Digital
Wallet application built with **Java 21 and Maven**.

The project demonstrates a realistic backend application structure using
**Clean Architecture**, **SOLID principles**, automated testing, code
coverage and static quality analysis preparation for SonarQube.

The main goal of the project is to provide a reproducible DevOps
learning artifact: 
- build the application
- run automated tests
- generate coverage reports
- package an executable Java application
- analyze source code quality with SonarQube

------------------------------------------------------------------------

### Application Overview

Simple Wallet is a command-line digital banking simulator.

Users can: 
- create wallet accounts; 
- deposit and withdraw money; 
- transfer funds between accounts; 
- view balances; 
- review transaction
- history; 
- close accounts.

The application uses domain-driven design concepts: 
- entities; 
- value objects; 
- use cases; 
- domain services; 
- ports and adapters.

------------------------------------------------------------------------

## Architecture

The project follows **Clean Architecture (Ports & Adapters)**.

``` text
Presentation Layer
        |
        v
CLI (WalletApp + CommandRouter)
        |
        v
Application Layer
        |
        v
Use Cases
        |
        v
Domain Layer
        |
        +-- Entities
        +-- Value Objects
        +-- Domain Services
        +-- Domain Events
        +-- Ports
        |
        v
Infrastructure Layer
        |
        +-- Repositories
        +-- JSON Persistence
        +-- Event Publishing
```

Dependency rule:

``` text
Domain        -> no external dependencies
Application   -> Domain
Infrastructure -> Domain
CLI           -> Application
```

------------------------------------------------------------------------

## Main Features

-   Account management
-   Deposit and withdrawal operations
-   Money transfer between accounts
-   Transaction history
-   Fraud detection rules
-   Domain events
-   JSON file persistence
-   Input validation
-   Console user interface

------------------------------------------------------------------------

## Technology Stack

  Technology        Purpose
  ----------------- ---------------------------------
  Java 21           Application language
  Maven             Build and dependency management
  JUnit 5           Automated testing
  Mockito           Mocking
  AssertJ           Assertions
  JaCoCo            Code coverage
  SLF4J + Logback   Logging
  Gson              JSON serialization



------------------------------------------------------------------------

## Requirements

-   Java 21+
-   Maven 3.9+

------------------------------------------------------------------------

## Build Application

``` bash
mvn clean verify
```

The command performs:

-   compilation;
-   unit tests;
-   integration tests;
-   coverage generation;
-   quality checks;
-   application packaging.

------------------------------------------------------------------------

## Run Application

After successful build:

``` bash
java -jar target/simple-wallet-1.0.0-SNAPSHOT.jar
```

Example:

``` text
wallet> create-account evg USD

[OK] Account created for 'evg' with currency USD

Account: evg
ID: 7494c9a4-ad3a-43ee-989f-d5abdc62c536
Balance: $0.00 USD
Status: ACTIVE
```

------------------------------------------------------------------------

## Testing Strategy

The project contains several testing levels:

  Layer            Type
  ---------------- -----------------------------
  Domain           Unit tests
  Application      Use case tests with Mockito
  Infrastructure   Integration tests
  CLI              End-to-end workflow tests

Run tests:

``` bash
mvn test
```

Run full verification including integration tests::

``` bash
mvn clean verify
```

Current project verification:

-   Unit tests: 109
-   Integration tests: 49
-   Total: 158 automated tests

------------------------------------------------------------------------

## SonarQube Integration

The project is prepared for SonarQube analysis.

Generated artifacts:

-   compiled classes;
-   test reports;
-   JaCoCo coverage reports.

After SonarQube server deployment:

``` bash
mvn sonar:sonar \
-Dsonar.host.url=http://localhost:9000 \
-Dsonar.token=YOUR_TOKEN
```

Expected result:

SonarQube dashboard will contain:

-   Bugs
-   Code Smells
-   Security Hotspots
-   Test Coverage
-   Quality Metrics

------------------------------------------------------------------------
