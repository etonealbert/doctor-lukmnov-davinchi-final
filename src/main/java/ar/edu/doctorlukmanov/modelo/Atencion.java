package ar.edu.doctorlukmanov.modelo;

import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.util.Validador;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Atencion {

    private Long idAtencion;
    private Long idTurno;
    private String diagnostico;
    private BigDecimal pesoRegistrado;
    private BigDecimal temperatura;
    private String observacionesClinicas;
    private String indicaciones;
    private LocalDateTime fechaRegistro;
    private final List<DetalleTratamiento> tratamientos = new ArrayList<>();

    public Atencion(
            Long idAtencion,
            Long idTurno,
            String diagnostico,
            BigDecimal pesoRegistrado,
            BigDecimal temperatura,
            String observacionesClinicas,
            String indicaciones,
            LocalDateTime fechaRegistro) {
        this.idAtencion = idAtencion;
        this.idTurno = idTurno;
        this.diagnostico = diagnostico;
        this.pesoRegistrado = pesoRegistrado;
        this.temperatura = temperatura;
        this.observacionesClinicas = observacionesClinicas;
        this.indicaciones = indicaciones;
        this.fechaRegistro = fechaRegistro;
    }

    public void validar() {
        Validador.identificadorRequerido(idTurno, "turno");
        Validador.textoRequerido(diagnostico, "diagnóstico");
        Validador.decimalNoNegativo(pesoRegistrado, "peso registrado");
        if (temperatura != null
                && (temperatura.compareTo(new BigDecimal("30")) < 0
                || temperatura.compareTo(new BigDecimal("45")) > 0)) {
            throw new ValidacionException("La temperatura debe estar entre 30 y 45 °C.");
        }
        tratamientos.forEach(DetalleTratamiento::validar);
    }

    public void agregarTratamiento(DetalleTratamiento detalle) {
        Validador.requerido(detalle, "detalle de tratamiento");
        detalle.validar();
        boolean duplicado = tratamientos.stream()
                .anyMatch(actual -> actual.getIdTratamiento().equals(detalle.getIdTratamiento()));
        if (duplicado) {
            throw new ValidacionException("El tratamiento ya fue agregado a la atención.");
        }
        tratamientos.add(detalle);
    }

    public Long getIdAtencion() {
        return idAtencion;
    }

    public void setIdAtencion(Long idAtencion) {
        this.idAtencion = idAtencion;
    }

    public Long getIdTurno() {
        return idTurno;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public BigDecimal getPesoRegistrado() {
        return pesoRegistrado;
    }

    public BigDecimal getTemperatura() {
        return temperatura;
    }

    public String getObservacionesClinicas() {
        return observacionesClinicas;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public List<DetalleTratamiento> getTratamientos() {
        return Collections.unmodifiableList(tratamientos);
    }
}
