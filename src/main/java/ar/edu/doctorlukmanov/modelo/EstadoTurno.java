package ar.edu.doctorlukmanov.modelo;

public enum EstadoTurno {
    PROGRAMADO("Programado"),
    CONFIRMADO("Confirmado"),
    COMPLETADO("Completado"),
    CANCELADO("Cancelado");

    private final String descripcion;

    EstadoTurno(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esTerminal() {
        return this == COMPLETADO || this == CANCELADO;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
