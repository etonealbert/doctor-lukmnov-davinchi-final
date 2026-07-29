package ar.edu.doctorlukmanov.dao;

import ar.edu.doctorlukmanov.dto.HistoriaClinicaDto;
import ar.edu.doctorlukmanov.dto.ReportePacientesMesDto;
import ar.edu.doctorlukmanov.dto.ReporteProductividadVeterinarioDto;
import ar.edu.doctorlukmanov.dto.ReporteTratamientoFrecuenteDto;
import ar.edu.doctorlukmanov.dto.ReporteTurnosMensualesDto;
import java.time.LocalDate;
import java.util.List;

public interface ReporteDao {

    List<ReportePacientesMesDto> pacientesPorMes(LocalDate desde, LocalDate hasta);

    List<ReporteTurnosMensualesDto> turnosPorMes(LocalDate desde, LocalDate hasta);

    List<ReporteTratamientoFrecuenteDto> tratamientosFrecuentes(LocalDate desde, LocalDate hasta);

    List<ReporteProductividadVeterinarioDto> productividadVeterinarios(
            LocalDate desde, LocalDate hasta, Long idVeterinario);

    List<HistoriaClinicaDto> historiaClinica(Long idGato);
}
