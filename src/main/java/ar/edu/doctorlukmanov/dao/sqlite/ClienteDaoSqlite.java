package ar.edu.doctorlukmanov.dao.sqlite;

import ar.edu.doctorlukmanov.dao.ClienteDao;
import ar.edu.doctorlukmanov.excepcion.PersistenciaException;
import ar.edu.doctorlukmanov.modelo.Cliente;
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

public final class ClienteDaoSqlite implements ClienteDao {

    private static final String COLUMNAS = "id_cliente, nombre, apellido, dni, telefono, "
            + "correo_electronico, direccion, activo, fecha_registro";

    private final ConexionBaseDatos baseDatos;

    public ClienteDaoSqlite(ConexionBaseDatos baseDatos) {
        this.baseDatos = baseDatos;
    }

    @Override
    public Cliente crear(Cliente cliente) {
        cliente.validar();
        if (cliente.getFechaRegistro() == null) {
            cliente.setFechaRegistro(LocalDateTime.now());
        }
        String sql = "INSERT INTO clientes (nombre, apellido, dni, telefono, correo_electronico, "
                + "direccion, activo, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, cliente.getNombre());
            sentencia.setString(2, cliente.getApellido());
            sentencia.setString(3, cliente.getDni());
            sentencia.setString(4, cliente.getTelefono());
            sentencia.setString(5, cliente.getCorreoElectronico());
            sentencia.setString(6, cliente.getDireccion());
            sentencia.setInt(7, cliente.isActivo() ? 1 : 0);
            sentencia.setString(8, FechasUtil.aTexto(cliente.getFechaRegistro()));
            sentencia.executeUpdate();
            asignarId(cliente, sentencia);
            return cliente;
        } catch (SQLException ex) {
            throw error("crear el cliente", ex);
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        String sql = "SELECT " + COLUMNAS + " FROM clientes WHERE id_cliente = ?";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw error("buscar el cliente", ex);
        }
    }

    @Override
    public Optional<Cliente> buscarPorDni(String dni) {
        if (dni == null) {
            return Optional.empty();
        }
        String sql = "SELECT " + COLUMNAS + " FROM clientes WHERE lower(dni) = lower(?)";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, dni);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw error("buscar el cliente por DNI", ex);
        }
    }

    @Override
    public List<Cliente> buscarPorTexto(String texto) {
        String sql = "SELECT " + COLUMNAS + " FROM clientes "
                + "WHERE lower(nombre) LIKE lower(?) OR lower(apellido) LIKE lower(?) "
                + "OR lower(dni) LIKE lower(?) ORDER BY apellido, nombre";
        String patron = "%" + (texto == null ? "" : texto.trim()) + "%";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, patron);
            sentencia.setString(2, patron);
            sentencia.setString(3, patron);
            return listar(sentencia);
        } catch (SQLException ex) {
            throw error("buscar clientes", ex);
        }
    }

    @Override
    public List<Cliente> listarTodos() {
        return listarConSql("SELECT " + COLUMNAS + " FROM clientes ORDER BY apellido, nombre",
                "listar los clientes");
    }

    @Override
    public List<Cliente> listarActivos() {
        return listarConSql("SELECT " + COLUMNAS
                + " FROM clientes WHERE activo = 1 ORDER BY apellido, nombre", "listar clientes activos");
    }

    @Override
    public boolean actualizar(Cliente cliente) {
        cliente.validar();
        String sql = "UPDATE clientes SET nombre = ?, apellido = ?, dni = ?, telefono = ?, "
                + "correo_electronico = ?, direccion = ?, activo = ?, "
                + "fecha_actualizacion = CURRENT_TIMESTAMP WHERE id_cliente = ?";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, cliente.getNombre());
            sentencia.setString(2, cliente.getApellido());
            sentencia.setString(3, cliente.getDni());
            sentencia.setString(4, cliente.getTelefono());
            sentencia.setString(5, cliente.getCorreoElectronico());
            sentencia.setString(6, cliente.getDireccion());
            sentencia.setInt(7, cliente.isActivo() ? 1 : 0);
            sentencia.setLong(8, cliente.getIdCliente());
            return sentencia.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("actualizar el cliente", ex);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     "DELETE FROM clientes WHERE id_cliente = ?")) {
            sentencia.setLong(1, id);
            return sentencia.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("eliminar el cliente", ex);
        }
    }

    private List<Cliente> listarConSql(String sql, String operacion) {
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            return listar(sentencia);
        } catch (SQLException ex) {
            throw error(operacion, ex);
        }
    }

    private List<Cliente> listar(PreparedStatement sentencia) throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        try (ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                clientes.add(mapear(resultado));
            }
        }
        return clientes;
    }

    private Cliente mapear(ResultSet resultado) throws SQLException {
        return new Cliente(
                resultado.getLong("id_cliente"),
                resultado.getString("nombre"),
                resultado.getString("apellido"),
                resultado.getString("telefono"),
                resultado.getString("correo_electronico"),
                resultado.getInt("activo") == 1,
                resultado.getString("dni"),
                resultado.getString("direccion"),
                MapeadorResultado.fechaHora(resultado, "fecha_registro"));
    }

    private void asignarId(Cliente cliente, PreparedStatement sentencia) throws SQLException {
        try (ResultSet claves = sentencia.getGeneratedKeys()) {
            if (!claves.next()) {
                throw new SQLException("SQLite no devolvió el identificador generado.");
            }
            cliente.setIdCliente(claves.getLong(1));
        }
    }

    private PersistenciaException error(String operacion, SQLException ex) {
        return new PersistenciaException("No fue posible " + operacion + ".", ex);
    }
}
