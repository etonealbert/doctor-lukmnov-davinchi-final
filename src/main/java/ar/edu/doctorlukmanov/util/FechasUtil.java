package ar.edu.doctorlukmanov.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FechasUtil {

    public static final DateTimeFormatter FECHA_VISIBLE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FECHA_HORA_VISIBLE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private FechasUtil() {
    }

    public static LocalDate parsearFecha(String valor) {
        return valor == null ? null : LocalDate.parse(valor);
    }

    public static LocalDateTime parsearFechaHora(String valor) {
        return valor == null ? null : LocalDateTime.parse(valor);
    }

    public static String aTexto(LocalDate valor) {
        return valor == null ? null : valor.toString();
    }

    public static String aTexto(LocalDateTime valor) {
        return valor == null ? null : valor.withNano(0).toString();
    }
}
