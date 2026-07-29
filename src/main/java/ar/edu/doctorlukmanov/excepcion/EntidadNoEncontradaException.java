package ar.edu.doctorlukmanov.excepcion;

public class EntidadNoEncontradaException extends ClinicaException {

    public EntidadNoEncontradaException(String mensaje) {
        super(mensaje);
    }

    public EntidadNoEncontradaException(String tipoEntidad, Object identificador) {
        super("No se encontró " + tipoEntidad + " con identificador " + identificador + ".");
    }
}
