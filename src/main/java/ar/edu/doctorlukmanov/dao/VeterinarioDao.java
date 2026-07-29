package ar.edu.doctorlukmanov.dao;

import ar.edu.doctorlukmanov.modelo.Veterinario;
import java.util.List;
import java.util.Optional;

public interface VeterinarioDao extends DaoCrud<Veterinario, Long> {

    Optional<Veterinario> buscarPorMatricula(String matricula);

    List<Veterinario> listarActivos();
}
