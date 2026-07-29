package ar.edu.doctorlukmanov.util;

import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public final class Validador {

    private static final Pattern PATRON_CORREO = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private Validador() {
    }

    public static String textoRequerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException("El campo " + campo + " es obligatorio.");
        }
        return valor.trim();
    }

    public static String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public static void correo(String valor) {
        if (valor != null && !valor.isBlank() && !PATRON_CORREO.matcher(valor.trim()).matches()) {
            throw new ValidacionException("El correo electrónico no tiene un formato válido.");
        }
    }

    public static void decimalNoNegativo(BigDecimal valor, String campo) {
        if (valor != null && valor.signum() < 0) {
            throw new ValidacionException("El campo " + campo + " no puede ser negativo.");
        }
    }

    public static void decimalPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new ValidacionException("El campo " + campo + " debe ser mayor que cero.");
        }
    }

    public static void fechaNoFutura(LocalDate valor, String campo) {
        if (valor != null && valor.isAfter(LocalDate.now())) {
            throw new ValidacionException("El campo " + campo + " no puede estar en el futuro.");
        }
    }

    public static void fechaHoraFutura(LocalDateTime valor, String campo) {
        if (valor == null || !valor.isAfter(LocalDateTime.now())) {
            throw new ValidacionException("El campo " + campo + " debe indicar una fecha y hora futura.");
        }
    }

    public static <T> T requerido(T valor, String campo) {
        if (valor == null) {
            throw new ValidacionException("El campo " + campo + " es obligatorio.");
        }
        return valor;
    }

    public static Long identificadorRequerido(Long valor, String campo) {
        if (valor == null || valor <= 0) {
            throw new ValidacionException("Seleccione un valor válido para " + campo + ".");
        }
        return valor;
    }
}
