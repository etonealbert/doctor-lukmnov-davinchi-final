package ar.edu.doctorlukmanov.dao;

import ar.edu.doctorlukmanov.modelo.Tratamiento;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface TratamientoDao extends DaoCrud<Tratamiento, Long> {

    Optional<Tratamiento> buscarPorNombre(String nombre);

    List<Tratamiento> listarActivos();

    Optional<Tratamiento> buscarPorId(Connection conexion, Long id);
}
