package ar.edu.doctorlukmanov.vista.reporte;

import ar.edu.doctorlukmanov.controlador.ControladorGato;
import ar.edu.doctorlukmanov.controlador.ControladorReporte;
import ar.edu.doctorlukmanov.controlador.ControladorVeterinario;
import ar.edu.doctorlukmanov.dto.FiltroReporteDto;
import ar.edu.doctorlukmanov.dto.HistoriaClinicaDto;
import ar.edu.doctorlukmanov.dto.ReportePacientesMesDto;
import ar.edu.doctorlukmanov.dto.ReporteProductividadVeterinarioDto;
import ar.edu.doctorlukmanov.dto.ReporteTratamientoFrecuenteDto;
import ar.edu.doctorlukmanov.dto.ReporteTurnosMensualesDto;
import ar.edu.doctorlukmanov.estrategia.TipoReporte;
import ar.edu.doctorlukmanov.excepcion.ClinicaException;
import ar.edu.doctorlukmanov.modelo.Gato;
import ar.edu.doctorlukmanov.modelo.Veterinario;
import ar.edu.doctorlukmanov.util.FechasUtil;
import ar.edu.doctorlukmanov.vista.componentes.Dialogos;
import ar.edu.doctorlukmanov.vista.componentes.ModeloTablaNoEditable;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingWorker;

public final class PanelReportes extends JPanel {

    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MM/yyyy");

    private final ControladorReporte controlador;
    private final ControladorVeterinario controladorVeterinario;
    private final ControladorGato controladorGato;
    private final JComboBox<TipoReporte> tipo = new JComboBox<>();
    private final JSpinner desde = new JSpinner(new SpinnerDateModel());
    private final JSpinner hasta = new JSpinner(new SpinnerDateModel());
    private final JComboBox<Veterinario> veterinario = new JComboBox<>();
    private final JComboBox<Gato> gato = new JComboBox<>();
    private final JButton generar = new JButton("Generar");
    private final JLabel estado = new JLabel("Seleccione filtros y genere un reporte.");
    private final JTable tabla = new JTable(new ModeloTablaNoEditable(new Object[]{"Resultado"}));

    public PanelReportes(
            ControladorReporte controlador,
            ControladorVeterinario controladorVeterinario,
            ControladorGato controladorGato) {
        super(new BorderLayout(10, 10));
        this.controlador = controlador;
        this.controladorVeterinario = controladorVeterinario;
        this.controladorGato = controladorGato;
        controlador.listarTipos().forEach(tipo::addItem);
        configurarFechas();
        configurarRenderizadores();
        cargarSelectores();
        add(crearEncabezado(), BorderLayout.NORTH);
        tabla.setAutoCreateRowSorter(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);
        actualizarFiltros();
    }

    public void seleccionarTipo(TipoReporte tipoReporte) {
        tipo.setSelectedItem(tipoReporte);
        actualizarFiltros();
    }

