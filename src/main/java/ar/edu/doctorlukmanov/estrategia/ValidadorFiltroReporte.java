package ar.edu.doctorlukmanov.estrategia;

import ar.edu.doctorlukmanov.dto.FiltroReporteDto;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.util.Validador;

final class ValidadorFiltroReporte {

    private ValidadorFiltroReporte() {
    }

    static FiltroReporteDto validarRango(FiltroReporteDto filtro) {
        Validador.requerido(filtro, "filtros del reporte");
        Validador.requerido(filtro.fechaDesde(), "fecha desde");
        Validador.requerido(filtro.fechaHasta(), "fecha hasta");
        if (filtro.fechaDesde().isAfter(filtro.fechaHasta())) {
            throw new ValidacionException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
        return filtro;
    }

    static FiltroReporteDto validarGato(FiltroReporteDto filtro) {
        Validador.requerido(filtro, "filtros del reporte");
        Validador.identificadorRequerido(filtro.idGato(), "gato");
        return filtro;
    }
}
