package ar.edu.doctorlukmanov.dao;

import ar.edu.doctorlukmanov.modelo.Cliente;
import java.util.List;
import java.util.Optional;

public interface ClienteDao extends DaoCrud<Cliente, Long> {

    Optional<Cliente> buscarPorDni(String dni);

    List<Cliente> buscarPorTexto(String texto);

    List<Cliente> listarActivos();
}
