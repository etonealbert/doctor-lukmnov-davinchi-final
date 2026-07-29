package ar.edu.doctorlukmanov.vista.componentes;

import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public final class FormularioUtil {

    private FormularioUtil() {
    }

    public static void agregarFila(JPanel panel, int fila, String texto, Component campo) {
        GridBagConstraints etiqueta = restricciones(0, fila);
        etiqueta.anchor = GridBagConstraints.LINE_END;
        JLabel label = new JLabel(texto);
        label.setLabelFor(campo);
        panel.add(label, etiqueta);

        GridBagConstraints control = restricciones(1, fila);
        control.weightx = 1;
        control.fill = GridBagConstraints.HORIZONTAL;
        panel.add(campo, control);
    }

    public static JScrollPane areaDesplazable(JTextArea area) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return new JScrollPane(area);
    }

    public static BigDecimal decimalOpcional(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(valor.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new ValidacionException("El campo " + campo + " debe ser un número válido.", ex);
        }
    }

    public static Integer enteroOpcional(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException ex) {
            throw new ValidacionException("El campo " + campo + " debe ser un número entero.", ex);
        }
    }

    public static LocalDate fechaOpcional(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException ex) {
            throw new ValidacionException("El campo " + campo + " debe usar el formato AAAA-MM-DD.", ex);
        }
    }

    private static GridBagConstraints restricciones(int columna, int fila) {
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.gridx = columna;
        restricciones.gridy = fila;
        restricciones.insets = new Insets(5, 6, 5, 6);
        restricciones.anchor = GridBagConstraints.LINE_START;
        return restricciones;
    }
}
