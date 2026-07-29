package ar.edu.doctorlukmanov.vista.turno;

import ar.edu.doctorlukmanov.controlador.ControladorCliente;
import ar.edu.doctorlukmanov.controlador.ControladorGato;
import ar.edu.doctorlukmanov.controlador.ControladorTratamiento;
import ar.edu.doctorlukmanov.controlador.ControladorTurno;
import ar.edu.doctorlukmanov.controlador.ControladorVeterinario;
import ar.edu.doctorlukmanov.excepcion.ClinicaException;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.modelo.Atencion;
import ar.edu.doctorlukmanov.modelo.Cliente;
import ar.edu.doctorlukmanov.modelo.DetalleTratamiento;
import ar.edu.doctorlukmanov.modelo.EstadoTurno;
import ar.edu.doctorlukmanov.modelo.Gato;
import ar.edu.doctorlukmanov.modelo.Tratamiento;
import ar.edu.doctorlukmanov.modelo.Turno;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;

public final class PanelTurnos extends JPanel {

    private final ControladorTurno controlador;
    private final ControladorCliente controladorCliente;
    private final ControladorGato controladorGato;
    private final ControladorVeterinario controladorVeterinario;
    private final ControladorTratamiento controladorTratamiento;
    private final JSpinner fecha = new JSpinner(new SpinnerDateModel());
    private final JComboBox<Veterinario> veterinario = new JComboBox<>();
    private final JComboBox<Object> estado = new JComboBox<>();
    private final JTextField buscar = new JTextField(17);
    private final ModeloTablaNoEditable modelo = new ModeloTablaNoEditable(new Object[]{
        "Hora", "Gato", "Cliente", "Veterinario", "Motivo", "Estado", "Duración"
    });
    private final JTable tabla = new JTable(modelo);
    private final JButton confirmar = new JButton("Confirmar");
    private final JButton completar = new JButton("Completar");
    private final JButton cancelar = new JButton("Cancelar");
    private final JButton editar = new JButton("Editar");
    private final JButton verDetalle = new JButton("Ver detalle");
    private List<Turno> filas = new ArrayList<>();
    private Map<Long, Gato> gatos = new LinkedHashMap<>();
    private Map<Long, Cliente> clientes = new LinkedHashMap<>();
    private Map<Long, Veterinario> veterinarios = new LinkedHashMap<>();

    public PanelTurnos(
            ControladorTurno controlador,
            ControladorCliente controladorCliente,
            ControladorGato controladorGato,
            ControladorVeterinario controladorVeterinario,
            ControladorTratamiento controladorTratamiento) {
        super(new BorderLayout(10, 10));
        this.controlador = controlador;
        this.controladorCliente = controladorCliente;
        this.controladorGato = controladorGato;
        this.controladorVeterinario = controladorVeterinario;
        this.controladorTratamiento = controladorTratamiento;
        fecha.setEditor(new JSpinner.DateEditor(fecha, "dd/MM/yyyy"));
        estado.addItem("Todos");
        for (EstadoTurno valor : EstadoTurno.values()) {
            estado.addItem(valor);
        }
        configurarRenderizadores();
        add(crearEncabezado(), BorderLayout.NORTH);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.getSelectionModel().addListSelectionListener(evento -> actualizarAcciones());
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(crearAcciones(), BorderLayout.SOUTH);
        refrescar();
    }

    public void nuevo() {
        nuevoParaGato(null);
    }

    public void nuevoParaGato(Gato preferido) {
        try {
            List<Cliente> clientesActivos = controladorCliente.listarActivos();
            List<Gato> gatosActivos = controladorGato.listarActivos();
            List<Veterinario> veterinariosActivos = controladorVeterinario.listarActivos();
            if (clientesActivos.isEmpty() || gatosActivos.isEmpty() || veterinariosActivos.isEmpty()) {
                throw new ValidacionException(
                        "Se requieren un cliente, un gato y un veterinario activos para programar un turno.");
            }
            if (DialogoTurno.mostrar(this, null, preferido, clientesActivos, gatosActivos,
                    veterinariosActivos, controlador::programar)) {
                refrescar();
                Dialogos.informar(this, "El turno fue programado correctamente.");
            }
        } catch (ClinicaException ex) {
            Dialogos.error(this, ex.getMessage());
        }
    }

