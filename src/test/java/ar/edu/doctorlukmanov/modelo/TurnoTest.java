package ar.edu.doctorlukmanov.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.doctorlukmanov.excepcion.TransicionTurnoInvalidaException;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

class TurnoTest {

    @Test
    void noModificaElTurnoCuandoFallaElCierre() {
        Turno programado = crearTurno(EstadoTurno.PROGRAMADO);
        Turno cancelado = crearTurno(EstadoTurno.CANCELADO);
        String motivoOriginal = cancelado.getMotivoCancelacion();

        assertThrows(RuntimeException.class, () -> programado.completar(null));
        assertThrows(TransicionTurnoInvalidaException.class,
                () -> cancelado.cancelar("Motivo reemplazado", LocalDateTime.now()));

        assertEquals(EstadoTurno.PROGRAMADO, programado.getEstado());
        assertEquals(motivoOriginal, cancelado.getMotivoCancelacion());
    }

    @ParameterizedTest
    @MethodSource("transicionesPermitidas")
    void permiteLasTransicionesDefinidas(EstadoTurno origen, EstadoTurno destino) {
        Turno turno = crearTurno(origen);

        assertTrue(turno.puedeTransicionarA(destino));
        turno.cambiarEstado(destino);

        assertEquals(destino, turno.getEstado());
        assertEquals(destino == EstadoTurno.PROGRAMADO || destino == EstadoTurno.CONFIRMADO,
                turno.estaAbierto());
    }

    @ParameterizedTest
    @MethodSource("transicionesRechazadas")
    void rechazaTransicionesInvalidasSinModificarElEstado(EstadoTurno origen, EstadoTurno destino) {
        Turno turno = crearTurno(origen);

        assertFalse(turno.puedeTransicionarA(destino));
        assertThrows(TransicionTurnoInvalidaException.class, () -> turno.cambiarEstado(destino));
        assertEquals(origen, turno.getEstado());
    }

    private static Stream<Arguments> transicionesPermitidas() {
        return Stream.of(
                Arguments.of(EstadoTurno.PROGRAMADO, EstadoTurno.CONFIRMADO),
                Arguments.of(EstadoTurno.PROGRAMADO, EstadoTurno.COMPLETADO),
                Arguments.of(EstadoTurno.PROGRAMADO, EstadoTurno.CANCELADO),
                Arguments.of(EstadoTurno.CONFIRMADO, EstadoTurno.COMPLETADO),
                Arguments.of(EstadoTurno.CONFIRMADO, EstadoTurno.CANCELADO));
    }

    private static Stream<Arguments> transicionesRechazadas() {
        return Stream.of(
                Arguments.of(EstadoTurno.PROGRAMADO, EstadoTurno.PROGRAMADO),
                Arguments.of(EstadoTurno.CONFIRMADO, EstadoTurno.PROGRAMADO),
                Arguments.of(EstadoTurno.CONFIRMADO, EstadoTurno.CONFIRMADO),
                Arguments.of(EstadoTurno.COMPLETADO, EstadoTurno.PROGRAMADO),
                Arguments.of(EstadoTurno.COMPLETADO, EstadoTurno.CANCELADO),
                Arguments.of(EstadoTurno.CANCELADO, EstadoTurno.COMPLETADO),
                Arguments.of(EstadoTurno.CANCELADO, EstadoTurno.CONFIRMADO));
    }

    private static Turno crearTurno(EstadoTurno estado) {
        return new Turno(
                42L,
                3L,
                7L,
                LocalDateTime.now().plusDays(1),
                30,
                "Control general",
                estado,
                LocalDateTime.now(),
                estado.esTerminal() ? LocalDateTime.now() : null,
                estado == EstadoTurno.CANCELADO ? "Solicitud del cliente" : null,
                null);
    }
}
