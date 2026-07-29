package ar.edu.doctorlukmanov.dao;

import ar.edu.doctorlukmanov.modelo.Atencion;
import ar.edu.doctorlukmanov.modelo.DetalleTratamiento;
import java.sql.Connection;
import java.util.Optional;

public interface AtencionDao extends DaoCrud<Atencion, Long> {

    Optional<Atencion> buscarPorTurno(Long idTurno);

    Optional<Atencion> buscarPorTurno(Connection conexion, Long idTurno);

    Atencion crear(Connection conexion, Atencion atencion);

    void agregarTratamiento(Connection conexion, Long idAtencion, DetalleTratamiento detalle);
}
