package ar.edu.doctorlukmanov.dto;

public record ClienteFormularioDto(
        Long idCliente,
        String nombre,
        String apellido,
        String dni,
        String telefono,
        String correoElectronico,
        String direccion) {
}
