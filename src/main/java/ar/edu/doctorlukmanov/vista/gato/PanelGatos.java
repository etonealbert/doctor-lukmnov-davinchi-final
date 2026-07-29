package ar.edu.doctorlukmanov.vista.gato;

import ar.edu.doctorlukmanov.controlador.ControladorCliente;
import ar.edu.doctorlukmanov.controlador.ControladorGato;
import ar.edu.doctorlukmanov.controlador.ControladorReporte;
import ar.edu.doctorlukmanov.dto.FiltroReporteDto;
import ar.edu.doctorlukmanov.dto.HistoriaClinicaDto;
import ar.edu.doctorlukmanov.estrategia.TipoReporte;
import ar.edu.doctorlukmanov.excepcion.ClinicaException;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.modelo.Cliente;
import ar.edu.doctorlukmanov.modelo.Gato;
import ar.edu.doctorlukmanov.vista.componentes.Dialogos;
import ar.edu.doctorlukmanov.vista.componentes.ModeloTablaNoEditable;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public final class PanelGatos extends JPanel {

    private final ControladorGato controlador;
    private final ControladorCliente controladorCliente;
    private final ControladorReporte controladorReporte;
    private final JTextField buscar = new JTextField(18);
    private final JComboBox<Cliente> cliente = new JComboBox<>();
    private final JComboBox<String> estado = new JComboBox<>(new String[]{"Activos", "Inactivos", "Todos"});
    private final ModeloTablaNoEditable modelo = new ModeloTablaNoEditable(new Object[]{
        "ID", "Nombre", "Cliente", "Sexo", "Raza", "Edad", "Peso", "Microchip", "Estado"
    });
    private final JTable tabla = new JTable(modelo);
    private List<Gato> filas = new ArrayList<>();
    private Map<Long, Cliente> clientes = new LinkedHashMap<>();
    private Consumer<Gato> alProgramarTurno = gato -> Dialogos.informar(
            this, "Seleccione el módulo Turnos para programar una consulta para " + gato.getNombre() + ".");

    public PanelGatos(
            ControladorGato controlador,
            ControladorCliente controladorCliente,
            ControladorReporte controladorReporte) {
        super(new BorderLayout(10, 10));
        this.controlador = controlador;
        this.controladorCliente = controladorCliente;
        this.controladorReporte = controladorReporte;
        configurarSelectorCliente();
        add(crearEncabezado(), BorderLayout.NORTH);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(crearAcciones(), BorderLayout.SOUTH);
        refrescar();
    }

    public void setAlProgramarTurno(Consumer<Gato> alProgramarTurno) {
        this.alProgramarTurno = alProgramarTurno;
    }

    public void nuevo() {
        List<Cliente> activos = controladorCliente.listarActivos();
        if (activos.isEmpty()) {
            Dialogos.error(this, "Registre un cliente activo antes de registrar un gato.");
            return;
        }
        if (DialogoGato.mostrar(this, null, activos, controlador::crear)) {
            refrescar();
            Dialogos.informar(this, "El gato fue registrado correctamente.");
        }
    }

    public void filtrarPorCliente(Cliente seleccionado) {
        cliente.setSelectedItem(seleccionado);
        refrescar();
    }

    public void refrescar() {
        try {
            List<Cliente> todosClientes = controladorCliente.listarTodos();
            clientes = new LinkedHashMap<>();
            for (Cliente item : todosClientes) {
                clientes.put(item.getIdCliente(), item);
            }
            Cliente filtroClienteAnterior = (Cliente) cliente.getSelectedItem();
            cliente.removeAllItems();
            cliente.addItem(null);
            todosClientes.forEach(cliente::addItem);
            if (filtroClienteAnterior != null) {
                todosClientes.stream()
                        .filter(item -> item.getIdCliente().equals(filtroClienteAnterior.getIdCliente()))
                        .findFirst().ifPresent(cliente::setSelectedItem);
            }

            Cliente filtroCliente = (Cliente) cliente.getSelectedItem();
            String filtroEstado = (String) estado.getSelectedItem();
            filas = controlador.buscar(buscar.getText()).stream()
                    .filter(gato -> filtroCliente == null || gato.getIdCliente().equals(filtroCliente.getIdCliente()))
                    .filter(gato -> "Todos".equals(filtroEstado)
                            || ("Activos".equals(filtroEstado) && gato.isActivo())
                            || ("Inactivos".equals(filtroEstado) && !gato.isActivo()))
                    .toList();
            modelo.setRowCount(0);
            filas.forEach(gato -> modelo.addRow(new Object[]{
                gato.getIdGato(),
                gato.getNombre(),
                nombreCliente(gato.getIdCliente()),
                gato.getSexo().getDescripcion(),
                gato.getRaza(),
                edad(gato),
                gato.getPesoActual(),
                gato.getNumeroMicrochip(),
                gato.isActivo() ? "Activo" : "Inactivo"
            }));
        } catch (ClinicaException ex) {
            Dialogos.error(this, ex.getMessage());
        }
    }

    private void configurarSelectorCliente() {
        cliente.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> lista, Object valor, int indice, boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                setText(valor == null ? "Todos" : ((Cliente) valor).getNombreCompleto());
                return this;
            }
        });
    }

    private JPanel crearEncabezado() {
        JPanel contenedor = new JPanel(new BorderLayout());
        JLabel titulo = new JLabel("Pacientes felinos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        contenedor.add(titulo, BorderLayout.NORTH);
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Cliente"));
        filtros.add(cliente);
        filtros.add(new JLabel("Nombre o microchip"));
        filtros.add(buscar);
        filtros.add(new JLabel("Estado"));
        filtros.add(estado);
        JButton aplicar = new JButton("Buscar");
        aplicar.addActionListener(evento -> refrescar());
        buscar.addActionListener(evento -> refrescar());
        estado.addActionListener(evento -> refrescar());
        filtros.add(aplicar);
        contenedor.add(filtros, BorderLayout.SOUTH);
        return contenedor;
    }

    private JPanel crearAcciones() {
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton nuevo = new JButton("Registrar gato");
        JButton editar = new JButton("Editar");
        JButton historia = new JButton("Historia clínica");
        JButton programar = new JButton("Programar turno");
        JButton activo = new JButton("Activar/Desactivar");
        JButton eliminar = new JButton("Eliminar");
        nuevo.addActionListener(evento -> nuevo());
        editar.addActionListener(evento -> editar());
        historia.addActionListener(evento -> mostrarHistoria());
        programar.addActionListener(evento -> ejecutarSeleccion(alProgramarTurno));
        activo.addActionListener(evento -> cambiarActivo());
        eliminar.addActionListener(evento -> eliminar());
        acciones.add(nuevo);
        acciones.add(editar);
        acciones.add(historia);
        acciones.add(programar);
        acciones.add(activo);
        acciones.add(eliminar);
        return acciones;
    }

    private void editar() {
        ejecutarSeleccion(gato -> {
            List<Cliente> opciones = new ArrayList<>(controladorCliente.listarActivos());
            if (opciones.stream().noneMatch(item -> item.getIdCliente().equals(gato.getIdCliente()))) {
                opciones.add(controladorCliente.buscarPorId(gato.getIdCliente()));
            }
            if (DialogoGato.mostrar(this, gato, opciones, controlador::actualizar)) {
                refrescar();
            }
        });
    }

    private void mostrarHistoria() {
        ejecutarSeleccion(gato -> {
            List<HistoriaClinicaDto> historia = controladorReporte.generar(
                    TipoReporte.HISTORIA_CLINICA, new FiltroReporteDto(null, null, null, gato.getIdGato()));
            ModeloTablaNoEditable tablaHistoria = new ModeloTablaNoEditable(new Object[]{
                "Fecha", "Veterinario", "Motivo", "Diagnóstico", "Peso", "Temperatura", "Tratamientos"
            });
            historia.forEach(fila -> tablaHistoria.addRow(new Object[]{
                fila.fechaHora(), fila.nombreVeterinario(), fila.motivo(), fila.diagnostico(),
                fila.pesoRegistrado(), fila.temperatura(), fila.tratamientos()
            }));
            JTable vista = new JTable(tablaHistoria);
            vista.setAutoCreateRowSorter(true);
            JScrollPane desplazamiento = new JScrollPane(vista);
            desplazamiento.setPreferredSize(new java.awt.Dimension(900, 300));
            JOptionPane.showMessageDialog(this, desplazamiento,
                    "Historia clínica de " + gato.getNombre(), JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void cambiarActivo() {
        ejecutarSeleccion(gato -> {
            if (Dialogos.confirmar(this,
                    "¿Desea " + (gato.isActivo() ? "desactivar" : "activar") + " a " + gato.getNombre() + "?")) {
                controlador.cambiarActivo(gato.getIdGato(), !gato.isActivo());
                refrescar();
            }
        });
    }

    private void eliminar() {
        ejecutarSeleccion(gato -> {
            if (Dialogos.confirmar(this, "¿Desea dar de baja a " + gato.getNombre()
                    + "? Su historia clínica se conservará.")) {
                controlador.eliminar(gato.getIdGato());
                refrescar();
            }
        });
    }

    private void ejecutarSeleccion(Consumer<Gato> accion) {
        try {
            int filaVista = tabla.getSelectedRow();
            if (filaVista < 0) {
                throw new ValidacionException("Seleccione un gato de la tabla.");
            }
            accion.accept(filas.get(tabla.convertRowIndexToModel(filaVista)));
        } catch (ClinicaException ex) {
            Dialogos.error(this, ex.getMessage());
        }
    }

    private String nombreCliente(Long idCliente) {
        Cliente responsable = clientes.get(idCliente);
        return responsable == null ? "-" : responsable.getNombreCompleto();
    }

    private String edad(Gato gato) {
        if (gato.getFechaNacimiento() == null) {
            return "Sin dato";
        }
        Period periodo = gato.calcularEdadAproximada();
        return periodo.getYears() + " a " + periodo.getMonths() + " m";
    }
}
