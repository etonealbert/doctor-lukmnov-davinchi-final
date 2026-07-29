package ar.edu.doctorlukmanov.dto;

import java.math.BigDecimal;

public record DetalleTratamientoDto(
        Long idTratamiento,
        String dosis,
        String frecuencia,
        Integer duracionDias,
        String observaciones,
        BigDecimal cantidad,
        BigDecimal precioAplicado) {
}
