package ar.edu.doctorlukmanov.vista.cliente;

import ar.edu.doctorlukmanov.controlador.ControladorCliente;
import ar.edu.doctorlukmanov.excepcion.ClinicaException;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.modelo.Cliente;
import ar.edu.doctorlukmanov.vista.componentes.Dialogos;
import ar.edu.doctorlukmanov.vista.componentes.ModeloTablaNoEditable;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public final class PanelClientes extends JPanel {

    private final ControladorCliente controlador;
    private final JTextField buscar = new JTextField(24);
    private final JComboBox<String> estado = new JComboBox<>(new String[]{"Activos", "Inactivos", "Todos"});
    private final ModeloTablaNoEditable modelo = new ModeloTablaNoEditable(new Object[]{
        "ID", "Nombre", "Apellido", "DNI", "Teléfono", "Correo electrónico", "Estado"
    });
    private final JTable tabla = new JTable(modelo);
    private List<Cliente> filas = new ArrayList<>();
    private Consumer<Cliente> alVerGatos = cliente -> Dialogos.informar(
            this, "Seleccione el módulo Gatos para consultar los pacientes de " + cliente.getNombreCompleto() + ".");

    public PanelClientes(ControladorCliente controlador) {
        super(new BorderLayout(10, 10));
        this.controlador = controlador;
        add(crearEncabezado(), BorderLayout.NORTH);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                if (evento.getClickCount() == 2) {
                    editar();
                }
            }
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(crearAcciones(), BorderLayout.SOUTH);
        refrescar();
    }

    public void setAlVerGatos(Consumer<Cliente> alVerGatos) {
        this.alVerGatos = alVerGatos;
    }

    public void nuevo() {
        if (DialogoCliente.mostrar(this, null, controlador::crear)) {
            refrescar();
            Dialogos.informar(this, "El cliente fue registrado correctamente.");
        }
    }

    public void refrescar() {
        try {
            List<Cliente> encontrados = controlador.buscar(buscar.getText());
            String filtro = (String) estado.getSelectedItem();
            filas = encontrados.stream()
                    .filter(cliente -> "Todos".equals(filtro)
                            || ("Activos".equals(filtro) && cliente.isActivo())
                            || ("Inactivos".equals(filtro) && !cliente.isActivo()))
                    .toList();
            modelo.setRowCount(0);
            filas.forEach(cliente -> modelo.addRow(new Object[]{
                cliente.getIdCliente(), cliente.getNombre(), cliente.getApellido(), cliente.getDni(),
                cliente.getTelefono(), cliente.getCorreoElectronico(),
                cliente.isActivo() ? "Activo" : "Inactivo"
            }));
        } catch (ClinicaException ex) {
            Dialogos.error(this, ex);
        }
    }

    private JPanel crearEncabezado() {
        JPanel contenedor = new JPanel(new BorderLayout());
        JLabel titulo = new JLabel("Clientes y responsables");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        contenedor.add(titulo, BorderLayout.NORTH);
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Buscar"));
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
        JButton nuevo = new JButton("Nuevo cliente");
        JButton editar = new JButton("Editar");
        JButton verGatos = new JButton("Ver gatos");
        JButton activo = new JButton("Activar/Desactivar");
        JButton eliminar = new JButton("Eliminar");
        JButton actualizar = new JButton("Actualizar lista");
        nuevo.addActionListener(evento -> nuevo());
        editar.addActionListener(evento -> editar());
        verGatos.addActionListener(evento -> ejecutarSeleccion(alVerGatos));
        activo.addActionListener(evento -> cambiarActivo());
        eliminar.addActionListener(evento -> eliminar());
        actualizar.addActionListener(evento -> refrescar());
        acciones.add(nuevo);
        acciones.add(editar);
        acciones.add(verGatos);
        acciones.add(activo);
        acciones.add(eliminar);
        acciones.add(actualizar);
        return acciones;
    }

    private void editar() {
        ejecutarSeleccion(cliente -> {
            if (DialogoCliente.mostrar(this, cliente, controlador::actualizar)) {
                refrescar();
                Dialogos.informar(this, "Los datos del cliente fueron actualizados.");
            }
        });
    }

    private void cambiarActivo() {
        ejecutarSeleccion(cliente -> {
            String accion = cliente.isActivo() ? "desactivar" : "activar";
            if (Dialogos.confirmar(this, "¿Desea " + accion + " a " + cliente.getNombreCompleto() + "?")) {
                controlador.cambiarActivo(cliente.getIdCliente(), !cliente.isActivo());
                refrescar();
            }
        });
    }

    private void eliminar() {
        ejecutarSeleccion(cliente -> {
            if (Dialogos.confirmar(this,
                    "¿Desea dar de baja al cliente? Sus antecedentes se conservarán.")) {
                controlador.eliminar(cliente.getIdCliente());
                refrescar();
            }
        });
    }

    private void ejecutarSeleccion(Consumer<Cliente> accion) {
        try {
            int filaVista = tabla.getSelectedRow();
            if (filaVista < 0) {
                throw new ValidacionException("Seleccione un cliente de la tabla.");
            }
            accion.accept(filas.get(tabla.convertRowIndexToModel(filaVista)));
        } catch (ClinicaException ex) {
            Dialogos.error(this, ex);
        }
    }
}
