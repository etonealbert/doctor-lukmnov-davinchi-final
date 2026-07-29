package ar.edu.doctorlukmanov.dto;

import java.math.BigDecimal;
import java.util.List;

public record CierreAtencionDto(
        Long idTurno,
        String diagnostico,
        BigDecimal pesoRegistrado,
        BigDecimal temperatura,
        String observacionesClinicas,
        String indicaciones,
        List<DetalleTratamientoDto> tratamientos) {

    public CierreAtencionDto {
        tratamientos = tratamientos == null ? List.of() : List.copyOf(tratamientos);
    }
}
