PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;

CREATE TABLE IF NOT EXISTS version_esquema (
    id_version       INTEGER PRIMARY KEY,
    version          INTEGER NOT NULL UNIQUE,
    fecha_aplicacion TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    descripcion      TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS clientes (
    id_cliente          INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre              TEXT NOT NULL COLLATE NOCASE,
    apellido            TEXT NOT NULL COLLATE NOCASE,
    dni                 TEXT NOT NULL UNIQUE,
    telefono            TEXT NOT NULL,
    correo_electronico  TEXT,
    direccion           TEXT,
    activo              INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
    fecha_registro      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TEXT
);

CREATE TABLE IF NOT EXISTS veterinarios (
    id_veterinario      INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre              TEXT NOT NULL COLLATE NOCASE,
    apellido            TEXT NOT NULL COLLATE NOCASE,
    matricula           TEXT NOT NULL UNIQUE,
    telefono            TEXT,
    correo_electronico  TEXT,
    especialidad        TEXT NOT NULL DEFAULT 'Medicina felina',
    activo              INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
    fecha_registro      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TEXT
);

CREATE TABLE IF NOT EXISTS gatos (
    id_gato             INTEGER PRIMARY KEY AUTOINCREMENT,
    id_cliente          INTEGER NOT NULL,
    nombre              TEXT NOT NULL COLLATE NOCASE,
    fecha_nacimiento    TEXT,
    sexo                TEXT NOT NULL DEFAULT 'DESCONOCIDO'
                         CHECK (sexo IN ('MACHO', 'HEMBRA', 'DESCONOCIDO')),
    raza                TEXT,
    color               TEXT,
    peso_actual         NUMERIC CHECK (peso_actual IS NULL OR peso_actual >= 0),
    numero_microchip    TEXT UNIQUE,
    esterilizado        INTEGER NOT NULL DEFAULT 0 CHECK (esterilizado IN (0, 1)),
    alergias            TEXT,
    observaciones       TEXT,
    activo              INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
    fecha_registro      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TEXT,
    CONSTRAINT fk_gatos_clientes FOREIGN KEY (id_cliente)
        REFERENCES clientes (id_cliente) ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS turnos (
    id_turno            INTEGER PRIMARY KEY AUTOINCREMENT,
    id_gato             INTEGER NOT NULL,
    id_veterinario      INTEGER NOT NULL,
    fecha_hora          TEXT NOT NULL,
    duracion_minutos    INTEGER NOT NULL DEFAULT 30 CHECK (duracion_minutos > 0),
    motivo              TEXT NOT NULL,
    estado              TEXT NOT NULL DEFAULT 'PROGRAMADO'
                         CHECK (estado IN ('PROGRAMADO', 'CONFIRMADO', 'COMPLETADO', 'CANCELADO')),
    fecha_creacion      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre        TEXT,
    motivo_cancelacion  TEXT,
    observaciones       TEXT,
    CONSTRAINT fk_turnos_gatos FOREIGN KEY (id_gato)
        REFERENCES gatos (id_gato) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_turnos_veterinarios FOREIGN KEY (id_veterinario)
        REFERENCES veterinarios (id_veterinario) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_turno_cancelado_motivo CHECK (
        estado <> 'CANCELADO'
        OR (motivo_cancelacion IS NOT NULL AND length(trim(motivo_cancelacion)) > 0)
    ),
    CONSTRAINT ck_turno_cierre CHECK (
        (estado IN ('PROGRAMADO', 'CONFIRMADO') AND fecha_cierre IS NULL)
        OR (estado IN ('COMPLETADO', 'CANCELADO') AND fecha_cierre IS NOT NULL)
    )
);

CREATE TABLE IF NOT EXISTS atenciones (
    id_atencion            INTEGER PRIMARY KEY AUTOINCREMENT,
    id_turno               INTEGER NOT NULL UNIQUE,
    diagnostico            TEXT NOT NULL,
    peso_registrado        NUMERIC CHECK (peso_registrado IS NULL OR peso_registrado >= 0),
    temperatura            NUMERIC CHECK (
                               temperatura IS NULL OR (temperatura >= 30 AND temperatura <= 45)
                           ),
    observaciones_clinicas TEXT,
    indicaciones           TEXT,
    fecha_registro         TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_atenciones_turnos FOREIGN KEY (id_turno)
        REFERENCES turnos (id_turno) ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS tratamientos (
    id_tratamiento     INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre             TEXT NOT NULL UNIQUE COLLATE NOCASE,
    descripcion        TEXT,
    precio_referencia  NUMERIC NOT NULL DEFAULT 0 CHECK (precio_referencia >= 0),
    activo             INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
    fecha_registro     TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS atencion_tratamientos (
    id_atencion_tratamiento INTEGER PRIMARY KEY AUTOINCREMENT,
    id_atencion             INTEGER NOT NULL,
    id_tratamiento          INTEGER NOT NULL,
    dosis                   TEXT,
    frecuencia              TEXT,
    duracion_dias           INTEGER CHECK (duracion_dias IS NULL OR duracion_dias >= 0),
    observaciones           TEXT,
    cantidad                NUMERIC NOT NULL DEFAULT 1 CHECK (cantidad > 0),
    precio_aplicado         NUMERIC NOT NULL DEFAULT 0 CHECK (precio_aplicado >= 0),
    CONSTRAINT fk_atencion_tratamientos_atencion FOREIGN KEY (id_atencion)
        REFERENCES atenciones (id_atencion) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_atencion_tratamientos_tratamiento FOREIGN KEY (id_tratamiento)
        REFERENCES tratamientos (id_tratamiento) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT uq_atencion_tratamiento UNIQUE (id_atencion, id_tratamiento)
);

CREATE INDEX IF NOT EXISTS idx_clientes_apellido_nombre ON clientes (apellido, nombre);
CREATE INDEX IF NOT EXISTS idx_gatos_cliente ON gatos (id_cliente);
CREATE INDEX IF NOT EXISTS idx_gatos_nombre ON gatos (nombre);
CREATE INDEX IF NOT EXISTS idx_turnos_fecha_hora ON turnos (fecha_hora);
CREATE INDEX IF NOT EXISTS idx_turnos_gato ON turnos (id_gato);
CREATE INDEX IF NOT EXISTS idx_turnos_veterinario_fecha ON turnos (id_veterinario, fecha_hora);
CREATE INDEX IF NOT EXISTS idx_turnos_estado ON turnos (estado);
CREATE INDEX IF NOT EXISTS idx_atenciones_fecha ON atenciones (fecha_registro);
CREATE INDEX IF NOT EXISTS idx_atencion_tratamientos_tratamiento
    ON atencion_tratamientos (id_tratamiento);

CREATE UNIQUE INDEX IF NOT EXISTS uq_turno_abierto_veterinario_fecha
    ON turnos (id_veterinario, fecha_hora)
    WHERE estado IN ('PROGRAMADO', 'CONFIRMADO');

CREATE TRIGGER IF NOT EXISTS validar_superposicion_turno_insertar
BEFORE INSERT ON turnos
FOR EACH ROW
WHEN NEW.estado IN ('PROGRAMADO', 'CONFIRMADO')
 AND EXISTS (
    SELECT 1
    FROM turnos existente
    WHERE existente.id_veterinario = NEW.id_veterinario
      AND existente.estado IN ('PROGRAMADO', 'CONFIRMADO')
      AND datetime(existente.fecha_hora) < datetime(
          NEW.fecha_hora, '+' || NEW.duracion_minutos || ' minutes')
      AND datetime(
          existente.fecha_hora, '+' || existente.duracion_minutos || ' minutes')
          > datetime(NEW.fecha_hora)
 )
BEGIN
    SELECT RAISE(ABORT, 'turno_superpuesto');
END;

CREATE TRIGGER IF NOT EXISTS validar_superposicion_turno_actualizar
BEFORE UPDATE OF id_veterinario, fecha_hora, duracion_minutos, estado ON turnos
FOR EACH ROW
WHEN NEW.estado IN ('PROGRAMADO', 'CONFIRMADO')
 AND EXISTS (
    SELECT 1
    FROM turnos existente
    WHERE existente.id_turno <> NEW.id_turno
      AND existente.id_veterinario = NEW.id_veterinario
      AND existente.estado IN ('PROGRAMADO', 'CONFIRMADO')
      AND datetime(existente.fecha_hora) < datetime(
          NEW.fecha_hora, '+' || NEW.duracion_minutos || ' minutes')
      AND datetime(
          existente.fecha_hora, '+' || existente.duracion_minutos || ' minutes')
          > datetime(NEW.fecha_hora)
 )
BEGIN
    SELECT RAISE(ABORT, 'turno_superpuesto');
END;

CREATE VIEW IF NOT EXISTS vista_historia_clinica AS
SELECT
    g.id_gato,
    g.nombre AS nombre_gato,
    c.id_cliente,
    c.nombre || ' ' || c.apellido AS nombre_cliente,
    t.id_turno,
    t.fecha_hora,
    t.motivo,
    t.estado,
    v.nombre || ' ' || v.apellido AS nombre_veterinario,
    a.id_atencion,
    a.diagnostico,
    a.peso_registrado,
    a.temperatura,
    a.observaciones_clinicas,
    a.indicaciones,
    a.fecha_registro AS fecha_atencion
FROM gatos g
JOIN clientes c ON c.id_cliente = g.id_cliente
JOIN turnos t ON t.id_gato = g.id_gato
JOIN veterinarios v ON v.id_veterinario = t.id_veterinario
LEFT JOIN atenciones a ON a.id_turno = t.id_turno;

CREATE VIEW IF NOT EXISTS vista_turnos_detallados AS
SELECT
    t.id_turno,
    t.fecha_hora,
    t.duracion_minutos,
    t.motivo,
    t.estado,
    t.fecha_creacion,
    t.fecha_cierre,
    t.motivo_cancelacion,
    g.id_gato,
    g.nombre AS nombre_gato,
    c.id_cliente,
    c.nombre || ' ' || c.apellido AS nombre_cliente,
    v.id_veterinario,
    v.nombre || ' ' || v.apellido AS nombre_veterinario
FROM turnos t
JOIN gatos g ON g.id_gato = t.id_gato
JOIN clientes c ON c.id_cliente = g.id_cliente
JOIN veterinarios v ON v.id_veterinario = t.id_veterinario;

CREATE VIEW IF NOT EXISTS vista_tratamientos_frecuentes AS
SELECT
    tr.id_tratamiento,
    tr.nombre AS nombre_tratamiento,
    COUNT(at.id_atencion_tratamiento) AS cantidad_aplicaciones,
    COALESCE(SUM(at.cantidad), 0) AS cantidad_total,
    COALESCE(SUM(at.precio_aplicado * at.cantidad), 0) AS importe_total
FROM tratamientos tr
LEFT JOIN atencion_tratamientos at ON at.id_tratamiento = tr.id_tratamiento
GROUP BY tr.id_tratamiento, tr.nombre;

INSERT OR IGNORE INTO version_esquema (id_version, version, descripcion)
VALUES (1, 1, 'Esquema inicial de la Clínica Veterinaria Doctor Lukmanov');

INSERT OR IGNORE INTO version_esquema (id_version, version, descripcion)
VALUES (2, 2, 'Validación transaccional de superposición de turnos');

INSERT OR IGNORE INTO tratamientos (nombre, descripcion, precio_referencia, activo)
VALUES
    ('Consulta clínica general', 'Evaluación clínica general para pacientes felinos.', 0, 1),
    ('Vacunación', 'Aplicación y registro de vacuna indicada para el paciente.', 0, 1),
    ('Desparasitación interna', 'Administración de tratamiento antiparasitario interno.', 0, 1),
    ('Desparasitación externa', 'Aplicación contra pulgas, garrapatas u otros ectoparásitos.', 0, 1),
    ('Curación de herida', 'Limpieza, desinfección y control de una herida.', 0, 1),
    ('Administración de medicación', 'Aplicación de medicación indicada por el veterinario.', 0, 1),
    ('Control posoperatorio', 'Revisión clínica posterior a una cirugía.', 0, 1),
    ('Consejería nutricional', 'Evaluación y recomendaciones de alimentación felina.', 0, 1),
    ('Corte de uñas', 'Procedimiento de corte y control de uñas.', 0, 1),
    ('Limpieza ótica', 'Evaluación y limpieza de oídos.', 0, 1);

COMMIT;
