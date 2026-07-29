# Clínica Veterinaria Doctor Lukmanov

Desktop management system for a veterinary clinic dedicated exclusively to feline patients. The application is implemented in Java 17 with Swing, JDBC, and SQLite. Source identifiers, business states, validation messages, and all visible interface text are in Spanish.

## Features

- Complete ABM for clients and cats, including logical deactivation and owner relationships.
- Veterinarian and treatment catalog maintenance.
- Appointment booking with active-entity checks and duration-aware schedule conflict detection.
- Controlled `PROGRAMADO`, `CONFIRMADO`, `COMPLETADO`, and `CANCELADO` lifecycle.
- Transactional clinical closure with diagnosis, weight, temperature, indications, and treatment details.
- Atomic rollback across attention, treatment details, cat weight, and appointment state.
- Clinical history and four statistical reports implemented with Strategy.
- SQLite schema initialization, constraints, indexes, views, and treatment seed catalog.
- Database backup and file-based error logging.
- Spanish Swing interface with state-aware actions and background report generation.

## Requirements

- JDK 17 or newer.
- Maven 3.8 or newer.

Verify the installation:

```bash
java -version
mvn -version
```

## Run

From the repository root:

```bash
mvn clean test
mvn exec:java
```

The first launch creates `data/doctor_lukmanov.db` and initializes the complete schema automatically. SQLite foreign keys are enabled for every connection.

## Package

Build the self-contained executable JAR:

```bash
mvn clean package
java -jar target/doctor-lukmanov-1.0.0.jar
```

## Runtime Files

- Database: `data/doctor_lukmanov.db`
- Backups: `backups/doctor_lukmanov_YYYYMMDD_HHMMSS_SSS.db`
- Error log: `logs/doctor-lukmanov.log`
- Configuration: `src/main/resources/configuracion.properties`

Override the database URL without editing the resource file:

```bash
java -Ddoctorlukmanov.baseDatosUrl=jdbc:sqlite:/ruta/clinica.db \
  -jar target/doctor-lukmanov-1.0.0.jar
```

## Architecture

```text
Swing view -> controller -> service -> DAO interface -> SQLite JDBC
```

The source is organized under `ar.edu.doctorlukmanov`:

- `modelo`: domain entities, inheritance, enums, and lifecycle rules.
- `dto`: immutable form and report records.
- `dao` and `dao.sqlite`: generic contracts and prepared JDBC implementations.
- `servicio`: validations, use cases, and transaction boundaries.
- `estrategia`: polymorphic report algorithms.
- `controlador`: presentation-facing operations.
- `vista`: main `JFrame`, module panels, dialogs, and reusable components.
- `configuracion` and `util`: dependency wiring, Singleton connection manager, schema initialization, logging, and generic helpers.

Implemented patterns include Singleton, DAO, DTO, Strategy, and constructor-based dependency injection. `Persona` is the abstract superclass of `Cliente` and `Veterinario`; `DaoCrud<T, ID>` and `EstrategiaReporte<R>` demonstrate generic abstraction and polymorphism.

## Tests

The automated suite uses JUnit 5 and temporary SQLite databases. It covers:

- model validation and every appointment-state transition;
- generic collection helpers;
- schema initialization and foreign-key enforcement;
- CRUD and uniqueness rules;
- schedule conflicts and terminal-state behavior;
- successful transactional completion and forced rollback;
- all report queries, percentages, ordering, and empty ranges;
- Swing panel construction on the Event Dispatch Thread;
- readable database backup creation.

Run one integration test when diagnosing a workflow:

```bash
mvn -Dtest=ServicioTurnoIntegracionTest test
```

## Project Documentation

- Complete original specification: `DoctorLukmanov_ProjectPlan/`
- SQL script: `scripts/crear_base_datos.sql`
- Demonstration checklist: `docs/DEMONSTRATION_CHECKLIST.md`
