package ar.edu.doctorlukmanov.controlador;

import ar.edu.doctorlukmanov.dto.CierreAtencionDto;
import ar.edu.doctorlukmanov.dto.TurnoFormularioDto;
import ar.edu.doctorlukmanov.modelo.Atencion;
import ar.edu.doctorlukmanov.modelo.Turno;
import ar.edu.doctorlukmanov.servicio.ServicioTurno;
import java.time.LocalDate;
import java.util.List;

public final class ControladorTurno {

    private final ServicioTurno servicio;

    public ControladorTurno(ServicioTurno servicio) {
        this.servicio = servicio;
    }

    public Turno programar(TurnoFormularioDto dto) {
        return servicio.programar(dto);
    }

    public boolean actualizar(Long id, TurnoFormularioDto dto) {
        return servicio.actualizar(id, dto);
    }

    public void confirmar(Long id) {
        servicio.confirmar(id);
    }

    public void cancelar(Long id, String motivo) {
        servicio.cancelar(id, motivo);
    }

    public void completar(CierreAtencionDto dto) {
        servicio.completar(dto);
    }

    public Turno buscarPorId(Long id) {
        return servicio.buscarPorId(id);
    }

    public Atencion buscarAtencion(Long idTurno) {
        return servicio.buscarAtencion(idTurno);
    }

    public List<Turno> listarTodos() {
        return servicio.listarTodos();
    }

    public List<Turno> listarPorFecha(LocalDate fecha) {
        return servicio.listarPorFecha(fecha);
    }
}
