# 04 — Implementation Plan and Architecture

## 1. Recommended source structure

```text
src/
├── main/
│   ├── java/
│   │   └── ar/
│   │       └── edu/
│   │           └── doctorlukmanov/
│   │               ├── AplicacionClinica.java
│   │               ├── configuracion/
│   │               │   ├── ConfiguracionAplicacion.java
│   │               │   └── FabricaDependencias.java
│   │               ├── modelo/
│   │               │   ├── Persona.java
│   │               │   ├── Cliente.java
│   │               │   ├── Veterinario.java
│   │               │   ├── Gato.java
│   │               │   ├── Turno.java
│   │               │   ├── Atencion.java
│   │               │   ├── Tratamiento.java
│   │               │   ├── DetalleTratamiento.java
│   │               │   ├── EstadoTurno.java
│   │               │   └── SexoGato.java
│   │               ├── dto/
│   │               │   ├── ClienteFormularioDto.java
│   │               │   ├── GatoFormularioDto.java
│   │               │   ├── TurnoFormularioDto.java
│   │               │   ├── CierreAtencionDto.java
│   │               │   ├── DetalleTratamientoDto.java
│   │               │   ├── FiltroReporteDto.java
│   │               │   ├── ReporteTratamientoFrecuenteDto.java
│   │               │   ├── ReporteTurnosMensualesDto.java
│   │               │   ├── ReporteProductividadVeterinarioDto.java
│   │               │   └── HistoriaClinicaDto.java
│   │               ├── dao/
│   │               │   ├── DaoCrud.java
│   │               │   ├── ClienteDao.java
│   │               │   ├── GatoDao.java
│   │               │   ├── VeterinarioDao.java
│   │               │   ├── TurnoDao.java
│   │               │   ├── AtencionDao.java
│   │               │   ├── TratamientoDao.java
│   │               │   ├── ReporteDao.java
│   │               │   └── sqlite/
│   │               │       ├── ClienteDaoSqlite.java
│   │               │       ├── GatoDaoSqlite.java
│   │               │       ├── VeterinarioDaoSqlite.java
│   │               │       ├── TurnoDaoSqlite.java
│   │               │       ├── AtencionDaoSqlite.java
│   │               │       ├── TratamientoDaoSqlite.java
│   │               │       ├── ReporteDaoSqlite.java
│   │               │       └── MapeadorResultado.java
│   │               ├── servicio/
│   │               │   ├── ServicioCliente.java
│   │               │   ├── ServicioGato.java
│   │               │   ├── ServicioVeterinario.java
│   │               │   ├── ServicioTurno.java
│   │               │   ├── ServicioTratamiento.java
│   │               │   └── ServicioReporte.java
│   │               ├── estrategia/
│   │               │   ├── EstrategiaReporte.java
│   │               │   ├── TipoReporte.java
│   │               │   ├── EstrategiaTratamientosFrecuentes.java
│   │               │   ├── EstrategiaTurnosMensuales.java
│   │               │   ├── EstrategiaPacientesPorMes.java
│   │               │   └── EstrategiaProductividadVeterinario.java
│   │               ├── controlador/
│   │               │   ├── ControladorCliente.java
│   │               │   ├── ControladorGato.java
│   │               │   ├── ControladorVeterinario.java
│   │               │   ├── ControladorTurno.java
│   │               │   ├── ControladorTratamiento.java
│   │               │   └── ControladorReporte.java
│   │               ├── vista/
│   │               │   ├── VentanaPrincipal.java
│   │               │   ├── componentes/
│   │               │   │   ├── ModeloTablaNoEditable.java
│   │               │   │   ├── SelectorFechaHora.java
│   │               │   │   └── Dialogos.java
│   │               │   ├── cliente/
│   │               │   │   ├── PanelClientes.java
│   │               │   │   └── DialogoCliente.java
│   │               │   ├── gato/
│   │               │   │   ├── PanelGatos.java
│   │               │   │   └── DialogoGato.java
│   │               │   ├── veterinario/
│   │               │   │   ├── PanelVeterinarios.java
│   │               │   │   └── DialogoVeterinario.java
│   │               │   ├── turno/
│   │               │   │   ├── PanelTurnos.java
│   │               │   │   ├── DialogoTurno.java
│   │               │   │   ├── DialogoCancelarTurno.java
│   │               │   │   └── DialogoCompletarTurno.java
│   │               │   ├── tratamiento/
│   │               │   │   ├── PanelTratamientos.java
│   │               │   │   └── DialogoTratamiento.java
│   │               │   └── reporte/
│   │               │       └── PanelReportes.java
│   │               ├── excepcion/
│   │               │   ├── ClinicaException.java
│   │               │   ├── ValidacionException.java
│   │               │   ├── EntidadNoEncontradaException.java
│   │               │   ├── TurnoNoDisponibleException.java
│   │               │   ├── TransicionTurnoInvalidaException.java
│   │               │   └── PersistenciaException.java
│   │               └── util/
│   │                   ├── ConexionBaseDatos.java
│   │                   ├── InicializadorBaseDatos.java
│   │                   ├── TrabajoTransaccional.java
│   │                   ├── ColeccionesUtil.java
│   │                   ├── Validador.java
│   │                   ├── FechasUtil.java
│   │                   └── RegistroErrores.java
│   └── resources/
│       ├── configuracion.properties
│       └── sql/
│           └── crear_base_datos.sql
└── test/
    └── java/
        └── ar/edu/doctorlukmanov/
            ├── modelo/
            ├── servicio/
            ├── dao/
            └── integracion/
```

