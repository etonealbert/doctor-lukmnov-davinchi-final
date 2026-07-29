package ar.edu.doctorlukmanov.estrategia;

import ar.edu.doctorlukmanov.dao.ReporteDao;
import ar.edu.doctorlukmanov.dto.FiltroReporteDto;
import ar.edu.doctorlukmanov.dto.ReporteProductividadVeterinarioDto;
import java.util.List;

public final class EstrategiaProductividadVeterinario
        implements EstrategiaReporte<ReporteProductividadVeterinarioDto> {

    private final ReporteDao reporteDao;

    public EstrategiaProductividadVeterinario(ReporteDao reporteDao) {
        this.reporteDao = reporteDao;
    }

    @Override
    public TipoReporte getTipo() {
        return TipoReporte.PRODUCTIVIDAD_VETERINARIO;
    }

    @Override
    public List<ReporteProductividadVeterinarioDto> generar(FiltroReporteDto filtro) {
        FiltroReporteDto validado = ValidadorFiltroReporte.validarRango(filtro);
        return reporteDao.productividadVeterinarios(
                validado.fechaDesde(), validado.fechaHasta(), validado.idVeterinario());
    }
}
