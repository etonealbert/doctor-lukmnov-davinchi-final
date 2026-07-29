package ar.edu.doctorlukmanov.vista.tratamiento;

import ar.edu.doctorlukmanov.controlador.ControladorTratamiento;
import ar.edu.doctorlukmanov.excepcion.ClinicaException;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.modelo.Tratamiento;
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

public final class PanelTratamientos extends JPanel {

    private final ControladorTratamiento controlador;
    private final ModeloTablaNoEditable modelo = new ModeloTablaNoEditable(new Object[]{
        "ID", "Nombre", "Descripción", "Precio de referencia", "Estado"
    });
    private final JTable tabla = new JTable(modelo);
    private List<Tratamiento> filas = new ArrayList<>();

    public PanelTratamientos(ControladorTratamiento controlador) {
        super(new BorderLayout(10, 10));
        this.controlador = controlador;
        JLabel titulo = new JLabel("Catálogo de tratamientos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        add(titulo, BorderLayout.NORTH);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(crearAcciones(), BorderLayout.SOUTH);
        refrescar();
    }

    public void nuevo() {
        if (DialogoTratamiento.mostrar(this, null, controlador::crear)) {
            refrescar();
        }
    }

    public void refrescar() {
        try {
            filas = controlador.listarTodos();
            modelo.setRowCount(0);
            filas.forEach(tratamiento -> modelo.addRow(new Object[]{
                tratamiento.getIdTratamiento(), tratamiento.getNombre(), tratamiento.getDescripcion(),
                tratamiento.getPrecioReferencia(), tratamiento.isActivo() ? "Activo" : "Inactivo"
            }));
        } catch (ClinicaException ex) {
            Dialogos.error(this, ex.getMessage());
        }
    }

    private JPanel crearAcciones() {
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton nuevo = new JButton("Nuevo tratamiento");
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
        ejecutarSeleccion(tratamiento -> {
            if (DialogoTratamiento.mostrar(this, tratamiento, controlador::actualizar)) {
                refrescar();
            }
        });
    }

    private void cambiarActivo() {
        ejecutarSeleccion(tratamiento -> {
            if (Dialogos.confirmar(this, "¿Desea " + (tratamiento.isActivo() ? "desactivar" : "activar")
                    + " el tratamiento " + tratamiento.getNombre() + "?")) {
                controlador.cambiarActivo(tratamiento.getIdTratamiento(), !tratamiento.isActivo());
                refrescar();
            }
        });
    }

    private void ejecutarSeleccion(Consumer<Tratamiento> accion) {
        try {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                throw new ValidacionException("Seleccione un tratamiento de la tabla.");
            }
            accion.accept(filas.get(tabla.convertRowIndexToModel(fila)));
        } catch (ClinicaException ex) {
            Dialogos.error(this, ex.getMessage());
        }
    }
}
