package ar.edu.doctorlukmanov.modelo;

public enum SexoGato {
    MACHO("Macho"),
    HEMBRA("Hembra"),
    DESCONOCIDO("Desconocido");

    private final String descripcion;

    SexoGato(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
