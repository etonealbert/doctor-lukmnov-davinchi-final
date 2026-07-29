package ar.edu.doctorlukmanov.excepcion;

public class TurnoNoDisponibleException extends ClinicaException {

    public TurnoNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public TurnoNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
