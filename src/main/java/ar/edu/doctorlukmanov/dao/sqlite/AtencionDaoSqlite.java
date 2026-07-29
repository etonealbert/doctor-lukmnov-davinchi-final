package ar.edu.doctorlukmanov.dao.sqlite;

import ar.edu.doctorlukmanov.dao.AtencionDao;
import ar.edu.doctorlukmanov.excepcion.PersistenciaException;
import ar.edu.doctorlukmanov.modelo.Atencion;
import ar.edu.doctorlukmanov.modelo.DetalleTratamiento;
import ar.edu.doctorlukmanov.util.ConexionBaseDatos;
import ar.edu.doctorlukmanov.util.FechasUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AtencionDaoSqlite implements AtencionDao {

    private static final String COLUMNAS = "id_atencion, id_turno, diagnostico, peso_registrado, "
            + "temperatura, observaciones_clinicas, indicaciones, fecha_registro";

    private final ConexionBaseDatos baseDatos;

    public AtencionDaoSqlite(ConexionBaseDatos baseDatos) {
        this.baseDatos = baseDatos;
    }

    @Override
    public Atencion crear(Atencion atencion) {
        return baseDatos.ejecutarEnTransaccion(conexion -> {
            crear(conexion, atencion);
            for (DetalleTratamiento detalle : atencion.getTratamientos()) {
                agregarTratamiento(conexion, atencion.getIdAtencion(), detalle);
            }
            return atencion;
        });
    }

    @Override
    public Atencion crear(Connection conexion, Atencion atencion) {
        atencion.validar();
        String sql = "INSERT INTO atenciones (id_turno, diagnostico, peso_registrado, temperatura, "
                + "observaciones_clinicas, indicaciones, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setLong(1, atencion.getIdTurno());
            sentencia.setString(2, atencion.getDiagnostico());
            sentencia.setBigDecimal(3, atencion.getPesoRegistrado());
            sentencia.setBigDecimal(4, atencion.getTemperatura());
            sentencia.setString(5, atencion.getObservacionesClinicas());
            sentencia.setString(6, atencion.getIndicaciones());
            sentencia.setString(7, FechasUtil.aTexto(atencion.getFechaRegistro()));
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("SQLite no devolvió el identificador generado.");
                }
                atencion.setIdAtencion(claves.getLong(1));
            }
            return atencion;
        } catch (SQLException ex) {
            throw error("crear la atención", ex);
        }
    }

    @Override
    public void agregarTratamiento(
            Connection conexion, Long idAtencion, DetalleTratamiento detalle) {
        detalle.validar();
        String sql = "INSERT INTO atencion_tratamientos (id_atencion, id_tratamiento, dosis, "
                + "frecuencia, duracion_dias, observaciones, cantidad, precio_aplicado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idAtencion);
            sentencia.setLong(2, detalle.getIdTratamiento());
            sentencia.setString(3, detalle.getDosis());
            sentencia.setString(4, detalle.getFrecuencia());
            if (detalle.getDuracionDias() == null) {
                sentencia.setObject(5, null);
            } else {
                sentencia.setInt(5, detalle.getDuracionDias());
            }
            sentencia.setString(6, detalle.getObservaciones());
            sentencia.setBigDecimal(7, detalle.getCantidad());
            sentencia.setBigDecimal(8, detalle.getPrecioAplicado());
            sentencia.executeUpdate();
        } catch (SQLException ex) {
            throw error("agregar el tratamiento a la atención", ex);
        }
    }

    @Override
    public Optional<Atencion> buscarPorId(Long id) {
        try (Connection conexion = baseDatos.obtenerConexion()) {
            return buscarUno(conexion, "id_atencion", id);
        } catch (SQLException ex) {
            throw error("buscar la atención", ex);
        }
    }

    @Override
    public Optional<Atencion> buscarPorTurno(Long idTurno) {
        try (Connection conexion = baseDatos.obtenerConexion()) {
            return buscarPorTurno(conexion, idTurno);
        } catch (SQLException ex) {
            throw error("buscar la atención del turno", ex);
        }
    }

    @Override
    public Optional<Atencion> buscarPorTurno(Connection conexion, Long idTurno) {
        return buscarUno(conexion, "id_turno", idTurno);
    }

    @Override
    public List<Atencion> listarTodos() {
        List<Atencion> atenciones = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM atenciones ORDER BY fecha_registro DESC";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                Atencion atencion = mapear(resultado);
                cargarTratamientos(conexion, atencion);
                atenciones.add(atencion);
            }
            return atenciones;
        } catch (SQLException ex) {
            throw error("listar las atenciones", ex);
        }
    }

    @Override
    public boolean actualizar(Atencion atencion) {
        atencion.validar();
        String sql = "UPDATE atenciones SET diagnostico = ?, peso_registrado = ?, temperatura = ?, "
                + "observaciones_clinicas = ?, indicaciones = ? WHERE id_atencion = ?";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, atencion.getDiagnostico());
            sentencia.setBigDecimal(2, atencion.getPesoRegistrado());
            sentencia.setBigDecimal(3, atencion.getTemperatura());
            sentencia.setString(4, atencion.getObservacionesClinicas());
            sentencia.setString(5, atencion.getIndicaciones());
            sentencia.setLong(6, atencion.getIdAtencion());
            return sentencia.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("actualizar la atención", ex);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     "DELETE FROM atenciones WHERE id_atencion = ?")) {
            sentencia.setLong(1, id);
            return sentencia.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("eliminar la atención", ex);
        }
    }

    private Optional<Atencion> buscarUno(Connection conexion, String columna, Long valor) {
        String sql = "SELECT " + COLUMNAS + " FROM atenciones WHERE " + columna + " = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, valor);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }
                Atencion atencion = mapear(resultado);
                cargarTratamientos(conexion, atencion);
                return Optional.of(atencion);
            }
        } catch (SQLException ex) {
            throw error("buscar la atención", ex);
        }
    }

    private void cargarTratamientos(Connection conexion, Atencion atencion) throws SQLException {
        String sql = "SELECT id_tratamiento, dosis, frecuencia, duracion_dias, observaciones, "
                + "cantidad, precio_aplicado FROM atencion_tratamientos WHERE id_atencion = ? "
                + "ORDER BY id_atencion_tratamiento";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, atencion.getIdAtencion());
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    atencion.agregarTratamiento(new DetalleTratamiento(
                            resultado.getLong("id_tratamiento"),
                            resultado.getString("dosis"),
                            resultado.getString("frecuencia"),
                            MapeadorResultado.enteroNulo(resultado, "duracion_dias"),
                            resultado.getString("observaciones"),
                            MapeadorResultado.decimal(resultado, "cantidad"),
                            MapeadorResultado.decimal(resultado, "precio_aplicado")));
                }
            }
        }
    }

    private Atencion mapear(ResultSet resultado) throws SQLException {
        LocalDateTime fechaRegistro = MapeadorResultado.fechaHora(resultado, "fecha_registro");
        return new Atencion(
                resultado.getLong("id_atencion"),
                resultado.getLong("id_turno"),
                resultado.getString("diagnostico"),
                MapeadorResultado.decimal(resultado, "peso_registrado"),
                MapeadorResultado.decimal(resultado, "temperatura"),
                resultado.getString("observaciones_clinicas"),
                resultado.getString("indicaciones"),
                fechaRegistro);
    }

    private PersistenciaException error(String operacion, SQLException ex) {
        return new PersistenciaException("No fue posible " + operacion + ".", ex);
    }
}
