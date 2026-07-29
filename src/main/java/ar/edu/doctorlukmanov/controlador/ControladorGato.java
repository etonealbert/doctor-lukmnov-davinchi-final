package ar.edu.doctorlukmanov.controlador;

import ar.edu.doctorlukmanov.dto.GatoFormularioDto;
import ar.edu.doctorlukmanov.modelo.Gato;
import ar.edu.doctorlukmanov.servicio.ServicioGato;
import java.util.List;

public final class ControladorGato {

    private final ServicioGato servicio;

    public ControladorGato(ServicioGato servicio) {
        this.servicio = servicio;
    }

    public Gato crear(GatoFormularioDto dto) {
        return servicio.crear(dto);
    }

    public boolean actualizar(GatoFormularioDto dto) {
        return servicio.actualizar(dto);
    }

    public boolean eliminar(Long id) {
        return servicio.eliminar(id);
    }

    public boolean cambiarActivo(Long id, boolean activo) {
        return servicio.cambiarActivo(id, activo);
    }

    public Gato buscarPorId(Long id) {
        return servicio.buscarPorId(id);
    }

    public List<Gato> buscar(String texto) {
        return servicio.buscar(texto);
    }

    public List<Gato> listarTodos() {
        return servicio.listarTodos();
    }

    public List<Gato> listarActivos() {
        return servicio.listarActivos();
    }

    public List<Gato> listarPorCliente(Long idCliente) {
        return servicio.listarPorCliente(idCliente);
    }
}
