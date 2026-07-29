package ar.edu.doctorlukmanov.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class FechasUtil {

    public static final DateTimeFormatter FECHA_VISIBLE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FECHA_HORA_VISIBLE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private FechasUtil() {
    }

    public static LocalDate parsearFecha(String valor) {
        return valor == null ? null : LocalDate.parse(valor);
    }

    public static LocalDateTime parsearFechaHora(String valor) {
        if (valor == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(valor);
        } catch (DateTimeParseException ex) {
            return LocalDateTime.parse(valor, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    public static String aTexto(LocalDate valor) {
        return valor == null ? null : valor.toString();
    }

    public static String aTexto(LocalDateTime valor) {
        return valor == null ? null : valor.withNano(0).toString();
    }
}
