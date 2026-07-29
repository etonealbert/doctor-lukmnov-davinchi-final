package ar.edu.doctorlukmanov.excepcion;

import ar.edu.doctorlukmanov.modelo.EstadoTurno;

public class TransicionTurnoInvalidaException extends ClinicaException {

    public TransicionTurnoInvalidaException(
            Long idTurno, EstadoTurno estadoActual, EstadoTurno estadoSolicitado) {
        super("No se puede cambiar el turno " + (idTurno == null ? "nuevo" : idTurno)
                + " de " + estadoActual + " a " + estadoSolicitado + ".");
    }

    public TransicionTurnoInvalidaException(String mensaje) {
        super(mensaje);
    }
}
