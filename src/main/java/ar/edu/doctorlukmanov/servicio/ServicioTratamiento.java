package ar.edu.doctorlukmanov.servicio;

import ar.edu.doctorlukmanov.dao.TratamientoDao;
import ar.edu.doctorlukmanov.dto.TratamientoFormularioDto;
import ar.edu.doctorlukmanov.excepcion.EntidadNoEncontradaException;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.modelo.Tratamiento;
import ar.edu.doctorlukmanov.util.Validador;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ServicioTratamiento {

    private final TratamientoDao tratamientoDao;

    public ServicioTratamiento(TratamientoDao tratamientoDao) {
        this.tratamientoDao = tratamientoDao;
    }

    public Tratamiento crear(TratamientoFormularioDto dto) {
        Validador.requerido(dto, "datos del tratamiento");
        String nombre = Validador.textoRequerido(dto.nombre(), "nombre");
        if (tratamientoDao.buscarPorNombre(nombre).isPresent()) {
            throw new ValidacionException("Ya existe un tratamiento con ese nombre.");
        }
        Tratamiento tratamiento = construir(dto, nombre, true, LocalDateTime.now());
        tratamiento.validar();
        return tratamientoDao.crear(tratamiento);
    }

    public boolean actualizar(TratamientoFormularioDto dto) {
        Validador.requerido(dto, "datos del tratamiento");
        Validador.identificadorRequerido(dto.idTratamiento(), "tratamiento");
        Tratamiento actual = buscarPorId(dto.idTratamiento());
        String nombre = Validador.textoRequerido(dto.nombre(), "nombre");
        tratamientoDao.buscarPorNombre(nombre)
                .filter(encontrado -> !encontrado.getIdTratamiento().equals(dto.idTratamiento()))
                .ifPresent(encontrado -> {
                    throw new ValidacionException("Ya existe un tratamiento con ese nombre.");
                });
        Tratamiento tratamiento = construir(dto, nombre, actual.isActivo(), actual.getFechaRegistro());
        tratamiento.validar();
        return tratamientoDao.actualizar(tratamiento);
    }

    public boolean eliminar(Long idTratamiento) {
        Tratamiento tratamiento = buscarPorId(idTratamiento);
        tratamiento.setActivo(false);
        return tratamientoDao.actualizar(tratamiento);
    }

    public boolean cambiarActivo(Long idTratamiento, boolean activo) {
        Tratamiento tratamiento = buscarPorId(idTratamiento);
        tratamiento.setActivo(activo);
        return tratamientoDao.actualizar(tratamiento);
    }

    public Tratamiento buscarPorId(Long idTratamiento) {
        Validador.identificadorRequerido(idTratamiento, "tratamiento");
        return tratamientoDao.buscarPorId(idTratamiento)
                .orElseThrow(() -> new EntidadNoEncontradaException("tratamiento", idTratamiento));
    }

    public List<Tratamiento> listarTodos() {
        return tratamientoDao.listarTodos();
    }

    public List<Tratamiento> listarActivos() {
        return tratamientoDao.listarActivos();
    }

    private Tratamiento construir(
            TratamientoFormularioDto dto,
            String nombre,
            boolean activo,
            LocalDateTime fechaRegistro) {
        BigDecimal precio = dto.precioReferencia() == null ? BigDecimal.ZERO : dto.precioReferencia();
        return new Tratamiento(
                dto.idTratamiento(),
                nombre,
                Validador.textoOpcional(dto.descripcion()),
                precio,
                activo,
                fechaRegistro);
    }
}
