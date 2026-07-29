# 06 — Report and UI Specifications

## 1. UI language and terminology

All visible labels, actions, validation messages, table headings, and enum display values must be Spanish.

Examples:

- `PROGRAMADO` → **"Programado"**
- `CONFIRMADO` → **"Confirmado"**
- `COMPLETADO` → **"Completado"**
- `CANCELADO` → **"Cancelado"**

The code may preserve enum constants in uppercase Spanish words while using a display method such as `getDescripcion()` for title case.

## 2. Main window specification

### Class

`VentanaPrincipal extends JFrame`

### Suggested title

**"Clínica Veterinaria Doctor Lukmanov — Gestión Felina"**

### Minimum dimensions

- width: 1200
- height: 750
- minimum: 1000 × 650

### Main regions

- Header: clinic name and current date.
- Navigation: `JMenuBar` and optional shortcut buttons.
- Content: `CardLayout` containing each module panel.
- Status bar: database status, current module, and operation messages.

### Main menu

```text
Archivo
  Inicio
  Respaldar base de datos
  Salir

Clientes
  Gestionar clientes
  Nuevo cliente

Gatos
  Gestionar gatos
  Registrar gato
  Historia clínica

Turnos
  Agenda diaria
  Programar turno
  Confirmar turno
  Completar turno
  Cancelar turno

Veterinarios
  Gestionar veterinarios

Tratamientos
  Gestionar tratamientos

Reportes
  Pacientes por mes
  Turnos por mes
  Tratamientos frecuentes
  Productividad veterinaria

Ayuda
  Acerca de
```

## 3. Client screen

### Panel

`PanelClientes`

### Filters

- **"Buscar"** text field
- **"Estado"** combo: Activos, Inactivos, Todos

### Table columns

- ID
- Nombre
- Apellido
- DNI
- Teléfono
- Correo electrónico
- Estado

### Actions

- **"Nuevo cliente"**
- **"Editar"**
- **"Ver gatos"**
- **"Activar/Desactivar"**
- **"Eliminar"**
- **"Actualizar lista"**

### Validation messages

- **"El nombre es obligatorio."**
- **"El apellido es obligatorio."**
- **"El DNI ya está registrado."**
- **"El correo electrónico no tiene un formato válido."**

## 4. Cat screen

### Panel

`PanelGatos`

### Filters

- **"Cliente"** combo or search selector
- **"Nombre del gato"**
- **"Estado"**

### Table columns

- ID
- Nombre
- Cliente
- Sexo
- Raza
- Edad aproximada
- Peso actual
- Microchip
- Estado

### Actions

- **"Registrar gato"**
- **"Editar"**
- **"Historia clínica"**
- **"Programar turno"**
- **"Activar/Desactivar"**
- **"Eliminar"**

### Form labels

- **"Cliente responsable"**
- **"Nombre"**
- **"Fecha de nacimiento"**
- **"Sexo"**
- **"Raza"**
- **"Color"**
- **"Peso actual"**
- **"Número de microchip"**
- **"Esterilizado"**
- **"Alergias"**
- **"Observaciones"**

## 5. Appointment agenda

### Panel

`PanelTurnos`

### Filters

- selected date
- veterinarian
- state
- cat or client search

### Table columns

- Hora
- Gato
- Cliente
- Veterinario
- Motivo
- Estado
- Duración

### State-sensitive actions

For `PROGRAMADO`:

- Confirmar
- Completar
- Cancelar
- Editar

For `CONFIRMADO`:

- Completar
- Cancelar

For `COMPLETADO`:

- Ver atención
- Ver historia clínica

For `CANCELADO`:

- Ver motivo de cancelación

Buttons should be disabled when the selected state does not support the action. The service must still validate the rule because UI state alone is not a security or integrity boundary.

## 6. Completion dialog

### Class

`DialogoCompletarTurno`

### Read-only summary

- Turno
- Fecha y hora
- Gato
- Cliente
- Veterinario
- Motivo

### Clinical fields

- **"Diagnóstico"** — required multiline text
- **"Peso registrado (kg)"**
- **"Temperatura (°C)"**
- **"Observaciones clínicas"**
- **"Indicaciones"**

### Treatment grid

Users can add one or more active treatments. Duplicate treatment rows should be rejected or merged according to a documented policy.

### Confirmation message

**"El turno se completó y la atención clínica fue registrada correctamente."**

## 7. Cancellation dialog

### Class

`DialogoCancelarTurno`

### Required field

- **"Motivo de cancelación"**

### Confirmation text

**"¿Confirma la cancelación del turno seleccionado? Esta acción cerrará el turno."**

### Success message

**"El turno fue cancelado correctamente."**

## 8. Report screen architecture

### Panel

`PanelReportes`

### Shared controls

- **"Tipo de reporte"**
- **"Fecha desde"**
- **"Fecha hasta"**
- optional veterinarian selector
- optional cat selector
- **"Generar"**
- **"Limpiar"**
- optional **"Exportar CSV"**

