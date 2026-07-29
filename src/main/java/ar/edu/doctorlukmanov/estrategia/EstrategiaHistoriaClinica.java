package ar.edu.doctorlukmanov.estrategia;

import ar.edu.doctorlukmanov.dao.ReporteDao;
import ar.edu.doctorlukmanov.dto.FiltroReporteDto;
import ar.edu.doctorlukmanov.dto.HistoriaClinicaDto;
import java.util.List;

public final class EstrategiaHistoriaClinica implements EstrategiaReporte<HistoriaClinicaDto> {

    private final ReporteDao reporteDao;

    public EstrategiaHistoriaClinica(ReporteDao reporteDao) {
        this.reporteDao = reporteDao;
    }

    @Override
    public TipoReporte getTipo() {
        return TipoReporte.HISTORIA_CLINICA;
    }

    @Override
    public List<HistoriaClinicaDto> generar(FiltroReporteDto filtro) {
        FiltroReporteDto validado = ValidadorFiltroReporte.validarGato(filtro);
        return reporteDao.historiaClinica(validado.idGato());
    }
}
