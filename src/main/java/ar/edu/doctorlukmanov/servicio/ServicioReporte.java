package ar.edu.doctorlukmanov.servicio;

import ar.edu.doctorlukmanov.dto.FiltroReporteDto;
import ar.edu.doctorlukmanov.estrategia.EstrategiaReporte;
import ar.edu.doctorlukmanov.estrategia.TipoReporte;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.util.ColeccionesUtil;
import ar.edu.doctorlukmanov.util.Validador;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class ServicioReporte {

    private final Map<TipoReporte, EstrategiaReporte<?>> estrategias;

    public ServicioReporte(Collection<? extends EstrategiaReporte<?>> estrategias) {
        Validador.requerido(estrategias, "estrategias de reporte");
        this.estrategias = ColeccionesUtil.indexarPor(
                new ArrayList<EstrategiaReporte<?>>(estrategias), EstrategiaReporte::getTipo);
    }

    @SuppressWarnings("unchecked")
    public <R> List<R> generar(TipoReporte tipo, FiltroReporteDto filtro) {
        Validador.requerido(tipo, "tipo de reporte");
        EstrategiaReporte<?> estrategia = estrategias.get(tipo);
        if (estrategia == null) {
            throw new ValidacionException("El tipo de reporte no está configurado.");
        }
        return (List<R>) estrategia.generar(filtro);
    }

    public List<TipoReporte> listarTipos() {
        return List.copyOf(estrategias.keySet());
    }
}
