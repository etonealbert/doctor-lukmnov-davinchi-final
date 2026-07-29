package ar.edu.doctorlukmanov.servicio;

import ar.edu.doctorlukmanov.dao.ClienteDao;
import ar.edu.doctorlukmanov.dto.ClienteFormularioDto;
import ar.edu.doctorlukmanov.excepcion.EntidadNoEncontradaException;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.modelo.Cliente;
import ar.edu.doctorlukmanov.util.Validador;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public final class ServicioCliente {

    private final ClienteDao clienteDao;

    public ServicioCliente(ClienteDao clienteDao) {
        this.clienteDao = clienteDao;
    }

    public Cliente crear(ClienteFormularioDto dto) {
        Validador.requerido(dto, "datos del cliente");
        String dni = normalizarCodigo(dto.dni());
        if (clienteDao.buscarPorDni(dni).isPresent()) {
            throw new ValidacionException("Ya existe un cliente con ese DNI.");
        }
        Cliente cliente = construir(dto, dni, true, LocalDateTime.now());
        cliente.validar();
        return clienteDao.crear(cliente);
    }

    public boolean actualizar(ClienteFormularioDto dto) {
        Validador.requerido(dto, "datos del cliente");
        Validador.identificadorRequerido(dto.idCliente(), "cliente");
        Cliente actual = buscarPorId(dto.idCliente());
        String dni = normalizarCodigo(dto.dni());
        clienteDao.buscarPorDni(dni)
                .filter(encontrado -> !encontrado.getIdCliente().equals(dto.idCliente()))
                .ifPresent(encontrado -> {
                    throw new ValidacionException("Ya existe un cliente con ese DNI.");
                });
        Cliente cliente = construir(dto, dni, actual.isActivo(), actual.getFechaRegistro());
        cliente.validar();
        return clienteDao.actualizar(cliente);
    }

    public boolean eliminar(Long idCliente) {
        Cliente cliente = buscarPorId(idCliente);
        cliente.setActivo(false);
        return clienteDao.actualizar(cliente);
    }

    public boolean cambiarActivo(Long idCliente, boolean activo) {
        Cliente cliente = buscarPorId(idCliente);
        cliente.setActivo(activo);
        return clienteDao.actualizar(cliente);
    }

    public Cliente buscarPorId(Long idCliente) {
        Validador.identificadorRequerido(idCliente, "cliente");
        return clienteDao.buscarPorId(idCliente)
                .orElseThrow(() -> new EntidadNoEncontradaException("cliente", idCliente));
    }

    public List<Cliente> buscar(String texto) {
        return clienteDao.buscarPorTexto(texto);
    }

    public List<Cliente> listarTodos() {
        return clienteDao.listarTodos();
    }

    public List<Cliente> listarActivos() {
        return clienteDao.listarActivos();
    }

    private Cliente construir(
            ClienteFormularioDto dto, String dni, boolean activo, LocalDateTime fechaRegistro) {
        String correo = Validador.textoOpcional(dto.correoElectronico());
        Validador.correo(correo);
        return new Cliente(
                dto.idCliente(),
                Validador.textoRequerido(dto.nombre(), "nombre"),
                Validador.textoRequerido(dto.apellido(), "apellido"),
                Validador.textoRequerido(dto.telefono(), "teléfono"),
                correo == null ? null : correo.toLowerCase(Locale.ROOT),
                activo,
                dni,
                Validador.textoOpcional(dto.direccion()),
                fechaRegistro);
    }

    private String normalizarCodigo(String valor) {
        return Validador.textoRequerido(valor, "DNI").toUpperCase(Locale.ROOT);
    }
}
