# DoctorLukmanov Project Plan

This folder contains the complete architectural and implementation plan for **Clínica Veterinaria "Doctor Lukmanov"**, a desktop application dedicated exclusively to feline patients.

The documentation is written in English. The proposed implementation uses Spanish names for classes, variables, database objects, business states, and visible user-interface labels.

## Document index

1. `01_Project_Overview_and_Instructions.md` — scope, functional overview, build/run instructions, and grading checklist.
2. `02_UML_and_Design_Decisions.md` — Mermaid UML, architectural rationale, OOP principles, design patterns, exceptions, collections, and generics.
3. `03_Database_Script.md` — complete SQLite schema, constraints, indexes, views, and seed catalog data.
4. `04_Implementation_Plan_and_Architecture.md` — package structure and ordered implementation guide.
5. `05_Testing_and_Validation_Plan.md` — unit, integration, GUI, database, and rubric-evidence testing plan.
6. `06_Report_and_UI_Specifications.md` — detailed report definitions, screen specifications, menu structure, and workflow acceptance criteria.

## Proposed application name

- Product name: **Clínica Veterinaria Doctor Lukmanov**
- Main Java package: `ar.edu.doctorlukmanov`
- Main class: `AplicacionClinica`
- Main window: `VentanaPrincipal`
- SQLite file: `doctor_lukmanov.db`

## Core domain

The system is centred on the relationship between:

- `Cliente`: the cat's owner or responsible person.
- `Gato`: the feline patient.
- `Turno`: an appointment booked for a cat and a veterinarian.
- `Atencion`: the clinical record created when a scheduled appointment is completed.
- `Tratamiento`: a catalogued procedure, medication, or clinical action assigned during an appointment.

The principal business cycle is:

`Cliente` registration → `Gato` registration → `Turno` booking → appointment confirmation → completion or cancellation → optional `Atencion` and associated treatments → statistics and reports.
