package ar.edu.doctorlukmanov.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class RegistroErrores {

    private static final Logger LOGGER = Logger.getLogger("ar.edu.doctorlukmanov");
    private static boolean configurado;

    private RegistroErrores() {
    }

    public static synchronized void configurar() {
        if (configurado) {
            return;
        }
        try {
            Path directorio = Path.of("logs");
            Files.createDirectories(directorio);
            FileHandler archivo = new FileHandler(
                    directorio.resolve("doctor-lukmanov.log").toString(), true);
            archivo.setFormatter(new SimpleFormatter());
            LOGGER.setUseParentHandlers(false);
            LOGGER.addHandler(archivo);
            LOGGER.setLevel(Level.ALL);
            configurado = true;
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.WARNING, "No fue posible configurar el archivo de registro.", ex);
        }
    }

    public static void registrar(String operacion, Throwable error) {
        configurar();
        LOGGER.log(Level.SEVERE, operacion, error);
    }
}
