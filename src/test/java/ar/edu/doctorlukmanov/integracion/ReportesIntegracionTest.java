package ar.edu.doctorlukmanov.integracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.doctorlukmanov.dao.ReporteDao;
import ar.edu.doctorlukmanov.dao.sqlite.ReporteDaoSqlite;
import ar.edu.doctorlukmanov.dto.FiltroReporteDto;
import ar.edu.doctorlukmanov.dto.HistoriaClinicaDto;
import ar.edu.doctorlukmanov.dto.ReportePacientesMesDto;
import ar.edu.doctorlukmanov.dto.ReporteProductividadVeterinarioDto;
import ar.edu.doctorlukmanov.dto.ReporteTratamientoFrecuenteDto;
import ar.edu.doctorlukmanov.dto.ReporteTurnosMensualesDto;
import ar.edu.doctorlukmanov.estrategia.EstrategiaHistoriaClinica;
import ar.edu.doctorlukmanov.estrategia.EstrategiaPacientesPorMes;
import ar.edu.doctorlukmanov.estrategia.EstrategiaProductividadVeterinario;
import ar.edu.doctorlukmanov.estrategia.EstrategiaTratamientosFrecuentes;
import ar.edu.doctorlukmanov.estrategia.EstrategiaTurnosMensuales;
import ar.edu.doctorlukmanov.estrategia.TipoReporte;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.servicio.ServicioReporte;
import ar.edu.doctorlukmanov.util.ConexionBaseDatos;
import ar.edu.doctorlukmanov.util.InicializadorBaseDatos;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReportesIntegracionTest {

    @TempDir
    Path directorioTemporal;

    private ServicioReporte servicioReporte;

    @BeforeEach
    void prepararReportes() throws Exception {
        ConexionBaseDatos baseDatos = ConexionBaseDatos.desdeUrl(
                "jdbc:sqlite:" + directorioTemporal.resolve("reportes.db"));
        new InicializadorBaseDatos(baseDatos).inicializar();
        insertarDatos(baseDatos);
        ReporteDao reporteDao = new ReporteDaoSqlite(baseDatos);
        servicioReporte = new ServicioReporte(List.of(
                new EstrategiaPacientesPorMes(reporteDao),
                new EstrategiaTurnosMensuales(reporteDao),
                new EstrategiaTratamientosFrecuentes(reporteDao),
                new EstrategiaProductividadVeterinario(reporteDao),
                new EstrategiaHistoriaClinica(reporteDao)));
    }

    @Test
    void agrupaPacientesYTurnosPorMesConPorcentajes() {
        FiltroReporteDto filtro = rangoCompleto();

        List<ReportePacientesMesDto> pacientes = servicioReporte.generar(
                TipoReporte.PACIENTES_POR_MES, filtro);
        List<ReporteTurnosMensualesDto> turnos = servicioReporte.generar(
                TipoReporte.TURNOS_POR_MES, filtro);

        assertEquals(List.of(YearMonth.of(2026, 1), YearMonth.of(2026, 2), YearMonth.of(2026, 3)),
                pacientes.stream().map(ReportePacientesMesDto::mes).toList());
        assertEquals(2, pacientes.get(0).cantidadPacientes());
        assertEquals(2, turnos.get(0).total());
        assertEquals(1, turnos.get(0).completados());
        assertEquals(1, turnos.get(0).cancelados());
        assertEquals(50.0, turnos.get(0).porcentajeFinalizacion(), 0.001);
    }

    @Test
    void ordenaTratamientosYCalculaProductividad() {
        List<ReporteTratamientoFrecuenteDto> tratamientos = servicioReporte.generar(
                TipoReporte.TRATAMIENTOS_FRECUENTES, rangoCompleto());
        List<ReporteProductividadVeterinarioDto> productividad = servicioReporte.generar(
                TipoReporte.PRODUCTIVIDAD_VETERINARIO, rangoCompleto());

        assertEquals("Consulta clínica general", tratamientos.get(0).nombreTratamiento());
        assertEquals(2, tratamientos.get(0).cantidadAplicaciones());
        assertEquals(3, tratamientos.stream().mapToLong(
                ReporteTratamientoFrecuenteDto::cantidadAplicaciones).sum());
        assertEquals("Laura Gómez", productividad.get(0).nombreVeterinario());
        assertEquals(3, productividad.get(0).totalTurnos());
        assertEquals(2, productividad.get(0).turnosCompletados());
        assertEquals(1, productividad.get(0).turnosCancelados());
    }

    @Test
    void devuelveSoloLaHistoriaDelGatoSeleccionadoEnOrdenDescendente() {
        FiltroReporteDto filtro = new FiltroReporteDto(null, null, null, 1L);

        List<HistoriaClinicaDto> historia = servicioReporte.generar(TipoReporte.HISTORIA_CLINICA, filtro);

        assertEquals(2, historia.size());
        assertTrue(historia.get(0).fechaHora().isAfter(historia.get(1).fechaHora()));
        assertEquals("Control dental", historia.get(0).diagnostico());
        assertTrue(historia.get(0).tratamientos().contains("Vacunación"));
        assertEquals("Milo", historia.get(0).nombreGato());
    }

    @Test
    void validaRangosYDevuelveListaVaciaSinDatos() {
        assertThrows(ValidacionException.class, () -> servicioReporte.generar(
                TipoReporte.PACIENTES_POR_MES,
                new FiltroReporteDto(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 1, 1), null, null)));

        List<ReportePacientesMesDto> vacio = servicioReporte.generar(
                TipoReporte.PACIENTES_POR_MES,
                new FiltroReporteDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), null, null));
        assertTrue(vacio.isEmpty());
    }

    private FiltroReporteDto rangoCompleto() {
        return new FiltroReporteDto(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), null, null);
    }

    private void insertarDatos(ConexionBaseDatos baseDatos) throws Exception {
        try (Connection conexion = baseDatos.obtenerConexion();
             Statement sentencia = conexion.createStatement()) {
            sentencia.executeUpdate("INSERT INTO clientes "
                    + "(id_cliente, nombre, apellido, dni, telefono) "
                    + "VALUES (1, 'Ana', 'Pérez', '1', '555')");
            sentencia.executeUpdate("INSERT INTO gatos "
                    + "(id_gato, id_cliente, nombre, sexo, activo, fecha_registro) VALUES "
                    + "(1, 1, 'Milo', 'MACHO', 1, '2026-01-05T10:00:00'), "
                    + "(2, 1, 'Luna', 'HEMBRA', 1, '2026-01-20T10:00:00'), "
                    + "(3, 1, 'Nina', 'HEMBRA', 1, '2026-02-02T10:00:00'), "
                    + "(4, 1, 'Simón', 'MACHO', 1, '2026-03-02T10:00:00')");
            sentencia.executeUpdate("INSERT INTO veterinarios "
                    + "(id_veterinario, nombre, apellido, matricula, especialidad) VALUES "
                    + "(1, 'Laura', 'Gómez', 'MAT-1', 'Felinos'), "
                    + "(2, 'Carlos', 'Ruiz', 'MAT-2', 'Felinos')");
            sentencia.executeUpdate("INSERT INTO turnos "
                    + "(id_turno, id_gato, id_veterinario, fecha_hora, motivo, estado, fecha_cierre, "
                    + "motivo_cancelacion) VALUES "
                    + "(1, 1, 1, '2026-01-10T10:00:00', 'Control', 'COMPLETADO', "
                    + "'2026-01-10T10:30:00', NULL), "
                    + "(2, 2, 1, '2026-01-11T10:00:00', 'Vacuna', 'CANCELADO', "
                    + "'2026-01-11T09:00:00', 'No asiste'), "
                    + "(3, 1, 2, '2026-02-10T10:00:00', 'Odontología', 'COMPLETADO', "
                    + "'2026-02-10T10:30:00', NULL), "
                    + "(4, 3, 1, '2026-03-10T10:00:00', 'Control', 'COMPLETADO', "
                    + "'2026-03-10T10:30:00', NULL)");
            sentencia.executeUpdate("INSERT INTO atenciones "
                    + "(id_atencion, id_turno, diagnostico, peso_registrado, temperatura, indicaciones) VALUES "
                    + "(1, 1, 'Paciente sano', 4.5, 38.2, 'Control anual'), "
                    + "(2, 3, 'Control dental', 4.7, 38.4, 'Higiene dental'), "
                    + "(3, 4, 'Paciente sano', 3.9, 38.1, NULL)");
            sentencia.executeUpdate("INSERT INTO atencion_tratamientos "
                    + "(id_atencion, id_tratamiento, cantidad, precio_aplicado) VALUES "
                    + "(1, 1, 1, 0), (2, 1, 1, 0), (2, 2, 1, 0)");
        }
    }
}
