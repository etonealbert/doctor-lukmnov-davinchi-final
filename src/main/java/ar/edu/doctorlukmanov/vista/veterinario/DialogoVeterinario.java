package ar.edu.doctorlukmanov.vista.veterinario;

import ar.edu.doctorlukmanov.dto.VeterinarioFormularioDto;
import ar.edu.doctorlukmanov.excepcion.ClinicaException;
import ar.edu.doctorlukmanov.modelo.Veterinario;
import ar.edu.doctorlukmanov.vista.componentes.Dialogos;
import ar.edu.doctorlukmanov.vista.componentes.FormularioUtil;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class DialogoVeterinario {

    private DialogoVeterinario() {
    }

    public static boolean mostrar(
            Component padre, Veterinario veterinario, Consumer<VeterinarioFormularioDto> guardar) {
        JTextField nombre = new JTextField(veterinario == null ? "" : veterinario.getNombre(), 24);
        JTextField apellido = new JTextField(veterinario == null ? "" : veterinario.getApellido(), 24);
        JTextField matricula = new JTextField(
                veterinario == null ? "" : veterinario.getMatricula(), 24);
        JTextField telefono = new JTextField(
                veterinario == null || veterinario.getTelefono() == null ? "" : veterinario.getTelefono(), 24);
        JTextField correo = new JTextField(
                veterinario == null || veterinario.getCorreoElectronico() == null
                        ? "" : veterinario.getCorreoElectronico(), 24);
        JTextField especialidad = new JTextField(
                veterinario == null ? "Medicina felina" : veterinario.getEspecialidad(), 24);
        JPanel formulario = new JPanel(new GridBagLayout());
        FormularioUtil.agregarFila(formulario, 0, "Nombre *", nombre);
        FormularioUtil.agregarFila(formulario, 1, "Apellido *", apellido);
        FormularioUtil.agregarFila(formulario, 2, "Matrícula *", matricula);
        FormularioUtil.agregarFila(formulario, 3, "Teléfono", telefono);
        FormularioUtil.agregarFila(formulario, 4, "Correo electrónico", correo);
        FormularioUtil.agregarFila(formulario, 5, "Especialidad *", especialidad);

        while (JOptionPane.showConfirmDialog(
                padre,
                formulario,
                veterinario == null ? "Nuevo veterinario" : "Editar veterinario",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                guardar.accept(new VeterinarioFormularioDto(
                        veterinario == null ? null : veterinario.getIdVeterinario(),
                        nombre.getText(), apellido.getText(), matricula.getText(), telefono.getText(),
                        correo.getText(), especialidad.getText()));
                return true;
            } catch (ClinicaException ex) {
                Dialogos.error(padre, ex);
            }
        }
        return false;
    }
}
