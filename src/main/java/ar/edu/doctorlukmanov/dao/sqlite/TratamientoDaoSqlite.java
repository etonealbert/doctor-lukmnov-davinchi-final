package ar.edu.doctorlukmanov.dao.sqlite;

import ar.edu.doctorlukmanov.dao.TratamientoDao;
import ar.edu.doctorlukmanov.excepcion.PersistenciaException;
import ar.edu.doctorlukmanov.modelo.Tratamiento;
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

public final class TratamientoDaoSqlite implements TratamientoDao {

    private static final String COLUMNAS = "id_tratamiento, nombre, descripcion, precio_referencia, "
            + "activo, fecha_registro";

    private final ConexionBaseDatos baseDatos;

    public TratamientoDaoSqlite(ConexionBaseDatos baseDatos) {
        this.baseDatos = baseDatos;
    }

    @Override
    public Tratamiento crear(Tratamiento tratamiento) {
        tratamiento.validar();
        if (tratamiento.getFechaRegistro() == null) {
            tratamiento.setFechaRegistro(LocalDateTime.now());
        }
        String sql = "INSERT INTO tratamientos (nombre, descripcion, precio_referencia, activo, "
                + "fecha_registro) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, tratamiento.getNombre());
            sentencia.setString(2, tratamiento.getDescripcion());
            sentencia.setBigDecimal(3, tratamiento.getPrecioReferencia());
            sentencia.setInt(4, tratamiento.isActivo() ? 1 : 0);
            sentencia.setString(5, FechasUtil.aTexto(tratamiento.getFechaRegistro()));
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("SQLite no devolvió el identificador generado.");
                }
                tratamiento.setIdTratamiento(claves.getLong(1));
            }
            return tratamiento;
        } catch (SQLException ex) {
            throw error("crear el tratamiento", ex);
        }
    }

    @Override
    public Optional<Tratamiento> buscarPorId(Long id) {
        return buscarUno("SELECT " + COLUMNAS + " FROM tratamientos WHERE id_tratamiento = ?", id,
                "buscar el tratamiento");
    }

    @Override
    public Optional<Tratamiento> buscarPorNombre(String nombre) {
        if (nombre == null) {
            return Optional.empty();
        }
        String sql = "SELECT " + COLUMNAS + " FROM tratamientos WHERE lower(nombre) = lower(?)";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, nombre);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw error("buscar el tratamiento por nombre", ex);
        }
    }

    @Override
    public List<Tratamiento> listarTodos() {
        return listar("SELECT " + COLUMNAS + " FROM tratamientos ORDER BY nombre", "listar tratamientos");
    }

    @Override
    public List<Tratamiento> listarActivos() {
        return listar("SELECT " + COLUMNAS + " FROM tratamientos WHERE activo = 1 ORDER BY nombre",
                "listar tratamientos activos");
    }

    @Override
    public boolean actualizar(Tratamiento tratamiento) {
        tratamiento.validar();
        String sql = "UPDATE tratamientos SET nombre = ?, descripcion = ?, precio_referencia = ?, "
                + "activo = ? WHERE id_tratamiento = ?";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, tratamiento.getNombre());
            sentencia.setString(2, tratamiento.getDescripcion());
            sentencia.setBigDecimal(3, tratamiento.getPrecioReferencia());
            sentencia.setInt(4, tratamiento.isActivo() ? 1 : 0);
            sentencia.setLong(5, tratamiento.getIdTratamiento());
            return sentencia.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("actualizar el tratamiento", ex);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     "DELETE FROM tratamientos WHERE id_tratamiento = ?")) {
            sentencia.setLong(1, id);
            return sentencia.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("eliminar el tratamiento", ex);
        }
    }

    private Optional<Tratamiento> buscarUno(String sql, Long id, String operacion) {
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw error(operacion, ex);
        }
    }

    private List<Tratamiento> listar(String sql, String operacion) {
        List<Tratamiento> tratamientos = new ArrayList<>();
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                tratamientos.add(mapear(resultado));
            }
            return tratamientos;
        } catch (SQLException ex) {
            throw error(operacion, ex);
        }
    }

    private Tratamiento mapear(ResultSet resultado) throws SQLException {
        return new Tratamiento(
                resultado.getLong("id_tratamiento"),
                resultado.getString("nombre"),
                resultado.getString("descripcion"),
                MapeadorResultado.decimal(resultado, "precio_referencia"),
                resultado.getInt("activo") == 1,
                MapeadorResultado.fechaHora(resultado, "fecha_registro"));
    }

    private PersistenciaException error(String operacion, SQLException ex) {
        return new PersistenciaException("No fue posible " + operacion + ".", ex);
    }
}
