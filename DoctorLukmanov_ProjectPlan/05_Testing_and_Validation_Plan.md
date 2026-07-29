# 05 — Testing and Validation Plan

## 1. Testing objectives

The testing plan must prove that the application:

- enforces domain rules
- persists data correctly
- protects relational integrity
- completes multi-table operations atomically
- provides complete ABM for required entities
- handles errors without terminating unexpectedly
- generates correct statistics
- exposes all functionality through the Swing interface

## 2. Test levels

### 2.1 Unit tests

Target classes with no database or Swing dependency:

- `Turno`
- `Cliente`
- `Gato`
- `Atencion`
- `Validador`
- `ColeccionesUtil`
- report filter validation

### 2.2 DAO integration tests

Use a temporary SQLite database for each test class or test method.

Recommended URL:

```text
jdbc:sqlite:file:doctorlukmanov_test?mode=memory&cache=shared
```

Keep one anchor connection open while shared in-memory tests run, or use a temporary file database for simpler lifecycle management.

### 2.3 Service integration tests

Use real DAO implementations against the test database. These tests validate cross-entity rules and transactions.

### 2.4 GUI tests

Manual GUI tests are acceptable for an academic project, but each scenario should have a written expected result and screenshot evidence. Optional automated tests may use AssertJ Swing.

## 3. Core test cases

### Domain-state tests

| ID | Scenario | Expected result |
|---|---|---|
| TUR-01 | `PROGRAMADO` to `CONFIRMADO` | Allowed. |
| TUR-02 | `PROGRAMADO` to `CANCELADO` | Allowed with reason and closure date. |
| TUR-03 | `CONFIRMADO` to `COMPLETADO` | Allowed. |
| TUR-04 | `CANCELADO` to `COMPLETADO` | `TransicionTurnoInvalidaException`. |
| TUR-05 | `COMPLETADO` to `PROGRAMADO` | `TransicionTurnoInvalidaException`. |
| TUR-06 | Cancel without reason | `ValidacionException`. |

### Client ABM tests

| ID | Scenario | Expected result |
|---|---|---|
| CLI-01 | Create valid client | Row inserted and generated ID returned. |
| CLI-02 | Create duplicate DNI | Operation rejected. |
| CLI-03 | Search by surname | Matching clients returned. |
| CLI-04 | Update telephone | Updated value persists. |
| CLI-05 | Deactivate client | `activo = 0`; record remains in database. |
| CLI-06 | Physically delete client with cats | Rejected by foreign key or service policy. |

### Cat ABM tests

| ID | Scenario | Expected result |
|---|---|---|
| GAT-01 | Create cat for active client | Row inserted. |
| GAT-02 | Create cat for missing client | `EntidadNoEncontradaException`. |
| GAT-03 | Create cat with future birth date | `ValidacionException`. |
| GAT-04 | Create duplicate microchip | Operation rejected. |
| GAT-05 | Update current weight | New value persists. |
| GAT-06 | Deactivate cat | Cat excluded from default booking list. |

### Appointment tests

| ID | Scenario | Expected result |
|---|---|---|
| AGE-01 | Book valid future appointment | `PROGRAMADO` appointment inserted. |
| AGE-02 | Book in the past | `ValidacionException`. |
| AGE-03 | Book inactive cat | Operation rejected. |
| AGE-04 | Double-book veterinarian at same time | `TurnoNoDisponibleException`. |
| AGE-05 | Confirm programmed appointment | State becomes `CONFIRMADO`. |
| AGE-06 | Cancel confirmed appointment | State becomes `CANCELADO`; reason and closure date stored. |

### Appointment completion transaction tests

| ID | Scenario | Expected result |
|---|---|---|
| CIE-01 | Complete appointment without treatments | `Atencion` inserted and appointment becomes `COMPLETADO`. |
| CIE-02 | Complete with two treatments | One attention and two detail rows inserted. |
| CIE-03 | Fail during second treatment insert | Entire transaction rolled back. |
| CIE-04 | Complete canceled appointment | `TransicionTurnoInvalidaException`. |
| CIE-05 | Complete appointment twice | Second operation rejected. |
| CIE-06 | Complete with recorded weight | Historical weight saved; current cat weight optionally updated. |