## 2. Ordered implementation plan

### Step 1 — Create the project skeleton

1. Create the Maven project and package structure.
2. Add Java 17 compiler settings.
3. Add SQLite JDBC and JUnit dependencies.
4. Add `configuracion.properties`.
5. Copy the SQL script into resources.

**Checkpoint:** `mvn test` succeeds with an empty test class.

### Step 2 — Implement configuration and the Singleton connection manager

Implement:

- `ConfiguracionAplicacion`
- `ConexionBaseDatos`
- `InicializadorBaseDatos`
- `TrabajoTransaccional<R>`

`ConexionBaseDatos` must:

- Be a Singleton.
- Read the JDBC URL from configuration.
- Return configured connections.
- Enable foreign keys.
- Set an appropriate busy timeout.
- Provide a transaction helper.

`InicializadorBaseDatos` must:

- Check whether `version_esquema` exists.
- Execute the creation script when required.
- Avoid recreating or deleting existing operational data.

**Checkpoint:** a test opens the database and verifies `PRAGMA foreign_keys` returns `1`.

### Step 3 — Implement enums and domain models

Implement in this order:

1. `EstadoTurno`
2. `SexoGato`
3. `Persona`
4. `Cliente`
5. `Veterinario`
6. `Gato`
7. `Tratamiento`
8. `DetalleTratamiento`
9. `Atencion`
10. `Turno`

Key model rule: `Turno` owns the state-transition logic. A setter such as `setEstado(...)` should not be publicly available without validation.

Suggested transition map:

```text
PROGRAMADO  → CONFIRMADO, COMPLETADO, CANCELADO
CONFIRMADO  → COMPLETADO, CANCELADO
COMPLETADO  → none
CANCELADO   → none
```

**Checkpoint:** unit tests verify all allowed and rejected transitions.

### Step 4 — Implement the exception hierarchy

Create all custom exceptions before service development so every layer can use a consistent policy.

Recommended constructors:

- message only
- message and cause
- entity type and identifier for not-found errors
- current and requested state for transition errors

**Checkpoint:** state-transition tests assert the exact custom exception type.

### Step 5 — Implement DTOs and validation

Form DTOs should be immutable records when the course permits Java records. Otherwise, use regular classes with private fields and getters.

Implement `Validador` methods for:

- required text
- email format
- non-negative decimal values
- date not in future
- date-time not in past
- required identifier

Validation should occur in the service layer even if Swing also performs immediate field checks.

**Checkpoint:** invalid form DTOs produce `ValidacionException` before any SQL runs.

### Step 6 — Implement generic DAO contracts

Implement `DaoCrud<T, ID>` and the specific DAO interfaces.

CRUD expectations for every main entity:

- `crear`
- `buscarPorId`
- `listarTodos`
- `actualizar`
- `eliminar`

