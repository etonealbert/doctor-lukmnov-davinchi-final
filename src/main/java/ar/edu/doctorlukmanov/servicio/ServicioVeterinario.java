package ar.edu.doctorlukmanov.servicio;

import ar.edu.doctorlukmanov.dao.VeterinarioDao;
import ar.edu.doctorlukmanov.dto.VeterinarioFormularioDto;
import ar.edu.doctorlukmanov.excepcion.EntidadNoEncontradaException;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.modelo.Veterinario;
import ar.edu.doctorlukmanov.util.Validador;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public final class ServicioVeterinario {

    private final VeterinarioDao veterinarioDao;

    public ServicioVeterinario(VeterinarioDao veterinarioDao) {
        this.veterinarioDao = veterinarioDao;
    }

    public Veterinario crear(VeterinarioFormularioDto dto) {
        Validador.requerido(dto, "datos del veterinario");
        String matricula = normalizarMatricula(dto.matricula());
        if (veterinarioDao.buscarPorMatricula(matricula).isPresent()) {
            throw new ValidacionException("Ya existe un veterinario con esa matrícula.");
        }
        Veterinario veterinario = construir(dto, matricula, true, LocalDateTime.now());
        veterinario.validar();
        return veterinarioDao.crear(veterinario);
    }

    public boolean actualizar(VeterinarioFormularioDto dto) {
        Validador.requerido(dto, "datos del veterinario");
        Validador.identificadorRequerido(dto.idVeterinario(), "veterinario");
        Veterinario actual = buscarPorId(dto.idVeterinario());
        String matricula = normalizarMatricula(dto.matricula());
        veterinarioDao.buscarPorMatricula(matricula)
                .filter(encontrado -> !encontrado.getIdVeterinario().equals(dto.idVeterinario()))
                .ifPresent(encontrado -> {
                    throw new ValidacionException("Ya existe un veterinario con esa matrícula.");
                });
        Veterinario veterinario = construir(dto, matricula, actual.isActivo(), actual.getFechaRegistro());
        veterinario.validar();
        return veterinarioDao.actualizar(veterinario);
    }

    public boolean eliminar(Long idVeterinario) {
        Veterinario veterinario = buscarPorId(idVeterinario);
        veterinario.setActivo(false);
        return veterinarioDao.actualizar(veterinario);
    }

    public boolean cambiarActivo(Long idVeterinario, boolean activo) {
        Veterinario veterinario = buscarPorId(idVeterinario);
        veterinario.setActivo(activo);
        return veterinarioDao.actualizar(veterinario);
    }

    public Veterinario buscarPorId(Long idVeterinario) {
        Validador.identificadorRequerido(idVeterinario, "veterinario");
        return veterinarioDao.buscarPorId(idVeterinario)
                .orElseThrow(() -> new EntidadNoEncontradaException("veterinario", idVeterinario));
    }

    public List<Veterinario> listarTodos() {
        return veterinarioDao.listarTodos();
    }

    public List<Veterinario> listarActivos() {
        return veterinarioDao.listarActivos();
    }

    private Veterinario construir(
            VeterinarioFormularioDto dto,
            String matricula,
            boolean activo,
            LocalDateTime fechaRegistro) {
        String correo = Validador.textoOpcional(dto.correoElectronico());
        Validador.correo(correo);
        return new Veterinario(
                dto.idVeterinario(),
                Validador.textoRequerido(dto.nombre(), "nombre"),
                Validador.textoRequerido(dto.apellido(), "apellido"),
                Validador.textoOpcional(dto.telefono()),
                correo == null ? null : correo.toLowerCase(Locale.ROOT),
                activo,
                matricula,
                Validador.textoRequerido(dto.especialidad(), "especialidad"),
                fechaRegistro);
    }

    private String normalizarMatricula(String valor) {
        return Validador.textoRequerido(valor, "matrícula").toUpperCase(Locale.ROOT);
    }
}
