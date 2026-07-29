package ar.edu.doctorlukmanov.modelo;

import ar.edu.doctorlukmanov.util.Validador;
import java.time.LocalDateTime;

public final class Cliente extends Persona {

    private String dni;
    private String direccion;
    private LocalDateTime fechaRegistro;

    public Cliente(
            Long idCliente,
            String nombre,
            String apellido,
            String telefono,
            String correoElectronico,
            boolean activo,
            String dni,
            String direccion,
            LocalDateTime fechaRegistro) {
        super(idCliente, nombre, apellido, telefono, correoElectronico, activo);
        this.dni = dni;
        this.direccion = direccion;
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public void validar() {
        super.validar();
        Validador.textoRequerido(dni, "DNI");
        Validador.textoRequerido(getTelefono(), "teléfono");
    }

    @Override
    public String getTipoPersona() {
        return "Cliente";
    }

    public Long getIdCliente() {
        return getId();
    }

    public void setIdCliente(Long idCliente) {
        setId(idCliente);
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (DNI " + dni + ")";
    }
}
