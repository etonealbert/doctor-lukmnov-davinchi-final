package ar.edu.doctorlukmanov.dao.sqlite;

import ar.edu.doctorlukmanov.dao.VeterinarioDao;
import ar.edu.doctorlukmanov.excepcion.PersistenciaException;
import ar.edu.doctorlukmanov.modelo.Veterinario;
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

public final class VeterinarioDaoSqlite implements VeterinarioDao {

    private static final String COLUMNAS = "id_veterinario, nombre, apellido, matricula, telefono, "
            + "correo_electronico, especialidad, activo, fecha_registro";

    private final ConexionBaseDatos baseDatos;

    public VeterinarioDaoSqlite(ConexionBaseDatos baseDatos) {
        this.baseDatos = baseDatos;
    }

    @Override
    public Veterinario crear(Veterinario veterinario) {
        veterinario.validar();
        if (veterinario.getFechaRegistro() == null) {
            veterinario.setFechaRegistro(LocalDateTime.now());
        }
        String sql = "INSERT INTO veterinarios (nombre, apellido, matricula, telefono, "
                + "correo_electronico, especialidad, activo, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, veterinario.getNombre());
            sentencia.setString(2, veterinario.getApellido());
            sentencia.setString(3, veterinario.getMatricula());
            sentencia.setString(4, veterinario.getTelefono());
            sentencia.setString(5, veterinario.getCorreoElectronico());
            sentencia.setString(6, veterinario.getEspecialidad());
            sentencia.setInt(7, veterinario.isActivo() ? 1 : 0);
            sentencia.setString(8, FechasUtil.aTexto(veterinario.getFechaRegistro()));
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("SQLite no devolvió el identificador generado.");
                }
                veterinario.setIdVeterinario(claves.getLong(1));
            }
            return veterinario;
        } catch (SQLException ex) {
            throw error("crear el veterinario", ex);
        }
    }

    @Override
    public Optional<Veterinario> buscarPorId(Long id) {
        String sql = "SELECT " + COLUMNAS + " FROM veterinarios WHERE id_veterinario = ?";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw error("buscar el veterinario", ex);
        }
    }

    @Override
    public Optional<Veterinario> buscarPorMatricula(String matricula) {
        if (matricula == null) {
            return Optional.empty();
        }
        String sql = "SELECT " + COLUMNAS + " FROM veterinarios WHERE lower(matricula) = lower(?)";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, matricula);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw error("buscar el veterinario por matrícula", ex);
        }
    }

    @Override
    public List<Veterinario> listarTodos() {
        return listar("SELECT " + COLUMNAS + " FROM veterinarios ORDER BY apellido, nombre",
                "listar los veterinarios");
    }

    @Override
    public List<Veterinario> listarActivos() {
        return listar("SELECT " + COLUMNAS
                + " FROM veterinarios WHERE activo = 1 ORDER BY apellido, nombre",
                "listar los veterinarios activos");
    }

    @Override
    public boolean actualizar(Veterinario veterinario) {
        veterinario.validar();
        String sql = "UPDATE veterinarios SET nombre = ?, apellido = ?, matricula = ?, telefono = ?, "
                + "correo_electronico = ?, especialidad = ?, activo = ?, "
                + "fecha_actualizacion = CURRENT_TIMESTAMP WHERE id_veterinario = ?";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, veterinario.getNombre());
            sentencia.setString(2, veterinario.getApellido());
            sentencia.setString(3, veterinario.getMatricula());
            sentencia.setString(4, veterinario.getTelefono());
            sentencia.setString(5, veterinario.getCorreoElectronico());
            sentencia.setString(6, veterinario.getEspecialidad());
            sentencia.setInt(7, veterinario.isActivo() ? 1 : 0);
            sentencia.setLong(8, veterinario.getIdVeterinario());
            return sentencia.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("actualizar el veterinario", ex);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     "DELETE FROM veterinarios WHERE id_veterinario = ?")) {
            sentencia.setLong(1, id);
            return sentencia.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("eliminar el veterinario", ex);
        }
    }

    private List<Veterinario> listar(String sql, String operacion) {
        List<Veterinario> veterinarios = new ArrayList<>();
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                veterinarios.add(mapear(resultado));
            }
            return veterinarios;
        } catch (SQLException ex) {
            throw error(operacion, ex);
        }
    }

    private Veterinario mapear(ResultSet resultado) throws SQLException {
        return new Veterinario(
                resultado.getLong("id_veterinario"),
                resultado.getString("nombre"),
                resultado.getString("apellido"),
                resultado.getString("telefono"),
                resultado.getString("correo_electronico"),
                resultado.getInt("activo") == 1,
                resultado.getString("matricula"),
                resultado.getString("especialidad"),
                MapeadorResultado.fechaHora(resultado, "fecha_registro"));
    }

    private PersistenciaException error(String operacion, SQLException ex) {
        return new PersistenciaException("No fue posible " + operacion + ".", ex);
    }
}
