package ar.edu.doctorlukmanov.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ValidadorTest {

    @Test
    void validaTextoCorreoNumerosYFechas() {
        assertThrows(ValidacionException.class, () -> Validador.textoRequerido("  ", "Nombre"));
        assertThrows(ValidacionException.class, () -> Validador.correo("correo-invalido"));
        assertThrows(ValidacionException.class,
                () -> Validador.decimalNoNegativo(new BigDecimal("-1"), "Peso"));
        assertThrows(ValidacionException.class,
                () -> Validador.fechaNoFutura(LocalDate.now().plusDays(1), "Nacimiento"));
        assertThrows(ValidacionException.class,
                () -> Validador.fechaHoraFutura(LocalDateTime.now().minusMinutes(1), "Turno"));
        assertDoesNotThrow(() -> Validador.correo("persona@example.com"));
        assertDoesNotThrow(() -> Validador.correo(null));
    }
}
