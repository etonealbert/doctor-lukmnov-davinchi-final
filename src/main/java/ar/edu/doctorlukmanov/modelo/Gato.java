package ar.edu.doctorlukmanov.modelo;

import ar.edu.doctorlukmanov.util.Validador;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public final class Gato {

    private Long idGato;
    private Long idCliente;
    private String nombre;
    private LocalDate fechaNacimiento;
    private SexoGato sexo;
    private String raza;
    private String color;
    private BigDecimal pesoActual;
    private String numeroMicrochip;
    private boolean esterilizado;
    private String alergias;
    private String observaciones;
    private boolean activo;
    private LocalDateTime fechaRegistro;

    public Gato(
            Long idGato,
            Long idCliente,
            String nombre,
            LocalDate fechaNacimiento,
            SexoGato sexo,
            String raza,
            String color,
            BigDecimal pesoActual,
            String numeroMicrochip,
            boolean esterilizado,
            String alergias,
            String observaciones,
            boolean activo,
            LocalDateTime fechaRegistro) {
        this.idGato = idGato;
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.raza = raza;
        this.color = color;
        this.pesoActual = pesoActual;
        this.numeroMicrochip = numeroMicrochip;
        this.esterilizado = esterilizado;
        this.alergias = alergias;
        this.observaciones = observaciones;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
    }

    public void validar() {
        Validador.identificadorRequerido(idCliente, "cliente responsable");
        Validador.textoRequerido(nombre, "nombre");
        Validador.requerido(sexo, "sexo");
        Validador.fechaNoFutura(fechaNacimiento, "fecha de nacimiento");
        Validador.decimalNoNegativo(pesoActual, "peso actual");
    }

    public Period calcularEdadAproximada() {
        return fechaNacimiento == null ? Period.ZERO : Period.between(fechaNacimiento, LocalDate.now());
    }

    public Long getIdGato() {
        return idGato;
    }

    public void setIdGato(Long idGato) {
        this.idGato = idGato;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public SexoGato getSexo() {
        return sexo;
    }

    public void setSexo(SexoGato sexo) {
        this.sexo = sexo;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getPesoActual() {
        return pesoActual;
    }

    public void setPesoActual(BigDecimal pesoActual) {
        this.pesoActual = pesoActual;
    }

    public String getNumeroMicrochip() {
        return numeroMicrochip;
    }

    public void setNumeroMicrochip(String numeroMicrochip) {
        this.numeroMicrochip = numeroMicrochip;
    }

    public boolean isEsterilizado() {
        return esterilizado;
    }

    public void setEsterilizado(boolean esterilizado) {
        this.esterilizado = esterilizado;
    }

    public String getAlergias() {
        return alergias;
    }

    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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
        return nombre + (numeroMicrochip == null ? "" : " (" + numeroMicrochip + ")");
    }
}
