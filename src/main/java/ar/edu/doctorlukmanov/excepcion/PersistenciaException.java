package ar.edu.doctorlukmanov.excepcion;

public class PersistenciaException extends ClinicaException {

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
