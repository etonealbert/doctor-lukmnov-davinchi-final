package ar.edu.doctorlukmanov.controlador;

import ar.edu.doctorlukmanov.servicio.ServicioRespaldo;
import java.nio.file.Path;

public final class ControladorSistema {

    private final ServicioRespaldo servicioRespaldo;

    public ControladorSistema(ServicioRespaldo servicioRespaldo) {
        this.servicioRespaldo = servicioRespaldo;
    }

    public Path respaldarBaseDatos() {
        return servicioRespaldo.crearRespaldo();
    }
}
