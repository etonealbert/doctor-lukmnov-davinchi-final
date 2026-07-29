package ar.edu.doctorlukmanov.dao;

import java.util.List;
import java.util.Optional;

public interface DaoCrud<T, ID> {

    T crear(T entidad);

    Optional<T> buscarPorId(ID id);

    List<T> listarTodos();

    boolean actualizar(T entidad);

    boolean eliminar(ID id);
}