    private void configurarFechas() {
        LocalDate hoy = LocalDate.now();
        desde.setValue(Date.from(hoy.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        hasta.setValue(Date.from(hoy.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        desde.setEditor(new JSpinner.DateEditor(desde, "dd/MM/yyyy"));
        hasta.setEditor(new JSpinner.DateEditor(hasta, "dd/MM/yyyy"));
    }

    private void configurarRenderizadores() {
        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> lista, Object valor, int indice, boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor == null) {
                    setText("Todos");
                } else if (valor instanceof Veterinario profesional) {
                    setText(profesional.getNombreCompleto());
                } else if (valor instanceof Gato paciente) {
                    setText(paciente.getNombre());
                }
                return this;
            }
        };
        veterinario.setRenderer(renderer);
        gato.setRenderer(renderer);
    }

    private void cargarSelectores() {
        try {
            veterinario.addItem(null);
            controladorVeterinario.listarTodos().forEach(veterinario::addItem);
            gato.addItem(null);
            controladorGato.listarTodos().forEach(gato::addItem);
        } catch (ClinicaException ex) {
            Dialogos.error(this, ex.getMessage());
        }
    }

    private JPanel crearEncabezado() {
        JPanel contenedor = new JPanel(new BorderLayout());
        JLabel titulo = new JLabel("Reportes y estadísticas");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        contenedor.add(titulo, BorderLayout.NORTH);
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Tipo de reporte"));
        filtros.add(tipo);
        filtros.add(new JLabel("Fecha desde"));
        filtros.add(desde);
        filtros.add(new JLabel("Fecha hasta"));
        filtros.add(hasta);
        filtros.add(new JLabel("Veterinario"));
        filtros.add(veterinario);
        filtros.add(new JLabel("Gato"));
        filtros.add(gato);
        JButton limpiar = new JButton("Limpiar");
        tipo.addActionListener(evento -> actualizarFiltros());
        generar.addActionListener(evento -> generar());
        limpiar.addActionListener(evento -> limpiar());
        filtros.add(generar);
        filtros.add(limpiar);
        contenedor.add(filtros, BorderLayout.SOUTH);
        return contenedor;
    }

    private void actualizarFiltros() {
        TipoReporte seleccionado = (TipoReporte) tipo.getSelectedItem();
        boolean historia = seleccionado == TipoReporte.HISTORIA_CLINICA;
        boolean productividad = seleccionado == TipoReporte.PRODUCTIVIDAD_VETERINARIO;
        desde.setEnabled(!historia);
        hasta.setEnabled(!historia);
        veterinario.setEnabled(productividad);
        gato.setEnabled(historia);
    }

    private void generar() {
        TipoReporte seleccionado = (TipoReporte) tipo.getSelectedItem();
        Veterinario profesional = (Veterinario) veterinario.getSelectedItem();
        Gato paciente = (Gato) gato.getSelectedItem();
        FiltroReporteDto filtro = new FiltroReporteDto(
                seleccionado == TipoReporte.HISTORIA_CLINICA ? null : fecha(desde),
                seleccionado == TipoReporte.HISTORIA_CLINICA ? null : fecha(hasta),
                profesional == null ? null : profesional.getIdVeterinario(),
                paciente == null ? null : paciente.getIdGato());
        generar.setEnabled(false);
        estado.setText("Generando reporte...");
        new SwingWorker<List<?>, Void>() {
            @Override
            protected List<?> doInBackground() {
                return controlador.generar(seleccionado, filtro);
            }

            @Override
            protected void done() {
                try {
                    List<?> resultado = get();
                    mostrarResultado(seleccionado, resultado);
                    estado.setText(resultado.isEmpty()
                            ? "No se encontraron datos para los filtros seleccionados."
                            : "Reporte generado: " + resultado.size() + " fila(s).");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    Dialogos.error(PanelReportes.this, "La generación del reporte fue interrumpida.");
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause();
                    Dialogos.error(PanelReportes.this,
                            causa instanceof ClinicaException ? causa.getMessage()
                                    : "Ocurrió un error inesperado al generar el reporte.");
                } finally {
                    generar.setEnabled(true);
                }
            }
        }.execute();
    }

    private void mostrarResultado(TipoReporte reporte, List<?> filas) {
        ModeloTablaNoEditable modelo;
        switch (reporte) {
            case PACIENTES_POR_MES -> {
                modelo = new ModeloTablaNoEditable(new Object[]{"Mes", "Pacientes registrados"});
                filas.stream().map(ReportePacientesMesDto.class::cast).forEach(fila ->
                        modelo.addRow(new Object[]{MES.format(fila.mes().atDay(1)), fila.cantidadPacientes()}));
            }
            case TURNOS_POR_MES -> {
                modelo = new ModeloTablaNoEditable(new Object[]{
                    "Mes", "Programados", "Confirmados", "Completados", "Cancelados", "Total", "% finalización"
                });
                filas.stream().map(ReporteTurnosMensualesDto.class::cast).forEach(fila -> modelo.addRow(new Object[]{
                    MES.format(fila.mes().atDay(1)), fila.programados(), fila.confirmados(), fila.completados(),
                    fila.cancelados(), fila.total(), porcentaje(fila.porcentajeFinalizacion())
                }));
            }
            case TRATAMIENTOS_FRECUENTES -> {
                modelo = new ModeloTablaNoEditable(new Object[]{
                    "Posición", "Tratamiento", "Aplicaciones", "Cantidad total", "% aplicaciones"
                });
                int[] posicion = {1};
                filas.stream().map(ReporteTratamientoFrecuenteDto.class::cast).forEach(fila ->
                        modelo.addRow(new Object[]{posicion[0]++, fila.nombreTratamiento(),
                            fila.cantidadAplicaciones(), fila.cantidadTotal(), porcentaje(fila.porcentaje())}));
            }
            case PRODUCTIVIDAD_VETERINARIO -> {
                modelo = new ModeloTablaNoEditable(new Object[]{
                    "Veterinario", "Turnos totales", "Completados", "Cancelados", "Tasa de finalización"
                });
                filas.stream().map(ReporteProductividadVeterinarioDto.class::cast).forEach(fila ->
                        modelo.addRow(new Object[]{fila.nombreVeterinario(), fila.totalTurnos(),
                            fila.turnosCompletados(), fila.turnosCancelados(), porcentaje(fila.tasaFinalizacion())}));
            }
            case HISTORIA_CLINICA -> {
                modelo = new ModeloTablaNoEditable(new Object[]{
                    "Fecha", "Gato", "Cliente", "Veterinario", "Motivo", "Diagnóstico", "Peso",
                    "Temperatura", "Tratamientos", "Indicaciones"
                });
                filas.stream().map(HistoriaClinicaDto.class::cast).forEach(fila -> modelo.addRow(new Object[]{
                    FechasUtil.FECHA_HORA_VISIBLE.format(fila.fechaHora()), fila.nombreGato(), fila.nombreCliente(),
                    fila.nombreVeterinario(), fila.motivo(), fila.diagnostico(), fila.pesoRegistrado(),
                    fila.temperatura(), fila.tratamientos(), fila.indicaciones()
                }));
            }
            default -> throw new IllegalStateException("Tipo de reporte no soportado: " + reporte);
        }
        tabla.setModel(modelo);
        tabla.setAutoCreateRowSorter(true);
    }

    private void limpiar() {
        tabla.setModel(new ModeloTablaNoEditable(new Object[]{"Resultado"}));
        veterinario.setSelectedIndex(0);
        gato.setSelectedIndex(0);
        estado.setText("Filtros restablecidos.");
    }

    private LocalDate fecha(JSpinner spinner) {
        return ((Date) spinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private String porcentaje(double valor) {
        return String.format("%.2f %%", valor);
    }
}
