package ar.edu.doctorlukmanov.dto;

public record VeterinarioFormularioDto(
        Long idVeterinario,
        String nombre,
        String apellido,
        String matricula,
        String telefono,
        String correoElectronico,
        String especialidad) {
}
