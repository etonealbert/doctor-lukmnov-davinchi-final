package ar.edu.doctorlukmanov.modelo;

import ar.edu.doctorlukmanov.util.Validador;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Tratamiento {

    private Long idTratamiento;
    private String nombre;
    private String descripcion;
    private BigDecimal precioReferencia;
    private boolean activo;
    private LocalDateTime fechaRegistro;

    public Tratamiento(
            Long idTratamiento,
            String nombre,
            String descripcion,
            BigDecimal precioReferencia,
            boolean activo,
            LocalDateTime fechaRegistro) {
        this.idTratamiento = idTratamiento;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioReferencia = precioReferencia;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
    }

    public void validar() {
        Validador.textoRequerido(nombre, "nombre");
        Validador.decimalNoNegativo(precioReferencia, "precio de referencia");
    }

    public Long getIdTratamiento() {
        return idTratamiento;
    }

    public void setIdTratamiento(Long idTratamiento) {
        this.idTratamiento = idTratamiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioReferencia() {
        return precioReferencia;
    }

    public void setPrecioReferencia(BigDecimal precioReferencia) {
        this.precioReferencia = precioReferencia;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
