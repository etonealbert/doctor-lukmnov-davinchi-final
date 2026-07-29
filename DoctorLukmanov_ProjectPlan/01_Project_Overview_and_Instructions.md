# 01 — Project Overview and Instructions

## 1. General description

**Clínica Veterinaria "Doctor Lukmanov"** is a Java Swing desktop application for managing a veterinary clinic that treats cats exclusively. It centralises owner records, feline patient records, appointments, clinical attention, treatments, and operational reports in a single SQLite database.

The documentation is written in English, while the proposed software artefacts use Spanish terminology. Examples include `Cliente`, `Gato`, `Turno`, `Atencion`, `Tratamiento`, `VentanaPrincipal`, `fechaNacimiento`, `estadoTurno`, and UI labels such as **"Registrar gato"**, **"Buscar cliente"**, and **"Finalizar turno"**.

The application is intended for reception and clinical staff. It is deliberately designed as a local desktop system using `JFrame`, JDBC, and SQLite so it can be implemented within the scope of a Java final examination without requiring an application server.

## 2. Main objectives

The system must allow staff to:

1. Register, search, edit, deactivate, and delete clients.
2. Register, search, edit, deactivate, and delete cats linked to clients.
3. Book appointments for cats and veterinarians.
4. Confirm, complete, or cancel appointments according to controlled business rules.
5. Record clinical attention, diagnosis, weight, observations, and treatments.
6. Maintain a treatment catalog.
7. Generate statistical reports based on stored operational and clinical data.
8. Preserve data integrity through foreign keys, validations, transactions, and exception handling.

## 3. Main entities and relationships

### 3.1 `Cliente`

Represents the cat's owner or responsible person.

Suggested fields:

- `idCliente`
- `nombre`
- `apellido`
- `dni`
- `telefono`
- `correoElectronico`
- `direccion`
- `activo`
- `fechaRegistro`

A client may own one or more cats.

### 3.2 `Gato`

Represents a feline patient.

Suggested fields:

- `idGato`
- `idCliente`
- `nombre`
- `fechaNacimiento`
- `sexo`
- `raza`
- `color`
- `pesoActual`
- `numeroMicrochip`
- `esterilizado`
- `alergias`
- `observaciones`
- `activo`

Every cat must be linked to one client.

### 3.3 `Turno`

Represents an appointment for a cat with a veterinarian.

Suggested fields:

- `idTurno`
- `idGato`
- `idVeterinario`
- `fechaHora`
- `motivo`
- `estado`
- `fechaCreacion`
- `fechaCierre`
- `motivoCancelacion`
- `observaciones`

A cat may have many appointments. A veterinarian may attend many appointments.

### 3.4 `Atencion`

Represents the clinical result of a completed appointment.

Suggested fields:

- `idAtencion`
- `idTurno`
- `diagnostico`
- `pesoRegistrado`
- `temperatura`
- `observacionesClinicas`
- `indicaciones`
- `fechaRegistro`

A completed appointment has zero or one clinical attention record. It has zero while the appointment is still open, and exactly one when a clinical consultation is completed successfully.

### 3.5 `Tratamiento`

Represents a reusable treatment catalog item, such as vaccination, deworming, wound care, medication administration, or nutritional counselling.

It has a many-to-many relationship with `Atencion` through `AtencionTratamiento`.

## 4. Main business process: appointment opening and closing cycle

The complete process links `Cliente`, `Gato`, `Turno`, `Veterinario`, `Atencion`, and `Tratamiento`.

### Opening cycle

1. A receptionist selects or creates a `Cliente`.
2. The receptionist selects or creates a `Gato` owned by that client.
3. The receptionist chooses a veterinarian, date, and time.
4. The system verifies that the cat is active, the veterinarian is active, the date is not in the past, and the veterinarian is available.
5. The system creates a `Turno` with state `PROGRAMADO`.
6. Optionally, the appointment is moved to `CONFIRMADO` before the visit.

### Closing cycle — completed

1. Staff opens a `PROGRAMADO` or `CONFIRMADO` appointment.
2. The veterinarian records diagnosis, weight, temperature, observations, indications, and treatments.
3. The system inserts one `Atencion` record and any required `AtencionTratamiento` records inside one database transaction.
4. The appointment state changes to `COMPLETADO` and `fechaCierre` is stored.
5. The transaction is committed only when all inserts and updates succeed.

### Closing cycle — canceled

1. Staff selects an open appointment.
2. A cancellation reason is required.
3. The system changes the state to `CANCELADO`, saves `motivoCancelacion`, and stores `fechaCierre`.
4. A canceled appointment cannot receive an `Atencion` record.

