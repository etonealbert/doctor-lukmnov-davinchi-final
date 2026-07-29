package ar.edu.doctorlukmanov.vista.turno;

import ar.edu.doctorlukmanov.dto.CierreAtencionDto;
import ar.edu.doctorlukmanov.dto.DetalleTratamientoDto;
import ar.edu.doctorlukmanov.excepcion.ClinicaException;
import ar.edu.doctorlukmanov.excepcion.ValidacionException;
import ar.edu.doctorlukmanov.modelo.Tratamiento;
import ar.edu.doctorlukmanov.modelo.Turno;
import ar.edu.doctorlukmanov.vista.componentes.Dialogos;
import ar.edu.doctorlukmanov.vista.componentes.FormularioUtil;
import ar.edu.doctorlukmanov.vista.componentes.ModeloTablaNoEditable;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public final class DialogoCompletarTurno {

    private DialogoCompletarTurno() {
    }

    public static boolean mostrar(
            Component padre,
            Turno turno,
            String resumen,
            List<Tratamiento> tratamientos,
            Consumer<CierreAtencionDto> completar) {
        JTextArea diagnostico = new JTextArea(3, 30);
        JTextField peso = new JTextField(12);
        JTextField temperatura = new JTextField(12);
        JTextArea observaciones = new JTextArea(3, 30);
        JTextArea indicaciones = new JTextArea(3, 30);
        JPanel datos = new JPanel(new GridBagLayout());
        FormularioUtil.agregarFila(datos, 0, "Diagnóstico *", FormularioUtil.areaDesplazable(diagnostico));
        FormularioUtil.agregarFila(datos, 1, "Peso registrado (kg)", peso);
        FormularioUtil.agregarFila(datos, 2, "Temperatura (°C)", temperatura);
        FormularioUtil.agregarFila(datos, 3, "Observaciones clínicas",
                FormularioUtil.areaDesplazable(observaciones));
        FormularioUtil.agregarFila(datos, 4, "Indicaciones", FormularioUtil.areaDesplazable(indicaciones));

        List<DetalleTratamientoDto> detalles = new ArrayList<>();
        ModeloTablaNoEditable modelo = new ModeloTablaNoEditable(new Object[]{
            "Tratamiento", "Dosis", "Frecuencia", "Duración (días)", "Cantidad",
            "Precio aplicado", "Observaciones"
        });
        JTable tabla = new JTable(modelo);
        JPanel accionesTratamiento = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton agregar = new JButton("Agregar tratamiento");
        JButton quitar = new JButton("Quitar tratamiento");
        agregar.addActionListener(evento -> mostrarDetalle(padre, tratamientos).ifPresent(detalle -> {
            boolean duplicado = detalles.stream()
                    .anyMatch(actual -> actual.idTratamiento().equals(detalle.idTratamiento()));
            if (duplicado) {
                Dialogos.error(padre, "El tratamiento ya fue agregado a la atención.");
                return;
            }
            detalles.add(detalle);
            Tratamiento tratamiento = tratamientos.stream()
                    .filter(item -> item.getIdTratamiento().equals(detalle.idTratamiento()))
                    .findFirst().orElseThrow();
            modelo.addRow(new Object[]{
                tratamiento.getNombre(), detalle.dosis(), detalle.frecuencia(), detalle.duracionDias(),
                detalle.cantidad(), detalle.precioAplicado(), detalle.observaciones()
            });
        }));
        quitar.addActionListener(evento -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                Dialogos.error(padre, "Seleccione un tratamiento para quitar.");
                return;
            }
            int indice = tabla.convertRowIndexToModel(fila);
            detalles.remove(indice);
            modelo.removeRow(indice);
        });
        accionesTratamiento.add(agregar);
        accionesTratamiento.add(quitar);

        JPanel tratamientosPanel = new JPanel(new BorderLayout(5, 5));
        tratamientosPanel.add(new JLabel("Tratamientos aplicados"), BorderLayout.NORTH);
        tratamientosPanel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        tratamientosPanel.add(accionesTratamiento, BorderLayout.SOUTH);

        JPanel contenido = new JPanel(new BorderLayout(8, 8));
        contenido.add(new JLabel("<html>" + resumen + "</html>"), BorderLayout.NORTH);
        contenido.add(datos, BorderLayout.WEST);
        contenido.add(tratamientosPanel, BorderLayout.CENTER);
        contenido.setPreferredSize(new Dimension(980, 520));

        while (JOptionPane.showConfirmDialog(
                padre,
                contenido,
                "Completar turno y registrar atención",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                completar.accept(new CierreAtencionDto(
                        turno.getIdTurno(),
                        diagnostico.getText(),
                        FormularioUtil.decimalOpcional(peso.getText(), "peso registrado"),
                        FormularioUtil.decimalOpcional(temperatura.getText(), "temperatura"),
                        observaciones.getText(),
                        indicaciones.getText(),
                        detalles));
                return true;
            } catch (ClinicaException ex) {
                Dialogos.error(padre, ex);
            }
        }
        return false;
    }

    private static Optional<DetalleTratamientoDto> mostrarDetalle(
            Component padre, List<Tratamiento> tratamientos) {
        if (tratamientos.isEmpty()) {
            Dialogos.error(padre, "No hay tratamientos activos disponibles.");
            return Optional.empty();
        }
        JComboBox<Tratamiento> tratamiento = new JComboBox<>(tratamientos.toArray(Tratamiento[]::new));
        JTextField dosis = new JTextField(20);
        JTextField frecuencia = new JTextField(20);
        JTextField duracion = new JTextField(20);
        JTextField cantidad = new JTextField("1", 20);
        JTextField precio = new JTextField(20);
        JTextArea observaciones = new JTextArea(3, 20);
        tratamiento.addActionListener(evento -> {
            Tratamiento seleccionado = (Tratamiento) tratamiento.getSelectedItem();
            if (seleccionado != null && seleccionado.getPrecioReferencia() != null) {
                precio.setText(seleccionado.getPrecioReferencia().toPlainString());
            }
        });
        Tratamiento inicial = (Tratamiento) tratamiento.getSelectedItem();
        if (inicial != null && inicial.getPrecioReferencia() != null) {
            precio.setText(inicial.getPrecioReferencia().toPlainString());
        }

        JPanel formulario = new JPanel(new GridBagLayout());
        FormularioUtil.agregarFila(formulario, 0, "Tratamiento *", tratamiento);
        FormularioUtil.agregarFila(formulario, 1, "Dosis", dosis);
        FormularioUtil.agregarFila(formulario, 2, "Frecuencia", frecuencia);
        FormularioUtil.agregarFila(formulario, 3, "Duración (días)", duracion);
        FormularioUtil.agregarFila(formulario, 4, "Cantidad *", cantidad);
        FormularioUtil.agregarFila(formulario, 5, "Precio aplicado", precio);
        FormularioUtil.agregarFila(formulario, 6, "Observaciones",
                FormularioUtil.areaDesplazable(observaciones));

        while (JOptionPane.showConfirmDialog(
                padre, formulario, "Agregar tratamiento", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                Tratamiento seleccionado = (Tratamiento) tratamiento.getSelectedItem();
                if (seleccionado == null) {
                    throw new ValidacionException("Seleccione un tratamiento.");
                }
                BigDecimal cantidadValor = FormularioUtil.decimalOpcional(cantidad.getText(), "cantidad");
                if (cantidadValor == null) {
                    throw new ValidacionException("La cantidad es obligatoria.");
                }
                BigDecimal precioValor = FormularioUtil.decimalOpcional(precio.getText(), "precio aplicado");
                return Optional.of(new DetalleTratamientoDto(
                        seleccionado.getIdTratamiento(),
                        dosis.getText(),
                        frecuencia.getText(),
                        FormularioUtil.enteroOpcional(duracion.getText(), "duración"),
                        observaciones.getText(),
                        cantidadValor,
                        precioValor == null ? BigDecimal.ZERO : precioValor));
            } catch (ClinicaException ex) {
                Dialogos.error(padre, ex);
            }
        }
        return Optional.empty();
    }
}
