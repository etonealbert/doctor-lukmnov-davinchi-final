package ar.edu.doctorlukmanov.integracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.doctorlukmanov.dao.sqlite.ClienteDaoSqlite;
import ar.edu.doctorlukmanov.dao.sqlite.GatoDaoSqlite;
import ar.edu.doctorlukmanov.dao.sqlite.TratamientoDaoSqlite;
import ar.edu.doctorlukmanov.dao.sqlite.VeterinarioDaoSqlite;
import ar.edu.doctorlukmanov.dto.ClienteFormularioDto;
import ar.edu.doctorlukmanov.dto.GatoFormularioDto;
import ar.edu.doctorlukmanov.dto.TratamientoFormularioDto;
import ar.edu.doctorlukmanov.dto.VeterinarioFormularioDto;
import ar.edu.doctorlukmanov.excepcion.EntidadNoEncontradaException;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.modelo.Cliente;
import ar.edu.doctorlukmanov.modelo.Gato;
import ar.edu.doctorlukmanov.modelo.SexoGato;
import ar.edu.doctorlukmanov.modelo.Tratamiento;
import ar.edu.doctorlukmanov.modelo.Veterinario;
import ar.edu.doctorlukmanov.servicio.ServicioCliente;
import ar.edu.doctorlukmanov.servicio.ServicioGato;
import ar.edu.doctorlukmanov.servicio.ServicioTratamiento;
import ar.edu.doctorlukmanov.servicio.ServicioVeterinario;
import ar.edu.doctorlukmanov.util.ConexionBaseDatos;
import ar.edu.doctorlukmanov.util.InicializadorBaseDatos;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CrudServiciosIntegracionTest {

    @TempDir
    Path directorioTemporal;

    private ServicioCliente servicioCliente;
    private ServicioGato servicioGato;
    private ServicioVeterinario servicioVeterinario;
    private ServicioTratamiento servicioTratamiento;

    @BeforeEach
    void prepararBaseDeDatos() {
        ConexionBaseDatos baseDatos = ConexionBaseDatos.desdeUrl(
                "jdbc:sqlite:" + directorioTemporal.resolve("crud.db"));
        new InicializadorBaseDatos(baseDatos).inicializar();

        ClienteDaoSqlite clienteDao = new ClienteDaoSqlite(baseDatos);
        GatoDaoSqlite gatoDao = new GatoDaoSqlite(baseDatos);
        VeterinarioDaoSqlite veterinarioDao = new VeterinarioDaoSqlite(baseDatos);
        TratamientoDaoSqlite tratamientoDao = new TratamientoDaoSqlite(baseDatos);
        servicioCliente = new ServicioCliente(clienteDao);
        servicioGato = new ServicioGato(gatoDao, clienteDao);
        servicioVeterinario = new ServicioVeterinario(veterinarioDao);
        servicioTratamiento = new ServicioTratamiento(tratamientoDao);
    }

    @Test
    void realizaAbmCompletoDeClienteConBusquedaYBajaLogica() {
        Cliente creado = servicioCliente.crear(clienteDto(null, "20-123"));

        assertNotNull(creado.getIdCliente());
        assertEquals("Ana", creado.getNombre());
        assertEquals(1, servicioCliente.buscar("pérez").size());
        assertThrows(ValidacionException.class,
                () -> servicioCliente.crear(clienteDto(null, " 20-123 ")));

        ClienteFormularioDto actualizado = new ClienteFormularioDto(
                creado.getIdCliente(), "Ana", "Pérez", "20-123", "999",
                "ana.nueva@example.com", "Calle 2");
        assertTrue(servicioCliente.actualizar(actualizado));
        assertEquals("999", servicioCliente.buscarPorId(creado.getIdCliente()).getTelefono());

        assertTrue(servicioCliente.eliminar(creado.getIdCliente()));
        assertFalse(servicioCliente.buscarPorId(creado.getIdCliente()).isActivo());
        assertEquals(0, servicioCliente.listarActivos().size());
    }

    @Test
    void realizaAbmDeGatoYExigeClienteActivoYMicrochipUnico() {
        Cliente cliente = servicioCliente.crear(clienteDto(null, "30-456"));
        Gato gato = servicioGato.crear(gatoDto(null, cliente.getIdCliente(), "MICRO-1"));

        assertNotNull(gato.getIdGato());
        assertEquals(1, servicioGato.listarPorCliente(cliente.getIdCliente()).size());
        assertThrows(ValidacionException.class,
                () -> servicioGato.crear(gatoDto(null, cliente.getIdCliente(), "micro-1")));
        assertThrows(EntidadNoEncontradaException.class,
                () -> servicioGato.crear(gatoDto(null, 9999L, "MICRO-2")));

        GatoFormularioDto actualizado = new GatoFormularioDto(
                gato.getIdGato(), cliente.getIdCliente(), "Milo", LocalDate.now().minusYears(3),
                SexoGato.MACHO, "Mestizo", "Negro", new BigDecimal("5.1"), "MICRO-1",
                true, "Ninguna", "Paciente tranquilo");
        assertTrue(servicioGato.actualizar(actualizado));
        assertEquals(new BigDecimal("5.1"), servicioGato.buscarPorId(gato.getIdGato()).getPesoActual());

        assertTrue(servicioGato.eliminar(gato.getIdGato()));
        assertFalse(servicioGato.buscarPorId(gato.getIdGato()).isActivo());
    }

    @Test
    void mantieneVeterinariosYTratamientosConCatalogosActivos() {
        Veterinario veterinario = servicioVeterinario.crear(new VeterinarioFormularioDto(
                null, "Laura", "Gómez", "MAT-10", "444", "laura@example.com", "Felinos"));
        Tratamiento tratamiento = servicioTratamiento.crear(new TratamientoFormularioDto(
                null, "Ecografía", "Diagnóstico por imágenes", new BigDecimal("1500")));

        assertNotNull(veterinario.getIdVeterinario());
        assertNotNull(tratamiento.getIdTratamiento());
        assertTrue(servicioVeterinario.listarActivos().stream()
                .anyMatch(item -> item.getIdVeterinario().equals(veterinario.getIdVeterinario())));
        assertTrue(servicioTratamiento.listarActivos().stream()
                .anyMatch(item -> item.getNombre().equals("Ecografía")));
        assertThrows(ValidacionException.class,
                () -> servicioVeterinario.crear(new VeterinarioFormularioDto(
                        null, "Otro", "Profesional", "mat-10", null, null, "Felinos")));
        assertThrows(ValidacionException.class,
                () -> servicioTratamiento.crear(new TratamientoFormularioDto(
                        null, " ecografía ", null, BigDecimal.ZERO)));
        assertThrows(ValidacionException.class,
                () -> servicioTratamiento.crear(new TratamientoFormularioDto(
                        null, "ECOGRAFÍA", null, BigDecimal.ZERO)));

        assertTrue(servicioVeterinario.eliminar(veterinario.getIdVeterinario()));
        assertTrue(servicioTratamiento.eliminar(tratamiento.getIdTratamiento()));
        assertFalse(servicioVeterinario.buscarPorId(veterinario.getIdVeterinario()).isActivo());
        assertFalse(servicioTratamiento.buscarPorId(tratamiento.getIdTratamiento()).isActivo());
    }

    private ClienteFormularioDto clienteDto(Long id, String dni) {
        return new ClienteFormularioDto(
                id, " Ana ", " Pérez ", dni, "555", "ANA@EXAMPLE.COM", " Calle 1 ");
    }

    private GatoFormularioDto gatoDto(Long id, Long idCliente, String microchip) {
        return new GatoFormularioDto(
                id, idCliente, " Milo ", LocalDate.now().minusYears(2), SexoGato.MACHO,
                " Mestizo ", " Negro ", new BigDecimal("4.5"), microchip,
                true, null, null);
    }
}