### Business-state transitions

Allowed transitions:

- `PROGRAMADO` → `CONFIRMADO`
- `PROGRAMADO` → `CANCELADO`
- `PROGRAMADO` → `COMPLETADO`
- `CONFIRMADO` → `CANCELADO`
- `CONFIRMADO` → `COMPLETADO`

Terminal states:

- `COMPLETADO`
- `CANCELADO`

Any invalid transition must throw `TransicionTurnoInvalidaException`.

## 5. Reports and statistics

The database stores sufficient information to generate at least the following reports:

- Number of feline patients registered per month.
- Number of appointments per month, grouped by state.
- Appointment completion and cancellation percentages.
- Most frequent treatments within a selected date range.
- Number of consultations per veterinarian.
- Cats with the highest number of consultations.
- Average recorded patient weight by age group or breed.
- Clients with the largest number of active cats.
- Upcoming appointments for a selected day or week.
- Clinical history for a selected cat.

The report layer should return DTOs rather than domain entities, because report rows normally combine columns from several tables.

## 6. Technology and architecture

- Java 17 or later.
- Java Swing, using a main `JFrame` and child dialogs or internal panels.
- JDBC for persistence.
- SQLite as the embedded relational database.
- Layered architecture:
  - `modelo`
  - `dto`
  - `dao`
  - `servicio`
  - `controlador`
  - `vista`
  - `excepcion`
  - `util`
  - `estrategia`
- Design patterns:
  - Singleton
  - DAO
  - DTO
  - Strategy as the additional pattern
- Optional supporting patterns:
  - Factory for DAO creation
  - Observer-style UI refresh through Java listeners

## 7. Compilation and execution instructions

Two supported approaches are described below. Maven is recommended because it handles the SQLite JDBC dependency automatically.

### 7.1 Prerequisites

Install:

- JDK 17 or later.
- Maven 3.8 or later, for the recommended build method.
- A code editor or IDE such as IntelliJ IDEA, Eclipse, or NetBeans.

Verify the tools:

```bash
java -version
javac -version
mvn -version
```

### 7.2 Recommended Maven project configuration

Create this root structure:

```text
DoctorLukmanov/
├── pom.xml
├── data/
│   └── doctor_lukmanov.db
├── scripts/
│   └── crear_base_datos.sql
└── src/
    ├── main/
    │   ├── java/
    │   │   └── ar/edu/doctorlukmanov/
    │   └── resources/
    │       └── configuracion.properties
    └── test/
        └── java/
```

Use a `pom.xml` equivalent to the following. The SQLite driver version may be replaced with another version approved by the course.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>ar.edu</groupId>
    <artifactId>doctor-lukmanov</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.46.1.0</version>
        </dependency>

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.3</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.4.1</version>
                <configuration>
                    <mainClass>ar.edu.doctorlukmanov.AplicacionClinica</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 7.3 Create the database

1. Create the `data` and `scripts` directories.
2. Copy the SQL from `03_Database_Script.md` into `scripts/crear_base_datos.sql`.
3. Either execute the script with an SQLite command-line tool or let the Java application initialise the schema on first run.

Command-line option:

```bash
sqlite3 data/doctor_lukmanov.db < scripts/crear_base_datos.sql
```

Java-initialisation option:

- `InicializadorBaseDatos` reads `/sql/crear_base_datos.sql` from resources.
- It opens a connection using `ConexionBaseDatos`.
- It executes each complete statement inside a transaction.
- It records the schema version in `version_esquema`.

### 7.4 Configure the database path

Create `src/main/resources/configuracion.properties`:

```properties
base.datos.url=jdbc:sqlite:data/doctor_lukmanov.db
base.datos.foreign_keys=true
aplicacion.nombre=Clínica Veterinaria Doctor Lukmanov
```

The Singleton connection manager should read this file and fall back to:

```text
jdbc:sqlite:data/doctor_lukmanov.db
```

### 7.5 Compile and run with Maven

From the project root:

```bash
mvn clean test
mvn compile
mvn exec:java
```

To package the application:

```bash
mvn clean package
```

If an executable JAR plugin is added later, run:

```bash
java -jar target/doctor-lukmanov-1.0.0.jar
```

### 7.6 Compile and run manually without Maven

Place the SQLite JDBC JAR in `lib/`:

```text
DoctorLukmanov/
├── lib/
│   └── sqlite-jdbc.jar
├── out/
├── data/
└── src/
```

Linux or macOS:

