package ar.edu.doctorlukmanov.vista;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ar.edu.doctorlukmanov.controlador.ControladorCliente;
import ar.edu.doctorlukmanov.controlador.ControladorGato;
import ar.edu.doctorlukmanov.controlador.ControladorReporte;
import ar.edu.doctorlukmanov.controlador.ControladorTratamiento;
import ar.edu.doctorlukmanov.controlador.ControladorTurno;
import ar.edu.doctorlukmanov.controlador.ControladorVeterinario;
import ar.edu.doctorlukmanov.dao.ReporteDao;
import ar.edu.doctorlukmanov.dao.sqlite.AtencionDaoSqlite;
import ar.edu.doctorlukmanov.dao.sqlite.ClienteDaoSqlite;
import ar.edu.doctorlukmanov.dao.sqlite.GatoDaoSqlite;
import ar.edu.doctorlukmanov.dao.sqlite.ReporteDaoSqlite;
import ar.edu.doctorlukmanov.dao.sqlite.TratamientoDaoSqlite;
import ar.edu.doctorlukmanov.dao.sqlite.TurnoDaoSqlite;
import ar.edu.doctorlukmanov.dao.sqlite.VeterinarioDaoSqlite;
import ar.edu.doctorlukmanov.estrategia.EstrategiaHistoriaClinica;
import ar.edu.doctorlukmanov.estrategia.EstrategiaPacientesPorMes;
import ar.edu.doctorlukmanov.estrategia.EstrategiaProductividadVeterinario;
import ar.edu.doctorlukmanov.estrategia.EstrategiaTratamientosFrecuentes;
import ar.edu.doctorlukmanov.estrategia.EstrategiaTurnosMensuales;
import ar.edu.doctorlukmanov.servicio.ServicioCliente;
import ar.edu.doctorlukmanov.servicio.ServicioGato;
import ar.edu.doctorlukmanov.servicio.ServicioReporte;
import ar.edu.doctorlukmanov.servicio.ServicioTratamiento;
import ar.edu.doctorlukmanov.servicio.ServicioTurno;
import ar.edu.doctorlukmanov.servicio.ServicioVeterinario;
import ar.edu.doctorlukmanov.util.ConexionBaseDatos;
import ar.edu.doctorlukmanov.util.InicializadorBaseDatos;
import ar.edu.doctorlukmanov.vista.cliente.PanelClientes;
import ar.edu.doctorlukmanov.vista.componentes.ModeloTablaNoEditable;
import ar.edu.doctorlukmanov.vista.gato.PanelGatos;
import ar.edu.doctorlukmanov.vista.reporte.PanelReportes;
import ar.edu.doctorlukmanov.vista.tratamiento.PanelTratamientos;
import ar.edu.doctorlukmanov.vista.turno.PanelTurnos;
import ar.edu.doctorlukmanov.vista.veterinario.PanelVeterinarios;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PanelesSwingTest {

    @TempDir
    Path directorioTemporal;

    private ControladorCliente clientes;
    private ControladorGato gatos;
    private ControladorVeterinario veterinarios;
    private ControladorTratamiento tratamientos;
    private ControladorTurno turnos;
    private ControladorReporte reportes;

    @BeforeEach
    void prepararControladores() {
        System.setProperty("java.awt.headless", "true");
        ConexionBaseDatos baseDatos = ConexionBaseDatos.desdeUrl(
                "jdbc:sqlite:" + directorioTemporal.resolve("ui.db"));
        new InicializadorBaseDatos(baseDatos).inicializar();
        ClienteDaoSqlite clienteDao = new ClienteDaoSqlite(baseDatos);
        GatoDaoSqlite gatoDao = new GatoDaoSqlite(baseDatos);
        VeterinarioDaoSqlite veterinarioDao = new VeterinarioDaoSqlite(baseDatos);
        TratamientoDaoSqlite tratamientoDao = new TratamientoDaoSqlite(baseDatos);
        TurnoDaoSqlite turnoDao = new TurnoDaoSqlite(baseDatos);
        AtencionDaoSqlite atencionDao = new AtencionDaoSqlite(baseDatos);
        ReporteDao reporteDao = new ReporteDaoSqlite(baseDatos);

        clientes = new ControladorCliente(new ServicioCliente(clienteDao));
        gatos = new ControladorGato(new ServicioGato(gatoDao, clienteDao));
        veterinarios = new ControladorVeterinario(new ServicioVeterinario(veterinarioDao));
        tratamientos = new ControladorTratamiento(new ServicioTratamiento(tratamientoDao));
        turnos = new ControladorTurno(new ServicioTurno(
                turnoDao, gatoDao, veterinarioDao, atencionDao, tratamientoDao, baseDatos));
        reportes = new ControladorReporte(new ServicioReporte(List.of(
                new EstrategiaPacientesPorMes(reporteDao),
                new EstrategiaTurnosMensuales(reporteDao),
                new EstrategiaTratamientosFrecuentes(reporteDao),
                new EstrategiaProductividadVeterinario(reporteDao),
                new EstrategiaHistoriaClinica(reporteDao))));
    }

    @Test
    void construyeTodosLosPanelesEnElEventDispatchThread() throws Exception {
        AtomicReference<List<JPanel>> paneles = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> paneles.set(List.of(
                new PanelClientes(clientes),
                new PanelGatos(gatos, clientes, reportes),
                new PanelVeterinarios(veterinarios),
                new PanelTratamientos(tratamientos),
                new PanelTurnos(turnos, clientes, gatos, veterinarios, tratamientos),
                new PanelReportes(reportes, veterinarios, gatos))));

        assertNotNull(paneles.get());
        paneles.get().forEach(panel -> assertNotNull(panel.getLayout()));
    }

    @Test
    void elModeloDeTablaNoPermiteEdicionDirecta() {
        ModeloTablaNoEditable modelo = new ModeloTablaNoEditable(
                new Object[][]{{"Milo"}}, new Object[]{"Nombre"});

        assertFalse(modelo.isCellEditable(0, 0));
    }
}
