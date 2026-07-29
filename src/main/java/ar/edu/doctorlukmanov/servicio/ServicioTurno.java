package ar.edu.doctorlukmanov.servicio;

import ar.edu.doctorlukmanov.dao.AtencionDao;
import ar.edu.doctorlukmanov.dao.GatoDao;
import ar.edu.doctorlukmanov.dao.TratamientoDao;
import ar.edu.doctorlukmanov.dao.TurnoDao;
import ar.edu.doctorlukmanov.dao.VeterinarioDao;
import ar.edu.doctorlukmanov.dto.CierreAtencionDto;
import ar.edu.doctorlukmanov.dto.DetalleTratamientoDto;
import ar.edu.doctorlukmanov.dto.TurnoFormularioDto;
import ar.edu.doctorlukmanov.excepcion.EntidadNoEncontradaException;
import ar.edu.doctorlukmanov.excepcion.PersistenciaException;
import ar.edu.doctorlukmanov.excepcion.TransicionTurnoInvalidaException;
import ar.edu.doctorlukmanov.excepcion.TurnoNoDisponibleException;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.modelo.Atencion;
import ar.edu.doctorlukmanov.modelo.DetalleTratamiento;
import ar.edu.doctorlukmanov.modelo.EstadoTurno;
import ar.edu.doctorlukmanov.modelo.Gato;
import ar.edu.doctorlukmanov.modelo.Tratamiento;
import ar.edu.doctorlukmanov.modelo.Turno;
import ar.edu.doctorlukmanov.modelo.Veterinario;
import ar.edu.doctorlukmanov.util.ConexionBaseDatos;
import ar.edu.doctorlukmanov.util.Validador;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ServicioTurno {

    private static final int DURACION_PREDETERMINADA = 30;

    private final TurnoDao turnoDao;
    private final GatoDao gatoDao;
    private final VeterinarioDao veterinarioDao;
    private final AtencionDao atencionDao;
    private final TratamientoDao tratamientoDao;
    private final ConexionBaseDatos baseDatos;

    public ServicioTurno(
            TurnoDao turnoDao,
            GatoDao gatoDao,
            VeterinarioDao veterinarioDao,
            AtencionDao atencionDao,
            TratamientoDao tratamientoDao,
            ConexionBaseDatos baseDatos) {
        this.turnoDao = turnoDao;
        this.gatoDao = gatoDao;
        this.veterinarioDao = veterinarioDao;
        this.atencionDao = atencionDao;
        this.tratamientoDao = tratamientoDao;
        this.baseDatos = baseDatos;
    }

    public Turno programar(TurnoFormularioDto dto) {
        DatosProgramacion datos = validarProgramacion(dto, null);
        Turno turno = new Turno(
                null,
                datos.idGato(),
                datos.idVeterinario(),
                datos.fechaHora(),
                DURACION_PREDETERMINADA,
                datos.motivo(),
                EstadoTurno.PROGRAMADO,
                LocalDateTime.now(),
                null,
                null,
                datos.observaciones());
        turno.validar();
        try {
            return turnoDao.crear(turno);
        } catch (PersistenciaException ex) {
            if (esConflictoUnico(ex)) {
                throw new TurnoNoDisponibleException(
                        "El veterinario ya tiene un turno en el horario seleccionado.", ex);
            }
            throw ex;
        }
    }

    public boolean actualizar(Long idTurno, TurnoFormularioDto dto) {
        Turno actual = buscarPorId(idTurno);
        if (actual.getEstado() != EstadoTurno.PROGRAMADO) {
            throw new TransicionTurnoInvalidaException(
                    "Solo se pueden editar turnos en estado PROGRAMADO.");
        }
        DatosProgramacion datos = validarProgramacion(dto, idTurno);
        Turno actualizado = new Turno(
                idTurno,
                datos.idGato(),
                datos.idVeterinario(),
                datos.fechaHora(),
                actual.getDuracionMinutos(),
                datos.motivo(),
                actual.getEstado(),
                actual.getFechaCreacion(),
                null,
                null,
                datos.observaciones());
        return turnoDao.actualizar(actualizado);
    }

    public void confirmar(Long idTurno) {
        Turno turno = buscarPorId(idTurno);
        turno.confirmar();
        if (!turnoDao.actualizar(turno)) {
            throw new EntidadNoEncontradaException("turno", idTurno);
        }
    }

    public void cancelar(Long idTurno, String motivo) {
        String motivoValidado = Validador.textoRequerido(motivo, "motivo de cancelación");
        Turno turno = buscarPorId(idTurno);
        turno.cancelar(motivoValidado, LocalDateTime.now());
        if (!turnoDao.actualizar(turno)) {
            throw new EntidadNoEncontradaException("turno", idTurno);
        }
    }

    public void completar(CierreAtencionDto dto) {
        Validador.requerido(dto, "datos de la atención");
        Validador.identificadorRequerido(dto.idTurno(), "turno");
        Atencion atencion = construirAtencion(dto);
        atencion.validar();

        baseDatos.ejecutarEnTransaccion(conexion -> {
            Turno turno = turnoDao.buscarPorId(conexion, dto.idTurno())
                    .orElseThrow(() -> new EntidadNoEncontradaException("turno", dto.idTurno()));
            LocalDateTime fechaCierre = LocalDateTime.now();
            turno.completar(fechaCierre);
            if (atencionDao.buscarPorTurno(conexion, turno.getIdTurno()).isPresent()) {
                throw new TransicionTurnoInvalidaException(
                        "El turno ya posee una atención clínica registrada.");
            }

            atencionDao.crear(conexion, atencion);
            for (DetalleTratamiento detalle : atencion.getTratamientos()) {
                Tratamiento tratamiento = tratamientoDao
                        .buscarPorId(conexion, detalle.getIdTratamiento())
                        .orElseThrow(() -> new EntidadNoEncontradaException(
                                "tratamiento", detalle.getIdTratamiento()));
                if (!tratamiento.isActivo()) {
                    throw new ValidacionException(
                            "El tratamiento " + tratamiento.getNombre() + " está inactivo.");
                }
                atencionDao.agregarTratamiento(conexion, atencion.getIdAtencion(), detalle);
            }

            if (atencion.getPesoRegistrado() != null
                    && !gatoDao.actualizarPeso(conexion, turno.getIdGato(), atencion.getPesoRegistrado())) {
                throw new EntidadNoEncontradaException("gato", turno.getIdGato());
            }
            if (!turnoDao.actualizar(conexion, turno)) {
                throw new EntidadNoEncontradaException("turno", turno.getIdTurno());
            }
            return null;
        });
    }

    public Turno buscarPorId(Long idTurno) {
        Validador.identificadorRequerido(idTurno, "turno");
        return turnoDao.buscarPorId(idTurno)
                .orElseThrow(() -> new EntidadNoEncontradaException("turno", idTurno));
    }

    public List<Turno> listarTodos() {
        return turnoDao.listarTodos();
    }

    public List<Turno> listarPorFecha(LocalDate fecha) {
        Validador.requerido(fecha, "fecha");
        return turnoDao.listarPorFecha(fecha);
    }

    public Atencion buscarAtencion(Long idTurno) {
        buscarPorId(idTurno);
        return atencionDao.buscarPorTurno(idTurno)
                .orElseThrow(() -> new EntidadNoEncontradaException("atención del turno", idTurno));
    }

    private DatosProgramacion validarProgramacion(TurnoFormularioDto dto, Long idExcluir) {
        Validador.requerido(dto, "datos del turno");
        Long idGato = Validador.identificadorRequerido(dto.idGato(), "gato");
        Long idVeterinario = Validador.identificadorRequerido(dto.idVeterinario(), "veterinario");
        Validador.fechaHoraFutura(dto.fechaHora(), "fecha y hora");
        String motivo = Validador.textoRequerido(dto.motivo(), "motivo");

        Gato gato = gatoDao.buscarPorId(idGato)
                .orElseThrow(() -> new EntidadNoEncontradaException("gato", idGato));
        if (!gato.isActivo()) {
            throw new ValidacionException("El gato seleccionado está inactivo.");
        }
        Veterinario veterinario = veterinarioDao.buscarPorId(idVeterinario)
                .orElseThrow(() -> new EntidadNoEncontradaException("veterinario", idVeterinario));
        if (!veterinario.isActivo()) {
            throw new ValidacionException("El veterinario seleccionado está inactivo.");
        }
        if (turnoDao.existeSuperposicion(
                idVeterinario, dto.fechaHora(), DURACION_PREDETERMINADA, idExcluir)) {
            throw new TurnoNoDisponibleException(
                    "El veterinario ya tiene un turno en el horario seleccionado.");
        }
        return new DatosProgramacion(
                idGato,
                idVeterinario,
                dto.fechaHora().withNano(0),
                motivo,
                Validador.textoOpcional(dto.observaciones()));
    }

    private Atencion construirAtencion(CierreAtencionDto dto) {
        Atencion atencion = new Atencion(
                null,
                dto.idTurno(),
                Validador.textoRequerido(dto.diagnostico(), "diagnóstico"),
                dto.pesoRegistrado(),
                dto.temperatura(),
                Validador.textoOpcional(dto.observacionesClinicas()),
                Validador.textoOpcional(dto.indicaciones()),
                LocalDateTime.now());
        for (DetalleTratamientoDto detalleDto : dto.tratamientos()) {
            atencion.agregarTratamiento(new DetalleTratamiento(
                    detalleDto.idTratamiento(),
                    Validador.textoOpcional(detalleDto.dosis()),
                    Validador.textoOpcional(detalleDto.frecuencia()),
                    detalleDto.duracionDias(),
                    Validador.textoOpcional(detalleDto.observaciones()),
                    detalleDto.cantidad() == null ? BigDecimal.ONE : detalleDto.cantidad(),
                    detalleDto.precioAplicado() == null ? BigDecimal.ZERO : detalleDto.precioAplicado()));
        }
        return atencion;
    }

    private boolean esConflictoUnico(Throwable ex) {
        Throwable actual = ex;
        while (actual != null) {
            if (actual.getMessage() != null && actual.getMessage().contains("UNIQUE constraint failed")) {
                return true;
            }
            actual = actual.getCause();
        }
        return false;
    }

    private record DatosProgramacion(
            Long idGato,
            Long idVeterinario,
            LocalDateTime fechaHora,
            String motivo,
            String observaciones) {
    }
}
