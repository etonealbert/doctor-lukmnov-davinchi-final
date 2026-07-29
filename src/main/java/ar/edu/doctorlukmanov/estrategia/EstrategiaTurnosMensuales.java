package ar.edu.doctorlukmanov.estrategia;

import ar.edu.doctorlukmanov.dao.ReporteDao;
import ar.edu.doctorlukmanov.dto.FiltroReporteDto;
import ar.edu.doctorlukmanov.dto.ReporteTurnosMensualesDto;
import java.util.List;

public final class EstrategiaTurnosMensuales implements EstrategiaReporte<ReporteTurnosMensualesDto> {

    private final ReporteDao reporteDao;

    public EstrategiaTurnosMensuales(ReporteDao reporteDao) {
        this.reporteDao = reporteDao;
    }

    @Override
    public TipoReporte getTipo() {
        return TipoReporte.TURNOS_POR_MES;
    }

    @Override
    public List<ReporteTurnosMensualesDto> generar(FiltroReporteDto filtro) {
        FiltroReporteDto validado = ValidadorFiltroReporte.validarRango(filtro);
        return reporteDao.turnosPorMes(validado.fechaDesde(), validado.fechaHasta());
    }
}