### Report tests

| ID | Scenario | Expected result |
|---|---|---|
| REP-01 | Patients across three months | Three grouped rows with correct counts. |
| REP-02 | Appointments by state | Totals match source rows. |
| REP-03 | Frequent treatments | Ordered descending by application count. |
| REP-04 | Veterinarian productivity | Completed and canceled counts are correct. |
| REP-05 | Clinical history | Only selected cat's records are returned in descending date order. |
| REP-06 | Empty date range | Empty list, not an exception. |

## 4. Exception-handling tests

Verify that:

- SQL exceptions become `PersistenciaException`.
- The original `SQLException` remains available through `getCause()`.
- Controllers show a user-friendly Spanish message.
- Invalid input does not produce partial inserts.
- The application remains open after recoverable errors.
- Fatal database-initialisation errors stop startup with a clear dialog.

## 5. Generic-method tests

For `ColeccionesUtil.filtrar`:

- empty input produces an empty list
- all-match predicate returns all elements
- no-match predicate returns an empty list
- original list is not modified
- null policy is documented and tested

For `ColeccionesUtil.indexarPor`:

- elements are indexed by the extracted key
- duplicate-key policy is explicit
- strategy instances can be retrieved by `TipoReporte`

## 6. Database-integrity tests

Execute tests for:

- foreign keys enabled
- duplicate DNI rejected
- duplicate veterinarian licence rejected
- duplicate microchip rejected when not null
- invalid appointment state rejected
- invalid cat sex rejected
- negative weight rejected
- attention cannot reference a missing appointment
- appointment cannot reference a missing cat or veterinarian
- one appointment cannot have two attention rows

## 7. Manual Swing acceptance checklist

### Main window

- [ ] Opens centred on screen.
- [ ] Displays application name.
- [ ] Provides access to all modules.
- [ ] Closing the window asks for confirmation.
- [ ] UI remains responsive during reports.

### Clients

- [ ] New client form validates required fields.
- [ ] Search works by DNI, name, and surname.
- [ ] Edit reloads and displays updated values.
- [ ] Delete/deactivate requests confirmation.

### Cats

- [ ] Client selector shows active clients.
- [ ] Cat list can filter by client.
- [ ] Duplicate microchip error is understandable.
- [ ] Clinical-history action opens the correct patient.

### Appointments

- [ ] Agenda filters by day.
- [ ] State is visible in Spanish.
- [ ] Invalid actions are disabled or rejected.
- [ ] Cancellation requires a reason.
- [ ] Completion form records diagnosis and treatment details.

### Reports

- [ ] Date-range validation works.
- [ ] Strategy selector changes report columns.
- [ ] Empty results show an informational message.
- [ ] Totals match manually checked database rows.

## 8. Rubric evidence package

Prepare the following evidence for presentation:

1. UML diagram showing inheritance, interfaces, DAO, DTO, Strategy, and Singleton.
2. Database diagram or table list showing foreign keys.
3. Screenshot of client ABM.
4. Screenshot of cat ABM and owner relationship.
5. Screenshot of programmed appointment.
6. Screenshot of completed appointment with clinical data.
7. Screenshot of canceled appointment with reason.
8. Screenshot of a statistical report.
9. Unit test result for invalid state transition.
10. Integration test result proving transaction rollback.
11. Source-code excerpt of `DaoCrud<T, ID>`.
12. Source-code excerpt of the custom generic method.
13. Source-code excerpt of the Singleton connection manager.
14. Source-code excerpt of a prepared SQL statement and try-catch handling.

## 9. Suggested demonstration sequence

A concise exam demonstration can follow this order:

1. Start the application and show the main `JFrame` menu.
2. Create a client.
3. Create two cats for that client.
4. Edit one cat.
5. Show client and cat search.
6. Book an appointment.
7. Attempt a conflicting appointment and show the custom exception message.
8. Confirm and complete the original appointment with a treatment.
9. Book and cancel another appointment.
10. Open the cat's clinical history.
11. Generate treatment-frequency and monthly-appointment reports.
12. Briefly show the UML, Singleton, DAO, DTO, Strategy, abstraction, inheritance, and generic code.

This sequence directly demonstrates nearly every grading criterion with minimal navigation.