The `TipoReporte` selection determines which optional filters are enabled.

### Loading behaviour

- Disable **"Generar"** while the report runs.
- Show **"Generando reporte..."** in the status bar.
- Use `SwingWorker`.
- Restore controls in `done()`.
- Show **"No se encontraron datos para los filtros seleccionados."** when the result is empty.

## 9. Report specifications

### 9.1 Patients per month

#### Strategy

`EstrategiaPacientesPorMes`

#### Purpose

Show the number of cats registered during each month in the selected range.

#### Required filters

- date from
- date to

#### Output columns

- Mes
- Pacientes registrados

#### Optional statistic

- cumulative total

#### Acceptance criteria

- Months are ordered chronologically.
- Months with no registrations may be omitted or represented as zero; the chosen behaviour must be documented.
- Date boundaries are inclusive from and exclusive to the next-day or next-month endpoint.

### 9.2 Appointments per month

#### Strategy

`EstrategiaTurnosMensuales`

#### Purpose

Show appointment volume and outcome distribution.

#### Output columns

- Mes
- Programados
- Confirmados
- Completados
- Cancelados
- Total
- Porcentaje de finalización

#### Formula

```text
porcentaje_finalizacion = completados / total * 100
```

When total is zero, the percentage is zero.

### 9.3 Most frequent treatments

#### Strategy

`EstrategiaTratamientosFrecuentes`

#### Purpose

Identify the treatments most frequently recorded in completed appointments.

#### Output columns

- Posición
- Tratamiento
- Aplicaciones
- Cantidad total
- Porcentaje sobre aplicaciones

#### Formula

```text
porcentaje = aplicaciones_del_tratamiento / aplicaciones_totales * 100
```

#### Acceptance criteria

- Only treatments linked to actual attention records count.
- Date range is based on appointment or attention date; choose one and use it consistently. Appointment date is recommended.
- Results are ordered by applications descending, then treatment name ascending.

### 9.4 Veterinarian productivity

#### Strategy

`EstrategiaProductividadVeterinario`

#### Output columns

- Veterinario
- Turnos totales
- Completados
- Cancelados
- Tasa de finalización

#### Optional filters

- veterinarian
- date range

### 9.5 Clinical history

#### Strategy

`EstrategiaHistoriaClinica`

#### Required filter

- cat

#### Output columns

- Fecha
- Veterinario
- Motivo
- Diagnóstico
- Peso
- Temperatura
- Tratamientos
- Indicaciones

#### Acceptance criteria

- Records are ordered newest first.
- Canceled appointments do not appear as clinical attention.
- The cat and responsible client are displayed in the report header.

## 10. DTO-to-table mapping

Controllers should convert report DTOs into row arrays or dedicated table models.

Example conceptual mapping:

```text
ReporteTratamientoFrecuenteDto
  nombreTratamiento      → "Tratamiento"
  cantidadAplicaciones   → "Aplicaciones"
  cantidadTotal          → "Cantidad total"
  porcentaje             → "Porcentaje"
```

Formatting belongs in the presentation layer:

- dates: `dd/MM/yyyy`
- date-time: `dd/MM/yyyy HH:mm`
- percentages: two decimal places and `%`
- money: locale-appropriate decimal formatting if prices are displayed
- booleans: **"Sí"** / **"No"**

## 11. Error-message catalogue

| Condition | Spanish message |
|---|---|
| Missing required field | `Complete los campos obligatorios.` |
| Entity not found | `No se encontró el registro solicitado.` |
| Duplicate DNI | `Ya existe un cliente con ese DNI.` |
| Duplicate microchip | `Ya existe un gato con ese número de microchip.` |
| Schedule conflict | `El veterinario ya tiene un turno en el horario seleccionado.` |
| Invalid transition | `El turno no puede cambiar al estado solicitado.` |
| Missing cancellation reason | `Ingrese el motivo de cancelación.` |
| Database unavailable | `No fue posible acceder a la base de datos.` |
| Unexpected error | `Ocurrió un error inesperado. Consulte el registro de errores.` |

## 12. Accessibility and usability

- Provide keyboard mnemonics for main menu items.
- Define a logical tab order.
- Associate labels with fields using `setLabelFor`.
- Avoid colour as the only indication of appointment state.
- Provide readable state text in every row.
- Use confirmation dialogs for destructive operations.
- Keep the primary action consistently positioned.
- Do not close forms after validation failure.

## 13. Optional enhancements after rubric completion

These are secondary and should not delay required features:

- CSV export for reports.
- Database backup action.
- Dashboard cards for today's appointments.
- Appointment reminder flag.
- Printable clinical-history summary.
- Simple user login and role permissions.
- Configurable appointment duration.
- Audit table for critical changes.

The core rubric should be completed and tested before implementing optional features.
