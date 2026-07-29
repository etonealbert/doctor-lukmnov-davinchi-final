package ar.edu.doctorlukmanov.vista.turno;

import ar.edu.doctorlukmanov.excepcion.ClinicaException;
import ar.edu.doctorlukmanov.vista.componentes.Dialogos;
import java.awt.Component;
import java.awt.BorderLayout;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public final class DialogoCancelarTurno {

    private DialogoCancelarTurno() {
    }

    public static boolean mostrar(Component padre, String resumen, Consumer<String> cancelar) {
        JTextArea motivo = new JTextArea(5, 35);
        motivo.setLineWrap(true);
        motivo.setWrapStyleWord(true);
        JPanel contenido = new JPanel(new BorderLayout(8, 8));
        contenido.add(new JLabel("<html>" + resumen + "<br><b>Motivo de cancelación *</b></html>"),
                BorderLayout.NORTH);
        contenido.add(new JScrollPane(motivo), BorderLayout.CENTER);

        while (JOptionPane.showConfirmDialog(
                padre,
                contenido,
                "Cancelar turno",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
            if (!Dialogos.confirmar(padre,
                    "¿Confirma la cancelación del turno seleccionado? Esta acción cerrará el turno.")) {
                continue;
            }
            try {
                cancelar.accept(motivo.getText());
                return true;
            } catch (ClinicaException ex) {
                Dialogos.error(padre, ex);
            }
        }
        return false;
    }
}
