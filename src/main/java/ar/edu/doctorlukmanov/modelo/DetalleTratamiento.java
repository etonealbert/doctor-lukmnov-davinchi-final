package ar.edu.doctorlukmanov.modelo;

import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.util.Validador;
import java.math.BigDecimal;

public final class DetalleTratamiento {

    private final Long idTratamiento;
    private final String dosis;
    private final String frecuencia;
    private final Integer duracionDias;
    private final String observaciones;
    private final BigDecimal cantidad;
    private final BigDecimal precioAplicado;

    public DetalleTratamiento(
            Long idTratamiento,
            String dosis,
            String frecuencia,
            Integer duracionDias,
            String observaciones,
            BigDecimal cantidad,
            BigDecimal precioAplicado) {
        this.idTratamiento = idTratamiento;
        this.dosis = dosis;
        this.frecuencia = frecuencia;
        this.duracionDias = duracionDias;
        this.observaciones = observaciones;
        this.cantidad = cantidad;
        this.precioAplicado = precioAplicado;
    }

    public void validar() {
        Validador.identificadorRequerido(idTratamiento, "tratamiento");
        if (duracionDias != null && duracionDias < 0) {
            throw new ValidacionException("La duración no puede ser negativa.");
        }
        Validador.decimalPositivo(cantidad, "cantidad");
        Validador.decimalNoNegativo(precioAplicado, "precio aplicado");
    }

    public Long getIdTratamiento() {
        return idTratamiento;
    }

    public String getDosis() {
        return dosis;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public Integer getDuracionDias() {
        return duracionDias;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioAplicado() {
        return precioAplicado;
    }
}
