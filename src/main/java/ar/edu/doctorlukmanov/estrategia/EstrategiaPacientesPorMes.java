package ar.edu.doctorlukmanov.estrategia;

import ar.edu.doctorlukmanov.dao.ReporteDao;
import ar.edu.doctorlukmanov.dto.FiltroReporteDto;
import ar.edu.doctorlukmanov.dto.ReportePacientesMesDto;
import java.util.List;

public final class EstrategiaPacientesPorMes implements EstrategiaReporte<ReportePacientesMesDto> {

    private final ReporteDao reporteDao;

    public EstrategiaPacientesPorMes(ReporteDao reporteDao) {
        this.reporteDao = reporteDao;
    }

    @Override
    public TipoReporte getTipo() {
        return TipoReporte.PACIENTES_POR_MES;
    }

    @Override
    public List<ReportePacientesMesDto> generar(FiltroReporteDto filtro) {
        FiltroReporteDto validado = ValidadorFiltroReporte.validarRango(filtro);
        return reporteDao.pacientesPorMes(validado.fechaDesde(), validado.fechaHasta());
    }
}
