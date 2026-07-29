package ar.edu.doctorlukmanov.servicio;

import ar.edu.doctorlukmanov.excepcion.PersistenciaException;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.util.ConexionBaseDatos;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ServicioRespaldo {

    private static final String PREFIJO_SQLITE = "jdbc:sqlite:";
    private static final DateTimeFormatter MARCA_TIEMPO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private final ConexionBaseDatos baseDatos;
    private final Path directorioRespaldos;

    public ServicioRespaldo(ConexionBaseDatos baseDatos) {
        this(baseDatos, Path.of("backups"));
    }

    public ServicioRespaldo(ConexionBaseDatos baseDatos, Path directorioRespaldos) {
        this.baseDatos = baseDatos;
        this.directorioRespaldos = directorioRespaldos;
    }

    public Path crearRespaldo() {
        Path origen = rutaBaseDatos();
        if (!Files.isRegularFile(origen)) {
            throw new PersistenciaException("No se encontró el archivo de base de datos para respaldar.");
        }
        try {
            Files.createDirectories(directorioRespaldos);
            Path destino = directorioRespaldos.resolve(
                    "doctor_lukmanov_" + MARCA_TIEMPO.format(LocalDateTime.now()) + ".db");
            crearCopiaConsistente(destino.toAbsolutePath().normalize());
            return destino;
        } catch (IOException ex) {
            throw new PersistenciaException("No fue posible crear el respaldo de la base de datos.", ex);
        }
    }

    private Path rutaBaseDatos() {
        String url = baseDatos.getUrl();
        if (!url.startsWith(PREFIJO_SQLITE)) {
            throw new ValidacionException("La conexión configurada no corresponde a un archivo SQLite.");
        }
        String ruta = url.substring(PREFIJO_SQLITE.length());
        if (ruta.isBlank() || ruta.equals(":memory:") || ruta.startsWith("file:")) {
            throw new ValidacionException("La base de datos en memoria no puede respaldarse como archivo.");
        }
        return Path.of(ruta).toAbsolutePath().normalize();
    }

    private void crearCopiaConsistente(Path destino) {
        String rutaEscapada = destino.toString().replace("'", "''");
        try (Connection conexion = baseDatos.obtenerConexion();
             Statement sentencia = conexion.createStatement()) {
            sentencia.execute("VACUUM INTO '" + rutaEscapada + "'");
        } catch (RuntimeException | SQLException ex) {
            try {
                Files.deleteIfExists(destino);
            } catch (IOException errorLimpieza) {
                ex.addSuppressed(errorLimpieza);
            }
            throw new PersistenciaException("No fue posible crear el respaldo de la base de datos.", ex);
        }
    }
}
