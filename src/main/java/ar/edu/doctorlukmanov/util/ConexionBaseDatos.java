package ar.edu.doctorlukmanov.util;

import ar.edu.doctorlukmanov.configuracion.ConfiguracionAplicacion;
import ar.edu.doctorlukmanov.excepcion.PersistenciaException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public final class ConexionBaseDatos {

    private final String url;
    private final boolean clavesForaneas;
    private final int tiempoEsperaMilisegundos;

    private ConexionBaseDatos(String url, boolean clavesForaneas, int tiempoEsperaMilisegundos) {
        this.url = Objects.requireNonNull(url, "La URL de la base de datos es obligatoria.");
        this.clavesForaneas = clavesForaneas;
        this.tiempoEsperaMilisegundos = tiempoEsperaMilisegundos;
    }

    private static final class ContenedorInstancia {
        private static final ConfiguracionAplicacion CONFIGURACION = ConfiguracionAplicacion.cargar();
        private static final ConexionBaseDatos INSTANCIA = new ConexionBaseDatos(
                CONFIGURACION.getUrlBaseDatos(),
                CONFIGURACION.isClavesForaneas(),
                CONFIGURACION.getTiempoEsperaMilisegundos());
    }

    public static ConexionBaseDatos getInstancia() {
        return ContenedorInstancia.INSTANCIA;
    }

    public static ConexionBaseDatos desdeUrl(String url) {
        return new ConexionBaseDatos(url, true, 5000);
    }

    public Connection obtenerConexion() {
        try {
            Connection conexion = DriverManager.getConnection(url);
            try (Statement sentencia = conexion.createStatement()) {
                sentencia.execute("PRAGMA foreign_keys = " + (clavesForaneas ? "ON" : "OFF"));
                sentencia.execute("PRAGMA busy_timeout = " + tiempoEsperaMilisegundos);
            }
            return conexion;
        } catch (SQLException ex) {
            throw new PersistenciaException("No fue posible acceder a la base de datos.", ex);
        }
    }

    public <R> R ejecutarEnTransaccion(TrabajoTransaccional<R> trabajo) {
        Objects.requireNonNull(trabajo, "El trabajo transaccional es obligatorio.");
        try (Connection conexion = obtenerConexion()) {
            boolean autoCommitOriginal = conexion.getAutoCommit();
            conexion.setAutoCommit(false);
            try {
                R resultado = trabajo.ejecutar(conexion);
                conexion.commit();
                return resultado;
            } catch (Exception ex) {
                revertir(conexion, ex);
                if (ex instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new PersistenciaException("Error al ejecutar la transacción.", ex);
            } finally {
                conexion.setAutoCommit(autoCommitOriginal);
            }
        } catch (SQLException ex) {
            throw new PersistenciaException("Error al ejecutar la transacción.", ex);
        }
    }

    private static void revertir(Connection conexion, Exception causa) {
        try {
            conexion.rollback();
        } catch (SQLException ex) {
            causa.addSuppressed(ex);
        }
    }

    public String getUrl() {
        return url;
    }
}
