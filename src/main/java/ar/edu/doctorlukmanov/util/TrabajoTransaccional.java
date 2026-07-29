package ar.edu.doctorlukmanov.util;

import java.sql.Connection;

@FunctionalInterface
public interface TrabajoTransaccional<R> {

    R ejecutar(Connection conexion) throws Exception;
}
