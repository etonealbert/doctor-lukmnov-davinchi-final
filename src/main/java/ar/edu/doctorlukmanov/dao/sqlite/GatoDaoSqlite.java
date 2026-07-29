package ar.edu.doctorlukmanov.dao.sqlite;

import ar.edu.doctorlukmanov.dao.GatoDao;
import ar.edu.doctorlukmanov.excepcion.PersistenciaException;
import ar.edu.doctorlukmanov.modelo.Gato;
import ar.edu.doctorlukmanov.modelo.SexoGato;
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

public final class GatoDaoSqlite implements GatoDao {

    private static final String COLUMNAS = "id_gato, id_cliente, nombre, fecha_nacimiento, sexo, raza, "
            + "color, peso_actual, numero_microchip, esterilizado, alergias, observaciones, activo, "
            + "fecha_registro";

    private final ConexionBaseDatos baseDatos;

    public GatoDaoSqlite(ConexionBaseDatos baseDatos) {
        this.baseDatos = baseDatos;
    }

    @Override
    public Gato crear(Gato gato) {
        gato.validar();
        if (gato.getFechaRegistro() == null) {
            gato.setFechaRegistro(LocalDateTime.now());
        }
        String sql = "INSERT INTO gatos (id_cliente, nombre, fecha_nacimiento, sexo, raza, color, "
                + "peso_actual, numero_microchip, esterilizado, alergias, observaciones, activo, "
                + "fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            establecerParametros(sentencia, gato, false);
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("SQLite no devolvió el identificador generado.");
                }
                gato.setIdGato(claves.getLong(1));
            }
            return gato;
        } catch (SQLException ex) {
            throw error("crear el gato", ex);
        }
    }

    @Override
    public Optional<Gato> buscarPorId(Long id) {
        return buscarUno("SELECT " + COLUMNAS + " FROM gatos WHERE id_gato = ?", id,
                "buscar el gato");
    }

    @Override
    public Optional<Gato> buscarPorMicrochip(String numeroMicrochip) {
        if (numeroMicrochip == null) {
            return Optional.empty();
        }
        String sql = "SELECT " + COLUMNAS + " FROM gatos WHERE lower(numero_microchip) = lower(?)";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, numeroMicrochip);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw error("buscar el gato por microchip", ex);
        }
    }

    @Override
    public List<Gato> listarTodos() {
        return listarConSql("SELECT " + COLUMNAS + " FROM gatos ORDER BY nombre", "listar los gatos");
    }

    @Override
    public List<Gato> listarActivos() {
        return listarConSql("SELECT " + COLUMNAS + " FROM gatos WHERE activo = 1 ORDER BY nombre",
                "listar los gatos activos");
    }

    @Override
    public List<Gato> listarPorCliente(Long idCliente) {
        String sql = "SELECT " + COLUMNAS + " FROM gatos WHERE id_cliente = ? ORDER BY nombre";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idCliente);
            return listar(sentencia);
        } catch (SQLException ex) {
            throw error("listar los gatos del cliente", ex);
        }
    }

    @Override
    public List<Gato> buscarPorTexto(String texto) {
        String sql = "SELECT " + COLUMNAS + " FROM gatos WHERE lower(nombre) LIKE lower(?) "
                + "OR lower(COALESCE(numero_microchip, '')) LIKE lower(?) ORDER BY nombre";
        String patron = "%" + (texto == null ? "" : texto.trim()) + "%";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, patron);
            sentencia.setString(2, patron);
            return listar(sentencia);
        } catch (SQLException ex) {
            throw error("buscar gatos", ex);
        }
    }

    @Override
    public boolean actualizar(Gato gato) {
        gato.validar();
        String sql = "UPDATE gatos SET id_cliente = ?, nombre = ?, fecha_nacimiento = ?, sexo = ?, "
                + "raza = ?, color = ?, peso_actual = ?, numero_microchip = ?, esterilizado = ?, "
                + "alergias = ?, observaciones = ?, activo = ?, fecha_actualizacion = CURRENT_TIMESTAMP "
                + "WHERE id_gato = ?";
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            establecerParametros(sentencia, gato, true);
            return sentencia.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("actualizar el gato", ex);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement("DELETE FROM gatos WHERE id_gato = ?")) {
            sentencia.setLong(1, id);
            return sentencia.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("eliminar el gato", ex);
        }
    }

    private void establecerParametros(PreparedStatement sentencia, Gato gato, boolean actualizacion)
            throws SQLException {
        sentencia.setLong(1, gato.getIdCliente());
        sentencia.setString(2, gato.getNombre());
        sentencia.setString(3, FechasUtil.aTexto(gato.getFechaNacimiento()));
        sentencia.setString(4, gato.getSexo().name());
        sentencia.setString(5, gato.getRaza());
        sentencia.setString(6, gato.getColor());
        sentencia.setBigDecimal(7, gato.getPesoActual());
        sentencia.setString(8, gato.getNumeroMicrochip());
        sentencia.setInt(9, gato.isEsterilizado() ? 1 : 0);
        sentencia.setString(10, gato.getAlergias());
        sentencia.setString(11, gato.getObservaciones());
        sentencia.setInt(12, gato.isActivo() ? 1 : 0);
        if (actualizacion) {
            sentencia.setLong(13, gato.getIdGato());
        } else {
            sentencia.setString(13, FechasUtil.aTexto(gato.getFechaRegistro()));
        }
    }

    private Optional<Gato> buscarUno(String sql, Long id, String operacion) {
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

    private List<Gato> listarConSql(String sql, String operacion) {
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            return listar(sentencia);
        } catch (SQLException ex) {
            throw error(operacion, ex);
        }
    }

    private List<Gato> listar(PreparedStatement sentencia) throws SQLException {
        List<Gato> gatos = new ArrayList<>();
        try (ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                gatos.add(mapear(resultado));
            }
        }
        return gatos;
    }

    private Gato mapear(ResultSet resultado) throws SQLException {
        return new Gato(
                resultado.getLong("id_gato"),
                resultado.getLong("id_cliente"),
                resultado.getString("nombre"),
                MapeadorResultado.fecha(resultado, "fecha_nacimiento"),
                SexoGato.valueOf(resultado.getString("sexo")),
                resultado.getString("raza"),
                resultado.getString("color"),
                MapeadorResultado.decimal(resultado, "peso_actual"),
                resultado.getString("numero_microchip"),
                resultado.getInt("esterilizado") == 1,
                resultado.getString("alergias"),
                resultado.getString("observaciones"),
                resultado.getInt("activo") == 1,
                MapeadorResultado.fechaHora(resultado, "fecha_registro"));
    }

    private PersistenciaException error(String operacion, SQLException ex) {
        return new PersistenciaException("No fue posible " + operacion + ".", ex);
    }
}
