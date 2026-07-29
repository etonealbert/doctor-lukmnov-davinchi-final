package ar.edu.doctorlukmanov.controlador;

import ar.edu.doctorlukmanov.dto.VeterinarioFormularioDto;
import ar.edu.doctorlukmanov.modelo.Veterinario;
import ar.edu.doctorlukmanov.servicio.ServicioVeterinario;
import java.util.List;

public final class ControladorVeterinario {

    private final ServicioVeterinario servicio;

    public ControladorVeterinario(ServicioVeterinario servicio) {
        this.servicio = servicio;
    }

    public Veterinario crear(VeterinarioFormularioDto dto) {
        return servicio.crear(dto);
    }

    public boolean actualizar(VeterinarioFormularioDto dto) {
        return servicio.actualizar(dto);
    }

    public boolean eliminar(Long id) {
        return servicio.eliminar(id);
    }

    public boolean cambiarActivo(Long id, boolean activo) {
        return servicio.cambiarActivo(id, activo);
    }

    public Veterinario buscarPorId(Long id) {
        return servicio.buscarPorId(id);
    }

    public List<Veterinario> listarTodos() {
        return servicio.listarTodos();
    }

    public List<Veterinario> listarActivos() {
        return servicio.listarActivos();
    }
}
