package ar.edu.doctorlukmanov.vista.gato;

import ar.edu.doctorlukmanov.dto.GatoFormularioDto;
import ar.edu.doctorlukmanov.excepcion.ClinicaException;
import ar.edu.doctorlukmanov.modelo.Cliente;
import ar.edu.doctorlukmanov.modelo.Gato;
import ar.edu.doctorlukmanov.modelo.SexoGato;
import ar.edu.doctorlukmanov.vista.componentes.Dialogos;
import ar.edu.doctorlukmanov.vista.componentes.FormularioUtil;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public final class DialogoGato {

    private DialogoGato() {
    }

    public static boolean mostrar(
            Component padre,
            Gato gato,
            List<Cliente> clientes,
            Consumer<GatoFormularioDto> guardar) {
        JComboBox<Cliente> cliente = new JComboBox<>(clientes.toArray(Cliente[]::new));
        JTextField nombre = new JTextField(gato == null ? "" : gato.getNombre(), 22);
        JTextField nacimiento = new JTextField(
                gato == null || gato.getFechaNacimiento() == null ? "" : gato.getFechaNacimiento().toString(), 22);
        JComboBox<SexoGato> sexo = new JComboBox<>(SexoGato.values());
        JTextField raza = new JTextField(gato == null || gato.getRaza() == null ? "" : gato.getRaza(), 22);
        JTextField color = new JTextField(gato == null || gato.getColor() == null ? "" : gato.getColor(), 22);
        JTextField peso = new JTextField(
                gato == null || gato.getPesoActual() == null ? "" : gato.getPesoActual().toPlainString(), 22);
        JTextField microchip = new JTextField(
                gato == null || gato.getNumeroMicrochip() == null ? "" : gato.getNumeroMicrochip(), 22);
        JCheckBox esterilizado = new JCheckBox("Sí", gato != null && gato.isEsterilizado());
        JTextArea alergias = new JTextArea(gato == null || gato.getAlergias() == null ? "" : gato.getAlergias(), 3, 22);
        JTextArea observaciones = new JTextArea(
                gato == null || gato.getObservaciones() == null ? "" : gato.getObservaciones(), 3, 22);
        if (gato != null) {
            clientes.stream().filter(item -> item.getIdCliente().equals(gato.getIdCliente()))
                    .findFirst().ifPresent(cliente::setSelectedItem);
            sexo.setSelectedItem(gato.getSexo());
        }

        JPanel formulario = new JPanel(new GridBagLayout());
        FormularioUtil.agregarFila(formulario, 0, "Cliente responsable *", cliente);
        FormularioUtil.agregarFila(formulario, 1, "Nombre *", nombre);
        FormularioUtil.agregarFila(formulario, 2, "Fecha de nacimiento (AAAA-MM-DD)", nacimiento);
        FormularioUtil.agregarFila(formulario, 3, "Sexo *", sexo);
        FormularioUtil.agregarFila(formulario, 4, "Raza", raza);
        FormularioUtil.agregarFila(formulario, 5, "Color", color);
        FormularioUtil.agregarFila(formulario, 6, "Peso actual (kg)", peso);
        FormularioUtil.agregarFila(formulario, 7, "Número de microchip", microchip);
        FormularioUtil.agregarFila(formulario, 8, "Esterilizado", esterilizado);
        FormularioUtil.agregarFila(formulario, 9, "Alergias", FormularioUtil.areaDesplazable(alergias));
        FormularioUtil.agregarFila(formulario, 10, "Observaciones", FormularioUtil.areaDesplazable(observaciones));
        formulario.setPreferredSize(new Dimension(550, 520));

        while (JOptionPane.showConfirmDialog(
                padre,
                formulario,
                gato == null ? "Registrar gato" : "Editar gato",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                Cliente responsable = (Cliente) cliente.getSelectedItem();
                guardar.accept(new GatoFormularioDto(
                        gato == null ? null : gato.getIdGato(),
                        responsable == null ? null : responsable.getIdCliente(),
                        nombre.getText(),
                        FormularioUtil.fechaOpcional(nacimiento.getText(), "fecha de nacimiento"),
                        (SexoGato) sexo.getSelectedItem(),
                        raza.getText(),
                        color.getText(),
                        FormularioUtil.decimalOpcional(peso.getText(), "peso actual"),
                        microchip.getText(),
                        esterilizado.isSelected(),
                        alergias.getText(),
                        observaciones.getText()));
                return true;
            } catch (ClinicaException ex) {
                Dialogos.error(padre, ex.getMessage());
            }
        }
        return false;
    }
}
