package ar.edu.doctorlukmanov.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.doctorlukmanov.util.ConexionBaseDatos;
import ar.edu.doctorlukmanov.util.InicializadorBaseDatos;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServicioRespaldoTest {

    @TempDir
    Path directorioTemporal;

    @Test
    void creaUnaCopiaLegibleConLosDatosExistentes() throws Exception {
        Path original = directorioTemporal.resolve("datos/clinica.db");
        Files.createDirectories(original.getParent());
        ConexionBaseDatos baseDatos = ConexionBaseDatos.desdeUrl("jdbc:sqlite:" + original);
        new InicializadorBaseDatos(baseDatos).inicializar();
        try (Connection conexion = baseDatos.obtenerConexion();
             Statement sentencia = conexion.createStatement()) {
            sentencia.executeUpdate("INSERT INTO clientes "
                    + "(nombre, apellido, dni, telefono) VALUES ('Ana', 'Pérez', '123', '555')");
        }

        Path respaldo = new ServicioRespaldo(baseDatos, directorioTemporal.resolve("respaldos"))
                .crearRespaldo();

        assertTrue(Files.exists(respaldo));
        assertNotEquals(original, respaldo);
        try (Connection conexion = ConexionBaseDatos.desdeUrl("jdbc:sqlite:" + respaldo).obtenerConexion();
             Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery("SELECT COUNT(*) FROM clientes")) {
            assertEquals(1, resultado.getInt(1));
        }
    }

    @Test
    void incluyeCambiosConfirmadosEnWalAunqueExistaUnLectorActivo() throws Exception {
        Path original = directorioTemporal.resolve("wal/clinica.db");
        Files.createDirectories(original.getParent());
        ConexionBaseDatos baseDatos = ConexionBaseDatos.desdeUrl("jdbc:sqlite:" + original);
        new InicializadorBaseDatos(baseDatos).inicializar();

        Path respaldo;
        try (Connection escritor = baseDatos.obtenerConexion();
             Statement sentenciaEscritor = escritor.createStatement()) {
            sentenciaEscritor.execute("PRAGMA journal_mode = WAL");
            sentenciaEscritor.execute("PRAGMA wal_autocheckpoint = 0");
            sentenciaEscritor.executeUpdate("INSERT INTO clientes "
                    + "(nombre, apellido, dni, telefono) VALUES ('Ana', 'Pérez', '1', '555')");
            try (Connection lector = baseDatos.obtenerConexion()) {
                lector.setAutoCommit(false);
                try (Statement sentenciaLector = lector.createStatement();
                     ResultSet ignorado = sentenciaLector.executeQuery("SELECT COUNT(*) FROM clientes")) {
                    assertTrue(ignorado.next());
                    sentenciaEscritor.executeUpdate("INSERT INTO clientes "
                            + "(nombre, apellido, dni, telefono) VALUES ('Luis', 'Ruiz', '2', '777')");
                    respaldo = new ServicioRespaldo(
                            baseDatos, directorioTemporal.resolve("respaldos-wal")).crearRespaldo();
                }
                lector.rollback();
            }
        }

        try (Connection conexion = ConexionBaseDatos.desdeUrl("jdbc:sqlite:" + respaldo).obtenerConexion();
             Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery("SELECT COUNT(*) FROM clientes")) {
            assertEquals(2, resultado.getInt(1));
        }
    }
}