For historical entities such as `Turno` and `Atencion`, `eliminar` may be restricted or implemented only for test/administrative scenarios. Normal UI behaviour should favour cancellation or logical deactivation.

**Checkpoint:** DAO interfaces compile independently from Swing.

### Step 7 — Implement SQLite DAO classes

Implement one DAO at a time:

1. `ClienteDaoSqlite`
2. `GatoDaoSqlite`
3. `VeterinarioDaoSqlite`
4. `TratamientoDaoSqlite`
5. `TurnoDaoSqlite`
6. `AtencionDaoSqlite`
7. `ReporteDaoSqlite`

Rules:

- Use `PreparedStatement`.
- Use try-with-resources for `Connection`, `PreparedStatement`, and `ResultSet`.
- Convert `LocalDate` and `LocalDateTime` using ISO strings.
- Convert SQLite integers to booleans explicitly.
- Return `Optional<T>` for single-item searches.
- Wrap SQL failures in `PersistenciaException`.
- Keep row-mapping in private methods or `MapeadorResultado` helpers.

**Checkpoint:** integration tests perform CRUD against a temporary database.

### Step 8 — Implement the client ABM module

`ServicioCliente` responsibilities:

- Validate the DTO.
- Normalise DNI, email, and whitespace.
- Prevent duplicate DNI.
- Create and update `Cliente`.
- Decide whether delete means physical deletion or deactivation.

`ControladorCliente` responsibilities:

- Read values from `DialogoCliente`.
- Call the service.
- Refresh the client table.
- Convert exceptions into Spanish messages.

UI actions:

- **"Nuevo cliente"**
- **"Editar cliente"**
- **"Eliminar cliente"**
- **"Activar/Desactivar"**
- **"Buscar"**
- **"Ver gatos"**

**Checkpoint:** complete create/read/update/delete demonstration for `Cliente`.

### Step 9 — Implement the cat ABM module

`ServicioGato` must verify that the selected client exists and is active. It must prevent duplicate microchip numbers.

UI actions:

- **"Registrar gato"**
- **"Editar gato"**
- **"Eliminar gato"**
- **"Activar/Desactivar"**
- **"Buscar por nombre"**
- **"Filtrar por cliente"**
- **"Ver historia clínica"**

A cat form should include a client selector populated from active clients.

**Checkpoint:** complete create/read/update/delete demonstration for `Gato`, including its relationship with `Cliente`.

### Step 10 — Implement veterinarian and treatment maintenance

These modules can be simpler but should still use DAO and service layers.

Veterinarian fields:

- name
- surname
- licence number
- telephone
- email
- specialty
- active state

Treatment fields:

- name
- description
- reference price
- active state

**Checkpoint:** the appointment form can load active veterinarians, and the completion form can load active treatments.

### Step 11 — Implement appointment booking

#### Input

`TurnoFormularioDto` contains:

- `idGato`
- `idVeterinario`
- `fechaHora`
- `motivo`
- `observaciones`

#### Service validations

1. The cat exists and is active.
2. The veterinarian exists and is active.
3. The appointment is in the future.
4. The reason is present.
5. No open appointment overlaps the selected slot.

#### Persistence

Create the appointment with state `PROGRAMADO`.

#### UI

`DialogoTurno` should allow:

- Search/select client.
- Select one of that client's cats.
- Select veterinarian.
- Select date and time.
- Enter reason and notes.

**Checkpoint:** trying to double-book the same veterinarian throws `TurnoNoDisponibleException` and does not insert a second row.

### Step 12 — Implement appointment confirmation

`ServicioTurno.confirmar(idTurno)`:

1. Load the appointment.
2. Call `turno.cambiarEstado(CONFIRMADO)`.
3. Persist the new state.
4. Refresh the daily agenda.

Only `PROGRAMADO` appointments can be confirmed.

### Step 13 — Implement cancellation closing cycle

`ServicioTurno.cancelar(idTurno, motivo)`:

1. Require a non-empty cancellation reason.
2. Load the appointment.
3. Validate the transition to `CANCELADO`.
4. Set `fechaCierre` to the current timestamp.
5. Persist state, closure date, and reason.

No `Atencion` row is created.

The UI must ask for confirmation and display the selected cat, owner, and date before cancellation.

