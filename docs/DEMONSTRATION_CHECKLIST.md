# Doctor Lukmanov Demonstration Checklist

## Before the Demonstration

- [ ] Run `mvn clean test` and retain the result.
- [ ] Run `mvn clean package` and start the executable JAR.
- [ ] Confirm that the status bar reports a connected database.
- [ ] Keep `DoctorLukmanov_ProjectPlan/02_UML_and_Design_Decisions.md` available for the architecture explanation.

## Functional Sequence

1. Open the main `JFrame` and show every menu module.
2. Register a client and demonstrate search, edit, deactivation, and reactivation.
3. Register two cats for that client and edit one cat's weight.
4. Create or verify an active veterinarian and treatment.
5. Program a future appointment.
6. Attempt an overlapping appointment for the same veterinarian and show the rejection message.
7. Confirm and complete the original appointment with diagnosis and a treatment.
8. Open the cat's clinical history and verify the new attention.
9. Program another appointment and cancel it with a required reason.
10. Generate patients-per-month, appointments-per-month, treatment-frequency, and veterinarian-productivity reports.
11. Create a database backup from the **Archivo** menu.

## Rubric Evidence

- [ ] `Persona` inheritance and polymorphic `getTipoPersona()` behavior.
- [ ] `DaoCrud<T, ID>` abstraction and concrete SQLite DAOs.
- [ ] Immutable form/report DTO records.
- [ ] `ConexionBaseDatos` Singleton and `PRAGMA foreign_keys = ON`.
- [ ] `EstrategiaReporte<R>` and runtime Strategy selection.
- [ ] `ColeccionesUtil.filtrar` and `ColeccionesUtil.indexarPor` generic methods.
- [ ] Prepared statements and try-with-resources in DAO classes.
- [ ] `TransicionTurnoInvalidaException` and `TurnoNoDisponibleException` behavior.
- [ ] Transactional completion in `ServicioTurno.completar`.
- [ ] Rollback proof in `ServicioTurnoIntegracionTest`.
- [ ] Full client and cat ABM screens.
- [ ] Statistical reports backed by real aggregate SQL.

## Data Policy to Explain

- Clients, cats, veterinarians, and treatments are normally deactivated rather than physically deleted.
- Historical appointments and clinical records are protected by foreign keys.
- Report date ranges are inclusive at the start and inclusive through the selected end date.
- Months without source records are omitted from monthly reports.
