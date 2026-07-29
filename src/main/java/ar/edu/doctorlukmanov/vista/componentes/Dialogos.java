package ar.edu.doctorlukmanov.vista.componentes;

import ar.edu.doctorlukmanov.excepcion.ClinicaException;
import ar.edu.doctorlukmanov.util.RegistroErrores;
import java.awt.Component;
import javax.swing.JOptionPane;

public final class Dialogos {

    private Dialogos() {
    }

    public static void informar(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Doctor Lukmanov", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "No se pudo completar la operación",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void error(Component padre, Throwable error) {
        RegistroErrores.registrar("Error controlado en una operación de la interfaz.", error);
        String mensaje = error instanceof ClinicaException && error.getMessage() != null
                ? error.getMessage()
                : "Ocurrió un error inesperado. Consulte el registro de errores.";
        error(padre, mensaje);
    }

    public static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(
                padre, mensaje, "Confirmar operación", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
