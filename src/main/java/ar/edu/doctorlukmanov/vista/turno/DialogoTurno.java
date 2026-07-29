package ar.edu.doctorlukmanov.vista.turno;

import ar.edu.doctorlukmanov.dto.TurnoFormularioDto;
import ar.edu.doctorlukmanov.excepcion.ClinicaException;
import ar.edu.doctorlukmanov.modelo.Cliente;
import ar.edu.doctorlukmanov.modelo.Gato;
import ar.edu.doctorlukmanov.modelo.Turno;
import ar.edu.doctorlukmanov.modelo.Veterinario;
import ar.edu.doctorlukmanov.vista.componentes.Dialogos;
import ar.edu.doctorlukmanov.vista.componentes.FormularioUtil;
import ar.edu.doctorlukmanov.vista.componentes.SelectorFechaHora;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public final class DialogoTurno {

    private DialogoTurno() {
    }

    public static boolean mostrar(
            Component padre,
            Turno turno,
            Gato gatoPreferido,
            List<Cliente> clientes,
            List<Gato> gatos,
            List<Veterinario> veterinarios,
            Consumer<TurnoFormularioDto> guardar) {
        JComboBox<Cliente> cliente = new JComboBox<>(clientes.toArray(Cliente[]::new));
        JComboBox<Gato> gato = new JComboBox<>();
        JComboBox<Veterinario> veterinario = new JComboBox<>(veterinarios.toArray(Veterinario[]::new));
        SelectorFechaHora fechaHora = new SelectorFechaHora();
        JTextField motivo = new JTextField(turno == null ? "" : turno.getMotivo(), 25);
        JTextArea observaciones = new JTextArea(
                turno == null || turno.getObservaciones() == null ? "" : turno.getObservaciones(), 4, 25);

        Runnable cargarGatos = () -> {
            Cliente seleccionado = (Cliente) cliente.getSelectedItem();
            Gato anterior = (Gato) gato.getSelectedItem();
            gato.removeAllItems();
            gatos.stream()
                    .filter(item -> seleccionado != null && item.getIdCliente().equals(seleccionado.getIdCliente()))
                    .forEach(gato::addItem);
            Gato objetivo = anterior != null ? anterior : gatoPreferido;
            if (objetivo != null) {
                gatos.stream().filter(item -> item.getIdGato().equals(objetivo.getIdGato()))
                        .findFirst().ifPresent(gato::setSelectedItem);
            }
        };

        Gato gatoActual = turno == null ? gatoPreferido : gatos.stream()
                .filter(item -> item.getIdGato().equals(turno.getIdGato())).findFirst().orElse(gatoPreferido);
        if (gatoActual != null) {
            clientes.stream().filter(item -> item.getIdCliente().equals(gatoActual.getIdCliente()))
                    .findFirst().ifPresent(cliente::setSelectedItem);
        }
        cargarGatos.run();
        cliente.addActionListener(evento -> cargarGatos.run());
        if (turno != null) {
            fechaHora.setValor(turno.getFechaHora());
            veterinarios.stream().filter(item -> item.getIdVeterinario().equals(turno.getIdVeterinario()))
                    .findFirst().ifPresent(veterinario::setSelectedItem);
        }

        JPanel formulario = new JPanel(new GridBagLayout());
        FormularioUtil.agregarFila(formulario, 0, "Cliente *", cliente);
        FormularioUtil.agregarFila(formulario, 1, "Gato *", gato);
        FormularioUtil.agregarFila(formulario, 2, "Veterinario *", veterinario);
        FormularioUtil.agregarFila(formulario, 3, "Fecha y hora *", fechaHora);
        FormularioUtil.agregarFila(formulario, 4, "Motivo *", motivo);
        FormularioUtil.agregarFila(formulario, 5, "Observaciones", FormularioUtil.areaDesplazable(observaciones));
        formulario.setPreferredSize(new Dimension(580, 390));

        while (JOptionPane.showConfirmDialog(
                padre,
                formulario,
                turno == null ? "Programar turno" : "Editar turno",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                Gato gatoSeleccionado = (Gato) gato.getSelectedItem();
                Veterinario veterinarioSeleccionado = (Veterinario) veterinario.getSelectedItem();
                guardar.accept(new TurnoFormularioDto(
                        gatoSeleccionado == null ? null : gatoSeleccionado.getIdGato(),
                        veterinarioSeleccionado == null ? null : veterinarioSeleccionado.getIdVeterinario(),
                        fechaHora.getValor(),
                        motivo.getText(),
                        observaciones.getText()));
                return true;
            } catch (ClinicaException ex) {
                Dialogos.error(padre, ex.getMessage());
            }
        }
        return false;
    }
}
