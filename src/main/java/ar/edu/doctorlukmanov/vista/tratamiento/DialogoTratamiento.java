package ar.edu.doctorlukmanov.vista.tratamiento;

import ar.edu.doctorlukmanov.dto.TratamientoFormularioDto;
import ar.edu.doctorlukmanov.excepcion.ClinicaException;
import ar.edu.doctorlukmanov.modelo.Tratamiento;
import ar.edu.doctorlukmanov.vista.componentes.Dialogos;
import ar.edu.doctorlukmanov.vista.componentes.FormularioUtil;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public final class DialogoTratamiento {

    private DialogoTratamiento() {
    }

    public static boolean mostrar(
            Component padre, Tratamiento tratamiento, Consumer<TratamientoFormularioDto> guardar) {
        JTextField nombre = new JTextField(tratamiento == null ? "" : tratamiento.getNombre(), 24);
        JTextArea descripcion = new JTextArea(
                tratamiento == null || tratamiento.getDescripcion() == null ? "" : tratamiento.getDescripcion(),
                4, 24);
        JTextField precio = new JTextField(
                tratamiento == null || tratamiento.getPrecioReferencia() == null
                        ? "0" : tratamiento.getPrecioReferencia().toPlainString(), 24);
        JPanel formulario = new JPanel(new GridBagLayout());
        FormularioUtil.agregarFila(formulario, 0, "Nombre *", nombre);
        FormularioUtil.agregarFila(formulario, 1, "Descripción", FormularioUtil.areaDesplazable(descripcion));
        FormularioUtil.agregarFila(formulario, 2, "Precio de referencia", precio);

        while (JOptionPane.showConfirmDialog(
                padre,
                formulario,
                tratamiento == null ? "Nuevo tratamiento" : "Editar tratamiento",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                guardar.accept(new TratamientoFormularioDto(
                        tratamiento == null ? null : tratamiento.getIdTratamiento(),
                        nombre.getText(),
                        descripcion.getText(),
                        FormularioUtil.decimalOpcional(precio.getText(), "precio de referencia")));
                return true;
            } catch (ClinicaException ex) {
                Dialogos.error(padre, ex);
            }
        }
        return false;
    }
}
