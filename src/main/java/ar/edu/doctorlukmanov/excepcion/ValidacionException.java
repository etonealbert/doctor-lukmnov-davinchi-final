package ar.edu.doctorlukmanov.excepcion;

public class ValidacionException extends ClinicaException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }

    public ValidacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
