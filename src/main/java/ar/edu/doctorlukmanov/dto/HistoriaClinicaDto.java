package ar.edu.doctorlukmanov.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoriaClinicaDto(
        Long idGato,
        String nombreGato,
        String nombreCliente,
        LocalDateTime fechaHora,
        String nombreVeterinario,
        String motivo,
        String diagnostico,
        BigDecimal pesoRegistrado,
        BigDecimal temperatura,
        String tratamientos,
        String indicaciones) {
}
