package ar.edu.doctorlukmanov.dto;

import java.math.BigDecimal;

public record TratamientoFormularioDto(
        Long idTratamiento,
        String nombre,
        String descripcion,
        BigDecimal precioReferencia) {
}
