package ar.edu.doctorlukmanov.dao.sqlite;

import ar.edu.doctorlukmanov.dao.ReporteDao;
import ar.edu.doctorlukmanov.dto.HistoriaClinicaDto;
import ar.edu.doctorlukmanov.dto.ReportePacientesMesDto;
import ar.edu.doctorlukmanov.dto.ReporteProductividadVeterinarioDto;
import ar.edu.doctorlukmanov.dto.ReporteTratamientoFrecuenteDto;
import ar.edu.doctorlukmanov.dto.ReporteTurnosMensualesDto;
import ar.edu.doctorlukmanov.excepcion.PersistenciaException;
import ar.edu.doctorlukmanov.modelo.EstadoTurno;
import ar.edu.doctorlukmanov.util.ConexionBaseDatos;
import ar.edu.doctorlukmanov.util.FechasUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ReporteDaoSqlite implements ReporteDao {

    private final ConexionBaseDatos baseDatos;

    public ReporteDaoSqlite(ConexionBaseDatos baseDatos) {
        this.baseDatos = baseDatos;
    }

    @Override
    public List<ReportePacientesMesDto> pacientesPorMes(LocalDate desde, LocalDate hasta) {
        String sql = "SELECT strftime('%Y-%m', fecha_registro) AS mes, COUNT(*) AS cantidad "
                + "FROM gatos WHERE datetime(fecha_registro) >= datetime(?) "
                + "AND datetime(fecha_registro) < datetime(?) "
                + "GROUP BY strftime('%Y-%m', fecha_registro) ORDER BY mes";
        List<ReportePacientesMesDto> filas = new ArrayList<>();
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            establecerRango(sentencia, desde, hasta, 1);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    filas.add(new ReportePacientesMesDto(
                            YearMonth.parse(resultado.getString("mes")), resultado.getLong("cantidad")));
                }
            }
            return filas;
        } catch (SQLException ex) {
            throw error("generar el reporte de pacientes por mes", ex);
        }
    }

    @Override
    public List<ReporteTurnosMensualesDto> turnosPorMes(LocalDate desde, LocalDate hasta) {
        String sql = "SELECT strftime('%Y-%m', fecha_hora) AS mes, estado, COUNT(*) AS cantidad "
                + "FROM turnos WHERE datetime(fecha_hora) >= datetime(?) "
                + "AND datetime(fecha_hora) < datetime(?) "
                + "GROUP BY strftime('%Y-%m', fecha_hora), estado ORDER BY mes, estado";
        Map<YearMonth, long[]> totales = new LinkedHashMap<>();
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            establecerRango(sentencia, desde, hasta, 1);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    YearMonth mes = YearMonth.parse(resultado.getString("mes"));
                    EstadoTurno estado = EstadoTurno.valueOf(resultado.getString("estado"));
                    totales.computeIfAbsent(mes, ignorado -> new long[4])[estado.ordinal()]
                            = resultado.getLong("cantidad");
                }
            }
        } catch (SQLException ex) {
            throw error("generar el reporte de turnos por mes", ex);
        }

        List<ReporteTurnosMensualesDto> filas = new ArrayList<>();
        totales.forEach((mes, estados) -> filas.add(new ReporteTurnosMensualesDto(
                mes, estados[EstadoTurno.PROGRAMADO.ordinal()], estados[EstadoTurno.CONFIRMADO.ordinal()],
                estados[EstadoTurno.COMPLETADO.ordinal()], estados[EstadoTurno.CANCELADO.ordinal()])));
        return filas;
    }

    @Override
    public List<ReporteTratamientoFrecuenteDto> tratamientosFrecuentes(
            LocalDate desde, LocalDate hasta) {
        String sql = "SELECT tr.id_tratamiento, tr.nombre, COUNT(*) AS aplicaciones, "
                + "SUM(det.cantidad) AS cantidad_total FROM atencion_tratamientos det "
                + "JOIN tratamientos tr ON tr.id_tratamiento = det.id_tratamiento "
                + "JOIN atenciones a ON a.id_atencion = det.id_atencion "
                + "JOIN turnos t ON t.id_turno = a.id_turno "
                + "WHERE t.estado = 'COMPLETADO' AND datetime(t.fecha_hora) >= datetime(?) "
                + "AND datetime(t.fecha_hora) < datetime(?) "
                + "GROUP BY tr.id_tratamiento, tr.nombre "
                + "ORDER BY aplicaciones DESC, tr.nombre ASC";
        List<TratamientoAgregado> agregados = new ArrayList<>();
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            establecerRango(sentencia, desde, hasta, 1);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    agregados.add(new TratamientoAgregado(
                            resultado.getLong("id_tratamiento"),
                            resultado.getString("nombre"),
                            resultado.getLong("aplicaciones"),
                            MapeadorResultado.decimal(resultado, "cantidad_total")));
                }
            }
        } catch (SQLException ex) {
            throw error("generar el reporte de tratamientos frecuentes", ex);
        }

        long totalAplicaciones = agregados.stream().mapToLong(TratamientoAgregado::aplicaciones).sum();
        return agregados.stream()
                .map(fila -> new ReporteTratamientoFrecuenteDto(
                        fila.id(),
                        fila.nombre(),
                        fila.aplicaciones(),
                        fila.cantidadTotal(),
                        totalAplicaciones == 0 ? 0 : fila.aplicaciones() * 100.0 / totalAplicaciones))
                .toList();
    }

    @Override
    public List<ReporteProductividadVeterinarioDto> productividadVeterinarios(
            LocalDate desde, LocalDate hasta, Long idVeterinario) {
        String sql = "SELECT v.id_veterinario, v.nombre || ' ' || v.apellido AS veterinario, "
                + "COUNT(t.id_turno) AS total, "
                + "SUM(CASE WHEN t.estado = 'COMPLETADO' THEN 1 ELSE 0 END) AS completados, "
                + "SUM(CASE WHEN t.estado = 'CANCELADO' THEN 1 ELSE 0 END) AS cancelados "
                + "FROM veterinarios v LEFT JOIN turnos t ON t.id_veterinario = v.id_veterinario "
                + "AND datetime(t.fecha_hora) >= datetime(?) AND datetime(t.fecha_hora) < datetime(?) "
                + "WHERE (? IS NULL OR v.id_veterinario = ?) "
                + "GROUP BY v.id_veterinario, v.nombre, v.apellido "
                + "ORDER BY completados DESC, veterinario";
        List<ReporteProductividadVeterinarioDto> filas = new ArrayList<>();
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            establecerRango(sentencia, desde, hasta, 1);
            if (idVeterinario == null) {
                sentencia.setNull(3, Types.BIGINT);
                sentencia.setNull(4, Types.BIGINT);
            } else {
                sentencia.setLong(3, idVeterinario);
                sentencia.setLong(4, idVeterinario);
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    long total = resultado.getLong("total");
                    long completados = resultado.getLong("completados");
                    filas.add(new ReporteProductividadVeterinarioDto(
                            resultado.getLong("id_veterinario"),
                            resultado.getString("veterinario"),
                            total,
                            completados,
                            resultado.getLong("cancelados"),
                            total == 0 ? 0 : completados * 100.0 / total));
                }
            }
            return filas;
        } catch (SQLException ex) {
            throw error("generar el reporte de productividad veterinaria", ex);
        }
    }

    @Override
    public List<HistoriaClinicaDto> historiaClinica(Long idGato) {
        String sql = "SELECT g.id_gato, g.nombre AS gato, "
                + "c.nombre || ' ' || c.apellido AS cliente, t.fecha_hora, "
                + "v.nombre || ' ' || v.apellido AS veterinario, t.motivo, a.diagnostico, "
                + "a.peso_registrado, a.temperatura, a.indicaciones, "
                + "COALESCE(GROUP_CONCAT(tr.nombre, ', '), '') AS tratamientos "
                + "FROM atenciones a JOIN turnos t ON t.id_turno = a.id_turno "
                + "JOIN gatos g ON g.id_gato = t.id_gato "
                + "JOIN clientes c ON c.id_cliente = g.id_cliente "
                + "JOIN veterinarios v ON v.id_veterinario = t.id_veterinario "
                + "LEFT JOIN atencion_tratamientos det ON det.id_atencion = a.id_atencion "
                + "LEFT JOIN tratamientos tr ON tr.id_tratamiento = det.id_tratamiento "
                + "WHERE g.id_gato = ? GROUP BY a.id_atencion, g.id_gato, g.nombre, c.nombre, "
                + "c.apellido, t.fecha_hora, v.nombre, v.apellido, t.motivo, a.diagnostico, "
                + "a.peso_registrado, a.temperatura, a.indicaciones ORDER BY t.fecha_hora DESC";
        List<HistoriaClinicaDto> filas = new ArrayList<>();
        try (Connection conexion = baseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idGato);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    filas.add(new HistoriaClinicaDto(
                            resultado.getLong("id_gato"),
                            resultado.getString("gato"),
                            resultado.getString("cliente"),
                            MapeadorResultado.fechaHora(resultado, "fecha_hora"),
                            resultado.getString("veterinario"),
                            resultado.getString("motivo"),
                            resultado.getString("diagnostico"),
                            MapeadorResultado.decimal(resultado, "peso_registrado"),
                            MapeadorResultado.decimal(resultado, "temperatura"),
                            resultado.getString("tratamientos"),
                            resultado.getString("indicaciones")));
                }
            }
            return filas;
        } catch (SQLException ex) {
            throw error("generar la historia clínica", ex);
        }
    }

    private void establecerRango(
            PreparedStatement sentencia, LocalDate desde, LocalDate hasta, int indiceInicial)
            throws SQLException {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime finExclusivo = hasta.plusDays(1).atStartOfDay();
        sentencia.setString(indiceInicial, FechasUtil.aTexto(inicio));
        sentencia.setString(indiceInicial + 1, FechasUtil.aTexto(finExclusivo));
    }

    private PersistenciaException error(String operacion, SQLException ex) {
        return new PersistenciaException("No fue posible " + operacion + ".", ex);
    }

    private record TratamientoAgregado(
            Long id, String nombre, long aplicaciones, BigDecimal cantidadTotal) {
    }
}
