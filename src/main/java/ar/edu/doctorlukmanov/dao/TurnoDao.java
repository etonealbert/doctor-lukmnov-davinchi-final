package ar.edu.doctorlukmanov.dao;

import ar.edu.doctorlukmanov.modelo.Turno;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TurnoDao extends DaoCrud<Turno, Long> {

    Optional<Turno> buscarPorId(Connection conexion, Long id);

    List<Turno> listarPorFecha(LocalDate fecha);

    boolean existeSuperposicion(
            Long idVeterinario,
            LocalDateTime fechaHora,
            int duracionMinutos,
            Long idExcluir);

    boolean actualizar(Connection conexion, Turno turno);
}
