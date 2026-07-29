package ar.edu.doctorlukmanov.integracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.doctorlukmanov.dao.AtencionDao;
import ar.edu.doctorlukmanov.dao.TurnoDao;
import ar.edu.doctorlukmanov.dao.sqlite.AtencionDaoSqlite;
import ar.edu.doctorlukmanov.dao.sqlite.ClienteDaoSqlite;
import ar.edu.doctorlukmanov.dao.sqlite.GatoDaoSqlite;
import ar.edu.doctorlukmanov.dao.sqlite.TratamientoDaoSqlite;
import ar.edu.doctorlukmanov.dao.sqlite.TurnoDaoSqlite;
import ar.edu.doctorlukmanov.dao.sqlite.VeterinarioDaoSqlite;
import ar.edu.doctorlukmanov.dto.CierreAtencionDto;
import ar.edu.doctorlukmanov.dto.ClienteFormularioDto;
import ar.edu.doctorlukmanov.dto.DetalleTratamientoDto;
import ar.edu.doctorlukmanov.dto.GatoFormularioDto;
import ar.edu.doctorlukmanov.dto.TurnoFormularioDto;
import ar.edu.doctorlukmanov.dto.VeterinarioFormularioDto;
import ar.edu.doctorlukmanov.excepcion.EntidadNoEncontradaException;
import ar.edu.doctorlukmanov.excepcion.TransicionTurnoInvalidaException;
import ar.edu.doctorlukmanov.excepcion.TurnoNoDisponibleException;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.modelo.Atencion;
import ar.edu.doctorlukmanov.modelo.Cliente;
import ar.edu.doctorlukmanov.modelo.EstadoTurno;
import ar.edu.doctorlukmanov.modelo.Gato;
import ar.edu.doctorlukmanov.modelo.SexoGato;
import ar.edu.doctorlukmanov.modelo.Turno;
import ar.edu.doctorlukmanov.modelo.Veterinario;
import ar.edu.doctorlukmanov.servicio.ServicioCliente;
import ar.edu.doctorlukmanov.servicio.ServicioGato;
import ar.edu.doctorlukmanov.servicio.ServicioTurno;
import ar.edu.doctorlukmanov.servicio.ServicioVeterinario;
import ar.edu.doctorlukmanov.util.ConexionBaseDatos;
import ar.edu.doctorlukmanov.util.InicializadorBaseDatos;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServicioTurnoIntegracionTest {

    @TempDir
    Path directorioTemporal;

    private ServicioTurno servicioTurno;
    private TurnoDao turnoDao;
    private AtencionDao atencionDao;
    private GatoDaoSqlite gatoDao;
    private Gato gato;
    private Veterinario veterinario;

    @BeforeEach
    void prepararEscenario() {
        ConexionBaseDatos baseDatos = ConexionBaseDatos.desdeUrl(
                "jdbc:sqlite:" + directorioTemporal.resolve("turnos.db"));
        new InicializadorBaseDatos(baseDatos).inicializar();

        ClienteDaoSqlite clienteDao = new ClienteDaoSqlite(baseDatos);
        gatoDao = new GatoDaoSqlite(baseDatos);
        VeterinarioDaoSqlite veterinarioDao = new VeterinarioDaoSqlite(baseDatos);
        TratamientoDaoSqlite tratamientoDao = new TratamientoDaoSqlite(baseDatos);
        turnoDao = new TurnoDaoSqlite(baseDatos);
        atencionDao = new AtencionDaoSqlite(baseDatos);
        servicioTurno = new ServicioTurno(
                turnoDao, gatoDao, veterinarioDao, atencionDao, tratamientoDao, baseDatos);

        ServicioCliente servicioCliente = new ServicioCliente(clienteDao);
        Cliente cliente = servicioCliente.crear(new ClienteFormularioDto(
                null, "Ana", "Pérez", "123", "555", null, null));
        gato = new ServicioGato(gatoDao, clienteDao).crear(new GatoFormularioDto(
                null, cliente.getIdCliente(), "Milo", LocalDate.now().minusYears(2),
                SexoGato.MACHO, "Mestizo", "Negro", new BigDecimal("4.5"), "CHIP-1",
                true, null, null));
        veterinario = new ServicioVeterinario(veterinarioDao).crear(new VeterinarioFormularioDto(
                null, "Laura", "Gómez", "MAT-1", "444", null, "Medicina felina"));
    }

    @Test
    void programaYRechazaTurnosSuperpuestosOPasados() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
        Turno turno = servicioTurno.programar(turnoDto(inicio));

        assertNotNull(turno.getIdTurno());
        assertEquals(EstadoTurno.PROGRAMADO, turno.getEstado());
        assertThrows(TurnoNoDisponibleException.class,
                () -> servicioTurno.programar(turnoDto(inicio.plusMinutes(15))));
        assertThrows(ValidacionException.class,
                () -> servicioTurno.programar(turnoDto(LocalDateTime.now().minusHours(1))));
        assertEquals(1, servicioTurno.listarPorFecha(inicio.toLocalDate()).size());
    }

    @Test
    void confirmaYCancelaSinCrearAtencion() {
        Turno turno = servicioTurno.programar(turnoDto(LocalDateTime.now().plusDays(1)));

        servicioTurno.confirmar(turno.getIdTurno());
        servicioTurno.cancelar(turno.getIdTurno(), "El cliente no puede asistir");

        Turno cancelado = servicioTurno.buscarPorId(turno.getIdTurno());
        assertEquals(EstadoTurno.CANCELADO, cancelado.getEstado());
        assertNotNull(cancelado.getFechaCierre());
        assertEquals("El cliente no puede asistir", cancelado.getMotivoCancelacion());
        assertTrue(atencionDao.buscarPorTurno(turno.getIdTurno()).isEmpty());
        assertThrows(TransicionTurnoInvalidaException.class,
                () -> servicioTurno.confirmar(turno.getIdTurno()));
    }

    @Test
    void completaAtencionConTratamientosYActualizaElPeso() {
        Turno turno = servicioTurno.programar(turnoDto(LocalDateTime.now().plusDays(1)));
        CierreAtencionDto cierre = new CierreAtencionDto(
                turno.getIdTurno(),
                "Gingivitis leve",
                new BigDecimal("4.8"),
                new BigDecimal("38.4"),
                "Buen estado general",
                "Control en 30 días",
                List.of(detalle(1L), detalle(2L)));

        servicioTurno.completar(cierre);

        Turno completado = servicioTurno.buscarPorId(turno.getIdTurno());
        Atencion atencion = atencionDao.buscarPorTurno(turno.getIdTurno()).orElseThrow();
        assertEquals(EstadoTurno.COMPLETADO, completado.getEstado());
        assertEquals(2, atencion.getTratamientos().size());
        assertEquals(new BigDecimal("4.8"), gatoDao.buscarPorId(gato.getIdGato()).orElseThrow().getPesoActual());
        assertThrows(TransicionTurnoInvalidaException.class, () -> servicioTurno.completar(cierre));
    }

    @Test
    void revierteTodaLaOperacionCuandoFallaUnTratamientoIntermedio() {
        Turno turno = servicioTurno.programar(turnoDto(LocalDateTime.now().plusDays(1)));
        CierreAtencionDto cierre = new CierreAtencionDto(
                turno.getIdTurno(),
                "Control general",
                new BigDecimal("5.2"),
                null,
                null,
                null,
                List.of(detalle(1L), detalle(99999L)));

        assertThrows(EntidadNoEncontradaException.class, () -> servicioTurno.completar(cierre));

        assertTrue(atencionDao.buscarPorTurno(turno.getIdTurno()).isEmpty());
        assertEquals(EstadoTurno.PROGRAMADO, servicioTurno.buscarPorId(turno.getIdTurno()).getEstado());
        assertEquals(new BigDecimal("4.5"), gatoDao.buscarPorId(gato.getIdGato()).orElseThrow().getPesoActual());
        assertFalse(turnoDao.buscarPorId(turno.getIdTurno()).orElseThrow().getEstado().esTerminal());
    }

    private TurnoFormularioDto turnoDto(LocalDateTime fechaHora) {
        return new TurnoFormularioDto(
                gato.getIdGato(), veterinario.getIdVeterinario(), fechaHora, "Control general", null);
    }

    private DetalleTratamientoDto detalle(Long idTratamiento) {
        return new DetalleTratamientoDto(
                idTratamiento, "1 dosis", "Cada 24 horas", 3, null,
                BigDecimal.ONE, BigDecimal.ZERO);
    }
}
