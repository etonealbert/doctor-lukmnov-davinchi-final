package ar.edu.doctorlukmanov.servicio;

import ar.edu.doctorlukmanov.dao.ClienteDao;
import ar.edu.doctorlukmanov.dao.GatoDao;
import ar.edu.doctorlukmanov.dto.GatoFormularioDto;
import ar.edu.doctorlukmanov.excepcion.EntidadNoEncontradaException;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.modelo.Cliente;
import ar.edu.doctorlukmanov.modelo.Gato;
import ar.edu.doctorlukmanov.util.Validador;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public final class ServicioGato {

    private final GatoDao gatoDao;
    private final ClienteDao clienteDao;

    public ServicioGato(GatoDao gatoDao, ClienteDao clienteDao) {
        this.gatoDao = gatoDao;
        this.clienteDao = clienteDao;
    }

    public Gato crear(GatoFormularioDto dto) {
        Validador.requerido(dto, "datos del gato");
        verificarClienteActivo(dto.idCliente());
        String microchip = normalizarMicrochip(dto.numeroMicrochip());
        verificarMicrochipDisponible(microchip, null);
        Gato gato = construir(dto, microchip, true, LocalDateTime.now());
        gato.validar();
        return gatoDao.crear(gato);
    }

    public boolean actualizar(GatoFormularioDto dto) {
        Validador.requerido(dto, "datos del gato");
        Validador.identificadorRequerido(dto.idGato(), "gato");
        Gato actual = buscarPorId(dto.idGato());
        verificarClienteActivo(dto.idCliente());
        String microchip = normalizarMicrochip(dto.numeroMicrochip());
        verificarMicrochipDisponible(microchip, dto.idGato());
        Gato gato = construir(dto, microchip, actual.isActivo(), actual.getFechaRegistro());
        gato.validar();
        return gatoDao.actualizar(gato);
    }

    public boolean eliminar(Long idGato) {
        Gato gato = buscarPorId(idGato);
        gato.setActivo(false);
        return gatoDao.actualizar(gato);
    }

    public boolean cambiarActivo(Long idGato, boolean activo) {
        Gato gato = buscarPorId(idGato);
        if (activo) {
            verificarClienteActivo(gato.getIdCliente());
        }
        gato.setActivo(activo);
        return gatoDao.actualizar(gato);
    }

    public Gato buscarPorId(Long idGato) {
        Validador.identificadorRequerido(idGato, "gato");
        return gatoDao.buscarPorId(idGato)
                .orElseThrow(() -> new EntidadNoEncontradaException("gato", idGato));
    }

    public List<Gato> listarTodos() {
        return gatoDao.listarTodos();
    }

    public List<Gato> listarActivos() {
        return gatoDao.listarActivos();
    }

    public List<Gato> listarPorCliente(Long idCliente) {
        Validador.identificadorRequerido(idCliente, "cliente");
        return gatoDao.listarPorCliente(idCliente);
    }

    public List<Gato> buscar(String texto) {
        return gatoDao.buscarPorTexto(texto);
    }

    private Gato construir(
            GatoFormularioDto dto, String microchip, boolean activo, LocalDateTime fechaRegistro) {
        return new Gato(
                dto.idGato(),
                dto.idCliente(),
                Validador.textoRequerido(dto.nombre(), "nombre"),
                dto.fechaNacimiento(),
                dto.sexo(),
                Validador.textoOpcional(dto.raza()),
                Validador.textoOpcional(dto.color()),
                dto.pesoActual(),
                microchip,
                dto.esterilizado(),
                Validador.textoOpcional(dto.alergias()),
                Validador.textoOpcional(dto.observaciones()),
                activo,
                fechaRegistro);
    }

    private void verificarClienteActivo(Long idCliente) {
        Validador.identificadorRequerido(idCliente, "cliente responsable");
        Cliente cliente = clienteDao.buscarPorId(idCliente)
                .orElseThrow(() -> new EntidadNoEncontradaException("cliente", idCliente));
        if (!cliente.isActivo()) {
            throw new ValidacionException("El cliente responsable está inactivo.");
        }
    }

    private void verificarMicrochipDisponible(String microchip, Long idActual) {
        if (microchip == null) {
            return;
        }
        gatoDao.buscarPorMicrochip(microchip)
                .filter(encontrado -> !encontrado.getIdGato().equals(idActual))
                .ifPresent(encontrado -> {
                    throw new ValidacionException("Ya existe un gato con ese número de microchip.");
                });
    }

    private String normalizarMicrochip(String valor) {
        String normalizado = Validador.textoOpcional(valor);
        return normalizado == null ? null : normalizado.toUpperCase(Locale.ROOT);
    }
}
