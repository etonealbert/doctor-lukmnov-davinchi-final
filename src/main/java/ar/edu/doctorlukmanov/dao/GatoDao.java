package ar.edu.doctorlukmanov.dao;

import ar.edu.doctorlukmanov.modelo.Gato;
import java.util.List;
import java.util.Optional;

public interface GatoDao extends DaoCrud<Gato, Long> {

    List<Gato> listarPorCliente(Long idCliente);

    Optional<Gato> buscarPorMicrochip(String numeroMicrochip);

    List<Gato> buscarPorTexto(String texto);

    List<Gato> listarActivos();
}
