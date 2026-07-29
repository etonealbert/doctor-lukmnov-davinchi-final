package ar.edu.doctorlukmanov;

import ar.edu.doctorlukmanov.configuracion.FabricaDependencias;
import ar.edu.doctorlukmanov.util.ConexionBaseDatos;
import ar.edu.doctorlukmanov.util.InicializadorBaseDatos;
import ar.edu.doctorlukmanov.util.ManejadorEventosSwing;
import ar.edu.doctorlukmanov.util.RegistroErrores;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class AplicacionClinica {

    private AplicacionClinica() {
    }

    public static void main(String[] argumentos) {
        RegistroErrores.configurar();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            ConexionBaseDatos baseDatos = ConexionBaseDatos.getInstancia();
            prepararDirectorioBaseDatos(baseDatos.getUrl());
            new InicializadorBaseDatos(baseDatos).inicializar();
            try (Connection ignorada = baseDatos.obtenerConexion()) {
                // Opening a configured connection here fails fast before the UI is displayed.
            }
            ManejadorEventosSwing.instalar();
            FabricaDependencias fabrica = new FabricaDependencias(baseDatos);
            SwingUtilities.invokeLater(() -> fabrica.crearVentanaPrincipal().setVisible(true));
        } catch (Exception ex) {
            RegistroErrores.registrar("Error fatal durante el inicio de la aplicación.", ex);
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(
                        null,
                        "No fue posible iniciar la aplicación. Consulte logs/doctor-lukmanov.log.",
                        "Clínica Veterinaria Doctor Lukmanov",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void prepararDirectorioBaseDatos(String url) throws IOException {
        String prefijo = "jdbc:sqlite:";
        if (!url.startsWith(prefijo)) {
            return;
        }
        String ruta = url.substring(prefijo.length());
        if (ruta.isBlank() || ruta.equals(":memory:") || ruta.startsWith("file:")) {
            return;
        }
        Path padre = Path.of(ruta).toAbsolutePath().normalize().getParent();
        if (padre != null) {
            Files.createDirectories(padre);
        }
    }
}
