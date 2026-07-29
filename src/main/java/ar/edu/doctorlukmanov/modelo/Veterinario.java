package ar.edu.doctorlukmanov.modelo;

import ar.edu.doctorlukmanov.util.Validador;
import java.time.LocalDateTime;

public final class Veterinario extends Persona {

    private String matricula;
    private String especialidad;
    private LocalDateTime fechaRegistro;

    public Veterinario(
            Long idVeterinario,
            String nombre,
            String apellido,
            String telefono,
            String correoElectronico,
            boolean activo,
            String matricula,
            String especialidad,
            LocalDateTime fechaRegistro) {
        super(idVeterinario, nombre, apellido, telefono, correoElectronico, activo);
        this.matricula = matricula;
        this.especialidad = especialidad;
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public void validar() {
        super.validar();
        Validador.textoRequerido(matricula, "matrícula");
        Validador.textoRequerido(especialidad, "especialidad");
    }

    @Override
    public String getTipoPersona() {
        return "Veterinario";
    }

    public Long getIdVeterinario() {
        return getId();
    }

    public void setIdVeterinario(Long idVeterinario) {
        setId(idVeterinario);
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public String toString() {
        return "Dr./Dra. " + getNombreCompleto() + " (" + matricula + ")";
    }
}
