package ar.edu.doctorlukmanov.vista.cliente;

import ar.edu.doctorlukmanov.dto.ClienteFormularioDto;
import ar.edu.doctorlukmanov.excepcion.ClinicaException;
import ar.edu.doctorlukmanov.modelo.Cliente;
import ar.edu.doctorlukmanov.vista.componentes.Dialogos;
import ar.edu.doctorlukmanov.vista.componentes.FormularioUtil;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class DialogoCliente {

    private DialogoCliente() {
    }

    public static boolean mostrar(
            Component padre, Cliente cliente, Consumer<ClienteFormularioDto> guardar) {
        JTextField nombre = new JTextField(cliente == null ? "" : cliente.getNombre(), 24);
        JTextField apellido = new JTextField(cliente == null ? "" : cliente.getApellido(), 24);
        JTextField dni = new JTextField(cliente == null ? "" : cliente.getDni(), 24);
        JTextField telefono = new JTextField(cliente == null ? "" : cliente.getTelefono(), 24);
        JTextField correo = new JTextField(
                cliente == null || cliente.getCorreoElectronico() == null
                        ? "" : cliente.getCorreoElectronico(), 24);
        JTextField direccion = new JTextField(
                cliente == null || cliente.getDireccion() == null ? "" : cliente.getDireccion(), 24);
        JPanel formulario = new JPanel(new GridBagLayout());
        FormularioUtil.agregarFila(formulario, 0, "Nombre *", nombre);
        FormularioUtil.agregarFila(formulario, 1, "Apellido *", apellido);
        FormularioUtil.agregarFila(formulario, 2, "DNI *", dni);
        FormularioUtil.agregarFila(formulario, 3, "Teléfono *", telefono);
        FormularioUtil.agregarFila(formulario, 4, "Correo electrónico", correo);
        FormularioUtil.agregarFila(formulario, 5, "Dirección", direccion);

        while (JOptionPane.showConfirmDialog(
                padre,
                formulario,
                cliente == null ? "Nuevo cliente" : "Editar cliente",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            ClienteFormularioDto dto = new ClienteFormularioDto(
                    cliente == null ? null : cliente.getIdCliente(),
                    nombre.getText(),
                    apellido.getText(),
                    dni.getText(),
                    telefono.getText(),
                    correo.getText(),
                    direccion.getText());
            try {
                guardar.accept(dto);
                return true;
            } catch (ClinicaException ex) {
                Dialogos.error(padre, ex.getMessage());
            }
        }
        return false;
    }
}
