package ar.edu.doctorlukmanov.controlador;

import ar.edu.doctorlukmanov.dto.FiltroReporteDto;
import ar.edu.doctorlukmanov.estrategia.TipoReporte;
import ar.edu.doctorlukmanov.servicio.ServicioReporte;
import java.util.List;

public final class ControladorReporte {

    private final ServicioReporte servicio;

    public ControladorReporte(ServicioReporte servicio) {
        this.servicio = servicio;
    }

    public <R> List<R> generar(TipoReporte tipo, FiltroReporteDto filtro) {
        return servicio.generar(tipo, filtro);
    }

    public List<TipoReporte> listarTipos() {
        return servicio.listarTipos();
    }
}
