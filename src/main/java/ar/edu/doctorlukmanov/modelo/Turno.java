package ar.edu.doctorlukmanov.modelo;

import ar.edu.doctorlukmanov.excepcion.TransicionTurnoInvalidaException;
import ar.edu.doctorlukmanov.util.Validador;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class Turno {

    private static final Map<EstadoTurno, Set<EstadoTurno>> TRANSICIONES = crearTransiciones();

    private Long idTurno;
    private Long idGato;
    private Long idVeterinario;
    private LocalDateTime fechaHora;
    private int duracionMinutos;
    private String motivo;
    private EstadoTurno estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCierre;
    private String motivoCancelacion;
    private String observaciones;

    public Turno(
            Long idTurno,
            Long idGato,
            Long idVeterinario,
            LocalDateTime fechaHora,
            int duracionMinutos,
            String motivo,
            EstadoTurno estado,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaCierre,
            String motivoCancelacion,
            String observaciones) {
        this.idTurno = idTurno;
        this.idGato = idGato;
        this.idVeterinario = idVeterinario;
        this.fechaHora = fechaHora;
        this.duracionMinutos = duracionMinutos;
        this.motivo = motivo;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaCierre = fechaCierre;
        this.motivoCancelacion = motivoCancelacion;
        this.observaciones = observaciones;
    }

    private static Map<EstadoTurno, Set<EstadoTurno>> crearTransiciones() {
        Map<EstadoTurno, Set<EstadoTurno>> transiciones = new EnumMap<>(EstadoTurno.class);
        transiciones.put(EstadoTurno.PROGRAMADO,
                EnumSet.of(EstadoTurno.CONFIRMADO, EstadoTurno.COMPLETADO, EstadoTurno.CANCELADO));
        transiciones.put(EstadoTurno.CONFIRMADO,
                EnumSet.of(EstadoTurno.COMPLETADO, EstadoTurno.CANCELADO));
        transiciones.put(EstadoTurno.COMPLETADO, EnumSet.noneOf(EstadoTurno.class));
        transiciones.put(EstadoTurno.CANCELADO, EnumSet.noneOf(EstadoTurno.class));
        return Map.copyOf(transiciones);
    }

    public void validar() {
        Validador.identificadorRequerido(idGato, "gato");
        Validador.identificadorRequerido(idVeterinario, "veterinario");
        Validador.requerido(fechaHora, "fecha y hora");
        Validador.textoRequerido(motivo, "motivo");
        Validador.requerido(estado, "estado");
        if (duracionMinutos <= 0) {
            throw new ar.edu.doctorlukmanov.excepcion.ValidacionException(
                    "La duración del turno debe ser mayor que cero.");
        }
    }

    public boolean puedeTransicionarA(EstadoTurno nuevoEstado) {
        return nuevoEstado != null && TRANSICIONES.getOrDefault(estado, Set.of()).contains(nuevoEstado);
    }

    public void cambiarEstado(EstadoTurno nuevoEstado) {
        if (!puedeTransicionarA(nuevoEstado)) {
            throw new TransicionTurnoInvalidaException(idTurno, estado, nuevoEstado);
        }
        estado = nuevoEstado;
    }

    public void confirmar() {
        cambiarEstado(EstadoTurno.CONFIRMADO);
    }

    public void cancelar(String motivo, LocalDateTime fecha) {
        String motivoValidado = Validador.textoRequerido(motivo, "motivo de cancelación");
        LocalDateTime fechaValidada = Validador.requerido(fecha, "fecha de cierre");
        cambiarEstado(EstadoTurno.CANCELADO);
        motivoCancelacion = motivoValidado;
        fechaCierre = fechaValidada;
    }

    public void completar(LocalDateTime fecha) {
        LocalDateTime fechaValidada = Validador.requerido(fecha, "fecha de cierre");
        cambiarEstado(EstadoTurno.COMPLETADO);
        fechaCierre = fechaValidada;
    }

    public boolean estaAbierto() {
        return estado == EstadoTurno.PROGRAMADO || estado == EstadoTurno.CONFIRMADO;
    }

    public Long getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(Long idTurno) {
        this.idTurno = idTurno;
    }

    public Long getIdGato() {
        return idGato;
    }

    public Long getIdVeterinario() {
        return idVeterinario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