### Step 14 — Implement completed-attention closing cycle

This is the most important transactional use case.

#### Input

`CierreAtencionDto`:

- appointment identifier
- diagnosis
- recorded weight
- temperature
- clinical observations
- indications
- list of treatment details

#### Transaction sequence

```text
BEGIN
  load Turno
  validate current state
  verify no Atencion already exists
  insert Atencion
  insert each AtencionTratamiento
  update Turno to COMPLETADO and set fechaCierre
COMMIT
```

On error:

```text
ROLLBACK
throw appropriate ClinicaException
```

#### Additional update

When `pesoRegistrado` is provided, optionally update `gatos.peso_actual` within the same transaction. This keeps the patient record current without losing historical weights stored in `atenciones`.

#### UI

`DialogoCompletarTurno` displays read-only appointment data plus editable clinical fields and a treatment table.

Treatment-table columns:

- **"Tratamiento"**
- **"Dosis"**
- **"Frecuencia"**
- **"Duración (días)"**
- **"Cantidad"**
- **"Precio aplicado"**
- **"Observaciones"**

Buttons:

- **"Agregar tratamiento"**
- **"Quitar tratamiento"**
- **"Completar turno"**
- **"Cancelar"**

**Checkpoint:** simulate a failure during treatment insertion and verify that neither the attention nor the completed state remains in the database.

### Step 15 — Implement Strategy-based reports

Create `TipoReporte` enum values:

- `PACIENTES_POR_MES`
- `TURNOS_POR_MES`
- `TRATAMIENTOS_FRECUENTES`
- `PRODUCTIVIDAD_VETERINARIO`
- `HISTORIA_CLINICA`

Each strategy:

1. Validates `FiltroReporteDto`.
2. Calls the correct DAO query.
3. Returns a typed `List<R>`.
4. Supplies column metadata or lets the controller select a table model.

`ServicioReporte` receives all strategies and indexes them with the generic `ColeccionesUtil.indexarPor(...)` method.

**Checkpoint:** switching the report selector changes the strategy without changing `PanelReportes` SQL logic.

### Step 16 — Implement the main `JFrame`

`VentanaPrincipal extends JFrame`.

Recommended layout:

- `BorderLayout`
- north: title and current date
- west: optional navigation panel
- centre: `CardLayout` for functional panels
- south: status bar

Recommended menu bar:

```text
Archivo
├── Inicio
├── Respaldar base de datos
└── Salir

Clientes
├── Gestionar clientes
└── Registrar cliente

Gatos
├── Gestionar gatos
├── Registrar gato
└── Historia clínica

Turnos
├── Agenda diaria
├── Programar turno
├── Confirmar turno
├── Completar turno
└── Cancelar turno

Veterinarios
└── Gestionar veterinarios

Tratamientos
└── Gestionar tratamientos

Reportes
├── Pacientes por mes
├── Turnos por mes
├── Tratamientos frecuentes
└── Productividad veterinaria

Ayuda
└── Acerca de Doctor Lukmanov
```

Every menu item calls a controller or changes the visible panel. It must not run SQL directly.

### Step 17 — Wire dependencies

`FabricaDependencias` creates objects in this order:

1. Singleton connection manager.
2. DAO implementations.
3. Services.
4. Report strategies.
5. Controllers.
6. Panels and dialogs.
7. `VentanaPrincipal`.

Prefer constructor injection:

```text
PanelTurnos(ControladorTurno controladorTurno)
ControladorTurno(ServicioTurno servicioTurno)
ServicioTurno(TurnoDao turnoDao, GatoDao gatoDao, ...)
```

This avoids hidden global dependencies and makes unit tests straightforward.

### Step 18 — Implement error handling and logging

For every button action:

1. Disable the action button while processing.
2. Validate the input.
3. Call the controller/service inside try-catch.
4. Show a success or error dialog.
5. Re-enable the button in `finally`.

Use `java.util.logging` or a simple file logger:

```text
logs/doctor-lukmanov.log
```

Log:

- timestamp
- operation
- exception type
- message
- stack trace

Do not log unnecessary personal details.

### Step 19 — Testing and demonstration data

Create test data that proves:

