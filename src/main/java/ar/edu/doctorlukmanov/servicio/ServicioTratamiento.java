package ar.edu.doctorlukmanov.servicio;

import ar.edu.doctorlukmanov.dao.TratamientoDao;
import ar.edu.doctorlukmanov.dto.TratamientoFormularioDto;
import ar.edu.doctorlukmanov.excepcion.EntidadNoEncontradaException;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.modelo.Tratamiento;
import ar.edu.doctorlukmanov.util.Validador;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public final class ServicioTratamiento {

    private final TratamientoDao tratamientoDao;

    public ServicioTratamiento(TratamientoDao tratamientoDao) {
        this.tratamientoDao = tratamientoDao;
    }

    public Tratamiento crear(TratamientoFormularioDto dto) {
        Validador.requerido(dto, "datos del tratamiento");
        String nombre = normalizarNombre(dto.nombre());
        verificarNombreDisponible(nombre, null);
        Tratamiento tratamiento = construir(dto, nombre, true, LocalDateTime.now());
        tratamiento.validar();
        return tratamientoDao.crear(tratamiento);
    }

    public boolean actualizar(TratamientoFormularioDto dto) {
        Validador.requerido(dto, "datos del tratamiento");
        Validador.identificadorRequerido(dto.idTratamiento(), "tratamiento");
        Tratamiento actual = buscarPorId(dto.idTratamiento());
        String nombre = normalizarNombre(dto.nombre());
        verificarNombreDisponible(nombre, dto.idTratamiento());
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

    private void verificarNombreDisponible(String nombre, Long idActual) {
        String clave = claveNombre(nombre);
        boolean duplicado = tratamientoDao.listarTodos().stream()
                .anyMatch(existente -> !existente.getIdTratamiento().equals(idActual)
                        && claveNombre(existente.getNombre()).equals(clave));
        if (duplicado) {
            throw new ValidacionException("Ya existe un tratamiento con ese nombre.");
        }
    }

    private String normalizarNombre(String nombre) {
        return Normalizer.normalize(
                Validador.textoRequerido(nombre, "nombre"), Normalizer.Form.NFC);
    }

    private String claveNombre(String nombre) {
        return Normalizer.normalize(nombre, Normalizer.Form.NFC).toUpperCase(Locale.ROOT);
    }
}
