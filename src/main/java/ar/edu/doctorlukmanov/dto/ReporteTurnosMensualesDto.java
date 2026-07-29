package ar.edu.doctorlukmanov.dto;

import java.time.YearMonth;

public record ReporteTurnosMensualesDto(
        YearMonth mes,
        long programados,
        long confirmados,
        long completados,
        long cancelados) {

    public long total() {
        return programados + confirmados + completados + cancelados;
    }

    public double porcentajeFinalizacion() {
        return total() == 0 ? 0 : completados * 100.0 / total();
    }
}
