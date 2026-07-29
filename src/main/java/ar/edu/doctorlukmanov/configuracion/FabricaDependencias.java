package ar.edu.doctorlukmanov.configuracion;

import ar.edu.doctorlukmanov.controlador.ControladorCliente;
import ar.edu.doctorlukmanov.controlador.ControladorGato;
import ar.edu.doctorlukmanov.controlador.ControladorReporte;
import ar.edu.doctorlukmanov.controlador.ControladorSistema;
import ar.edu.doctorlukmanov.controlador.ControladorTratamiento;
import ar.edu.doctorlukmanov.controlador.ControladorTurno;
import ar.edu.doctorlukmanov.controlador.ControladorVeterinario;
import ar.edu.doctorlukmanov.dao.AtencionDao;
import ar.edu.doctorlukmanov.dao.ClienteDao;
import ar.edu.doctorlukmanov.dao.GatoDao;
import ar.edu.doctorlukmanov.dao.ReporteDao;
import ar.edu.doctorlukmanov.dao.TratamientoDao;
import ar.edu.doctorlukmanov.dao.TurnoDao;
import ar.edu.doctorlukmanov.dao.VeterinarioDao;
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
import ar.edu.doctorlukmanov.estrategia.EstrategiaReporte;
import ar.edu.doctorlukmanov.estrategia.EstrategiaTratamientosFrecuentes;
import ar.edu.doctorlukmanov.estrategia.EstrategiaTurnosMensuales;
import ar.edu.doctorlukmanov.servicio.ServicioCliente;
import ar.edu.doctorlukmanov.servicio.ServicioGato;
import ar.edu.doctorlukmanov.servicio.ServicioReporte;
import ar.edu.doctorlukmanov.servicio.ServicioRespaldo;
import ar.edu.doctorlukmanov.servicio.ServicioTratamiento;
import ar.edu.doctorlukmanov.servicio.ServicioTurno;
import ar.edu.doctorlukmanov.servicio.ServicioVeterinario;
import ar.edu.doctorlukmanov.util.ConexionBaseDatos;
import ar.edu.doctorlukmanov.vista.VentanaPrincipal;
import ar.edu.doctorlukmanov.vista.cliente.PanelClientes;
import ar.edu.doctorlukmanov.vista.gato.PanelGatos;
import ar.edu.doctorlukmanov.vista.reporte.PanelReportes;
import ar.edu.doctorlukmanov.vista.tratamiento.PanelTratamientos;
import ar.edu.doctorlukmanov.vista.turno.PanelTurnos;
import ar.edu.doctorlukmanov.vista.veterinario.PanelVeterinarios;
import java.util.List;

public final class FabricaDependencias {

    private final ConexionBaseDatos baseDatos;

    public FabricaDependencias() {
        this(ConexionBaseDatos.getInstancia());
    }

    public FabricaDependencias(ConexionBaseDatos baseDatos) {
        this.baseDatos = baseDatos;
    }

    public VentanaPrincipal crearVentanaPrincipal() {
        ClienteDao clienteDao = new ClienteDaoSqlite(baseDatos);
        GatoDao gatoDao = new GatoDaoSqlite(baseDatos);
        VeterinarioDao veterinarioDao = new VeterinarioDaoSqlite(baseDatos);
        TratamientoDao tratamientoDao = new TratamientoDaoSqlite(baseDatos);
        TurnoDao turnoDao = new TurnoDaoSqlite(baseDatos);
        AtencionDao atencionDao = new AtencionDaoSqlite(baseDatos);
        ReporteDao reporteDao = new ReporteDaoSqlite(baseDatos);

        ControladorCliente clientes = new ControladorCliente(new ServicioCliente(clienteDao));
        ControladorGato gatos = new ControladorGato(new ServicioGato(gatoDao, clienteDao));
        ControladorVeterinario veterinarios = new ControladorVeterinario(
                new ServicioVeterinario(veterinarioDao));
        ControladorTratamiento tratamientos = new ControladorTratamiento(
                new ServicioTratamiento(tratamientoDao));
        ControladorTurno turnos = new ControladorTurno(new ServicioTurno(
                turnoDao, gatoDao, veterinarioDao, atencionDao, tratamientoDao, baseDatos));
        List<EstrategiaReporte<?>> estrategias = List.of(
                new EstrategiaPacientesPorMes(reporteDao),
                new EstrategiaTurnosMensuales(reporteDao),
                new EstrategiaTratamientosFrecuentes(reporteDao),
                new EstrategiaProductividadVeterinario(reporteDao),
                new EstrategiaHistoriaClinica(reporteDao));
        ControladorReporte reportes = new ControladorReporte(new ServicioReporte(estrategias));
        ControladorSistema sistema = new ControladorSistema(new ServicioRespaldo(baseDatos));

        PanelClientes panelClientes = new PanelClientes(clientes);
        PanelGatos panelGatos = new PanelGatos(gatos, clientes, reportes);
        PanelVeterinarios panelVeterinarios = new PanelVeterinarios(veterinarios);
        PanelTratamientos panelTratamientos = new PanelTratamientos(tratamientos);
        PanelTurnos panelTurnos = new PanelTurnos(
                turnos, clientes, gatos, veterinarios, tratamientos);
        PanelReportes panelReportes = new PanelReportes(reportes, veterinarios, gatos);

        return new VentanaPrincipal(
                panelClientes,
                panelGatos,
                panelTurnos,
                panelVeterinarios,
                panelTratamientos,
                panelReportes,
                sistema);
    }
}
