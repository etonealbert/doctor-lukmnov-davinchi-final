package ar.edu.doctorlukmanov.dto;

import ar.edu.doctorlukmanov.modelo.SexoGato;
import java.math.BigDecimal;
import java.time.LocalDate;

public record GatoFormularioDto(
        Long idGato,
        Long idCliente,
        String nombre,
        LocalDate fechaNacimiento,
        SexoGato sexo,
        String raza,
        String color,
        BigDecimal pesoActual,
        String numeroMicrochip,
        boolean esterilizado,
        String alergias,
        String observaciones) {
}
