package ar.edu.doctorlukmanov.dao.sqlite;

import ar.edu.doctorlukmanov.util.FechasUtil;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

final class MapeadorResultado {

    private MapeadorResultado() {
    }

    static Long longNulo(ResultSet resultado, String columna) throws SQLException {
        long valor = resultado.getLong(columna);
        return resultado.wasNull() ? null : valor;
    }

    static Integer enteroNulo(ResultSet resultado, String columna) throws SQLException {
        int valor = resultado.getInt(columna);
        return resultado.wasNull() ? null : valor;
    }

    static BigDecimal decimal(ResultSet resultado, String columna) throws SQLException {
        String valor = resultado.getString(columna);
        return valor == null ? null : new BigDecimal(valor);
    }

    static LocalDate fecha(ResultSet resultado, String columna) throws SQLException {
        return FechasUtil.parsearFecha(resultado.getString(columna));
    }

    static LocalDateTime fechaHora(ResultSet resultado, String columna) throws SQLException {
        return FechasUtil.parsearFechaHora(resultado.getString(columna));
    }
}