- one client can own multiple cats
- one cat can have multiple appointments
- appointments can be completed and canceled
- treatments appear in frequency reports
- reports span multiple months
- duplicate DNI and microchip values are rejected
- double-booking is rejected
- a terminal appointment cannot be reopened

Use a separate temporary database for automated tests.

### Step 20 — Final packaging

Deliver:

- source code
- `pom.xml`
- SQL creation script
- sample or empty database
- README with execution commands
- UML diagram
- test report or screenshots
- short demonstration checklist aligned with the rubric

## 3. Controller-to-database flow

Example: **"Programar turno"**

```text
Button click
  → DialogoTurno extracts fields
  → ControladorTurno.programar(dto)
  → ServicioTurno.programar(dto)
  → GatoDao.buscarPorId
  → VeterinarioDao.buscarPorId
  → TurnoDao.existeSuperposicion
  → TurnoDao.crear
  → controller refreshes PanelTurnos
```

Example: **"Completar turno"**

```text
Button click
  → DialogoCompletarTurno creates CierreAtencionDto
  → ControladorTurno.completar(dto)
  → ServicioTurno.completar(dto)
  → ConexionBaseDatos.ejecutarEnTransaccion
       → TurnoDao loads and validates
       → AtencionDao inserts attention
       → AtencionDao inserts treatments
       → TurnoDao marks appointment COMPLETADO
  → controller shows confirmation
  → agenda and reports are refreshed
```

## 4. GUI implementation details

### Event Dispatch Thread

All Swing creation and visible-state updates must occur on the Event Dispatch Thread:

```java
SwingUtilities.invokeLater(() -> {
    VentanaPrincipal ventana = fabrica.crearVentanaPrincipal();
    ventana.setVisible(true);
});
```

### Long queries

Use `SwingWorker` for report generation or operations that may block the UI. The background task calls the controller; `done()` updates the table.

### Table models

Use dedicated table models or a reusable non-editable model. Do not let users edit database records directly inside a `JTable` unless validation and commit semantics are explicitly implemented.

### Dialog behaviour

- Use modal `JDialog` forms for create/update operations.
- Disable **"Guardar"** until required fields are present when practical.
- Show field-level messages and a summary dialog.
- Confirm destructive actions.
- Preserve entered values after validation errors.

## 5. ABM implementation contract

At minimum, `Cliente` and `Gato` must demonstrate complete ABM.

### Create / Alta

- Show form.
- Validate data.
- Insert with DAO.
- Return generated identifier.
- Refresh table.

### Read / Consulta

- List all active records.
- Search by identifier and text.
- Show details and relationships.

### Update / Modificación

- Load selected record.
- Pre-fill form.
- Validate changed data.
- Update through DAO.
- Refresh table.

### Delete / Baja

Preferred normal operation:

- Set `activo = 0`.
- Hide from default active lists.
- Preserve history.

Optional physical delete:

- Only if no dependent records exist.
- Catch foreign-key failure.
- Explain why deletion is blocked.

## 6. Critical implementation risks

### Risk: foreign keys not enforced

Mitigation: execute `PRAGMA foreign_keys = ON` for every newly opened connection and test it.

### Risk: UI contains SQL

Mitigation: enforce package boundaries and code review; only DAO classes contain SQL.

### Risk: partial appointment completion

Mitigation: one explicit transaction for attention, treatment details, cat weight update, and appointment state.

### Risk: invalid state changes

Mitigation: state-transition rules in the `Turno` domain class and repeated validation in `ServicioTurno`.

### Risk: report code becomes a large conditional

Mitigation: Strategy pattern and a strategy map.

### Risk: physical deletion destroys history

Mitigation: logical deactivation and foreign-key restrictions.

## 7. Recommended implementation milestones

| Milestone | Deliverable |
|---|---|
| 1 | Project starts, database initialises, connection test passes. |
| 2 | Domain models, exceptions, and transition tests pass. |
| 3 | `Cliente` and `Gato` DAOs and services pass integration tests. |
| 4 | Client and cat ABM screens work. |
| 5 | Veterinarian, treatment, and appointment booking work. |
| 6 | Confirmation, cancellation, and transactional completion work. |
| 7 | Reports work through Strategy implementations. |
| 8 | Main menu, error handling, logging, tests, and final documentation are complete. |
