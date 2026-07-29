package ar.edu.doctorlukmanov.estrategia;

import ar.edu.doctorlukmanov.dao.ReporteDao;
import ar.edu.doctorlukmanov.dto.FiltroReporteDto;
import ar.edu.doctorlukmanov.dto.ReporteTratamientoFrecuenteDto;
import java.util.List;

public final class EstrategiaTratamientosFrecuentes
        implements EstrategiaReporte<ReporteTratamientoFrecuenteDto> {

    private final ReporteDao reporteDao;

    public EstrategiaTratamientosFrecuentes(ReporteDao reporteDao) {
        this.reporteDao = reporteDao;
    }

    @Override
    public TipoReporte getTipo() {
        return TipoReporte.TRATAMIENTOS_FRECUENTES;
    }

    @Override
    public List<ReporteTratamientoFrecuenteDto> generar(FiltroReporteDto filtro) {
        FiltroReporteDto validado = ValidadorFiltroReporte.validarRango(filtro);
        return reporteDao.tratamientosFrecuentes(validado.fechaDesde(), validado.fechaHasta());
    }
}
