package ar.edu.doctorlukmanov.controlador;

import ar.edu.doctorlukmanov.dto.ClienteFormularioDto;
import ar.edu.doctorlukmanov.modelo.Cliente;
import ar.edu.doctorlukmanov.servicio.ServicioCliente;
import java.util.List;

public final class ControladorCliente {

    private final ServicioCliente servicio;

    public ControladorCliente(ServicioCliente servicio) {
        this.servicio = servicio;
    }

    public Cliente crear(ClienteFormularioDto dto) {
        return servicio.crear(dto);
    }

    public boolean actualizar(ClienteFormularioDto dto) {
        return servicio.actualizar(dto);
    }

    public boolean eliminar(Long id) {
        return servicio.eliminar(id);
    }

    public boolean cambiarActivo(Long id, boolean activo) {
        return servicio.cambiarActivo(id, activo);
    }

    public Cliente buscarPorId(Long id) {
        return servicio.buscarPorId(id);
    }

    public List<Cliente> buscar(String texto) {
        return servicio.buscar(texto);
    }

    public List<Cliente> listarTodos() {
        return servicio.listarTodos();
    }

    public List<Cliente> listarActivos() {
        return servicio.listarActivos();
    }
}