    public void refrescar() {
        try {
            List<Cliente> listaClientes = controladorCliente.listarTodos();
            List<Gato> listaGatos = controladorGato.listarTodos();
            List<Veterinario> listaVeterinarios = controladorVeterinario.listarTodos();
            clientes = indexarClientes(listaClientes);
            gatos = indexarGatos(listaGatos);
            veterinarios = indexarVeterinarios(listaVeterinarios);
            Veterinario filtroAnterior = (Veterinario) veterinario.getSelectedItem();
            veterinario.removeAllItems();
            veterinario.addItem(null);
            listaVeterinarios.forEach(veterinario::addItem);
            if (filtroAnterior != null) {
                listaVeterinarios.stream()
                        .filter(item -> item.getIdVeterinario().equals(filtroAnterior.getIdVeterinario()))
                        .findFirst().ifPresent(veterinario::setSelectedItem);
            }

            LocalDate dia = fechaSeleccionada();
            Veterinario filtroVeterinario = (Veterinario) veterinario.getSelectedItem();
            Object filtroEstado = estado.getSelectedItem();
            String texto = buscar.getText().trim().toLowerCase(Locale.ROOT);
            filas = controlador.listarPorFecha(dia).stream()
                    .filter(turno -> filtroVeterinario == null
                            || turno.getIdVeterinario().equals(filtroVeterinario.getIdVeterinario()))
                    .filter(turno -> !(filtroEstado instanceof EstadoTurno valor)
                            || turno.getEstado() == valor)
                    .filter(turno -> coincideTexto(turno, texto))
                    .toList();
            modelo.setRowCount(0);
            filas.forEach(turno -> modelo.addRow(new Object[]{
                turno.getFechaHora().toLocalTime(),
                nombreGato(turno),
                nombreCliente(turno),
                nombreVeterinario(turno),
                turno.getMotivo(),
                turno.getEstado().getDescripcion(),
                turno.getDuracionMinutos() + " min"
            }));
            actualizarAcciones();
        } catch (ClinicaException ex) {
            Dialogos.error(this, ex.getMessage());
        }
    }

