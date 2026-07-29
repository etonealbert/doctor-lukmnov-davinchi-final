package ar.edu.doctorlukmanov.modelo;

import ar.edu.doctorlukmanov.util.Validador;

public abstract class Persona {

    private Long id;
    private String nombre;
    private String apellido;
    private String telefono;
    private String correoElectronico;
    private boolean activo;

    protected Persona(
            Long id,
            String nombre,
            String apellido,
            String telefono,
            String correoElectronico,
            boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.correoElectronico = correoElectronico;
        this.activo = activo;
    }

    public void validar() {
        Validador.textoRequerido(nombre, "nombre");
        Validador.textoRequerido(apellido, "apellido");
        Validador.correo(correoElectronico);
    }

    public String getNombreCompleto() {
        return Validador.textoRequerido(nombre, "nombre") + " "
                + Validador.textoRequerido(apellido, "apellido");
    }

    public abstract String getTipoPersona();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
