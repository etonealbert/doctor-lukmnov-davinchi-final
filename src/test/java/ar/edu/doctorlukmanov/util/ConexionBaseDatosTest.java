package ar.edu.doctorlukmanov.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConexionBaseDatosTest {

    @TempDir
    Path directorioTemporal;

    @Test
    void inicializaElEsquemaConClavesForaneasYSinPerderDatos() throws Exception {
        String url = "jdbc:sqlite:" + directorioTemporal.resolve("clinica.db");
        ConexionBaseDatos conexionBaseDatos = ConexionBaseDatos.desdeUrl(url);
        InicializadorBaseDatos inicializador = new InicializadorBaseDatos(conexionBaseDatos);

        inicializador.inicializar();
        try (Connection conexion = conexionBaseDatos.obtenerConexion();
             Statement sentencia = conexion.createStatement()) {
            try (ResultSet resultado = sentencia.executeQuery("PRAGMA foreign_keys")) {
                assertEquals(1, resultado.getInt(1));
            }
            sentencia.executeUpdate("INSERT INTO clientes "
                    + "(nombre, apellido, dni, telefono) VALUES ('Ana', 'Perez', '123', '555')");
        }

        inicializador.inicializar();
        try (Connection conexion = conexionBaseDatos.obtenerConexion();
             Statement sentencia = conexion.createStatement()) {
            try (ResultSet resultado = sentencia.executeQuery("SELECT COUNT(*) FROM clientes")) {
                assertTrue(resultado.next());
                assertEquals(1, resultado.getInt(1));
            }
            try (ResultSet resultado = sentencia.executeQuery("SELECT COUNT(*) FROM tratamientos")) {
                assertTrue(resultado.next());
                assertEquals(10, resultado.getInt(1));
            }
        }
    }
}
