package ar.edu.doctorlukmanov.dto;

public record ReporteProductividadVeterinarioDto(
        Long idVeterinario,
        String nombreVeterinario,
        long totalTurnos,
        long turnosCompletados,
        long turnosCancelados,
        double tasaFinalizacion) {
}
