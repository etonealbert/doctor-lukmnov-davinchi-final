package ar.edu.doctorlukmanov.configuracion;

import ar.edu.doctorlukmanov.excepcion.PersistenciaException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfiguracionAplicacion {

    private static final String RECURSO = "/configuracion.properties";
    private static final String URL_PREDETERMINADA = "jdbc:sqlite:data/doctor_lukmanov.db";

    private final String urlBaseDatos;
    private final boolean clavesForaneas;
    private final int tiempoEsperaMilisegundos;
    private final String nombreAplicacion;

    private ConfiguracionAplicacion(Properties propiedades) {
        urlBaseDatos = propiedadDelSistemaOArchivo(
                "doctorlukmanov.baseDatosUrl", propiedades, "base.datos.url", URL_PREDETERMINADA);
        clavesForaneas = Boolean.parseBoolean(propiedades.getProperty("base.datos.foreign_keys", "true"));
        tiempoEsperaMilisegundos = Integer.parseInt(
                propiedades.getProperty("base.datos.busy_timeout_ms", "5000"));
        nombreAplicacion = propiedades.getProperty(
                "aplicacion.nombre", "Clínica Veterinaria Doctor Lukmanov");
    }

    public static ConfiguracionAplicacion cargar() {
        Properties propiedades = new Properties();
        try (InputStream entrada = ConfiguracionAplicacion.class.getResourceAsStream(RECURSO)) {
            if (entrada != null) {
                propiedades.load(entrada);
            }
            return new ConfiguracionAplicacion(propiedades);
        } catch (IOException | NumberFormatException ex) {
            throw new PersistenciaException("No fue posible cargar la configuración de la aplicación.", ex);
        }
    }

    private static String propiedadDelSistemaOArchivo(
            String claveSistema, Properties propiedades, String claveArchivo, String valorPredeterminado) {
        return System.getProperty(claveSistema, propiedades.getProperty(claveArchivo, valorPredeterminado));
    }

    public String getUrlBaseDatos() {
        return urlBaseDatos;
    }

    public boolean isClavesForaneas() {
        return clavesForaneas;
    }

    public int getTiempoEsperaMilisegundos() {
        return tiempoEsperaMilisegundos;
    }

    public String getNombreAplicacion() {
        return nombreAplicacion;
    }
}
