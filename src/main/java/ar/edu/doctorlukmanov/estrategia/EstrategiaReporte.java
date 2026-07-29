package ar.edu.doctorlukmanov.estrategia;

import ar.edu.doctorlukmanov.dto.FiltroReporteDto;
import java.util.List;

public interface EstrategiaReporte<R> {

    TipoReporte getTipo();

    List<R> generar(FiltroReporteDto filtro);
}
