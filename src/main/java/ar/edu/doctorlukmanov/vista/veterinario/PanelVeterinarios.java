package ar.edu.doctorlukmanov.vista.veterinario;

import ar.edu.doctorlukmanov.controlador.ControladorVeterinario;
import ar.edu.doctorlukmanov.excepcion.ClinicaException;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.modelo.Veterinario;
import ar.edu.doctorlukmanov.vista.componentes.Dialogos;
import ar.edu.doctorlukmanov.vista.componentes.ModeloTablaNoEditable;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public final class PanelVeterinarios extends JPanel {

    private final ControladorVeterinario controlador;
    private final ModeloTablaNoEditable modelo = new ModeloTablaNoEditable(new Object[]{
        "ID", "Nombre", "Apellido", "Matrícula", "Teléfono", "Correo", "Especialidad", "Estado"
    });
    private final JTable tabla = new JTable(modelo);
    private List<Veterinario> filas = new ArrayList<>();

    public PanelVeterinarios(ControladorVeterinario controlador) {
        super(new BorderLayout(10, 10));
        this.controlador = controlador;
        JLabel titulo = new JLabel("Equipo veterinario");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        add(titulo, BorderLayout.NORTH);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(crearAcciones(), BorderLayout.SOUTH);
        refrescar();
    }

    public void nuevo() {
        if (DialogoVeterinario.mostrar(this, null, controlador::crear)) {
            refrescar();
        }
    }

    public void refrescar() {
        try {
            filas = controlador.listarTodos();
            modelo.setRowCount(0);
            filas.forEach(veterinario -> modelo.addRow(new Object[]{
                veterinario.getIdVeterinario(), veterinario.getNombre(), veterinario.getApellido(),
                veterinario.getMatricula(), veterinario.getTelefono(), veterinario.getCorreoElectronico(),
                veterinario.getEspecialidad(), veterinario.isActivo() ? "Activo" : "Inactivo"
            }));
        } catch (ClinicaException ex) {
            Dialogos.error(this, ex);
        }
    }

    private JPanel crearAcciones() {
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton nuevo = new JButton("Nuevo veterinario");
        JButton editar = new JButton("Editar");
        JButton activo = new JButton("Activar/Desactivar");
        JButton actualizar = new JButton("Actualizar lista");
        nuevo.addActionListener(evento -> nuevo());
        editar.addActionListener(evento -> editar());
        activo.addActionListener(evento -> cambiarActivo());
        actualizar.addActionListener(evento -> refrescar());
        acciones.add(nuevo);
        acciones.add(editar);
        acciones.add(activo);
        acciones.add(actualizar);
        return acciones;
    }

    private void editar() {
        ejecutarSeleccion(veterinario -> {
            if (DialogoVeterinario.mostrar(this, veterinario, controlador::actualizar)) {
                refrescar();
            }
        });
    }

    private void cambiarActivo() {
        ejecutarSeleccion(veterinario -> {
            if (Dialogos.confirmar(this, "¿Desea " + (veterinario.isActivo() ? "desactivar" : "activar")
                    + " a " + veterinario.getNombreCompleto() + "?")) {
                controlador.cambiarActivo(veterinario.getIdVeterinario(), !veterinario.isActivo());
                refrescar();
            }
        });
    }

    private void ejecutarSeleccion(Consumer<Veterinario> accion) {
        try {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                throw new ValidacionException("Seleccione un veterinario de la tabla.");
            }
            accion.accept(filas.get(tabla.convertRowIndexToModel(fila)));
        } catch (ClinicaException ex) {
            Dialogos.error(this, ex);
        }
    }
}
