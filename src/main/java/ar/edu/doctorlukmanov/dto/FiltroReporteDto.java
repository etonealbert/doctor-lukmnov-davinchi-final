package ar.edu.doctorlukmanov.dto;

import java.time.LocalDate;

public record FiltroReporteDto(
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        Long idVeterinario,
        Long idGato) {
}
