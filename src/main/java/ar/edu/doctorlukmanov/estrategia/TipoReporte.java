package ar.edu.doctorlukmanov.estrategia;

public enum TipoReporte {
    PACIENTES_POR_MES("Pacientes por mes"),
    TURNOS_POR_MES("Turnos por mes"),
    TRATAMIENTOS_FRECUENTES("Tratamientos frecuentes"),
    PRODUCTIVIDAD_VETERINARIO("Productividad veterinaria"),
    HISTORIA_CLINICA("Historia clínica");

    private final String descripcion;

    TipoReporte(String descripcion) {
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
