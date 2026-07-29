package ar.edu.doctorlukmanov.dao.sqlite;

import ar.edu.doctorlukmanov.dao.TurnoDao;
import ar.edu.doctorlukmanov.excepcion.PersistenciaException;
import ar.edu.doctorlukmanov.modelo.EstadoTurno;
import ar.edu.doctorlukmanov.modelo.Turno;
import ar.edu.doctorlukmanov.util.ConexionBaseDatos;
import ar.edu.doctorlukmanov.util.FechasUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TurnoDaoSqlite implements TurnoDao {

    private static final String COLUMNAS = "id_turno, id_gato, id_veterinario, fecha_hora, "
            + "duracion_minutos, motivo, estado, fecha_creacion, fecha_cierre, motivo_cancelacion, "
            + "observaciones";

    private final ConexionBaseDatos baseDatos;

    public TurnoDaoSqlite(ConexionBaseDatos baseDatos) {
        this.baseDatos = baseDatos;
    }

    @Override
    public Turno crear(Turno turno) {
        turno.validar();
        String sql = "INSERT INTO turnos (id_gato, id_veterinario, fecha_hora, duracion_minutos, "
                + "motivo, estado, fecha_creacion, fecha_cierre, motivo_cancelacion, observaciones) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            establecerDatos(sentencia, turno, false);
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("SQLite no devolvió el identificador generado.");
                }
                turno.setIdTurno(claves.getLong(1));
            }
            return turno;
        } catch (SQLException ex) {
            throw error("crear el turno", ex);
        }
    }

    @Override
    public Optional<Turno> buscarPorId(Long id) {
        try (Connection conexion = baseDatos.obtenerConexion()) {
            return buscarPorId(conexion, id);
        } catch (SQLException ex) {
            throw error("buscar el turno", ex);
        }
    }

    @Override
    public Optional<Turno> buscarPorId(Connection conexion, Long id) {
        String sql = "SELECT " + COLUMNAS + " FROM turnos WHERE id_turno = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw error("buscar el turno", ex);
        }
    }

    @Override
    public List<Turno> listarTodos() {
        return listar("SELECT " + COLUMNAS + " FROM turnos ORDER BY fecha_hora DESC",
                "listar los turnos");
    }

    @Override
    public List<Turno> listarPorFecha(LocalDate fecha) {
        String sql = "SELECT " + COLUMNAS + " FROM turnos WHERE date(fecha_hora) = ? ORDER BY fecha_hora";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, fecha.toString());
            return listar(sentencia);
        } catch (SQLException ex) {
            throw error("listar los turnos de la fecha", ex);
        }
    }

    @Override
    public boolean existeSuperposicion(
            Long idVeterinario,
            LocalDateTime fechaHora,
            int duracionMinutos,
            Long idExcluir) {
        String sql = "SELECT 1 FROM turnos WHERE id_veterinario = ? "
                + "AND estado IN ('PROGRAMADO', 'CONFIRMADO') "
                + "AND (? IS NULL OR id_turno <> ?) "
                + "AND datetime(fecha_hora) < datetime(?) "
                + "AND datetime(fecha_hora, '+' || duracion_minutos || ' minutes') > datetime(?) LIMIT 1";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idVeterinario);
            if (idExcluir == null) {
                sentencia.setObject(2, null);
                sentencia.setObject(3, null);
            } else {
                sentencia.setLong(2, idExcluir);
                sentencia.setLong(3, idExcluir);
            }
            sentencia.setString(4, FechasUtil.aTexto(fechaHora.plusMinutes(duracionMinutos)));
            sentencia.setString(5, FechasUtil.aTexto(fechaHora));
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next();
            }
        } catch (SQLException ex) {
            throw error("verificar la disponibilidad del veterinario", ex);
        }
    }

    @Override
    public boolean actualizar(Turno turno) {
        try (Connection conexion = baseDatos.obtenerConexion()) {
            return actualizar(conexion, turno);
        } catch (SQLException ex) {
            throw error("actualizar el turno", ex);
        }
    }

    @Override
    public boolean actualizar(Connection conexion, Turno turno) {
        return actualizar(conexion, turno, null);
    }

    @Override
    public boolean actualizarSiEstadoActual(Turno turno, EstadoTurno estadoEsperado) {
        try (Connection conexion = baseDatos.obtenerConexion()) {
            return actualizarSiEstadoActual(conexion, turno, estadoEsperado);
        } catch (SQLException ex) {
            throw error("actualizar el turno", ex);
        }
    }

    @Override
    public boolean actualizarSiEstadoActual(
            Connection conexion, Turno turno, EstadoTurno estadoEsperado) {
        return actualizar(conexion, turno, estadoEsperado);
    }

    private boolean actualizar(
            Connection conexion, Turno turno, EstadoTurno estadoEsperado) {
        turno.validar();
        String sql = "UPDATE turnos SET id_gato = ?, id_veterinario = ?, fecha_hora = ?, "
                + "duracion_minutos = ?, motivo = ?, estado = ?, fecha_cierre = ?, "
                + "motivo_cancelacion = ?, observaciones = ? WHERE id_turno = ?"
                + (estadoEsperado == null ? "" : " AND estado = ?");
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            establecerDatos(sentencia, turno, true);
            if (estadoEsperado != null) {
                sentencia.setString(11, estadoEsperado.name());
            }
            return sentencia.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("actualizar el turno", ex);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement("DELETE FROM turnos WHERE id_turno = ?")) {
            sentencia.setLong(1, id);
            return sentencia.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("eliminar el turno", ex);
        }
    }

    private void establecerDatos(PreparedStatement sentencia, Turno turno, boolean actualizacion)
            throws SQLException {
        sentencia.setLong(1, turno.getIdGato());
        sentencia.setLong(2, turno.getIdVeterinario());
        sentencia.setString(3, FechasUtil.aTexto(turno.getFechaHora()));
        sentencia.setInt(4, turno.getDuracionMinutos());
        sentencia.setString(5, turno.getMotivo());
        sentencia.setString(6, turno.getEstado().name());
        if (actualizacion) {
            sentencia.setString(7, FechasUtil.aTexto(turno.getFechaCierre()));
            sentencia.setString(8, turno.getMotivoCancelacion());
            sentencia.setString(9, turno.getObservaciones());
            sentencia.setLong(10, turno.getIdTurno());
        } else {
            sentencia.setString(7, FechasUtil.aTexto(turno.getFechaCreacion()));
            sentencia.setString(8, FechasUtil.aTexto(turno.getFechaCierre()));
            sentencia.setString(9, turno.getMotivoCancelacion());
            sentencia.setString(10, turno.getObservaciones());
        }
    }

    private List<Turno> listar(String sql, String operacion) {
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            return listar(sentencia);
        } catch (SQLException ex) {
            throw error(operacion, ex);
        }
    }

    private List<Turno> listar(PreparedStatement sentencia) throws SQLException {
        List<Turno> turnos = new ArrayList<>();
        try (ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                turnos.add(mapear(resultado));
            }
        }
        return turnos;
    }

    private Turno mapear(ResultSet resultado) throws SQLException {
        return new Turno(
                resultado.getLong("id_turno"),
                resultado.getLong("id_gato"),
                resultado.getLong("id_veterinario"),
                MapeadorResultado.fechaHora(resultado, "fecha_hora"),
                resultado.getInt("duracion_minutos"),
                resultado.getString("motivo"),
                EstadoTurno.valueOf(resultado.getString("estado")),
                MapeadorResultado.fechaHora(resultado, "fecha_creacion"),
                MapeadorResultado.fechaHora(resultado, "fecha_cierre"),
                resultado.getString("motivo_cancelacion"),
                resultado.getString("observaciones"));
    }

    private PersistenciaException error(String operacion, SQLException ex) {
        return new PersistenciaException("No fue posible " + operacion + ".", ex);
    }
}
