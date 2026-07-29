package ar.edu.doctorlukmanov.util;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import javax.swing.JOptionPane;

public final class ManejadorEventosSwing extends EventQueue {

    private ManejadorEventosSwing() {
    }

    public static void instalar() {
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new ManejadorEventosSwing());
    }

    @Override
    protected void dispatchEvent(AWTEvent evento) {
        try {
            super.dispatchEvent(evento);
        } catch (VirtualMachineError errorFatal) {
            throw errorFatal;
        } catch (Throwable error) {
            RegistroErrores.registrar("Error inesperado al procesar un evento de la interfaz.", error);
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Ocurrió un error inesperado. Consulte el registro de errores.",
                        "Doctor Lukmanov",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
