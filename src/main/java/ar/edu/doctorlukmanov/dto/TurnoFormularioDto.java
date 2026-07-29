package ar.edu.doctorlukmanov.dto;

import java.time.LocalDateTime;

public record TurnoFormularioDto(
        Long idGato,
        Long idVeterinario,
        LocalDateTime fechaHora,
        String motivo,
        String observaciones) {
}
