package ar.edu.doctorlukmanov.util;

import ar.edu.doctorlukmanov.excepcion.PersistenciaException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class InicializadorBaseDatos {

    private static final String RECURSO_ESQUEMA = "/sql/crear_base_datos.sql";

    private final ConexionBaseDatos conexionBaseDatos;

    public InicializadorBaseDatos(ConexionBaseDatos conexionBaseDatos) {
        this.conexionBaseDatos = conexionBaseDatos;
    }

    public void inicializar() {
        try (Connection conexion = conexionBaseDatos.obtenerConexion()) {
            if (existeEsquema(conexion)) {
                return;
            }
            ejecutarScript(conexion, leerScript());
        } catch (SQLException ex) {
            throw new PersistenciaException("No fue posible inicializar la base de datos.", ex);
        }
    }

    private boolean existeEsquema(Connection conexion) throws SQLException {
        String sql = "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'version_esquema'";
        try (Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery(sql)) {
            return resultado.next();
        }
    }

    private String leerScript() {
        try (InputStream entrada = InicializadorBaseDatos.class.getResourceAsStream(RECURSO_ESQUEMA)) {
            if (entrada == null) {
                throw new PersistenciaException("No se encontró el script de creación de la base de datos.");
            }
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new PersistenciaException("No fue posible leer el script de creación.", ex);
        }
    }

    private void ejecutarScript(Connection conexion, String script) throws SQLException {
        boolean autoCommitOriginal = conexion.getAutoCommit();
        conexion.setAutoCommit(false);
        try (Statement sentencia = conexion.createStatement()) {
            for (String bloque : quitarComentarios(script).split(";")) {
                String sql = bloque.trim();
                if (!sql.isEmpty() && !esControlTransaccional(sql) && !sql.startsWith("PRAGMA")) {
                    sentencia.execute(sql);
                }
            }
            conexion.commit();
        } catch (SQLException ex) {
            conexion.rollback();
            throw ex;
        } finally {
            conexion.setAutoCommit(autoCommitOriginal);
        }
    }

    private String quitarComentarios(String script) {
        return Arrays.stream(script.split("\\R"))
                .filter(linea -> !linea.trim().startsWith("--"))
                .collect(Collectors.joining("\n"));
    }

    private boolean esControlTransaccional(String sql) {
        String normalizado = sql.replaceAll("\\s+", " ").trim().toUpperCase();
        return normalizado.equals("BEGIN TRANSACTION") || normalizado.equals("COMMIT");
    }
}
