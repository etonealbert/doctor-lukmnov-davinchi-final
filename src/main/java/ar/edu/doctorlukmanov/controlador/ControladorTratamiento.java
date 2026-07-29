package ar.edu.doctorlukmanov.controlador;

import ar.edu.doctorlukmanov.dto.TratamientoFormularioDto;
import ar.edu.doctorlukmanov.modelo.Tratamiento;
import ar.edu.doctorlukmanov.servicio.ServicioTratamiento;
import java.util.List;

public final class ControladorTratamiento {

    private final ServicioTratamiento servicio;

    public ControladorTratamiento(ServicioTratamiento servicio) {
        this.servicio = servicio;
    }

    public Tratamiento crear(TratamientoFormularioDto dto) {
        return servicio.crear(dto);
    }

    public boolean actualizar(TratamientoFormularioDto dto) {
        return servicio.actualizar(dto);
    }

    public boolean eliminar(Long id) {
        return servicio.eliminar(id);
    }

    public boolean cambiarActivo(Long id, boolean activo) {
        return servicio.cambiarActivo(id, activo);
    }

    public Tratamiento buscarPorId(Long id) {
        return servicio.buscarPorId(id);
    }

    public List<Tratamiento> listarTodos() {
        return servicio.listarTodos();
    }

    public List<Tratamiento> listarActivos() {
        return servicio.listarActivos();
    }
}