    private void configurarRenderizadores() {
        veterinario.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> lista, Object valor, int indice, boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                setText(valor == null ? "Todos" : ((Veterinario) valor).getNombreCompleto());
                return this;
            }
        });
    }

    private JPanel crearEncabezado() {
        JPanel contenedor = new JPanel(new BorderLayout());
        JLabel titulo = new JLabel("Agenda de turnos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        contenedor.add(titulo, BorderLayout.NORTH);
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Fecha"));
        filtros.add(fecha);
        filtros.add(new JLabel("Veterinario"));
        filtros.add(veterinario);
        filtros.add(new JLabel("Estado"));
        filtros.add(estado);
        filtros.add(new JLabel("Gato o cliente"));
        filtros.add(buscar);
        JButton aplicar = new JButton("Actualizar agenda");
        aplicar.addActionListener(evento -> refrescar());
        buscar.addActionListener(evento -> refrescar());
        filtros.add(aplicar);
        contenedor.add(filtros, BorderLayout.SOUTH);
        return contenedor;
    }

    private JPanel crearAcciones() {
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton nuevo = new JButton("Programar turno");
        nuevo.addActionListener(evento -> nuevo());
        editar.addActionListener(evento -> editar());
        confirmar.addActionListener(evento -> confirmar());
        completar.addActionListener(evento -> completar());
        cancelar.addActionListener(evento -> cancelar());
        verDetalle.addActionListener(evento -> verDetalle());
        acciones.add(nuevo);
        acciones.add(editar);
        acciones.add(confirmar);
        acciones.add(completar);
        acciones.add(cancelar);
        acciones.add(verDetalle);
        actualizarAcciones();
        return acciones;
    }

    private void editar() {
        ejecutarSeleccion(turno -> {
            Gato actual = gatos.get(turno.getIdGato());
            List<Cliente> clientesDisponibles = new ArrayList<>(controladorCliente.listarActivos());
            Cliente responsable = clientes.get(actual.getIdCliente());
            agregarSiFaltaCliente(clientesDisponibles, responsable);
            List<Gato> gatosDisponibles = new ArrayList<>(controladorGato.listarActivos());
            agregarSiFaltaGato(gatosDisponibles, actual);
            List<Veterinario> veterinariosDisponibles = new ArrayList<>(controladorVeterinario.listarActivos());
            agregarSiFaltaVeterinario(veterinariosDisponibles, veterinarios.get(turno.getIdVeterinario()));
            if (DialogoTurno.mostrar(this, turno, actual, clientesDisponibles, gatosDisponibles,
                    veterinariosDisponibles, dto -> controlador.actualizar(turno.getIdTurno(), dto))) {
                refrescar();
            }
        });
    }

    private void confirmar() {
        ejecutarSeleccion(turno -> {
            if (Dialogos.confirmar(this, "¿Confirma el turno de " + nombreGato(turno) + "?")) {
                controlador.confirmar(turno.getIdTurno());
                refrescar();
                Dialogos.informar(this, "El turno fue confirmado correctamente.");
            }
        });
    }

    private void cancelar() {
        ejecutarSeleccion(turno -> {
            if (DialogoCancelarTurno.mostrar(this, resumen(turno),
                    motivo -> controlador.cancelar(turno.getIdTurno(), motivo))) {
                refrescar();
                Dialogos.informar(this, "El turno fue cancelado correctamente.");
            }
        });
    }

    private void completar() {
        ejecutarSeleccion(turno -> {
            if (DialogoCompletarTurno.mostrar(this, turno, resumen(turno),
                    controladorTratamiento.listarActivos(), controlador::completar)) {
                refrescar();
                Dialogos.informar(this,
                        "El turno se completó y la atención clínica fue registrada correctamente.");
            }
        });
    }

    private void verDetalle() {
        ejecutarSeleccion(turno -> {
            if (turno.getEstado() == EstadoTurno.COMPLETADO) {
                Atencion atencion = controlador.buscarAtencion(turno.getIdTurno());
                Map<Long, Tratamiento> catalogo = new LinkedHashMap<>();
                controladorTratamiento.listarTodos().forEach(
                        item -> catalogo.put(item.getIdTratamiento(), item));
                String tratamientosTexto = atencion.getTratamientos().stream()
                        .map(detalle -> describirTratamiento(detalle, catalogo))
                        .reduce((uno, otro) -> uno + "<br>" + otro)
                        .orElse("Sin tratamientos");
                JOptionPane.showMessageDialog(this,
                        "<html>" + resumen(turno)
                                + "<br><b>Diagnóstico:</b> " + atencion.getDiagnostico()
                                + "<br><b>Peso:</b> " + valor(atencion.getPesoRegistrado())
                                + "<br><b>Temperatura:</b> " + valor(atencion.getTemperatura())
                                + "<br><b>Indicaciones:</b> " + valor(atencion.getIndicaciones())
                                + "<br><b>Tratamientos:</b><br>" + tratamientosTexto + "</html>",
                        "Atención clínica", JOptionPane.INFORMATION_MESSAGE);
            } else if (turno.getEstado() == EstadoTurno.CANCELADO) {
                Dialogos.informar(this, "Motivo de cancelación: " + turno.getMotivoCancelacion());
            } else {
                Dialogos.informar(this, resumen(turno).replace("<br>", "\n"));
            }
        });
    }

    private void ejecutarSeleccion(Consumer<Turno> accion) {
        try {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                throw new ValidacionException("Seleccione un turno de la agenda.");
            }
            accion.accept(filas.get(tabla.convertRowIndexToModel(fila)));
        } catch (ClinicaException ex) {
            Dialogos.error(this, ex.getMessage());
        }
    }

    private void actualizarAcciones() {
        int fila = tabla.getSelectedRow();
        Turno seleccionado = fila < 0 ? null : filas.get(tabla.convertRowIndexToModel(fila));
        boolean programado = seleccionado != null && seleccionado.getEstado() == EstadoTurno.PROGRAMADO;
        boolean abierto = seleccionado != null && seleccionado.estaAbierto();
        confirmar.setEnabled(programado);
        editar.setEnabled(programado);
        completar.setEnabled(abierto);
        cancelar.setEnabled(abierto);
        verDetalle.setEnabled(seleccionado != null);
    }

    private LocalDate fechaSeleccionada() {
        return ((Date) fecha.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private boolean coincideTexto(Turno turno, String texto) {
        if (texto.isBlank()) {
            return true;
        }
        return nombreGato(turno).toLowerCase(Locale.ROOT).contains(texto)
                || nombreCliente(turno).toLowerCase(Locale.ROOT).contains(texto);
    }

    private String nombreGato(Turno turno) {
        Gato gato = gatos.get(turno.getIdGato());
        return gato == null ? "-" : gato.getNombre();
    }

    private String nombreCliente(Turno turno) {
        Gato gato = gatos.get(turno.getIdGato());
        Cliente cliente = gato == null ? null : clientes.get(gato.getIdCliente());
        return cliente == null ? "-" : cliente.getNombreCompleto();
    }

    private String nombreVeterinario(Turno turno) {
        Veterinario profesional = veterinarios.get(turno.getIdVeterinario());
        return profesional == null ? "-" : profesional.getNombreCompleto();
    }

    private String resumen(Turno turno) {
        return "<b>Turno:</b> " + turno.getIdTurno()
                + "<br><b>Fecha y hora:</b> " + FechasUtil.FECHA_HORA_VISIBLE.format(turno.getFechaHora())
                + "<br><b>Gato:</b> " + nombreGato(turno)
                + "<br><b>Cliente:</b> " + nombreCliente(turno)
                + "<br><b>Veterinario:</b> " + nombreVeterinario(turno)
                + "<br><b>Motivo:</b> " + turno.getMotivo();
    }

    private String describirTratamiento(
            DetalleTratamiento detalle, Map<Long, Tratamiento> catalogo) {
        Tratamiento tratamiento = catalogo.get(detalle.getIdTratamiento());
        return (tratamiento == null ? "Tratamiento " + detalle.getIdTratamiento() : tratamiento.getNombre())
                + (detalle.getDosis() == null ? "" : " - " + detalle.getDosis());
    }

    private String valor(Object valor) {
        return valor == null ? "Sin dato" : valor.toString();
    }

    private Map<Long, Cliente> indexarClientes(List<Cliente> elementos) {
        Map<Long, Cliente> resultado = new LinkedHashMap<>();
        elementos.forEach(item -> resultado.put(item.getIdCliente(), item));
        return resultado;
    }

    private Map<Long, Gato> indexarGatos(List<Gato> elementos) {
        Map<Long, Gato> resultado = new LinkedHashMap<>();
        elementos.forEach(item -> resultado.put(item.getIdGato(), item));
        return resultado;
    }

    private Map<Long, Veterinario> indexarVeterinarios(List<Veterinario> elementos) {
        Map<Long, Veterinario> resultado = new LinkedHashMap<>();
        elementos.forEach(item -> resultado.put(item.getIdVeterinario(), item));
        return resultado;
    }

    private void agregarSiFaltaCliente(List<Cliente> lista, Cliente valor) {
        if (valor != null && lista.stream().noneMatch(item -> item.getIdCliente().equals(valor.getIdCliente()))) {
            lista.add(valor);
        }
    }

    private void agregarSiFaltaGato(List<Gato> lista, Gato valor) {
        if (valor != null && lista.stream().noneMatch(item -> item.getIdGato().equals(valor.getIdGato()))) {
            lista.add(valor);
        }
    }

    private void agregarSiFaltaVeterinario(List<Veterinario> lista, Veterinario valor) {
        if (valor != null && lista.stream()
                .noneMatch(item -> item.getIdVeterinario().equals(valor.getIdVeterinario()))) {
            lista.add(valor);
        }
    }
}