```bash
find src/main/java -name "*.java" > fuentes.txt
javac -encoding UTF-8 -cp "lib/sqlite-jdbc.jar" -d out @fuentes.txt
java -cp "out:lib/sqlite-jdbc.jar" ar.edu.doctorlukmanov.AplicacionClinica
```

Windows PowerShell:

```powershell
Get-ChildItem -Recurse src/main/java -Filter *.java | ForEach-Object FullName | Set-Content fuentes.txt
javac -encoding UTF-8 -cp "lib/sqlite-jdbc.jar" -d out '@fuentes.txt'
java -cp "out;lib/sqlite-jdbc.jar" ar.edu.doctorlukmanov.AplicacionClinica
```

### 7.7 First execution

The main method should:

1. Set the system look and feel.
2. Initialise the database schema if necessary.
3. Test the connection.
4. Start the UI on the Swing Event Dispatch Thread.
5. Display `VentanaPrincipal`.

Conceptual sequence:

```text
AplicacionClinica.main
  → SwingUtilities.invokeLater
  → InicializadorBaseDatos.inicializar
  → FabricaDependencias.crearControladores
  → new VentanaPrincipal(...)
  → setVisible(true)
```

## 8. Exam-requirement traceability checklist

| Exam requirement | Planned evidence in this project |
|---|---|
| At least three related entities | `Cliente`, `Gato`, and `Turno`; additionally `Veterinario`, `Atencion`, and `Tratamiento`. |
| Complete opening/closing process | Appointment cycle from `PROGRAMADO` or `CONFIRMADO` to `COMPLETADO` or `CANCELADO`, including transactional creation of `Atencion`. |
| Reports/statistics | Monthly patients, appointments by state, treatment frequency, veterinarian productivity, clinical history, and other queries. |
| Inheritance | `Persona` is the abstract superclass of `Cliente` and `Veterinario`. |
| Polymorphism | Collections and methods can treat `Cliente` and `Veterinario` as `Persona`; report strategies implement a shared interface. |
| Abstraction | `Persona` and `DaoCrud<T, ID>` define common behaviour without concrete persistence details. |
| At least one interface | `DaoCrud<T, ID>`, specific DAO contracts, and `EstrategiaReporte<R>`. |
| At least one abstract class | `Persona`; optionally `ServicioBase`. |
| Complete ABM for at least two entities | Full create/read/update/delete for `Cliente` and `Gato`; CRUD is also planned for all main entities. |
| Try-catch error handling | Controllers, services, JDBC DAOs, transactions, initialisation, and UI actions include structured handling. |
| Custom business exception | `TransicionTurnoInvalidaException`; also `TurnoNoDisponibleException` and `EntidadNoEncontradaException`. |
| Generic collections | `ArrayList<Cliente>`, `List<Gato>`, `Map<EstadoTurno, Long>`, and other typed collections. |
| Custom generic method | `ColeccionesUtil.filtrar(List<T>, Predicate<T>)` and `DaoCrud<T, ID>`. |
| JDBC and SQLite | `ConexionBaseDatos` Singleton uses `DriverManager` with `jdbc:sqlite:`. |
| CRUD for all main entities | DAO contracts and SQLite implementations for `Cliente`, `Gato`, `Veterinario`, `Turno`, `Atencion`, and `Tratamiento`. |
| Singleton pattern | One `ConexionBaseDatos` instance controls connection creation and SQLite configuration. |
| DAO pattern | Persistence interfaces and `*DaoSqlite` implementations isolate SQL from business/UI code. |
| DTO pattern | Form and report DTOs transfer validated data without exposing persistence details. |
| Additional pattern | Strategy selects report-generation algorithms through `EstrategiaReporte<R>`. |
| `JFrame` GUI | `VentanaPrincipal extends JFrame`, with menus for all modules. |
| General description | Sections 1–6 of this document. |
| Design justification | `02_UML_and_Design_Decisions.md`. |
| Pattern explanation | `02_UML_and_Design_Decisions.md`. |
| Compilation/execution instructions | Section 7 of this document. |
| UML class diagram | Mermaid diagram in `02_UML_and_Design_Decisions.md`. |
| SQL script | Complete script in `03_Database_Script.md`. |

## 9. Definition of done

The project is considered complete when:

- The application starts without uncaught exceptions.
- SQLite foreign keys are enabled for every connection.
- Two complete ABM modules work for `Cliente` and `Gato`.
- Appointment booking prevents schedule conflicts.
- Appointment completion is transactional.
- Invalid state transitions are rejected with a custom exception.
- All main entities persist through JDBC DAOs.
- At least three reports return correct data for a chosen date range.
- The main `JFrame` provides access to every required function.
- Tests and demonstration evidence map directly to the exam rubric.
