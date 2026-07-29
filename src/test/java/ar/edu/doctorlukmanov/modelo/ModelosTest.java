package ar.edu.doctorlukmanov.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ModelosTest {

    @Test
    void validaClienteYExponePolimorficamenteSuTipo() {
        Persona persona = new Cliente(
                1L, "  Ana ", " Pérez ", "555", "ana@example.com", true,
                "12345678", "Calle 1", LocalDateTime.now());

        persona.validar();

        assertEquals("Cliente", persona.getTipoPersona());
        assertEquals("Ana Pérez", persona.getNombreCompleto());
    }

    @Test
    void rechazaUnGatoConNacimientoFuturoOPesoNegativo() {
        Gato gato = new Gato(
                null, 1L, "Milo", LocalDate.now().plusDays(1), SexoGato.MACHO,
                "Común europeo", "Negro", new BigDecimal("-0.1"), null,
                false, null, null, true, LocalDateTime.now());

        assertThrows(ValidacionException.class, gato::validar);
    }

    @Test
    void evitaTratamientosDuplicadosEnLaMismaAtencion() {
        Atencion atencion = new Atencion(
                null, 10L, "Dermatitis", new BigDecimal("4.2"), new BigDecimal("38.5"),
                null, "Control en siete días", LocalDateTime.now());
        DetalleTratamiento detalle = new DetalleTratamiento(
                2L, "1 ml", "Cada 24 horas", 5, null, BigDecimal.ONE, BigDecimal.ZERO);

        atencion.agregarTratamiento(detalle);

        assertEquals(1, atencion.getTratamientos().size());
        assertThrows(ValidacionException.class, () -> atencion.agregarTratamiento(detalle));
    }
}
