package ar.edu.doctorlukmanov.dto;

import java.math.BigDecimal;

public record ReporteTratamientoFrecuenteDto(
        Long idTratamiento,
        String nombreTratamiento,
        long cantidadAplicaciones,
        BigDecimal cantidadTotal,
        double porcentaje) {
}
